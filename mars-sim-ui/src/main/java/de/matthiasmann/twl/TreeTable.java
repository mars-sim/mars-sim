/*
 * Copyright (c) 2008-2013, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.matthiasmann.twl;

import de.matthiasmann.twl.model.BooleanModel;
import de.matthiasmann.twl.model.TreeTableModel;
import de.matthiasmann.twl.model.TreeTableNode;
import de.matthiasmann.twl.utils.CallbackSupport;
import de.matthiasmann.twl.utils.HashEntry;
import de.matthiasmann.twl.utils.SizeSequence;

/**
 * A Tree+Table widget.
 *
 * It does not have a {@link TableSelectionManager} by default. To make the
 * table entries selectable you need to install a selection manager:
 * {@link #setSelectionManager(de.matthiasmann.twl.TableSelectionManager) } or
 * {@link #setDefaultSelectionManager() }
 * 
 * @author Matthias Mann
 */
public class TreeTable extends TableBase {
    
    public interface ExpandListener {
        public void nodeExpanded(int row, TreeTableNode node);
        public void nodeCollapsed(int row, TreeTableNode node);
    }
    
    private final ModelChangeListener modelChangeListener;
    private final TreeLeafCellRenderer leafRenderer;
    private final TreeNodeCellRenderer nodeRenderer;

    private NodeState[] nodeStateTable;
    private int nodeStateTableSize;
    TreeTableModel model;
    private NodeState rootNodeState;
    private ExpandListener[] expandListeners;

    @SuppressWarnings("LeakingThisInConstructor")
    public TreeTable() {
        modelChangeListener = new ModelChangeListener();
        nodeStateTable = new NodeState[64];
        leafRenderer = new TreeLeafCellRenderer();
        nodeRenderer = new TreeNodeCellRenderer();
        hasCellWidgetCreators = true;

        ActionMap am = getOrCreateActionMap();
        am.addMapping("expandLeadRow", this, "setLeadRowExpanded", new Object[] { Boolean.TRUE }, ActionMap.FLAG_ON_PRESSED);
        am.addMapping("collapseLeadRow", this, "setLeadRowExpanded", new Object[] { Boolean.FALSE }, ActionMap.FLAG_ON_PRESSED);
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public TreeTable(TreeTableModel model) {
        this();
        setModel(model);
    }

    public void setModel(TreeTableModel model) {
        if(this.model != null) {
            this.model.removeChangeListener(modelChangeListener);
        }
        this.columnHeaderModel = model;
        this.model = model;
        this.nodeStateTable = new NodeState[64];
        this.nodeStateTableSize = 0;
        if(this.model != null) {
            this.model.addChangeListener(modelChangeListener);
            this.rootNodeState = createNodeState(model);
            this.rootNodeState.level = -1;
            this.rootNodeState.expanded = true;
            this.rootNodeState.initChildSizes();
            this.numRows = computeNumRows();
            this.numColumns = model.getNumColumns();
        } else {
            this.rootNodeState = null;
            this.numRows = 0;
            this.numColumns = 0;
        }
        modelAllChanged();
        invalidateLayout();
    }

    public void addExpandListener(ExpandListener listener) {
        expandListeners = CallbackSupport.addCallbackToList(expandListeners, listener, ExpandListener.class);
    }
    
    public void removeExpandListener(ExpandListener listener) {
        expandListeners = CallbackSupport.removeCallbackFromList(expandListeners, listener);
    }
    
    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        applyThemeTreeTable(themeInfo);
    }

    protected void applyThemeTreeTable(ThemeInfo themeInfo) {
        applyCellRendererTheme(leafRenderer);
        applyCellRendererTheme(nodeRenderer);
    }

    /**
     * Computes the row for the given node in the TreeTable.
     *
     * @param node the node to locate
     * @return the row in the table or -1 if the node is not visible
     */
    public int getRowFromNode(TreeTableNode node) {
        int position = -1;
        TreeTableNode parent = node.getParent();
        while(parent != null) {
            NodeState ns = HashEntry.get(nodeStateTable, parent);
            if(ns == null) {
                // parent was not yet expanded, or not part of tree
                return -1;
            }
            int idx = parent.getChildIndex(node);
            if(idx < 0) {
                // node is not part of the tree
                return -1;
            }
            if(ns.childSizes == null) {
                if(ns.expanded) {
                    ns.initChildSizes();
                } else {
                    return -1;
                }
            }
            idx = ns.childSizes.getPosition(idx);
            position += idx + 1;
            node = parent;
            parent = node.getParent();
        }
        return position;
    }

    public int getRowFromNodeExpand(TreeTableNode node) {
        if(node.getParent() != null) {
            TreeTableNode parent = node.getParent();
            int row = getRowFromNodeExpand(parent);
            int idx = parent.getChildIndex(node);
            NodeState ns = getOrCreateNodeState(parent);
            ns.setValue(true);
            if(ns.childSizes == null) {
                ns.initChildSizes();
            }
            return row + 1 + ns.childSizes.getPosition(idx);
        } else {
            return -1;
        }
    }

    public TreeTableNode getNodeFromRow(int row) {
        NodeState ns = rootNodeState;
        for(;;) {
            int idx;
            if(ns.childSizes == null) {
                idx = Math.min(ns.key.getNumChildren()-1, row);
                row -= idx + 1;
            } else {
                idx = ns.childSizes.getIndex(row);
                row -= ns.childSizes.getPosition(idx) + 1;
            }
            if(row < 0) {
                return ns.key.getChild(idx);
            }
            assert ns.children[idx] != null;
            ns = ns.children[idx];
        }
    }

    public void collapseAll() {
        for(int i=0 ; i<nodeStateTable.length ; ++i) {
            for(NodeState ns=nodeStateTable[i] ; ns!=null ; ns=ns.next()) {
                if(ns != rootNodeState) {
                    ns.setValue(false);
                }
            }
        }
    }

    public boolean isRowExpanded(int row) {
        checkRowIndex(row);
        TreeTableNode node = getNodeFromRow(row);
        NodeState ns = HashEntry.get(nodeStateTable, node);
        return (ns != null) && ns.expanded;
    }

    public void setRowExpanded(int row, boolean expanded) {
        checkRowIndex(row);
        TreeTableNode node = getNodeFromRow(row);
        NodeState state = getOrCreateNodeState(node);
        state.setValue(expanded);
    }

    public void setLeadRowExpanded(boolean expanded) {
        TableSelectionManager sm = getSelectionManager();
        if(sm != null) {
            int row = sm.getLeadRow();
            if(row >= 0 && row < numRows) {
                setRowExpanded(row, expanded);
            }
        }
    }

    protected NodeState getOrCreateNodeState(TreeTableNode node) {
        NodeState ns = HashEntry.get(nodeStateTable, node);
        if(ns == null) {
            ns = createNodeState(node);
        }
        return ns;
    }

    protected NodeState createNodeState(TreeTableNode node) {
        TreeTableNode parent = node.getParent();
        NodeState nsParent = null;
        if(parent != null) {
            nsParent = HashEntry.get(nodeStateTable, parent);
            assert nsParent != null;
        }
        NodeState newNS = new NodeState(node, nsParent);
        nodeStateTable = HashEntry.maybeResizeTable(nodeStateTable, ++nodeStateTableSize);
        HashEntry.insertEntry(nodeStateTable, newNS);
        return newNS;
    }

    protected void expandedChanged(NodeState ns) {
        TreeTableNode node = ns.key;
        int count = ns.getChildRows();
        int size = ns.expanded ? count : 0;
        
        TreeTableNode parent = node.getParent();
        while(parent != null) {
            NodeState nsParent = HashEntry.get(nodeStateTable, parent);
            if(nsParent.childSizes == null) {
                nsParent.initChildSizes();
            }
            
            int idx = nsParent.key.getChildIndex(node);
            nsParent.childSizes.setSize(idx, size + 1);
            size = nsParent.childSizes.getEndPosition();

            node = parent;
            parent = node.getParent();
        }

        numRows = computeNumRows();
        int row = getRowFromNode(ns.key);
        if(ns.expanded) {
            modelRowsInserted(row+1, count);
        } else {
            modelRowsDeleted(row+1, count);
        }
        modelRowsChanged(row, 1);

        if(ns.expanded) {
            ScrollPane scrollPane = ScrollPane.getContainingScrollPane(this);
            if(scrollPane != null) {
                scrollPane.validateLayout();
                int rowStart = getRowStartPosition(row);
                int rowEnd = getRowEndPosition(row + count);
                int height = rowEnd - rowStart;
                scrollPane.scrollToAreaY(rowStart, height, rowHeight/2);
            }
        }
        
        if(expandListeners != null) {
            for(ExpandListener el : expandListeners) {
                if(ns.expanded) {
                    el.nodeExpanded(row, ns.key);
                } else {
                    el.nodeCollapsed(row, ns.key);
                }
            }
        }
    }

    protected int computeNumRows() {
        return rootNodeState.childSizes.getEndPosition();
    }

    @Override
    protected Object getCellData(int row, int column, TreeTableNode node) {
        if(node == null) {
            node = getNodeFromRow(row);
        }
        return node.getData(column);
    }

    @Override
    protected CellRenderer getCellRenderer(int row, int col, TreeTableNode node) {
        if(node == null) {
            node = getNodeFromRow(row);
        }
        if(col == 0) {
            Object data = node.getData(col);
            if(node.isLeaf()) {
                leafRenderer.setCellData(row, col, data, node);
                return leafRenderer;
            }
            NodeState nodeState = getOrCreateNodeState(node);
            nodeRenderer.setCellData(row, col, data, nodeState);
            return nodeRenderer;
        }
        return super.getCellRenderer(row, col, node);
    }

    @Override
    protected Object getTooltipContentFromRow(int row, int column) {
        TreeTableNode node = getNodeFromRow(row);
        if(node != null) {
            return node.getTooltipContent(column);
        }
        return null;
    }

    private boolean updateParentSizes(NodeState ns) {
        while(ns.expanded && ns.parent != null) {
            NodeState parent = ns.parent;
            int idx = parent.key.getChildIndex(ns.key);
            assert parent.childSizes.size() == parent.key.getNumChildren();
            parent.childSizes.setSize(idx, ns.getChildRows() + 1);
            ns = parent;
        }
        numRows = computeNumRows();
        return ns.parent == null;
    }
    
    protected void modelNodesAdded(TreeTableNode parent, int idx, int count) {
        NodeState ns = HashEntry.get(nodeStateTable, parent);
        // if ns is null then this node has not yet been displayed
        if(ns != null) {
            if(ns.childSizes != null) {
                assert idx <= ns.childSizes.size();
                ns.childSizes.insert(idx, count);
                assert ns.childSizes.size() == parent.getNumChildren();
            }
            if(ns.children != null) {
                NodeState[] newChilds = new NodeState[parent.getNumChildren()];
                System.arraycopy(ns.children, 0, newChilds, 0, idx);
                System.arraycopy(ns.children, idx, newChilds, idx+count, ns.children.length - idx);
                ns.children = newChilds;
            }
            if(updateParentSizes(ns)) {
                int row = getRowFromNode(parent.getChild(idx));
                assert row < numRows;
                modelRowsInserted(row, count);
            }
        }
    }

    protected void recursiveRemove(NodeState ns) {
        if(ns != null) {
            --nodeStateTableSize;
            HashEntry.remove(nodeStateTable, ns);
            if(ns.children != null) {
                for(NodeState nsChild : ns.children) {
                    recursiveRemove(nsChild);
                }
            }
        }
    }

    protected void modelNodesRemoved(TreeTableNode parent, int idx, int count) {
        NodeState ns = HashEntry.get(nodeStateTable, parent);
        // if ns is null then this node has not yet been displayed
        if(ns != null) {
            int rowsBase = getRowFromNode(parent) + 1;
            int rowsStart = rowsBase + idx;
            int rowsEnd = rowsBase + idx + count;
            if(ns.childSizes != null) {
                assert ns.childSizes.size() == parent.getNumChildren() + count;
                rowsStart = rowsBase + ns.childSizes.getPosition(idx);
                rowsEnd = rowsBase + ns.childSizes.getPosition(idx + count);
                ns.childSizes.remove(idx, count);
                assert ns.childSizes.size() == parent.getNumChildren();
            }
            if(ns.children != null) {
                for(int i=0 ; i<count ; i++) {
                    recursiveRemove(ns.children[idx+i]);
                }
                int numChildren = parent.getNumChildren();
                if(numChildren > 0) {
                    NodeState[] newChilds = new NodeState[numChildren];
                    System.arraycopy(ns.children, 0, newChilds, 0, idx);
                    System.arraycopy(ns.children, idx+count, newChilds, idx, newChilds.length - idx);
                    ns.children = newChilds;
                } else {
                    ns.children = null;
                }
            }
            if(updateParentSizes(ns)) {
                modelRowsDeleted(rowsStart, rowsEnd - rowsStart);
            }
        }
    }

    protected boolean isVisible(NodeState ns) {
        while(ns.expanded && ns.parent != null) {
            ns = ns.parent;
        }
        return ns.expanded;
    }
    
    protected void modelNodesChanged(TreeTableNode parent, int idx, int count) {
        NodeState ns = HashEntry.get(nodeStateTable, parent);
        // if ns is null then this node has not yet been displayed
        if(ns != null && isVisible(ns)) {
            int rowsBase = getRowFromNode(parent) + 1;
            int rowsStart = rowsBase + idx;
            int rowsEnd = rowsBase + idx + count;
            if(ns.childSizes != null) {
                rowsStart = rowsBase + ns.childSizes.getPosition(idx);
                rowsEnd = rowsBase + ns.childSizes.getPosition(idx + count);
            }
            modelRowsChanged(rowsStart, rowsEnd - rowsStart);
        }
    }

    protected class ModelChangeListener implements TreeTableModel.ChangeListener {
        public void nodesAdded(TreeTableNode parent, int idx, int count) {
            modelNodesAdded(parent, idx, count);
        }
        public void nodesRemoved(TreeTableNode parent, int idx, int count) {
            modelNodesRemoved(parent, idx, count);
        }
        public void nodesChanged(TreeTableNode parent, int idx, int count) {
            modelNodesChanged(parent, idx, count);
        }
        public void columnInserted(int idx, int count) {
            numColumns = model.getNumColumns();
            modelColumnsInserted(idx, count);
        }
        public void columnDeleted(int idx, int count) {
            numColumns = model.getNumColumns();
            modelColumnsDeleted(idx, count);
        }
        public void columnHeaderChanged(int column) {
            modelColumnHeaderChanged(column);
        }
    }

    protected class NodeState extends HashEntry<TreeTableNode, NodeState> implements BooleanModel {
        final NodeState parent;
        boolean expanded;
        boolean hasNoChildren;
        SizeSequence childSizes;
        NodeState[] children;
        Runnable[] callbacks;
        int level;

        @SuppressWarnings("LeakingThisInConstructor")
        public NodeState(TreeTableNode key, NodeState parent) {
            super(key);
            this.parent = parent;
            this.level = (parent != null) ? parent.level + 1 : 0;

            if(parent != null) {
                if(parent.children == null) {
                    parent.children = new NodeState[parent.key.getNumChildren()];
                }
                parent.children[parent.key.getChildIndex(key)] = this;
            }
        }

        public void addCallback(Runnable callback) {
            callbacks = CallbackSupport.addCallbackToList(callbacks, callback, Runnable.class);
        }

        public void removeCallback(Runnable callback) {
            callbacks = CallbackSupport.removeCallbackFromList(callbacks, callback);
        }

        public boolean getValue() {
            return expanded;
        }

        public void setValue(boolean value) {
            if(this.expanded != value) {
                this.expanded = value;
                expandedChanged(this);
                CallbackSupport.fireCallbacks(callbacks);
            }
        }

        void initChildSizes() {
            childSizes = new SizeSequence();
            childSizes.setDefaultValue(1);
            childSizes.initializeAll(key.getNumChildren());
        }

        int getChildRows() {
            if(childSizes != null) {
                return childSizes.getEndPosition();
            }
            int childCount = key.getNumChildren();
            hasNoChildren = childCount == 0;
            return childCount;
        }

        boolean hasNoChildren() {
            return hasNoChildren;
        }
    }

    static int getLevel(TreeTableNode node) {
        int level = -2;
        while(node != null) {
            level++;
            node = node.getParent();
        }
        return level;
    }

    class TreeLeafCellRenderer implements CellRenderer, CellWidgetCreator {
        protected int treeIndent;
        protected int level;
        protected Dimension treeButtonSize = new Dimension(5, 5);
        protected CellRenderer subRenderer;

        public TreeLeafCellRenderer() {
            setClip(true);
        }

        public void applyTheme(ThemeInfo themeInfo) {
            treeIndent = themeInfo.getParameter("treeIndent", 10);
            treeButtonSize = themeInfo.getParameterValue("treeButtonSize", true, Dimension.class, Dimension.ZERO);
        }

        public String getTheme() {
            return getClass().getSimpleName();
        }
        
        public void setCellData(int row, int column, Object data) {
            throw new UnsupportedOperationException("Don't call this method");
        }

        public void setCellData(int row, int column, Object data, TreeTableNode node) {
            level = getLevel(node);
            setSubRenderer(row, column, data);
        }

        protected int getIndentation() {
            return level * treeIndent + treeButtonSize.getX();
        }

        protected void setSubRenderer(int row, int column, Object colData) {
            subRenderer = getCellRenderer(colData, column);
            if(subRenderer != null) {
                subRenderer.setCellData(row, column, colData);
            }
        }

        public int getColumnSpan() {
            return (subRenderer != null) ? subRenderer.getColumnSpan() : 1;
        }

        public int getPreferredHeight() {
            if(subRenderer != null) {
                return Math.max(treeButtonSize.getY(), subRenderer.getPreferredHeight());
            }
            return treeButtonSize.getY();
        }

        public Widget getCellRenderWidget(int x, int y, int width, int height, boolean isSelected) {
            if(subRenderer != null) {
                int indent = getIndentation();
                Widget widget = subRenderer.getCellRenderWidget(
                        x + indent, y, Math.max(0, width-indent), height, isSelected);
                return widget;
            }
            return null;
        }
        
        public Widget updateWidget(Widget existingWidget) {
            if(subRenderer instanceof CellWidgetCreator) {
                CellWidgetCreator subCreator = (CellWidgetCreator)subRenderer;
                return subCreator.updateWidget(existingWidget);
            }
            return null;
        }
        
        public void positionWidget(Widget widget, int x, int y, int w, int h) {
            if(subRenderer instanceof CellWidgetCreator) {
                CellWidgetCreator subCreator = (CellWidgetCreator)subRenderer;
                int indent = level * treeIndent;
                subCreator.positionWidget(widget, x+indent, y, Math.max(0, w-indent), h);
            }
        }
    }
    
    static class WidgetChain extends Widget {
        final ToggleButton expandButton;
        Widget userWidget;
        
        WidgetChain() {
            setTheme("");
            expandButton = new ToggleButton();
            expandButton.setTheme("treeButton");
            add(expandButton);
        }

        void setUserWidget(Widget userWidget) {
            if(this.userWidget != userWidget) {
                if(this.userWidget != null) {
                    removeChild(1);
                }
                this.userWidget = userWidget;
                if(userWidget != null) {
                    insertChild(userWidget, 1);
                }
            }
        }
    }

    class TreeNodeCellRenderer extends TreeLeafCellRenderer {
        private NodeState nodeState;

        @Override
        public Widget updateWidget(Widget existingWidget) {
            if(subRenderer instanceof CellWidgetCreator) {
                CellWidgetCreator subCreator = (CellWidgetCreator)subRenderer;
                WidgetChain widgetChain = null;
                if(existingWidget instanceof WidgetChain) {
                    widgetChain = (WidgetChain)existingWidget;
                }
                if(nodeState.hasNoChildren()) {
                    if(widgetChain != null) {
                        existingWidget = null;
                    }
                    return subCreator.updateWidget(existingWidget);
                }
                if(widgetChain == null) {
                    widgetChain = new WidgetChain();
                }
                widgetChain.expandButton.setModel(nodeState);
                widgetChain.setUserWidget(subCreator.updateWidget(widgetChain.userWidget));
                return widgetChain;
            }
            if(nodeState.hasNoChildren()) {
                return null;
            }
            ToggleButton tb = (ToggleButton)existingWidget;
            if(tb == null) {
                tb = new ToggleButton();
                tb.setTheme("treeButton");
            }
            tb.setModel(nodeState);
            return tb;
        }

        @Override
        public void positionWidget(Widget widget, int x, int y, int w, int h) {
            int indent = level * treeIndent;
            int availWidth = Math.max(0, w-indent);
            int expandButtonWidth = Math.min(availWidth, treeButtonSize.getX());
            widget.setPosition(x + indent, y + (h-treeButtonSize.getY())/2);
            if(subRenderer instanceof CellWidgetCreator) {
                CellWidgetCreator subCreator = (CellWidgetCreator)subRenderer;
                WidgetChain widgetChain = (WidgetChain)widget;
                ToggleButton expandButton = widgetChain.expandButton;
                widgetChain.setSize(Math.max(0, w-indent), h);
                expandButton.setSize(expandButtonWidth, treeButtonSize.getY());
                if(widgetChain.userWidget != null) {
                    subCreator.positionWidget(widgetChain.userWidget,
                            expandButton.getRight(), y, widget.getWidth(), h);
                }
            } else {
                widget.setSize(expandButtonWidth, treeButtonSize.getY());
            }
        }

        public void setCellData(int row, int column, Object data, NodeState nodeState) {
            assert nodeState != null;
            this.nodeState = nodeState;
            setSubRenderer(row, column, data);
            level = nodeState.level;
        }
    }
}

/*
 * Copyright (c) 2008-2012, Matthias Mann
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

import de.matthiasmann.twl.model.AbstractListModel;
import de.matthiasmann.twl.model.AbstractTreeTableModel;
import de.matthiasmann.twl.model.AbstractTreeTableNode;
import de.matthiasmann.twl.model.ListModel;
import de.matthiasmann.twl.model.Property;
import de.matthiasmann.twl.model.PropertyList;
import de.matthiasmann.twl.model.SimplePropertyList;
import de.matthiasmann.twl.model.TreeTableModel;
import de.matthiasmann.twl.model.TreeTableNode;
import de.matthiasmann.twl.utils.TypeMapping;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A property sheet class
 * 
 * @author Matthias Mann
 */
public class PropertySheet extends TreeTable {

    public interface PropertyEditor {
        public Widget getWidget();
        public void valueChanged();
        public void preDestroy();
        public void setSelected(boolean selected);
        
        /**
         * Can be used to position the widget in a cell.
         * <p>If this method returns false, the table will position the widget itself.</p>
         *
         * <p>This method is responsible to call setPosition and setSize on the
         * widget or return false.</p>
         *
         * @param x the left edge of the cell
         * @param y the top edge of the cell
         * @param width the width of the cell
         * @param height the height of the cell
         * 
         * @return true if the position was changed by this method.
         */
        public boolean positionWidget(int x, int y, int width, int height);
    }

    public interface PropertyEditorFactory<T> {
        public PropertyEditor createEditor(Property<T> property);
    }

    private final SimplePropertyList rootList;
    private final PropertyListCellRenderer subListRenderer;
    private final CellRenderer editorRenderer;
    private final TypeMapping<PropertyEditorFactory<?>> factories;

    public PropertySheet() {
        this(new Model());
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    private PropertySheet(Model model) {
        super(model);
        this.rootList = new SimplePropertyList("<root>");
        this.subListRenderer = new PropertyListCellRenderer();
        this.editorRenderer = new EditorRenderer();
        this.factories = new TypeMapping<PropertyEditorFactory<?>>();
        rootList.addValueChangedCallback(new TreeGenerator(rootList, model));
        registerPropertyEditorFactory(String.class, new StringEditorFactory());
    }

    public SimplePropertyList getPropertyList() {
        return rootList;
    }

    public<T> void registerPropertyEditorFactory(Class<T> clazz, PropertyEditorFactory<T> factory) {
        if(clazz == null) {
            throw new NullPointerException("clazz");
        }
        if(factory == null) {
            throw new NullPointerException("factory");
        }
        factories.put(clazz, factory);
    }

    @Override
    public void setModel(TreeTableModel model) {
        if(model instanceof Model) {
            super.setModel(model);
        } else {
            throw new UnsupportedOperationException("Do not call this method");
        }
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        applyThemePropertiesSheet(themeInfo);
    }

    protected void applyThemePropertiesSheet(ThemeInfo themeInfo) {
        applyCellRendererTheme(subListRenderer);
        applyCellRendererTheme(editorRenderer);
    }

    @Override
    protected CellRenderer getCellRenderer(int row, int col, TreeTableNode node) {
        if(node == null) {
            node = getNodeFromRow(row);
        }
        if(node instanceof ListNode) {
            if(col == 0) {
                PropertyListCellRenderer cr = subListRenderer;
                NodeState nodeState = getOrCreateNodeState(node);
                cr.setCellData(row, col, node.getData(col), nodeState);
                return cr;
            } else {
                return null;
            }
        } else if(col == 0) {
            return super.getCellRenderer(row, col, node);
        } else {
            CellRenderer cr = editorRenderer;
            cr.setCellData(row, col, node.getData(col));
            return cr;
        }
    }

    @SuppressWarnings("unchecked")
    TreeTableNode createNode(TreeTableNode parent, Property<?> property) {
        if(property.getType() == PropertyList.class) {
            return new ListNode(parent, property);
        } else {
            Class<?> type = property.getType();
            PropertyEditorFactory factory = factories.get(type);
            if(factory != null) {
                PropertyEditor editor = factory.createEditor(property);
                if(editor != null) {
                    return new LeafNode(parent, property, editor);
                }
            } else {
                Logger.getLogger(PropertySheet.class.getName()).log(Level.WARNING, "No property editor factory for type {0}", type);
            }
            return null;
        }
    }

    interface PSTreeTableNode extends TreeTableNode {
        public void addChild(TreeTableNode parent);
        public void removeAllChildren();
    }

    static abstract class PropertyNode extends AbstractTreeTableNode implements Runnable, PSTreeTableNode {
        protected final Property<?> property;
        @SuppressWarnings("LeakingThisInConstructor")
        public PropertyNode(TreeTableNode parent, Property<?> property) {
            super(parent);
            this.property = property;
            property.addValueChangedCallback(this);
        }
        protected void removeCallback() {
            property.removeValueChangedCallback(this);
        }
        @Override
        public void removeAllChildren() {
            super.removeAllChildren();
        }
        public void addChild(TreeTableNode parent) {
            insertChild(parent, getNumChildren());
        }
    }

    class TreeGenerator implements Runnable {
        private final PropertyList list;
        private final PSTreeTableNode parent;

        public TreeGenerator(PropertyList list, PSTreeTableNode parent) {
            this.list = list;
            this.parent = parent;
        }
        public void run() {
            parent.removeAllChildren();
            addSubProperties();
        }
        void removeChildCallbacks(PSTreeTableNode parent) {
            for(int i=0,n=parent.getNumChildren() ; i<n ; ++i) {
                ((PropertyNode)parent.getChild(i)).removeCallback();
            }
        }
        void addSubProperties() {
            for(int i=0 ; i<list.getNumProperties() ; ++i) {
                TreeTableNode node = createNode(parent, list.getProperty(i));
                if(node != null) {
                    parent.addChild(node);
                }
            }
        }
    }

    static class LeafNode extends PropertyNode {
        private final PropertyEditor editor;

        public LeafNode(TreeTableNode parent, Property<?> property, PropertyEditor editor) {
            super(parent, property);
            this.editor = editor;
            setLeaf(true);
        }
        public Object getData(int column) {
            switch(column) {
            case 0: return property.getName();
            case 1: return editor;
            default: return "???";
            }
        }
        public void run() {
            editor.valueChanged();
            fireNodeChanged();
        }
    }

    class ListNode extends PropertyNode {
        protected final TreeGenerator treeGenerator;

        public ListNode(TreeTableNode parent, Property<?> property) {
            super(parent, property);
            this.treeGenerator = new TreeGenerator(
                    (PropertyList)property.getPropertyValue(), this);
            treeGenerator.run();
        }
        public Object getData(int column) {
            return property.getName();
        }
        public void run() {
            treeGenerator.run();
        }
        @Override
        protected void removeCallback() {
            super.removeCallback();
            treeGenerator.removeChildCallbacks(this);
        }
    }

    class PropertyListCellRenderer extends TreeNodeCellRenderer {
        private final Widget bgRenderer;
        private final Label textRenderer;

        public PropertyListCellRenderer() {
            bgRenderer = new Widget();
            textRenderer = new Label(bgRenderer.getAnimationState());
            textRenderer.setAutoSize(false);
            bgRenderer.add(textRenderer);
            bgRenderer.setTheme(getTheme());
        }
        @Override
        public int getColumnSpan() {
            return 2;
        }
        @Override
        public Widget getCellRenderWidget(int x, int y, int width, int height, boolean isSelected) {
            bgRenderer.setPosition(x, y);
            bgRenderer.setSize(width, height);
            int indent = getIndentation();
            textRenderer.setPosition(x + indent, y);
            textRenderer.setSize(Math.max(0, width-indent), height);
            bgRenderer.getAnimationState().setAnimationState(STATE_SELECTED, isSelected);
            return bgRenderer;
        }
        @Override
        public void setCellData(int row, int column, Object data, NodeState nodeState) {
            super.setCellData(row, column, data, nodeState);
            textRenderer.setText((String)data);
        }
        @Override
        protected void setSubRenderer(int row, int column, Object colData) {
        }
    }

    static class EditorRenderer implements CellRenderer, TreeTable.CellWidgetCreator {
        private PropertyEditor editor;

        public void applyTheme(ThemeInfo themeInfo) {
        }
        public Widget getCellRenderWidget(int x, int y, int width, int height, boolean isSelected) {
            editor.setSelected(isSelected);
            return null;
        }
        public int getColumnSpan() {
            return 1;
        }
        public int getPreferredHeight() {
            return editor.getWidget().getPreferredHeight();
        }
        public String getTheme() {
            return "PropertyEditorCellRender";
        }
        public void setCellData(int row, int column, Object data) {
            editor = (PropertyEditor)data;
        }
        public Widget updateWidget(Widget existingWidget) {
            return editor.getWidget();
        }
        public void positionWidget(Widget widget, int x, int y, int w, int h) {
            if(!editor.positionWidget(x, y, w, h)) {
                widget.setPosition(x, y);
                widget.setSize(w, h);
            }
        }
    }
    
    static class Model extends AbstractTreeTableModel implements PSTreeTableNode {
        public String getColumnHeaderText(int column) {
            switch(column) {
            case 0: return "Name";
            case 1: return "Value";
            default: return "???";
            }
        }
        public int getNumColumns() {
            return 2;
        }
        @Override
        public void removeAllChildren() {
            super.removeAllChildren();
        }
        public void addChild(TreeTableNode parent) {
            insertChild(parent, getNumChildren());
        }
    }

    static class StringEditor implements PropertyEditor, EditField.Callback {
        private final EditField editField;
        private final Property<String> property;

        @SuppressWarnings("LeakingThisInConstructor")
        public StringEditor(Property<String> property) {
            this.property = property;
            this.editField = new EditField();
            editField.addCallback(this);
            resetValue();
        }
        public Widget getWidget() {
            return editField;
        }
        public void valueChanged() {
            resetValue();
        }
        public void preDestroy() {
            editField.removeCallback(this);
        }
        public void setSelected(boolean selected) {
        }
        public void callback(int key) {
            if(key == Event.KEY_ESCAPE) {
                resetValue();
            } else if(!property.isReadOnly()) {
                try {
                    property.setPropertyValue(editField.getText());
                    editField.setErrorMessage(null);
                } catch (IllegalArgumentException ex) {
                    editField.setErrorMessage(ex.getMessage());
                }
            }
        }
        private void resetValue() {
            editField.setText(property.getPropertyValue());
            editField.setErrorMessage(null);
            editField.setReadOnly(property.isReadOnly());
        }
        public boolean positionWidget(int x, int y, int width, int height) {
            return false;
        }
    }
    static class StringEditorFactory implements PropertyEditorFactory<String> {
        public PropertyEditor createEditor(Property<String> property) {
            return new StringEditor(property);
        }
    }

    public static class ComboBoxEditor<T> implements PropertyEditor, Runnable {
        protected final ComboBox<T> comboBox;
        protected final Property<T> property;
        protected final ListModel<T> model;

        @SuppressWarnings({"LeakingThisInConstructor", "OverridableMethodCallInConstructor"})
        public ComboBoxEditor(Property<T> property, ListModel<T> model) {
            this.property = property;
            this.comboBox = new ComboBox<T>(model);
            this.model = model;
            comboBox.addCallback(this);
            resetValue();
        }
        public Widget getWidget() {
            return comboBox;
        }
        public void valueChanged() {
            resetValue();
        }
        public void preDestroy() {
            comboBox.removeCallback(this);
        }
        public void setSelected(boolean selected) {
        }
        public void run() {
            if(property.isReadOnly()) {
                resetValue();
            } else {
                int idx = comboBox.getSelected();
                if(idx >= 0) {
                    property.setPropertyValue(model.getEntry(idx));
                }
            }
        }
        protected void resetValue() {
            comboBox.setSelected(findEntry(property.getPropertyValue()));
        }
        protected int findEntry(T value) {
            for(int i=0,n=model.getNumEntries() ; i<n ; i++) {
                if(model.getEntry(i).equals(value)) {
                    return i;
                }
            }
            return -1;
        }
        public boolean positionWidget(int x, int y, int width, int height) {
            return false;
        }
    }
    public static class ComboBoxEditorFactory<T> implements PropertyEditorFactory<T> {
        private final ModelForwarder modelForwarder;
        public ComboBoxEditorFactory(ListModel<T> model) {
            this.modelForwarder = new ModelForwarder(model);
        }
        public ListModel<T> getModel() {
            return modelForwarder.getModel();
        }
        public void setModel(ListModel<T> model) {
            modelForwarder.setModel(model);
        }
        public PropertyEditor createEditor(Property<T> property) {
            return new ComboBoxEditor<T>(property, modelForwarder);
        }
        class ModelForwarder extends AbstractListModel<T> implements ListModel.ChangeListener {
            private ListModel<T> model;
            @SuppressWarnings("OverridableMethodCallInConstructor")
            public ModelForwarder(ListModel<T> model) {
                setModel(model);
            }
            public int getNumEntries() {
                return model.getNumEntries();
            }
            public T getEntry(int index) {
                return model.getEntry(index);
            }
            public Object getEntryTooltip(int index) {
                return model.getEntryTooltip(index);
            }
            public boolean matchPrefix(int index, String prefix) {
                return model.matchPrefix(index, prefix);
            }
            public ListModel<T> getModel() {
                return model;
            }
            public void setModel(ListModel<T> model) {
                if(this.model != null) {
                    this.model.removeChangeListener(this);
                }
                this.model = model;
                this.model.addChangeListener(this);
                fireAllChanged();
            }
            public void entriesInserted(int first, int last) {
                fireEntriesInserted(first, last);
            }
            public void entriesDeleted(int first, int last) {
                fireEntriesDeleted(first, last);
            }
            public void entriesChanged(int first, int last) {
                fireEntriesChanged(first, last);
            }
            public void allChanged() {
                fireAllChanged();
            }
        }
    }
}

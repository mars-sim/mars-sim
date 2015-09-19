/*
 * Copyright (c) 2008-2011, Matthias Mann
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A column oriented layout engine.
 *
 * <p>It's based on named columns and rows.<br>
 * Each row is constructed with a set of columns.<br>
 * Rows can be added to the implicit root panel or to sub panels.<br>
 * Panels can be removed and/or cleared to create dynamic settings.</p>
 *
 * @author Matthias Mann
 */
public class ColumnLayout extends DialogLayout {

    final ArrayList<Group> columnGroups;
    private final Panel rootPanel;
    private final HashMap<Columns, Columns> columns;

    public ColumnLayout() {
        this.columnGroups = new ArrayList<Group>();
        this.rootPanel = new Panel(null);
        this.columns = new HashMap<Columns, Columns>();

        setHorizontalGroup(createParallelGroup());
        setVerticalGroup(rootPanel.rows);
    }

    public final Panel getRootPanel() {
        return rootPanel;
    }

    /**
     * Returns the column layout for the specified list of columns.
     *
     * <p>A column name of {@code ""} or {@code "-"} is used to create a
     * flexible gap.</p>
     *
     * <p>Layouts are merged starting with the first column if that column
     * name is already in use. Merged column layouts share the column width.</p>
     * 
     * @param columnNames list of column names
     * @return the column layout
     */
    public Columns getColumns(String ... columnNames) {
        if(columnNames.length == 0) {
            throw new IllegalArgumentException("columnNames");
        }
        Columns key = new Columns(columnNames);
        Columns cl = columns.get(key);
        if(cl != null) {
            return cl;
        }
        createColumns(key);
        return key;
    }

    /**
     * Adds a new row. This is a short cut for {@code getRootPanel().addRow(columns)}
     *
     * @param columns the column layout info
     * @return the new row
     */
    public Row addRow(Columns columns) {
        return rootPanel.addRow(columns);
    }

    /**
     * Adds a new row. This is a short cut for
     * {@code getRootPanel().addRow(getColumns(columnNames))}
     *
     * @param columnNames the column names
     * @return the new row
     */
    public Row addRow(String ... columnNames) {
        return rootPanel.addRow(getColumns(columnNames));
    }

    private void createColumns(Columns cl) {
        int prefixSize = 0;
        Columns prefixColumns = null;
        for(Columns c : columns.values()) {
            int match = c.match(cl);
            if(match > prefixSize) {
                prefixSize = match;
                prefixColumns = c;
            }
        }

        int numColumns = 0;
        for(int i=0,n=cl.names.length ; i<n ; i++) {
            if(!cl.isGap(i)) {
                numColumns++;
            }
        }
        
        cl.numColumns = numColumns;
        cl.firstColumn = columnGroups.size();
        cl.childGroups = new Group[cl.names.length];
        Group h = createSequentialGroup();

        if(prefixColumns == null) {
            getHorizontalGroup().addGroup(h);
        } else {
            for(int i=0 ; i<prefixSize ; i++) {
                if(!cl.isGap(i)) {
                    Group g = columnGroups.get(prefixColumns.firstColumn + i);
                    columnGroups.add(g);
                }
            }
            System.arraycopy(prefixColumns.childGroups, 0, cl.childGroups, 0, prefixSize);
            cl.childGroups[prefixSize-1].addGroup(h);
        }

        for(int i=prefixSize,n=cl.names.length ; i<n ; i++) {
            if(cl.isGap(i)) {
                h.addGap();
            } else {
                Group g = createParallelGroup();
                h.addGroup(g);
                columnGroups.add(g);
            }
            Group nextSequential = createSequentialGroup();
            Group childGroup = createParallelGroup().addGroup(nextSequential);
            h.addGroup(childGroup);
            h = nextSequential;
            cl.childGroups[i] = childGroup;
        }
        columns.put(cl, cl);
    }

    public static final class Columns {
        final String[] names;
        final int hashcode;
        int firstColumn;
        int numColumns;
        Group[] childGroups;

        Columns(String[] names) {
            this.names = names.clone();
            this.hashcode = Arrays.hashCode(this.names);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final Columns other = (Columns)obj;
            return this.hashcode == other.hashcode &&
                    Arrays.equals(this.names, other.names);
        }

        /**
         * Returns the number of non gap columns.
         * @return the number of non gap columns.
         */
        public int getNumColumns() {
            return numColumns;
        }

        public int getNumColumnNames() {
            return names.length;
        }

        public String getColumnName(int idx) {
            return names[idx];
        }
        
        @Override
        public int hashCode() {
            return hashcode;
        }

        boolean isGap(int column) {
            String name = names[column];
            return name.length() == 0 || "-".equals(name);
        }
        
        int match(Columns other) {
            int cnt = Math.min(this.names.length, other.names.length);
            for(int i=0 ; i<cnt ; i++) {
                if(!names[i].equals(other.names[i])) {
                    return i;
                }
            }
            return cnt;
        }
    }

    public final class Row {
        final Columns columns;
        final Panel panel;
        final Group row;
        int curColumn;

        Row(Columns columns, Panel panel, Group row) {
            this.columns = columns;
            this.panel = panel;
            this.row = row;
        }

        /**
         * Returns the current column. Adding a widget increments the this.
         * @return the current column.
         * @see Columns#getNumColumns()
         */
        public int getCurrentColumn() {
            return curColumn;
        }

        public Columns getColumns() {
            return columns;
        }

        /**
         * Adds a new widget to the row using {@link Alignment#FILL} alignment.
         *
         * @param w the new widget
         * @return this
         * @throws IllegalStateException if all widgets for this row have already been added
         */
        public Row add(Widget w) {
            if(curColumn == columns.numColumns) {
                throw new IllegalStateException("Too many widgets for column layout");
            }
            panel.getColumn(columns.firstColumn + curColumn).addWidget(w);
            row.addWidget(w);
            curColumn++;
            return this;
        }

        /**
         * Adds a new widget to this row using the specified alignment.
         *
         * @param w the new widget
         * @param alignment the alignment for the new widget
         * @return this
         * @throws IllegalStateException if all widgets for this row have already been added
         */
        public Row add(Widget w, Alignment alignment) {
            add(w);
            setWidgetAlignment(w, alignment);
            return this;
        }

        /**
         * Adds a new label to this row. The label is not associate with any widget.
         *
         * <p>It is equivalent to {@code add(new Label(label))}.</p>
         *
         * @param labelText the label text
         * @return this
         * @throws IllegalStateException if all widgets for this row have already been added
         */
        public Row addLabel(String labelText) {
            if(labelText == null) {
                throw new NullPointerException("labelText");
            }
            return add(new Label(labelText));
        }

        /**
         * Adds a label followed by the specified widget. The label uses
         * {@link Alignment#TOPLEFT} alignment, and is associated to the widget.
         * The alignment of the widget is {@link Alignment#FILL}.
         *
         * @param labelText the label text
         * @param w the new widget
         * @return this
         * @throws IllegalStateException if all widgets for this row have already been added
         */
        public Row addWithLabel(String labelText, Widget w) {
            if(labelText == null) {
                throw new NullPointerException("labelText");
            }
            Label labelWidget = new Label(labelText);
            labelWidget.setLabelFor(w);
            add(labelWidget, Alignment.TOPLEFT).add(w);
            return this;
        }

        /**
         * Adds a label followed by the specified widget. The label uses
         * {@link Alignment#TOPLEFT} alignment, and is associated to the widget.
         *
         * @param labelText the label text
         * @param w the new widget
         * @param alignment the alignment for the new widget
         * @return this
         * @throws IllegalStateException if all widgets for this row have already been added
         */
        public Row addWithLabel(String labelText, Widget w, Alignment alignment) {
            addWithLabel(labelText, w);
            setWidgetAlignment(w, alignment);
            return this;
        }
    }

    public final class Panel {
        final Panel parent;
        final ArrayList<Group> usedColumnGroups;
        final ArrayList<Panel> children;
        final Group rows;
        boolean valid;

        Panel(Panel parent) {
            this.parent = parent;
            this.usedColumnGroups = new ArrayList<Group>();
            this.children = new ArrayList<Panel>();
            this.rows = createSequentialGroup();
            this.valid = true;
        }

        public boolean isValid() {
            return valid;
        }

        /**
         * Calls {@link ColumnLayout#getColumns(java.lang.String[]) }
         *
         * @param columnNames the column names.
         * @return the column layout.
         */
        public Columns getColumns(String ... columnNames) {
            return ColumnLayout.this.getColumns(columnNames);
        }

        /**
         * Adds a new row to this panel using the specified column names.
         *
         * <p>It is equivalent to {@code addRow(getColumns(columnNames))}</p>
         *
         * @param columnNames the column names.
         * @return the new row
         */
        public Row addRow(String ... columnNames) {
            return addRow(ColumnLayout.this.getColumns(columnNames));
        }

        /**
         * Adds a new row to this panel.
         *
         * @param columns the column layout
         * @return the new row.
         * @throws IllegalStateException when the panel has been removed from the root.
         */
        public Row addRow(Columns columns) {
            if(columns == null) {
                throw new NullPointerException("columns");
            }
            checkValid();
            Group row = createParallelGroup();
            rows.addGroup(row);
            return new Row(columns, this, row);
        }

        /**
         * Adds a named vertical gap.
         *
         * @param name the gap name.
         * @throws IllegalStateException when the panel has been removed from the root.
         */
        public void addVerticalGap(String name) {
            checkValid();
            rows.addGap(name);
        }

        /**
         * Adds a new child panel
         *
         * @return the new child panel
         * @throws IllegalStateException when the panel has been removed from the root.
         */
        public Panel addPanel() {
            checkValid();
            Panel panel = new Panel(this);
            rows.addGroup(panel.rows);
            children.add(panel);
            return panel;
        }

        /**
         * Removes the specified child panel. Can also be called on an invalidated panel.
         * @param panel the child panel.
         */
        public void removePanel(Panel panel) {
            if(panel == null) {
                throw new NullPointerException("panel");
            }
            if(valid) {
                if(children.remove(panel)) {
                    panel.markInvalid();
                    rows.removeGroup(panel.rows, true);
                    for(int i=0,n=panel.usedColumnGroups.size() ; i<n ; i++) {
                        Group column = panel.usedColumnGroups.get(i);
                        if(column != null) {
                            usedColumnGroups.get(i).removeGroup(column, false);
                        }
                    }
                }
            }
        }

        /**
         * Removes all child panels and rows from this panel.
         */
        public void clearPanel() {
            if(valid) {
                children.clear();
                rows.clear(true);
                for(int i=0,n=usedColumnGroups.size() ; i<n ; i++) {
                    Group column = usedColumnGroups.get(i);
                    if(column != null) {
                        column.clear(false);
                    }
                }
            }
        }

        void markInvalid() {
            valid = false;
            for(int i=0,n=children.size() ; i<n ; i++) {
                children.get(i).markInvalid();
            }
        }

        void checkValid() {
            if(!valid) {
                throw new IllegalStateException("Panel has been removed");
            }
        }
        
        Group getColumn(int idx) {
            checkValid();
            if(usedColumnGroups.size() > idx) {
                Group column = usedColumnGroups.get(idx);
                if(column != null) {
                    return column;
                }
            }
            return makeColumn(idx);
        }

        private Group makeColumn(int idx) {
            Group parentColumn;
            if(parent != null) {
                parentColumn = parent.getColumn(idx);
            } else {
                parentColumn = columnGroups.get(idx);
            }
            Group column = createParallelGroup();
            parentColumn.addGroup(column);
            while(usedColumnGroups.size() <= idx) {
                usedColumnGroups.add(null);
            }
            usedColumnGroups.set(idx, column);
            return column;
        }
    }
}

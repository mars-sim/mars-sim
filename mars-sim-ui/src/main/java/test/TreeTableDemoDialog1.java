/*
 * Copyright (c) 2008-2009, Matthias Mann
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
package test;

import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Timer;
import de.matthiasmann.twl.TreeTable;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.AbstractTreeTableModel;
import de.matthiasmann.twl.model.AbstractTreeTableNode;
import de.matthiasmann.twl.model.PersistentStringModel;
import de.matthiasmann.twl.model.StringModel;
import de.matthiasmann.twl.model.TreeTableNode;

/**
 *
 * @author Matthias Mann
 */
public class TreeTableDemoDialog1 extends FadeFrame implements Runnable {

    private ScrollPane scrollPane;
    private Timer timer;
    private MyNode dynamicNode;

    public TreeTableDemoDialog1() {
        MyModel m = new MyModel();
        PersistentStringModel psm = new PersistentStringModel(
                AppletPreferences.userNodeForPackage(getClass()), "demoEditField", "you can edit this");

        MyNode a = m.insert("A", "1");
        a.insert("Aa", "2");
        a.insert("Ab", "3");
        MyNode ac = a.insert("Ac", "4");
        ac.insert("Ac1", "Hello");
        ac.insert("Ac2", "World");
        ac.insert("EditField", psm);
        a.insert("Ad", "5");
        MyNode b = m.insert("B", "6");
        b.insert("Ba", "7");
        b.insert("Bb", "8");
        b.insert("Bc", "9");
        dynamicNode = b.insert("Dynamic", "stuff");
        m.insert(new SpanString("This is a very long string which will span into the next column.", 2), "Not visible");
        m.insert("This is a very long string which will be clipped.", "This is visible");

        TreeTable t = new TreeTable(m);
        t.setTheme("/table");
        t.registerCellRenderer(SpanString.class, new SpanRenderer());
        t.registerCellRenderer(StringModel.class, new EditFieldCellRenderer());
        t.setDefaultSelectionManager();

        scrollPane = new ScrollPane(t);
        scrollPane.setTheme("/tableScrollPane");

        setTheme("scrollPaneDemoDialog1");
        setTitle("Dynamic TreeTable");
        add(scrollPane);
    }

    @Override
    protected void afterAddToGUI(GUI gui) {
        super.afterAddToGUI(gui);
        timer = gui.createTimer();
        timer.setCallback(this);
        timer.setDelay(1500);
        timer.setContinuous(true);
        timer.start();
    }

    int state;
    MyNode subNode;

    public void run() {
        //System.out.println("state="+state);
        switch(state++) {
        case 0:
            dynamicNode.insert("Counting", "3...");
            break;
        case 1:
            dynamicNode.insert("Counting", "2...");
            break;
        case 2:
            dynamicNode.insert("Counting", "1...");
            break;
        case 3:
            subNode = dynamicNode.insert("this is a", "folder");
            break;
        case 4:
            subNode.insert("first", "entry");
            break;
        case 5:
            subNode.insert("now starting to remove", "counter");
            break;
        case 6:
        case 7:
        case 8:
            dynamicNode.remove(0);
            break;
        case 9:
            subNode.insert("last", "entry");
            break;
        case 10:
            dynamicNode.insert("now removing", "folder");
            break;
        case 11:
            dynamicNode.remove(0);
            break;
        case 12:
            dynamicNode.insert("starting", "again");
            break;
        case 13:
            dynamicNode.removeAll();
            state = 0;
            break;
        }
    }

    public void centerScrollPane() {
        scrollPane.updateScrollbarSizes();
        scrollPane.setScrollPositionX(scrollPane.getMaxScrollPosX()/2);
        scrollPane.setScrollPositionY(scrollPane.getMaxScrollPosY()/2);
    }

    static class MyNode extends AbstractTreeTableNode {
        private Object str0;
        private Object str1;

        public MyNode(TreeTableNode parent, Object str0, Object str1) {
            super(parent);
            this.str0 = str0;
            this.str1 = str1;
            setLeaf(true);
        }

        public Object getData(int column) {
            return (column == 0) ? str0 : str1;
        }

        public MyNode insert(Object str0, Object str1) {
            MyNode n = new MyNode(this, str0, str1);
            insertChild(n, getNumChildren());
            setLeaf(false);
            return n;
        }

        public void remove(int idx) {
            removeChild(idx);
        }

        public void removeAll() {
            removeAllChildren();
        }
    }

    static class MyModel extends AbstractTreeTableModel {
        private static final String[] COLUMN_NAMES = {"Left", "Right"};
        public int getNumColumns() {
            return 2;
        }
        public String getColumnHeaderText(int column) {
            return COLUMN_NAMES[column];
        }
        public MyNode insert(Object str0, String str1) {
            MyNode n = new MyNode(this, str0, str1);
            insertChild(n, getNumChildren());
            return n;
        }
    }

    static class SpanString {
        private final String str;
        private final int span;

        public SpanString(String str, int span) {
            this.str = str;
            this.span = span;
        }

        @Override
        public String toString() {
            return str;
        }
    }
    
    static class SpanRenderer extends TreeTable.StringCellRenderer {
        int span;

        @Override
        public void setCellData(int row, int column, Object data) {
            super.setCellData(row, column, data);
            span = ((SpanString)data).span;
        }

        @Override
        public int getColumnSpan() {
            return span;
        }
    }

    static class EditFieldCellRenderer implements TreeTable.CellWidgetCreator {
        private StringModel model;
        private int editFieldHeight;

        public Widget updateWidget(Widget existingWidget) {
            EditField ef = (EditField)existingWidget;
            if(ef == null) {
                ef = new EditField();
            }
            ef.setModel(model);
            return ef;
        }

        public void positionWidget(Widget widget, int x, int y, int w, int h) {
            widget.setPosition(x, y);
            widget.setSize(w, h);
        }

        public void applyTheme(ThemeInfo themeInfo) {
            editFieldHeight = themeInfo.getParameter("editFieldHeight", 10);
        }

        public String getTheme() {
            return "EditFieldCellRenderer";
        }

        public void setCellData(int row, int column, Object data) {
            this.model = (StringModel)data;
        }

        public int getColumnSpan() {
            return 1;
        }

        public int getPreferredHeight() {
            return editFieldHeight;
        }

        public Widget getCellRenderWidget(int x, int y, int width, int height, boolean isSelected) {
            return null;
        }
    }
}

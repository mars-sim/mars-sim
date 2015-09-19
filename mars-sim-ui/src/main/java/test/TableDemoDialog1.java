/*
 * Copyright (c) 2008-2010, Matthias Mann
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

import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.Table;
import de.matthiasmann.twl.TableBase.CellWidgetCreator;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.AbstractTableModel;
import de.matthiasmann.twl.model.ListModel;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;
import de.matthiasmann.twl.model.TableModel;

/**
 *
 * @author Matthias Mann
 */
public class TableDemoDialog1 extends FadeFrame {

    private ScrollPane scrollPane;

    public TableDemoDialog1() {
        final ListModel<String> cbm = new SimpleChangableListModel<String>("Hallo", "Welt", "Test");
        final ComboBoxValue cbv = new ComboBoxValue(0, cbm);
        TableModel m = new AbstractTableModel() {
            public int getNumRows() {
                return 20;
            }

            public int getNumColumns() {
                return 3;
            }

            public String getColumnHeaderText(int column) {
                return "Column " + column;
            }

            public Object getCell(int row, int column) {
                if(row == 7 && column == 1) {
                    // This cell will contain a ComboBoxValue - via registerCellRenderer
                    // below this will cause a comobox to appear
                    return cbv;
                }
                if(row == 6 && column == 1) {
                    return "Selected: " + cbv.getValue();
                }
                return "Row " + row + (((row*getNumColumns()+column)%17 == 0)?"\n":"") + " Column " + column;
            }

            @Override
            public Object getTooltipContent(int row, int column) {
                return "X:"+(column+1)+" Y:"+(row+1);
            }

        };

        Table t = new Table(m);
        // register the ComboBoxValue class (see below) and it's cell widget creator
        // this will change the behavior of cells when they contain a data valzue of
        // the type "ComboBoxValue"
        t.registerCellRenderer(ComboBoxValue.class, new ComboBoxCellWidgetCreator());

        t.setTheme("/table");
        t.setVaribleRowHeight(true);
        t.setDefaultSelectionManager();

        scrollPane = new ScrollPane(t);
        scrollPane.setTheme("/tableScrollPane");

        setTheme("scrollPaneDemoDialog1");
        setTitle("Table with variable row height");
        add(scrollPane);
    }

    public void centerScrollPane() {
        scrollPane.updateScrollbarSizes();
        scrollPane.setScrollPositionX(scrollPane.getMaxScrollPosX()/2);
        scrollPane.setScrollPositionY(scrollPane.getMaxScrollPosY()/2);
    }

    /**
     * This is a very simple model class which will store the currently selected
     * entry of the combobox and the model for the combobox.
     *
     * It is also the "key" type which will cause the cell to become a ComboBox
     */
    public static class ComboBoxValue extends SimpleIntegerModel {
        private final ListModel<String> model;
        public ComboBoxValue(int value, ListModel<String> model) {
            super(0, model.getNumEntries()-1, value);
            this.model = model;
        }

        public ListModel<String> getModel() {
            return model;
        }
    }

    /**
     * A CellWidgetCreator instance is used to create and position the ComboBox
     * widget inside the table cell. This class is also responsible to connect
     * all listeners so that updates to/from the combobox can happen.
     *
     * Only a single ComboBoxCellWidgetCreator will be created per table. But it
     * can manage several widgets/cells.
     *
     * As this is a simple example only the listener ComboBox -> ComboBoxValue is
     * implemeted.
     */
    private static class ComboBoxCellWidgetCreator implements CellWidgetCreator {
        private int comboBoxHeight;
        private ComboBoxValue data;

        public void applyTheme(ThemeInfo themeInfo) {
            comboBoxHeight = themeInfo.getParameter("comboBoxHeight", 0);
        }

        public String getTheme() {
            return "ComboBoxCellRenderer";
        }

        /**
         * Update or create the ComboBox widget.
         *
         * @param existingWidget null on first call per cell or the previous
         *   widget when an update has been send to that cell.
         * @return the widget to use for this cell
         */
        public Widget updateWidget(Widget existingWidget) {
            MyComboBox cb = (MyComboBox)existingWidget;
            if(cb == null) {
                cb = new MyComboBox();
            }
            // in this example there should be no update to cells
            // but the code pattern here can also be used when updates are
            // generated. Care should be taken that the above type cast
            // does not fail.
            cb.setData(data);
            return cb;
        }

        public void positionWidget(Widget widget, int x, int y, int w, int h) {
            // this method will size and position the ComboBox
            // If the widget should be centered (like a check box) then this
            // would be done here
            widget.setPosition(x, y);
            widget.setSize(w, h);
        }

        public void setCellData(int row, int column, Object data) {
            // we have to remember the cell data for the next call of updateWidget
            this.data = (ComboBoxValue)data;
        }

        public Widget getCellRenderWidget(int x, int y, int width, int height, boolean isSelected) {
            // this cell does not render anything itself
            return null;
        }

        public int getColumnSpan() {
            // no column spanning
            return 1;
        }

        public int getPreferredHeight() {
            // we have to inform the table about the required cell height before
            // we can create the widget - so we need to get the required height
            // from the theme -  see applyTheme/getTheme
            return comboBoxHeight;
        }

        /**
         * We need a subclass of ComboBox to contain ("be" in this example) the
         * listeners.
         */
        private static class MyComboBox extends ComboBox<String> implements Runnable {
            ComboBoxValue data;

            public MyComboBox() {
                setTheme("combobox");   // keep default theme name
                addCallback(this);
            }
            
            void setData(ComboBoxValue data) {
                this.data = null;
                setModel(data.getModel());
                setSelected(data.getValue());
                this.data = data;
            }

            public void run() {
                if(data != null) {
                    data.setValue(getSelected());
                }
            }
        }
    }

}

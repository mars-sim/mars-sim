/**
 * Mars Simulation Project
 * TableTab.java
 * @version 2.74 2002-05-16
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.standard.monitor;

import org.mars_sim.msp.ui.standard.ImageLoader;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.EtchedBorder;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.UIManager;

/**
 * This class represents a table view displayed within the Monitor Window. It
 * displays the contents of a UnitTableModel in a JTable window. It supports
 * the selection and deletion of rows.
 */
class TableTab extends MonitorTab {

    /**
     * This class provides a fixed image icon that is drawn using a Graphics
     * object. It represents an arrow Icon that can be other ascending or
     * or descending.
     */
    static class ColumnSortIcon implements Icon
    {
        static final int midw = 4;
        private Color lightShadow;
        private Color darkShadow;
        private boolean downwards;

        public ColumnSortIcon(boolean downwards, Color baseColor) {
            this.downwards = downwards;
            this.lightShadow = baseColor.brighter();
            this.darkShadow = baseColor.darker().darker();
        }


        public void paintIcon(Component c, Graphics g, int xo, int yo) {
            int w = getIconWidth();
            int xw = xo+w-1;
            int h = getIconHeight();
            int yh = yo+h-1;

            if (downwards)
            {
                g.setColor(lightShadow);
                g.drawLine(xo+midw+1, yo, xw, yh-1);
                g.drawLine(xo, yh, xw, yh);
                g.setColor(darkShadow);
                g.drawLine(xo+midw-1, yo, xo, yh-1);
            }
            else
            {
                g.setColor(lightShadow);
                g.drawLine(xw, yo+1, xo+midw, yh);
                g.setColor(darkShadow);
                g.drawLine(xo+1, yo+1, xo+midw-1, yh);
                g.drawLine(xo, yo, xw, yo);
            }
        }

        public int getIconWidth(){
            return 2 * midw;
        }

        public int getIconHeight() {
            return getIconWidth()-1;
        }
    }

    /**
     * This renderer use a delegation software design pattern to delegate
     * this rendering of the table cell header to the real default render,
     * however this renderer adds in an icon on the cells which are sorted.
     **/
    class TableHeaderRenderer implements TableCellRenderer {
        private TableCellRenderer defaultRenderer;

        public TableHeaderRenderer(TableCellRenderer theRenderer) {
            defaultRenderer = theRenderer;
        }

        /**
         * Renderer the specified Table Header cell
         **/
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column)
        {
            Component theResult = defaultRenderer.getTableCellRendererComponent(
                                         table, value, isSelected, hasFocus,
                                         row, column);
            if (theResult instanceof JLabel) {
                // Must clear the icon if not sorted column. This is a renderer
                // class used to render each column heading in turn.
                JLabel cell = (JLabel)theResult;
                Icon icon = null;
                if (column == sortedColumn) {
                    if (sortAscending)
                        icon = ascendingIcon;
                    else
                        icon = descendingIcon;
                }
                cell.setIcon(icon);
            }
            return theResult;
        }
    }

    // These icons are used to render the sorting images on the column header
    private static Icon ascendingIcon = null;
    private static Icon descendingIcon = null;
    private final static Icon TABLEICON = ImageLoader.getIcon("Table");

    private JTable table;                   // Table component
    private TableSorter sortedModel;        // Sortable modle proxy
    private boolean sortAscending = true;   // Constructor will flip this
    private int sortedColumn = 0;           // Sort column is defined

    /**
     * Create a Jtable within a tab displaying the specified model.
     * @param model The model of Units to display.
     * @param mandatory Is this table view mandatory.
     */
    public TableTab(MonitorModel model, boolean mandatory) {
        super(model, mandatory, TABLEICON);

        // Can not create icons until UIManager is up and running
        if (ascendingIcon == null) {
            Color baseColor = UIManager.getColor("Label.background");

            ascendingIcon = new ColumnSortIcon(false, baseColor);
            descendingIcon = new ColumnSortIcon(true, baseColor);
        }

        // If the model is not ordered, allow user the facility
        if (!model.getOrdered()) {
            // Create a sortable model to act as a proxy
            sortedModel = new TableSorter(model);

            // Create scrollable table window
            table = new JTable(sortedModel) {

                /**
                 * Display the cell contents as a tooltip. Useful when cell
                 * contents in wider than the cell
                 */
                public String getToolTipText(MouseEvent e) {
                    return getCellText(e);
                };
            };

        // Get the TableColumn header to display sorted column
        JTableHeader theHeader = table.getTableHeader();
        TableHeaderRenderer theRenderer =
                        new TableHeaderRenderer(theHeader.getDefaultRenderer());
        theHeader.setDefaultRenderer(theRenderer);

         // Add a mouse listener for the mouse event selecting the sorted column
         // Not the best way but no double click is provided on Header class
        theHeader.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                // Find the column at this point
                int column = table.getTableHeader().columnAtPoint(e.getPoint());
                setSortColumn(column);
                table.getTableHeader().repaint();
             }
        });
        }
        else {
            // Simple JTable
            table = new JTable(model) {

                /**
                 * Display the cell contents as a tooltip. Useful when cell
                 * contents in wider than the cell
                 */
                public String getToolTipText(MouseEvent e) {
                    return getCellText(e);
                };
            };
        }

        // Add a scrolled window and center it with the table
        JScrollPane scroller = new JScrollPane(table);
        scroller.setBorder(new EtchedBorder());
        add(scroller, "Center");

        setName(model.getName());
        setSortColumn(0);
    }

    /**
     * Display property window anchored to a main desktop.
     *
     * @param desktop Main desktop owing the properties dialog.
     */
    public void displayProps(MainDesktopPane desktop) {
        TableProperties propsWindow = new TableProperties(getName(), table,
                                                          desktop);
        propsWindow.show();
    }

    /**
     * This return the selected rows in the model that are current
     * selected in this view.
     *
     * @return array of row indexes.
     */
    protected List getSelection() {
        MonitorModel target = (sortedModel != null ? sortedModel : getModel());

        int indexes[] = table.getSelectedRows();
        ArrayList selectedRows = new ArrayList();
        for(int i = 0; i < indexes.length; i++) {
            selectedRows.add(target.getObject(indexes[i]));
        }

        return selectedRows;
    }

    /**
     * Get the cell contents under the MouseEvent, this will be displayed
     * as a tooltip.
     * @param e MouseEvent triggering tool tip.
     * @return Tooltip text.
     */
    private String getCellText(MouseEvent e) {
        Point p = e.getPoint();
        int column = table.columnAtPoint(p);
        int row = table.rowAtPoint(p);
        String result = null;
        if ((column >= 0) && (row >= 0)) {
            Object cell = table.getValueAt(row, column);
            if (cell != null) {
                result = cell.toString();
            }
        }
        return result;
    }

    /**
     * Remove this view.
     */
    public void removeTab() {
        super.removeTab();
        table = null;
        sortedModel = null;
    }

    private void setSortColumn(int index) {
        if (sortedModel != null) {
            if (sortedColumn == index) {
                sortAscending = !sortAscending;
            }
            sortedColumn = index;
            sortedModel.sortByColumn(sortedColumn, sortAscending);
        }
    }
}

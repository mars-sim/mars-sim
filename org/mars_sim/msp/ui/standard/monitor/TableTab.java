/**
 * Mars Simulation Project
 * TableTab.java
 * @version 2.73 2002-01-30
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.standard.monitor;

import org.mars_sim.msp.ui.standard.MainDesktopPane;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import java.awt.event.MouseEvent;
import java.awt.Point;
import javax.swing.ImageIcon;
import javax.swing.Icon;

/**
 * This class represents a table view displayed within the Monitor Window. It
 * displays the contents of a UnitTableModel in a JTable window. It supports
 * the selection and deletion of rows.
 */
class TableTab extends MonitorTab {

    private final static Icon TABLEICON = new ImageIcon("images/Table.gif");

    private JTable table;            // Table component

    /**
     * Create a Jtable within a tab displaying the specified model.
     * @param model The model of Units to display.
     * @param mandatory Is this table view mandatory.
     */
    public TableTab(UnitTableModel model, boolean mandatory) {
        super(model, mandatory, TABLEICON);

        // Create scrollable table window
        table = new JTable(model) {
            /**
             * Display the cell contents as a tooltip. Useful when cell
             * contents in wider than the cell
             */
            public String getToolTipText(MouseEvent e) {
                return getCellText(e);
            };
        };


        // Add a scrolled window and center it with the table
        JScrollPane scroller = new JScrollPane(table);
        scroller.setBorder(new EtchedBorder());
        add(scroller, "Center");

        setName(model.getName());
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
    protected int[] getSelection() {
        return table.getSelectedRows();
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
}

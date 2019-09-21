/**
 * Mars Simulation Project
 * ColumnResizer.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ColumnResizer {

    public static void adjustColumnPreferredWidths(JTable table) {

        // Gets max width for cells in column as the preferred width
        TableColumnModel columnModel = table.getColumnModel();
        for (int col=0; col<table.getColumnCount(); col++) {
            // System.out.println ("--- col " + col + " ---");
            int maxwidth = 40;
            //int minwidth = 40;
            int colWidth = 0;
            for (int row=0; row<table.getRowCount(); row++) {
                TableCellRenderer rend = table.getCellRenderer (row, col);
//                Object value = table.getValueAt (row, col);
//                Component comp =
//                    rend.getTableCellRendererComponent (table,
//                                                        value,
//                                                        false,
//                                                        false,
//                                                        row,
//                                                        col);

                Component comp = table.prepareRenderer(rend, row, col);
                colWidth = comp.getPreferredSize().width;
                //minwidth = colWidth;
                //if (colWidth > maxwidth) maxwidth = colWidth;
                //if (colWidth < minwidth) minwidth = colWidth;
                maxwidth = Math.max (colWidth, maxwidth);
                //minwidth = Math.max (colWidth, minwidth);

                //System.out.println ("col " + col +
                //                    " pref width now " +
                //                    maxwidth);
            }

            // Considers the column header's preferred width too
            TableColumn column = columnModel.getColumn (col);

//            int headerWidth = 0;
//            TableCellRenderer headerRenderer = column.getHeaderRenderer();
//            if (headerRenderer == null)
//                headerRenderer = table.getTableHeader().getDefaultRenderer();
//            Object headerValue = column.getHeaderValue();
//            Component headerComp =
//                    headerRenderer.getTableCellRendererComponent (table,
//                                                                  headerValue,
//                                                                  false,
//                                                                  false,
//                                                                  0,
//                                                                  col);
//            headerWidth = headerComp.getPreferredSize().width;
//
//            maxwidth = Math.max (colWidth, headerWidth);
//            minwidth = Math.min (colWidth, headerWidth);

            column.setPreferredWidth (maxwidth);
            //column.setMaxWidth(maxwidth); // very bad!
            column.setMinWidth(maxwidth);

        }
    }
}

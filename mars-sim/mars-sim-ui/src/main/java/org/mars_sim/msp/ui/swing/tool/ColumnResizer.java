/**
 * Mars Simulation Project
 * ColumnResizer.java
 * @version 3.07 2014-12-28
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;


import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class ColumnResizer {

    public static void adjustColumnPreferredWidths(JTable table) {
        // Gets max width for cells in column as the preferred width
        TableColumnModel columnModel = table.getColumnModel();
        for (int col=0; col<table.getColumnCount(); col++) {
            // System.out.println ("--- col " + col + " ---");
            int maxwidth = 0;
            for (int row=0; row<table.getRowCount(); row++) {
                TableCellRenderer rend = table.getCellRenderer (row, col);
                Object value = table.getValueAt (row, col);
                Component comp =
                    rend.getTableCellRendererComponent (table, 
                                                        value,
                                                        false,
                                                        false,
                                                        row,
                                                        col);
                maxwidth = Math.max (comp.getPreferredSize().width,
                                     maxwidth);
                //System.out.println ("col " + col +
                //                    " pref width now " + 
                //                    maxwidth);
            } 

            // Version 1: doesn't consider the column header's preferred width         
            // TableColumn column = columnModel.getColumn (col);
            // column.setPreferredWidth (maxwidth);
             
            
            // Version 2: considers the column header's preferred width too
            TableColumn column = columnModel.getColumn (col);
            TableCellRenderer headerRenderer = column.getHeaderRenderer();
            if (headerRenderer == null)
                headerRenderer = table.getTableHeader().getDefaultRenderer();
            Object headerValue = column.getHeaderValue();
            Component headerComp = 
                    headerRenderer.getTableCellRendererComponent (table, 
                                                                  headerValue,
                                                                  false,
                                                                  false,
                                                                  0,
                                                                  col);
            maxwidth = Math.max (maxwidth, 
                                 headerComp.getPreferredSize().width);
            column.setPreferredWidth (maxwidth);
            

        }
    }
}

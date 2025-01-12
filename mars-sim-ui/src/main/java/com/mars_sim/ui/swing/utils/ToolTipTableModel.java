/*
 * Mars Simulation Project
 * ToolTipTableModel.java
 * @date 2025-01-12
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.TableModel;


/**
 * This is an interface to represent a TableModel that can also deliver
 * tooltips for values.
 */
public interface ToolTipTableModel extends TableModel {

    /**
     * Get a String that represents a tooltip for a specific vale in the model
     * @param row Row offset
     * @param col Column offset
     * @return Could return null if on tootip is available
     */
    String getToolTipAt(int row, int col);

    /**
     * Static helper method that extract the tooltip under the Mouse for a JTable
     * that is using the ToolTipTableModel
     * @param e
     * @param source
     * @return
     */
    public static String extractToolTip(MouseEvent e, JTable source) {
        var p = e.getPoint();
		int rowIndex = source.rowAtPoint(p);
		int colIndex = source.columnAtPoint(p);

        // Use the column model to handle hidden columns
        var tc = source.getColumnModel().getColumn(colIndex);
        var sorter = source.getRowSorter();
        if (sorter != null) {
            rowIndex = sorter.convertRowIndexToModel(rowIndex);
        }
        ToolTipTableModel model = (ToolTipTableModel) source.getModel();
        return model.getToolTipAt(rowIndex, tc.getModelIndex());
    }
}

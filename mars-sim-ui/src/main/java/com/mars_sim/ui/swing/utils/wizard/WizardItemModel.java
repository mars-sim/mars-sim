/**
 * Mars Simulation Project
 * WizardItemModel.java
 * @date 2026-02-03
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.wizard;

import com.mars_sim.ui.swing.components.EnhancedTableModel;

/**
 * This represents a model that is used to select items in the WizardItemStep class.
 * It is a standard table model but supports the concept of failure of cells that do not match a criteria.
 */
 
public interface WizardItemModel<T> extends EnhancedTableModel {

    /**
     * Checks if the item at the given row has any failure cells. This checks all columns in the row
     * @param row Index of the item to check.
     * @return true if the item has any failure cells, false otherwise.
     */
    boolean isFailureItem(int row);

    /**
     * Get the item at the given row in the model. Note this is the model row, not the view row.
     * @param row the row index.
     * @return the item at the given row, or null if none.
     */
    T getItem(int row);

    /**
     * Releases any resources held by the model.
     */
    void release();

    /**
     * Checks if the cell at the given row and column has a failure.
     * @param row Index of the row to check.
     * @param column Index of the column to check.
     * @return Message if the cell is in a failure state, or null if valid.
     */
    String isFailureCell(int row, int column);

}

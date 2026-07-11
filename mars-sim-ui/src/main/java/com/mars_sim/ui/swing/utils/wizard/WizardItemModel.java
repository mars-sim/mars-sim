/**
 * Mars Simulation Project
 * WizardItemModel.java
 * @date 2026-02-03
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.wizard;

import com.mars_sim.ui.swing.components.EnhancedTableModel;
import com.mars_sim.ui.swing.utils.StatefulComponent;

/**
 * This represents a model that is used to select items in the WizardItemStep class.
 * It is a standard table model but supports the concept of failure of cells that do not match a criteria.
 */
 
public interface WizardItemModel<T> extends EnhancedTableModel, StatefulComponent {

    /**
     * Get the item at the given row in the model. Note this is the model row, not the view row.
     * @param row the row index.
     * @return the item at the given row, or null if none.
     */
    T getItem(int row);

    /**
     * Checks if the cell at the given row and column has a failure.
     * @param row Index of the row to check.
     * @param column Index of the column to check.
     * @return Message if the cell is in a failure state, or null if valid.
     */
    String isFailureCell(int row, int column);

}

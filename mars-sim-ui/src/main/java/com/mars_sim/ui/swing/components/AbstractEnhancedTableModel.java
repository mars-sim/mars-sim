/*
 * Mars Simulation Project
 * AbstractEnhancedTableModel.java
 * @date 2026-08-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.components;

import javax.swing.table.AbstractTableModel;

/**
 * Default implementation of the EnhancedTableModel interface. This class provides a base implementation for table models that need to support enhanced features such as column specifications and tooltips.
 * Subclasses can extend this class to provide specific data and behavior for their table models.
 */
public abstract class AbstractEnhancedTableModel extends AbstractTableModel
    implements EnhancedTableModel {
    private ColumnSpec[] columns;
    
    protected AbstractEnhancedTableModel(ColumnSpec... columns) {
        this.columns = columns;
    }

	/**
	 * Returns the number of columns in the model.
	 * @return the number of columns in the model.
	 */
	@Override
	public int getColumnCount() {
		return columns.length;
	}

    /**
     * Get the column name from teh assoaicted ColumnSpec.
     */
	@Override
	public String getColumnName(int columnIndex) {
		return getColumnSpec(columnIndex).name();
	}

    /**
	 * Returns the most specific superclass for all the cell values in the column.
	 * @param columnIndex the index of the column.
	 * @return the common ancestor class of the object values in the model.
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return getColumnSpec(columnIndex).type();
	}

    /**
     * Get the column specification for the given model index.
     */
    @Override
    public ColumnSpec getColumnSpec(int modelIndex) {
        return columns[modelIndex];
    }
    
    /**
     * Default tooltip is nothing.
     * @param row Row of cell
     * @param col Column of cell
     * @return Always returns null.
     */
    @Override
    public String getToolTipAt(int row, int col) {
        return null;
    }
}
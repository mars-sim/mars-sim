/**
 * Mars Simulation Project
 * UnitTableModel.java
 * @version 2.80 2007-03-22
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission.create;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.UnitCollection;

/**
 * An abstract table model for unit tables in create mission wizard.
 */
abstract class UnitTableModel extends AbstractTableModel {

	// Data members.
	protected UnitCollection units;
	protected List columns;
	
	/**
	 * Constructor
	 */
	UnitTableModel() {
		// Use AbstractTableModel constructor.
		super();
		
		// Initialize data members.
		units = new UnitCollection();
		columns = new ArrayList();
	}
	
	/**
	 * Returns the number of rows in the model.
	 * @return number of rows.
	 */
	public int getRowCount() {
		return units.size();
	}

	/**
	 * Returns the number of columns in the model.
	 * @return number of columns.
	 */
	public int getColumnCount() {
		return columns.size();
	}

	/**
	 * Returns the name of the column at columnIndex.
	 * @param columnIndex the column index.
	 * @return column name.
	 */
	public String getColumnName(int columnIndex) {
		return (String) columns.get(columnIndex);
    }
	
	/**
	 * Gets the unit at a row index.
	 * @param row the row index.
	 * @return the unit in the row, or null if none.
	 */
	Unit getUnit(int row) {
		Unit result = null;
		if ((row > -1) && (row < getRowCount())) result = units.get(row);
		return result;
	}
	
	/**
	 * Updates the table data.
	 */
	abstract void updateTable();
	
	/**
	 * Checks if a table cell is a failure cell.
	 * @param row the table row.
	 * @param column the table column.
	 * @return true if cell is a failure cell.
	 */
	abstract boolean isFailureCell(int row, int column);
	
	/**
	 * Checks if row contains a failure cell.
	 * @param row the row index.
	 * @return true if row has failure cell.
	 */
	boolean isFailureRow(int row) {
		boolean result = false;
		for (int x = 0; x < getColumnCount(); x++) {
			if (isFailureCell(row, x)) result = true;
		}
		return result;
	}
}
/**
 * Mars Simulation Project
 * UnitTableModel.java
 * @version 3.1.0 2017-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * An abstract table model for unit tables in create mission wizard.
 */
@SuppressWarnings("serial")
abstract class UnitTableModel extends AbstractTableModel {

	// Data members.
	protected Collection<Unit> units;
	protected List<String> columns;
	
	protected static UnitManager unitManager = Simulation.instance().getUnitManager();

	/**
	 * Constructor
	 */
	UnitTableModel() {
		// Use AbstractTableModel constructor.
		super();
		
		// Initialize data members.
		units = new ConcurrentLinkedQueue<Unit>();
		columns = new ArrayList<String>();
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
		return columns.get(columnIndex);
    }
	
	/**
	 * Gets the unit at a row index.
	 * @param row the row index.
	 * @return the unit in the row, or null if none.
	 */
	Unit getUnit(int row) {
		Unit result = null;
		Object[] array = units.toArray();
		if ((row > -1) && (row < getRowCount())) result = (Unit) array[row];
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
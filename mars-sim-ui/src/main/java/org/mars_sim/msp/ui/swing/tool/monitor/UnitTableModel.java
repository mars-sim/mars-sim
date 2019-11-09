/**
 * Mars Simulation Project
 * UnitTableModel.java
 * @version 3.1.0 2017-09-13
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;

/**
 * The UnitTableModel that maintains a table model of Units objects. It is only
 * a partial implementation of the TableModel interface.
 */
@SuppressWarnings("serial")
abstract public class UnitTableModel extends AbstractTableModel implements MonitorModel, UnitListener {

	// Data members
	/** Should it be refreshed to get the number of units. */
	private boolean refreshSize = true;
	/** The number of the units */
	private int size = -1;

	/** Model name. */
	private String name;
	/** Key for calling the internationalized text that counts the number of units. */
	private String countingMsgKey;
	/** Names of the displayed columns. */
	private String columnNames[];
	
	/** Types of the individual columns. */
	private Class<?> columnTypes[];
	/** Collection of units. */
	private Collection<Unit> units;

	protected static UnitManager unitManager = Simulation.instance().getUnitManager();

	/**
	 * Constructor.
	 * 
	 * @param name           Name of the model.
	 * @param countingMsgKey {@link String} key for calling the internationalized
	 *                       text that counts the number of units. should be a valid
	 *                       key to an existing value in
	 *                       <code>messages.properties</code>.
	 * @param names          Names of the columns displayed.
	 * @param types          The Classes of the individual columns.
	 */
	protected UnitTableModel(String name, String countingMsgKey, String names[], Class<?> types[]) {
		// Initialize data members
		this.name = name;
		this.countingMsgKey = countingMsgKey;
		this.units = new ConcurrentLinkedQueue<Unit>();
		// getRowCount();
		this.columnNames = names;
		this.columnTypes = types;
	}

	/**
	 * Add a unit to the model.
	 * 
	 * @param newUnit Unit to add to the model.
	 */
	protected void addUnit(Unit newUnit) {
		if (!units.contains(newUnit)) {
			units.add(newUnit);
			refreshSize = true;
			newUnit.addUnitListener(this);

			// Inform listeners of new row
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireTableRowsInserted(getUnitNumber() - 1, getUnitNumber() - 1);
				}
			});
		}
	}

	/**
	 * Remove a unit from the model.
	 * 
	 * @param oldUnit Unit to remove from the model.
	 */
	protected void removeUnit(Unit oldUnit) {
		if (units.contains(oldUnit)) {
			int index = getIndex(oldUnit);

			units.remove(oldUnit);
			refreshSize = true;
			oldUnit.removeUnitListener(this);

			// Inform listeners of new row
			SwingUtilities.invokeLater(new RemoveUnitTableUpdater(index));
		}
	}

	/**
	 * Gets the index value of a given unit.
	 * 
	 * @param unit the unit
	 * @return the index value.
	 */
	private int getIndex(Unit unit) {
		final Iterator<Unit> it = units.iterator();
		int idx = -1;
		Unit u;
		while (it.hasNext()) {
			idx++;
			u = it.next();
			if (u.equals(unit)) {
				return idx;
			}
		}
		throw new IllegalStateException("Could not find index for unit " + unit);
	}

	/**
	 * Adds a collection of units to the model.
	 * 
	 * @param newUnits the units to add.
	 */
	protected void addAll(Collection<Unit> newUnits) {
		Iterator<Unit> i = newUnits.iterator();
		while (i.hasNext())
			addUnit(i.next());
	}

	/**
	 * Clears out units from the model.
	 */
	protected void clear() {
		Iterator<Unit> i = units.iterator();
		while (i.hasNext())
			i.next().removeUnitListener(this);
		units.clear();
		refreshSize = true;
		fireTableDataChanged();
	}

	/**
	 * Checks if unit is in table model already.
	 * 
	 * @param unit the unit to check.
	 * @return true if unit is in table.
	 */
	protected boolean containsUnit(Unit unit) {
		return units.contains(unit);
	}

	/**
	 * Gets the number of units in the model.
	 * 
	 * @return number of units.
	 */
	protected int getUnitNumber() {
		if (refreshSize) {
			this.size = units == null ? 0 : units.size();
			refreshSize = false;
		}
		// if (units != null) return units.size();
		// else return 0;
		return this.size;
	}

	protected boolean getRefreshSize() {
		return refreshSize;
	}

	protected void setRefreshSize(boolean value) {
		refreshSize = value;
	}

	protected Collection<Unit> getUnits() {
		return units;
	}

	protected int getSize() {
		return units.size();
	}

	protected void setSize(int value) {
		size = value;
	}

	/**
	 * Return the number of columns
	 * 
	 * @return column count.
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * Return the type of the column requested.
	 * 
	 * @param columnIndex Index of column.
	 * @return Class of specified column.
	 */
	public Class<?> getColumnClass(int columnIndex) {
		if ((columnIndex >= 0) && (columnIndex < columnTypes.length)) {
			return columnTypes[columnIndex];
		}
		return Object.class;
	}

	/**
	 * Return the name of the column requested.
	 * 
	 * @param columnIndex Index of column.
	 * @return name of specified column.
	 */
	public String getColumnName(int columnIndex) {
		if ((columnIndex >= 0) && (columnIndex < columnNames.length)) {
			return columnNames[columnIndex];
		}
		return "Unknown";
	}

	/**
	 * Get the name of the model.
	 * 
	 * @return model name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the number of rows in the model.
	 * 
	 * @return the number of Units.
	 */
	public int getRowCount() {
		return getUnitNumber();
	}

	/**
	 * Is this model already ordered according to some external criteria.
	 * 
	 * @return FALSE as the Units have no natural order.
	 */
	public boolean getOrdered() {
		return false;
	}

	/**
	 * Get the unit at the specified row.
	 * 
	 * @param index Index of the row.
	 * @return Unit matching row
	 */
	protected Unit getUnit(int index) {
		if (index > (getRowCount() - 1))
			throw new IllegalStateException("Invalid index " + index + " for " + getRowCount() + " rows");
		int idx = -1;
		Iterator<Unit> it = units.iterator();
		while (it.hasNext()) {
			idx++;
			if (idx == index) {
				return it.next();
			}
			it.next();
		}
		throw new IllegalStateException("Could not find an index " + index);
	}

	/**
	 * Gets the index of the row a given unit is at.
	 * 
	 * @param unit the unit to find.
	 * @return the row index or -1 if not in table model.
	 */
	protected int getUnitIndex(Unit unit) {
		if ((units != null) && units.contains(unit))
			return getIndex(unit);
		else
			return -1;
	}

	/**
	 * Get the unit at the specified row.
	 * 
	 * @param row Indexes of Unit to retrieve.
	 * @return Unit at specified position.
	 */
	public Object getObject(int row) {
		return getUnit(row);
	}

	/**
	 * Gets the model count string.
	 */
	public String getCountString() {
		return " " + Msg.getString(countingMsgKey,
//				Integer.toString(getUnitNumber())
				getUnitNumber());
	}

	/**
	 * Prepares the model for deletion.
	 */
	public void destroy() {
		if (units != null) {
			clear();
		}
		units = null;
	}

	@Override
	public boolean equals(Object o) {
		boolean result = true;

		if (o instanceof UnitTableModel) {
			UnitTableModel oModel = (UnitTableModel) o;

			if (!units.equals(oModel.units))
				result = false;

			if (!name.equals(oModel.name))
				result = false;

			if (!countingMsgKey.equals(oModel.countingMsgKey))
				result = false;
		} else
			result = false;

		return result;
	}

	/**
	 * Inner class for updating table after removing units.
	 */
	private class RemoveUnitTableUpdater implements Runnable {

		private int index;

		private RemoveUnitTableUpdater(int index) {
			this.index = index;
		}

		public void run() {
			fireTableRowsDeleted(index, index);
		}
	}
}
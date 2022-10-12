/**
 * Mars Simulation Project
 * UnitTableModel.java
 * @date 2021-12-07
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.UnitType;

/**
 * The UnitTableModel that maintains a table model of Units objects. It is only
 * a partial implementation of the TableModel interface.
 */
@SuppressWarnings("serial")
abstract public class UnitTableModel extends AbstractTableModel implements MonitorModel, UnitListener {

	/**
	 * UnitManagerListener inner class.
	 */
	private class LocalUnitManagerListener implements UnitManagerListener {

		/**
		 * Catches unit manager update event.
		 *
		 * @param event the unit event.
		 */
		public void unitManagerUpdate(UnitManagerEvent event) {
			Unit unit = event.getUnit();

			switch(event.getEventType()) {
				case ADD_UNIT:
					if (!containsUnit(unit)) {
						addUnit(unit);
					}
					break;

				case REMOVE_UNIT:
					if (containsUnit(unit)) {
						removeUnit(unit);
					}
					break;
			}
		}
	}

	/** Model name. */
	private String name;
	/** Key for calling the internationalized text that counts the number of units. */
	private String countingMsgKey;
	/** Names of the displayed columns. */
	private String [] columnNames;

	/** Types of the individual columns. */
	private Class<?>[] columnTypes;
	/** Collection of units. */
	private List<Unit> units;

	private UnitManagerListener umListener;

	private UnitType unitType;

	private boolean fireEnabled = true;

	protected static GameMode mode = GameManager.getGameMode();
	
	protected static UnitManager unitManager = Simulation.instance().getUnitManager();

	/**
	 * Constructor.
	 *
	 * @param unitType		 Type of Unit being displayed
	 * @param name           Name of the model.
	 * @param countingMsgKey {@link String} key for calling the internationalized
	 *                       text that counts the number of units. should be a valid
	 *                       key to an existing value in
	 *                       <code>messages.properties</code>.
	 * @param names          Names of the columns displayed.
	 * @param types          The Classes of the individual columns.
	 */
	protected UnitTableModel(UnitType unitType, String name, String countingMsgKey, String names[], Class<?> types[]) throws Exception {
		// Initialize data members
		this.unitType = unitType;
		this.name = name;
		this.countingMsgKey = countingMsgKey;
		this.units = new ArrayList<>();
		this.columnNames = names;
		this.columnTypes = types;
	}

	protected void listenForUnits() {
		umListener = new LocalUnitManagerListener();
		unitManager.addUnitManagerListener(unitType, umListener);
	}

	/**
	 * Reset thhe monitored Units
	 * @param newUnits
	 */
	protected void resetUnits(Collection<? extends Unit> newUnits) {
		fireEnabled = false;

		if (!units.isEmpty()) {
			// Take a shallow copy as going to be removing items
			List<Unit> oldUnits = new ArrayList<>(units);
			for(Unit old : oldUnits) {
				removeUnit(old);
			}
		}

		for(Unit newUnit : newUnits) {
			addUnit(newUnit);
		}

		// Just fire one table event for teh whole table
		fireEnabled = true;
		fireTableDataChanged();
	}

	/**
	 * Add a unit to the model.
	 *
	 * @param newUnit Unit to add to the model.
	 */
	protected void addUnit(Unit newUnit) {
		if (!units.contains(newUnit)) {
			units.add(newUnit);
			newUnit.addUnitListener(this);

			if (fireEnabled) {
				int idx = units.indexOf(newUnit);
				fireTableRowsInserted(idx, idx);
			}
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
			oldUnit.removeUnitListener(this);

			if (fireEnabled)
				SwingUtilities.invokeLater(new RemoveUnitTableUpdater(index));
		}
	}

	/**
	 * Gets the index value of a given unit.
	 *
	 * @param unit the unit
	 * @return the index value.
	 */
	protected int getIndex(Unit unit) {
		return units.indexOf(unit);
	}

	/**
	 * Clears out units from the model.
	 */
	protected void clear() {
		for(Unit old: units) {
			old.removeUnitListener(this);
		}
		units.clear();
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

	protected List<Unit> getUnits() {
		return units;
	}

	protected int getSize() {
		return units.size();
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
	@Override
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
	@Override
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
		return units.size();
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
		return units.get(index);
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
		return "  " + Msg.getString(countingMsgKey, getRowCount());
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
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
	 * Prepares the model for deletion.
	 */
	public void destroy() {
		if (units != null) {
			clear();
		}
		units = null;
		if (umListener != null) {
			unitManager.removeUnitManagerListener(unitType, umListener);
		}
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

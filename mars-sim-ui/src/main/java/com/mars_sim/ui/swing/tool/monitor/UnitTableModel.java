/**
 * Mars Simulation Project
 * UnitTableModel.java
 * @date 2021-12-07
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.Unit;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitManagerEvent;
import com.mars_sim.core.UnitManagerListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * The UnitTableModel that maintains a table model of Units objects. It is only
 * a partial implementation of the TableModel interface.
 */
@SuppressWarnings("serial")
public abstract class UnitTableModel<T extends Unit> extends EntityTableModel<T>
		implements EntityListener {

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

			@SuppressWarnings("unchecked") T unit = (T) event.getUnit();
			switch(event.getEventType()) {
				case ADD_UNIT:
					addEntity(unit);
					break;

				case REMOVE_UNIT:
					removeEntity(unit);
					break;
			}
		}
	}

	private UnitManagerListener umListener;

	private UnitType unitType;

	private boolean monitorUnits = false;
	
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
	 * @param names          Details of the columns displayed.
	 */
	protected UnitTableModel(UnitType unitType, String name, String countingMsgKey, ColumnSpec[] columns) {
		super(name, countingMsgKey, columns);

		// Initialize data members
		this.unitType = unitType;
	}

	protected void listenForUnits() {
		umListener = new LocalUnitManagerListener();
		unitManager.addUnitManagerListener(unitType, umListener);
	}

	
	/**
	 * Sets whether the changes to the Entities should be monitor for change. Sets up the 
	 * Unitlisteners for the Units in the table.
	 * 
	 * @param activate 
	 */
    public void setMonitorEntites(boolean activate) {
		if (activate != monitorUnits) {
			if (activate) {
				for(Unit u : getEntities()) {
					u.addUnitListener(this);
				}
			}
			else {
				for(Unit u : getEntities()) {
					u.removeUnitListener(this);
				}
			}
			monitorUnits = activate;
		}
	}

	/**
	 * Adds a unit to the model. Attaches a listener to the Unit.
	 *
	 * @param newUnit Unit to add to the model.
	 */
	@Override
	protected boolean addEntity(T newUnit) {
		boolean added = super.addEntity(newUnit);
		if (added && monitorUnits) {
			newUnit.addUnitListener(this);
		}
		return added;
	}

	/**
	 * Removes a unit from the model.
	 *
	 * @param oldUnit Unit to remove from the model.
	 */
	@Override
	protected void removeEntity(T oldUnit) {
		super.removeEntity(oldUnit);
		oldUnit.removeUnitListener(this);
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		super.destroy();
		if (umListener != null) {
			unitManager.removeUnitManagerListener(unitType, umListener);
		}
	}
}

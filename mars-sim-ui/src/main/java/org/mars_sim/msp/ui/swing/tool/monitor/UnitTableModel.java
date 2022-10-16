/**
 * Mars Simulation Project
 * UnitTableModel.java
 * @date 2021-12-07
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

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
public abstract class UnitTableModel<T extends Unit> extends EntityTableModel<T>
		implements UnitListener {

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
			T unit = (T) event.getUnit();

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
	 * @param names          Names of the columns displayed.
	 * @param types          The Classes of the individual columns.
	 */
	protected UnitTableModel(UnitType unitType, String name, String countingMsgKey, String[] names, Class<?>[] types) {
		super(name, countingMsgKey, names, types);

		// Initialize data members
		this.unitType = unitType;
	}

	protected void listenForUnits() {
		umListener = new LocalUnitManagerListener();
		unitManager.addUnitManagerListener(unitType, umListener);
	}

	
	/**
	 * Set whether the changes to the Entities should be monitor for change. Set up the 
	 * Unitlisteners for the Units in the table.
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
	 * Add a unit to the model. Attach a listner to the Unit
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
	 * Remove a unit from the model.
	 *
	 * @param oldUnit Unit to remove from the model.
	 */
	@Override
	protected void removeEntity(T oldUnit) {
		super.removeEntity(oldUnit);
		oldUnit.removeUnitListener(this);
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

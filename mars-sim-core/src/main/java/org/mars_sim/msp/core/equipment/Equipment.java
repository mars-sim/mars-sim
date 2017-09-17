/**
 * Mars Simulation Project
 * Equipment.java
 * @version 3.1.0 2017-09-07
 * @author Scott Davis
 */

package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageInfo;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The Equipment class is an abstract class that represents
 * a useful piece of equipment, such as a EVA suit or a
 * medpack.
 */
public abstract class Equipment
extends Unit
implements Salvagable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private boolean isSalvaged;
	private SalvageInfo salvageInfo;

	private Unit lastOwner;
	//private String name;

	/** Constructs an Equipment object
	 *  @param name the name of the unit
	 *  @param location the unit's location
	 */
	protected Equipment(String name, Coordinates location) {
		super(name, location);

		//this.name = name;
		// Initialize data members.
		isSalvaged = false;
		salvageInfo = null;
	}

	/**
	 * Gets a collection of people affected by this entity.
	 * @return person collection
	 */
	public Collection<Person> getAffectedPeople() {
		Collection<Person> people = new ConcurrentLinkedQueue<Person>();

		// Check all people.
		Iterator<Person> i = Simulation.instance().getUnitManager().getPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			Task task = person.getMind().getTaskManager().getTask();

			// Add all people maintaining this equipment.
			if (task instanceof Maintenance) {
				if (((Maintenance) task).getEntity() == this) {
					if (!people.contains(person)) people.add(person);
				}
			}

			// Add all people repairing this equipment.
			if (task instanceof Repair) {
				if (((Repair) task).getEntity() == this) {
					if (!people.contains(person)) people.add(person);
				}
			}
		}

		return people;
	}

	/**
	 * Checks if the item is salvaged.
	 * @return true if salvaged.
	 */
	public boolean isSalvaged() {
		return isSalvaged;
	}

	//public String getName() {
	//	return name;
	//}
	/**
	 * Indicate the start of a salvage process on the item.
	 * @param info the salvage process info.
	 * @param settlement the settlement where the salvage is taking place.
	 */
	public void startSalvage(SalvageProcessInfo info, Settlement settlement) {
		salvageInfo = new SalvageInfo(this, info, settlement);
		isSalvaged = true;
	}

	/**
	 * Gets the salvage info.
	 * @return salvage info or null if item not salvaged.
	 */
	public SalvageInfo getSalvageInfo() {
		return salvageInfo;
	}

	/**
	 * Get settlement equipment is at, null if not at a settlement
	 *
	 * @return the equipment's settlement
	 */
	// 2017-03-19 Add getSettlement()
	@Override
	public Settlement getSettlement() {
		if (getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
			Settlement settlement = (Settlement) getContainerUnit();
			return settlement;
			//return (Settlement) getContainerUnit();
		}

		else if (getLocationSituation() == LocationSituation.OUTSIDE)
			return null;

		else if (getLocationSituation() == LocationSituation.IN_VEHICLE) {
			Vehicle vehicle = (Vehicle) getContainerUnit();
			// Note: a vehicle's container unit may be null if it's outside a settlement
			Settlement settlement = (Settlement) vehicle.getContainerUnit();
			return settlement;
		}

		else if (getLocationSituation() == LocationSituation.BURIED) {
			return null;
		}

		else {
			System.err.println("Error in determining " + getName() + "'s getSettlement() ");
			return null;
		}
	}

	/**
	 * Gets the building the equipment is located at, null if outside of a
	 * settlement
	 *
	 * @return building
	 */
	// 2017-03-19 Added getBuildingLocation()
	@Override
	public Building getBuildingLocation() {
		// TODO: what building do you want to store equipment ?
		return null;
	}

	/**
	 * Get vehicle the equipment is in, null if not in vehicle
	 *
	 * @return the equipment's vehicle
	 */
	// 2017-03-19 Add getSettlement()
	@Override
	public Vehicle getVehicle() {
		if (getLocationSituation() == LocationSituation.IN_VEHICLE)
			return (Vehicle) getContainerUnit();
		else
			return null;
	}

	/**
	 * Get the equipment's location
	 */
	@Override
	public LocationSituation getLocationSituation() {
		Unit container = getContainerUnit();
		if (container instanceof Settlement)
			return LocationSituation.IN_SETTLEMENT;
		else if (container instanceof Vehicle)
			return LocationSituation.IN_VEHICLE;
		else if (container == null)
			return LocationSituation.OUTSIDE;
		else {
			return null;
		}
	}

	public void setLastOwner(Unit unit) {
		lastOwner = unit;
	}
	
	public Unit getLastOwner() {
		return lastOwner;
	}
	
	@Override
	public void destroy() {
		super.destroy();
		if (salvageInfo != null) salvageInfo.destroy();
		salvageInfo = null;
	}
}
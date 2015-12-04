/**
 * Mars Simulation Project
 * Unit.java
 * @version 3.07 2014-12-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;

/**
 * The Unit class is the abstract parent class to all units in the
 * Simulation.  Units include people, vehicles and settlements.
 * This class provides data members and methods common to all units.
 */
public abstract class Unit
implements Serializable, Comparable<Unit> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Unit.class.getName());

	// Data members
	/** Unit location coordinates. */
	private Coordinates location;
	/** TODO Unit name needs to be internationalized. */
	private String name;
	/** TODO Unit description needs to be internationalized. */
	private String description;
	/** The mass of the unit without inventory. */
	private double baseMass;
	/** The unit's inventory. */
	private Inventory inventory;
	/** The unit containing this unit. */
	private Unit containerUnit;
	/** Unit listeners. */
	private transient List<UnitListener> listeners;// = Collections.synchronizedList(new ArrayList<UnitListener>());

	/**
	 * Constructor.
	 * @param name {@link String} the name of the unit
	 * @param location {@link Coordinates} the unit's location
	 */
	public Unit(String name, Coordinates location) {
		listeners = Collections.synchronizedList(new ArrayList<UnitListener>()); // Unit listeners.

		// Initialize data members from parameters
		this.name = name;
		description = name;
		baseMass = Double.MAX_VALUE;

		inventory = new Inventory(this);

		this.location = new Coordinates(0D, 0D);
		this.location.setCoords(location);
		this.inventory.setCoordinates(location);
	}

	/**
	 * Change the unit's name
	 * @param name new name
	 */
	public final void changeName(String name) {
		this.name = name;
	}

	/**
	 * Gets the unit's UnitManager
	 * @return {@link UnitManager} the unit's unit manager
	 */
	public UnitManager getUnitManager() {
		return Simulation.instance().getUnitManager();
	}

	/**
	 * Gets the unit's name
	 * @return the unit's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the unit's name
	 * @param name new name
	 */
	public final void setName(String name) {
		this.name = name;
		fireUnitUpdate(UnitEventType.NAME_EVENT, name);
	}

	/**
	 * Gets the unit's description
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the unit's description.
	 * @param description new description.
	 */
	protected final void setDescription(String description) {
		this.description = description;
		fireUnitUpdate(UnitEventType.DESCRIPTION_EVENT, description);
	}

	/**
	 * Gets the unit's location
	 * @return the unit's location
	 */
	public Coordinates getCoordinates() {
		return location;
	}

	/**
	 * Sets unit's location coordinates
	 * @param newLocation the new location of the unit
	 */
	public void setCoordinates(Coordinates newLocation) {
		// if (location == null) location = new Coordinates(0D, 0D);
		location.setCoords(newLocation);
		inventory.setCoordinates(newLocation);
		fireUnitUpdate(UnitEventType.LOCATION_EVENT, newLocation);
	}

	/**
	 * Time passing for unit.
	 * Unit should take action or be modified by time as appropriate.
	 * @param time the amount of time passing (in millisols)
	 * @throws Exception if error during time passing.
	 */
	public void timePassing(double time) {
	}

	/**
	 * Gets the unit's inventory
	 * @return the unit's inventory object
	 */
	public Inventory getInventory() {
		return inventory;
	}

	/**
	 * Gets the unit's container unit.
	 * Returns null if unit has no container unit.
	 * @return the unit's container unit
	 */
	public Unit getContainerUnit() {
		return containerUnit;
	}

	/**
	 * Gets the topmost container unit that owns this unit.
	 * Returns null if unit has no container unit (meaning that he's outside)
	 * @return the unit's topmost container unit
	 */
	public Unit getTopContainerUnit() {
		Unit topUnit = containerUnit;
		if (topUnit != null) {
			while (topUnit.containerUnit != null) {
				topUnit = topUnit.containerUnit;
			}
		}
		else {
			if (this instanceof Person) {
				Person person = (Person) this;
				person.getAssociatedSettlement();
			}
			else if (this instanceof Robot) {
				Robot robot = (Robot) this;
				robot.getAssociatedSettlement();
			}
			
		}
		return topUnit;
	}

	/**
	 * Sets the unit's container unit.
	 * @param containerUnit the unit to contain this unit.
	 */
	public void setContainerUnit(Unit containerUnit) {
		this.containerUnit = containerUnit;
		fireUnitUpdate(UnitEventType.CONTAINER_UNIT_EVENT, containerUnit);
	}

	/**
	 * Gets the unit's mass including inventory mass.
	 * @return mass of unit and inventory
	 * @throws Exception if error getting the mass.
	 */
	public double getMass() {
		return baseMass + inventory.getTotalInventoryMass(false);
	}

	/**
	 * Sets the unit's base mass.
	 * @param baseMass mass (kg)
	 */
	protected final void setBaseMass(double baseMass) {
		this.baseMass = baseMass;
		fireUnitUpdate(UnitEventType.MASS_EVENT);
	}

	/**
	 * Gets the base mass of the unit.
	 * @return base mass (kg).
	 */
	public double getBaseMass() {
		return baseMass;
	}

	/**
	 * String representation of this Unit.
	 * @return The units name.
	 */
	@Override
	public String toString() {
		return name;
	}

	public synchronized boolean hasUnitListener(UnitListener listener) {
		if(listeners == null) return false;
		return listeners.contains(listener);
	}

	/**
	 * Adds a unit listener
	 * @param newListener the listener to add.
	 */
	public synchronized final void addUnitListener(UnitListener newListener) {
		if(newListener == null) throw new IllegalArgumentException();
		if (listeners == null) listeners = Collections.synchronizedList(new ArrayList<UnitListener>());

		if (!listeners.contains(newListener)) {
			listeners.add(newListener);
		}
		else {
			try {
				throw new IllegalStateException(
					Msg.getString(
						"Unit.log.alreadyContainsListener", //$NON-NLS-1$
						newListener.getClass().getName(),
						newListener.toString()
					)
				);
			}
			catch (Exception e){
				e.printStackTrace();
				logger.log(Level.SEVERE,Msg.getString("Unit.log.addingListenerDupe"),e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Removes a unit listener
	 * @param oldListener the listener to remove.
	 */
	public synchronized final void removeUnitListener(UnitListener oldListener) {
		if(oldListener == null) throw new IllegalArgumentException();

		if(listeners == null){
			listeners = Collections.synchronizedList(new ArrayList<UnitListener>());
		}
		if(listeners.size() < 1) return;
		listeners.remove(oldListener);
	}

	/**
	 * Fire a unit update event.
	 * @param updateType the update type.
	 */
	public final void fireUnitUpdate(UnitEventType updateType) {
		fireUnitUpdate(updateType, null);
	}

	/**
	 * Fire a unit update event.
	 * @param updateType the update type.
	 * @param target the event target object or null if none.
	 */
	public final void fireUnitUpdate(UnitEventType updateType, Object target) {
		if (listeners == null || listeners.size() < 1) {
			// listeners = Collections.synchronizedList(new ArrayList<UnitListener>());
			// we don't do anything if there's no listeners attached
			return;
		}
		final UnitEvent ue = new UnitEvent(this, updateType, target);
		synchronized(listeners) {
			Iterator<UnitListener> i = listeners.iterator();
			while (i.hasNext()) {
				i.next().unitUpdate(ue);
			}
		}
	}

	/**
	 * Compares this object with the specified object for order.
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less than,
	 * equal to, or greater than the specified object.
	 */
	@Override
	public int compareTo(Unit o) {
		return name.compareToIgnoreCase(o.name);
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		location = null;
		name = null;
		description = null;
		inventory.destroy();
		inventory = null;
		containerUnit = null;
		if (listeners != null) listeners.clear();
		listeners = null;
	}
}
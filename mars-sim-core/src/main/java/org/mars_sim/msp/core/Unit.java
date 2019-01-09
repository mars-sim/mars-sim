/**
 * Mars Simulation Project
 * Unit.java
 * @version 3.1.0 2017-03-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.location.LocationSituation;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.location.LocationTag;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.Structure;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.vehicle.Vehicle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

/**
 * The Unit class is the abstract parent class to all units in the Simulation.
 * Units include people, vehicles and settlements. This class provides data
 * members and methods common to all units.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.PROPERTY, property = "@class")
@JsonSubTypes({ @Type(value = Person.class, name = "person"), 
				@Type(value = Structure.class, name = "structure"),
				@Type(value = Vehicle.class, name = "vehicle"),
				@Type(value = Equipment.class, name = "equipment"),})
public abstract class Unit implements Serializable, Comparable<Unit> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Unit.class.getName());

	// private static String sourceName =
	// logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
	// logger.getName().length());
	// static unit identifier
	private static int unitIdentifer = 0;

	// Data members
//	private static boolean once = true;
	// Unique identifier
	private int identifier;
	// The unit's location code
	private int locationCode = 110_000;
	
	/** The mass of the unit without inventory. */
	private double baseMass;
	
	/** TODO Unit name needs to be internationalized. */
	private String name;
	/** TODO Unit description needs to be internationalized. */
	private String description;

	/** The unit's location tag. */
	private LocationTag tag;
	/** The unit's inventory. */
	@JsonIgnore
	private Inventory inventory;
	/** The unit containing this unit. */
	protected Unit containerUnit;
	/** The cache of containerUnit. */
//	protected Unit containerUnitCache;
	/** Unit location coordinates. */
	private Coordinates location;

	protected LocationStateType currentStateType;

	/** Unit listeners. */
	private transient List<UnitListener> listeners;// = Collections.synchronizedList(new ArrayList<UnitListener>());

	protected static Simulation sim = Simulation.instance();
	protected static Mars mars;
	protected static MarsClock marsClock;
	protected static EarthClock earthClock;
	protected static MasterClock masterClock;
	protected static MarsSurface marsSurface;
	protected static UnitManager unitManager;
	
	
//	private static MarsSurface marsSurface;
	
	/**
	 * Must be synchronised to prevent duplicate ids being assigned via different
	 * threads.
	 * 
	 * @return
	 */
	private static synchronized int getNextIdentifier() {
		return unitIdentifer++;
	}

	/**
	 * Constructor.
	 * 
	 * @param name     {@link String} the name of the unit
	 * @param location {@link Coordinates} the unit's location
	 */
	public Unit(String name, Coordinates location) {
		listeners = Collections.synchronizedList(new ArrayList<UnitListener>()); // Unit listeners.

		this.identifier = getNextIdentifier();
		
		// Creates a new location tag instance for each unit
		tag = new LocationTag(this);

		// Initialize data members from parameters
		this.name = name;
		this.description = name;
		this.baseMass = 0;//Double.MAX_VALUE;

		this.inventory = new Inventory(this);
		this.location = new Coordinates(0D, 0D);

		if (location != null) {
			this.location.setCoords(location);
			this.inventory.setCoordinates(location);
		}
		
		// Define the default LocationStateType of an unit at the start of the sim
		if (this instanceof Robot)
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
		else if (this instanceof Equipment)
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
		else if (this instanceof Person)
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
		else if (this instanceof Building)
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
		else if (this instanceof Vehicle)
			currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;
		else if (this instanceof Settlement)
			currentStateType = LocationStateType.OUTSIDE_ON_MARS;
		
		mars = sim.getMars();
//		masterClock = sim.getMasterClock();
//		marsClock = masterClock.getMarsClock();
//		earthClock = masterClock.getEarthClock();
//		marsSurface = mars.getMarsSurface();
//		unitManager = sim.getUnitManager();
	}

	/**
	 * Get the unique identifier for this unit
	 * 
	 * @return Identifier
	 */
	public int getIdentifier() {
		return identifier;
	}

	/**
	 * Change the unit's name
	 * 
	 * @param newName new name
	 */
	public final void changeName(String newName) {
		String oldName = this.name;
		Unit unit = this;
		if (unit instanceof Settlement) {
			SimulationConfig.instance().getSettlementConfiguration().changeSettlementName(oldName, newName);
		}
		this.name = newName;
	}

	/**
	 * Gets the unit's UnitManager
	 * 
	 * @return {@link UnitManager} the unit's unit manager
	 */
	public UnitManager getUnitManager() {
		return Simulation.instance().getUnitManager();
	}

	/**
	 * Gets the unit's name
	 * 
	 * @return the unit's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the unit's nickname
	 * 
	 * @return the unit's nickname
	 */
	public String getNickName() {
		return name;
	}

	/**
	 * Gets the unit's shortened name
	 * 
	 * @return the unit's shortened name
	 */
	public String getShortenedName() {
		name = name.trim();
		int num = name.length();

		boolean hasSpace = name.matches("^\\s*$");

		if (hasSpace) {
			int space = name.indexOf(" ");

			String oldFirst = name.substring(0, space);
			String oldLast = name.substring(space + 1, num);
			String newFirst = oldFirst;
			String newLast = oldLast;
			String newName = name;

			if (num > 20) {

				if (oldFirst.length() > 10) {
					newFirst = oldFirst.substring(0, 10);
				} else if (oldLast.length() > 10) {
					newLast = oldLast.substring(0, 10);
				}
				newName = newFirst + " " + newLast;

			}

			return newName;
		}

		else
			return name;
	}

	/**
	 * Sets the unit's name
	 * 
	 * @param name new name
	 */
	public void setName(String name) {
		this.name = name;
		fireUnitUpdate(UnitEventType.NAME_EVENT, name);
	}

	/**
	 * Gets the unit's description
	 * 
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the unit's description.
	 * 
	 * @param description new description.
	 */
	protected void setDescription(String description) {
		this.description = description;
		fireUnitUpdate(UnitEventType.DESCRIPTION_EVENT, description);
	}

	/**
	 * Gets the unit's location
	 * 
	 * @return the unit's location
	 */
	public Coordinates getCoordinates() {
		return location;
	}

	/**
	 * Sets unit's location coordinates
	 * 
	 * @param newLocation the new location of the unit
	 */
	public void setCoordinates(Coordinates newLocation) {
		// if (location == null) location = new Coordinates(0D, 0D);
		location.setCoords(newLocation);
		inventory.setCoordinates(newLocation);
		fireUnitUpdate(UnitEventType.LOCATION_EVENT, newLocation);
	}

	/**
	 * Time passing for unit. Unit should take action or be modified by time as
	 * appropriate.
	 * 
	 * @param time the amount of time passing (in millisols)
	 * @throws Exception if error during time passing.
	 */
	public void timePassing(double time) {
	}
	
	/**
	 * Gets the unit's inventory
	 * 
	 * @return the unit's inventory object
	 */
	public Inventory getInventory() {
		return inventory;
	}

	/**
	 * Gets the unit's container unit. Returns null if unit has no container unit.
	 * 
	 * @return the unit's container unit
	 */
	public Unit getContainerUnit() {
//		if (once && containerUnit != null && containerUnit instanceof MarsSurface) {
//			System.out.println(containerUnit + " has " + containerUnit.getCode());
//			once = false;
//		}
		return containerUnit;
	}

	/**
	 * Gets the topmost container unit that owns this unit (Settlement, Vehicle, Person or Robot) 
	 * If it's on the surface of Mars, then the topmost container is MarsSurface. 
	 * 
	 * @return the unit's topmost container unit
	 */
	public Unit getTopContainerUnit() {
		Unit topUnit = containerUnit;
		if (!(topUnit instanceof MarsSurface)) {
			while (topUnit.containerUnit != null && !(topUnit.containerUnit instanceof MarsSurface)) {
				topUnit = topUnit.containerUnit;
			}
		}

		return topUnit;
	}

	public void setTopContainerUnit(Unit u) {
		Unit topUnit = containerUnit;
		if (!(topUnit instanceof MarsSurface)) {
			while (topUnit.containerUnit != null && !(topUnit.containerUnit instanceof MarsSurface)) {
				topUnit = topUnit.containerUnit;
			}
		}

		containerUnit.setContainerUnit(u);
	}
	
	/**
	 * Sets the unit's container unit.
	 * 
	 * @param newContainer the unit to contain this unit.
	 */
	public void setContainerUnit(Unit newContainer) {	
		if (newContainer == null && mars != null) // to pass maven test
			newContainer = mars.getMarsSurface();//marsSurface;
			
		if (this instanceof Person || this instanceof Robot)
			updatePersonRobotState(newContainer);
		else if (this instanceof Equipment)
			updateEquipmentState(newContainer);
		else if (this instanceof Vehicle)
			updateVehicleState(newContainer);
		else if (this instanceof Building)
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
		else if (this instanceof Settlement)
			currentStateType = LocationStateType.OUTSIDE_ON_MARS;
		
//		if (containerUnit != null)
//			containerUnitCache = containerUnit;
		
		this.containerUnit = newContainer;

		fireUnitUpdate(UnitEventType.CONTAINER_UNIT_EVENT, newContainer);
	}

	/**
	 * Updates the location state type of a person or robot
	 * 
	 * @param newContainer
	 */
	public void updatePersonRobotState(Unit newContainer) {
		Unit oldContainer = this.containerUnit;

		if (newContainer == null)
			logger.severe("updatePersonRobotState(): " + getName() + " has an null newContainer");
		
		// Case 1a : exiting a settlement
		if (oldContainer instanceof Settlement && newContainer instanceof MarsSurface)
			currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;

		// Case 1b (reverse of Case 1a)
		else if (oldContainer instanceof MarsSurface && newContainer instanceof Settlement)
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;

		// Case 2a : boarding a vehicle parked inside a garage
		else if (oldContainer instanceof Settlement && newContainer instanceof Vehicle)
			// only if the vehicle is inside a garage can this happen
			currentStateType = LocationStateType.INSIDE_VEHICLE;

		// Case 2b (reverse of Case 2a)
		else if (oldContainer instanceof Vehicle && newContainer instanceof Settlement)
			// only if the vehicle is inside a garage can this happen
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;

		// Case 3a : to board a vehicle from outside
		else if (oldContainer instanceof MarsSurface && newContainer instanceof Vehicle)
			currentStateType = LocationStateType.INSIDE_VEHICLE;

		// Case 3b and 3c
		else if (oldContainer instanceof Vehicle && newContainer instanceof MarsSurface) {
			if (((Vehicle) oldContainer).getLocationStateType() == LocationStateType.OUTSIDE_SETTLEMENT_VICINITY)
				// Case 3b : a person exits a vehicle that is within the settlement vicinity
				currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;
			else
				// Case 3c : a person exits a vehicle that is outside on Mars on a mission
				currentStateType = LocationStateType.OUTSIDE_ON_MARS;
		}
		// Case 4a and 4b : a person walks from the settlement vicinity to outside on
		// Mars
		// Unrealistic and forbidden at this point.

	}

	/**
	 * Updates the location state type of an equipment
	 * 
	 * @param newContainer
	 */
	public void updateEquipmentState(Unit newContainer) {
		Unit oldContainer = this.containerUnit;
		if (newContainer == null)
			logger.severe("updateEquipmentState(): " + getName() + " has an null newContainer");
		// Note : a person or a robot must be the carrier of an equipment

		// Case 1a
		if (oldContainer instanceof Settlement && newContainer instanceof Person)
			currentStateType = LocationStateType.ON_A_PERSON;

		// Case 2b (reverse of Case 5a)
		else if (oldContainer instanceof Person && newContainer instanceof Settlement)
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;

		// Case 2a
		else if (oldContainer instanceof Person && newContainer instanceof Vehicle)
			currentStateType = LocationStateType.INSIDE_VEHICLE;

		// Case 2b
		else if (oldContainer instanceof Vehicle && newContainer instanceof Person)
			currentStateType = LocationStateType.ON_A_PERSON;

		// Case 3a
		else if (oldContainer instanceof MarsSurface && newContainer instanceof Person)
			currentStateType = LocationStateType.ON_A_PERSON;

		// Case 3b and 3c
		else if (oldContainer instanceof Person && newContainer instanceof MarsSurface) {
			// Case 3b (reverse of Case 3a)
			if (((Person) oldContainer).getLocationStateType() == LocationStateType.OUTSIDE_SETTLEMENT_VICINITY)
				// this equipment can be placed in the settlement vicinity (Note : a new field
				// work feature for future)
				currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;
			else
				// Case 3c (reverse of Case 3a)
				// this equipment can be placed out there on the surface of Mars (Note : a new
				// field work feature for future)
				currentStateType = LocationStateType.OUTSIDE_ON_MARS;
		}
	}

	/**
	 * Updates the location state type of a vehicle
	 * 
	 * @param newContainer
	 */
	public void updateVehicleState(Unit newContainer) {
//		Unit oldContainer = this.containerUnit;
		if (newContainer == null)
			logger.severe("updateVehicleState(): " + getName() + " has an null newContainer");
		// Note : "within a settlement vicinity" is the intermediate state between being
		// "in a settlement" and being "outside on Mars"
		// Case 1
		if (newContainer instanceof MarsSurface) {
			if (newContainer instanceof Vehicle)
				// in case of luv
				currentStateType = LocationStateType.INSIDE_VEHICLE;
			else if (((Vehicle) this).getBuildingLocation() != null)
				// 1a : to park in a garage
				currentStateType = LocationStateType.INSIDE_SETTLEMENT;
			else // if (newContainer instanceof Settlement)
					// 1b : park outside of a settlement
				currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;
		}

		else { // if (newContainer == null)
				// Case 2 : leaving settlement vicinity to outside on mars
			currentStateType = LocationStateType.OUTSIDE_ON_MARS;
		}

	}

	/**
	 * Gets the unit's mass including inventory mass.
	 * 
	 * @return mass of unit and inventory
	 * @throws Exception if error getting the mass.
	 */
	public double getMass() {
		return baseMass + inventory.getTotalInventoryMass(false);
	}

	/**
	 * Sets the unit's base mass.
	 * 
	 * @param baseMass mass (kg)
	 */
	protected final void setBaseMass(double baseMass) {
		this.baseMass = baseMass;
		fireUnitUpdate(UnitEventType.MASS_EVENT);
	}

	/**
	 * Gets the base mass of the unit.
	 * 
	 * @return base mass (kg).
	 */
	public double getBaseMass() {
		return baseMass;
	}

	/**
	 * String representation of this Unit.
	 * 
	 * @return The units name.
	 */
	@Override
	public String toString() {
		// return name + " (" + identifier + ")";
		return name;
	}

    public String getCode() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }
    
	public synchronized boolean hasUnitListener(UnitListener listener) {
		if (listeners == null)
			return false;
		return listeners.contains(listener);
	}

	/**
	 * Adds a unit listener
	 * 
	 * @param newListener the listener to add.
	 */
	public synchronized final void addUnitListener(UnitListener newListener) {
		if (newListener == null)
			throw new IllegalArgumentException();
		if (listeners == null)
			listeners = Collections.synchronizedList(new ArrayList<UnitListener>());

		if (!listeners.contains(newListener)) {
			listeners.add(newListener);
		} else {
			try {
				throw new IllegalStateException(Msg.getString("Unit.log.alreadyContainsListener", //$NON-NLS-1$
						newListener.getClass().getName(), newListener.toString()));
			} catch (Exception e) {
				e.printStackTrace();
				logger.log(Level.SEVERE, Msg.getString("Unit.log.addingListenerDupe"), e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Removes a unit listener
	 * 
	 * @param oldListener the listener to remove.
	 */
	public synchronized final void removeUnitListener(UnitListener oldListener) {
		if (oldListener == null)
			throw new IllegalArgumentException();

		if (listeners == null) {
			listeners = Collections.synchronizedList(new ArrayList<UnitListener>());
		}
		if (listeners.size() < 1)
			return;
		listeners.remove(oldListener);
	}

	/**
	 * Fire a unit update event.
	 * 
	 * @param updateType the update type.
	 */
	public final void fireUnitUpdate(UnitEventType updateType) {
		fireUnitUpdate(updateType, null);
	}

	/**
	 * Fire a unit update event.
	 * 
	 * @param updateType the update type.
	 * @param target     the event target object or null if none.
	 */
	public final void fireUnitUpdate(UnitEventType updateType, Object target) {
		// logger.info("Unit's fireUnitUpdate() is on " +
		// Thread.currentThread().getName() + " Thread");

		if (listeners == null || listeners.size() < 1) {
			// listeners = Collections.synchronizedList(new ArrayList<UnitListener>());
			// we don't do anything if there's no listeners attached
			return;
		}
		final UnitEvent ue = new UnitEvent(this, updateType, target);
		synchronized (listeners) {
			// Iterator<UnitListener> i = listeners.iterator();
			// while (i.hasNext()) {
			// i.next().unitUpdate(ue);
			// }
			for (UnitListener u : listeners) {
				u.unitUpdate(ue);
			}
		}
	}

	/**
	 * Compares this object with the specified object for order.
	 * 
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	@Override
	public int compareTo(Unit o) {
		return name.compareToIgnoreCase(o.name);
	}

	public LocationSituation getLocationSituation() {
		return null;
	}

	public LocationStateType getLocationStateType() {
		return currentStateType;
	}

	public LocationTag getLocationTag() {
		return tag;
	}

	public Settlement getSettlement() {
		if (this instanceof Equipment) {
			return ((Equipment) this).getSettlement();
		} else if (this instanceof Person) {
			return ((Person) this).getSettlement();
		}

		else if (this instanceof Robot) {
			return ((Robot) this).getSettlement();
		}

		else if (this instanceof Vehicle) {
			return ((Vehicle) this).getSettlement();
		} 
		
		else
			return null;
	}

	public Building getBuildingLocation() {
		if (this instanceof Equipment) {
			return ((Equipment) this).getBuildingLocation();
		} 
		
		else if (this instanceof Person) {
			return ((Person) this).getBuildingLocation();
		}

		else if (this instanceof Robot) {
			return ((Robot) this).getBuildingLocation();
		}

		else if (this instanceof Vehicle) {
			return ((Vehicle) this).getBuildingLocation();
		} 
		
		else
			return null;
	}

	public Settlement getAssociatedSettlement() {
		if (this instanceof Equipment) {
			return ((Equipment) this).getAssociatedSettlement();
		} 
		
		else if (this instanceof Person) {
			return ((Person) this).getAssociatedSettlement();
		}

		else if (this instanceof Robot) {
			return ((Robot) this).getAssociatedSettlement();
		}

		else if (this instanceof Vehicle) {
			return ((Vehicle) this).getAssociatedSettlement();
		} 
		
		else
			return null;
	}

	public Vehicle getVehicle() {
		if (this instanceof Equipment) {
			return ((Equipment) this).getVehicle();
		} 
		
		else if (this instanceof Person) {
			return ((Person) this).getVehicle();
		}

		else if (this instanceof Robot) {
			return ((Robot) this).getVehicle();
		}

		else if (this instanceof Vehicle) {
			return ((Vehicle) this).getVehicle();
		} 
		
		else
			return null;
	}

	public int getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(int code) {
		locationCode = code;
	}
	
//	public void enter(LocationCodeType type) {
//		locationCode = locationCode + type.getCode();
//	}
//
//	public void exit(LocationCodeType type) {
//		locationCode = locationCode - type.getCode();
//	}


	/**
	 * Loads instances
	 * 
	 * @param m
	 */
	public static void setInstances(MasterClock c0, MarsClock c1, Simulation s, Mars m, MarsSurface ms, EarthClock e, UnitManager u) {
		masterClock = c0;
		marsClock = c1;
		sim = s;
		mars = m;
		marsSurface = ms;
		earthClock = e;
		unitManager = u;
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
		// if (listeners != null) listeners.clear();
		listeners = null;
	}

}
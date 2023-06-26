/*
 * Mars Simulation Project
 * Unit.java
 * @date 2023-05-09
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.mars_sim.msp.core.environment.Weather;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.location.LocationTag;
import org.mars_sim.msp.core.logging.Loggable;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MarsClockFormat;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The Unit class is the abstract parent class to all units in the simulation.
 * Units include people, vehicles and settlements. This class provides data
 * members and methods common to all units.
 */
public abstract class Unit implements Serializable, Loggable, UnitIdentifer, Comparable<Unit> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Unit.class.getName());

	public static final int MOON_UNIT_ID = -2;
	public static final int OUTER_SPACE_UNIT_ID = -1;
	public static final int MARS_SURFACE_UNIT_ID = 0;
	public static final Integer UNKNOWN_UNIT_ID = -3;

	// Data members
	/** The unit containing this unit. */
	protected Integer containerID = UNKNOWN_UNIT_ID;

	// Unique Unit identifier
	private int identifer;
	/** The mass of the unit without inventory. */
	private double baseMass;
	/** The last pulse applied. */
	private long lastPulse = 0;
	
	/** TODO Unit name needs to be internationalized. */
	private String name;
	/** TODO Unit description needs to be internationalized. */
	private String description;
	/** Commander's notes on this unit. */
	private String notes = "";
	/** The unit's location tag. */
	private LocationTag tag;
	/** The unit's coordinates. */
	private Coordinates location;

	/** The unit's current location state. */
	protected LocationStateType currentStateType;
	/** Unit listeners. */
	private transient Set<UnitListener> listeners;

	protected static SimulationConfig simulationConfig = SimulationConfig.instance();

	protected static MarsClock marsClock;
	protected static MasterClock masterClock;

	protected static UnitManager unitManager;
	protected static MissionManager missionManager;

	protected static Weather weather;

	// File for diagnostics output
	private static PrintWriter diagnosticFile = null;

	/**
	 * Enable the detailed diagnostics
	 *
	 * @throws FileNotFoundException
	 */
	public static void setDiagnostics(boolean diagnostics) throws FileNotFoundException {
		if (diagnostics) {
			if (diagnosticFile == null) {
				String filename = SimulationFiles.getLogDir() + "/unit-create.txt";
				diagnosticFile = new PrintWriter(filename);
				logger.config("Diagnostics enabled to " + filename);
			}
		} else if (diagnosticFile != null) {
			diagnosticFile.close();
			diagnosticFile = null;
		}
	}

	/**
	 * Log the creation of a new Unit
	 *
	 * @param entry
	 */
	private static void logCreation(Unit entry) {
		StringBuilder output = new StringBuilder();
		output.append(MarsClockFormat.getDateTimeStamp(marsClock)).append(" Id:").append(entry.getIdentifier())
				.append(" Type:").append(entry.getUnitType()).append(" Name:").append(entry.getName());

		synchronized (diagnosticFile) {
			diagnosticFile.println(output.toString());
			diagnosticFile.flush();
		}
	}

	public final int getIdentifier() {
		return identifer;
	}

	/**
	 * Constructor 1: the name and identifier are defined.
	 *
	 * @param name     {@link String} the name of the unit
	 * @param id Unit identifier
	 * @param containerId Identifier of the container
	 */
	protected Unit(String name, int id, int containerId) {
		// Initialize data members from parameters
		this.name = name;
		this.description = name;
		this.baseMass = 0;
		this.identifer = id;
		this.containerID = containerId;
		
		// For now, set currentStateType to MARS_SURFACE
		currentStateType = LocationStateType.MARS_SURFACE;
	}

	/**
	 * Constructor 2: where the name and location are defined.
	 *
	 * @param name     {@link String} the name of the unit
	 * @param location {@link Coordinates} the unit's location
	 */
	protected Unit(String name, Coordinates location) {
		// Initialize data members from parameters
		this.name = name;
		this.description = name;
		this.baseMass = 0;

		if (masterClock != null) {
			// Needed for maven test
			this.lastPulse = masterClock.getNextPulse() - 1;

			// Creates a new location tag instance for each unit
			tag = new LocationTag(this);
	
			// Calculate the new Identifier for this type
			identifer = unitManager.generateNewId(getUnitType());
		}

		// Define the default LocationStateType of an unit at the start of the sim
		// Instantiate Inventory as needed. Still needs to be pushed to subclass
		// constructors
		switch (getUnitType()) {
		case ROBOT:

		case CONTAINER:

		case PERSON:
			
		case BUILDING:

		case EVA_SUIT:
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
			break;
			
		case VEHICLE:
			currentStateType = LocationStateType.WITHIN_SETTLEMENT_VICINITY;
			containerID = (Integer) MARS_SURFACE_UNIT_ID;
			break;

		case SETTLEMENT:

		case CONSTRUCTION:
			currentStateType = LocationStateType.MARS_SURFACE;
			containerID = (Integer) MARS_SURFACE_UNIT_ID;
			break;
			
		case MARS:
			currentStateType = LocationStateType.MARS_SURFACE;
			containerID = (Integer) MARS_SURFACE_UNIT_ID;
			break;

		case MOON:
			currentStateType = LocationStateType.MOON;
			containerID = (Integer) MOON_UNIT_ID;
			break;
			
		default:
			throw new IllegalStateException("Do not know Unittype " + getUnitType());
		}

		if (location != null) {
			// Set the unit's location coordinates
			setCoordinates(location);
		}
		else
			// Set to (0, 0) when still initializing Settlement instance
			this.location = new Coordinates(0D, 0D);

		if (diagnosticFile != null) {
			logCreation(this);
		}
	}

	/**
	 * What logical UnitType of this object in terms of the management. This is NOT
	 * a direct mapping to the concrete subclass of Unit since some logical
	 * UnitTypes can have multiple implementation, e.g. Equipment.
	 *
	 * @return
	 */
	public abstract UnitType getUnitType();

	/**
	 * Is this time pulse valid for the Unit. Has it been already applied? The logic
	 * on this method can be commented out later on
	 *
	 * @param pulse Pulse to apply
	 * @return Valid to accept
	 */
	protected boolean isValid(ClockPulse pulse) {
		long newPulse = pulse.getId();
		boolean result = (newPulse > lastPulse);
		if (result) {
			long expectedPulse = lastPulse + 1;
			if (expectedPulse != newPulse) {
				// Pulse out of sequence; maybe missed one
				logger.warning(getName() + " expected pulse #" + expectedPulse + " but received " + newPulse);
			}
			lastPulse = newPulse;
		} else {
			// Seen already
			logger.severe(getName() + " rejected pulse #" + newPulse + ", last pulse was " + lastPulse);
		}
		return result;
	}

	/**
	 * Changes the unit's name.
	 *
	 * @param newName new name
	 */
	public final void changeName(String newName) {
		// Create an event here ?
		setName(newName);
	}

	/**
	 * Gets the unit's name.
	 *
	 * @return the unit's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the unit's nickname.
	 *
	 * @return the unit's nickname
	 */
	public String getNickName() {
		return name;
	}

	/**
	 * Gets the unit's shortened name.
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
	 * Sets the unit's name.
	 *
	 * @param name new name
	 */
	public void setName(String name) {
		this.name = name;
		fireUnitUpdate(UnitEventType.NAME_EVENT, name);
	}

	/**
	 * Gets the unit's description.
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
	 * Gets the commander's notes on this unit.
	 *
	 * @return notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * Sets the commander's notes on this unit.
	 *
	 * @param notes.
	 */
	public void setNotes(String notes) {
		this.notes = notes;
		fireUnitUpdate(UnitEventType.NOTES_EVENT, notes);
	}

	/**
	 * Gets the unit's location.
	 *
	 * @return the unit's location
	 */
	public Coordinates getCoordinates() {
		if (LocationStateType.MARS_SURFACE == currentStateType) {	
			return location;
		}
		else if (getUnitType() == UnitType.SETTLEMENT) {	
			return location;
		}
		else {
			return getTopContainerUnit().getCoordinates();
		}
	}

	/**
	 * Sets unit's location coordinates.
	 *
	 * @param newLocation the new location of the unit
	 */
	public void setCoordinates(Coordinates newLocation) {
		location = newLocation;
		fireUnitUpdate(UnitEventType.LOCATION_EVENT, newLocation);
	}

	/**
	 * Sets unit's location coordinates to null.
	 */
	public void setNullCoordinates() {
		location = null;
		// fireUnitUpdate(UnitEventType.LOCATION_EVENT, getTopContainerUnit().getCoordinates());
	}
	
	/**
	 * Gets the unit's container unit. Returns null if unit has no container unit.
	 *
	 * @return the unit's container unit
	 */
	public Unit getContainerUnit() {
		if (unitManager == null) // for maven test
			return null;
		return unitManager.getUnitByID(containerID);
	}

	public int getContainerID() {
		return containerID;
	}

	protected void setContainerID(Integer id) {
		containerID = id;
	}

	/**
	 * Gets the topmost container unit that owns this unit (Settlement, Vehicle,
	 * Person or Robot) If it's on the surface of Mars, then the topmost container
	 * is MarsSurface.
	 *
	 * @return the unit's topmost container unit
	 */
	public Unit getTopContainerUnit() {
		Unit topUnit = getContainerUnit();
		if (!(topUnit.getUnitType() == UnitType.MARS)) {
			while (topUnit != null && topUnit.getContainerUnit() != null
					&& !(topUnit.getContainerUnit().getUnitType() == UnitType.MARS)) {
				topUnit = topUnit.getContainerUnit();
			}
		}

		return topUnit;
	}

	/**
	 * Gets the topmost container unit that owns this unit (Settlement, Vehicle,
	 * Person or Robot) If it's on the surface of Mars, then the topmost container
	 * is MarsSurface.
	 *
	 * @return the unit's topmost container unit
	 */
	public int getTopContainerID() {

		int topID = getContainerUnit().getContainerID();
		if (topID != Unit.MARS_SURFACE_UNIT_ID) {
			while (topID != Unit.MARS_SURFACE_UNIT_ID) {
				topID = getContainerUnit().getContainerID();
			}
		}

		return topID;
	}

	/**
	 * Sets the unit's container unit.
	 *
	 * @param newContainer the unit to contain this unit.
	 */
	protected void setContainerUnit(Unit newContainer) {
		if (newContainer != null && newContainer.equals(getContainerUnit())) {
			return;
		}

		// 1. Set Coordinates
		if (newContainer == null || newContainer.getUnitType() == UnitType.MARS) {
			// Since it's on the surface of Mars,
			// First set its initial location to its old parent's location as it's leaving its parent.
			// Later it will move around and updates its coordinates by itself
			setCoordinates(getContainerUnit().getCoordinates());
		}
		else {
			// Null its coordinates since it's now slaved after its parent
			setNullCoordinates();
		}
		
		// 2. Set LocationStateType
		if (getUnitType() == UnitType.MARS) {
			currentStateType = LocationStateType.OUTER_SPACE;
			containerID = (Integer) OUTER_SPACE_UNIT_ID;
		} else if (getUnitType() == UnitType.CONSTRUCTION) {
			currentStateType = LocationStateType.MARS_SURFACE;
			containerID = (Integer) MARS_SURFACE_UNIT_ID;
		} else if (getUnitType() == UnitType.BUILDING) {
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
		} else {
			currentStateType = LocationStateType.UNKNOWN;
			containerID = (Integer) UNKNOWN_UNIT_ID;
		}

		// 3. Set containerID
		if (newContainer == null || newContainer.getIdentifier() == UNKNOWN_UNIT_ID) {
			containerID = (Integer) UNKNOWN_UNIT_ID;
		} else {
			containerID = newContainer.getIdentifier();
		}

		fireUnitUpdate(UnitEventType.CONTAINER_UNIT_EVENT, newContainer);
	}

	/**
	 * Gets the location state type based on the type of the new container unit.
	 *
	 * @param newContainer
	 * @return {@link LocationStateType}
	 */
	public LocationStateType getNewLocationState(Unit newContainer) {

		if (newContainer.getUnitType() == UnitType.SETTLEMENT) {
			if (getUnitType() == UnitType.PERSON 
					|| getUnitType() == UnitType.ROBOT
					|| getUnitType() == UnitType.CONTAINER)
				return LocationStateType.INSIDE_SETTLEMENT;
			else if (getUnitType() == UnitType.VEHICLE)
				return LocationStateType.WITHIN_SETTLEMENT_VICINITY;
		}

		if (newContainer.getUnitType() == UnitType.BUILDING)
			return LocationStateType.INSIDE_SETTLEMENT;

		if (newContainer.getUnitType() == UnitType.VEHICLE)
			return LocationStateType.INSIDE_VEHICLE;

		if (newContainer.getUnitType() == UnitType.CONSTRUCTION)
			return LocationStateType.MARS_SURFACE; // or WITHIN_SETTLEMENT_VICINITY

		if (newContainer.getUnitType() == UnitType.PERSON)
			return LocationStateType.ON_PERSON_OR_ROBOT;

		if (newContainer.getUnitType() == UnitType.MARS)
			return LocationStateType.MARS_SURFACE;

		return null;
	}

	/**
	 * Gets the unit's mass including inventory mass.
	 *
	 * @return mass of unit and inventory
	 * @throws Exception if error getting the mass.
	 */
	public double getMass() {
		// Note: this method will be overridden by those inheriting this Unit
		return baseMass;// + getStoredMass(); ?
	}

	/**
	 * Sets the unit's base mass.
	 *
	 * @param base mass (kg)
	 */
	public final void setBaseMass(double baseMass) {
		this.baseMass = baseMass;
		fireUnitUpdate(UnitEventType.MASS_EVENT);
	}

	/**
	 * Gets the base mass of the unit.
	 *
	 * @return base mass (kg)
	 */
	public double getBaseMass() {
		return baseMass;
	}
	
	/**
	 * Checks if it has a unit listener.
	 * 
	 * @param listener
	 * @return
	 */
	public synchronized boolean hasUnitListener(UnitListener listener) {
		if (listeners == null)
			return false;
		return listeners.contains(listener);
	}

	/**
	 * Adds a unit listener.
	 *
	 * @param newListener the listener to add.
	 */
	public synchronized final void addUnitListener(UnitListener newListener) {
		if (newListener == null)
			throw new IllegalArgumentException();
		if (listeners == null)
			listeners = new HashSet<>();

		synchronized(listeners) {	
			//logger.info(this, "Add listeners #" + listeners.size() + " " + newListener.toString());
			listeners.add(newListener);
		}
	}

	/**
	 * Removes a unit listener.
	 *
	 * @param oldListener the listener to remove.
	 */
	public synchronized final void removeUnitListener(UnitListener oldListener) {
		if (oldListener == null)
			throw new IllegalArgumentException();

		if (listeners != null) {
			synchronized(listeners) {
				//logger.info(this, "Remove listeners #" + listeners.size() + " " + oldListener.toString());
				listeners.remove(oldListener);
			}
		}
	}

	/**
	 * Fires a unit update event.
	 *
	 * @param updateType the update type.
	 */
	public final void fireUnitUpdate(UnitEventType updateType) {
		fireUnitUpdate(updateType, null);
	}

	/**
	 * Fires a unit update event.
	 *
	 * @param updateType the update type.
	 * @param target     the event target object or null if none.
	 */
	public final void fireUnitUpdate(UnitEventType updateType, Object target) {
		if (listeners == null || listeners.isEmpty()) {
			return;
		}
		final UnitEvent ue = new UnitEvent(this, updateType, target);
		synchronized (listeners) {
			for(UnitListener i : listeners) {
				try {
					// Stop listeners breaking th update thread
					i.unitUpdate(ue);
				}
				catch(RuntimeException rte) {
					logger.warning(this, "Problem executing listener " + i + " for event " + ue
									+ " due to " + rte.getMessage());
				}
			}
		}
	}

	public LocationStateType getLocationStateType() {
		return currentStateType;
	}

	public void setLocationStateType(LocationStateType locationStateType) {
		currentStateType = locationStateType;
	}
	
	public LocationTag getLocationTag() {
		return tag;
	}

	public abstract Settlement getSettlement();

	/**
	 * Gets the building this unit is at.
	 *
	 * @return the building
	 */
	public Building getBuildingLocation() {
		return null;
	}

	/**
	 * Gets the associated settlement this unit is with.
	 *
	 * @return the associated settlement
	 */
	public Settlement getAssociatedSettlement() {
		return null;
	}

	/**
	 * Gets the vehicle this unit is in, null if not in vehicle.
	 *
	 * @return the vehicle
	 */
	public Vehicle getVehicle() {
		return null;
	}

	/**
	 * Is this unit inside an environmentally enclosed breathable living space such
	 * as inside a settlement or a vehicle (NOT including in an EVA Suit) ?
	 *
	 * @return true if the unit is inside a breathable environment
	 */
	public boolean isInside() {
		if (LocationStateType.INSIDE_SETTLEMENT == currentStateType
				|| LocationStateType.INSIDE_VEHICLE == currentStateType)
			return true;

		if (LocationStateType.ON_PERSON_OR_ROBOT == currentStateType)
			return getContainerUnit().isInside();

		return false;
	}

	/**
	 * Is this unit outside on the surface of Mars, including wearing an EVA Suit
	 * and being just right outside in a settlement/building/vehicle vicinity
	 *
	 * @return true if the unit is outside
	 */
	public boolean isOutside() {
		if (LocationStateType.MARS_SURFACE == currentStateType
				|| LocationStateType.WITHIN_SETTLEMENT_VICINITY == currentStateType)
			return true;

		if (LocationStateType.ON_PERSON_OR_ROBOT == currentStateType)
			return getContainerUnit().isOutside();

		return false;
	}

	/**
	 * Is this unit inside a vehicle ?
	 *
	 * @return true if the unit is inside a vehicle
	 */
	public boolean isInVehicle() {
		if (LocationStateType.INSIDE_VEHICLE == currentStateType)
			return true;

//		if (LocationStateType.INSIDE_EVASUIT == currentStateType)
//			return getContainerUnit().isInVehicle();

		if (LocationStateType.ON_PERSON_OR_ROBOT == currentStateType)
			return getContainerUnit().isInVehicle();

		return false;
	}

	/**
	 * Is this unit inside a settlement ?
	 *
	 * @return true if the unit is inside a settlement
	 */
	public abstract boolean isInSettlement();

	/**
	 * Is this unit in the vicinity of a settlement ?
	 *
	 * @return true if the unit is inside a settlement
	 */
	public boolean isInSettlementVicinity() {
		return tag.isInSettlementVicinity();
	}

	/**
	 * Is this unit inside a vehicle in a garage ?
	 *
	 * @return true if the unit is in a vehicle inside a garage
	 */
	public boolean isInVehicleInGarage() {
		Unit cu = getContainerUnit();
		if (cu.getUnitType() == UnitType.VEHICLE) {
			// still inside the garage
			return ((Vehicle)cu).isInAGarage();
		}
		return false;
	}

//	/**
//	 * Gets the total capacity of this unit.
//	 *
//	 * @return
//	 */
//	public double getCargoCapacity() {
//		if (getUnitType() == UnitType.CONTAINER) {
//			return ((Equipment) this).getCargoCapacity();
//		}
//
//		// if Inventory is presents, use getGeneralCapacity
//		return 0;
//	}

	/**
	 * Loads instances.
	 *
	 */
	public static void initializeInstances(MasterClock c0, UnitManager um,
			Weather w, MissionManager mm) {
		masterClock = c0;
		marsClock = masterClock.getMarsClock();
		weather = w;
		unitManager = um;
		missionManager = mm;
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

	/**
	 * String representation of this Unit.
	 *
	 * @return The units name.
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Compares if an object is the same as this unit
	 *
	 * @param obj
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		return this.getIdentifier() == ((Unit) obj).getIdentifier();
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	public int hashCode() {
		int hashCode = getIdentifier() % 32;
		return hashCode;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		location = null;
		name = null;
		description = null;
		listeners = null;
	}

}

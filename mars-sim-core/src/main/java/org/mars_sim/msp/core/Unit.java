/**
 * Mars Simulation Project
 * Unit.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.location.LocationTag;
import org.mars_sim.msp.core.logging.Loggable;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.TerrainElevation;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotConfig;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleConfig;


/**
 * The Unit class is the abstract parent class to all units in the Simulation.
 * Units include people, vehicles and settlements. This class provides data
 * members and methods common to all units.
 */
public abstract class Unit implements Serializable, Loggable, UnitIdentifer, Comparable<Unit> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Unit.class.getName());
	
	public static final int OUTER_SPACE_UNIT_ID = 10000;
	public static final int MARS_SURFACE_UNIT_ID = 0;
	public static final int FIRST_SETTLEMENT_UNIT_ID = 1;
	public static final int FIRST_SITE_UNIT_ID = 20;
	public static final int FIRST_BUILDING_UNIT_ID = 100;
	public static final int FIRST_VEHICLE_UNIT_ID = 2050;
	public static final int FIRST_PERSON_UNIT_ID = 2500;
	public static final int FIRST_ROBOT_UNIT_ID = 3550;
	public static final int FIRST_EQUIPMENT_UNIT_ID = 4050;
	public static final Integer UNKNOWN_UNIT_ID = -1;
	
	// Data members
	/** The unit containing this unit. */
	private Integer containerID = MARS_SURFACE_UNIT_ID;
	
	/** The mass of the unit without inventory. */
	private double baseMass;
	
	/** TODO Unit name needs to be internationalized. */
	private String name;
	/** TODO Unit description needs to be internationalized. */
	private String description;
	/** Commander's notes on this unit. */
	private String notes = "";

	/** The unit's location tag. */
	private LocationTag tag;
	/** The unit's inventory. */
	private Inventory inventory;

	/** The last pulse applied */
	private long lastPulse = 0;
	
	/** Unit location coordinates. */
	private Coordinates location;

	protected LocationStateType currentStateType;

	/** Unit listeners. */
	private transient List<UnitListener> listeners;// = Collections.synchronizedList(new ArrayList<UnitListener>());

	protected static Simulation sim = Simulation.instance();
	protected static SimulationConfig simulationConfig = SimulationConfig.instance();
	
	protected static MarsClock marsClock;
	protected static EarthClock earthClock;
	protected static MasterClock masterClock;
	
	protected static MarsSurface marsSurface;
	
	protected static UnitManager unitManager = sim.getUnitManager();
	protected static MissionManager missionManager;
	
	protected static Mars mars;
	protected static Weather weather;
	protected static SurfaceFeatures surfaceFeatures;
	protected static TerrainElevation terrainElevation;
	
	protected static PersonConfig personConfig = simulationConfig.getPersonConfig();
	protected static VehicleConfig vehicleConfig = simulationConfig.getVehicleConfiguration();
	protected static RobotConfig robotConfig = simulationConfig.getRobotConfiguration();
	
	public int getIdentifier() {

//		Use inheritance to return correct identifier
		
//		if (this instanceof Settlement)
//			return ((Settlement)this).getIdentifier();
//		
//		if (this instanceof Equipment)
//			return ((Equipment)this).getIdentifier();
//		
//		if (this instanceof Person)
//			return ((Person)this).getIdentifier();
//		
//		if (this instanceof Robot)
//			return ((Robot)this).getIdentifier();
//		
//		if (this instanceof Vehicle)
//			return ((Vehicle)this).getIdentifier();
//
//		if (this instanceof Building)
//			return ((Building)this).getIdentifier();
//		
//		if (this instanceof ConstructionSite)
//			return ((ConstructionSite)this).getIdentifier();
//		
//		if (this instanceof MarsSurface)
//			return ((MarsSurface)this).getIdentifier();
		
		return (Integer) UNKNOWN_UNIT_ID;
	}
	
	public void incrementID() {
 
//  Use inheritance for correct increment

//		if (this instanceof Settlement)
//			((Settlement)this).incrementID();
//		
//		if (this instanceof Equipment)
//			((Equipment)this).incrementID();
//		
//		if (this instanceof Person)
//			((Person)this).incrementID();
//		
//		if (this instanceof Robot)
//			((Robot)this).incrementID();
//		
//		if (this instanceof Vehicle)
//			((Vehicle)this).incrementID();
//
//		if (this instanceof Building)
//			((Building)this).incrementID();
//		
//		if (this instanceof ConstructionSite)
//			((ConstructionSite)this).incrementID();
//		
//		if (this instanceof MarsSurface)
//			((MarsSurface)this).incrementID();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param name     {@link String} the name of the unit
	 * @param location {@link Coordinates} the unit's location
	 */
	public Unit(String name, Coordinates location) {
		// Initialize data members from parameters
		this.name = name;
		this.description = name;
		this.baseMass = 0;//Double.MAX_VALUE;

		unitManager = sim.getUnitManager();
		
		// Set up unit listeners.
		listeners = Collections.synchronizedList(new CopyOnWriteArrayList<UnitListener>()); 

		// Creates a new location tag instance for each unit
		tag = new LocationTag(this);

		incrementID();
		
		// Define the default LocationStateType of an unit at the start of the sim
		// Instantiate Inventory as needed
		// TODO shouldn't be using instanceof in a Constructor. Add overloaded constructor and pass in
		if (this instanceof Robot) {
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
//			containerID = FIRST_SETTLEMENT_ID;
			this.inventory = new Inventory(this);
		}
		else if (this instanceof Equipment) {
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
//			containerID = FIRST_SETTLEMENT_ID;
			this.inventory = new Inventory(this);
		}
		else if (this instanceof Person) {
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
//			containerID = FIRST_SETTLEMENT_ID;
			this.inventory = new Inventory(this);
		}
		else if (this instanceof Building) {// || this instanceof MockBuilding) {
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
//			containerID = FIRST_SETTLEMENT_ID;
		}
		else if (this instanceof Vehicle) {
			currentStateType = LocationStateType.WITHIN_SETTLEMENT_VICINITY;
			containerID = (Integer) MARS_SURFACE_UNIT_ID;
			this.inventory = new Inventory(this);
		}
		else if (this instanceof Settlement) {// || this instanceof MockSettlement) {
			currentStateType = LocationStateType.MARS_SURFACE;
			containerID = (Integer) MARS_SURFACE_UNIT_ID;
			this.inventory = new Inventory(this);
		}
		else if (this instanceof ConstructionSite) {
			currentStateType = LocationStateType.MARS_SURFACE;
			containerID = (Integer) MARS_SURFACE_UNIT_ID;
		}
//		else if (this instanceof MarsSurface) {
//			currentStateType = LocationStateType.IN_OUTER_SPACE;
//			containerID = (Integer) OUTER_SPACE_UNIT_ID;
////			System.out.println(this + " has containerID " + containerID + " and is " + currentStateType);
//			this.inventory = new Inventory(this);
//		}
		else { //if (this instanceof Unit) {
			currentStateType = LocationStateType.MARS_SURFACE;
			containerID = (Integer) MARS_SURFACE_UNIT_ID;	
			this.inventory = new Inventory(this);
		}
		
		this.location = new Coordinates(0D, 0D);

		if (location != null) {
			// Set the unit's location coordinates
			this.location.setCoords(location);
			// Set the unit's inventory location coordinates
			if (inventory != null) {
				inventory.setCoordinates(location);
			}
		}
	}

	/**
	 * Is this time pulse valid for the Unit.Has it been already applied?
	 * The logic on this method can be commented out later on
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
				logger.warning(getName() + " expected pulse #" + expectedPulse
						+ " but received " + newPulse);
			}
			lastPulse = newPulse;
		}
		else {
			// Seen already
			logger.severe(getName() + " rejected pulse #" + newPulse
						+ ", last pulse was " + lastPulse);
		}
		return result;
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
			((Settlement)unit).setName(newName);
			simulationConfig.getSettlementConfiguration().changeSettlementName(oldName, newName);
		}
		this.name = newName;
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
		location.setCoords(newLocation);
		inventory.setCoordinates(newLocation);
		fireUnitUpdate(UnitEventType.LOCATION_EVENT, newLocation);
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
		if (unitManager == null) // for maven test
			return null;
		return unitManager.getUnitByID(containerID);
	}

	public int getContainerID() {
		return containerID;
	}
	
	public void setContainerID(Integer id) {
		containerID = id;
//		inventory.setOwnerID(id);
	}
	
	/**
	 * Gets the topmost container unit that owns this unit (Settlement, Vehicle, Person or Robot) 
	 * If it's on the surface of Mars, then the topmost container is MarsSurface. 
	 * 
	 * @return the unit's topmost container unit
	 */
	public Unit getTopContainerUnit() {
		Unit topUnit = getContainerUnit();
		if (!(topUnit instanceof MarsSurface)) {
			while (topUnit != null && topUnit.getContainerUnit() != null && !(topUnit.getContainerUnit() instanceof MarsSurface)) {
				topUnit = topUnit.getContainerUnit();
			}
		}

		return topUnit;
	}
	
	/**
	 * Gets the topmost container unit that owns this unit (Settlement, Vehicle, Person or Robot) 
	 * If it's on the surface of Mars, then the topmost container is MarsSurface. 
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
	public void setContainerUnit(Unit newContainer) {
		Unit oldContainer = getContainerUnit();
		// 1. Set Coordinates
		if (newContainer == null) {
			// Set back to its previous container unit's coordinates
			if (oldContainer != null)
				setCoordinates(oldContainer.getCoordinates());
		}
		else if (this instanceof MarsSurface) {
			// Do nothing
		}
		else if (newContainer instanceof MarsSurface){
			// Do nothing
		}
		else {
			// Slave the coordinates to that of the newContainer
			setCoordinates(newContainer.getCoordinates());
		}
			
		// 2. Set LocationStateType
		if (this instanceof Person || this instanceof Robot) {
			updatePersonRobotState(newContainer);
		}
		else if (this instanceof Equipment) {
			updateEquipmentState(newContainer);
		}
		else if (this instanceof Vehicle) {
			updateVehicleState(newContainer);
		}
		else if (this instanceof Building) {
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
		}
		else if (this instanceof Settlement
				|| this instanceof ConstructionSite) {
			currentStateType = LocationStateType.MARS_SURFACE;
			containerID = (Integer) MARS_SURFACE_UNIT_ID;
		}		
		else if (this instanceof MarsSurface) {
			currentStateType = LocationStateType.OUTER_SPACE;
			containerID = (Integer) OUTER_SPACE_UNIT_ID;
		}	
		else {
			currentStateType = LocationStateType.UNKNOWN;
			containerID = (Integer) UNKNOWN_UNIT_ID;
		}	
		
		// c. Set containerID
		if (newContainer == null || newContainer.getIdentifier() == UNKNOWN_UNIT_ID) {
			containerID = (Integer) UNKNOWN_UNIT_ID;
		}
		else 
			containerID = newContainer.getIdentifier();
	
		fireUnitUpdate(UnitEventType.CONTAINER_UNIT_EVENT, newContainer);
	}

	
	/**
	 * Gets the location state type based on the type of the new container unit
	 * 
	 * @param newContainer
	 * @return {@link LocationStateType}
	 */
	public LocationStateType getNewLocationState(Unit newContainer) {
		
		if (newContainer instanceof Settlement) {
			if (this instanceof Person || this instanceof Robot || this instanceof Equipment)
				return LocationStateType.INSIDE_SETTLEMENT;
			else if (this instanceof Vehicle)
				return LocationStateType.WITHIN_SETTLEMENT_VICINITY;
		}
		
		if (newContainer instanceof Building)
			return LocationStateType.INSIDE_SETTLEMENT;	
		
		if (newContainer instanceof Vehicle)
			return LocationStateType.INSIDE_VEHICLE;
		
		if (newContainer instanceof ConstructionSite)
			return LocationStateType.WITHIN_SETTLEMENT_VICINITY;
		
//		if (newContainer instanceof EVASuit)
//			return LocationStateType.INSIDE_EVASUIT;
		
		if (newContainer instanceof Person)
			return LocationStateType.ON_PERSON_OR_ROBOT;

		if (newContainer instanceof MarsSurface)
			return LocationStateType.MARS_SURFACE;
		
		return null;
	}
	
	/**
	 * Updates the location state type of a person or robot
	 * 
	 * @param newContainer
	 */
	public void updatePersonRobotState(Unit newContainer) {
		if (newContainer == null) {
			currentStateType = LocationStateType.UNKNOWN; 
			return;
		}
		
		currentStateType = getNewLocationState(newContainer);
	}

	/**
	 * Updates the location state type of an equipment
	 * 
	 * @param newContainer
	 */
	public void updateEquipmentState(Unit newContainer) {
		if (newContainer == null) {
			currentStateType = LocationStateType.UNKNOWN;
			return;
		}
		
		currentStateType = getNewLocationState(newContainer);
	}

	/**
	 * Updates the location state type of a vehicle.
	 * 
	 * @apiNote (1) : WITHIN_SETTLEMENT_VICINITY is the intermediate state between being INSIDE_SETTLEMENT (in a garage) and being OUTSIDE_ON_MARS.
	 *
	 * @apiNote (2) : WITHIN_SETTLEMENT_VICINITY can be used by a person or a vehicle.
	 *
	 * @apiNote (3) : If a vehicle may be in a garage inside a building, this vehicle is INSIDE_SETTLEMENT.
	 *                If a vehicle is parked right outside a settlement, this vehicle is WITHIN_SETTLEMENT_VICINITY.
	 * 
	 * @param newContainer
	 */
	public void updateVehicleState(Unit newContainer) {
		if (newContainer == null) {
			currentStateType = LocationStateType.UNKNOWN;
			return;
		}
		
		currentStateType = getNewLocationState(newContainer);
	}

	/**
	 * Gets the unit's mass including inventory mass.
	 * 
	 * @return mass of unit and inventory
	 * @throws Exception if error getting the mass.
	 */
	public double getMass() {
		double invMass = inventory.getTotalInventoryMass(false);
		if (invMass == 0)
			return baseMass;
		
		return baseMass + invMass;
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
			listeners = Collections.synchronizedList(new CopyOnWriteArrayList<UnitListener>());

		if (!listeners.contains(newListener)) {
			listeners.add(newListener);
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
			listeners = Collections.synchronizedList(new CopyOnWriteArrayList<UnitListener>());
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
		if (listeners == null || listeners.size() < 1) {
			// listeners = Collections.synchronizedList(new ArrayList<UnitListener>());
			// we don't do anything if there's no listeners attached
			return;
		}
		final UnitEvent ue = new UnitEvent(this, updateType, target);
		synchronized (listeners) {
			 Iterator<UnitListener> i = listeners.iterator();
				 while (i.hasNext()) {
				 i.next().unitUpdate(ue);
			 }
		}
	}

	
	public LocationStateType getLocationStateType() {
		return currentStateType;
	}

	public LocationTag getLocationTag() {
		return tag;
	}
	
	public String getLocale() {
		return tag.getLocale();
	}

	public Settlement getSettlement() {
		if (this instanceof Equipment) {
			return ((Equipment) this).getSettlement();
		} 
		
		else if (this instanceof Person) {
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

	/**
	 * Is this unit inside an environmentally enclosed breathable living space
	 * such as inside a settlement or a vehicle (NOT including in an EVA Suit)
	 * 
	 * @return true if the unit is inside a breathable environment
	 */
	public boolean isInside() {
		if (LocationStateType.INSIDE_SETTLEMENT == currentStateType
				|| LocationStateType.INSIDE_VEHICLE == currentStateType)
			return true;
		
//		if (LocationStateType.INSIDE_EVASUIT == currentStateType)
//			return getContainerUnit().isInside();
		
		if (LocationStateType.ON_PERSON_OR_ROBOT == currentStateType)
			return getContainerUnit().isInside();
		
		return false;
	}

	/**
	 * Is this unit outside on the surface of Mars, including wearing an EVA Suit 
	 * and being outside the settlement/building/vehicle
	 * 
	 * @return true if the unit is outside
	 */
	public boolean isOutside() {
		if (LocationStateType.MARS_SURFACE == currentStateType
				|| LocationStateType.WITHIN_SETTLEMENT_VICINITY == currentStateType)
			return true;
		
//		if (LocationStateType.INSIDE_EVASUIT == currentStateType)
//			return getContainerUnit().isOutside();
		
		if (LocationStateType.ON_PERSON_OR_ROBOT == currentStateType)
			return getContainerUnit().isOutside();
		
		return false;
	}
	
//	/**
//	 * Checks if this unit is inside an airlock
//	 * 
//	 * @return true if the unit is inside an airlock
//	 */
//	public boolean isInAirlock() {
//		if (LocationStateType.IN_AIRLOCK == currentStateType)
//			return true;
//		
////		if (LocationStateType.INSIDE_EVASUIT == currentStateType)
////			return getContainerUnit().isInAirlock();
//		
//		if (LocationStateType.ON_A_PERSON_OR_ROBOT == currentStateType)
//			return getContainerUnit().isInAirlock();
//		
//		return false;
//	}
	
	/**
	 * Is this unit inside a vehicle
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
	 * Is this unit inside a settlement
	 * 
	 * @return true if the unit is inside a settlement
	 */
	public boolean isInSettlement() {
//		System.out.println("Unit : " + this + "'s location state type : " + currentStateType);

		if (LocationStateType.INSIDE_SETTLEMENT == currentStateType)
			return true;
		
		if (this instanceof Vehicle) {
			// if the vehicle is parked in a settlement or a garage
			if (LocationStateType.INSIDE_SETTLEMENT == currentStateType)
				return true;
			
			if (getContainerUnit() instanceof Settlement 
					&& getContainerUnit().getInventory().containsUnit(this)) {
				return true;
			}
			
		}
		
//		if (LocationStateType.IN_AIRLOCK == currentStateType) {
//			if (getContainerUnit() instanceof Settlement 
//					&& getContainerUnit().getInventory().containsUnit(this)) {
//				return true;
//			}
//		}

//		if (LocationStateType.INSIDE_EVASUIT == currentStateType)
//			return getContainerUnit().isInSettlement();
		
		if (LocationStateType.ON_PERSON_OR_ROBOT == currentStateType)
			return getContainerUnit().isInSettlement();
		
		return false;
	}
	
	/**
	 * Is this unit in the vicinity of a settlement
	 * 
	 * @return true if the unit is inside a settlement
	 */
	public boolean isInSettlementVicinity() {
		return tag.isInSettlementVicinity();
	}
	
	/**
	 * Is this unit in a vehicle inside a garage
	 * 
	 * @return true if the unit is in a vehicle inside a garage
	 */
	public boolean isInVehicleInGarage() {
//		if (LocationStateType.INSIDE_EVASUIT == currentStateType)
//			return getContainerUnit().isInVehicleInGarage();
		
		
//		if (containerID >= FIRST_VEHICLE_ID && containerID < FIRST_PERSON_ID) {
//			int vid = getContainerUnit().getContainerID();
//			
//			if (vid >= FIRST_SETTLEMENT_ID
//					&& vid < FIRST_VEHICLE_ID)
//				return true;
//		}
//		
//		return false;	
		
		if (getContainerUnit() instanceof Vehicle) {
			if (BuildingManager.getBuilding((Vehicle) getContainerUnit()) != null)
				// still inside the garage
				return true;
		}
		return false;
	}
	
//	/**
//	 * Is this unit inside a garage
//	 * 
//	 * @return true if the unit is inside a garage
//	 */
//	public boolean isInGarage() {
//		if (containerID >= FIRST_SETTLEMENT_ID
//				&& containerID < FIRST_VEHICLE_ID)
//			return true;
//		
//		int vid = getContainerUnit().getContainerID();
//		
//		if (vid >= FIRST_SETTLEMENT_ID
//				&& vid < FIRST_VEHICLE_ID)
//			return true;
//		
//		return false;		
//	}
	
	/**
	 * Loads instances
	 * 
	 * @param c0 {@link MasterClock}
	 * @param c1 {@link MarsClock}
	 * @param e {@link EarthClock}
	 * @param s {@link Simulation}
	 * @param m {@link Mars}
	 * @param ms {@link MarsSurface}
	 * @param w {@link Weather}
	 * @param u {@link UnitManager}
	 * @param mm {@link MissionManager}
	 */
	public static void initializeInstances(MasterClock c0, MarsClock c1, EarthClock e, Simulation s, 
			Mars m, MarsSurface ms, Weather w, SurfaceFeatures sf, MissionManager mm) {
		masterClock = c0;
		marsClock = c1;
		earthClock = e;
		sim = s;
		mars = m;
		marsSurface = ms;
		weather = w;
		surfaceFeatures = sf;
		missionManager = mm;
	}
	
	public static void setUnitManager(UnitManager u) {
		unitManager = u;
	}
	
	public static void setMarsSurface(MarsSurface ms) {
		marsSurface = ms;
	}
	
	/**
	 * Transfer the unit from one unit to another unit
	 * 
	 * @param origin {@link Unit} the original container unit
	 * @param destination {@link Unit} the destination container unit
	 */
	public boolean transfer(Unit origin, Unit destination) {
		return origin.getInventory().transferUnit(this, destination);
	}

	/**
	 * Transfer the unit from one inventory to another unit
	 * 
	 * @param originInv {@link Inventory} the inventory of the original container unit
	 * @param destination {@link Unit} the destination container unit
	 */
	public boolean transfer(Inventory originInv, Unit destination) {
		return originInv.transferUnit(this, destination);
	}
	
	/**
	 * Transfer the unit from one unit to another inventory
	 * 
	 * @param origin {@link Unit} the original container unit
	 * @param destinationInv {@link Inventory} the inventory of the destination container unit
	 */
	public boolean transfer(Unit origin, Inventory destinationInv) {
		return origin.getInventory().transferUnit(this, destinationInv.getOwner());
	}
	
	/**
	 * Transfer the unit from one inventory to another inventory
	 * 
	 * @param originInv {@link Inventory} the inventory of the original container unit
	 * @param destinationInv {@link Inventory} the inventory of the destination container unit
	 */
	public boolean transfer(Inventory originInv, Inventory destinationInv) {
		return originInv.transferUnit(this, destinationInv.getOwner());
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
		// return name + " (" + identifier + ")";
		return name;
	}

    public String getCode() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }
    
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
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
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		location = null;
		name = null;
		description = null;
		inventory.destroy();
		inventory = null;
//		containerUnit = null;
		// if (listeners != null) listeners.clear();
		listeners = null;
	}

}

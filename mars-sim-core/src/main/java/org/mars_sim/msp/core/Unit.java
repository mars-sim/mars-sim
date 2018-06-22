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
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.location.LocationTag;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

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

    //private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1, logger.getName().length());
	// static unit identifier
	private static int unitIdentifer = 0;
	
	// Data members
	// Unique identifier
	private int identifier;
	/** The mass of the unit without inventory. */
	private double baseMass;
	
	/** TODO Unit name needs to be internationalized. */
	private String name;
	/** TODO Unit description needs to be internationalized. */
	private String description;

	/** The unit's location tag. */
	private LocationTag tag;
	/** The unit's inventory. */
	private Inventory inventory;
	/** The unit containing this unit. */
	protected Unit containerUnit;
	/** The cache of containerUnit. */
	protected Unit containerUnitCache;
	/** Unit location coordinates. */
	private Coordinates location;

	// 2015-12-20 Added LocationState class
	//private LocationState currentState, insideBuilding, insideVehicleOutsideOnMars, insideVehicleInSettlement, insideSettlement, outsideOnMars, settlementVicinity, onAPerson;

	// 2016-11-21 Added LocationStateType
	private LocationStateType currentStateType;

	/** Unit listeners. */
	private transient List<UnitListener> listeners;// = Collections.synchronizedList(new ArrayList<UnitListener>());
	


	/**
	 * Must be synchronised to prevent duplicate ids being assigned via different threads.
	 * @return
	 */
	private static synchronized int getNextIdentifier() {
		return unitIdentifer++;
	}

	/**
	 * Constructor.
	 * @param name {@link String} the name of the unit
	 * @param location {@link Coordinates} the unit's location
	 */
	public Unit(String name, Coordinates location) {
		listeners = Collections.synchronizedList(new ArrayList<UnitListener>()); // Unit listeners.

		this.identifier = getNextIdentifier();
		
		tag = new LocationTag(this);

		// Initialize data members from parameters
		this.name = name;
		this.description = name;
		this.baseMass = Double.MAX_VALUE;

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

/*
		insideBuilding = new InsideBuilding(this);
		insideVehicleOutsideOnMars = new InsideVehicleOutsideOnMars(this);
		insideVehicleInSettlement = new InsideVehicleInSettlement(this);
		outsideOnMars = new OutsideOnMars(this);
		settlementVicinity = new SettlementVicinity(this);
		insideSettlement =  new InsideSettlement(this);
		onAPerson = new OnAPerson(this);

		if (this instanceof Settlement)
			currentState = outsideOnMars;
		else if (this instanceof Person)
			currentState = insideBuilding;
		else if (this instanceof Robot)
			currentState = insideBuilding;
		else if (this instanceof Equipment)
			currentState = insideBuilding;
		else if (this instanceof Building)
			currentState = insideSettlement;
		else if (this instanceof Vehicle)
			currentState = settlementVicinity;
*/
	}


	/**
	 * Get the unique identifier for this unit
	 * @return Identifier
	 */
	public int getIdentifier() {
		return identifier;
	}

	/**
	 * Change the unit's name
	 * @param newName new name
	 */
	//2015-12-13 Modified changeName() to call changeSettlementName()
	public final void changeName(String newName) {
		String oldName = this.name;
		Unit unit = this;
		if (unit instanceof Settlement) {
			//Settlement settlement = (Settlement) unit;
			SimulationConfig.instance().getSettlementConfiguration().changeSettlementName(oldName, newName);
		}
		this.name = newName;
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
	 * Gets the unit's nickname
	 * @return the unit's nickname
	 */
	public String getNickName() {
		return name;
	}
	
	/**
	 * Gets the unit's shortened name
	 * @return the unit's shortened name
	 */
	//2016-08-31 Added getShortenedName()
	public String getShortenedName() {
		name = name.trim();
		int num = name.length();

		boolean hasSpace = name.matches("^\\s*$");

		if (hasSpace) {
			int space = name.indexOf(" ");

			String oldFirst = name.substring(0, space);
			String oldLast = name.substring(space+1, num);
			String newFirst = oldFirst;
			String newLast = oldLast;
			String newName = name;

			if (num > 20) {

				if (oldFirst.length() > 10) {
					newFirst = oldFirst.substring(0, 10);
				}
				else if (oldLast.length() > 10) {
					newLast = oldLast.substring(0, 10);
				}
				newName = newFirst + " " + newLast;
				//System.out.println("oldName : " + name + "    newName : " + newName);
			}

			return newName;
		}

		else
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
		//System.out.println("Description is : "+ description);
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
/*
	public Unit getContainerUnitCache() {
		if (containerUnitCache != null) {
			return containerUnitCache;			
		}
		else if (containerUnit != null) {
			return containerUnit;
		}
		return getTopContainerUnit();
	}
*/
	/**
	 * Gets the topmost container unit that owns this unit.
	 * Returns null if unit has no container unit (meaning that the unit is outside)
	 * @return the unit's topmost container unit
	 */
	public Unit getTopContainerUnit() {
		Unit topUnit = containerUnit;
		if (topUnit != null) {
			while (topUnit.containerUnit != null) {
				topUnit = topUnit.containerUnit;
			}
		}
/*		else {
			if (this instanceof Person) {
				Person person = (Person) this;
				topUnit = person.getAssociatedSettlement();
			}
			else if (this instanceof Robot) {
				Robot robot = (Robot) this;
				topUnit = robot.getAssociatedSettlement();
			}
		}
*/
		return topUnit;
	}

	/**
	 * Sets the unit's container unit.
	 * @param newContainer the unit to contain this unit.
	 */
	public void setContainerUnit(Unit newContainer) {
		if (this instanceof Person || this instanceof Robot) 
			updatePersonRobotState(newContainer);
		else if (this instanceof Equipment)
			updateEquipmentState(newContainer);
//		else if (this instanceof Person)
//			updatePersonRobotState(newContainer);
		else if (this instanceof Vehicle)
			updateVehicleState(newContainer);
		else if (this instanceof Building)
			currentStateType = LocationStateType.INSIDE_SETTLEMENT; 
		else if (this instanceof Settlement)
			currentStateType = LocationStateType.OUTSIDE_ON_MARS;
		
		if (containerUnit != null)
			containerUnitCache = containerUnit;
		
		this.containerUnit = newContainer;

		fireUnitUpdate(UnitEventType.CONTAINER_UNIT_EVENT, newContainer);
	}


	/**
	 * Switches from an old location state to a new location state
	 * @param newContainer

	public void updateState(Unit newContainer) {
		Unit oldContainer = this.containerUnit;
		LocationState oldState = this.currentState;
		LocationState newState = null;

		// Case P1
		if (oldState.equals(insideBuilding) && newContainer == null)
			// the person was inside a building and is going outside
			// oldContainer instanceof Settlement
			// oldState.equals(insideBuilding)
			newState = settlementVicinity;

		// Case P2
		else if (oldState.equals(insideBuilding) && newContainer instanceof Vehicle)
			// the person was inside a building and is getting into a vehicle inside a garage
			// oldContainer instanceof Settlement
			// oldState.equals(insideBuilding)
			newState = insideVehicle;

		// Case P3
		else if (oldState.equals(settlementVicinity) && newContainer instanceof Settlement)
			// the person was outside in a settlement's vicinity and is getting inside a building in a settlement
			// oldContainer == null
			// oldState.equals(settlementVicinity)
			newState = insideBuilding;

		// Case P4
		else if (oldState.equals(settlementVicinity) && newContainer instanceof Vehicle)
			// the person was outside in a settlement's vicinity and is getting into a vehicle
			// oldContainer == null
			// oldState.equals(settlementVicinity)
			newState = insideVehicle;

		// Case P5
		else if (oldState.equals(outsideOnMars) && newContainer instanceof Vehicle)
			// the person was on a mission outside on Mars and is getting into a vehicle
			// oldContainer == null
			// oldState.equals(outsideOnMars)
			newState = insideVehicle;

		else if (oldState.equals(insideVehicle) && newContainer == null){
			// oldContainer instanceof Vehicle
			Vehicle vv = null;
			if (oldContainer instanceof Vehicle)
				vv = (Vehicle) oldContainer;
			else {
				System.err.println("oldContainer is not a vehicle");
			}

			// Case P6
			if (vv.getLocationState().equals(settlementVicinity)) {
				// the person was inside a vehicle parked near a settlement's vicinity
				// and he is going outside of the vehicle
				newState = settlementVicinity;
			}

			// Case P7
			else if (vv.getLocationState().equals(outsideOnMars)) {
				// the person was inside a vehicle on a mission outside somewhere on Mars
				// and he is going outside of the vehicle
				newState = outsideOnMars;
			}
			else {
				newState = null;
				System.err.println("invalid state");
			}

		}
		// Case P8
		else if (oldState.equals(insideVehicle) && newContainer instanceof Building) {
			// the person was inside a vehicle parked inside a garage
			// and he is going outside of the vehicle
			newState = insideBuilding;
		}

		// Case P9 (for vehicle only)
		else if (oldState.equals(settlementVicinity) && newContainer == null)
			// the person was outside in a settlement's vicinity and is getting into a vehicle
			// oldContainer is a settlement
			// oldState.equals(settlementVicinity)
			newState = outsideOnMars;

		// Case P10 (for vehicle only)
		else if (oldState.equals(outsideOnMars) && newContainer instanceof Settlement)
			// the person was outside in a settlement's vicinity and is getting into a vehicle
			// oldContainer == null
			// oldState.equals(outsideOnMars)
			newState = settlementVicinity;

		// Case P11 (a person/robot has just arrived at a settlement)
		else if (oldContainer == null && newContainer instanceof Settlement) {
			//oldContainer.equals(null)
			if (this instanceof Settlement)
				newState = outsideOnMars;
			else if (this instanceof Person)
				newState = insideBuilding;
			else if (this instanceof Robot)
				newState = insideBuilding;
			else if (this instanceof Equipment)
				;//newState = insideBuilding;
			else if (this instanceof Building)
				newState = insideSettlement;
			else if (this instanceof Vehicle)
				;//newState = settlementVicinity;

			//System.out.print("Case 11. unit : " + this.getName() + "  oldState = " + oldState.getName() + "  newState = " + newState.getName());
			//System.out.println("  oldContainer = " + oldContainer + "  newContainer = " + newContainer);
		}

		else if (oldContainer == null) {// && newContainer.equals(null)) {
			System.err.print("Case 12. unit : " + this.getName() + "  oldState = " + oldState.getName() + "  newState = " + newState.getName());
			System.err.println("  oldContainer = " + oldContainer + "  newContainer = " + newContainer);
		}

		else {
			newState = currentState;
			//currentState = null;
			System.err.print("Beyond 12 cases. This unit is " + this.getName() + "  oldState = " + oldState.getName() + "  newState = " + newState.getName());
			System.err.println("  oldContainer = " + oldContainer + "  newContainer = " + newContainer);

		}

		// set currentState to newState
		currentState = newState;
	}
*/

	/**
	 * Switches from an old location state to a new location state
	 * @param newContainer

	public void updateEquipmentState(Unit newContainer) {
		Unit oldContainer = this.containerUnit;
		LocationState oldState = this.currentState;
		LocationState newState = null;

		// Case E1
		if (oldState.equals(insideBuilding) && newContainer == null)
			// the unit was inside a building and is going outside
			// oldContainer instanceof Settlement
			// oldState.equals(insideBuilding)
			newState = settlementVicinity;

		// Case E2
		else if (oldState.equals(insideBuilding) && newContainer instanceof Vehicle)
			// the unit was inside a building and is getting into a vehicle inside a garage
			// oldContainer instanceof Settlement
			// oldState.equals(insideBuilding)
			newState = insideVehicle;

		// Case E3
		else if (oldState.equals(settlementVicinity) && newContainer instanceof Settlement)
			// the unit was outside in a settlement's vicinity and is getting inside a building in a settlement
			// oldContainer == null
			// oldState.equals(settlementVicinity)
			newState = insideBuilding;

		// Case E4
		else if (oldState.equals(settlementVicinity) && newContainer instanceof Vehicle)
			// the unit was outside in a settlement's vicinity and is getting into a vehicle
			// oldContainer == null
			// oldState.equals(settlementVicinity)
			newState = insideVehicle;

		// Case E5
		else if (oldState.equals(outsideOnMars) && newContainer instanceof Vehicle)
			// the unit was on a mission outside on Mars and is getting into a vehicle
			// oldContainer == null
			// oldState.equals(outsideOnMars)
			newState = insideVehicle;

		else if (oldState.equals(insideVehicle) && newContainer == null) {
			// oldContainer instanceof Vehicle
			Vehicle vv = null;
			if (oldContainer instanceof Vehicle)
				vv = (Vehicle) oldContainer;
			else {
				System.err.println("oldContainer is not a vehicle");
			}

			// Case E6
			if (vv.getLocationState().equals(settlementVicinity)) {
				// the unit was inside a vehicle parked near a settlement's vicinity
				// and is going outside of the vehicle
				newState = settlementVicinity;
			}

			// Case E7
			else if (vv.getLocationState().equals(outsideOnMars)) {
				// the unit was inside a vehicle on a mission outside somewhere on Mars
				// and is going outside of the vehicle
				newState = outsideOnMars;
			}
			else {
				newState = null;
				System.err.println("invalid state");
			}

		}
		// Case E8
		else if (oldState.equals(insideVehicle) && newContainer instanceof Building) {
			// the person was inside a vehicle parked inside a garage
			// and he is going outside of the vehicle
			newState = insideBuilding;
		}

		// Case E9 (for vehicle only)
		else if (oldState.equals(settlementVicinity) && newContainer == null)
			// the person was outside in a settlement's vicinity and is getting into a vehicle
			// oldContainer is a settlement
			// oldState.equals(settlementVicinity)
			newState = outsideOnMars;

		// Case E10 (for vehicle only)
		else if (oldState.equals(outsideOnMars) && newContainer instanceof Settlement)
			// the person was outside in a settlement's vicinity and is getting into a vehicle
			// oldContainer == null
			// oldState.equals(outsideOnMars)
			newState = settlementVicinity;

		// Case E11 (a person/robot has just arrived at a settlement)
		else if (oldContainer == null && newContainer instanceof Settlement) {
			//oldContainer.equals(null)
			if (this instanceof Settlement)
				newState = outsideOnMars;
			else if (this instanceof Person)
				newState = insideBuilding;
			else if (this instanceof Robot)
				newState = insideBuilding;
			else if (this instanceof Equipment)
				newState = insideBuilding;
			else if (this instanceof Building)
				newState = insideSettlement;
			else if (this instanceof Vehicle)
				newState = settlementVicinity;

			//System.out.print("Case 11. unit : " + this.getName() + "  oldState = " + oldState.getName() + "  newState = " + newState.getName());
			//System.out.println("  oldContainer = " + oldContainer + "  newContainer = " + newContainer);
		}

		else if (oldContainer == null) {// && newContainer.equals(null)) {
			System.err.print("Case 12. unit : " + this.getName() + "  oldState = " + oldState.getName() + "  newState = " + newState.getName());
			System.err.println("  oldContainer = " + oldContainer + "  newContainer = " + newContainer);
		}

		else {
			newState = currentState;
			//currentState = null;
			System.err.print("Beyond 12 cases. This unit is " + this.getName() + "  oldState = " + oldState.getName() + "  newState = " + newState.getName());
			System.err.println("  oldContainer = " + oldContainer + "  newContainer = " + newContainer);

		}

		// set currentState to newState
		currentState = newState;
	}
*/

	/**
	 * Updates the location state type of a person or robot
	 * @param newContainer
	 */
	public void updatePersonRobotState(Unit newContainer) {
		Unit oldContainer = this.containerUnit;

		// Case 1a : exiting a settlement
		if (oldContainer instanceof Settlement && newContainer == null)
			currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;

		// Case 1b (reverse of Case 1a)
		else if (oldContainer == null && newContainer instanceof Settlement)
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;

		// Case 2a : boarding a vehicle parked in a garage
		else if (oldContainer instanceof Settlement && newContainer instanceof Vehicle)
			// only if the vehicle is inside a garage can this happen
			currentStateType = LocationStateType.INSIDE_VEHICLE;

		// Case 2b (reverse of Case 2a)
		else if (oldContainer instanceof Vehicle && newContainer instanceof Settlement)
			// only if the vehicle is inside a garage can this happen
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
		
		// Case 3a : to board a vehicle from outside  
		else if (oldContainer == null && newContainer instanceof Vehicle)
			currentStateType = LocationStateType.INSIDE_VEHICLE;
	
		// Case 3b and 3c
		else if (oldContainer instanceof Vehicle && newContainer == null) {
			if (((Vehicle)oldContainer).getLocationStateType() == LocationStateType.OUTSIDE_SETTLEMENT_VICINITY)
				// Case 3b : a person exits a vehicle that is within the settlement vicinity 
				currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;
			else 
				//Case 3c : a person exits a vehicle that is outside on Mars on a mission
				currentStateType = LocationStateType.OUTSIDE_ON_MARS;
		}
		// Case 4a and 4b : a person walks from the settlement vicinity to outside on Mars
		// Unrealistic and forbidden at this point.

	}

	/**
	 * Updates the location state type of an equipment
	 * @param newContainer
	 */
	public void updateEquipmentState(Unit newContainer) {
		Unit oldContainer = this.containerUnit;

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
		else if (oldContainer == null && newContainer instanceof Person)
			currentStateType = LocationStateType.ON_A_PERSON;

		// Case 3b and 3c
		else if (oldContainer instanceof Person && newContainer == null) {
			// Case 3b (reverse of Case 3a)
			if (((Person)oldContainer).getLocationStateType() == LocationStateType.OUTSIDE_SETTLEMENT_VICINITY)
				// this equipment can be placed in the settlement vicinity (Note : a new field work feature for future)
				currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;
			else
				// Case 3c (reverse of Case 3a)
				// this equipment can be placed out there on the surface of Mars (Note : a new field work feature for future)
				currentStateType = LocationStateType.OUTSIDE_ON_MARS;	
		}
	}


	/**
	 * Updates the location state type of a vehicle
	 * @param newContainer
	 */
	public void updateVehicleState(Unit newContainer) {
		Unit oldContainer = this.containerUnit;

		// Note : "within a settlement vicinity" is the intermediate state between being "in a settlement" and being "outside on Mars"
		
		if (newContainer != null) {
			// Case 1 : to park in a garage
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;//.INSIDE_BUILDING;
		}

		else {
			if (oldContainer == null)
				// Case 2 : leaving settlement vicinity to outside on mars
				currentStateType = LocationStateType.OUTSIDE_ON_MARS;
			else if (oldContainer instanceof Settlement)
				// Case 3 : leaving the garage to settlement vicinity
				currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;
						
//			if (((Vehicle)oldContainer).getLocationStateType() == LocationStateType.OUTSIDE_SETTLEMENT_VICINITY)
//				// Case 2 : this equipment can be placed in the settlement vicinity (Note : a new field work feature for future)
//				currentStateType = LocationStateType.OUTSIDE_ON_MARS;
//			else
//				// Case 3 
//				// this equipment can be placed out there on the surface of Mars (Note : a new field work feature for future)
//				currentStateType = LocationStateType.OUTSIDE_SETTLEMENT_VICINITY;
		}
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
		//return name  + " (" + identifier + ")";
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
	   	//logger.info("Unit's fireUnitUpdate() is on " + Thread.currentThread().getName() + " Thread");

		if (listeners == null || listeners.size() < 1) {
			// listeners = Collections.synchronizedList(new ArrayList<UnitListener>());
			// we don't do anything if there's no listeners attached
			return;
		}
		final UnitEvent ue = new UnitEvent(this, updateType, target);
		synchronized(listeners) {
			//Iterator<UnitListener> i = listeners.iterator();
			//while (i.hasNext()) {
			//	i.next().unitUpdate(ue);
			//}
			for (UnitListener u: listeners) {
				u.unitUpdate(ue);
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
	 * Sets the current location state
	 * @param state

	// 2015-12-21 Added setLocationState()
	public void setLocationState(LocationState state) {
		this.currentState = state;
	}

	// 2015-12-20 Added getLocationState()
	public LocationState getLocationState() {
		return currentState;
	}

	public LocationState getInsideVehicleOutsideOnMars() {
		return insideVehicleOutsideOnMars;
	}

	public LocationState getInsideVehicleInSettlement() {
		return insideVehicleInSettlement;
	}


	public LocationState getInsideBuilding() {
		return insideBuilding;
	}

	public LocationState getSettlementVicinity() {
		return settlementVicinity;
	}

	public LocationState getOutsideOnMars() {
		return outsideOnMars;
	}

	public LocationState getOnAPerson() {
		return onAPerson;
	}


	public void leaveBuilding() {
		currentState.leaveBuilding();
	}

	public void enterBuilding() {
		currentState.enterBuilding();
	}

	public void departFromVicinity() {
		currentState.departFromVicinity();
	}

	public void returnToVicinity() {
		currentState.returnToVicinity();
	}

	public void embarkVehicleInVicinity() {
		currentState.embarkVehicleInVicinity();
	}

	public void disembarkVehicleInVicinity() {
		currentState.disembarkVehicleInVicinity();
	}

	public void embarkVehicleInGarage() {
		currentState.embarkVehicleInGarage();
	}

	public void disembarkVehicleInGarage() {
		currentState.disembarkVehicleInGarage();
	}

	public void transferFromSettlementToPerson() {
		currentState.transferFromSettlementToPerson();
	}

	public void transferFromPersonToSettlement() {
		currentState.transferFromPersonToSettlement();
	}

	public void transferFromPersonToVehicle() {
		currentState.transferFromPersonToVehicle();
	}

	public void transferFromVehicleToPerson() {
		currentState.transferFromVehicleToPerson();
	}
*/


	public LocationSituation getLocationSituation() {
		return null;
	}

	public Vehicle getVehicle(){
		return null;
	}

	public Building getBuildingLocation(){
		return null;
	}

	public Settlement getSettlement(){
		return null;
	}

	public Settlement getAssociatedSettlement(){
		return null;
	}

	public Settlement getBuriedSettlement(){
		return null;
	}

	public LocationStateType getLocationStateType() {
		return currentStateType;
	}

	public LocationTag getLocationTag() {;
		return tag;
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
		//if (listeners != null) listeners.clear();
		listeners = null;
	}

}
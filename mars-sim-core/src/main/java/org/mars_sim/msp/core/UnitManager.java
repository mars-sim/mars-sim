/*
 * Mars Simulation Project
 * UnitManager.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.environment.MarsSurface;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.vehicle.Vehicle;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * The UnitManager class contains and manages all units in virtual Mars. It has
 * methods for getting information about units. It is also responsible for
 * creating all units on its construction. There should be only one instance of
 * this class and it should be constructed and owned by Simulation.
 */
public class UnitManager implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final Logger logger = Logger.getLogger(UnitManager.class.getName());

	public static final int THREE_SHIFTS_MIN_POPULATION = 6;
	
	//  The number of bits in the identifier field to use for the type element
	private static final int TYPE_BITS = 4;
	private static final int TYPE_MASK = (1 << (TYPE_BITS)) - 1;
	private static final int MAX_BASE_ID = (1 << (32-TYPE_BITS)) - 1; 
	
	/** Flag true if the class has just been loaded */
	public static boolean justLoaded = true;
	/** Flag true if the class has just been reloaded/deserialized */
	public static boolean justReloaded = false;
	
	/** List of unit manager listeners. */
	private static CopyOnWriteArrayList<UnitManagerListener> listeners;

	private static ExecutorService executor;
	
	private static List<SettlementTask> settlementTaskList = new ArrayList<>();

	/** Map of equipment types and their numbers. */
	private Map<String, Integer> unitCounts = new HashMap<>();
	
	// Data members
	/** The commander's unique id . */
    public int commanderID = -1;
	/** The core engine's original build. */
	public String originalBuild;
	
	/** A map of all map display units (settlements and vehicles). */
	private volatile List<Unit> displayUnits;
	/** A map of all units with its unit identifier. */
	//private volatile Map<Integer, Unit> lookupUnit;// = new HashMap<>();
	/** A map of settlements with its unit identifier. */
	private volatile Map<Integer, Settlement> lookupSettlement;// = new HashMap<>();
	/** A map of sites with its unit identifier. */
	private volatile Map<Integer, ConstructionSite> lookupSite;
	/** A map of persons with its unit identifier. */
	private volatile Map<Integer, Person> lookupPerson;// = new HashMap<>();
	/** A map of robots with its unit identifier. */
	private volatile Map<Integer, Robot> lookupRobot;// = new HashMap<>();
	/** A map of vehicle with its unit identifier. */
	private volatile Map<Integer, Vehicle> lookupVehicle;// = new HashMap<>();
	/** A map of equipment (excluding robots and vehicles) with its unit identifier. */
	private volatile Map<Integer, Equipment> lookupEquipment;// = new HashMap<>();
	/** A map of building with its unit identifier. */
	private volatile Map<Integer, Building> lookupBuilding;// = new HashMap<>();
	
	private static SimulationConfig simulationConfig = SimulationConfig.instance();
	private static Simulation sim = Simulation.instance();

	private static MalfunctionFactory factory;
	
	/** The instance of MarsSurface. */
	private MarsSurface marsSurface;

	/**
	 * Counter of unit identifiers
	 */
	private int uniqueId = 0;

	/**
	 * Constructor.
	 */
	public UnitManager() {
		// Initialize unit collection
		lookupSite       = new ConcurrentHashMap<>();
		lookupSettlement = new ConcurrentHashMap<>();
		lookupPerson     = new ConcurrentHashMap<>();
		lookupRobot      = new ConcurrentHashMap<>();
		lookupEquipment  = new ConcurrentHashMap<>();
		lookupVehicle    = new ConcurrentHashMap<>();
		lookupBuilding   = new ConcurrentHashMap<>();
		
		listeners = new CopyOnWriteArrayList<>();//Collections.synchronizedList(new ArrayList<UnitManagerListener>());

		factory = sim.getMalfunctionFactory();		
	}

	/**
	 * Get the appropriate Unit Map for a Unit identifier
	 * @param id
	 * @return
	 */
	private Map<Integer, ? extends Unit> getUnitMap(Integer id) {
		UnitType type = getTypeFromIdentifier(id);
		Map<Integer,? extends Unit> map = null;

		switch (type) {
		case PERSON:
			map = lookupPerson;
			break;
		case VEHICLE:
			map = lookupVehicle;
			break;
		case SETTLEMENT:
			map = lookupSettlement;
			break;
		case BUILDING:
			map = lookupBuilding;
			break;
		case EQUIPMENT:
			map = lookupEquipment;
			break;
		case ROBOT:
			map = lookupRobot;
			break;
		case CONSTRUCTION:
			map = lookupSite;
			break;
		default:
			throw new IllegalArgumentException("No Unit map for type " + type);
		}
		
		return map;
	}
	
	/**
	 * Gets the unit with a particular identifier (unit id)
	 * 
	 * @param id identifier
	 * @return
	 */
	public Unit getUnitByID(Integer id) {
		if (id.intValue() == Unit.MARS_SURFACE_UNIT_ID)
			return marsSurface;
		else if (id.intValue() == Unit.UNKNOWN_UNIT_ID) {
			return null;
		}
		
		Unit found = getUnitMap(id).get(id);
		if (found == null) {
			logger.warning("Unit not found " + id + " type:" + getTypeFromIdentifier(id)
			               + " baseID:" + (id >>> TYPE_BITS));
		}
		return found;
	}

	public Settlement getSettlementByID(Integer id) {
		return lookupSettlement.get(id);
	}

	public Settlement getCommanderSettlement() {
		return getPersonByID(commanderID).getAssociatedSettlement();
	}
	
	/**
	 * Gets the settlement list including the commander's associated settlement
	 * and the settlement that he's at or in the vicinity of
	 * 
	 * @return {@link List<Settlement>}
	 */
	public List<Settlement> getCommanderSettlements() {
		List<Settlement> settlements = new ArrayList<Settlement>();
		
		Person cc = getPersonByID(commanderID);
		// Add the commander's associated settlement
		Settlement as = cc.getAssociatedSettlement();
		settlements.add(as);
		
		// Find the settlement the commander is at
		Settlement s = cc.getSettlement();
		// If the commander is in the vicinity of a settlement
		if (s == null)
			s = CollectionUtils.findSettlement(cc.getCoordinates());
		if (s != null && as != s)
			settlements.add(s);
		
		return settlements;
	}
	
	public Person getPersonByID(Integer id) {
		return lookupPerson.get(id);
	}

	public Robot getRobotByID(Integer id) {
		return lookupRobot.get(id);
	}

	public Equipment getEquipmentByID(Integer id) {
		return lookupEquipment.get(id);
	}

	public Building getBuildingByID(Integer id) {
		return lookupBuilding.get(id);
	}

	public Vehicle getVehicleByID(Integer id) {
		return lookupVehicle.get(id);
	}
	
	/**
	 * Adds a unit to the unit manager if it doesn't already have it.
	 *
	 * @param unit new unit to add.
	 */
	public synchronized void addUnit(Unit unit) {

		if (unit != null) {
			switch(unit.getUnitType()) {
			case SETTLEMENT:
				lookupSettlement.put(unit.getIdentifier(),
			   			(Settlement) unit);
				addDisplayUnit(unit);
				break;
			case PERSON:
				lookupPerson.put(unit.getIdentifier(),
			   			(Person) unit);
				break;
			case ROBOT:
				lookupRobot.put(unit.getIdentifier(),
			   			(Robot) unit);
				break;
			case VEHICLE:
				lookupVehicle.put(unit.getIdentifier(),
			   			(Vehicle) unit);
				addDisplayUnit(unit);
				break;
			case EQUIPMENT:
				lookupEquipment.put(unit.getIdentifier(),
			   			(Equipment) unit);
				break;
			case BUILDING:
				lookupBuilding.put(unit.getIdentifier(),
						   			(Building) unit);
				break;
			case CONSTRUCTION:
				lookupSite.put(unit.getIdentifier(),
							   (ConstructionSite) unit);
				break;
			case PLANET:
				// Bit of a hack at the moment.
				// Need to revisit once extra Planets added.
				marsSurface = (MarsSurface) unit;
				break;
			default:
				throw new IllegalArgumentException("Cannot store unit type:" + unit.getUnitType());
			}

			// Fire unit manager event.
			fireUnitManagerUpdate(UnitManagerEventType.ADD_UNIT, unit);
		}
	}

	/**
	 * Removes a unit from the unit manager.
	 *
	 * @param unit the unit to remove.
	 */
	public synchronized void removeUnit(Unit unit) {
		Map<Integer,? extends Unit> map = getUnitMap(unit.getIdentifier());

		map.remove(unit.getIdentifier());

		// Fire unit manager event.
		fireUnitManagerUpdate(UnitManagerEventType.REMOVE_UNIT, unit);
	}

	/**
	 * Increment the count of the number of new unit requested.
	 * This count is independent of the actual Units held in the manager.
	 * @param name
	 * @return
	 */
	public int incrementTypeCount(String name) {
		synchronized (unitCounts) {
			return unitCounts.merge(name, 1, (a, b) -> a + b);
		}
	}	



	
	public void setCommanderId(int commanderID) {
		this.commanderID = commanderID;
	}
	public int getCommanderID() {
		return commanderID;
	}

	/**
	 * Determines the number of shifts for a settlement and assigns a work shift for
	 * each person
	 * 
	 * @param settlement
	 * @param pop population
	 */
	public void setupShift(Settlement settlement, int pop) {

		int numShift = 0;
		// ShiftType shiftType = ShiftType.OFF;

		if (pop == 1) {
			numShift = 1;
		} else if (pop < THREE_SHIFTS_MIN_POPULATION) {
			numShift = 2;
		} else {// if pop >= 6
			numShift = 3;
		}

		settlement.setNumShift(numShift);

		Collection<Person> people = settlement.getAllAssociatedPeople();

		for (Person p : people) {
			// keep pop as a param just
			// to speed up processing
			p.setShiftType(settlement.getAnEmptyWorkShift(pop));
		}

	}


	/**
	 * Notify all the units that time has passed. Times they are a changing.
	 *
	 * @param pulse the amount time passing (in millisols)
	 * @throws Exception if error during time passing.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {	
		if (pulse.isNewSol() || justLoaded) {			
			// Compute reliability daily
			factory.computeReliability();
			justLoaded = false;
		}

		if (pulse.getElapsed() > 0) {
			runExecutor(pulse);
		}
		else {
			logger.warning("Zero elapsed pulse #" + pulse.getId());
		}
		
		return true;
	}
	
	/**
	 * Sets up executive service
	 */
	private void setupExecutor() {
		if (executor == null) {
			int size = (int)(getSettlementNum()/2D);
			int num = Math.min(size, Simulation.NUM_THREADS - simulationConfig.getUnusedCores());
			if (num <= 0) num = 1;
			logger.config("Setting up " + num + " thread(s) for running the settlement update.");
			executor = Executors.newFixedThreadPool(num,
					new ThreadFactoryBuilder().setNameFormat("unitmanager-thread-%d").build());
		}
	}
	
	/**
	 * Sets up settlement tasks for executive service
	 */
	private void setupTasks() {
		if (settlementTaskList == null || settlementTaskList.isEmpty()) {
			settlementTaskList = new CopyOnWriteArrayList<>();
			lookupSettlement.values().forEach(s -> activateSettlement(s));
		}
	}
	
	public void activateSettlement(Settlement s) {
		if (!lookupSettlement.containsKey(s.getIdentifier())) {
			throw new IllegalStateException("Do not know new Settlement "
						+ s.getName());
		}
		
		SettlementTask st = new SettlementTask(s);
		settlementTaskList.add(st);
	}
	
	/**
	 * Fires the clock pulse to each clock listener
	 * 
	 * @param pulse
	 */
	private void runExecutor(ClockPulse pulse) {
		setupExecutor();
		setupTasks();
		settlementTaskList.forEach(s -> {
			s.setCurrentPulse(pulse);
		});

		// Execute all listener concurrently and wait for all to complete before advancing
		// Ensure that Settlements stay synch'ed and some don't get ahead of others as tasks queue
		try {
			List<Future<String>> results = executor.invokeAll(settlementTaskList);
			for (Future<String> future : results) {
				future.get();
			};
		} 
		catch (ExecutionException ee) {
			// Problem running the pulse
            logger.log(Level.SEVERE, "Problem running the pulse : " + ee.getMessage());
		}
		catch (InterruptedException ie) {
			// Program probably exiting
			if (executor.isShutdown()) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	/**
	 * Ends the current executor
	 */
	public void endSimulation() {
		if (executor != null)
			executor.shutdownNow();
	}
	
	/**
	 * Get number of settlements
	 *
	 * @return the number of settlements
	 */
	public int getSettlementNum() {
		return lookupSettlement.size();//CollectionUtils.getSettlement(units).size();
	}

	/**
	 * Get settlements in virtual Mars
	 *
	 * @return Collection of settlements
	 */
	public Collection<Settlement> getSettlements() {
		if (lookupSettlement != null && !lookupSettlement.isEmpty()) {
			return Collections.unmodifiableCollection(lookupSettlement.values());//CollectionUtils.getSettlement(units); 
		}
		else {
//			logger.severe("lookupSettlement is null.");
			return new ArrayList<>();
		}
	}

	/**
	 * Get vehicles in virtual Mars
	 *
	 * @return Collection of vehicles
	 */
	public Collection<Vehicle> getVehicles() {
		return Collections.unmodifiableCollection(lookupVehicle.values());//CollectionUtils.getVehicle(units);
	}

	/**
	 * Get number of people
	 *
	 * @return the number of people
	 */
	public int getTotalNumPeople() {
		return lookupPerson.size();//CollectionUtils.getPerson(units).size();
	}

	/**
	 * Get all people in Mars
	 *
	 * @return Collection of people
	 */
	public Collection<Person> getPeople() {
		return Collections.unmodifiableCollection(lookupPerson.values());//CollectionUtils.getPerson(units);
	}

	/**
	 * Get Robots in virtual Mars
	 *
	 * @return Collection of Robots
	 */
	public Collection<Robot> getRobots() {
		return Collections.unmodifiableCollection(lookupRobot.values());//CollectionUtils.getRobot(units);
	}

	private void addDisplayUnit(Unit unit) {
		if (displayUnits == null)
			displayUnits = new ArrayList<>();
		
		displayUnits.add(unit);
	}
	
	/**
	 * Obtains the settlement and vehicle units for map display
	 * @return
	 */
	public List<Unit> getDisplayUnits() {
		return displayUnits; //findDisplayUnits();	
	}

	/**
	 * Adds a unit manager listener
	 * 
	 * @param newListener the listener to add.
	 */
	public final void addUnitManagerListener(UnitManagerListener newListener) {
		if (listeners == null) {
			listeners = new CopyOnWriteArrayList<>();//Collections.synchronizedList(new ArrayList<UnitManagerListener>());
		}
		if (!listeners.contains(newListener)) {
			listeners.add(newListener);
		}
	}

	/**
	 * Removes a unit manager listener
	 * 
	 * @param oldListener the listener to remove.
	 */
	public final void removeUnitManagerListener(UnitManagerListener oldListener) {
		if (listeners == null) {
			listeners = new CopyOnWriteArrayList<>();//Collections.synchronizedList(new ArrayList<UnitManagerListener>());
		}
		if (listeners.contains(oldListener)) {
			listeners.remove(oldListener);
		}
	}

	/**
	 * Fire a unit update event.
	 * 
	 * @param eventType the event type.
	 * @param unit      the unit causing the event.
	 */
	public final void fireUnitManagerUpdate(UnitManagerEventType eventType, Unit unit) {
		if (listeners == null) {
			listeners = new CopyOnWriteArrayList<>();//Collections.synchronizedList(new ArrayList<UnitManagerListener>());
		}
		synchronized (listeners) {
			for (UnitManagerListener listener : listeners) {
				listener.unitManagerUpdate(new UnitManagerEvent(this, eventType, unit));
			}
		}
	}


	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 */
	public void reinit(MarsClock clock) {
		
		for (Person p: lookupPerson.values()) {
			p.reinit();
		}
		for (Robot r: lookupRobot.values()) {
			r.reinit();
		}
		for (Building b: lookupBuilding.values()) {
			b.reinit();
		}
		for (Settlement s: lookupSettlement.values()) {
			s.reinit();
		}
		
		// Sets up the executor
		setupExecutor();
		// Sets up the concurrent tasks
		setupTasks();
	}
	
	/**
	 * Returns Mars surface instance
	 * 
	 * @return {@Link MarsSurface}
	 */
	public MarsSurface getMarsSurface() {
		return marsSurface;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		Iterator<Settlement> i1 = lookupSettlement.values().iterator();
		while (i1.hasNext()) {
			i1.next().destroy();
		}
		Iterator<ConstructionSite> i0 = lookupSite.values().iterator();
		while (i0.hasNext()) {
			i0.next().destroy();
		}
		Iterator<Vehicle> i2 = lookupVehicle.values().iterator();
		while (i2.hasNext()) {
			i2.next().destroy();
		}
		Iterator<Building> i3 = lookupBuilding.values().iterator();
		while (i3.hasNext()) {
			i3.next().destroy();
		}
		Iterator<Person> i4 = lookupPerson.values().iterator();
		while (i4.hasNext()) {
			i4.next().destroy();
		}
		Iterator<Robot> i5 = lookupRobot.values().iterator();
		while (i5.hasNext()) {
			i5.next().destroy();
		}
		Iterator<Equipment> i6 = lookupEquipment.values().iterator();
		while (i6.hasNext()) {
			i6.next().destroy();
		}
	
		lookupSite.clear();
		lookupSettlement.clear();
		lookupVehicle.clear();
		lookupBuilding.clear();
		lookupPerson.clear();
		lookupRobot.clear();
		lookupEquipment.clear();
		
		lookupSite = null;
		lookupSettlement = null;
		lookupVehicle = null;
		lookupBuilding = null;
		lookupPerson = null;
		lookupRobot = null;
		lookupEquipment = null;

		sim = null;
		simulationConfig = SimulationConfig.instance();
		marsSurface = null;

		listeners.clear();
		listeners = null;

		factory = null;
	}
	
	/**
	 * Prepares the Settlement task for setting up its own thread.
	 */
	class SettlementTask implements Callable<String> {
		private Settlement settlement;
		private ClockPulse currentPulse;
		
		protected Settlement getSettlement() {
			return settlement;
		}
		
		public void setCurrentPulse(ClockPulse pulse) {
			this.currentPulse = pulse;
		}

		private SettlementTask(Settlement settlement) {
			this.settlement = settlement;
		}

		@Override
		public String call() throws Exception {
			settlement.timePassing(currentPulse);	
			return settlement.getName() + " completed pulse #" + currentPulse.getId();
		}
	}

	/**
	 * Extracts the UnitType from an identifier
	 * @param id
	 * @return
	 */
	public static UnitType getTypeFromIdentifier(int id) {
		// Extract the bottom 4 bit
		int typeId = (id & TYPE_MASK);
		
		return UnitType.values()[typeId];
	}
	
	/**
	 * Generate a new unique UnitId for a certain type. This will be used later
	 * for lookups.
	 * The lowest 4 bits contain the ordinal of the UnitType. Top remaining bits 
	 * are a unique increasing number.
	 * This guarantees 
	 * uniqueness PLUS a quick means to identify the UnitType from only the 
	 * identifier.
	 * @param unitType
	 * @return
	 */
	public synchronized int generateNewId(UnitType unitType) {
		int baseId = uniqueId++;
		if (baseId >= MAX_BASE_ID) {
			throw new IllegalStateException("Too many Unit created " + MAX_BASE_ID);
		}
		int typeId = unitType.ordinal();
		
		return (baseId << TYPE_BITS) + typeId;
	}

}

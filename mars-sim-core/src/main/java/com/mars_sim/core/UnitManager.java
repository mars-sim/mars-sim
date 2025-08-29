/**
 * Mars Simulation Project
 * UnitManager.java
 * @date 2025-07-22 (patched 2025-08-29)
 * @author Scott Davis
 *
 * Notes (patch):
 * - Applies the MasterClock listener-hardening pattern to UnitManager.
 * - Uses CopyOnWriteArraySet for CME-safe listener storage (already present).
 * - Adds a bounded listener ExecutorService and delivers events in parallel
 *   using invokeAll(), while waiting for completion to keep the world in sync.
 * - Shields each listener call so one faulty listener cannot break delivery.
 * - Executor sizing is bounded by the number of cores and listener count.
 * - Tiny perf tidy: avoid stream().count() in getObjectsLoad() and reuse size lookups.
 */
package com.mars_sim.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.environment.MarsSurface;
import com.mars_sim.core.environment.OuterSpace;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.moon.Moon;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.unit.TemporalExecutor;
import com.mars_sim.core.unit.TemporalExecutorService;
import com.mars_sim.core.unit.TemporalThreadExecutor;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.SimulationConfig;

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
	private static final SimLogger logger = SimLogger.getLogger(UnitManager.class.getName());

	public static final int THREE_SHIFTS_MIN_POPULATION = 6;

	//  The number of bits in the identifier field to use for the type element
	private static final int TYPE_BITS = 4;
	private static final int TYPE_MASK = (1 << (TYPE_BITS)) - 1;
	private static final int MAX_BASE_ID = (1 << (32-TYPE_BITS)) - 1;

	public static final String THREAD = "thread";
	public static final String SHARED = "shared";

	// Data members
	/** Counter of unit identifiers. (Atomic to avoid global monitor contention) */
	private final java.util.concurrent.atomic.AtomicInteger uniqueId = new java.util.concurrent.atomic.AtomicInteger(0);
	/** The commander's unique id . */
	private int commanderID = -1;
	/** The core engine's original build. */
	private String originalBuild;

	/** Map: UnitType -> listeners (CME-safe). */
	private transient EnumMap<UnitType, CopyOnWriteArraySet<UnitManagerListener>> listeners;

	/** Bounded executor for parallel, shielded listener delivery. */
	private transient ExecutorService umListenerExecutor;

	private transient TemporalExecutor executor;

	/** Map of equipment types and their numbers. */
	private Map<String, Integer> unitCounts = new HashMap<>();
	/** A map of settlements with its unit identifier. */
	private Map<Integer, Settlement> lookupSettlement;
	/** A map of sites with its unit identifier. */
	private Map<Integer, ConstructionSite> lookupSite;
	/** A map of persons with its unit identifier. */
	private Map<Integer, Person> lookupPerson;
	/** A map of robots with its unit identifier. */
	private Map<Integer, Robot> lookupRobot;
	/** A map of vehicle with its unit identifier. */
	private Map<Integer, Vehicle> lookupVehicle;
	/** A map of equipment (excluding robots and vehicles) with its unit identifier. */
	private Map<Integer, Equipment> lookupEquipment;
	/** A map of building with its unit identifier. */
	private Map<Integer, Building> lookupBuilding;
	/** A map of settlements with its coordinates. (CME-safe for parallel reads/writes) */
	private transient Map<Coordinates, Settlement> settlementCoordinateMap = new ConcurrentHashMap<>();

	/** The instance of Mars Surface. */
	private MarsSurface marsSurface;

	/** The instance of Outer Space. */
	private OuterSpace outerSpace;

	/** The instance of Moon. */
	private Moon moon;

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
	}

	/**
	 * Gets the appropriate Unit Map for a Unit type.
	 *
	 * @param type
	 * @return
	 */
	private Map<Integer, ? extends Unit> getUnitMap(UnitType type) {
		return switch (type) {
			case PERSON -> lookupPerson;
			case VEHICLE -> lookupVehicle;
			case SETTLEMENT -> lookupSettlement;
			case BUILDING -> lookupBuilding;
			case EVA_SUIT, CONTAINER -> lookupEquipment;
			case ROBOT -> lookupRobot;
			case CONSTRUCTION -> lookupSite;
			default -> throw new IllegalArgumentException("No Unit map for type " + type);
		};
	}

	/**
	 * Gets the Unit of a certain type matching the name.
	 *
	 * @param type The UnitType to search for
	 * @param name Name of the unit
	 */
	public Unit getUnitByName(UnitType type, String name) {
		Map<Integer,? extends Unit> map = getUnitMap(type);
		for(Unit u : map.values()) {
			if (u.getName().equalsIgnoreCase(name)) {
				return u;
			}
		}
		return null;
	}

	/**
	 * Gets the unit with a particular identifier (unit id).
	 *
	 * @param id identifier
	 * @return
	 */
	public Unit getUnitByID(Integer id) {
		if (id.intValue() == Unit.MARS_SURFACE_UNIT_ID)
			return marsSurface;
		else if (id.intValue() == Unit.OUTER_SPACE_UNIT_ID)
			return outerSpace;
		else if (id.intValue() == Unit.UNKNOWN_UNIT_ID) {
			return null;
		}

		UnitType type = getTypeFromIdentifier(id);
		Unit found = getUnitMap(type).get(id);
		if (found == null) {
			logger.warning("Unit not found. id: " + id + ". Type of unit: " + type
			               + " (Base ID: " + (id >>> TYPE_BITS) + ").");
		}
		return found;
	}

	public Settlement getSettlementByID(Integer id) {
		return lookupSettlement.get(id);
	}

	/**
	 * Finds a nearby settlement based on its coordinates.
	 *
	 * @param c {@link Coordinates}
	 * @return
	 */
	public Settlement findSettlement(Coordinates c) {
		return settlementCoordinateMap.get(c);
	}

	/**
	 * Gets commander settlement.
	 *
	 * @return
	 */
	public Settlement getCommanderSettlement() {
		return getPersonByID(commanderID).getAssociatedSettlement();
	}

	/**
	 * Gets the settlement list including the commander's associated settlement
	 * and the settlement that he's at or in the vicinity.
	 *
	 * @return {@link List<Settlement>}
	 */
	public List<Settlement> getCommanderSettlements() {
		List<Settlement> settlements = new ArrayList<>();

		Person cc = getPersonByID(commanderID);
		// Add the commander's associated settlement
		Settlement as = cc.getAssociatedSettlement();
		settlements.add(as);

		// Find the settlement the commander is at
		Settlement s = cc.getSettlement();
		// If the commander is in the vicinity of a settlement
		if (s == null)
			s = findSettlement(cc.getCoordinates());
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

	public Building getBuildingByID(Integer id) {
		return lookupBuilding.get(id);
	}

	/**
	 * Adds a unit to the unit manager if it doesn't already have it.
	 *
	 * @param unit new unit to add.
	 */
	public synchronized void addUnit(Unit unit) {
		int unitIdentifier = unit.getIdentifier();

		switch(unit) {
			case Settlement s -> {
				lookupSettlement.put(unitIdentifier, s);
				activateSettlement(s);
			}
			case Person p -> lookupPerson.put(unitIdentifier, p);
			case Robot r -> lookupRobot.put(unitIdentifier, r);
			case Vehicle v -> lookupVehicle.put(unitIdentifier, v);
			case Equipment e -> lookupEquipment.put(unitIdentifier, e);
			case Building b -> lookupBuilding.put(unitIdentifier, b);
			case ConstructionSite c -> lookupSite.put(unitIdentifier, c);
			case MarsSurface ms -> marsSurface = ms;
			case OuterSpace os -> outerSpace = os;
			case Moon m -> moon = m;
			default -> throw new IllegalArgumentException(
					"Cannot store unit type:" + unit.getUnitType());
		}

		// Notify listeners (CME-safe & exception-shielded, parallel delivery)
		fireUnitManagerUpdate(UnitManagerEventType.ADD_UNIT, unit);
	}

	/**
	 * Removes a unit from the unit manager.
	 *
	 * @param unit the unit to remove.
	 */
	public synchronized void removeUnit(Unit unit) {
		UnitType type = getTypeFromIdentifier(unit.getIdentifier());
		Map<Integer,? extends Unit> map = getUnitMap(type);

		map.remove(unit.getIdentifier());

		// Fire unit manager event.
		fireUnitManagerUpdate(UnitManagerEventType.REMOVE_UNIT, unit);
	}

	/**
	 * Increments the count of the number of new unit requested.
	 * This count is independent of the actual Units held in the manager.
	 *
	 * @param name
	 * @return
	 */
	public int incrementTypeCount(String name) {
		synchronized (unitCounts) {
			return unitCounts.merge(name, 1, Integer::sum);
		}
	}

	public void setCommanderId(int commanderID) {
		this.commanderID = commanderID;
	}

	public int getCommanderID() {
		return commanderID;
	}


	/**
	 * Notifies all the units that time has passed. Times they are a changing.
	 *
	 * @param pulse the amount time passing (in millisols)
	 * @throws Exception if error during time passing.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		if (pulse.getElapsed() > 0) {
			executor.applyPulse(pulse);
		}
		else {
			logger.warning("Zero elapsed pulse #" + pulse.getId());
		}

		return true;
	}

	/**
	 * Adds a settlement to the managed set and activate it for time pulses.
	 *
	 * @param s
	 */
	private void activateSettlement(Settlement s) {
		settlementCoordinateMap.put(s.getCoordinates(), s);

		logger.config("Activating the settlement task pulse for " + s + ".");
		if (executor == null) {
			String execType = SimulationConfig.instance().getExecutorType();
			executor = switch(execType) {
				case THREAD -> new TemporalThreadExecutor();
				case SHARED -> new TemporalExecutorService("Settlement-");
				default -> throw new IllegalArgumentException("Unknown executor type called " + execType);
			};
		}
		executor.addTarget(s);
	}

	/**
	 * Ends the current executor.
	 */
	public void endSimulation() {
		if (executor != null)
			executor.stop();
		// Stop the unit manager listener executor as well
		if (umListenerExecutor != null)
			umListenerExecutor.shutdownNow();
	}

	/**
	 * Gets a collection of settlements.
	 *
	 * @return Collection of settlements
	 */
	public Collection<Settlement> getSettlements() {
		return Collections.unmodifiableCollection(lookupSettlement.values());
	}

	/**
	 * Gets a collection of vehicles.
	 *
	 * @return Collection of vehicles
	 */
	public Collection<Vehicle> getVehicles() {
		return Collections.unmodifiableCollection(lookupVehicle.values());
	}

	/**
	 * Gets a collection of people.
	 *
	 * @return Collection of people
	 */
	public Collection<Person> getPeople() {
		return Collections.unmodifiableCollection(lookupPerson.values());
	}

	/**
	 * Gets a collection of robots.
	 *
	 * @return Collection of Robots
	 */
	public Collection<Robot> getRobots() {
		return Collections.unmodifiableCollection(lookupRobot.values());
	}

	/**
	 * Gets a collection of EVA suits.
	 *
	 * @return Collection of EVA suits
	 */
	public Collection<Equipment> getEVASuits() {
		return lookupEquipment.values().stream()
				.filter(e -> e.getUnitType() == UnitType.EVA_SUIT)
				.collect(Collectors.toSet());
	}

	/**
	 * Gets a composite load score from people, robots, buildings and vehicles.
	 * (Micro perf tidy: reuse size() values and avoid streams.)
	 *
	 * @return composite load metric
	 */
	public float getObjectsLoad() {
		final int p = lookupPerson.size();
		final int r = lookupRobot.size();
		final int b = lookupBuilding.size();
		final int v = lookupVehicle.size();
		return 0.45f * p + 0.20f * r + 0.25f * b + 0.10f * v;
	}
	
	/**
	 * Adds a unit manager listener.
	 *
	 * @param source UnitType monitored
	 * @param newListener the listener to add.
	 */
	public final void addUnitManagerListener(UnitType source, UnitManagerListener newListener) {
		if (newListener == null || source == null) return;

		if (listeners == null) {
			listeners = new EnumMap<>(UnitType.class);
		}

		// CopyOnWriteArraySet prevents CME during concurrent fire/update
		listeners.computeIfAbsent(source, k -> new CopyOnWriteArraySet<>())
		         .add(newListener);

		// Ensure executor exists (bounded by cores & listener count)
		ensureListenerExecutor();
	}

	/**
	 * Removes a unit manager listener.
	 *
	 * @param oldListener the listener to remove.
	 */
	public final void removeUnitManagerListener(UnitType source, UnitManagerListener oldListener) {
		if (listeners == null || source == null || oldListener == null) return;

		CopyOnWriteArraySet<UnitManagerListener> l = listeners.get(source);
		if (l != null) {
			l.remove(oldListener);
		}
	}

	/**
	 * Computes the total number of registered listeners across all sources.
	 */
	private int totalListenerCount() {
		if (listeners == null || listeners.isEmpty()) return 0;
		int c = 0;
		for (CopyOnWriteArraySet<UnitManagerListener> s : listeners.values()) {
			if (s != null) c += s.size();
		}
		return c;
	}

	/**
	 * Starts the listener thread pool executor if needed.
	 * Pool size is bounded by number of cores and total listeners.
	 */
	private void ensureListenerExecutor() {
		if (umListenerExecutor == null
				|| umListenerExecutor.isShutdown()
				|| umListenerExecutor.isTerminated()) {
			int listenerCount = Math.max(1, totalListenerCount());
			int cores = com.mars_sim.core.SimulationRuntime.NUM_CORES;
			int desired = Math.max(1, Math.min(cores, listenerCount));
			logger.config("Setting up UnitManager listener executor with " + desired + " thread(s).");
			umListenerExecutor = Executors.newFixedThreadPool(
					desired,
					new ThreadFactoryBuilder().setNameFormat("unitManager-listener-%d").build()
			);
		}
	}

	/**
	 * Fires a unit update event. (CME-safe & exception-shielded, parallel)
	 *
	 * @param eventType the event type.
	 * @param unit      the unit causing the event.
	 */
	private final void fireUnitManagerUpdate(UnitManagerEventType eventType, Unit unit) {
		if (listeners == null || unit == null) {
			return;
		}

		CopyOnWriteArraySet<UnitManagerListener> l = listeners.get(unit.getUnitType());
		if (l == null || l.isEmpty()) {
			return;
		}

		// Prepare event
		UnitManagerEvent e = new UnitManagerEvent(this, eventType, unit);

		// Make sure executor exists
		ensureListenerExecutor();

		// Build a parallel batch with shielding
		final java.util.List<Callable<String>> batch = new java.util.ArrayList<>(l.size());
		for (UnitManagerListener listener : l) {
			batch.add(() -> {
				try {
					listener.unitManagerUpdate(e);
				}
				catch (Throwable ex) {
					// Never let a bad listener break delivery to others
					logger.severe("UnitManager listener threw during " + eventType + " for " + unit + ": ", ex);
				}
				return "ok";
			});
		}

		try {
			final List<Future<String>> futures = umListenerExecutor.invokeAll(batch);
			// Surface (should be none, because we shield inside tasks)
			for (Future<String> f : futures) {
				try {
					f.get();
				}
				catch (ExecutionException ee) {
					logger.severe("ExecutionException in UnitManager listener batch: ", ee);
				}
			}
		}
		catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			logger.severe("Interrupted while delivering UnitManager event " + eventType + " for " + unit + ": ", ie);
		}
		catch (RejectedExecutionException ree) {
			logger.severe("Rejected while delivering UnitManager event (executor shutdown): ", ree);
		}
	}

	/**
	 * Returns the Mars surface instance.
	 *
	 * @return {@Link MarsSurface}
	 */
	public MarsSurface getMarsSurface() {
		return marsSurface;
	}

	/**
	 * Returns the outer space instance.
	 *
	 * @return {@Link OuterSpace}
	 */
	public OuterSpace getOuterSpace() {
		return outerSpace;
	}

	/**
	 * Returns the Moon instance.
	 *
	 * @return {@Link Moon}
	 */
	public Moon getMoon() {
		return moon;
	}

	/**
	 * Extracts the UnitType from an identifier.
	 *
	 * @param id
	 * @return
	 */
	public static UnitType getTypeFromIdentifier(int id) {
		// Extract the bottom 4 bit
		int typeId = (id & TYPE_MASK);

		return UnitType.values()[typeId];
	}

	/**
	 * Generates a new unique UnitId for a certain type. This will be used later
	 * for lookups.
	 * The lowest 4 bits contain the ordinal of the UnitType. Top remaining bits
	 * are a unique increasing number.
	 * This guarantees uniqueness PLUS a quick means to identify the UnitType
	 * from only the identifier.
	 *
	 * @param unitType
	 * @return
	 */
	public int generateNewId(UnitType unitType) {
		int baseId = uniqueId.getAndIncrement();
		if (baseId >= MAX_BASE_ID) {
			throw new IllegalStateException("Too many Unit created " + MAX_BASE_ID);
		}
		int typeId = unitType.ordinal();

		return (baseId << TYPE_BITS) + typeId;
	}

	public void setOriginalBuild(String build) {
		originalBuild = build;
	}

	public String getOriginalBuild() {
		return originalBuild;
	}

	/**
	 * Reloads instances after loading from a saved sim.
	 */
	public void reinit() {

		lookupPerson.values().forEach(Person::reinit);
		lookupRobot.values().forEach(Robot::reinit);
		lookupSettlement.values().forEach(Settlement::reinit);

		// Sets up the concurrent tasks
		settlementCoordinateMap = new ConcurrentHashMap<>();
		lookupSettlement.values().forEach(this::activateSettlement);

		// (Re)ensure listener executor after reload if listeners are present
		ensureListenerExecutor();
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {

		lookupSettlement.values().forEach(Settlement::destroy);
		lookupSite.values().forEach(ConstructionSite::destroy);
		lookupVehicle.values().forEach(Vehicle::destroy);
		lookupBuilding.values().forEach(Building::destroy);
		lookupPerson.values().forEach(Person::destroy);
		lookupRobot.values().forEach(Robot::destroy);
		lookupEquipment.values().forEach(Equipment::destroy);

		lookupSite.clear();
		lookupSettlement.clear();
		lookupVehicle.clear();
		lookupBuilding.clear();
		lookupPerson.clear();
		lookupRobot.clear();
		lookupEquipment.clear();

		marsSurface = null;

		// Stop the UnitManager listener executor
		if (umListenerExecutor != null) {
			umListenerExecutor.shutdownNow();
			umListenerExecutor = null;
		}

		listeners = null;
	}
}

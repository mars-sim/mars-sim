/*
 * Mars Simulation Project
 * Function.java
 * @date 2023-11-20
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.mars_sim.core.UnitManager;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.environment.Weather;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingConfig;
import com.mars_sim.core.structure.building.FunctionSpec;
import com.mars_sim.core.structure.building.function.farming.CropConfig;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.mapdata.location.LocalPosition;

/**
 * A settlement building function.
 */
public abstract class Function implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Function.class.getName());

	protected static final int BASE_MAINT_TIME = 5;
	
	protected static final int WATER_ID = ResourceUtil.waterID;
	protected static final int BLACK_WATER_ID = ResourceUtil.blackWaterID;
	protected static final int GREY_WATER_ID = ResourceUtil. greyWaterID;
	protected static final int TOILET_TISSUE_ID = ResourceUtil.toiletTissueID;
	protected static final int TOXIC_WASTE_ID = ResourceUtil.toxicWasteID;

	private long lastPulse = 0; // First initial pulse is always 1

	private FunctionType type;
	
	protected Building building;

	/**	A map of activity spot with the id of a person occupying this spot.  */
	private Map<LocalPosition, Integer> activitySpotMap = new HashMap<>();
	
	protected static BuildingConfig buildingConfig;
	protected static PersonConfig personConfig;
	protected static CropConfig cropConfig;

	protected static SurfaceFeatures surface;
	protected static UnitManager unitManager;
	protected static Weather weather;

	protected static MasterClock masterClock;

	/**
	 * Constructor.
	 *
	 * @param type Type of this function
	 * @param spec Functional configuration
	 * @param building Parent building.
	 */
	protected Function(FunctionType type, FunctionSpec spec, Building building) {
		this.type = type;
		this.building = building;

		// load any activity spots
		if (spec != null) {
			List<LocalPosition> activitySpots = spec.getActivitySpots();

			for (LocalPosition p: activitySpots) {
				activitySpotMap.put(p, -1);
			}
		}
	}


	/**
	 * Gets the function.
	 *
	 * @return {@link FunctionType}
	 */
	public FunctionType getFunctionType() {
		return type;
	}

	/**
	 * Gets the function's building.
	 *
	 * @return {@link Building}
	 */
	public Building getBuilding() {
		return building;
	}

	/**
	 * Gets the maintenance time for this building function.
	 *
	 * @return maintenance work time (millisols). Default zero
	 */
	public double getMaintenanceTime() {
		return BASE_MAINT_TIME;
	}

	/**
	 * Gets the function's malfunction scope strings.
	 *
	 * @return array of scope strings.
	 */
	public Set<String> getMalfunctionScopeStrings() {
		return Set.of(type.getName());
	}

	/**
	 * Is this time pulse valid for the Unit.Has it been already applied?
	 * The logic on this method can be commented out later on.
	 * 
	 * @param pulse Pulse to apply
	 * @return Valid to accept
	 */
	protected boolean isValid(ClockPulse pulse) {
		long newPulse = pulse.getId();
		boolean result = (newPulse > lastPulse);
		if (!result) {
			// Seen already
			logger.severe(building, type + ": rejected pulse #" + newPulse
						+ ", last pulse was " + lastPulse);
		}
		lastPulse = newPulse;
		return result;
	}

	/**
	 * Time passing for the function. By default this does nothing.
	 *
	 * @param time amount of time passing (in millisols)
	 */
	public boolean timePassing(ClockPulse pulse) {
		return true;
	}

	/**
	 * Gets the amount of heat required when function is at full heat.
	 *
	 * @return heat (kW) default 0
	 */
	public double getFullHeatRequired() {
		return 0;
	}
	
	/**
	 * Gets the amount of heat required when function is at heat down level.
	 *
	 * @return heat (kW) default zero
	 */
	public double getPoweredDownHeatRequired() {
		return 0;
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 *
	 * @return power (kW) default zero
	 */
	public double getFullPowerRequired() {
		return 0;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 *
	 * @return power (kW) default zero
	 */
	public double getPoweredDownPowerRequired() {
		return 0;
	}

	/**
	 * Performs any actions needed when removing this building function from the
	 * settlement.
	 */
	public void removeFromSettlement() {
		// Override as needed.
	}

	/**
	 * Adds the id of the unit to a local position.
	 * 
	 * @param p
	 * @param id
	 * @return
	 */
	public boolean addToNewActivitySpot(LocalPosition p, int id) {
		if (activitySpotMap.isEmpty())
			return false;
		
		if (activitySpotMap.containsKey(p) && activitySpotMap.get(p) == -1) {
			activitySpotMap.put(p, id);
			return true;
		}
		return false;
	}

	/**
	 * Removes the id of the unit in a local position.
	 * 
	 * @param p
	 * @param id
	 * @return
	 */
	public boolean removeFromActivitySpot(int id) {
		if (activitySpotMap.isEmpty())
			return false;
		
		for (Entry<LocalPosition, Integer> entry : activitySpotMap.entrySet()) {
	        if (entry.getValue().equals(id)) {
	        	LocalPosition p = entry.getKey();
	        	activitySpotMap.put(p, -1);
	        	return true;
	        }
	    }
	
		return false;
	}
	
	/**
	 * Gets an available local activity spot for the worker.
	 *
	 * @param worker the worker looking for activity spot.
	 * @return activity spot as {@link Point2D} or null if none found.
	 */
	public LocalPosition getAvailableActivitySpot() {

		if (activitySpotMap.isEmpty())
			return null;
				
		for (Entry<LocalPosition, Integer> entry : activitySpotMap.entrySet()) {
			LocalPosition p = entry.getKey();
			int id = entry.getValue();
			if (id == -1) {
				return p;
			}
		}

		return null;
		
//		LocalPosition result = null;		
//		
//		if (activitySpots != null) {
//
//			List<LocalPosition> availableActivitySpots = new ArrayList<>();
//			Iterator<LocalPosition> i = activitySpots.iterator();
//			while (i.hasNext()) {
//				LocalPosition buildingLoc = i.next();
//				// Check if spot is unoccupied.
//				boolean available = isActivitySpotEmpty(buildingLoc);
//
//				// If available, add activity spot to available list.
//				if (available) {
//					// Convert activity spot from building local to settlement local.
//					LocalPosition settlementActivitySpot = LocalAreaUtil.getLocalRelativePosition(buildingLoc, getBuilding());
//					availableActivitySpots.add(settlementActivitySpot);
//				}
//			}
//
//			if (!availableActivitySpots.isEmpty()) {
//
//				// Choose a random available activity spot.
//				int index = RandomUtil.getRandomInt(availableActivitySpots.size() - 1);
//				result = availableActivitySpots.get(index);
//			}
//		}
//
//		return result;
	}

	/**
	 * Checks if an activity spot is empty/unoccupied.
	 *
	 * @param s as a {@link Point2D}
	 * @return true if this activity spot is empty.
	 */
	public boolean isActivitySpotEmpty(LocalPosition s) {
		
		if (!activitySpotMap.isEmpty() 
			&& activitySpotMap.keySet().contains(s)
			&& activitySpotMap.get(s) == -1) {
			return true;
		}
		
		return false;
		
//		if (activitySpots == null || activitySpots.isEmpty())
//			return true;
//
//		boolean result = true;
//
//		// Convert activity spot from building local to settlement local.
//		Building b = getBuilding();
//		LocalPosition settlementActivitySpot = LocalAreaUtil.getLocalRelativePosition(s, b);
//
//		for (Person person : b.getInhabitants()) {
//			// Check if person's location is identical or very very close (1e-5 meters) to
//			// activity spot.
//			if (person.isInSettlement() && settlementActivitySpot.isClose(person.getPosition())) {
//				return false;
//			}
//		}
//
//		for (Robot robot : b.getRobots()) {
//			// Check if robot location is identical or very very close (1e-5 meters) to
//			// activity spot.
//			if (settlementActivitySpot.isClose(robot.getPosition())) {
//				return false;
//			}
//		}
//
//		return result;
	}

	/**
	 * Checks if a worker is at an activity spot for this building function.
	 *
	 * @param worker the Worker.
	 * @return true if the worker's Position is currently at an activity spot.
	 */
	public boolean isAtActivitySpot(Worker worker) {
		
		if (!activitySpotMap.isEmpty() && 
				!activitySpotMap.isEmpty() && activitySpotMap.values().contains(worker.getIdentifier())) {
			return true;
		}
		
		return false;
				
//		LocalPosition target = worker.getPosition();
//		boolean result = false;
//
//		Iterator<LocalPosition> i = activitySpots.iterator();
//		while (i.hasNext() && !result) {
//			LocalPosition activitySpot = i.next();
//			// Convert activity spot from building local to settlement local.
//			LocalPosition settlementActivitySpot = LocalAreaUtil.getLocalRelativePosition(activitySpot, getBuilding());
//
//			// Check if location is very close to activity spot.
//			if (settlementActivitySpot.isClose(target)) {
//				result = true;
//			}
//		}
//
//		return result;
	}

	/**
	 * Checks if this building function has any activity spots.
	 *
	 * @return true if building function has activity spots.
	 */
	public boolean hasActivitySpots() {
		return !activitySpotMap.isEmpty() && !activitySpotMap.keySet().isEmpty();
	}

	/**
	 * Gets a list of activity spots.
	 * 
	 * @return
	 */
	public List<LocalPosition> getActivitySpotsList() {
		return new ArrayList<>(activitySpotMap.keySet());
	}

	/**
	 * Gets the number of currently empty activity spots.
	 *
	 * @return
	 */
	public int getNumEmptyActivitySpots() {
		if (activitySpotMap.isEmpty())
			return 0;
		
		int empty = 0;
		
		for (int id: activitySpotMap.values()) {
			if (id == -1) {
				empty++;
			}
		}
	
//		if (activitySpots != null && !activitySpots.isEmpty()) {
//			for (LocalPosition s: activitySpots) {
//				if (isActivitySpotEmpty(s))
//					empty++;
//			}
//		}
		
		return empty;
	}

	/**
	 * Checks if an empty activity spot is available.
	 *
	 * @return
	 */
	public boolean hasEmptyActivitySpot() {
		
		if (activitySpotMap.isEmpty())
			return false;
		
		for (int id: activitySpotMap.values()) {
			if (id == -1) {
				return true;
			}
		}
				
//		if (activitySpots != null && !activitySpots.isEmpty()) {
//			for (LocalPosition s: activitySpots) {
//				if (isActivitySpotEmpty(s))
//					return true;
//			}
//		}
		
		return false;
	}

	/**
	 * Gets the number of currently occupied activity spots.
	 *
	 * @return
	 */
	public int getNumOccupiedActivitySpots() {
		
		if (activitySpotMap.isEmpty())
			return 0;
		
		int occupied = 0;
		
		for (int id: activitySpotMap.values()) {
			if (id != -1) {
				occupied++;
			}
		}
		
//		if (activitySpots != null && !activitySpots.isEmpty()) {
//			for (LocalPosition s: activitySpots) {
//				if (!isActivitySpotEmpty(s))
//					occupied++;
//			}
//		}
		return occupied;

	}


	/**
	 * Retrieves a resource from settlement.
	 * 
	 * @param amount
	 * @param resource
	 * @param value
	 * @return
	 */
	protected boolean retrieve(double amount, int resource, boolean value) {
		return Storage.retrieveAnResource(amount, resource, building.getSettlement(), value);
	}

	/**
	 * Stores a resource to settlement.
	 * 
	 * @param amount
	 * @param resource
	 * @param source
	 */
	protected void store(double amount, int resource, String source) {
		Storage.storeAnResource(amount, resource, building.getSettlement(), source);
	}

	/**
	 * Reloads instances after loading from a saved sim.
	 *
	 * @param bc {@link BuildingConfig}
	 * @param c0 {@link MasterClock}
	 * @param pc {@link PersonConfig}
	 */
	public static void initializeInstances(BuildingConfig bc, MasterClock c1, PersonConfig pc, CropConfig cc,
			SurfaceFeatures sf, Weather w, UnitManager u) {
		masterClock = c1;
		personConfig = pc;
		buildingConfig = bc;
		cropConfig = cc;
		weather = w;
		surface = sf;
		unitManager = u;
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		type = null;
		building = null;
		activitySpotMap.clear();
		activitySpotMap = null;
	}
}

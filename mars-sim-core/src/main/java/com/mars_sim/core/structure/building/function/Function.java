/*
 * Mars Simulation Project
 * Function.java
 * @date 2023-11-20
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
//	private Map<LocalPosition, Integer> activitySpotMap = new HashMap<>();
	
	/** A list of predefined activity spots. */
	private List<LocalPosition> setSpots = new ArrayList<>();
	
	/** A list of occupied activity spots. */
	private List<ActivitySpot> occupiedSpots = new ArrayList<>();
	
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
			setSpots = spec.getActivitySpots();
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
	 * Returns a set of ids in the occupied list.
	 * 
	 * @return
	 */
	public Set<Integer> getOccupiedID() {
		Set<Integer> occupied = new HashSet<>();
		for (ActivitySpot as: occupiedSpots) {
			occupied.add(as.getID());
		}
		return occupied;
	}
	
	/**
	 * Adds the entry into the occupied list.
	 * 
	 * @param p
	 * @param id
	 * @return
	 */
	public boolean addActivitySpot(LocalPosition p, int id) {
		ActivitySpot as = new ActivitySpot(p, id);
		return addActivitySpot(as);
	}


	/**
	 * Adds an entry into the occupied list.
	 * 
	 * @param spot
	 * @return
	 */
	public boolean addActivitySpot(ActivitySpot spot) {
		if (!occupiedSpots.contains(spot)) {
			occupiedSpots.add(spot);
			return true;
		}
		return false;
	}
	
	
	/**
	 * Removes the entry of the person with given id from the occupied list.
	 * 
	 * @param p
	 * @param id
	 * @return
	 */
	public boolean removeFromActivitySpot(int id) {
		Iterator<ActivitySpot> i = occupiedSpots.iterator();
		while (i.hasNext()) {
			ActivitySpot as = i.next();
			if (as.getID() == id) {
				i.remove();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets an empty local activity spot.
	 *
	 * @return
	 */
	public LocalPosition getAvailableActivitySpot() {
		Set<LocalPosition> occupied = new HashSet<>();
		for (ActivitySpot as: occupiedSpots) {
			occupied.add(as.getPos());
		}
		
		Set<LocalPosition> existing = new HashSet<>(setSpots);
		
		existing.removeAll(occupied);

		if (existing.size() > 0) {
			return new ArrayList<>(existing).get(0);
		}
		
		return null;
	}

	/**
	 * Checks if an activity spot is empty/unoccupied.
	 *
	 * @param pos {@link LocalPosition}
	 * @return true if this activity spot is empty.
	 */
	public boolean isActivitySpotEmpty(LocalPosition pos) {
		for (ActivitySpot s: occupiedSpots) {
			if (s.getPos().equals(pos))
				return false;
		}

		return true;
	}

	/**
	 * Checks if a worker is at an activity spot for this building function.
	 *
	 * @param worker the Worker.
	 * @return true if the worker's Position is currently at an activity spot.
	 */
	public boolean isAtActivitySpot(Worker worker) {
		for (ActivitySpot s: occupiedSpots) {
			if (s.getID() == worker.getIdentifier())
				return true;
		}

		return false;
	}

	/**
	 * Checks if this building function has predefined activity spots.
	 *
	 * @return true if building function has predefined activity spots.
	 */
	public boolean hasActivitySpots() {
		return setSpots.size() > 0;
	}

	/** 
	 * Returns a list of occupied activity spots. 
	 */
	public List<ActivitySpot> getOccupiedSpots() {
		return occupiedSpots;
	}
	
	/**
	 * Gets a list of predefined activity spots.
	 * 
	 * @return
	 */
	public List<LocalPosition> getActivitySpotsList() {
		return setSpots;
	}

	/**
	 * Gets the number of currently empty activity spots.
	 *
	 * @return
	 */
	public int getNumEmptyActivitySpots() {
		return setSpots.size() - occupiedSpots.size();
	}

	/**
	 * Checks if an empty activity spot is available.
	 *
	 * @return
	 */
	public boolean hasEmptyActivitySpot() {
		return getNumEmptyActivitySpots() > 0;
	}

	/**
	 * Gets the number of currently occupied activity spots.
	 *
	 * @return
	 */
	public int getNumOccupiedActivitySpots() {
		return occupiedSpots.size();
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
		occupiedSpots.clear();
		occupiedSpots = null;
		setSpots.clear();
		setSpots = null;
	}
}

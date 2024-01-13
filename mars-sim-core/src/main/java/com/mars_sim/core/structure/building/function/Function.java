/*
 * Mars Simulation Project
 * Function.java
 * @date 2023-11-20
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.LocalAreaUtil;
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
import com.mars_sim.core.structure.building.NamedPosition;
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

	// The default inspection work time in millisols for a function 
	protected static final int BASE_MAINT_TIME = 5;
	
	protected static final int WATER_ID = ResourceUtil.waterID;
	protected static final int BLACK_WATER_ID = ResourceUtil.blackWaterID;
	protected static final int GREY_WATER_ID = ResourceUtil. greyWaterID;
	protected static final int TOILET_TISSUE_ID = ResourceUtil.toiletTissueID;
	protected static final int TOXIC_WASTE_ID = ResourceUtil.toxicWasteID;

	private long lastPulse = 0; // First initial pulse is always 1

	private FunctionType type;
	
	protected Building building;

	/** A list of predefined activity spots. */
	private Set<ActivitySpot> spots = new HashSet<>();
	
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

		// A FunctionSpec should ALWAYS be present. root cause is the UnitTests
		// load any activity spots
		if (spec != null) {
			spots = createActivitySpot(spec.getActivitySpots(), building);
		}
		else {
			spots = Collections.emptySet();
		}
	}

	/**
	 * Create a set of Activity Spots from a set of LocalPositions. The Activity spots are created
	 * into the global Settlement coordinate frame.
	 * 
	 * @param set Source positions
	 * @param owner     Building that owns this function, provide reference point.

	 */
	private static Set<ActivitySpot> createActivitySpot(Set<NamedPosition> set, Building owner) {
		return set.stream()
							.map(p -> new ActivitySpot(p.name(),
											LocalAreaUtil.convert2SettlementPos(p.position(), owner)))
							.collect(Collectors.toSet());
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
	 * A worker claims an activity spot at a position within the Function.
	 * 
	 * @param p Position being claimed
	 * @param w Worker claiming
	 * @return Was an activity spot found?
	 */
	public boolean claimActivitySpot(LocalPosition p, Worker w) {
		var as = findActivitySpot(p);
		if (as == null) {
			throw new IllegalArgumentException("No activity spot " + p.getShortFormat());
		}

		// Check if the worker already has already claimed the spot previously
		if (as.possessTheSpot(w.getIdentifier())) {
			return true;
		}
		
		// Spot is claimed but only temporarily
		var allocated = as.claim(w, false, building);
		if (allocated != null) {
			w.setActivitySpot(allocated);
			return true;
		}
		return false;
	}

	/**
	 * Finds an activity spot via its position.
	 * 
	 * @param p Position to search
	 * @return Matched activity
	 */
	public ActivitySpot findActivitySpot(LocalPosition p) {
		for (var a : spots) {
			if (a.getPos().equals(p)) {
				return a;
			}
		}
		return null;
	}

	/**
	 * Gets an empty available local activity spot.
	 *
	 * @return
	 */
	public LocalPosition getAvailableActivitySpot() {
		var f = spots.stream()
							.filter(ActivitySpot::isEmpty)
							.map(ActivitySpot::getPos)
							.findFirst();
		return f.orElse(null);
	}

	/**
	 * Returns if a worker has already occupied an activity spot.
	 *
	 * @return
	 */
	public boolean checkWorkerActivitySpot(Worker worker) {
		for (ActivitySpot s: spots) {
			if (s.getID() == worker.getIdentifier()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if an activity spot is empty/unoccupied.
	 *
	 * @param pos {@link LocalPosition}
	 * @return true if this activity spot is empty.
	 */
	public boolean isActivitySpotEmpty(LocalPosition pos) {
		ActivitySpot a = findActivitySpot(pos);
		if (a == null)
			return false;
		return a.isEmpty();
	}

	/**
	 * Gets a set of predefined activity spots.
	 * 
	 * @return
	 */
	public Set<ActivitySpot> getActivitySpots() {
		return spots;
	}

	/**
	 * Gets the number of currently empty activity spots.
	 *
	 * @return
	 */
	public int getNumEmptyActivitySpots() {
		return (int) spots.stream()
					.filter(ActivitySpot::isEmpty)
					.count();
	}

	/**
	 * Checks if an empty unoccupied activity spot is available.
	 *
	 * @return
	 */
	public boolean hasEmptyActivitySpot() {
		return spots.stream().anyMatch(ActivitySpot::isEmpty);
	}

	/**
	 * Gets the number of currently occupied activity spots.
	 *
	 * @return
	 */
	public int getNumOccupiedActivitySpots() {
		return spots.size() - getNumEmptyActivitySpots();
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
		spots.clear();
		spots = null;
	}
}

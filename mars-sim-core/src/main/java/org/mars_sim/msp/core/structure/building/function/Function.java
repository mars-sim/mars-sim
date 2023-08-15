/*
 * Mars Simulation Project
 * Function.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mars.sim.mapdata.location.LocalPosition;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.environment.Weather;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.structure.building.function.farming.CropConfig;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.Temporal;

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

	private FunctionType type;
	protected Building building;
	private List<LocalPosition> activitySpots;

	private long lastPulse = 0; // First initial pulse is always 1

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
			activitySpots = spec.getActivitySpots();
		}
		else {
			activitySpots = Collections.emptyList();
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
	 * Gets an available activity spot for the worker.
	 *
	 * @param worker the worker looking for the activity spot.
	 * @return activity spot as {@link Point2D} or null if none found.
	 */
	public LocalPosition getAvailableActivitySpot() {

		LocalPosition result = null;

		if (activitySpots != null) {

			List<LocalPosition> availableActivitySpots = new ArrayList<>();
			Iterator<LocalPosition> i = activitySpots.iterator();
			while (i.hasNext()) {
				LocalPosition buildingLoc = i.next();
				// Check if spot is unoccupied.
				boolean available = isActivitySpotEmpty(buildingLoc);

				// If available, add activity spot to available list.
				if (available) {
					// Convert activity spot from building local to settlement local.
					LocalPosition settlementActivitySpot = LocalAreaUtil.getLocalRelativePosition(buildingLoc, getBuilding());
					availableActivitySpots.add(settlementActivitySpot);
				}
			}

			if (!availableActivitySpots.isEmpty()) {

				// Choose a random available activity spot.
				int index = RandomUtil.getRandomInt(availableActivitySpots.size() - 1);
				result = availableActivitySpots.get(index);
			}
		}

		return result;
	}

	/**
	 * Checks if an activity spot is empty/unoccupied.
	 *
	 * @param s as a {@link Point2D}
	 * @return true if this activity spot is empty.
	 */
	public boolean isActivitySpotEmpty(LocalPosition s) {
		if (activitySpots == null || activitySpots.isEmpty())
			return true;

		boolean result = false;

		// Convert activity spot from building local to settlement local.
		Building b = getBuilding();
		LocalPosition settlementActivitySpot = LocalAreaUtil.getLocalRelativePosition(s, b);

		for (Person person : b.getInhabitants()) {
			// Check if person's location is identical or very very close (1e-5 meters) to
			// activity spot.
			if (person.isInSettlement() && !settlementActivitySpot.isClose(person.getPosition())) {
				return true;
			}
		}

		for (Robot robot : b.getRobots()) {
			// Check if robot location is identical or very very close (1e-5 meters) to
			// activity spot.
			if (!settlementActivitySpot.isClose(robot.getPosition())) {
				return true;
			}
		}

		return result;
	}

	/**
	 * Checks if a worker is at an activity spot for this building function.
	 *
	 * @param worker the Worker.
	 * @return true if the worker's Position is currently at an activity spot.
	 */
	public boolean isAtActivitySpot(Worker worker) {
		LocalPosition target = worker.getPosition();
		boolean result = false;

		Iterator<LocalPosition> i = activitySpots.iterator();
		while (i.hasNext() && !result) {
			LocalPosition activitySpot = i.next();
			// Convert activity spot from building local to settlement local.
			LocalPosition settlementActivitySpot = LocalAreaUtil.getLocalRelativePosition(activitySpot, getBuilding());

			// Check if location is very close to activity spot.
			if (settlementActivitySpot.isClose(target)) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * Checks if this building function has any activity spots.
	 *
	 * @return true if building function has activity spots.
	 */
	public boolean hasActivitySpots() {
		return !activitySpots.isEmpty();
	}

	public List<LocalPosition> getActivitySpotsList() {
		return activitySpots;
	}

	/**
	 * Gets the number of currently empty activity spots.
	 *
	 * @return
	 */
	public int getNumEmptyActivitySpots() {
		int empty = 0;
		if (activitySpots != null && !activitySpots.isEmpty()) {
			for (LocalPosition s: activitySpots) {
				if (isActivitySpotEmpty(s))
					empty++;
			}
		}
		return empty;
	}

	/**
	 * Checks if an empty activity spot is available.
	 *
	 * @return
	 */
	public boolean hasEmptyActivitySpot() {
		if (activitySpots != null && !activitySpots.isEmpty()) {
			for (LocalPosition s: activitySpots) {
				if (isActivitySpotEmpty(s))
					return true;
			}
		}
		return false;
	}

	/**
	 * Gets the number of currently occupied activity spots.
	 *
	 * @return
	 */
	public int getNumOccupiedActivitySpots() {
		int occupied = 0;
		if (activitySpots != null && !activitySpots.isEmpty()) {
			for (LocalPosition s: activitySpots) {
				if (!isActivitySpotEmpty(s))
					occupied++;
			}
		}
		return occupied;

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
		activitySpots = null;
	}


	protected boolean retrieve(double amount, int resource, boolean value) {
		return Storage.retrieveAnResource(amount, resource, building.getSettlement(), value);
	}


	protected void store(double amount, int resource, String source) {
		Storage.storeAnResource(amount, resource, building.getSettlement(), source);
	}
}

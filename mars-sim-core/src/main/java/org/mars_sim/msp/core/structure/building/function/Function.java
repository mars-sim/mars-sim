/**
 * Mars Simulation Project
 * Function.java
 * @version 3.1.0 2017-09-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * A settlement building function.
 */
public abstract class Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private FunctionType type;
	protected Building building;
	private List<Point2D> activitySpots;
	private List<Point2D> bedLocations;

//	private static List<FunctionType> buildingFunctions;
	
	protected static BuildingConfig buildingConfig;
	protected static MasterClock masterClock;
	protected static MarsClock marsClock;
	protected static PersonConfig personConfig;
	protected static Weather weather;
	protected static SurfaceFeatures surface;
	protected static Mars mars;
	protected static UnitManager unitManager;

	/**
	 * Constructor.
	 * 
	 * @param type {@link FunctionType}
	 * @param      {@link Building}
	 */
	public Function(FunctionType type, Building building) {
		this.type = type;
		this.building = building;
		
		Simulation sim = Simulation.instance();
		buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
		masterClock = sim.getMasterClock();
		marsClock = masterClock.getMarsClock();
		personConfig = SimulationConfig.instance().getPersonConfig();
		mars = sim.getMars();
		weather = mars.getWeather();
		surface = mars.getSurfaceFeatures();
		unitManager = sim.getUnitManager();

	}

//	public void createFunctionTypeMap() {
//		buildingFunctions  = new ArrayList<>();
//	}
	
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
	 * @return maintenance work time (millisols).
	 */
	public abstract double getMaintenanceTime();

	/**
	 * Gets the function's malfunction scope strings.
	 * 
	 * @return array of scope strings.
	 */
	public String[] getMalfunctionScopeStrings() {
		String[] result = { type.getName() };
		return result;
	}

	/**
	 * Time passing for the building.
	 * 
	 * @param time amount of time passing (in millisols)
	 */
	public abstract void timePassing(double time);

	/**
	 * Gets the amount of heat required when function is at full heat.
	 * 
	 * @return heat (kW)
	 */
	public abstract double getFullHeatRequired();

	/**
	 * Gets the amount of heat required when function is at heat down level.
	 * 
	 * @return heat (kW)
	 */
	public abstract double getPoweredDownHeatRequired();

	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	public abstract double getFullPowerRequired();

	/**
	 * Gets the amount of power required when function is at power down level.
	 * 
	 * @return power (kW)
	 */
	public abstract double getPoweredDownPowerRequired();

	/**
	 * Perform any actions needed when removing this building function from the
	 * settlement.
	 */
	public void removeFromSettlement() {
		// Override as needed.
	}

	/**
	 * Loads activity spots into the building function.
	 * 
	 * @param newActivitySpots activity spots to add.
	 */
	protected void loadActivitySpots(Collection<Point2D> newActivitySpots) {

		if (activitySpots == null) {
			activitySpots = new ArrayList<Point2D>(newActivitySpots);
		} else {
			activitySpots.addAll(newActivitySpots);
		}
	}

	/**
	 * Loads bed locations into the building function.
	 * 
	 * @param newBedLocations bed locations to add.
	 */
	protected void loadBedLocations(Collection<Point2D> newBedLocations) {

		if (bedLocations == null) {
			bedLocations = new ArrayList<Point2D>(newBedLocations);
		} else {
			bedLocations.addAll(newBedLocations);
		}
	}
	
	/**
	 * Gets an available activity spot for the person.
	 * 
	 * @param person the person looking for the activity spot.
	 * @return activity spot as {@link Point2D} or null if none found.
	 */
	public Point2D getAvailableActivitySpot(Person person) {

		Point2D result = null;

		if (activitySpots != null) {

			List<Point2D> availableActivitySpots = new ArrayList<Point2D>();
			Iterator<Point2D> i = activitySpots.iterator();
			while (i.hasNext()) {
				Point2D activitySpot = i.next();
				// Convert activity spot from building local to settlement local.
				Point2D settlementActivitySpot = LocalAreaUtil.getLocalRelativeLocation(activitySpot.getX(),
						activitySpot.getY(), getBuilding());

				// Check if spot is unoccupied.
				boolean available = true;
				Settlement settlement = getBuilding().getSettlement();
				Iterator<Person> j = settlement.getIndoorPeople().iterator();
				while (j.hasNext() && available) {
					Person tempPerson = j.next();
					if (!tempPerson.equals(person)) {

						// Check if person's location is very close to activity spot.
						Point2D personLoc = new Point2D.Double(tempPerson.getXLocation(), tempPerson.getYLocation());
						if (LocalAreaUtil.areLocationsClose(settlementActivitySpot, personLoc)) {
							available = false;
						}
					}
				}

				// If available, add activity spot to available list.
				if (available) {
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
	 * Gets an available activity spot for the robot.
	 * 
	 * @param robot the bot looking for the activity spot.
	 * @return activity spot as {@link Point2D} or null if none found.
	 */
	public Point2D getAvailableActivitySpot(Robot robot) {

		Point2D result = null;

		if (activitySpots != null) {

			if (isAtActivitySpot(robot)) {
				result = new Point2D.Double(robot.getXLocation(), robot.getYLocation());
			} else {
				List<Point2D> availableActivitySpots = new ArrayList<Point2D>();
				Iterator<Point2D> i = activitySpots.iterator();
				while (i.hasNext()) {
					Point2D activitySpot = i.next();
					// Convert activity spot from building local to settlement local.
					Point2D settlementActivitySpot = LocalAreaUtil.getLocalRelativeLocation(activitySpot.getX(),
							activitySpot.getY(), getBuilding());

					// Check if spot is unoccupied.
					boolean available = true;
					Settlement settlement = getBuilding().getSettlement();
					Iterator<Robot> j = settlement.getRobots().iterator();
					while (j.hasNext() && available) {
						Robot tempRobot = j.next();
						if (!tempRobot.equals(robot)) {

							// Check if robot's location is very close to activity spot.
							Point2D robotLoc = new Point2D.Double(tempRobot.getXLocation(), tempRobot.getYLocation());
							if (LocalAreaUtil.areLocationsClose(settlementActivitySpot, robotLoc)) {
								available = false;
							}
						}
					}

					// If available, add activity spot to available list.
					if (available) {
						availableActivitySpots.add(settlementActivitySpot);
					}
				}

				if (!availableActivitySpots.isEmpty()) {

					// Choose a random available activity spot.
					int index = RandomUtil.getRandomInt(availableActivitySpots.size() - 1);
					result = availableActivitySpots.get(index);
				}
			}
		}

		return result;
	}

	/**
	 * Checks if an activity spot is empty/unoccupied
	 * 
	 * @param spot as a {@link Point2D}
	 * @return true if this activity spot is empty.
	 */
	public boolean isActivitySpotEmpty(Point2D spot) {
		boolean result = false;

		if (activitySpots.isEmpty())
			return true;

		else {
			Iterator<Point2D> i = activitySpots.iterator();
			while (i.hasNext()) {
				Point2D activitySpot = i.next();

				if (activitySpot == spot) {
					// Convert activity spot from building local to settlement local.
					Building b = getBuilding();
					Point2D settlementActivitySpot = LocalAreaUtil.getLocalRelativeLocation(activitySpot.getX(),
							activitySpot.getY(), b);

					for (Person person : b.getInhabitants()) {
						// Check if person's location is identical or very very close (1e-5 meters) to
						// activity spot.
						Point2D personLoc = new Point2D.Double(person.getXLocation(), person.getYLocation());
						if (LocalAreaUtil.areLocationsClose(settlementActivitySpot, personLoc)) {
							result = false;
						} else {
							result = true;
							break;
						}
					}
				}
			}

			return result;
		}
	}

	/**
	 * Checks if a person is at an activity for this building function.
	 * 
	 * @param person the person.
	 * @return true if the person is currently at an activity spot.
	 */
	public boolean isAtActivitySpot(Person person) {
		boolean result = false;

		Iterator<Point2D> i = activitySpots.iterator();
		while (i.hasNext() && !result) {
			Point2D activitySpot = i.next();
			// Convert activity spot from building local to settlement local.
			Point2D settlementActivitySpot = LocalAreaUtil.getLocalRelativeLocation(activitySpot.getX(),
					activitySpot.getY(), getBuilding());

			// Check if person's location is very close to activity spot.
			Point2D personLoc = new Point2D.Double(person.getXLocation(), person.getYLocation());
			if (LocalAreaUtil.areLocationsClose(settlementActivitySpot, personLoc)) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * Checks if a robot is at an activity for this building function.
	 * 
	 * @param robot the robot.
	 * @return true if the robot is currently at an activity spot.
	 */
	public boolean isAtActivitySpot(Robot robot) {
		boolean result = false;

		Iterator<Point2D> i = activitySpots.iterator();
		while (i.hasNext() && !result) {
			Point2D activitySpot = i.next();
			// Convert activity spot from building local to settlement local.
			Point2D settlementActivitySpot = LocalAreaUtil.getLocalRelativeLocation(activitySpot.getX(),
					activitySpot.getY(), getBuilding());

			// Check if robot's location is very close to activity spot.
			Point2D robotLoc = new Point2D.Double(robot.getXLocation(), robot.getYLocation());
			if (LocalAreaUtil.areLocationsClose(settlementActivitySpot, robotLoc)) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * Check if this building function has any activity spots.
	 * 
	 * @return true if building function has activity spots.
	 */
	public boolean hasActivitySpots() {
		return (activitySpots.size() > 0);
	}

	public List<Point2D> getActivitySpotsList() {
		return activitySpots;
	}

	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param bc {@link BuildingConfig}
	 * @param c0 {@link MasterClock}
	 * @param c1 {@link MarsClock}
	 * @param pc {@link PersonConfig}
	 */
	public static void initializeInstances(BuildingConfig bc, MasterClock c0, MarsClock c1, PersonConfig pc,
			Mars m, SurfaceFeatures sf, Weather w, UnitManager u) {
		masterClock = c0;
		marsClock = c1;
		personConfig = pc;
		buildingConfig = bc;
		mars = m;
		weather = w;
		surface = sf;
		unitManager = u;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		type = null;
		building = null;
		if (activitySpots != null) {
			activitySpots.clear();
			activitySpots = null;
		}
	}
}
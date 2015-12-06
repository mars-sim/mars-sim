/**
 * Mars Simulation Project
 * Sleep.java
 * @version 3.08 2015-05-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.RoboticStation;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The Sleep class is a task for sleeping.
 * The duration of the task is by default chosen randomly, between 250 - 330 millisols.
 * Note: Sleeping reduces fatigue and stress.
 */
public class Sleep extends Task implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(Sleep.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.sleep"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase SLEEPING = new TaskPhase(Msg.getString(
            "Task.phase.sleeping")); //$NON-NLS-1$

    /** Task name for robot */
    private static final String SLEEP_MODE = Msg.getString(
            "Task.description.sleepMode"); //$NON-NLS-1$

    /** Task phases for robot. */
    private static final TaskPhase SLEEP_MODE_PHASE = new TaskPhase(Msg.getString(
            "Task.phase.sleepMode")); //$NON-NLS-1$


    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -.3D;
    /** The base alarm time (millisols) at 0 degrees longitude. */
    private static final double BASE_ALARM_TIME = 300D;

    // Data members
    /** The previous time (millisols). */
    private double previousTime;

    private double timeFactor;

    /** The living accommodations if any. */
    private LivingAccommodations accommodations;

    private RoboticStation station;
    private MarsClock clock;


    /**
     * Constructor.
     * @param person the person to perform the task
     */
    public Sleep(Person person) {
        super(NAME, person, false, false, STRESS_MODIFIER, true,
                (250D + RandomUtil.getRandomDouble(80D)));

        boolean walkSite = false;

        timeFactor = 3D; // TODO: should vary this factor by person

        // If person is in a settlement, try to find a living accommodations building.
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            Building quarters = getAvailableLivingQuartersBuilding(person);
            if (quarters != null) {
                // Walk to quarters.
                walkToActivitySpotInBuilding(quarters, true);
                accommodations = (LivingAccommodations) quarters.getFunction(
                        BuildingFunction.LIVING_ACCOMODATIONS);
                accommodations.addSleeper();
                walkSite = true;
            }
        }

        if (!walkSite) {

            if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
                // If person is in rover, walk to passenger activity spot.
                if (person.getVehicle() instanceof Rover) {
                    walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
                }
            }
            else {
                // Walk to random location.
                walkToRandomLocation(true);
            }
        }


        previousTime = Simulation.instance().getMasterClock().getMarsClock().getMillisol();

        // Initialize phase
        addPhase(SLEEPING);
        setPhase(SLEEPING);
    }

    public Sleep(Robot robot) {
        super(SLEEP_MODE, robot, false, false, STRESS_MODIFIER, true, 10D);

        // If robot is in a settlement, try to find a living accommodations building.
        if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

        	// TODO: if power is below a certain threshold, go to robotic station for recharge, else stay at the same place

            // If currently in a building with a robotic station, go to a station activity spot.
            boolean atStation = false;
            Building currentBuilding = BuildingManager.getBuilding(robot);
            if (currentBuilding != null) {
                if (currentBuilding.hasFunction(BuildingFunction.ROBOTIC_STATION)) {
                    RoboticStation currentStation = (RoboticStation) currentBuilding.getFunction(BuildingFunction.ROBOTIC_STATION);
                    if (currentStation.getSleepers() < currentStation.getSlots()) {
                        atStation = true;
                        station = currentStation;
                        station.addSleeper();

                        // Check if rover is currently at an activity spot for the robotic station.
                        if (currentStation.hasActivitySpots() && !currentStation.isAtActivitySpot(robot)) {
                            // Walk to an available activity spot.
                            walkToActivitySpotInBuilding(currentBuilding, true);
                        }
                    }
                }
            }

            if (!atStation) {
                Building building = getAvailableRoboticStationBuilding(robot);
                if (building != null) {
                    //System.out.println("building.toString() is " + building.toString() );
                    walkToActivitySpotInBuilding(building, false);
                    station = (RoboticStation) building.getFunction(BuildingFunction.ROBOTIC_STATION);
                    station.addSleeper();
                }
            }
        }
        if (clock == null)
        	clock = Simulation.instance().getMasterClock().getMarsClock();
        previousTime = clock.getMillisol();

        // Initialize phase
        addPhase(SLEEP_MODE_PHASE);
        setPhase(SLEEP_MODE_PHASE);
    }

    @Override
    protected BuildingFunction getRelatedBuildingFunction() {
        return BuildingFunction.LIVING_ACCOMODATIONS;
    }

    protected BuildingFunction getRelatedBuildingRoboticFunction() {
        return BuildingFunction.ROBOTIC_STATION;
    }

    @Override
    protected double performMappedPhase(double time) {
        if (person != null) {
	    	if (getPhase() == null)
	            throw new IllegalArgumentException("Task phase is null");
	    	else if (SLEEPING.equals(getPhase()))
	        	return sleepingPhase(time);
	        else
	            return time;
        }

        else if (robot != null) {
	    	if (getPhase() == null)
	            throw new IllegalArgumentException("Task phase is null");
	    	else if (SLEEP_MODE_PHASE.equals(getPhase()))
	            return sleepingPhase(time);
	        else
	            return time;
        }
		return time;
    }

    /**
     * Performs the sleeping phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double sleepingPhase(double time) {

		if (person != null) {
	        // Reduce person's fatigue
	        double newFatigue = person.getPhysicalCondition().getFatigue() - (timeFactor * time);
	        if (newFatigue < 0D) {
	            newFatigue = 0D;
	        }
	        person.getPhysicalCondition().setFatigue(newFatigue);

	        // Check if alarm went off
	        if (clock == null)
	        	clock = Simulation.instance().getMasterClock().getMarsClock();
	        double newTime = clock.getMillisol();
	        double alarmTime = getAlarmTime();

	        if ((previousTime <= alarmTime) && (newTime >= alarmTime)) {
	            endTask();
	            logger.finest(person.getName() + " woke up from alarm.");
	        }
	        else {
	            previousTime = newTime;
	        }

		}
		else if (robot != null) {
			// Check if alarm went off.
	        double newTime = Simulation.instance().getMasterClock().getMarsClock().getMillisol();
	        double alarmTime = getAlarmTime();
	        if ((previousTime <= alarmTime) && (newTime >= alarmTime)) {
	            endTask();
	            logger.finest(robot.getName() + " woke up from alarm.");
	        }
	        else {
	            previousTime = newTime;
	        }
		}

        return 0D;
    }

    @Override
    protected void addExperience(double time) {
        // This task adds no experience.
    }

    @Override
    public void endTask() {
        super.endTask();

		if (person != null) {
	        // Remove person from living accommodations bed so others can use it.
	        if (accommodations != null && accommodations.getSleepers() > 0) {
	            accommodations.removeSleeper();
	        }
		}
		else if (robot != null) {
	        // Remove robot from stations so other robots can use it.
	        if (station != null && station.getSleepers() > 0) {
	        	station.removeSleeper();
	        	walkToAssignedDutyLocation(robot, true);
	        }
		}


    }

    /**
     * Gets an available living accommodations building that the person can use.
     * Returns null if no living accommodations building is currently available.
     * @param person the person
     * @return available living accommodations building
     */
    public static Building getAvailableLivingQuartersBuilding(Person person) {

        Building result = null;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            BuildingManager manager = person.getSettlement().getBuildingManager();
            List<Building> quartersBuildings = manager.getBuildings(BuildingFunction.LIVING_ACCOMODATIONS);
            quartersBuildings = BuildingManager.getNonMalfunctioningBuildings(quartersBuildings);
            quartersBuildings = getQuartersWithEmptyBeds(quartersBuildings);
            quartersBuildings = BuildingManager.getLeastCrowdedBuildings(quartersBuildings);

            if (quartersBuildings.size() > 0) {
                Map<Building, Double> quartersBuildingProbs = BuildingManager.getBestRelationshipBuildings(
                        person, quartersBuildings);
                result = RandomUtil.getWeightedRandomObject(quartersBuildingProbs);
            }
        }

        return result;
    }

    public static Building getAvailableRoboticStationBuilding(Robot robot) {

        Building result = null;

        if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            BuildingManager manager = robot.getSettlement().getBuildingManager();
            List<Building> buildings = manager.getBuildings(BuildingFunction.ROBOTIC_STATION);
            buildings = BuildingManager.getNonMalfunctioningBuildings(buildings);
            buildings = getRoboticStationsWithEmptySlots(buildings);
            buildings = BuildingManager.getLeastCrowdedBuildings(buildings);
            int size = buildings.size();
            //System.out.println("size is "+size);
            int selected = 0;
            if (size == 0)
            	result = null;
            if (size >= 1) {
            	selected = RandomUtil.getRandomInt(size-1);
            	result = buildings.get(selected);
            }
            //System.out.println("selected is "+selected);
            //if (quartersBuildings.size() > 0) {
             //   Map<Building, Double> quartersBuildingProbs = BuildingManager.getBestRelationshipBuildings(
            //            robot, quartersBuildings);
           //     result = RandomUtil.getWeightedRandomObject(quartersBuildingProbs);
            //}
        }

        return result;
    }

    /**
     * Gets living accommodations with empty beds from a list of buildings with the living accommodations function.
     * @param buildingList list of buildings with the living accommodations function.
     * @return list of buildings with empty beds.
     */
    private static List<Building> getQuartersWithEmptyBeds(List<Building> buildingList) {
        List<Building> result = new ArrayList<Building>();

        Iterator<Building> i = buildingList.iterator();
        while (i.hasNext()) {
            Building building = i.next();
            LivingAccommodations quarters = (LivingAccommodations) building.getFunction(BuildingFunction.LIVING_ACCOMODATIONS);
            if (quarters.getSleepers() < quarters.getBeds()) {
                result.add(building);
            }
        }

        return result;
    }

    private static List<Building> getRoboticStationsWithEmptySlots(List<Building> buildingList) {
        List<Building> result = new ArrayList<Building>();

        Iterator<Building> i = buildingList.iterator();
        while (i.hasNext()) {
            Building building = i.next();
            RoboticStation station = (RoboticStation) building.getFunction(BuildingFunction.ROBOTIC_STATION);
            if (station.getSleepers() < station.getSlots()) {
                result.add(building);
            }
        }

        return result;
    }

    /**
     * Gets the wakeup alarm time for the person's longitude.
     * @return alarm time in millisols.
     */
    private double getAlarmTime() {
    	double timeDiff = 0;
		double modifiedAlarmTime = 0;
		if (person != null) {

			ShiftType shiftType = person.getTaskSchedule().getShiftType();
			// Set to 50 millisols prior to the beginning of the duty shift hour
			if (shiftType.equals(ShiftType.A))
				modifiedAlarmTime = 950;
			else if (shiftType.equals(ShiftType.B))
				modifiedAlarmTime = 450;
			else if (shiftType.equals(ShiftType.X))
				modifiedAlarmTime = 950;
			else if (shiftType.equals(ShiftType.Y))
				modifiedAlarmTime = 283;
			else if (shiftType.equals(ShiftType.Z))
				modifiedAlarmTime = 616;
			else if (shiftType.equals(ShiftType.ON_CALL)) { // if only one person is at the settlement, go with this schedule
				timeDiff = 1000D * (person.getCoordinates().getTheta() / (2D * Math.PI));
				modifiedAlarmTime = BASE_ALARM_TIME - timeDiff;
			}

		}
		else if (robot != null) {
        	timeDiff = 1000D * (robot.getCoordinates().getTheta() / (2D * Math.PI));

		}

        if (modifiedAlarmTime < 0D) {
            modifiedAlarmTime += 1000D;
        }
        return modifiedAlarmTime;
    }

    @Override
    public int getEffectiveSkillLevel() {
        return 0;
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(0);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();
        station = null;
        accommodations = null;
    }
}
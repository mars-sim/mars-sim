/**
 * Mars Simulation Project
 * Workout.java
 * @version 3.06 2013-12-07
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;
import org.mars_sim.msp.core.structure.building.function.Exercise;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The Workout class is a task for working out in an exercise facility.
 */
public class Workout extends Task implements Serializable {

    private static Logger logger = Logger.getLogger(Workout.class.getName());

    // Task phase
    private static final String EXERCISING = "Exercising";

    // Static members
    private static final double STRESS_MODIFIER = -1D; // The stress modified per millisol.

    // Data members
    private Exercise gym; // The exercise building the person is using.

    /**
     * Constructor This is an effort-driven task.
     * @param person the person performing the task.
     * @throws Exception if error constructing task.
     */
    public Workout(Person person) {
        // Use Task constructor.
        super("Exercise", person, true, false, STRESS_MODIFIER, true,
                10D + RandomUtil.getRandomDouble(30D));

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {

            // If person is in a settlement, try to find a gym.
            Building gymBuilding = getAvailableGym(person);
            if (gymBuilding != null) {
                // Walk to gym building.
                walkToGymBuilding(gymBuilding);
                gym = (Exercise) gymBuilding.getFunction(Exercise.NAME);
            } else
                endTask();
        } else
            endTask();

        // Initialize phase
        addPhase(EXERCISING);
        setPhase(EXERCISING);
    }

    /**
     * Returns the weighted probability that a person might perform this task. It should return a 0 if there is no
     * chance to perform this task given the person and his/her situation.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {

        double result = 0D;

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            result = condition.getStress() - (condition.getFatigue() / 10D)
                    + 20D;
            if (result < 0D)
                result = 0D;

            // Get an available gym.
            Building building = getAvailableGym(person);
            if (building != null) {
                result *= Task.getCrowdingProbabilityModifier(person, building);
                result *= Task.getRelationshipModifier(person, building);
            } else
                result = 0D;
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        return result;
    }
    
    /**
     * Walk to gym building.
     * @param gymBuilding the gym building.
     */
    private void walkToGymBuilding(Building gymBuilding) {
        
        // Determine location within gym building.
        // TODO: Use action point rather than random internal location.
        Point2D.Double buildingLoc = LocalAreaUtil.getRandomInteriorLocation(gymBuilding);
        Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(buildingLoc.getX(), 
                buildingLoc.getY(), gymBuilding);
        
        // Check if there is a valid interior walking path between buildings.
        BuildingConnectorManager connectorManager = person.getSettlement().getBuildingConnectorManager();
        Building currentBuilding = BuildingManager.getBuilding(person);
        
        if (connectorManager.hasValidPath(currentBuilding, gymBuilding)) {
            Task walkingTask = new WalkInterior(person, gymBuilding, settlementLoc.getX(), 
                    settlementLoc.getY());
            addSubTask(walkingTask);
        }
        else {
            // TODO: Add task for EVA walking to get to gym.
            BuildingManager.addPersonToBuilding(person, gymBuilding, settlementLoc.getX(), 
                    settlementLoc.getY());
        }
    }

    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) {
        if (getPhase() == null)
            throw new IllegalArgumentException("Task phase is null");
        if (EXERCISING.equals(getPhase()))
            return exercisingPhase(time);
        else
            return time;
    }

    /**
     * Performs the exercising phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double exercisingPhase(double time) {

        // Do nothing

        return 0D;
    }

    /**
     * Adds experience to the person's skills used in this task.
     * @param time the amount of time (ms) the person performed this task.
     */
    protected void addExperience(double time) {
        // This task adds no experience.
    }

    /**
     * Ends the task and performs any final actions.
     */
    public void endTask() {
        super.endTask();

        // Remove person from exercise function so others can use it.
        if (gym != null && gym.getNumExercisers() > 0)
            gym.removeExerciser();
    }

    /**
     * Gets an available building with the exercise function.
     * @param person the person looking for the gym.
     * @return an available exercise building or null if none found.
     * @throws BuildingException if error finding gym building.
     */
    private static Building getAvailableGym(Person person) {
        Building result = null;

        // If person is in a settlement, try to find a building with a gym.
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            BuildingManager buildingManager = person.getSettlement()
                    .getBuildingManager();
            List<Building> gyms = buildingManager.getBuildings(Exercise.NAME);
            gyms = BuildingManager.getNonMalfunctioningBuildings(gyms);
            gyms = BuildingManager.getLeastCrowdedBuildings(gyms);
            
            if (gyms.size() > 0) {
                Map<Building, Double> gymProbs = BuildingManager.getBestRelationshipBuildings(
                        person, gyms);
                result = RandomUtil.getWeightedRandomObject(gymProbs);
            }
        }

        return result;
    }

    /**
     * Gets the effective skill level a person has at this task.
     * @return effective skill level
     */
    public int getEffectiveSkillLevel() {
        return 0;
    }

    /**
     * Gets a list of the skills associated with this task. May be empty list if no associated skills.
     * @return list of skills as strings
     */
    public List<String> getAssociatedSkills() {
        List<String> results = new ArrayList<String>(0);
        return results;
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        gym = null;
    }
}
/**
 * Mars Simulation Project
 * Workout.java
 * @version 3.06 2014-02-27
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Exercise;

/**
 * The Workout class is a task for working out in an exercise facility.
 */
public class Workout extends Task implements Serializable {
    
    /** default serial id. */
    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(Workout.class.getName());

    // Task phase
    private static final String EXERCISING = "Exercising";

    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -1D;

    // Data members
    /** The exercise building the person is using. */
    private Exercise gym;

    /**
     * Constructor This is an effort-driven task.
     * @param person the person performing the task.
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
            } 
            else {
                endTask();
            }
        } 
        else {
            endTask();
        }

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
            if (result < 0D) {
                result = 0D;
            }

            // Get an available gym.
            Building building = getAvailableGym(person);
            if (building != null) {
                result *= Task.getCrowdingProbabilityModifier(person, building);
                result *= Task.getRelationshipModifier(person, building);
            } 
            else {
                result = 0D;
            }
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
        
        if (Walk.canWalkAllSteps(person, settlementLoc.getX(), settlementLoc.getY(), 
                gymBuilding)) {
            
            // Add subtask for walking to gym building.
            addSubTask(new Walk(person, settlementLoc.getX(), settlementLoc.getY(), 
                    gymBuilding));
        }
        else {
            logger.fine(person.getName() + " unable to walk to gym building " + 
                    gymBuilding.getName());
            endTask();
        }
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (EXERCISING.equals(getPhase())) {
            return exercisingPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the exercising phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double exercisingPhase(double time) {

        // Do nothing

        return 0D;
    }

    @Override
    protected void addExperience(double time) {
        // This task adds no experience.
    }

    @Override
    public void endTask() {
        super.endTask();

        // Remove person from exercise function so others can use it.
        if (gym != null && gym.getNumExercisers() > 0) {
            gym.removeExerciser();
        }
    }

    /**
     * Gets an available building with the exercise function.
     * @param person the person looking for the gym.
     * @return an available exercise building or null if none found.
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
        
        gym = null;
    }
}
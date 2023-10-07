/*
 * Mars Simulation Project
 * TaskProbabilityUtil.java
 * @date 2022-06-11
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.util;

import java.util.Iterator;

import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.social.RelationshipUtil;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.RadiationStatus;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.RoboticStation;

/**
 * Utility class for calculating task probabilities.
 */
public class TaskProbabilityUtil {

    /**
     * Private constructor for utility class.
     */
    private TaskProbabilityUtil() {};

    /**
     * Assess if change of Building for a person based on teh overcrowding of the current
     * compared to the new building.
     * @param person the person to perform the task.
     * @param newBuilding the building the person is to go to.
     * @return probability modifier
     */
    public static double getCrowdingProbabilityModifier(Person person, Building newBuilding) {
        double modifier = 1D;

        Building currentBuilding = BuildingManager.getBuilding(person);
        if ((currentBuilding != null) && (newBuilding != null) && !currentBuilding.equals(newBuilding)) {
            double currentRatio = getBuildingCapacityRatio(currentBuilding);
            double newRatio = getBuildingCapacityRatio(newBuilding);

            // Adjust the modifier as the difference between the two.
            // If the new ratio is better then it adds a position weighting
            modifier += newRatio - currentRatio;
        }

        return modifier;
    }

    /**
     * Get a ratio of the capacity of this building as a percentage of the capacity.
     * Negative means building is over capacity.
     * @param b Building being assessed.
     */
    private static double getBuildingCapacityRatio(Building b) {
        LifeSupport currentLS = b.getLifeSupport();
        int capacity = currentLS.getOccupantCapacity();
        return (double)(capacity - currentLS.getOccupantNumber())/capacity;
    }


    public static double getCrowdingProbabilityModifier(Robot robot, Building newBuilding) {
        double modifier = 1D;

        Building currentBuilding = BuildingManager.getBuilding(robot);
        if ((currentBuilding != null) && (newBuilding != null) && (currentBuilding != newBuilding)) {

            // Increase probability if current building is overcrowded.
        	RoboticStation currentRS = currentBuilding.getRoboticStation();

            int currentOverCrowding = currentRS.getRobotOccupantNumber() - currentRS.getOccupantCapacity();
            if (currentOverCrowding > 0) {
                modifier *= ((double) currentOverCrowding + 2);
            }

            // Decrease probability if new building is overcrowded.
           	RoboticStation newRS = newBuilding.getRoboticStation();

            int newOverCrowding = newRS.getRobotOccupantNumber() - newRS.getOccupantCapacity();
            if (newOverCrowding > 0) {
                modifier /= ((double) newOverCrowding + 2);
            }
        }

        return modifier;
    }

    /**
     * Gets the probability modifier for a person performing a task based on his/her
     * relationships with the people in the room the task is to be performed in.
     * 
     * @param person the person to check for.
     * @param building the building the person will need to be in for the task.
     * @return probability modifier
     */
    public static double getRelationshipModifier(Person person, Building building) {
        double result = 1D;

        if ((person == null) || (building == null)) {
            throw new IllegalArgumentException("Task.getRelationshipModifier(): null parameter.");
        }
        
        if (building.hasFunction(FunctionType.LIFE_SUPPORT)) {
            LifeSupport lifeSupport = building.getLifeSupport();
            double totalOpinion = 0D;
            Iterator<Person> i = lifeSupport.getOccupants().iterator();
            while (i.hasNext()) {
                Person occupant = i.next();
                if (person != occupant) {
                    totalOpinion+= ((RelationshipUtil.getOpinionOfPerson(person, occupant) - 50D) / 50D);
                }
            }

            if (totalOpinion >= 0D) {
                result*= (1D + totalOpinion);
            }
            else {
                result/= (1D - totalOpinion);
            }
        }

        return result;
    }

    /**
     * Assess the suitability of the Robot for a task. This only assesses the basic Robot 
     * characteristics against the stanard SettlementTask. 
     * @param t Task to assess
     * @param r Robot being assessed
     * @return A rating
     */
    public static RatingScore assessRobot(SettlementTask t, Robot r) {
        if (t.isEVA()) {
            return RatingScore.ZERO_RATING;
        }

        var factor = new RatingScore(t.getScore());
        factor.addModifier("performance", r.getPerformanceRating());

        return factor;
    }

    /**
	 * Gets the modifier value for a Task score based on the Radiation events occurring
	 * at a Settlement. Events will scale down the modifier towards zero.
	 * 
	 * @param settlement
	 * @return
	 */
    public static double getRadiationModifier(Settlement settlement) {
        RadiationStatus exposed = settlement.getExposed();
        double result = 1D;

        if (exposed.isSEPEvent()) {
            // SEP event stops all activities so zero factor
            result = 0D;
        }

    	if (exposed.isBaselineEvent()) {
    		// Baseline can give a fair amount dose of radiation
			result /= 50D;
		}

    	if (exposed.isGCREvent()) {
    		// GCR can give nearly lethal dose of radiation
			result /= 100D;
		}

        return result;
    }

	/**
	 * Gets the modifier for a Person doing an EVA Operation.
	 * 
	 * @param person 
	 */
	public static double getEVAModifier(Person person) {
		// Check if an airlock is available
		if (EVAOperation.getWalkableAvailableAirlock(person, false) == null)
			return 0;

		// Check if it is night time.
		if (EVAOperation.isGettingDark(person))
			return 0;

		// Checks if the person's settlement is at meal time and is hungry
		if (EVAOperation.isHungryAtMealTime(person))
			return 0;
		
		// Checks if the person is physically fit for heavy EVA tasks
		if (!EVAOperation.isEVAFit(person))
			return 0;
		
		return 1D;
	}
}

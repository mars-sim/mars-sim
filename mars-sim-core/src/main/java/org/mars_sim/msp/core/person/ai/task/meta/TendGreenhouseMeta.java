/**
 * Mars Simulation Project
 * TendGreenhouseMeta.java
 * @version 3.08 2015-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.TendGreenhouse;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Gardenbot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;

/**
 * Meta task for the Tend Greenhouse task.
 */
public class TendGreenhouseMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.tendGreenhouse"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(TendGreenhouseMeta.class.getName());

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new TendGreenhouse(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            try {
                // See if there is an available greenhouse.
                Building farmingBuilding = TendGreenhouse.getAvailableGreenhouse(person);
                if (farmingBuilding != null) {
                    result += 10D;
                    // 2016-10-28 Added getCropsNeedingTendingCache()
                    int needyCropsNum = person.getSettlement().getCropsNeedingTending();
                    result += needyCropsNum * 20D;

                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, farmingBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, farmingBuilding);


                    // Effort-driven task modifier.
                    result *= person.getPerformanceRating();

                    // Job modifier.
                    Job job = person.getMind().getJob();
                    if (job != null) {
                        result *= job.getStartTaskProbabilityModifier(TendGreenhouse.class);
                    }

                    // Modify if tending plants is the person's favorite activity.
                    if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Tending Plants")) {
                        result *= 2D;
                    }

        	        // 2015-06-07 Added Preference modifier
        	        if (result > 0)
         	         	result = result + result * person.getPreference().getPreferenceScore(this)/5D;
        	        
        	        if (result < 0) result = 0;
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, person + " cannot calculate probability : " + e.getMessage());
            }

        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new TendGreenhouse(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.getBotMind().getRobotJob() instanceof Gardenbot)

	        if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

	            try {
	                // See if there is an available greenhouse.
	                Building farmingBuilding = TendGreenhouse.getAvailableGreenhouse(robot);
	                if (farmingBuilding != null) {
	                    result += 10D;
	                    // 2016-10-28 Added getCropsNeedingTendingCache()
	                    int needyCropsNum = robot.getSettlement().getCropsNeedingTending();
	                    //System.out.println("needyCropsNum is "+needyCropsNum);
	                    result += needyCropsNum * 100D;

	                    // Crowding modifier.
	                    //result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot, farmingBuilding);
	                    //result *= TaskProbabilityUtil.getRelationshipModifier(robot, farmingBuilding);
	               
	    	            // Effort-driven task modifier.
	    	            result *= robot.getPerformanceRating();
	    	            //System.out.println("probability is " + result);
	    	            
	                }
	            }
	            catch (Exception e) {
	                logger.log(Level.SEVERE, robot + " cannot calculate probability : " + e.getMessage());
	            }


	        }

        return result;
	}
}
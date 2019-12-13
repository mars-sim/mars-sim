/**
 * Mars Simulation Project
 * TendGreenhouseMeta.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.TendGreenhouse;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Gardenbot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the Tend Greenhouse task.
 */
public class TendGreenhouseMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    private static final double VALUE = 4D;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.tendGreenhouse"); //$NON-NLS-1$

    /** default logger. */
    //private static Logger logger = Logger.getLogger(TendGreenhouseMeta.class.getName());

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

        if (person.isInSettlement()) {
        	
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || stress > 80 || hunger > 500)
            	return 0;
            
            try {
                // See if there is an available greenhouse.
                Building farmingBuilding = TendGreenhouse.getAvailableGreenhouse(person);
                if (farmingBuilding != null) {

                    int needyCropsNum = person.getSettlement().getCropsNeedingTending();
                    result = needyCropsNum * VALUE;

                    if (result <= 0) result = 0;
                    
                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, farmingBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, farmingBuilding);

                    // Effort-driven task modifier.
                    result *= person.getPerformanceRating();

                    // Job modifier.
                    Job job = person.getMind().getJob();
                    if (job != null) {
                        result *= 2 * job.getStartTaskProbabilityModifier(TendGreenhouse.class)
                        		* (person.getSettlement().getGoodsManager().getCropFarmFactor()
                        				+ .5 * person.getAssociatedSettlement().getGoodsManager().getTourismFactor());
                    }

                    // Modify if tending plants is the person's favorite activity.
                    if (person.getFavorite().getFavoriteActivity() == FavoriteType.TENDING_PLANTS) {
                        result += RandomUtil.getRandomInt(1, 10);
                    }
                
        	        // Add Preference modifier
                    double pref = person.getPreference().getPreferenceScore(this);
                   
       	         	result = result + result * pref/4D;        	        	

        	        if (result < 0) result = 0;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            	//logger.log(Level.SEVERE, person + " cannot calculate probability : " + e.getMessage());
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

        if (robot.getBotMind().getRobotJob() instanceof Gardenbot && robot.isInSettlement()) {

            try {
                // See if there is an available greenhouse.
                Building farmingBuilding = TendGreenhouse.getAvailableGreenhouse(robot);
                if (farmingBuilding != null) {
 
                    int needyCropsNum = robot.getSettlement().getCropsNeedingTending();

                    result += needyCropsNum * 50D;
    	            // Effort-driven task modifier.
    	            result *= robot.getPerformanceRating();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                //logger.log(Level.SEVERE, robot + " cannot calculate probability : " + e.getMessage());
            }


        }

        return result;
	}
}
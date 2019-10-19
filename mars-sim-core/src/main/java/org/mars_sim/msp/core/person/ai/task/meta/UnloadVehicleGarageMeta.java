/**
 * Mars Simulation Project
 * UnloadVehicleGarageMeta.java
 * @version 3.1.0 2017-10-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Deliverybot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the UnloadVehicleGarage task.
 */
public class UnloadVehicleGarageMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.unloadVehicleGarage"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(RelaxMeta.class.getName());

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new UnloadVehicleGarage(person);
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
            
            if (fatigue > 1000 || stress > 50 || hunger > 500)
            	return 0;
            
	    	Settlement settlement = person.getSettlement();
	  
            // Check all vehicle missions occurring at the settlement.
            try {
                int numVehicles = 0;
                numVehicles += UnloadVehicleGarage.getAllMissionsNeedingUnloading(settlement).size();
                numVehicles += UnloadVehicleGarage.getNonMissionVehiclesNeedingUnloading(settlement).size();
                result = 100D * numVehicles;
            }
            catch (Exception e) {
                logger.log(Level.SEVERE,"Error finding unloading missions. " + e.getMessage());
                e.printStackTrace(System.err);
            }
            
            if (result <= 0) result = 0;

            // Effort-driven task modifier.
            result *= person.getPerformanceRating();

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartTaskProbabilityModifier(UnloadVehicleGarage.class)
                		* person.getSettlement().getGoodsManager().getTransportationFactor();
            }

            // Modify if operations is the person's favorite activity.
            if (person.getFavorite().getFavoriteActivity() == FavoriteType.OPERATION) {
                result += RandomUtil.getRandomInt(1, 20);
            }

            // Added Preference modifier
            if (result > 0)
            	result = result + result * person.getPreference().getPreferenceScore(this)/5D;
          
            if (result < 0) result = 0;
            
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new UnloadVehicleGarage(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.getBotMind().getRobotJob() instanceof Deliverybot)
	        if (robot.isInSettlement()) {

	            // Check all vehicle missions occurring at the settlement.
	            try {
	                int numVehicles = 0;
	                
	               	Settlement settlement = robot.getAssociatedSettlement();
	                
	                numVehicles += UnloadVehicleGarage.getAllMissionsNeedingUnloading(settlement).size();
	                numVehicles += UnloadVehicleGarage.getNonMissionVehiclesNeedingUnloading(settlement).size();
	                result = 100D * numVehicles;
	            }
	            catch (Exception e) {
	                logger.log(Level.SEVERE,"Error finding unloading missions. " + e.getMessage());
	                e.printStackTrace(System.err);
	            }


	        // Effort-driven task modifier.
	        result *= robot.getPerformanceRating();

	        }

        return result;
    }
}
/**
 * Mars Simulation Project
 * ProduceFoodMeta.java
 * @version 3.08 2015-06-08
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.ProduceFood;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Chefbot;
import org.mars_sim.msp.core.robot.ai.job.Makerbot;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the ProduceFood task.
 */
public class ProduceFoodMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.produceFood"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new ProduceFood(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInSettlement() && !person.getSettlement().getFoodProductionOverride()) {
	        // If settlement has foodProduction override, no new foodProduction processes can be created.
        	
            // See if there is an available foodProduction building.
            Building foodProductionBuilding = ProduceFood.getAvailableFoodProductionBuilding(person);
            
            if (foodProductionBuilding != null) {
            	result += 1D;

                // Crowding modifier.
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, foodProductionBuilding);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, foodProductionBuilding);

                // FoodProduction good value modifier.
                result *= ProduceFood.getHighestFoodProductionProcessValue(person, foodProductionBuilding);

                // Capping the probability at 100 as food production process values can be very large numbers.
                if (result > 100D) {
                    result = 100D;
                }

                // If foodProduction building has process requiring work, add
                // modifier.
                SkillManager skillManager = person.getMind().getSkillManager();
                int skill = skillManager.getEffectiveSkillLevel(SkillType.COOKING) * 5;
                skill += skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
                skill = (int) Math.round(skill / 7D);
                if (ProduceFood.hasProcessRequiringWork(foodProductionBuilding, skill)) {
                    result += 10D;
                }

    	        // Effort-driven task modifier.
    	        result *= person.getPerformanceRating();

    	        // Job modifier.
    	        Job job = person.getMind().getJob();
    	        if (job != null) {
    	            result *= job.getStartTaskProbabilityModifier(ProduceFood.class)
                    		* person.getSettlement().getGoodsManager().getCropFarmFactor();
    	        }

                // Modify if cooking is the person's favorite activity.
                if (person.getFavorite().getFavoriteActivity() == FavoriteType.COOKING) {
                    result *= 1.5D;
                }

    	        // Add Preference modifier
                if (result > 0D) {
                    result = result + result * person.getPreference().getPreferenceScore(this)/5D;
                }
                
    	        if (result < 0) result = 0;
            }
	        
	        // Cancel any foodProduction processes that's beyond the skill of any people
	        // associated with the settlement.
	        if (result > 0)
	        	ProduceFood.cancelDifficultFoodProductionProcesses(person);

        }
        
        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new ProduceFood(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.getBotMind().getRobotJob() instanceof Chefbot || robot.getBotMind().getRobotJob() instanceof Makerbot){

			if (robot.isInSettlement()) {

		        // If settlement has foodProduction override, no new
		        // foodProduction processes can be created.
		        if (!robot.getSettlement().getFoodProductionOverride()) {

		            // See if there is an available foodProduction building.
		            Building foodProductionBuilding = ProduceFood.getAvailableFoodProductionBuilding(robot);
		            if (foodProductionBuilding != null) {
		                result += 100D;

		                // FoodProduction good value modifier.
		                result *= ProduceFood.getHighestFoodProductionProcessValue(robot, foodProductionBuilding);

		                // If foodProduction building has process requiring work, add modifier.
		                SkillManager skillManager = robot.getBotMind().getSkillManager();
		                int skill = skillManager.getEffectiveSkillLevel(SkillType.COOKING) * 5;
		                skill += skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
		                skill = (int) Math.round(skill / 7D);

		                if (ProduceFood.hasProcessRequiringWork(foodProductionBuilding, skill)) {
		                    result += 100D;
		                }

			            // Effort-driven task modifier.
			            result *= robot.getPerformanceRating();

		            }

		        }
		        // Cancel any foodProduction processes that's beyond the skill of any people
		        // associated with the settlement.
		        if (result > 0)
		        	ProduceFood.cancelDifficultFoodProductionProcesses(robot);

			}
        }

        return result;
	}
}
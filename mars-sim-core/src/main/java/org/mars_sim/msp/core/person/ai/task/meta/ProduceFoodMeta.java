/**
 * Mars Simulation Project
 * ProduceFoodMeta.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.ProduceFood;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Chefbot;
import org.mars_sim.msp.core.robot.ai.job.Makerbot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the ProduceFood task.
 */
public class ProduceFoodMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.produceFood"); //$NON-NLS-1$

    private static final double CAP = 3000D;
    
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
    	if (person.isOutside() || person.isInVehicle()) {
    		return 0;
    	}
    	
        double result = 0D;

        if (person.isInSettlement() && !person.getSettlement().getFoodProductionOverride()) {
	        // If settlement has foodProduction override, no new foodProduction processes can be created.
        	
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || stress > 50 || hunger > 500)
            	return 0;
            
            // See if there is an available foodProduction building.
            Building foodProductionBuilding = ProduceFood.getAvailableFoodProductionBuilding(person);
            
            if (foodProductionBuilding != null) {

                // If foodProduction building has process requiring work, add
                // modifier.
                SkillManager skillManager = person.getSkillManager();
                int skill = skillManager.getEffectiveSkillLevel(SkillType.COOKING) * 5;
                skill += skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
                skill = (int) Math.round(skill / 7D);
                if (ProduceFood.hasProcessRequiringWork(foodProductionBuilding, skill)) {
                    result += 15D;
                }
                
                // Stress modifier
                result = result - stress * 3.5D;
                // fatigue modifier
                result = result - (fatigue - 100) / 2.5D;

                // Crowding modifier.
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, foodProductionBuilding);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, foodProductionBuilding);

                // FoodProduction good value modifier.
                result *= ProduceFood.getHighestFoodProductionProcessValue(person, foodProductionBuilding);

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
                    result *= RandomUtil.getRandomDouble(2D);
                }

    	        // Add Preference modifier
                result = result + result * person.getPreference().getPreferenceScore(this)/6D;
       
                // Capping the probability at 100 as manufacturing process values can be very large numbers.
                if (result > CAP) {
                    result = CAP;
                }

    	        if (result < 0) result = 0;
            }
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
		                SkillManager skillManager = robot.getSkillManager();
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
			}
        }

        return result;
	}
}
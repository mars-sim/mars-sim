/**
 * Mars Simulation Project
 * ProduceFoodMeta.java
 * @version 3.07 2015-02-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Chefbot;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.ProduceFood;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the ProduceFood task.
 */
public class ProduceFoodMeta implements MetaTask {
    
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
        
        double result = 100D;
        
        // TODO: the cook should check if he himself or someone else is hungry, 
        // he's more eager to cook except when he's tired
        result += person.getPhysicalCondition().getHunger() - 400D;
        result -= 0.4 * (person.getPhysicalCondition().getFatigue() - 700D);
        if (result < 0D) result = 0D;

        // Cancel any foodProduction processes that's beyond the skill of any people 
        // associated with the settlement.
        ProduceFood.cancelDifficultFoodProductionProcesses(person);

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // See if there is an available foodProduction building.
            Building foodProductionBuilding = ProduceFood.getAvailableFoodProductionBuilding(person);
            if (foodProductionBuilding != null) {
            	result += 50D;
            	
                // Crowding modifier.
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, foodProductionBuilding);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, foodProductionBuilding);

                // FoodProduction good value modifier.
                result *= ProduceFood.getHighestFoodProductionProcessValue(person, foodProductionBuilding);


                // If foodProduction building has process requiring work, add
                // modifier.
                SkillManager skillManager = person.getMind().getSkillManager();
                int skill = skillManager.getEffectiveSkillLevel(SkillType.COOKING);
                if (ProduceFood.hasProcessRequiringWork(foodProductionBuilding, skill)) {
                    result += 100D;
                }

                // If settlement has foodProduction override, no new  foodProduction processes can be created.
                if (person.getSettlement().getFoodProductionOverride()) {
                    result = 0;
                }
            }
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(ProduceFood.class);
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
        
        // Job modifier.
        //RobotJob robotJob = robot.getBotMind().getRobotJob();
        //if (robotJob != null)
         //   result = robotJob.getStartTaskProbabilityModifier(ProduceFood.class);        
        if (robot.getBotMind().getRobotJob() instanceof Chefbot)
	        if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
	        	
	        	result += 10D;
	            // See if there is an available foodProduction building.
	            Building foodProductionBuilding = ProduceFood.getAvailableFoodProductionBuilding(robot);
	            if (foodProductionBuilding != null) {
	                result += 100D;
	
	                // FoodProduction good value modifier.
	                result *= ProduceFood.getHighestFoodProductionProcessValue(robot, foodProductionBuilding);
	
	
	                // If foodProduction building has process requiring work, add
	                // modifier.
	                SkillManager skillManager = robot.getBotMind().getSkillManager();
	                int skill = skillManager.getEffectiveSkillLevel(SkillType.COOKING);
	                
	                if (ProduceFood.hasProcessRequiringWork(foodProductionBuilding, skill)) {
	                    result += 100D;
	                }
	
	                // If settlement has foodProduction override, no new
	                // foodProduction processes can be created.
	                if (robot.getSettlement().getFoodProductionOverride()) {
	                    result = 0;
	                }
	            }
	            
	            // Effort-driven task modifier.
	            result *= robot.getPerformanceRating();

	        }

        return result;
	}
}
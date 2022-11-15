/*
 * Mars Simulation Project
 * ProduceFoodMeta.java
 * @date 2022-07-26
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.ProduceFood;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the ProduceFood task.
 */
public class ProduceFoodMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.produceFood"); //$NON-NLS-1$

    private static final double CAP = 3_000D;
    
    public ProduceFoodMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.COOKING);
		setTrait(TaskTrait.ARTISTIC);
		
		setPreferredJob(JobType.BIOLOGIST, JobType.CHEF,
						JobType.CHEMIST, JobType.BOTANIST);

        addPreferredRobot(RobotType.CHEFBOT);
        addPreferredRobot(RobotType.MAKERBOT);
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

        if (person.isInSettlement() && !person.getSettlement().getProcessOverride(OverrideType.FOOD_PRODUCTION)) {
	        // If settlement has foodProduction override, no new foodProduction processes can be created.
        	
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            
            if (fatigue > 1000 || stress > 50)
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
        		result *= person.getSettlement().getGoodsManager().getCropFarmFactor();

    	        result = applyPersonModifier(result, person);
       
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

        if (robot.isInSettlement()) {

            // If settlement has foodProduction override, no new
            // foodProduction processes can be created.
            if (!robot.getSettlement().getProcessOverride(OverrideType.FOOD_PRODUCTION)) {

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

        return result;
	}
}

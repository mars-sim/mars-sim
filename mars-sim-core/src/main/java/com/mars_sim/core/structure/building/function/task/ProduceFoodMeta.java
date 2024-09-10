/*
 * Mars Simulation Project
 * ProduceFoodMeta.java
 * @date 2022-07-26
 * @author Manny Kung
 */
package com.mars_sim.core.structure.building.function.task;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the ProduceFood task.
 */
public class ProduceFoodMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.produceFood"); //$NON-NLS-1$
    
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

    /**
     * Assesses if a Person can prepare some food. Assessment is based on the ability to
     * find a Building that can produce food.
     * 
     * @param person Being assessed
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {
        // If settlement has foodProduction override, no new foodProduction processes can be created.
    	if (person.isOutside() || !person.isInSettlement()
            || person.getSettlement().getProcessOverride(OverrideType.FOOD_PRODUCTION)) {
    		return EMPTY_TASKLIST;
    	}
    	        	
        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double stress = condition.getStress();          
        if (fatigue > 1000 || stress > 50)
            return EMPTY_TASKLIST;
            
        // See if there is an available foodProduction building.
        Building foodProductionBuilding = ProduceFood.getAvailableFoodProductionBuilding(person);
        if (foodProductionBuilding == null) {
    		return EMPTY_TASKLIST;
    	}

        double base = 0D;

        // If foodProduction building has process requiring work, add
        // modifier.
        SkillManager skillManager = person.getSkillManager();
        int skill = skillManager.getEffectiveSkillLevel(SkillType.COOKING) * 5;
        skill += skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
        skill = (int) Math.round(skill / 7D);
        if (ProduceFood.hasProcessRequiringWork(foodProductionBuilding, skill)) {
            base += 15D;
        }
                
        // Stress modifier
        base -= stress * 3.5D;

        // fatigue modifier
        base -= (fatigue - 100) / 2.5D;

        var result = new RatingScore(base);
        result = assessBuildingSuitability(result, foodProductionBuilding, person);

        // FoodProduction good value modifier.
        result.addModifier("production",
                ProduceFood.getHighestFoodProductionProcessValue(person, foodProductionBuilding));
        result = applyCommerceFactor(result, person.getSettlement(), CommerceType.CROP);

    	result = assessPersonSuitability(result, person);

        return createTaskJobs(result);
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new ProduceFood(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;
      
        // If settlement has foodProduction override, no new
        // foodProduction processes can be created.
        if (robot.isInSettlement()
            && !robot.getSettlement().getProcessOverride(OverrideType.FOOD_PRODUCTION)) {

            // See if there is an available food production building.
            Building foodProductionBuilding = ProduceFood.getAvailableFoodProductionBuilding(robot);
            if (foodProductionBuilding != null) {
                result += 100D;

                // Food production good value modifier.
                result *= ProduceFood.getHighestFoodProductionProcessValue(robot, foodProductionBuilding);

                // If food production building has process requiring work, add modifier.
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

        return result;
	}
}

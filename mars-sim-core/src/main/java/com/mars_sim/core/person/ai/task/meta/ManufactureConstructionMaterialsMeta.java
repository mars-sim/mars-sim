/*
 * Mars Simulation Project
 * ManufactureConstructionMaterialsMeta.java
 * @date 2022-09-01
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.ManufactureConstructionMaterials;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the ManufactureConstructionMaterials task.
 */
public class ManufactureConstructionMaterialsMeta extends FactoryMetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.manufactureConstructionMaterials"); //$NON-NLS-1$
    
    /** default logger. */
    private static final SimLogger logger = SimLogger.getLogger(ManufactureConstructionMaterialsMeta.class.getName());

    public ManufactureConstructionMaterialsMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TINKERING);
		setTrait(TaskTrait.ARTISTIC);
		setPreferredJob(JobType.ARCHITECT, JobType.CHEMIST, JobType.ENGINEER);
        
		addPreferredRobot(RobotType.MEDICBOT);
        addPreferredRobot(RobotType.MAKERBOT);
        addPreferredRobot(RobotType.REPAIRBOT);
        addPreferredRobot(RobotType.CONSTRUCTIONBOT);
	}

    @Override
    public Task constructInstance(Person person) {
        return new ManufactureConstructionMaterials(person);
    }

    @Override
    public List<TaskJob> getTaskJobs(Person person) {
        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)
            || !person.isInSettlement()
            || person.getSettlement().getProcessOverride(OverrideType.MANUFACTURE)) {
        	return EMPTY_TASKLIST;
        }


        // See if there is an available manufacturing building.
        Building manufacturingBuilding = ManufactureConstructionMaterials.getAvailableManufacturingBuilding(person);
        if ((manufacturingBuilding == null) 
           || manufacturingBuilding.getManufacture().getBuilding().getMalfunctionManager().hasMalfunction()) {
            return EMPTY_TASKLIST;
        }
        
        double base = 1D;

        // If manufacturing building has process requiring work, add
        // modifier.
        SkillManager skillManager = person.getSkillManager();
        int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
        if (ManufactureConstructionMaterials.hasProcessRequiringWork(manufacturingBuilding, skill)) {
            base += 10D;
        }
            
        var score = new RatingScore(base);
        score = assessBuildingSuitability(score, manufacturingBuilding, person);

            // Manufacturing good value modifier.
        score.addModifier("production",
                ManufactureConstructionMaterials.getHighestManufacturingProcessValue(person,
                    manufacturingBuilding)/1000D);
            
        score.addModifier(GOODS_MODIFIER, person.getSettlement().getGoodsManager().getManufacturingFactor());
        score = assessPersonSuitability(score, person);

        return createTaskJobs(score);
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new ManufactureConstructionMaterials(robot);
	}

	@Override
	public double getProbability(Robot robot) {
		// Check for the override
        if (robot.getSettlement().getProcessOverride(OverrideType.MANUFACTURE)) {
            return 0;
        }
			
        double result = 0D;

        if (robot.isInSettlement()) {
        	
            try {
                // See if there is an available manufacturing building.
                Building manufacturingBuilding = ManufactureConstructionMaterials.getAvailableManufacturingBuilding(robot);
                if (manufacturingBuilding != null) {
                    result = 1D;

                    // Crowding modifier.
                    result *= TaskUtil.getCrowdingProbabilityModifier(robot,
                            manufacturingBuilding);

                    // Manufacturing good value modifier.
                    result *= ManufactureConstructionMaterials.getHighestManufacturingProcessValue(robot,
                            manufacturingBuilding);

                    // Cap the result to a max value of 100.
                    if (result > 100D) {
                        result = 100D;
                    }

                    // If manufacturing building has process requiring work, add
                    // modifier.
                    SkillManager skillManager = robot.getSkillManager();
                    int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
                    
                    if (ManufactureConstructionMaterials.hasProcessRequiringWork(manufacturingBuilding, skill)) {
                        result += 10D;
                    }
                }
                
            } catch (Exception e) {
                logger.severe(
                        "ManufactureConstructionMaterials.getProbability()", e);
            }

            // Effort-driven task modifier.
            result *= robot.getPerformanceRating();
            
            if (result < 0) result = 0;

        }

        return result;
	}
}

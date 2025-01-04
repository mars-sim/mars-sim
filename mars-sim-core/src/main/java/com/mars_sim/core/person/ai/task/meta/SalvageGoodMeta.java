/*
 * Mars Simulation Project
 * SalvageGoodMeta.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.manufacture.task.ManufactureGood;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.SalvageGood;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the SalvageGood task.
 */
public class SalvageGoodMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.salvageGood"); //$NON-NLS-1$

    public SalvageGoodMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.STRENGTH, TaskTrait.ARTISTIC);
		setPreferredJob(JobType.ENGINEER, JobType.TECHNICIAN);
	}

    @Override
    public Task constructInstance(Person person) {
        return new SalvageGood(person);
    }

    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        // If settlement has manufacturing override, no new settlement-driven
        // salvage processes can be created.
        if (!person.isInSettlement()
            || !person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
                return EMPTY_TASKLIST;
        }
        
        // If settlement has manufacturing override, no new settlement-driven
        // salvage processes can be created.
        
        // Note: need to account for player manually adding a salvage process
        //       in manu tab. Override means settlement cannot automatically add. 
        //       It doesn't mean that player cannot manually add.
        
//        if (person.getSettlement().getProcessOverride(OverrideType.SALVAGE)) {
//        	return EMPTY_TASKLIST;
//        }

        // No salvaging goods until after the first month of the simulation.
//        MarsTime startTime = getMasterClock().getInitialMarsTime();
//        MarsTime currentTime = getMasterClock().getMarsTime();
//        double totalTimeMillisols = currentTime.getTimeDiff(startTime);
//        double totalTimeOrbits = totalTimeMillisols / 1000D / MarsTime.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR;
//        if (totalTimeOrbits < MarsTime.SOLS_PER_MONTH_LONG) {
//            return EMPTY_TASKLIST;
//        }
        
        double base = 0;
        SkillManager skillManager = person.getSkillManager();
        int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
        
        // See if there is an available manufacturing building.
        Building manufacturingBuilding = null;
        
        for (Building potentialBuilding :
                ManufactureGood.getAvailableManufacturingBuilding(person.getSettlement(), skill)) {
            // Look for a building that has started salvage work
		    if (SalvageGood.hasSalvageProcessRequiringWork(potentialBuilding, skill)) {
		    	manufacturingBuilding = potentialBuilding;
		        base += 50D;
		    }
        }
        
        if (manufacturingBuilding == null) {
        	return EMPTY_TASKLIST;
        }
        
        var score = new RatingScore(base);
        
        score.addBase(SKILL_MODIFIER, 1 + (skill * 0.075D));
        
        score = assessBuildingSuitability(score, manufacturingBuilding, person);
        score = assessPersonSuitability(score, person);

        // Salvaging good value modifier.
        score.addModifier("production", SalvageGood.getHighestSalvagingProcessValue(person, manufacturingBuilding));

        return createTaskJobs(score);
    }
}

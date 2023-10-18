/*
 * Mars Simulation Project
 * SalvageGoodMeta.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.SalvageGood;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.tools.Msg;

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
    public double getProbability(Person person) {

        double result = 0D;

        // If settlement has manufacturing override, no new
        // salvage processes can be created.
        if (person.isInSettlement()) {
            // Check for the override
            if (person.getSettlement().getProcessOverride(OverrideType.SALVAGE)) {
                return 0;
            }
        
            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
            	return 0;

	        // No salvaging goods until after the first month of the simulation.
	        MarsTime startTime = getMasterClock().getInitialMarsTime();
	        MarsTime currentTime = getMasterClock().getMarsTime();
	        double totalTimeMillisols = currentTime.getTimeDiff(startTime);
	        double totalTimeOrbits = totalTimeMillisols / 1000D / MarsTime.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR;
	        if (totalTimeOrbits < MarsTime.SOLS_PER_MONTH_LONG) {
	            return 0;
	        }

            // See if there is an available manufacturing building.
            Building manufacturingBuilding = SalvageGood.getAvailableManufacturingBuilding(person);
            if (manufacturingBuilding != null) {
                result = 1D;

                // Crowding modifier.
                result *= getBuildingModifier(manufacturingBuilding, person);

                // Salvaging good value modifier.
                result *= SalvageGood.getHighestSalvagingProcessValue(person, manufacturingBuilding);

                if (result > 100D) {
                    result = 100D;
                }

                // If manufacturing building has salvage process requiring work, add
                // modifier.
                SkillManager skillManager = person.getSkillManager();
                int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
                if (SalvageGood.hasSalvageProcessRequiringWork(manufacturingBuilding, skill)) {
                    result += 10D;
                }

                result *= getPersonModifier(person);
            }
        }

        return result;
    }
}

/*
 * Mars Simulation Project
 * OptimizeSystem.java
 * @date 2023-07-29
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.OptimizeSystem;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * Meta task for the OptimizeSystem task.
 */
public class OptimizeSystemMeta extends FactoryMetaTask {

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.optimizeSystem"); //$NON-NLS-1$

	
    public OptimizeSystemMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.ACADEMIC);
			
		addPreferredJob(JobType.COMPUTER_SCIENTIST, 4D);
		addPreferredJob(JobType.ENGINEER, 3D);
		addPreferredJob(JobType.MATHEMATICIAN, JOB_BONUS);
		addPreferredJob(JobType.PHYSICIST, JOB_BONUS);
			
	    addPreferredRole(RoleType.COMPUTING_SPECIALIST, 3D);
	    addPreferredRole(RoleType.ENGINEERING_SPECIALIST, 2D);
	    addPreferredRole(RoleType.CHIEF_OF_COMPUTING, 2.5D);
	    addPreferredRole(RoleType.CHIEF_OF_ENGINEERING,1.5D);
    }
    
	@Override
	public Task constructInstance(Person person) {
		return new OptimizeSystem(person);
	}

	@Override
	public List<TaskJob> getTaskJobs(Person person) {
        
		if (!person.isInSettlement()) {
			return EMPTY_TASKLIST;
		}

		// Compute total entropy
		double base = person.getSettlement().getBuildingManager().
						getTotalEntropy();
						
		if (base < 0.01)
			base = 0.01;
				
		double org = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.ORGANIZATION);
		double com = 0;
		
		if (person.getSkillManager().getSkill(SkillType.COMPUTING) != null) {
			com = person.getSkillManager().getSkill(SkillType.COMPUTING).getCumuativeExperience();
		}

		var result = new RatingScore(RandomUtil.getRandomDouble(base + org + com));
			
		result = assessPersonSuitability(result, person);

		return createTaskJobs(result);
	}
}

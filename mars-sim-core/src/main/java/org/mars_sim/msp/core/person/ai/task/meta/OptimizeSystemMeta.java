/*
 * Mars Simulation Project
 * OptimizeSystem.java
 * @date 2023-07-29
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;

import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.OptimizeSystem;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;

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

/*
 * Mars Simulation Project
 * OptimizeSystem.java
 * @date 2023-11-30
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
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

/**
 * Meta task for the OptimizeSystem task.
 */
public class OptimizeSystemMeta extends FactoryMetaTask {

	private static final int CU_FACTOR = 10;
	private static final int NODE_FACTOR = 15;
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.optimizeSystem"); //$NON-NLS-1$
	/** default logger. */
	// May add back private static SimLogger logger = SimLogger.getLogger(OptimizeSystemMeta.class.getName())

	
    public OptimizeSystemMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.ACADEMIC);
			
		setPreferredRole(RoleType.CREW_ENGINEER, RoleType.CREW_OPERATION_OFFICER);
		
		addPreferredJob(JobType.COMPUTER_SCIENTIST, 3);
		addPreferredJob(JobType.ENGINEER, 2.5);
		addPreferredJob(JobType.MATHEMATICIAN, JOB_BONUS);
		addPreferredJob(JobType.PHYSICIST, JOB_BONUS);
			
	    addPreferredRole(RoleType.COMPUTING_SPECIALIST, 2.5);
	    addPreferredRole(RoleType.ENGINEERING_SPECIALIST, 2);
	    addPreferredRole(RoleType.CHIEF_OF_COMPUTING, 2);
	    addPreferredRole(RoleType.CHIEF_OF_ENGINEERING, 1.5);
    }
    
	@Override
	public Task constructInstance(Person person) {
		return new OptimizeSystem(person);
	}

	@Override
	public List<TaskJob> getTaskJobs(Person person) {
        
		if (!person.isInside()) {
			return EMPTY_TASKLIST;
		}

		// Compute total entropy and average minimum entropy per node
		double totENPerN = person.getAssociatedSettlement().getBuildingManager()
					.getTotalEntropyPerNode();
		
		double totENPerCU = person.getAssociatedSettlement().getBuildingManager()
				.getTotalEntropyPerCU();
		
//		double ave = person.getAssociatedSettlement().getBuildingManager()
//					.getAverageMinimumEntropy();
		
		if (totENPerN < 0 && totENPerCU < 0) {
			return EMPTY_TASKLIST;
		}
		
		if (totENPerN > 0 && totENPerCU < 0.1) {
			if (totENPerCU < 0.01) {
				totENPerCU = 0.01;
			}
			else {
				totENPerCU = 0.1;
			}
		}
		
		RatingScore score = new RatingScore(ENTROPY_CU, CU_FACTOR * totENPerCU);
				
		score.addBase(ENTROPY_NODE, NODE_FACTOR * totENPerN);
		
		NaturalAttributeManager manager = person.getNaturalAttributeManager();
		
		double att = (.5 * manager.getAttribute(NaturalAttributeType.ORGANIZATION)
				+ .3 * manager.getAttribute(NaturalAttributeType.METICULOUSNESS)
				+ .2 * manager.getAttribute(NaturalAttributeType.CREATIVITY)) / 25;
		
		// Make sure the minimum skillF is 1, not zero
		double skillF = 1;
		
		if (person.getSkillManager().getSkill(SkillType.COMPUTING) != null) {
			skillF = 1 + person.getSkillManager().getSkill(SkillType.COMPUTING).getLevel()/5;
		}

		score.addModifier(SKILL_MODIFIER, skillF);
        score.addModifier(ATTRIBUTE, att);
        
        score = assessPersonSuitability(score, person);
        
		return createTaskJobs(score);
	}
}

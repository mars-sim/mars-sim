/*
 * Mars Simulation Project
 * OptimizeSystem.java
 * @date 2023-11-30
 * @author Manny Kung
 */
package com.mars_sim.core.structure.building.function.task;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the OptimizeSystem task.
 */
public class OptimizeSystemMeta extends FactoryMetaTask {

	private static final int CU_FACTOR = 50;
	private static final int NODE_FACTOR = 50;
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.optimizeSystem"); //$NON-NLS-1$
	/** default logger. */
	// May add back private static SimLogger logger = SimLogger.getLogger(OptimizeSystemMeta.class.getName())

    public OptimizeSystemMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
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
        
		if (!person.isInSettlement()) {
			return EMPTY_TASKLIST;
		}

		double pop = person.getAssociatedSettlement().getIndoorPeopleCount();
		
		double popFactor = Math.sqrt(pop);
		
		// Compute total entropy and average minimum entropy per node
		double[] array = person.getAssociatedSettlement().getBuildingManager()
					.getTotalEntropyPerNode();
		
		int numNode = (int)array[0];
		double totalEntropy = array[1];
		
		double[] array1 = person.getAssociatedSettlement().getBuildingManager()
				.getTotalEntropyPerCU();
			
//		int numNode = (int)array[0];
		double totalEntropyPerCU = array1[1];
		
		if (totalEntropy < 0 && totalEntropyPerCU < 0) {
			return EMPTY_TASKLIST;
		}
		
		if (totalEntropy > 0 && totalEntropyPerCU < 0.1) {
			if (totalEntropyPerCU < 0.1) {
				totalEntropyPerCU = 0.1;
			}
		}
		
		RatingScore score = new RatingScore(ENTROPY_CU, CU_FACTOR * totalEntropyPerCU / popFactor);
				
		score.addBase(ENTROPY_NODE, NODE_FACTOR * totalEntropy / numNode / popFactor);
		
		NaturalAttributeManager manager = person.getNaturalAttributeManager();
		
		double att = (.5 * manager.getAttribute(NaturalAttributeType.ORGANIZATION)
				+ .3 * manager.getAttribute(NaturalAttributeType.METICULOUSNESS)
				+ .2 * manager.getAttribute(NaturalAttributeType.CREATIVITY)) / 25;
		
		// Make sure the minimum skillF is 1, not zero
		int skillF = 1;
		
		if (person.getSkillManager().getSkill(SkillType.COMPUTING) != null) {
			skillF = 1 + person.getSkillManager().getSkill(SkillType.COMPUTING).getLevel();
		}

		score.addModifier(SKILL_MODIFIER, skillF);
        score.addModifier(ATTRIBUTE, att);
        
        score = assessPersonSuitability(score, person);
        
		return createTaskJobs(score);
	}
}

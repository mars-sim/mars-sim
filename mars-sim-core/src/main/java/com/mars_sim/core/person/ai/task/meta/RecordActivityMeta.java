/*
 * Mars Simulation Project
 * RecordActivityMeta.java
 * @date 2021-09-27
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.RecordActivity;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Meta task for the RecordActivity task.
 */
public class RecordActivityMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.recordActivity"); //$NON-NLS-1$

    public RecordActivityMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setTrait(TaskTrait.ARTISTIC);
		setPreferredJob(JobType.REPORTER);
		setPreferredRole(RoleType.CREW_OPERATION_OFFICER);
	}

    @Override
    public Task constructInstance(Person person) {
        return new RecordActivity(person);
    }

    @Override
    public List<TaskJob> getTaskJobs(Person person) {

    	// Do not allow to record activity outside for now
    	if (!person.isOutside()
            || !person.getPhysicalCondition().isFitByLevel(1500, 75, 750)) {
    		return EMPTY_TASKLIST;
        }

    	var score = new RatingScore(RandomUtil.getRandomDouble(200));
        score = assessPersonSuitability(score, person);
        score = applyCommerceFactor(score, person.getAssociatedSettlement(), CommerceType.TOURISM);

        // Certain roles are less likely to engage in this task
        RoleType roleType = person.getRole().getType();
        double roleModifier = 1D;
        if (roleType != null) {
            if (roleType == RoleType.PRESIDENT)
                roleModifier = 0.01D;
            else if (roleType == RoleType.MAYOR)
                roleModifier = 0.05D;
            else if (roleType.equals(RoleType.ADMINISTRATOR))
            	roleModifier = 0.1D;
            else if (roleType == RoleType.COMMANDER)
                roleModifier = 0.15D;
            else if (roleType == RoleType.SUB_COMMANDER)
                roleModifier = 0.2D;
            else if (roleType.isChief())
                roleModifier = 0.3D;
        }
        score.addModifier(ROLE_MODIFIER, roleModifier);
        return createTaskJobs(score);
    }
}

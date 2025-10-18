/*
 * Mars Simulation Project
 * DelegateWorkMeta.java
 * @date 2023-06-16
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.DelegateWork;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.tool.Msg;

/**
 * The meta task for delegating work.
 */
public class DelegateWorkMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.delegateWork"); //$NON-NLS-1$
    
    public DelegateWorkMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		setTrait(TaskTrait.ORGANIZATION, TaskTrait.LEADERSHIP);
		setPreferredRole(RoleType.CREW_OPERATION_OFFICER);
		addAllLeadershipRoles();
	}

    @Override
    public Task constructInstance(Person person) {
        return new DelegateWork(person);
    }

    @Override
    public List<TaskJob> getTaskJobs(Person person) {
        RoleType roleType = person.getRole().getType();
        
    	if (RoleType.GUEST == roleType || !person.isInside()
            || !roleType.isLeadership()
            || !person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
            return EMPTY_TASKLIST;
        }
           
        double base;
        if (roleType.equals(RoleType.PRESIDENT))
            base = 45D;
        else if (roleType.equals(RoleType.MAYOR))
            base = 40D;
        else if (roleType.equals(RoleType.ADMINISTRATOR))
            base = 35D;
        else if (roleType.equals(RoleType.DEPUTY_ADMINISTRATOR))
            base = 30D;
        else if (roleType.equals(RoleType.COMMANDER))
            base = 25D;
        else if (roleType.equals(RoleType.SUB_COMMANDER))
            base = 20D;
        else if (roleType.isChief())
            base = 15D;
        else
            return EMPTY_TASKLIST;
        
        var score = new RatingScore(base);
            
        // Get an available office space.
        if (person.isInSettlement()) {
	        Building building = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.ADMINISTRATION);
	        score = assessBuildingSuitability(score, building, person);
	        score = assessPersonSuitability(score, person);
        }
        
        return createTaskJobs(score);
    }
}

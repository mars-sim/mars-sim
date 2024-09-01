/*
 * Mars Simulation Project
 * WriteReportMeta.java
 * @date 2021-09-27
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.WriteReport;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the WriteReport task.
 */
public class WriteReportMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.writeReport"); //$NON-NLS-1$
    
    public WriteReportMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setTrait(TaskTrait.TEACHING, TaskTrait.LEADERSHIP, TaskTrait.ORGANIZATION);
	}

    @Override
    public Task constructInstance(Person person) {
        return new WriteReport(person);
    }

	/**
	 * Assess the suitability of a person to write a report. Based on their Role.
	 * @param person Being assessed
	 * @return Potential tasks jobs
	 */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {
    		
		if (!person.isInside()
			|| !person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
			return EMPTY_TASKLIST;
		}
		
		RoleType roleType = person.getRole().getType();
		double base =  switch(roleType) {
			case PRESIDENT -> 50D;
			case MAYOR -> 40D;
			case COMMANDER -> 30D;
			case SUB_COMMANDER -> 20D;
			default -> 10D;
		};
		
		if (roleType.isChief())
			base = 15D;

		RatingScore score = new RatingScore(base);
		
		// Get an available office space.
		Building building = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.ADMINISTRATION);

		// Note: if an office space is not available such as in a vehicle, one can still write reports!
		assessBuildingSuitability(score, building, person);

		score = assessPersonSuitability(score, person);
		
		return createTaskJobs(score);
    }
}

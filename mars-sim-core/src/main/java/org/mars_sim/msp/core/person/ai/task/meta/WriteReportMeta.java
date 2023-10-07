/*
 * Mars Simulation Project
 * WriteReportMeta.java
 * @date 2021-09-27
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.WriteReport;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

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

		var result = new RatingScore(base);
		
		// Get an available office space.
		Building building = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.ADMINISTRATION);

		// Note: if an office space is not available such as in a vehicle, one can still write reports!
		assessBuildingSuitability(result, building, person);

		assessPersonSuitability(result, person);
		
		return createTaskJobs(result);
    }
}

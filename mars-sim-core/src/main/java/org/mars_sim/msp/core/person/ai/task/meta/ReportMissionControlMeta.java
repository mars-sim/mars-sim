/*
 * Mars Simulation Project
 * ReportMissionControlMeta.java
 * @date 2023-05-31
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;

import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.ReportMissionControl;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Management;
import org.mars_sim.tools.Msg;

/**
 * Meta task for the ReportMissionControl task.
 */
public class ReportMissionControlMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.reportMissionControl"); //$NON-NLS-1$
    
    public ReportMissionControlMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setTrait(TaskTrait.TEACHING, TaskTrait.LEADERSHIP);

	}

    @Override
    public Task constructInstance(Person person) {
        return new ReportMissionControl(person);
    }

	/**
	 * Assess if a Person is suitable to create a report for mission control.
	 * Assessment is based on the role type and the availability of a Management station.
	 * @param person Being assessed
	 * @return Rating 
	 */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {
        if (!person.isInside()
	        || !person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
            return EMPTY_TASKLIST;
		}
            
        RoleType roleType = person.getRole().getType();
		double base = switch(roleType) {
			case PRESIDENT -> 50D;
            case MAYOR -> 40D;
            case COMMANDER -> 30D;
            case SUB_COMMANDER -> 20D;
			default -> 0D;
		};
    	if (roleType.isChief()) {
			base = 10D;
		}
		
		// Not suitable then no
		if (base == 0D) {
			return EMPTY_TASKLIST;
		}
		var result = new RatingScore(base);
	            
	    // Get an available office space.
	    Building building = Management.getAvailableStation(person);
		assessBuildingSuitability(result, building, person);
		assessPersonSuitability(result, person);

        return createTaskJobs(result);
    }
}

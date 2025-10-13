/*
 * Mars Simulation Project
 * ReportMissionControlMeta.java
 * @date 2023-05-31
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.Management;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.ReportMissionControl;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.tool.Msg;

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
		setPreferredRole(RoleType.CREW_OPERATION_OFFICER);
	}

    @Override
    public Task constructInstance(Person person) {
        return new ReportMissionControl(person);
    }

	/**
	 * Assesses if a Person is suitable to create a report for mission control.
	 * Assessment is based on the role type and the availability of a Management station.
	 * 
	 * @param person Being assessed
	 * @return Rating 
	 */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {
        
    	RoleType roleType = person.getRole().getType();
    
        if (RoleType.GUEST == roleType || !person.isInside()
	        || !person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
            return EMPTY_TASKLIST;
		}

		double base = switch(roleType) {
			case PRESIDENT -> 70D;
            case MAYOR -> 60D;
            case ADMINISTRATOR -> 50D;
            case DEPUTY_ADMINISTRATOR -> 40D;
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

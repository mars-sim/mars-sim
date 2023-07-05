/*
 * Mars Simulation Project
 * ReportMissionControlMeta.java
 * @date 2023-05-31
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.ReportMissionControl;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Management;

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

    @Override
    public double getProbability(Person person) {

        double result = 0D;

    	// check if he has this meta task done
//    	if (!person.getPreference().isTaskDue(this)) {
    		
            if (person.isInside()) {
	        
                // Probability affected by the person's stress and fatigue.
                if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
                	return 0;
            
                RoleType roleType = person.getRole().getType();

                if (roleType.equals(RoleType.PRESIDENT))
                	result += 50D;
                
            	else if (roleType.equals(RoleType.MAYOR))
                	result -= 40D;
            			
            	else if (roleType.equals(RoleType.COMMANDER))
                    result += 30D;
            	
            	else if (roleType.equals(RoleType.SUB_COMMANDER))
            		result += 20D;
                
    	        else if (roleType.isChief())
	                result += 10D;
                
	            else
	            	result -= 30D;
	            
	            if (result <= 0) result = 0;
	            
	            // Get an available office space.
	            Building building = Management.getAvailableStation(person);

	            // Note: if an office space is not available such as in a vehicle, one can still write reports!
				result *= getBuildingModifier(building, person);

	            result *= getPersonModifier(person);
        	}

        return result;
    }
}

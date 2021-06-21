/**
 * Mars Simulation Project
 * ReportMissionControlMeta.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.ReportMissionControl;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Management;

/**
 * Meta task for the ReportMissionControl task.
 */
public class ReportMissionControlMeta extends MetaTask {

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
                
                else if (roleType.equals(RoleType.CHIEF_OF_AGRICULTURE)
                	|| roleType.equals(RoleType.CHIEF_OF_ENGINEERING)
                	|| roleType.equals(RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS)
                	|| roleType.equals(RoleType.CHIEF_OF_MISSION_PLANNING)
                	|| roleType.equals(RoleType.CHIEF_OF_SAFETY_N_HEALTH)
                	|| roleType.equals(RoleType.CHIEF_OF_SCIENCE)
                	|| roleType.equals(RoleType.CHIEF_OF_SUPPLY_N_RESOURCES)){
//	                	result = -5D;
                }
                
	            else
	            	result -= 30D;
	            
	            if (result <= 0) result = 0;
	            
	            // Get an available office space.
	            Building building = Management.getAvailableStation(person);

	            // Note: if an office space is not available such as in a vehicle, one can still write reports!
	            if (building != null) {
	                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
	                result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
	            }

	            result = applyPersonModifier(result, person);
        	}

        return result;
    }
}

/**
 * Mars Simulation Project
 * WriteReportMeta.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.WriteReport;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the WriteReport task.
 */
public class WriteReportMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.writeReport"); //$NON-NLS-1$
    
    public WriteReportMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setTrait(TaskTrait.TEACHING, TaskTrait.LEADERSHIP);

	}

    @Override
    public Task constructInstance(Person person) {
        return new WriteReport(person);
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
            
	            if (result > 0) {
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
	                
	                	result += 15D;
	                }
	                
		            else
		            	result += 10D;
	            }
	            
	            if (result <= 0) result = 0;
	            
	            // Get an available office space.
	            Building building = WriteReport.getAvailableOffice(person);

	            // Note: if an office space is not available such as in a vehicle, one can still write reports!
	            if (building != null) {
	                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
	                result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
	            }

	            result = applyPersonModifier(result, person);
        	}
//    	}
        
        //System.out.println("result : " + result);
        return result;
    }
}

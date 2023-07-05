/*
 * Mars Simulation Project
 * WriteReportMeta.java
 * @date 2021-09-27
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.WriteReport;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

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
                	result += 15D;
                
	            else
	            	result += 10D;
	            
	            if (result <= 0) result = 0;
	            
	            // Get an available office space.
	            Building building = BuildingManager.getAvailableAdminBuilding(person);

	            // Note: if an office space is not available such as in a vehicle, one can still write reports!
	            result *= getBuildingModifier(building, person);

	            result *= getPersonModifier(person);
        	}
//    	}
        
        return result;
    }
}

/*
 * Mars Simulation Project
 * DelegateWorktMeta.java
 * @date 2023-06-16
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.DelegateWork;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Administration;

/**
 * The meta task for delegating work.
 */
public class DelegateWorktMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.delegateWork"); //$NON-NLS-1$
    
    public DelegateWorktMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		setTrait(TaskTrait.ORGANIZATION, TaskTrait.LEADERSHIP);

	}

    @Override
    public Task constructInstance(Person person) {
        return new DelegateWork(person);
    }

    @Override
    public double getProbability(Person person) {

    	double result = 0D;

    	// May check if he has this meta task done, use if (!person.getPreference().isTaskDue(this)) {
    		
    	if (person.isInside()) {
        
            RoleType roleType = person.getRole().getType();

            if (roleType.isLeadership()) {
            	return 0;
            }
            	
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
            	return 0;
            
            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
            	return 0;
            
            if (result <= 0) result = 0;
            
            // Get an available office space.
            Building building = Administration.getAvailableOffice(person);

            // Note: if an office space is not available such as in a vehicle, he can still delegate work.
            result *= getBuildingModifier(building, person);

            result *= getPersonModifier(person);
    	}
        
        return result;
    }
}

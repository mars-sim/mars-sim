/**
 * Mars Simulation Project
 * TeachMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Collection;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Teach;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the Teach task.
 */
public class TeachMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.teach"); //$NON-NLS-1$

    public TeachMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		
		setTrait(TaskTrait.TEACHING);
	}

    @Override
    public Task constructInstance(Person person) {
        return new Teach(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInside()) {

            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 75, 750))
            	return 0;          

            // Find potential students.
            Collection<Person> potentialStudents = Teach.getBestStudents(person);
            if (potentialStudents.size() == 0)
            	return 0;

            else {

	            result = potentialStudents.size() * 20D;

	            if (person.isInVehicle()) {	
	    	        // Check if person is in a moving rover.
	    	        if (Vehicle.inMovingRover(person)) {
	    		        // the bonus for proposing scientific study inside a vehicle, 
	    	        	// rather than having nothing to do if a person is not driving
	    	        	result += 30;
	    	        } 	       
	    	        else
	    		        // the bonus for proposing scientific study inside a vehicle, 
	    	        	// rather than having nothing to do if a person is not driving
	    	        	result += 10;
	            }
	            
	            Person student = (Person) potentialStudents.toArray()[0];
                Building building = BuildingManager.getBuilding(student);

                if (building != null) {

                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person,
                            building);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, building);

                }
                
    	        // Add Preference modifier
    	        if (result > 0)
    	         	result = result + result * person.getPreference().getPreferenceScore(this)/5D;
    	    	
    	        if (result < 0) result = 0;

            }
        }


        return result;
    }
}

/*
 * Mars Simulation Project
 * TeachMeta.java
 * @date 2022-09-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Collection;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Teach;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the Teach task.
 */
public class TeachMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.teach"); //$NON-NLS-1$

	private static final int CAP = 1_000;
	
    public TeachMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		
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
            if (potentialStudents.isEmpty())
            	return 0;

            else {

	            result = potentialStudents.size() * 30.0;

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
	            
	            for (Person student : potentialStudents) {
	                Building building = BuildingManager.getBuilding(student);
	
					result *= getBuildingModifier(building, student);

	            }
	           
    	        // Add Preference modifier
    	        if (result > 0)
    	         	result = result + result * person.getPreference().getPreferenceScore(this)/5D;
    	    	
    	        if (result < 0) result = 0;

            }
        }

        if (result > CAP)
        	result = CAP;
        
        return result;
    }
    
    @Override
    public Task constructInstance(Robot robot) {
        return new Teach(robot);
    }

    @Override
    public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.isInSettlement()) {

            // Find potential students.
            Collection<Person> potentialStudents = Teach.getBestStudents(robot);
            if (potentialStudents.isEmpty())
            	return 0;

            else {

	            result = potentialStudents.size() * 15D;
	            
	            for (Person student : potentialStudents) {
	                Building building = BuildingManager.getBuilding(student);
	
	                if (building != null) {
	                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot,
	                            building);
	                }
	            }
	            
    	        if (result < 0) result = 0;
            }
        }

        if (result > CAP)
        	result = CAP;
        
        return result;
    }
}

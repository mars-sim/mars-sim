/*
 * Mars Simulation Project
 * ReviewJobReassignmentMeta.java
 * @date 2021-09-27
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.util.Assignment;
import org.mars_sim.msp.core.person.ai.job.util.AssignmentType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.ReviewJobReassignment;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The Meta task for the ReviewJobReassignment task.
 */
public class ReviewJobReassignmentMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.reviewJobReassignment"); //$NON-NLS-1$

    public static MarsClock marsClock;
    
    public ReviewJobReassignmentMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setTrait(TaskTrait.LEADERSHIP);

	}
    @Override
    public Task constructInstance(Person person) {
        return new ReviewJobReassignment(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;
    
        if (person.isInSettlement()) {

            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
            	return 0;
            
        	//if (roleType == null)
        	//NOTE: sometimes enum is null. sometimes it is NOT. why?
        	RoleType roleType = person.getRole().getType();

            if (roleType != null && (roleType.isCouncil()
        	        || roleType.isChief()     			
        			|| (roleType == RoleType.MISSION_SPECIALIST && person.getAssociatedSettlement().getNumCitizens() <= 4))) {

	        	    Iterator<Person> i = person.getAssociatedSettlement().getAllAssociatedPeople().iterator();
	                while (i.hasNext()) {
	                	// Get the job history of the candidate not the caller
	                    Person p = i.next();
	                    List<Assignment> list = p.getJobHistory().getJobAssignmentList();
	                    Assignment ja = list.get(list.size()-1);
	                    
	                    AssignmentType status = ja.getStatus();

	                    if (status != null && status == AssignmentType.PENDING) {

	                    	result += 100D;
	                    	
	                        if (person.isInVehicle()) {	
	                	        // Check if person is in a moving rover.
	                	        if (Vehicle.inMovingRover(person)) {
	                		        // the bonus inside a vehicle
	                	        	result += 30;
	                	        } 	       
	                	        else
	                		        // the bonus inside a vehicle
	                	        	result += 10;
	                        }
	                        
		                    RoleType role2 = p.getRole().getType();
		                    
		                    // Adjust the probability with penalty if approving his/her own job reassignment 
		                    if (roleType == RoleType.SUB_COMMANDER
			                    && role2 != null && role2 == RoleType.SUB_COMMANDER)
		    		            result /= 2D;

		                    else if (roleType == RoleType.COMMANDER
				                    && role2 != null && role2 == RoleType.COMMANDER)
		    		            result /= 2.5D;

		                    else if (roleType == RoleType.MAYOR
				                    && role2 != null && role2 == RoleType.MAYOR)
		                    	result /= 3D;

			                else if (roleType == RoleType.PRESIDENT
					                 && role2 != null && role2 == RoleType.PRESIDENT)
			    		        result /= 4D;
		                    
	                    	//result = result + result * preference / 10D ;
	                    	
	                    	// Add adjustment based on how many sol the request has since been submitted
                            if (marsClock == null)
                               marsClock = Simulation.instance().getMasterClock().getMarsClock();
                            // if the job assignment submitted date is > 1 sol
                            int sol = marsClock.getMissionSol();
                            int solRequest = ja.getSolSubmitted();
                            if (sol - solRequest == 1)
                                result += 50D;
                            else if (sol - solRequest == 2)
                                result += 100D;
                            else if (sol - solRequest == 3)
                                result += 150D;
                            else if (sol - solRequest > 3)
                                result += 200D;
	                    }
	                }
	                
	                if (result > 0D) {
	                    // Get an available office space.
	                    Building building = Administration.getAvailableOffice(person);
						result += 100D;
						result *= getBuildingModifier(building, person);

	                    result *= getPersonModifier(person);
	                }
                    
                    if (result < 0) {
                        result = 0;
                    }
            }
        }

        return result;
    }
}

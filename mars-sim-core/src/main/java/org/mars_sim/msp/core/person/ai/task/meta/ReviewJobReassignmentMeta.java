/**
 * Mars Simulation Project
 * ReviewJobReassignmentMeta.java
 * @version 3.1.0 2017-09-07
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.JobAssignment;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.ReviewJobReassignment;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The Meta task for the ReviewJobReassignment task.
 */
public class ReviewJobReassignmentMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.reviewJobReassignment"); //$NON-NLS-1$

    public static MarsClock marsClock;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new ReviewJobReassignment(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;
    
        if (person.isInside()) {

            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || stress > 50 || hunger > 500)
            	return 0;
            
        	//if (roleType == null)
        	//NOTE: sometimes enum is null. sometimes it is NOT. why?
        	RoleType roleType = person.getRole().getType();

            if (roleType != null && roleType == RoleType.PRESIDENT
                	|| roleType == RoleType.MAYOR
            		|| roleType == RoleType.COMMANDER
        			|| roleType == RoleType.SUB_COMMANDER
        			|| roleType == RoleType.CHIEF_OF_AGRICULTURE
           			|| roleType == RoleType.CHIEF_OF_ENGINEERING
           			|| roleType == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS
           			|| roleType == RoleType.CHIEF_OF_MISSION_PLANNING
           			|| roleType == RoleType.CHIEF_OF_SAFETY_N_HEALTH
           			|| roleType == RoleType.CHIEF_OF_SCIENCE
           			|| roleType == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES        			
        			|| (roleType == RoleType.MISSION_SPECIALIST && person.getAssociatedSettlement().getNumCitizens() <= 4)) {

	        	    Iterator<Person> i = person.getAssociatedSettlement().getAllAssociatedPeople().iterator();
	                while (i.hasNext()) {
	                    Person p = i.next();
	                    List<JobAssignment> list = person.getJobHistory().getJobAssignmentList();
	                    JobAssignment ja = list.get(list.size()-1);
	                    
	                    JobAssignmentType status = ja.getStatus();

	                    if (status != null && status == JobAssignmentType.PENDING) {

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
	                    if (building != null) {
	                        result += 100D;
	                        result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
	                        result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
	                    }

	                    // Modify if operation is the person's favorite activity.
	                    if (person.getFavorite().getFavoriteActivity() == FavoriteType.OPERATION) {
	                        result *= 1.5D;
	                    }

	                    if (result > 0)
	                        //result += result / 8D * person.getPreference().getPreferenceScore(this);
	                    	result = result + result * person.getPreference().getPreferenceScore(this)/5D;

	                    // Effort-driven task modifier.
	                    result *= person.getPerformanceRating();
	                }
                    
                    if (result < 0) {
                        result = 0;
                    }
            }
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}
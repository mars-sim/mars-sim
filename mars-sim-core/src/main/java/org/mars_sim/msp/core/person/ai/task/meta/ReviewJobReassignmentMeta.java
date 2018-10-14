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
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ai.job.JobAssignment;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.task.ReviewJobReassignment;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.time.MarsClock;

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

        	//if (roleType == null)
        	//NOTE: sometimes enum is null. sometimes it is NOT. why?
        	RoleType roleType = person.getRole().getType();

            if (roleType != null && roleType == RoleType.PRESIDENT
                	|| roleType == RoleType.MAYOR
            		|| roleType == RoleType.COMMANDER
        			|| roleType == RoleType.SUB_COMMANDER
        			|| (roleType == RoleType.MISSION_SPECIALIST && person.getAssociatedSettlement().getNumCitizens() <= 4)) {

//	            else if (roleType.equals(RoleType.CHIEF_OF_AGRICULTURE)
//            	|| roleType.equals(RoleType.CHIEF_OF_ENGINEERING)
//            	|| roleType.equals(RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS)
//            	|| roleType.equals(RoleType.CHIEF_OF_MISSION_PLANNING)
//            	|| roleType.equals(RoleType.CHIEF_OF_SAFETY_N_HEALTH)
//            	|| roleType.equals(RoleType.CHIEF_OF_SCIENCE)
//            	|| roleType.equals(RoleType.CHIEF_OF_SUPPLY) )
//            	result += 100D;

            	
	            // Probability affected by the person's stress and fatigue.
	            PhysicalCondition condition = person.getPhysicalCondition();
	            if (condition.getFatigue() < 1200D && condition.getStress() < 75D) {

		            result += 10D;

	        	    Iterator<Person> i = person.getAssociatedSettlement().getAllAssociatedPeople().iterator();
	                while (i.hasNext()) {
	                    Person p = i.next();

	                    RoleType role2 = p.getRole().getType();
	                    
	                    // TODO: should commander and sub-commander approve his/her own job reassignment ?
	                    if (roleType == RoleType.SUB_COMMANDER
		                    && role2 != null && role2 == RoleType.SUB_COMMANDER)
	    		            result -= 25D;

	                    else if (roleType == RoleType.COMMANDER
			                    && role2 != null && role2 == RoleType.COMMANDER)
	    		            result -= 50D;

	                    else if (roleType == RoleType.MAYOR
			                    && role2 != null && role2 == RoleType.MAYOR)
	                    	result -= 25D;

		                else if (roleType == RoleType.PRESIDENT
				                 && role2 != null && role2 == RoleType.PRESIDENT)
		    		        result -= 50D;
	                    
	                    List<JobAssignment> list = p.getJobHistory().getJobAssignmentList();
	                    JobAssignmentType status = list.get(list.size()-1).getStatus();

	                    if (status != null && status == JobAssignmentType.PENDING) {

	                    	result += 500D;
	                    	//result = result + result * preference / 10D ;
	                    	
	                    	// Add adjustment based on how many sol the request has since been submitted
                            if (marsClock == null)
                               marsClock = Simulation.instance().getMasterClock().getMarsClock();
                            // if the job assignment submitted date is > 1 sol
                            int sol = marsClock.getMissionSol();
                            int solRequest = list.get(list.size()-1).getSolSubmitted();
                            if (sol - solRequest == 1)
                                result += 500D;
                            else if (sol - solRequest == 2)
                                result += 1000D;
                            else if (sol - solRequest == 3)
                                result += 1500D;
                            else if (sol - solRequest > 3)
                                result += 2000D;
	                    }
	                }
	                
	                if (result > 0D) {
	                    // Get an available office space.
	                    Building building = Administration.getAvailableOffice(person);
	                    if (building != null) {
	                        result += 200D;
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

//                    if (result > 0) System.out.println("ReviewJobReassignmentMeta : probability is " + result);
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
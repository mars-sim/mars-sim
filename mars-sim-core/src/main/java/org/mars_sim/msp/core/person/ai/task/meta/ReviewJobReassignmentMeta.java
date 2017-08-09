/**
 * Mars Simulation Project
 * ReviewJobReassignmentMeta.java
 * @version 3.08 2015-10-08
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ai.job.JobAssignment;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.task.ReviewJobReassignment;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
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

    public RoleType roleType;

    public MarsClock marsClock;

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
        //System.out.println("ReviewJobReassignmentMeta : getProbability()");

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT
            	|| person.getLocationSituation() == LocationSituation.IN_VEHICLE) {

        	//if (roleType == null)
        	//NOTE: sometimes enum is null. sometimes it is NOT. why?
            roleType = person.getRole().getType();

            //System.out.println("ReviewJobReassignmentMeta " + person.getName() + " (" + roleType + ") checking in");

            if (roleType.equals(RoleType.PRESIDENT)
                	|| roleType.equals(RoleType.MAYOR)
            		|| roleType.equals(RoleType.COMMANDER)
        			|| roleType.equals(RoleType.SUB_COMMANDER) ) {

	            // Probability affected by the person's stress and fatigue.
	            PhysicalCondition condition = person.getPhysicalCondition();
	            if (condition.getFatigue() < 1200D && condition.getStress() < 75D) {
	                //System.out.println("ReviewJobReassignmentMeta : fatigue and stress within limits");

	                //System.out.println("ReviewJobReassignmentMeta "
	                //		+ person.getName() + " (" + roleType + ") : checking for any job reassignments");

		            	//result += 150D;
	/*
		            else if (roleType.equals(RoleType.CHIEF_OF_AGRICULTURE)
		            	|| roleType.equals(RoleType.CHIEF_OF_ENGINEERING)
		            	|| roleType.equals(RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS)
		            	|| roleType.equals(RoleType.CHIEF_OF_MISSION_PLANNING)
		            	|| roleType.equals(RoleType.CHIEF_OF_SAFETY_N_HEALTH)
		            	|| roleType.equals(RoleType.CHIEF_OF_SCIENCE)
		            	|| roleType.equals(RoleType.CHIEF_OF_SUPPLY) )
		            	result += 100D;
	*/

		       	    // Get highest person skill level.
	        	    Iterator<Person> i = person.getAssociatedSettlement().getAllAssociatedPeople().iterator();
	                while (i.hasNext()) {
	                    Person tempPerson = i.next();

	                    // commander and sub-commander should not approved his/her own job reassignment
	                    if (roleType.equals(RoleType.SUB_COMMANDER)
		                    && tempPerson.getRole().getType().equals(RoleType.SUB_COMMANDER))
	                    	; // do nothing

	                    else if (roleType.equals(RoleType.COMMANDER)
			                    && tempPerson.getRole().getType().equals(RoleType.COMMANDER))
		                    	; // do nothing

	                    else {

		                    List<JobAssignment> list = tempPerson.getJobHistory().getJobAssignmentList();
		                    JobAssignmentType status = list.get(list.size()-1).getStatus();

		                    if (status != null) {
			                    if (status.equals(JobAssignmentType.PENDING)) {
			    	                //System.out.println("ReviewJobReassignmentMeta : "
			    	                //		+ person.getName() + " (" + roleType
			    	                //		+ ") : found a pending job reassignment request");
			                    	result += 500D;
			                    	//result = result + result * preference / 10D ;
			                    	
			                    	// 2015-09-24 Added adjustment based on how many sol the request has since been submitted
		                            if (marsClock == null)
		                               marsClock = Simulation.instance().getMasterClock().getMarsClock();
		                            // if the job assignment submitted date is > 1 sol
		                            int sol = marsClock.getSolElapsedFromStart();
		                            int solRequest = list.get(list.size()-1).getSolSubmitted();
		                            if (sol == solRequest+1)
		                                result += 1000D;
		                            else if (sol == solRequest+2)
		                                result += 1500D;
		                            else if (sol == solRequest+3)
		                                result += 2000D;
		                            else if (sol > solRequest+3)
		                                result += 3000D;
			                    }
		                    }
	                    }
	                }
	                
	                if (result > 0D) {
	                    // Get an available office space.
	                    Building building = ReviewJobReassignment.getAvailableOffice(person);
	                    if (building != null) {
	                        result += 200D;
	                        result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
	                        result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
	                    }

	                    // Modify if working out is the person's favorite activity.
	                    if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Administration")) {
	                        result *= 1.5D;
	                    }

	                    // 2015-06-07 Added Preference modifier
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
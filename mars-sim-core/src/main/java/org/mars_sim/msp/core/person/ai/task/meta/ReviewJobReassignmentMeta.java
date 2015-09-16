/**
 * Mars Simulation Project
 * ReviewJobReassignmentMeta.java
 * @version 3.08 2015-06-15
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ai.job.JobAssignment;
import org.mars_sim.msp.core.person.ai.task.ReviewJobReassignment;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Meta task for the WriteReport task.
 */
public class ReviewJobReassignmentMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.reviewJobReassignment"); //$NON-NLS-1$

    public RoleType roleType;

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
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

        	if (roleType == null)
            	roleType = person.getRole().getType();

            if (roleType.equals(RoleType.PRESIDENT)
                	|| roleType.equals(RoleType.MAYOR)
            		|| roleType.equals(RoleType.COMMANDER)
        			|| roleType.equals(RoleType.SUB_COMMANDER) ) {

	            // Probability affected by the person's stress and fatigue.
	            PhysicalCondition condition = person.getPhysicalCondition();
	            if (condition.getFatigue() < 1200D && condition.getStress() < 75D) {
	                //System.out.println("ReviewJobReassignmentMeta's little fatigue and stress");


	                //System.out.println("ReviewJobReassignmentMeta's roleType : " + roleType);

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

	            	double preference = person.getPreference().getPreferenceScore(this);
		       	    // Get highest person skill level.
	        	    Iterator<Person> i = person.getSettlement().getAllAssociatedPeople().iterator();
	                while (i.hasNext()) {
	                    Person tempPerson = i.next();
	                    List<JobAssignment> list = tempPerson.getJobHistory().getJobAssignmentList();
	                    String status = list.get(list.size()-1).getStatus();

	                    if (status != null)
		                    if (status.equals("Pending")) {
		                    	//System.out.println("ReviewJobReassignmentMeta status equals pending");
		                    	result += 600D;
		                    	result = result + result * preference / 10D ;
		                    }
	                    //if (result > 0) System.out.println("ReviewJobReassignmentMeta's result : " + result);
	    	            // Note: if an office space is not available, one can still write reports
	                }

    	            // Get an available office space.
    	            Building building = ReviewJobReassignment.getAvailableOffice(person);
    	            //result += 200D;

    	            if (building != null) {
    	                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
    	                result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
    	            }

    	            // Modify if working out is the person's favorite activity.
    	            if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Administration")) {
    	                result *= 1.5D;
    	            }

		            // Effort-driven task modifier.
		            result *= person.getPerformanceRating();

			        // 2015-06-07 Added Preference modifier
			        //if (result > 0)
			        //	result += result / 4D * person.getPreference().getPreferenceScore(this);
			        //if (result < 0) result = 0;
	            }
            }
        }
        //if (result > 0) System.out.println("ReviewJobReassignmentMeta's result is " + result);
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
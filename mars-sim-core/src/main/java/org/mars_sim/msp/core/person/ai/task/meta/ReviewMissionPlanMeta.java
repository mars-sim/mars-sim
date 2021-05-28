/**
 * Mars Simulation Project
 * ReviewMissionPlanMeta.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionPlanning;
import org.mars_sim.msp.core.person.ai.mission.PlanType;
import org.mars_sim.msp.core.person.ai.task.ReviewMissionPlan;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The Meta task for the ReviewMissionPlan task.
 */
public class ReviewMissionPlanMeta extends MetaTask {
		
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.reviewMissionPlan"); //$NON-NLS-1$
    
    public ReviewMissionPlanMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setTrait(TaskTrait.LEADERSHIP);

	}

    @Override
    public Task constructInstance(Person person) {
        return new ReviewMissionPlan(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;
        
        if (person.isInside()) {

            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 75, 750))
            	return 0;
            
        	//if (roleType == null)
        	//NOTE: sometimes enum is null. sometimes it is NOT. why?
//        	RoleType roleType = person.getRole().getType();

        	int pop = person.getAssociatedSettlement().getNumCitizens();
//			if (pop <= 4		
//				|| (pop <= 8 && roleType == RoleType.RESOURCE_SPECIALIST)
//				|| ReviewMissionPlan.isRoleValid(roleType)) {
//        	System.out.println("missionManager :" + missionManager); 
        	MissionManager missionManager = Simulation.instance().getMissionManager();
            List<Mission> missions = missionManager.getPendingMissions(person.getAssociatedSettlement());
//   		    if (missions.size() > 0)
//   		    	System.out.println(person + " " + person.getRole().getType() + " has " + missions.size() + " to review.");

            for (Mission m : missions) {
            	
            	if (m.getPlan() != null) {

            		MissionPlanning mp = m.getPlan();
            		
                    PlanType status = mp.getStatus();

                    if (status != null && status == PlanType.PENDING) {
//	                    	&& mp.getPercentComplete() <= 100D) {
    		            		
                    	result += missions.size() * 1500D / pop;   
                    	
//	                    	System.out.println(person + " " + person.getRole().getType() + " on " 
//	                    			+ mp.getMission().getDescription() + " has " 
//	                    			+ mp.getPercentComplete() + "%");

						String reviewedBy = person.getName();
						
						Person p = m.getStartingMember();
						String requestedBy = p.getName();
						
						if (reviewedBy.equals(requestedBy) || !mp.isReviewerValid(reviewedBy, pop)) {
							// Add penalty to the probability score if reviewer is the same as requester
							return 0;
						}
                    	
                        
                    	// Add adjustment based on how many sol the request has since been submitted
                        // if the job assignment submitted date is > 1 sol
                        int sol = marsClock.getMissionSol();
                        int solRequest = m.getPlan().getMissionSol();
                        
                    	// Check if this reviewer has already exceeded the max # of reviews allowed
						if (!mp.isReviewerValid(reviewedBy, pop)) {
							if (sol - solRequest > 7) {
								// If no one else is able to offer the review after x days, 
								// do allow the review to go through even if the reviewer is not valid
								result += 800;
							}
							else
								result += (sol - solRequest) * 100;
						}                        
                    }
            	}
            }
            
            if (result > 0D) {
            	 
                // Get an available office space.
                Building building = Administration.getAvailableOffice(person);
                if (building != null) {
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
                }
                else if (person.isInVehicle()) {	
        	        // Check if person is in a moving rover.
        	        if (Vehicle.inMovingRover(person)) {
        		        // the bonus for proposing scientific study inside a vehicle, 
        	        	// rather than having nothing to do if a person is not driving
        	        	result += 40;
        	        } 	       
        	        else
        		        // the bonus for proposing scientific study inside a vehicle, 
        	        	// rather than having nothing to do if a person is not driving
        	        	result += 10;
                }

                result = applyPersonModifier(result, person);
            }
            
            if (result < 0) {
                result = 0;
            }
        }

//        if (result > 0) 
//        	logger.info(person + " (" + person.getRole().getType() + ") had a probability score of " 
//        			+ result + " at ReviewMissionPlanMeta");

        return result;
    }
}

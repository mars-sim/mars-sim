/**
 * Mars Simulation Project
 * ReviewMissionPlanMeta.java
 * @version 3.1.0 2018-10-10
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionPlanning;
import org.mars_sim.msp.core.person.ai.mission.PlanType;
import org.mars_sim.msp.core.person.ai.task.ReviewMissionPlan;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The Meta task for the ReviewMissionPlan task.
 */
public class ReviewMissionPlanMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	private static transient Logger logger = Logger.getLogger(ReviewMissionPlanMeta.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.reviewMissionPlan"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
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
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || stress > 75 || hunger > 750)
            	return 0;
            
        	//if (roleType == null)
        	//NOTE: sometimes enum is null. sometimes it is NOT. why?
//        	RoleType roleType = person.getRole().getType();

        	int pop = person.getAssociatedSettlement().getNumCitizens();
//			if (pop <= 4		
//				|| (pop <= 8 && roleType == RoleType.RESOURCE_SPECIALIST)
//				|| ReviewMissionPlan.isRoleValid(roleType)) {
//        	System.out.println("missionManager :" + missionManager); 
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

//        if (result > 0) 
//        	logger.info(person + " (" + person.getRole().getType() + ") had a probability score of " 
//        			+ result + " at ReviewMissionPlanMeta");

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
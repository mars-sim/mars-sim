/**
 * Mars Simulation Project
 * ReviewMissionPlanMeta.java
 * @version 3.1.0 2018-10-10
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionPlanning;
import org.mars_sim.msp.core.person.ai.mission.PlanType;
import org.mars_sim.msp.core.person.ai.task.ReviewMissionPlan;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The Meta task for the ReviewMissionPlan task.
 */
public class ReviewMissionPlanMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    
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
        	RoleType roleType = person.getRole().getType();

        	int pop = person.getAssociatedSettlement().getNumCitizens();
			if (pop <= 4		
				|| (pop <= 8 && roleType == RoleType.RESOURCE_SPECIALIST)
				|| ReviewMissionPlan.isRoleValid(roleType)) {

                List<Mission> missions = missionManager.getPendingMissions(person.getAssociatedSettlement());

                for (Mission m : missions) {
                	
                	if (m.getPlan() != null) {
                		
                		MissionPlanning mp = m.getPlan();
                		
	                    PlanType status = mp.getStatus();

	                    if (status != null && status == PlanType.PENDING 
	                    		&& mp.getPercentComplete() < 100D) {
	    		            	
	    						String reviewedBy = person.getName();
	    						
	    						Person p = m.getStartingMember();
	    						String requestedBy = p.getName();
						
	    						if (!mp.isValidReview(reviewedBy, pop)) {
	    							return 0;
	    						}
	    						
	    						if (reviewedBy.equals(requestedBy)) {
	    							// Add penalty to the probability score if reviewer is the same as requester
	    							result -= 50D;;
	    						}
	    						
	                    	result += 300D;                    	
	                    	// Add adjustment based on how many sol the request has since been submitted
                            // if the job assignment submitted date is > 1 sol
                            int sol = marsClock.getMissionSol();
                            int solRequest = m.getPlan().getMissionSol();
                            if (sol - solRequest == 1)
                                result += 200D;
                            else if (sol - solRequest == 2)
                                result += 3000D;
                            else if (sol - solRequest == 3)
                                result += 400D;
                            else if (sol - solRequest > 3)
                                result += 500D;
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

//                if (result > 0) System.out.println(person + " ReviewMissionPlanMeta : probability is " + result);
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
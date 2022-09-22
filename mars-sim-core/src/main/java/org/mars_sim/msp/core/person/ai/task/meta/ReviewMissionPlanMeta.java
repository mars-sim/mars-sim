/**
 * Mars Simulation Project
 * ReviewMissionPlanMeta.java
 * @version 3.2.0 2021-06-20
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
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.ReviewMissionPlan;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.Settlement;
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
        
        if (person.isInSettlement()) {

            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
            	return 0;
            

        	Settlement target = person.getAssociatedSettlement();
			int pop = target.getNumCitizens();

        	MissionManager missionManager = Simulation.instance().getMissionManager();
            List<Mission> missions = missionManager.getPendingMissions(target);

            for (Mission m : missions) {
            	
            	if (m.getPlan() != null) {

            		MissionPlanning mp = m.getPlan();
                    if (mp.getStatus() == PlanType.PENDING) {
						String reviewedBy = person.getName();
						
						Person p = m.getStartingPerson();
						String requestedBy = p.getName();
						
						if (reviewedBy.equals(requestedBy) || !mp.isReviewerValid(reviewedBy, pop)) {
							// Skip this plan if the request and review is the same person
							// Also, reviewer must be valid
							continue;
						}

                    	result += missions.size() * 1500D / pop;                       	
                        
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
            	RoleType roleType = person.getRole().getType();
            	
            	if (RoleType.MISSION_SPECIALIST == roleType)
            		result *= 1.5;
            	else if (RoleType.CHIEF_OF_MISSION_PLANNING == roleType)
            		result *= 3;
            	else if (RoleType.SUB_COMMANDER == roleType)
            		result *= 4.5;
            	else if (RoleType.COMMANDER == roleType)
            		result *= 6;
            	
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

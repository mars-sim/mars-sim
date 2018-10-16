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
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
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

    public static MarsClock marsClock;
	private static MissionManager missionManager;

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

        	//if (roleType == null)
        	//NOTE: sometimes enum is null. sometimes it is NOT. why?
        	RoleType roleType = person.getRole().getType();

            if (roleType == RoleType.PRESIDENT
                	|| roleType == RoleType.MAYOR
            		|| roleType == RoleType.COMMANDER
        			|| roleType == RoleType.SUB_COMMANDER
        			|| roleType == RoleType.CHIEF_OF_MISSION_PLANNING
        			|| (roleType == RoleType.MISSION_SPECIALIST && person.getAssociatedSettlement().getNumCitizens() <= 8)) {

//		            else if (roleType.equals(RoleType.CHIEF_OF_AGRICULTURE)
//		            	|| roleType.equals(RoleType.CHIEF_OF_ENGINEERING)
//		            	|| roleType.equals(RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS)
//		            	|| roleType.equals(RoleType.CHIEF_OF_MISSION_PLANNING)
//		            	|| roleType.equals(RoleType.CHIEF_OF_SAFETY_N_HEALTH)
//		            	|| roleType.equals(RoleType.CHIEF_OF_SCIENCE)
//		            	|| roleType.equals(RoleType.CHIEF_OF_SUPPLY) )
//		            	result += 100D;

	       	    // Get highest person skill level.
//	        	    Iterator<Person> i = person.getSettlement().getAllAssociatedPeople().iterator();
//	                while (i.hasNext()) {
//	                    Person p = i.next();
//
//	                    if (roleType.equals(RoleType.CHIEF_OF_MISSION_PLANNING)
//	                    	&& p.getRole().getType().equals(RoleType.CHIEF_OF_MISSION_PLANNING)) {
//	                    	result -= 50D;
//	                    }
//	                    // TODO: should commander and sub-commander approve his/her own job reassignment ?
//	                    else if (roleType.equals(RoleType.SUB_COMMANDER)
//		                    && p.getRole().getType().equals(RoleType.SUB_COMMANDER))
//	    		            result -= 50D;
//
//	                    else if (roleType.equals(RoleType.COMMANDER)
//			                    && p.getRole().getType().equals(RoleType.COMMANDER))
//	    		            result -= 50D;
//
//	                }
                
                if (missionManager == null)
                	missionManager = Simulation.instance().getMissionManager();
                
                List<Mission> missions = missionManager.getPendingMissions(person.getAssociatedSettlement());

                for (Mission m : missions) {
                	
                	if (m.getPlan() != null) {
	                    PlanType status = m.getPlan().getStatus();

	                    if (status != null && status == PlanType.PENDING) {
	                    	result += 200D;                    	
	                    	// Add adjustment based on how many sol the request has since been submitted
                            if (marsClock == null)
                               marsClock = Simulation.instance().getMasterClock().getMarsClock();
                            // if the job assignment submitted date is > 1 sol
                            int sol = marsClock.getMissionSol();
                            int solRequest = m.getPlan().getMissionSol();
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
                }
                
                if (result > 0D) {
                	
    	            // Probability affected by the person's stress and fatigue.
//	    	            PhysicalCondition condition = person.getPhysicalCondition();
//	    	            if (condition.getFatigue() < 1200D && condition.getStress() < 75D) {
//	    		            result += 50D;
//	    	            }
    	            
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
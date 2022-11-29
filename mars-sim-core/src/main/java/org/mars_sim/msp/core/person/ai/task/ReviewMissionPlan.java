/*
 * Mars Simulation Project
 * ReviewMissionPlan.java
 * @date 2022-09-28
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.util.JobAssignment;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionPlanning;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.PlanType;
import org.mars_sim.msp.core.person.ai.mission.SiteMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.social.RelationshipUtil;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.structure.ObjectiveType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Management;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * This class is a task for reviewing mission plans
 */
public class ReviewMissionPlan extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final SimLogger logger = SimLogger.getLogger(ReviewMissionPlan.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.reviewMissionPlan"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase REVIEWING = new TaskPhase(
			Msg.getString("Task.phase.reviewMissionPlan.reviewing")); //$NON-NLS-1$

	private static final TaskPhase APPROVING = new TaskPhase(Msg.getString("Task.phase.reviewMissionPlan.approving")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.1D;

	// Mapping of the preferred MissionType for each Objective
	private static final Map<ObjectiveType, Set<MissionType>> OBJECTIVE_TO_MISSION = new EnumMap<>(ObjectiveType.class);
	
	static {
		OBJECTIVE_TO_MISSION.put(ObjectiveType.BUILDERS_HAVEN,
				   Set.of(MissionType.COLLECT_REGOLITH,
						  MissionType.MINING));
		OBJECTIVE_TO_MISSION.put(ObjectiveType.CROP_FARM,
							   Set.of(MissionType.COLLECT_ICE,
									  MissionType.BIOLOGY));
		OBJECTIVE_TO_MISSION.put(ObjectiveType.TOURISM,
							   Set.of(MissionType.AREOLOGY,
									  MissionType.BIOLOGY,
									  MissionType.EXPLORATION,
									  MissionType.METEOROLOGY,
									  MissionType.TRAVEL_TO_SETTLEMENT));
		OBJECTIVE_TO_MISSION.put(ObjectiveType.TRADE_CENTER,
								Set.of(MissionType.TRADE, 
										MissionType.DELIVERY));
		OBJECTIVE_TO_MISSION.put(ObjectiveType.TRANSPORTATION_HUB,
								Set.of(MissionType.TRAVEL_TO_SETTLEMENT,
									   MissionType.EXPLORATION));
		OBJECTIVE_TO_MISSION.put(ObjectiveType.MANUFACTURING_DEPOT,
								Set.of(MissionType.MINING,
									   MissionType.COLLECT_REGOLITH));
	}

	
	// Data members
	/** The administration building the person is using. */
	private Administration office;
    
	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 */
	public ReviewMissionPlan(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, SkillType.MANAGEMENT,
				RandomUtil.getRandomInt(20, 40), RandomUtil.getRandomInt(40, 80));
				
		if (person.isInSettlement()) {

			List<Mission> missions = missionManager.getPendingMissions(person.getAssociatedSettlement());
	        
			if (missions.size() == 0)
				endTask();

			// If person is in a settlement, try to find an office building.
			Building officeBuilding = Administration.getAvailableOffice(person);

			// Note: office building is optional
			if (officeBuilding != null) {
				office = officeBuilding.getAdministration();	
				if (!office.isFull()) {
					office.addStaff();
					// Walk to the office building.
					walkToTaskSpecificActivitySpotInBuilding(officeBuilding, FunctionType.ADMINISTRATION, true);
				}
			}

			else {
				Building managementBuilding = Management.getAvailableStation(person);
				if (managementBuilding != null) {
					// Walk to the management building.
					walkToTaskSpecificActivitySpotInBuilding(managementBuilding, FunctionType.MANAGEMENT, true);
				}
				else {	
					Building dining = BuildingManager.getAvailableDiningBuilding(person, false);
					// Note: dining building is optional
					if (dining != null) {
						// Walk to the dining building.
						walkToTaskSpecificActivitySpotInBuilding(dining, FunctionType.DINING, true);
					}
				}
			}
		}
		else {
			endTask();
		}

		// Initialize phase
		addPhase(REVIEWING);
		addPhase(APPROVING);
		
		setPhase(REVIEWING);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (REVIEWING.equals(getPhase())) {
			return reviewingPhase(time);
		} else if (APPROVING.equals(getPhase())) {
			return approvingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the reviewing phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double reviewingPhase(double time) {
    	Settlement reviewerSettlement = person.getAssociatedSettlement();

        List<Mission> missions = missionManager.getPendingMissions(reviewerSettlement);
        
        if (missions.isEmpty())
        	endTask();
            
		// Iterates through each pending mission 
		for (Mission m : missions) {
			MissionPlanning mp = m.getPlan();
			
        	if (m.getPlan() == null) {
        		// Go to next mission
        		continue;
        	}

            PlanType status = mp.getStatus();

            if (status != null && status == PlanType.PENDING) {
	            if (mp.getPercentComplete() >= 100) {
	            	// Go to the finished phase and finalize the approval
	            	setPhase(APPROVING);
	            	
	                return time / 4.0; // return time is needed
	            }
	            
	            else if (mp.getPercentComplete() >= 60) {
			    	int sol = marsClock.getMissionSol();
			    	int solRequest = m.getPlan().getMissionSol();
			    	if (sol - solRequest > 7) {
			    		// If no one else is able to offer the review after x days, 
			    		// do allow the review to go through even if the reviewer is not valid
			        	setPhase(APPROVING);
			        	
			            return time / 4.0;
			    	}
			    }

            	// if not 100% reviewed
				Person p = m.getStartingPerson();
				String reviewedBy = person.getName();
				
				String requestedBy = p.getName();

				if (!reviewedBy.equals(requestedBy)
						&& mp.isReviewerValid(reviewedBy, reviewerSettlement.getNumCitizens())) {
					
				    // Simulate the person has just spent 90% duration for this task
				    // Now a mission score will be calculated
				    if (isAlmostTimeCompleted()) {
				    	
						// 9. reviewer role weight
						RoleType role = person.getRole().getType();
						int reviewerRole = 0;
						
						if (role == RoleType.PRESIDENT
								|| role == RoleType.MAYOR
								|| role == RoleType.COMMANDER
								|| role == RoleType.SUB_COMMANDER)
					        // Perform the executive review 
					    	completeExecutiveReview(m, p, reviewerSettlement, mp);
						else
							// Perform the general review 
							completeReview(m, p, reviewerSettlement, mp);
				    		
						logger.log(worker, Level.INFO, 0, "Done reviewing " + requestedBy
										+ "'s " + m.getName() + " mission plan.");
						// Add experience
						addExperience(time);
						
						endTask();
						
						return 0;
				    }
				    else {
				    	logger.log(worker, Level.INFO, 20_000, "Reviewing " + requestedBy
								+ "'s " + m.getName() + " mission plan.");
				    }
				}
			} // if (status != null && status == PlanType.PENDING)
		} // end of while
		
        return 0;
	}

	/**
	 * Completes the executive review.
	 * 
	 * @param m
	 * @param p
	 * @param reviewerSettlement
	 * @param mp
	 */
	private void completeExecutiveReview(Mission m, Person p, Settlement reviewerSettlement, MissionPlanning mp) {
		String requestedBy = p.getName();
		
	    GoodsManager goodsManager = reviewerSettlement.getGoodsManager();
	    
    	MissionType mt = m.getMissionType();
    	
		List<JobAssignment> list = p.getJobHistory().getJobAssignmentList();
		int last = list.size() - 1;
		
		// 1. Reviews requester's cumulative job rating
		double rating = list.get(last).getJobRating();
		double cumulative_rating = 0;
		int size = list.size();
		for (int j = 0; j < size; j++) {
			cumulative_rating += list.get(j).getJobRating();
		}
		cumulative_rating = cumulative_rating / size;

		rating = (rating + cumulative_rating);
				
		// 2. Relationship Score 
		
		// 2a. Reviewer's view of the mission lead
		double relationshipWithReviewer = RelationshipUtil.getOpinionOfPerson(person, p);
			
		double relationshipWithOthers = 0;
		int num = reviewerSettlement.getAllAssociatedPeople().size();
		for (Person pp : reviewerSettlement.getAllAssociatedPeople()) {
			relationshipWithOthers += RelationshipUtil.getOpinionOfPerson(person, pp);
		}
		
		// 2b. Others' view of the mission lead
		relationshipWithOthers = (int)(relationshipWithOthers / num);
		
		int relation = (int)((relationshipWithReviewer + relationshipWithOthers) / 10D) ;
		
		// 3. Mission Qualification Score
		double qual = 0;
		
		switch (mt) {
		case AREOLOGY :
		case BIOLOGY:
		case METEOROLOGY:
		case RESCUE_SALVAGE_VEHICLE:
		case TRAVEL_TO_SETTLEMENT:
			qual = .5;
			break;

		case COLLECT_REGOLITH:
			qual = .15;
			break;
			
		case COLLECT_ICE:
		case EXPLORATION:
			qual = .25;
			break;

		case MINING:
			qual = .3;
			break;
			
		case TRADE:
			qual = .35;
			break;
			
		default:
			qual = .4;
    	}
    
		qual = qual * m.getMissionQualification(person);
		qual = 2.5 * Math.round(qual * 10.0)/10.0;
		
		// 4. Settlement objective score, is the Mission type
		// is preferred for the Objective
		double obj = 0;
		ObjectiveType objective = reviewerSettlement.getObjective();
		if (OBJECTIVE_TO_MISSION.getOrDefault(objective, Collections.emptySet()).contains(mt)) {
			switch (objective) {
			case BUILDERS_HAVEN:
				obj += 5D * goodsManager.getBuildersFactor(); 
				break;
				
			case CROP_FARM:
				obj += 5D * goodsManager.getCropFarmFactor();
				break;
			
			case TOURISM:
				obj += 5D * goodsManager.getTourismFactor();
				break;
			
			case TRADE_CENTER:
				obj += 5D * goodsManager.getTradeFactor();
				break;
			
			case TRANSPORTATION_HUB:
				obj += 5D * goodsManager.getTransportationFactor();
				break;
			
			case MANUFACTURING_DEPOT:
				obj += 5D * goodsManager.getManufacturingFactor();
				break;
			default:
				break;
			}
		}

		// 5. emergency
		int emer = 0;
		if ((mt == MissionType.EMERGENCY_SUPPLY)
				|| (mt == MissionType.RESCUE_SALVAGE_VEHICLE)) {
			emer = 50;
		}	
		
		// 6. Site Value
		double siteValue = 0;
		if (m instanceof SiteMission) {
			siteValue = ((SiteMission)m).getTotalSiteScore(reviewerSettlement);
			
			// Why do we adjust these score ?
			if (mt == MissionType.COLLECT_ICE) {
				siteValue /= 40D;
			}
			else if (mt == MissionType.COLLECT_REGOLITH) {
				siteValue /= 30D;
			}
			else if (mt == MissionType.MINING) {
				siteValue /= 20D;
			}
			else if (mt == MissionType.EXPLORATION) {
				siteValue /= 20D;
			}
		}

		// 7. proposed route distance (note that a negative score represents a penalty)
		int dist = 0;
		if (m instanceof VehicleMission) {
			int range = m.getAssociatedSettlement().getMissionRadius(mt);
			if (range > 0) {
				int proposed = (int)(((VehicleMission) m).getDistanceProposed());
				
				// Scoring rule:
				// At range = 0, the score is 10
				// The half the range, the score is -40

				// Calculate the dist score
				dist = (int)(- 100.0 / range * proposed + 10);
			}
		}
		
		// 8. Leadership and Charisma
		NaturalAttributeManager attrMgr = person.getNaturalAttributeManager();
		int leadership = (int)(.075 * attrMgr
							.getAttribute(NaturalAttributeType.LEADERSHIP)
						+ .025 * attrMgr
							.getAttribute(NaturalAttributeType.ATTRACTIVENESS));				

		// 9. reviewer role weight
		RoleType role = person.getRole().getType();
		int reviewerRole = 0;
		
		if (role == RoleType.PRESIDENT)
			reviewerRole = 20;
		else if (role == RoleType.MAYOR)
			reviewerRole = 15;
		else if (role == RoleType.COMMANDER)
			reviewerRole = 10;
		else if (role == RoleType.SUB_COMMANDER)
			reviewerRole = 8;
		else if (role == RoleType.CHIEF_OF_MISSION_PLANNING)
			reviewerRole = 7;
		else if (role == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS)
			reviewerRole = 6;
		else if (role.isChief())
			reviewerRole = 5;
		else if (role == RoleType.MISSION_SPECIALIST)
			reviewerRole = 4;
		else
			reviewerRole = 2;
		
		
		// 10. luck
		int luck = RandomUtil.getRandomInt(-5, 5);	
		
		// Future: 9. Go to him/her to have a chat
		// Future: 10. mission lead's leadership/charisma
		
		double score = Math.round((rating + relation + qual + obj + emer + siteValue + dist + leadership + reviewerRole + luck)* 10.0)/10.0;

		// Updates the mission plan status
		missionManager.scoreMissionPlan(mp, score, person);

		StringBuilder msg = new StringBuilder();
		msg.append("Grading ").append(requestedBy).append("'s ").append(m.getName());
		msg.append(" plan -");
		msg.append(" Rating: ").append(rating); 
		msg.append(", Rels: ").append(relation); 
		msg.append(", Quals: ").append(qual); 
		msg.append(", Obj: ").append(obj);
		msg.append(", Emer: ").append(emer);
		msg.append(", Site: ").append(Math.round(siteValue*10.0)/10.0);
		msg.append(", Dist: ").append(dist);
		msg.append(", Lead: ").append(leadership); 							
		msg.append(", Review: ").append(reviewerRole); 
		msg.append(", Luck: ").append(luck); 
		msg.append(" = Subtotal: ").append(score);
		
		logger.log(worker, Level.INFO, 0,  msg.toString());
	}
	
	
	/**
	 * Completes the general review.
	 * 
	 * @param m
	 * @param p
	 * @param reviewerSettlement
	 * @param mp
	 */
	private void completeReview(Mission m, Person p, Settlement reviewerSettlement, MissionPlanning mp) {
		String requestedBy = p.getName();
  
    	MissionType mt = m.getMissionType();
    	
		// 2. Relationship Score 
		
		// 2a. Reviewer's view of the mission lead
		double relationshipWithReviewer = RelationshipUtil.getOpinionOfPerson(person, p);
			
		double relationshipWithOthers = 0;
		int num = reviewerSettlement.getAllAssociatedPeople().size();
		for (Person pp : reviewerSettlement.getAllAssociatedPeople()) {
			relationshipWithOthers += RelationshipUtil.getOpinionOfPerson(person, pp);
		}
		
		// 2b. Others' view of the mission lead
		relationshipWithOthers = (int)(relationshipWithOthers / num);
		
		int relation = (int)((relationshipWithReviewer + relationshipWithOthers) / 10D) ;
		
		// 6. Site Value
		double siteValue = 0;
		if (m instanceof SiteMission) {
			siteValue = ((SiteMission)m).getTotalSiteScore(reviewerSettlement);
			
			// Why do we adjust these score ?
			if (mt == MissionType.COLLECT_ICE) {
				siteValue /= 10D;
			}
			else if (mt == MissionType.COLLECT_REGOLITH) {
				siteValue /= 7.5;
			}
			else if (mt == MissionType.MINING) {
				siteValue /= 5D;
			}
			else if (mt == MissionType.EXPLORATION) {
				siteValue /= 5D;
			}
		}

		// 9. reviewer role weight
		RoleType role = person.getRole().getType();
		int reviewerRole = 0;
		
		if (role == RoleType.CHIEF_OF_MISSION_PLANNING)
			reviewerRole = 7;
		else if (role == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS)
			reviewerRole = 6;
		else if (role.isChief())
			reviewerRole = 5;
		else if (role == RoleType.MISSION_SPECIALIST)
			reviewerRole = 4;
		else
			reviewerRole = 2;
		
		
		double score = Math.round((relation + siteValue + reviewerRole)* 10.0)/10.0;

		// Updates the mission plan status
		missionManager.scoreMissionPlan(mp, score, person);

		StringBuilder msg = new StringBuilder();
		msg.append("Grading ").append(requestedBy).append("'s ").append(m.getName());
		msg.append(" plan -");
		msg.append(" Rels: ").append(relation); 
		msg.append(", Site: ").append(Math.round(siteValue*10.0)/10.0); 							
		msg.append(", Review: ").append(reviewerRole); 
		msg.append(" = Subtotal: ").append(score);
		
		logger.log(worker, Level.INFO, 0,  msg.toString());
	}
	
	/**
	 * Sees if the task is at least 90% completed.
	 * 
	 * @return true if the task is at least 90% completed.
	 */
	private boolean isAlmostTimeCompleted() {
		return getTimeCompleted() >= getDuration() * .9;
	}

	/**
	 * Performs the finished phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double approvingPhase(double time) {
        
		List<Mission> missions = missionManager.getPendingMissions(person.getAssociatedSettlement());
	     
		if (missions.size() == 0) {
			endTask();
		}
		
		else { 
	 		// Iterates through each pending mission 
			Iterator<Mission> i = missions.iterator();
			while (i.hasNext()) {
				Mission m = i.next();		
				MissionPlanning mp = m.getPlan();
				
				if (mp != null) {
		            PlanType status = mp.getStatus();
		
		            if (status != null && status == PlanType.PENDING
		            		&& mp.getPercentComplete() >= 60) {
		            							
		        		logger.log(worker, Level.INFO, 0, "Going over the approval of mission plans.");
		        	      
						Person p = m.getStartingPerson();
						String requestedBy = p.getName();
					
						Settlement settlement = person.getAssociatedSettlement();
						
						double score = mp.getScore();
						
						if (settlement.passMissionScore(score)) {
							// Approved
							// Updates the mission plan status
							missionManager.approveMissionPlan(mp, p, PlanType.APPROVED, settlement.getMinimumPassingScore());
								
							logger.log(worker, Level.INFO, 0, "Approved " + requestedBy
									+ "'s " + m.getName() + " mission plan. Total Score: " 
									+ Math.round(score*10.0)/10.0 
									+ " [Min: " + Math.round(settlement.getMinimumPassingScore()*10.0)/10.0 + "].");
						} else {
							// Not Approved
							// Updates the mission plan status
							missionManager.approveMissionPlan(mp, p, PlanType.NOT_APPROVED, settlement.getMinimumPassingScore());
						
							logger.log(worker, Level.INFO, 0, "Did NOT approve " + requestedBy
									+ "'s " + m.getName() + " mission plan. Total Score: " 
									+ Math.round(score*10.0)/10.0 
									+ " [Min: " + Math.round(settlement.getMinimumPassingScore()*10.0)/10.0 + "].");
						}
											
						settlement.saveMissionScore(score);
						
					      // Add experience
				        addExperience(time);
			        
						// Use up this time period 
				        // Do NOT call endTask()
				        // endTask();
				        
				        // Note: Do only one review each time
				        return 0;
					}
				}
			} // end of while
	    }
	    
        return 0;
	}

	
	@Override
	protected void addExperience(double time) {
        double newPoints = time / 20D;
        int experienceAptitude = worker.getNaturalAttributeManager().getAttribute(
                NaturalAttributeType.EXPERIENCE_APTITUDE);
        int leadershipAptitude = worker.getNaturalAttributeManager().getAttribute(
                NaturalAttributeType.LEADERSHIP);
        newPoints += newPoints * (experienceAptitude + leadershipAptitude- 100D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        worker.getSkillManager().addExperience(SkillType.MANAGEMENT, newPoints, time);

	}

	/**
	 * Release office space
	 */
	@Override
	protected void clearDown() {
		super.clearDown();
		
		// Remove person from administration function so others can use it.
		if (office != null && office.getNumStaff() > 0) {
			office.removeStaff();
		}
	}
}

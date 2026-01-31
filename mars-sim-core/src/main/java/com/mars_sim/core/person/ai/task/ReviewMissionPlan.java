/*
 * Mars Simulation Project
 * ReviewMissionPlan.java
 * @date 2022-12-22
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task;

import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.Administration;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.Management;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionPlanning;
import com.mars_sim.core.person.ai.mission.PlanType;
import com.mars_sim.core.person.ai.mission.SiteMission;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.ObjectiveUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The task for reviewing mission plans.
 */
public class ReviewMissionPlan extends Task {

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
	
	// Data members
	/** The administration building the person is using. */
	private Administration office;

	private MissionPlanning mp;
    
	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 * @param target Mission planning to review
	 */
	public ReviewMissionPlan(Person person, MissionPlanning target) {
		// Use Task constructor.
		super(NAME, person, false, false, STRESS_MODIFIER, SkillType.MANAGEMENT,
				RandomUtil.getRandomInt(20, 40), RandomUtil.getRandomInt(40, 80));
				
		if (person.isInSettlement()) {

			this.mp = target;
			mp.setActiveReviewer(person);

			// If person is in a settlement, try to find an office building.
			Building officeBuilding = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.ADMINISTRATION);

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

		PlanType status = mp.getStatus();
		if (status != null && status == PlanType.PENDING) {
			if (mp.getPercentComplete() >= 100) {
				// Go to the finished phase and finalize the approval
				setPhase(APPROVING);
				
				return time / 4.0; // return time is needed
			}
			
			else if (mp.getPercentComplete() >= 60) {
				int sol = getMarsTime().getMissionSol();
				int solRequest = mp.getMissionSol();
				if (sol - solRequest > 7) {
					// If no one else is able to offer the review after x days, 
					// do allow the review to go through even if the reviewer is not valid
					setPhase(APPROVING);
					
					return time / 4.0;
				}
			}

			// if not 100% reviewed
			Mission m = mp.getMission();
			if (!m.getStartingPerson().equals(person)
					&& mp.isReviewerValid(person)) {
				
				// Simulate the person has just spent 90% duration for this task
				// Now a mission score will be calculated
				if (isAlmostTimeCompleted()) {
					
					// 9. reviewer role weight
					RoleType role = person.getRole().getType();
					
					if (role.isCouncil()) 
						// Perform the executive review 
						completeExecutiveReview(m, reviewerSettlement, mp);
					else
						// Perform the general review 
						completeReview(m, reviewerSettlement, mp);
						
					logger.log(worker, Level.INFO, 0, "Done reviewing " + m.getStartingPerson().getName()
									+ "'s " + m.getName() + " mission plan.");
					// Add experience
					addExperience(time);
					
					endTask();
					
					return 0;
				}
			}
		}
		
        return 0;
	}

	/**
	 * Completes the executive review.
	 * 
	 * @param m
	 * @param reviewerSettlement
	 * @param mp
	 */
	private void completeExecutiveReview(Mission m, Settlement reviewerSettlement, MissionPlanning mp) {		
	    GoodsManager goodsManager = reviewerSettlement.getGoodsManager();
	    
		Person leader = m.getStartingPerson();
    	
		// 1. Reviews requester's cumulative job rating
		double rating = leader.getJobHistory().getCummulativeJobRating();
			
		// 2. Relationship Score 
		int relation = assessLeader(leader, reviewerSettlement);

		// 3. Mission Qualification Score
		double qual = m.getMissionQualification(person) * 0.4D;
		
		// 4. Settlement objective score, is the Mission type
		// is preferred for the Objective
		double obj = 0;
		ObjectiveType objective = reviewerSettlement.getObjective();
		Set<ObjectiveType> satisfiedObjectives = m.getObjectiveSatisified();
		if (satisfiedObjectives.contains(objective)) {
			CommerceType cFactor = ObjectiveUtil.toCommerce(objective);
			if (cFactor != null) {
				obj += 5D * goodsManager.getCommerceFactor(cFactor);
			}
		}

		// 5. emergency
		int emer = 0;
	
		// 6. Site Value
		double siteValue = 0;
		if (m instanceof SiteMission sm) {
			// The site value is divided by the distance proposed
			siteValue = sm.getTotalSiteScore(reviewerSettlement)/ ((VehicleMission)m).getTotalDistanceProposed();
		}

		// 7. proposed route distance (note that a negative score represents a penalty)
		int dist = 0;
		if (m instanceof VehicleMission vm) {
			double range = vm.getVehicle().getEstimatedRange();
			double proposed = vm.getTotalDistanceProposed();
			
			// Scoring rule:
			// At range = 0, the score is 0
			// At half the range, the score is -100
			// At full range, the score is -200
			
			// Calculate the dist score
			dist = (int)(- (200.0 * proposed)/ range);
		}
		
		// 8. Leadership and Charisma
		NaturalAttributeManager attrMgr = person.getNaturalAttributeManager();
		int leadership = (int)(.075 * attrMgr
							.getAttribute(NaturalAttributeType.LEADERSHIP)
						+ .025 * attrMgr
							.getAttribute(NaturalAttributeType.ATTRACTIVENESS));				

		// 9. reviewer role weight
		int reviewerRole = assessReviewer();

		// 10. luck
		int luck = RandomUtil.getRandomInt(-5, 5);	
		
		// Future: 9. Go to him/her to have a chat
		// Future: 10. mission lead's leadership/charisma
		
		double score = Math.round((rating + relation + qual + obj + emer + siteValue + dist + leadership + reviewerRole + luck)* 10.0)/10.0;

		// Updates the mission plan status
		mp.scoreMissionPlan(score, person);

		StringBuilder msg = new StringBuilder();
		msg.append("Grading ").append(m.getName());
		msg.append(" - Rating: ").append(rating); 
		msg.append(", Rels: ").append(relation); 
		msg.append(", Quals: ").append(qual); 
		msg.append(", Obj: ").append(obj);
		msg.append(", Emer: ").append(emer);
		msg.append(", Site: ").append(Math.round(siteValue*10.0)/10.0);
		msg.append(", Dist: ").append(dist);
		msg.append(", Lead: ").append(leadership); 							
		msg.append(", Review: ").append(reviewerRole); 
		msg.append(", Luck: ").append(luck); 
		msg.append("; Subtotal: ").append(score);
		
		logger.log(worker, Level.INFO, 0,  msg.toString());
	}

	/**
	 * Assesses the relationship of the reviewer with the Mission Leader.
	 * 
	 * @param leader
	 * @param reviewerSettlement
	 */
	private int assessLeader(Person leader, Settlement reviewerSettlement) {
		// 2a. Reviewer's view of the mission lead
		double relationshipWithReviewer = RelationshipUtil.getOpinionOfPerson(person, leader);
			
		double relationshipWithOthers = 0;
		int num = reviewerSettlement.getAllAssociatedPeople().size();
		for (Person pp : reviewerSettlement.getAllAssociatedPeople()) {
			relationshipWithOthers += RelationshipUtil.getOpinionOfPerson(person, pp);
		}
		
		// 2b. Others' view of the mission lead
		relationshipWithOthers = (int)(relationshipWithOthers / num);
		
		return (int)((relationshipWithReviewer + relationshipWithOthers) / 10D) ;
	}	

	/**
	 * Assesses the reviewer based on their Role.
	 */
	private int assessReviewer() {
		RoleType role = person.getRole().getType();
		int reviewerRole = 0;
		
		if (role == RoleType.PRESIDENT)
			reviewerRole = 16;
		else if (role == RoleType.MAYOR)
			reviewerRole = 14;
		else if (role == RoleType.ADMINISTRATOR)
			reviewerRole = 12;
		else if (role == RoleType.DEPUTY_ADMINISTRATOR)
			reviewerRole = 12;
		else if (role == RoleType.COMMANDER)
			reviewerRole = 10;
		else if (role == RoleType.SUB_COMMANDER)
			reviewerRole = 8;
		else if (role == RoleType.CHIEF_OF_MISSION_PLANNING)
			reviewerRole = 6;
		else if (role == RoleType.CHIEF_OF_LOGISTIC_OPERATION)
			reviewerRole = 6;
		else if (role.isChief())
			reviewerRole = 5;
		else if (role == RoleType.MISSION_SPECIALIST)
			reviewerRole = 4;
		else
			reviewerRole = 2;
		return reviewerRole;
	}
	
	/**
	 * Completes the general review.
	 * 
	 * @param m
	 * @param reviewerSettlement
	 * @param mp
	 */
	private void completeReview(Mission m, Settlement reviewerSettlement, MissionPlanning mp) {      	
		// 2. Relationship Score 
		Person leader = m.getStartingPerson();
		int relation = assessLeader(leader, reviewerSettlement);
		
		// 6. Site Value
		double siteValue = 0;
		if (m instanceof SiteMission sm) {
			// The site value is divided by the distance proposed
			siteValue = sm.getTotalSiteScore(reviewerSettlement)/ ((VehicleMission)m).getTotalDistanceProposed();
		}

		// 9. reviewer role weight
		int reviewerRole = assessReviewer();
		
		double score = Math.round((relation + siteValue + reviewerRole)* 10.0)/10.0;

		// Updates the mission plan status
		mp.scoreMissionPlan(score, person);

		StringBuilder msg = new StringBuilder();
		msg.append("Reviewing ").append(m.getName());
		msg.append(" - Rels: ").append(relation); 
		msg.append(", Site: ").append(Math.round(siteValue*10.0)/10.0); 							
		msg.append(", Review: ").append(reviewerRole); 
		msg.append("; Subtotal: ").append(score);
		
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
        
		PlanType status = mp.getStatus();

		if (status != null && status == PlanType.PENDING
				&& mp.getPercentComplete() >= 60) {
									
			logger.log(worker, Level.INFO, 0, "Going over the approval of mission plans.");
				

			Mission m = mp.getMission();
			String requestedBy = m.getStartingPerson().getName();
		
			Settlement settlement = person.getAssociatedSettlement();
			
			double score = mp.getScore();
			double minScore = mp.getPassingScore();
			if (score > minScore) {
				// Approved
				// Updates the mission plan status
				missionManager.approveMissionPlan(mp, PlanType.APPROVED);
					
				logger.log(worker, Level.INFO, 0, "Approved " + requestedBy
						+ "'s " + m.getName() + " mission plan. Score: " 
						+ Math.round(score*10.0)/10.0 
						+ " [Min: " + Math.round(minScore*10.0)/10.0 + "].");
			}
			else {
				// Not Approved
				// Updates the mission plan status
				missionManager.approveMissionPlan(mp, PlanType.NOT_APPROVED);
			
				logger.log(worker, Level.INFO, 0, "Did NOT approve " + requestedBy
						+ "'s " + m.getName() + " mission plan. Score: " 
						+ Math.round(score*10.0)/10.0 
						+ " [Min: " + Math.round(minScore*10.0)/10.0 + "].");
			}
								
			settlement.saveMissionScore(score);
			
			// Add experience
			addExperience(time);
		}

		// Approval phase is a one shot activity so end task
		endTask();
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
		if (mp != null) {
			mp.setActiveReviewer(null);
		}
	}
}

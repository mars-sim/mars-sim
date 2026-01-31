/*
 * Mars Simulation Project
 * MissionPlanning.java
 * @date 2022-09-28
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.mission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.role.RoleType;

public class MissionPlanning implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static final double PERCENT_PER_SCORE = 10D;

	// Event type when the plan reviewer is changed
	public static final String PLAN_REVIEWER_EVENT = "PlanReviewerEvent";

	// Event type when the plan state is changed
	public static final String PLAN_STATE_EVENT = "PlanStateEvent";

	private int requestedSol;
	private int reviewPercentComplete; // 0% to 100%
	private double score; // 0 to 1000 points
	private double passingScore = 0;

	private PlanType status = PlanType.PREPARING;

	private Mission mission;
	
	private List<String> reviewers;
	private Person activeReviewer = null;
	
	public MissionPlanning(Mission mission, int requestedOn, double passingScore) {
		this.requestedSol = requestedOn;
		this.mission = mission;
		this.passingScore = passingScore;
		reviewers = new ArrayList<>();
	}
	
	/**
	 * Scores this mission plan for a reviewer
	 *
	 * @param newScore The new score 
	 * @param reviewer 
	 */
	public void scoreMissionPlan(double newScore, Person reviewer) {
		RoleType role = reviewer.getRole().getType();

		double weight = switch (role) {
			case MAYOR -> 4D;
			case ADMINISTRATOR -> 3.5D;
			case DEPUTY_ADMINISTRATOR -> 3D;	
			case COMMANDER -> 2.5D;
			case SUB_COMMANDER, CHIEF_OF_MISSION_PLANNING -> 2D;
			case CHIEF_OF_AGRICULTURE, CHIEF_OF_COMPUTING, CHIEF_OF_ENGINEERING,
				CHIEF_OF_LOGISTIC_OPERATION, CHIEF_OF_SAFETY_HEALTH_SECURITY,
				CHIEF_OF_SCIENCE, CHIEF_OF_SUPPLY_RESOURCE -> 1.5D;
			case MISSION_SPECIALIST -> 1.5D;
			default -> 1D;
		};

		// Update stats
		reviewPercentComplete += weight * PERCENT_PER_SCORE;
		if (reviewPercentComplete > 100)
			reviewPercentComplete = 100;
		score += weight * newScore;

		String reviewerName = reviewer.getName();
		if (!reviewers.contains(reviewerName)) {
			reviewers.add(reviewerName);
		}

		mission.fireMissionUpdate(PLAN_STATE_EVENT, reviewer);
	}

	/**
	 * Checks if the person can review this mission plan
	 * Note: the maximum number of reviews are limited with the size
	 * of the settlement.
	 * 
	 * @param reviewer
	 * @return
	 */
	public boolean isReviewerValid(Person reviewer) {
		var reviewerName = reviewer.getName();
		if (!reviewers.contains(reviewerName)) {
			return true;
		}
		else {
			// If he has reviewed this mission plan before, 
			// he can still review it again, after other reviewers
			// have looked at the plan
			var rules = reviewer.getAssociatedSettlement().getChainOfCommand().getGovernance();
			return (reviewers.size() >= rules.getUniqueReviewers());
		}
	}
	
	public void setStatus(PlanType status) {
		this.status = status;
		mission.fireMissionUpdate(PLAN_STATE_EVENT, status);
	}
	
	public Mission getMission() {
		return mission;
	}
	
	public PlanType getStatus() {
		return status;
	}
	
	public int getMissionSol() {
		return requestedSol;
	}
	
	/**
	 * Percentage of the review completed.
	 * @return Value between 0 and 100.
	 */
	public int getPercentComplete() {
		return reviewPercentComplete;
	}
	
	public double getScore() {
		return score;
	}
	
	public double getPassingScore() {
		return passingScore;
	}

	/**
	 * Gets the active reviewer. Assigned via a Task
	 * @return Could be null if no review is in progress.
	 */
	public Person getActiveReviewer() {
		return activeReviewer;
	}

	/**
	 * Sets the active reviewer
	 * @param reviewer Can be null if no reviewer is active.
	 */
	public void setActiveReviewer(Person reviewer) {
		if (reviewer != null) {
			activeReviewer = reviewer;
		}
		else {
			activeReviewer = null;
		}

		mission.fireMissionUpdate(PLAN_REVIEWER_EVENT, reviewer);
	}
}

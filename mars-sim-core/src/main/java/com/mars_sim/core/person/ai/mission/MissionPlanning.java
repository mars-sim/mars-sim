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

	private int requestedSol;
	private double reviewPercentComplete; // 0% to 100%
	private double score; // 0 to 1000 points
	private double passingScore = 0;

	private PlanType status = PlanType.PREPARING;

	private Mission mission;
	
	private List<String> reviewers;
	private String activeReviewer = null;
	
	public MissionPlanning(Mission mission, int requestedOn) {
		this.requestedSol = requestedOn;
		this.mission = mission;
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
	}

	public void setPassingScore(double score) {
		passingScore = score;
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
	
	public double getPercentComplete() {
		return reviewPercentComplete;
	}
	
	public double getScore() {
		return score;
	}
	
	public double getPassingScore() {
		return passingScore;
	}

	public String getActiveReviewer() {
		return activeReviewer;
	}

	public void setActiveReviewer(Person reviewer) {
		if (reviewer != null) {
			activeReviewer = reviewer.getName();
		}
		else {
			activeReviewer = null;
		}
	}
}

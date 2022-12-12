/*
 * Mars Simulation Project
 * MissionPlanning.java
 * @date 2022-09-28
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.role.RoleType;

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
		double weight = 1D;
		RoleType role = reviewer.getRole().getType();

		switch (role) {
			case COMMANDER:
					weight = 2.5; break;
			case SUB_COMMANDER:
			case CHIEF_OF_MISSION_PLANNING:
				weight = 2D; break;
			case CHIEF_OF_AGRICULTURE:
			case CHIEF_OF_COMPUTING:
			case CHIEF_OF_ENGINEERING:
			case CHIEF_OF_LOGISTICS_N_OPERATIONS:
			case CHIEF_OF_SAFETY_N_HEALTH:
			case CHIEF_OF_SCIENCE:
			case CHIEF_OF_SUPPLY_N_RESOURCES:
			case MISSION_SPECIALIST:
				weight = 1.5;  break;
			default:
				weight = 1; break;
		}

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
	 * @param name
	 * @param pop
	 * @return
	 */
	public boolean isReviewerValid(String name, int pop) {
		if (!reviewers.contains(name)) {
			return true;
		}
		else {
			// If he has reviewed this mission plan before, 
			// he can still review it again, after other reviewers
			// have looked at the plan
			int num = reviewers.size();
			if (pop >= 48) {
                return num >= 10;
			}
			else if (pop >= 36) {
                return num >= 9;
			}
			else if (pop >= 30) {
                return num >= 8;
			}
			else if (pop >= 24) {
                return num >= 7;
			}
			else if (pop >= 18) {
                return num >= 6;
			}
			else if (pop >= 12) {
                return num >= 5;
			}
			else if (pop >= 8) {
                return num >= 4;
			}			
			else if (pop >= 6) {
                return num >= 3;
			}	
			else if (pop >= 4) {
                return num >= 2;
			}
			else if (pop == 3) {
                return num >= 1;
			}
			else if (pop == 2) {
                return true;
			}
			
			return true;
		}
	}
	
	public void setStatus(PlanType status) {
		this.status = status;
	}

	public void setPassingScore(double threshold) {
		passingScore = threshold;
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
	
	public double getScore() {;
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

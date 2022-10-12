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

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;

public class MissionPlanning implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private int requestedSol;
	private double proposalPercentComplete; // 0% to 100%
	private double reviewPercentComplete; // 0% to 100%
	private double score; // 0 to 1000 points
	private double passingScore = 0;

	private PlanType status = PlanType.PREPARING;

	private Mission mission;
	
	private List<String> reviewers;
	
	private static MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();

	public MissionPlanning(Mission mission) {
		this.requestedSol = clock.getMissionSol();
		this.mission = mission;
		reviewers = new ArrayList<>();
	}
	
	/**
	 * Adds the name of the reviewer to the map.
	 * 
	 * @param name
	 */
	public void setReviewedBy(String name) {
		if (!reviewers.contains(name)) {
			reviewers.add(name);
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
                return num > 12;
			}
			else if (pop >= 42) {
                return num > 11;
			}
			else if (pop >= 36) {
                return num > 10;
			}
			else if (pop >= 30) {
                return num > 9;
			}
			else if (pop >= 24) {
                return num > 8;
			}
			else if (pop >= 18) {
                return num > 7;
			}
			else if (pop >= 12) {
                return num > 6;
			}
			else if (pop >= 10) {
                return num > 5;
			}
			else if (pop >= 8) {
                return num > 4;
			}			
			else if (pop >= 6) {
                return num > 3;
			}	
			else if (pop >= 4) {
                return num > 2;
			}
			else if (pop == 3) {
                return num > 1;
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

	public void setScore(double value) {
		score = value;
	}
	
	public void setReviewPercentComplete(double value) {
		reviewPercentComplete = value;
	}
	
	public void setProposalPercentComplete(double value) {
		proposalPercentComplete = value;
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
	
	public double getProposalPercentComplete() {
		return proposalPercentComplete;
	}
	
	public double getScore() {;
		return score;
	}
	
	public double getPassingScore() {
		return passingScore;
	}
	
	public static void initializeInstances(MarsClock c) {
		clock = c;
	}

}

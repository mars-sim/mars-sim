/**
 * Mars Simulation Project
 * MissionPlanning.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;

public class MissionPlanning implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private int requestedSol;
	private double percentComplete; // 0% to 100%
	private double score; // 0 to 1000 points
	private double passingScore = 0;

	private PlanType status = PlanType.PENDING;

	private Mission mission;
	
	private Map<String, Integer> reviewers;
	
	private static MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();

	public MissionPlanning(Mission mission) {
		this.requestedSol = clock.getMissionSol();
		this.mission = mission;
		reviewers = new ConcurrentHashMap<>();
	}
	
	public void setReviewedBy(String name) {
		setReviewer(name);
	}
	
	public void setReviewer(String name) {
		if (reviewers.containsKey(name)) {
			int num = reviewers.get(name);
			reviewers.put(name, num++);
		}
		
		else {
			reviewers.put(name, 1);
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
		if (!reviewers.containsKey(name)) {
			return true;
		}
		else {
			int num = reviewers.get(name);
			if (pop >= 48) {
                return num < 2;
			}
			else if (pop >= 24) {
                return num < 2;
			}
			else if (pop >= 12) {
                return num < 2;
			}
			else if (pop >= 10) {
                return num < 3;
			}
			else if (pop >= 8) {
                return num < 3;
			}			
			else if (pop >= 6) {
                return num < 3;
			}	
			else if (pop >= 4) {
                return num < 4;
			}
			else if (pop == 3) {
                return num < 5;
			}
			else if (pop == 2) {
                return num < 6;
			}
			else {
				return true;
			}
		}
	}
	
	public void setStatus(PlanType status) {
		this.status = status;
	}

	public void setScore(double value) {
		score = value;
	}
	
	public void setPercentComplete(double value) {
		percentComplete = value;
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
		return percentComplete;
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

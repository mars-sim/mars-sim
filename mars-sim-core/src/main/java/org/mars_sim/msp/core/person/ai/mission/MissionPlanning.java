/**
 * Mars Simulation Project
 * MissionPlanning.java
 * @version 3.1.0 2018-10-10
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.time.MarsClock;

public class MissionPlanning implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
//	private static Logger logger = Logger.getLogger(MissionPlanning.class.getName());
//	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
//			logger.getName().length());
	
	private int requestedSol;
	private double percentComplete; // 0% to 100%
	private double score; // 0 to 1000 points
	private double qualityScore;
	
	private String requestedBy;
	private String approvedBy;
	
	private String requestTimeStamp;
	private String lastReviewedTimeStamp;

	private RoleType requesterRole;
	private RoleType approvedRole;
	private PlanType status = PlanType.PENDING;

	private Mission mission;
	
	private Map<String, Integer> reviewers;
	
	private static MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();

	public MissionPlanning(Mission mission, String requestedBy, RoleType role) {
		this.requestedSol = clock.getMissionSol();
		this.requestTimeStamp = clock.getDateTimeStamp();
		this.mission = mission;
		this.requestedBy = requestedBy;
		this.requesterRole = role;
		reviewers = new HashMap<>();
	}
	
	public void setReviewedBy(String name) {
		setReviewer(name);
		// Note : resetting marsClock is needed after loading from a saved sim 
		lastReviewedTimeStamp = clock.getDateTimeStamp();
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
				if (num < 2)
					return true;
				else
					return false;
			}
			else if (pop >= 24) {
				if (num < 2)
					return true;
				else
					return false;
			}
			else if (pop >= 12) {
				if (num < 2)
					return true;
				else
					return false;
			}
			else if (pop >= 10) {
				if (num < 3)
					return true;
				else
					return false;
			}
			else if (pop >= 8) {
				if (num < 3)
					return true;
				else
					return false;
			}			
			else if (pop >= 6) {
				if (num < 3)
					return true;
				else
					return false;
			}	
			else if (pop >= 4) {
				if (num < 4)
					return true;
				else
					return false;
			}
			else if (pop == 3) {
				if (num < 5)
					return true;
				else
					return false;
			}
			else if (pop == 2) {
				if (num < 6)
					return true;
				else
					return false;
			}
			else {
				return true;
			}
		}
	}
	
	public void setStatus(PlanType status) {
		this.status = status;
	}
	
	public void setApprovedRole(RoleType roleType) {
		this.approvedRole = roleType;
	}

	public void setApproved(Person p) {
		this.approvedBy = p.getName();
		this.approvedRole = p.getRole().getType();
	}
	
	public void setScore(double value) {
		score = value;
	}
	
	public void setQualityScore(double value) {
		qualityScore = value;
	}
	
	public void setPercentComplete(double value) {
		percentComplete = value;
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
	
	public double getQualityScore() {
		return qualityScore;
	}
	
	public static void initializeInstances(MarsClock c) {
		clock = c;
	}
}

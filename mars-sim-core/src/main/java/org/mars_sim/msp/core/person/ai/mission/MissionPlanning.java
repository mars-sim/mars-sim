/**
 * Mars Simulation Project
 * MissionPlanning.java
 * @version 3.1.0 2018-10-10
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.time.MarsClock;

public class MissionPlanning implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
//	private static Logger logger = Logger.getLogger(MissionPlanning.class.getName());
//
//	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
//			logger.getName().length());
//	
	
	private int requestedSol;
	private double percentComplete = 10; // 0% to 100%
	private double score = 0; // 0 to 1000 points
	private double qualityScore = 0;
	
	private String reviewedBy;
	private String requestedBy;
	private String requestTimeStamp;
	private String reviewedTimeStamp;

	private RoleType requesterRole;
	private RoleType reviewedRole;
	private PlanType status = PlanType.PENDING;

	private Mission mission;
	
	private static MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();

	public MissionPlanning(Mission mission, String requestedBy, RoleType role) {
		this.requestedSol = clock.getMissionSol();
		this.requestTimeStamp = clock.getDateTimeStamp();
		this.mission = mission;
		this.requestedBy = requestedBy;
		this.requesterRole = role;
	}
	
	public void setReviewedBy(String name) {
		this.reviewedBy = name;
		// Note : resetting marsClock is needed after loading from a saved sim 
		reviewedTimeStamp = clock.getDateTimeStamp();
	}
	
	public void setStatus(PlanType status) {
		this.status = status;
	}
	
	public void setReviewedRole(RoleType roleType) {
		this.reviewedRole = roleType;
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
	
}

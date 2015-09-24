/**
 * Mars Simulation Project
 * JobAssignment.java
 * @version 3.08 2015-03-31
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The JobAssignment class represents the characteristics of a job type
 */
public class JobAssignment implements Serializable {

    private static final long serialVersionUID = 1L;

    private String initiator;
    private String jobType;
    private String timeSubmitted;
    private String authorizedBy;
    private MarsClock timeAuthorized;
    private JobAssignmentType status; // "Pending" or "Approved"
    private int jobRating;
    private int sol;


	public JobAssignment(String timeSubmitted, String jobType, String initiator, JobAssignmentType status, String authorizedBy) {
		// 2015-09-23 Changed the first parameter of JobAssignment.java from MarsClock to String.
		this.timeSubmitted = timeSubmitted;
		this.jobType = jobType;
		this.initiator = initiator;
		this.status = status;
		this.authorizedBy = authorizedBy;


		//this.timeAuthorized = clock;

	}

	public String getTimeSubmitted() {
		return timeSubmitted;
	}

	public int getSolSubmitted() {
		return sol;
	}

	public void setSolSubmitted() {
		MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();
		sol = MarsClock.getTotalSol(clock);
	}

	//public MarsClock getTimeAuthorized() {
	//	return timeAuthorized;
	//}

	//public void setTimeAuthorized(MarsClock time) {
	//	this.timeAuthorized = time;
	//}

	public String getJobType() {
		return jobType;
	}

	public String getInitiator() {
		return initiator;
	}

	public String getAuthorizedBy() {
		return authorizedBy;
	}

	public void setAuthorizedBy(String name) {
		authorizedBy = name;
	}

	public JobAssignmentType getStatus() {
		return status;
	}

	public void setStatus(JobAssignmentType status) {
		//System.out.println("setStatus : status is " + status);
		this.status = status;
	}

	public void setJobRating(int value) {
		this.jobRating = value;
	}

	public int getJobRating() {
		return jobRating;
	}
}

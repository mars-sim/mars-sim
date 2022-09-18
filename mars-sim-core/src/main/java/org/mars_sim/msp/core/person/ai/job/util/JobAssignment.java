/*
 * Mars Simulation Project
 * JobAssignment.java
 * @date 2022-07-06
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.job.util;

import java.io.Serializable;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClockFormat;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The JobAssignment class represents the characteristics of a job type
 */
public class JobAssignment implements Serializable {

    private static final long serialVersionUID = 1L;

    private int sol;
	private int solRatingSubmitted = -1; //no rating has ever been submitted

    private double jobRating = 5; // has a score of 5 if unrated 
    	
    private String initiator;
    private JobType jobType;
    private String timeSubmitted;
    private String authorizedBy;
      
    private JobAssignmentType status; // JobAssignmentType.PENDING or JobAssignmentType.APPROVED
    
	public JobAssignment(JobType jobType, String initiator, JobAssignmentType status, String authorizedBy) {
		// Change the first parameter of JobAssignment.java from MarsClock to String.
		
		MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();
				
		this.timeSubmitted = MarsClockFormat.getDateTimeStamp(clock);
		this.sol = clock.getMissionSol();
		this.jobType = jobType;
		this.initiator = initiator;
		this.status = status;
		this.authorizedBy = authorizedBy;

	}

	public String getTimeSubmitted() {
		return timeSubmitted;
	}

	public int getSolSubmitted() {
		return sol;
	}

	public JobType getJobType() {
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
		this.status = status;
	}

	public void setJobRating(int value) {
		jobRating = (int) (0.7 * jobRating + 0.3 * value);	
	}

	public void setSolRatingSubmitted(int sol){
		solRatingSubmitted = sol;
	}

	public int getSolRatingSubmitted(){
		return solRatingSubmitted;
	}
	
	public int getJobRating() {	
		return (int)jobRating;
	}
}

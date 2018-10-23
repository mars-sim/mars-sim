/**
 * Mars Simulation Project
 * JobAssignment.java
 * @version 3.1.0 2017-08-30
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

    private int sol;
	private int solRatingSubmitted = -1; //no rating has ever been submitted

    private double jobRating = 5; // has a score of 5 if unrated 
    	
    private String initiator;
    private String jobType;
    private String timeSubmitted;
    private String authorizedBy;
      
    private JobAssignmentType status; // JobAssignmentType.PENDING or JobAssignmentType.APPROVED

	private static MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();
    
	public JobAssignment(String jobType, String initiator, JobAssignmentType status, String authorizedBy) {
		// Change the first parameter of JobAssignment.java from MarsClock to String.
		this.timeSubmitted = MarsClock.getDateTimeStamp(clock);
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

	public void setSolSubmitted() {
		sol = clock.getMissionSol();
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

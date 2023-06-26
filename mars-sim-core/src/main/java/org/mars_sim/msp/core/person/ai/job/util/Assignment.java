/*
 * Mars Simulation Project
 * Assignment.java
 * @date 2023-07-17
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.job.util;

import java.io.Serializable;

/**
 * This class represents an entry of a job or role assignment.
 */
public class Assignment implements Serializable {

    private static final long serialVersionUID = 1L;

	private int solRatingSubmitted = -1; //no rating has ever been submitted

    private double jobRating = 5; // has a score of 5 if unrated 
    	
    private String initiator;
    private JobType type;
    private String authorizedBy;
      
    private AssignmentType status;
    
	public Assignment(JobType newJob, String initiator, AssignmentType status, String authorizedBy) {
					
		this.type = newJob;
		this.initiator = initiator;
		this.status = status;
		this.authorizedBy = authorizedBy;
	}

	public JobType getType() {
		return type;
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

	public AssignmentType getStatus() {
		return status;
	}

	public void setJobRating(int value, int sol) {
		jobRating = (int) (0.7 * jobRating + 0.3 * value);	
		solRatingSubmitted = sol;
	}

	public int getSolRatingSubmitted(){
		return solRatingSubmitted;
	}
	
	public int getJobRating() {	
		return (int)jobRating;
	}
}

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

	public static final int INITIAL_RATING = 5;
	public static final double NEW_RATING_WEIGHT = 0.3D;
	public static final double OLD_RATING_WEIGHT = 1D - NEW_RATING_WEIGHT;

    private static final long serialVersionUID = 1L;

	private int solRatingSubmitted = -1; //no rating has ever been submitted

    private int jobRating = INITIAL_RATING; // has a score of 5 if unrated 
    	
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
		jobRating = (int) ((OLD_RATING_WEIGHT * jobRating) + (NEW_RATING_WEIGHT * value));	
		solRatingSubmitted = sol;
	}

	public int getSolRatingSubmitted(){
		return solRatingSubmitted;
	}
	
	public int getJobRating() {	
		return jobRating;
	}
}

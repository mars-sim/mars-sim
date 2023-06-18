/*
 * Mars Simulation Project
 * Assignment.java
 * @date 2023-07-17
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.job.util;

import java.io.Serializable;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClockFormat;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * This class represents an entry of a job or role assignment.
 */
public class Assignment implements Serializable {

    private static final long serialVersionUID = 1L;

    private int sol;
	private int solRatingSubmitted = -1; //no rating has ever been submitted

    private double jobRating = 5; // has a score of 5 if unrated 
    	
    private String initiator;
    private String type;
    private String timeSubmitted;
    private String authorizedBy;
      
    private AssignmentType status;
    
	public Assignment(String type, String initiator, AssignmentType status, String authorizedBy) {
	
		MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();
				
		this.timeSubmitted = MarsClockFormat.getDateTimeStamp(clock);
		this.sol = clock.getMissionSol();
		this.type = type;
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

	public String getType() {
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

	public void setStatus(AssignmentType status) {
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

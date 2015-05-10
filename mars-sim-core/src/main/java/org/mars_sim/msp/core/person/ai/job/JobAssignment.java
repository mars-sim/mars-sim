/**
 * Mars Simulation Project
 * JobAssignment.java
 * @version 3.08 2015-03-31
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;

import org.mars_sim.msp.core.time.MarsClock;

public class JobAssignment implements Serializable {

    private static final long serialVersionUID = 1L;

    private String initiator;
    private String jobType;
    private MarsClock timeSubmitted;
    private String authorizedBy;
    private MarsClock timeAuthorized;
    private String status; // Pending or Approved

	public JobAssignment(MarsClock timeSubmitted, String jobType, String initiator) {
		this.timeSubmitted = timeSubmitted;
		this.jobType = jobType;
		this.initiator = initiator;
	}

	public MarsClock getTimeSubmitted() {
		return timeSubmitted;
	}
	public MarsClock getTimeAuthorized() {
		return timeAuthorized;
	}

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}

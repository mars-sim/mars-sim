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
    private MarsClock time;

	public JobAssignment(MarsClock time, String jobType, String initiator) {
		this.time = time;
		this.jobType = jobType;
		this.initiator = initiator;
	}

	public MarsClock getTime() {
		return time;
	}

	public String getJobType() {
		return jobType;
	}

	public String getInitiator() {
		return initiator;
	}
}

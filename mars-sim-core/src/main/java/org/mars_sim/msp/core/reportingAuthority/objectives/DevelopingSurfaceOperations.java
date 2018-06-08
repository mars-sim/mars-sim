/**
 * Mars Simulation Project
 * DevelopingSpaceActivityISRO.java
 * @version 3.1.0 2017-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class DevelopingSurfaceOperations implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Developing Standard Planetary Surface Operations";
	
	private final String[] phases = new String[] {
			"Improve Rover Range and Performance",
			"Test Building Integrity", 
			"Test Return Vehicle Capability"};

	@Override
	public String[] getPhases() {
		return phases;
	}


	@Override
	public String getObjectiveName() {
		return name;
	}

	@Override
	public void reportFindings() {
		System.out.println("I'm putting together reports of the seismic activity in this region.");
	}

	@Override
	public void gatherSamples() {
		System.out.println("I'm analyzing the strength of the soil in this local region for the suitability future spaceport construction.");
	}


}

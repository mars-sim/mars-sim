/**
 * Mars Simulation Project
 * ResearchingSpaceApplication.java
 * @version 3.1.0 2017-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class ResearchingHealthHazards implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Researching Short and Long Term Health Hazards";
	
	private final String[] phases = new String[] {
			"Test Rover Performance",
			"Set up Radiation Sensor Grid", 
			"Gather Medical Data"};

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
		System.out.println("I'm putting together reports of the various health hazards for human beings on Mars.");
	}

	@Override
	public void gatherSamples() {
		System.out.println("I'm analyzing the soil samples from various sites for possible human health hazards");
	}

}

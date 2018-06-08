/**
 * Mars Simulation Project
 * FindingLife.java
 * @version 3.1.0 2017-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class FindingLife implements MissionAgenda, Serializable  {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Finding Life Past and Present on Mars";
	
	private final String[] phases = new String[] {
			"Test Rover Range",
			"Examine regions capable hosting and sustaining organic microbial life",			
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
		System.out.println("I'm putting together reports of the oxygen content in the soil samples.");
	}

	@Override
	public void gatherSamples() {
		System.out.println("I'm analyzing the soil samples from various sites for the amount of oxygen and water contents.");
	}


}

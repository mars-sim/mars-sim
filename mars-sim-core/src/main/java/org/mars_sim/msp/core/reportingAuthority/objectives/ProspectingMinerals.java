/**
 * Mars Simulation Project
 * ProspectingMineral.java
 * @version 3.1.0 2017-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class ProspectingMinerals implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Prospercting Precious Minerals on Mars";
	
	private final String[] phases = new String[] {
			"Corroborate Surface Geological Data with On-Orbit Scans",
			"Core Drill Rock Samples from Selected Locations"};

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
		System.out.println("I'm putting together reports of the helium-3 and other trace trace content in the collected soil samples.");
	}

	@Override
	public void gatherSamples() {
		System.out.println("I'm analyzing the wealth of mineral contents from the colleted soil samples.");
	}



}

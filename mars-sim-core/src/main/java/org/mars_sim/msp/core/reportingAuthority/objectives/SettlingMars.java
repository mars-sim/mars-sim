/**
 * Mars Simulation Project
 * SettlingMars.java
 * @version 3.1.0 2017-01-14
 * @author Manny Kung
 */
package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class SettlingMars implements MissionAgenda, Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Settling Mars";
	
	private final String[] phases = new String[] {
			"Engineer soil capable of hosting and sustaining organic microbial life",
			"Test Building Integrity", 
			"Gather Hydro-Meteorological Data"};

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
		System.out.println("I'm analyzing geological features in this region.");
	}

	@Override
	public void gatherSamples() {
		System.out.println("I'm putting together a report of the local in-situ resources "
				+ "that we can collect and process for our immediate uses.");
	}
}

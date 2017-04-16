/**
 * Mars Simulation Project
 * ProspectingMineral.java
 * @version 3.1.0 2017-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

public class ProspectingMineral implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Finding Precious Minerals on Mars";

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

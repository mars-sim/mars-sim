/**
 * Mars Simulation Project
 * FindingLife.java
 * @version 3.1.0 2017-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

public class FindingLife implements MissionAgenda, Serializable  {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Finding Life Past and Present on Mars";

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

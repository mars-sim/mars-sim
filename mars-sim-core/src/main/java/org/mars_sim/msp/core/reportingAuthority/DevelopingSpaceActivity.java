/**
 * Mars Simulation Project
 * DevelopingSpaceActivityISRO.java
 * @version 3.08 2015-10-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

public class DevelopingSpaceActivity implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Developing Space Activities";

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

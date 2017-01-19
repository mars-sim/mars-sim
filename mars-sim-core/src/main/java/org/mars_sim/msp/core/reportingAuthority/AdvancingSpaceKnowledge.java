/**
 * Mars Simulation Project
 * AdvancingSpaceKnowledge.java
 * @version 3.1.0 2017-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

public class AdvancingSpaceKnowledge implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Advancing of the Knowledge of Space";

	@Override
	public String getObjectiveName() {
		return name;
	}

	@Override
	public void reportFindings() {
		System.out.println("I'm putting together a report of possible research opportunities in this region.");
	}

	@Override
	public void gatherSamples() {
		System.out.println("I'm analyzing the variation of gravity and atmospheric conditions in this local region for the impact of deploying a laser communication array.");
	}


}

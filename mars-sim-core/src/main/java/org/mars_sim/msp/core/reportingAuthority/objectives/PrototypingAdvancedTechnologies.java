/**
 * Mars Simulation Project
 * DevelopingAdvancedTechnology.java
 * @version 3.1.0 2017-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class PrototypingAdvancedTechnologies implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Prototyping Advanced Technologies";
	
	private final String[] phases = new String[] {
			"Test Rover Range and Performance",
			"Prototype New Building Material", 
			"Test New Propulsion Systems"};

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
		System.out.println("I'm putting together a report of how advanced technologies may be tested and successfully deployed here.");
	}

	@Override
	public void gatherSamples() {
		System.out.println("I'm mappping the morphology of this local region and where to use as test bed for developing advanced technologies of interest.");
	}


}

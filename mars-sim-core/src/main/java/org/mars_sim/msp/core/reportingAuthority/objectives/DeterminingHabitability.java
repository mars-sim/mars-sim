/**
 * Mars Simulation Project
 * DeterminingHabitability.java
 * @version 3.1.0 2017-01-19
 * @author Manny Kung
 */
package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class DeterminingHabitability implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Determining Human Habitability";

	private final String[] phases = new String[] {
								"Collect Meteorological Data",
								"Collect Ice Samples", 
								"Collect Regolith Samples",
								"Analyze Medical Data"};
	
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
		System.out.println("I'm putting together a report of the habitability of this local region for human beings.");
	}

	@Override
	public void gatherSamples() {
		System.out.println("I'm analyzing soil samples, atmospheric condition dataset and geographical factors of how suitable human beings are to live in this local region.");
	}





}

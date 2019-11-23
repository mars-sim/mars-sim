/**
 * Mars Simulation Project
 * DeterminingHabitability.java
 * @version 3.1.0 2017-01-19
 * @author Manny Kung
 */
package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class BuildingSelfSustainingColonies implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Building Self-Sustaining Colonies";

	private final String[] phases = new String[] {
								"Study Meteorological Environmental Factors",
								"Fortify Building Structural Integrity",
								"Refine Techniques for ISRU Polymer Synthesis"
//								"Analyze Medical Data"
								};

	// Note : index for missionModifiers : 
	//	0 : AreologyFieldStudy
	//	1 : BiologyFieldStudy
	//	2 : CollectIce
	//	3 : CollectRegolith	
	//	4 : Exploration
	//	5 : MeteorologyFieldStudy
	//	6 : Mining
	//  7 : Trade
	//  8 : TravelToSettlement

	private final int[][] missionModifiers = new int[][] {
			{3, 0, 0, 0, 0, 9, 0, 0, 0},
			{0, 0, 0, 3, 0, 0, 3, 0, 0},
			{0, 0, 0, 9, 0, 0, 3, 0, 0}
	};
	
	@Override	
	public int[][] getMissionModifiers() {
		return missionModifiers;
	}
	
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
		System.out.println("I'm putting together a report of the best practices in resource utilization.");
	}

	@Override
	public void gatherSamples() {
		System.out.println("I'm analyzing various geological and environment factors affecting how one may build several self-sustainable colonies in this region.");
	}





}

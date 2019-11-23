/**
 * Mars Simulation Project
 * AdvancingSpaceKnowledge.java
 * @version 3.1.0 2017-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class AdvancingScientificKnowledge implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Advancing Scientific Knowledge";
	
	private final String[] phases = new String[] {
			"Collect Astronomical Data",
			"Sample Aerological Data", 
			"Analyze Bio-signature in Rocks"};

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
			{3, 0, 0, 0, 0, 6, 0, 0, 0},
			{9, 0, 0, 0, 3, 0, 0, 0, 0},
			{0, 6, 2, 0, 0, 6, 2, 0, 0}
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
		System.out.println("I'm putting together a report of possible research opportunities in this region.");
	}

	@Override
	public void gatherSamples() {
		System.out.println("I'm analyzing the variation of gravity and atmospheric conditions in this local region for the impact of deploying a laser communication array.");
	}


}

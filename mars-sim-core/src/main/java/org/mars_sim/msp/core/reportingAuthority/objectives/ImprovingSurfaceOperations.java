/**
 * Mars Simulation Project
 * DevelopingSpaceActivityISRO.java
 * @version 3.1.0 2017-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class ImprovingSurfaceOperations implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Pushing Boundaries of Standard Surface Operations";
	
	private final String[] phases = new String[] {
//			"Demonstrate Human Survivability for certain period of time",
			"Stress Test on Human Endurance", 
			"Test Return Vehicle Capability",
			"Characterize Human Interaction Dynamics During Contingency Situations",
			"Extend EVA Operations to Distances beyond a certain radius"};

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
			{0, 0, 0, 0, 3, 0, 0, 0, 2},
			{0, 0, 0, 0, 0, 0, 0, 0, 2},
			{1, 1, 1, 1, 1, 1, 1, 1, 1},
			{1, 1, 1, 1, 1, 1, 1, 1, 1},
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
		System.out.println("I'm putting together reports of the seismic activity in this region.");
	}

	@Override
	public void gatherSamples() {
		System.out.println("I'm analyzing the strength of the soil in this local region for the suitability future spaceport construction.");
	}


}

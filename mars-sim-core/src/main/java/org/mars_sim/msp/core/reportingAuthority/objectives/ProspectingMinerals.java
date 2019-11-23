/**
 * Mars Simulation Project
 * ProspectingMineral.java
 * @version 3.1.0 2017-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class ProspectingMinerals implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Prospercting Precious Minerals on Mars";
	
	private final String[] phases = new String[] {
			"Analyze Various Signature of Minerals",
			"Corroborate Surface Geological Data with On-Orbit Scans",
			"Core Drill Rock Samples from Selected Locations"};

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
			{3, 0, 0, 3, 3, 3, 3, 0, 0},
			{3, 0, 0, 0, 0, 3, 0, 0, 0},
			{0, 0, 0, 0, 4, 0, 6, 0, 0}
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
		System.out.println("I'm putting together reports of the helium-3 and other trace trace content in the collected soil samples.");
	}

	@Override
	public void gatherSamples() {
		System.out.println("I'm analyzing the wealth of mineral contents from the colleted soil samples.");
	}



}

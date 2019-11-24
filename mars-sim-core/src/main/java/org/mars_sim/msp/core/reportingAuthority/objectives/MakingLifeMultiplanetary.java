/**
 * Mars Simulation Project
 * MakingLifeMultiplanetary.java
 * @version 3.1.0 2017-01-19
 * @author Manny Kung
 */
package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class MakingLifeMultiplanetary implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Making Life Multiplanetary";

	private final String[] phases = new String[] {
								"Investigate Interplanetary Transportation Network",
								"Study Terraforming Mars environment", 
								"Investigate Environmentally Sustainable Energy Solutions"
//								"Build Self-Sustaining Colonies"
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
			{0, 0, 0, 0, 3, 3, 0, 0, 3},
			{3, 3, 0, 0, 3, 0, 0, 0, 0},
			{2, 2, 2, 2, 2, 2, 2, 2, 0}
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
		System.out.println("I'm analyzing various geological and environment factors affecting how we may transform Mars into a more hospitable environment to support lives.");
	}





}

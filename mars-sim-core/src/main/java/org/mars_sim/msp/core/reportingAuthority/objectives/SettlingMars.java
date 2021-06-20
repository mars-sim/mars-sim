/**
 * Mars Simulation Project
 * SettlingMars.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class SettlingMars implements MissionAgenda, Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(SettlingMars.class.getName());
	// Mars Society's goal
	private final String name = "Settling Mars";
	
	private final String[] phases = new String[] {
			"Engineer soil capable of hosting and sustaining organic microbial life",
			"Improve Building Structural Integrity", 
			"Minimize Physiological Effects of Long Term Exposure to Martian Environment" 
//			"Gather Hydro-Meteorological Data"
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
			{0, 0, 0, 3, 0, 0, 0, 0, 0},
			{0, 0, 0, 1, 0, 0, 0, 2, 0},
			{1, 1, 1, 1, 1, 1, 1, 1, 1}
	};

	private Unit unit;
	
	public SettlingMars(Unit unit) {
		this.unit = unit;
	}
	
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
		logger.info(unit, 20_000L, "Updating the report of the local in-situ resources "
				+ "that one can collect and process for immediate uses.");
	}

	@Override
	public void gatherSamples() {
		logger.info(unit, 20_000L, "Analyzing structural integrity of geological features for building settlements in this region.");
	}
	
	public void setMissionModifiers() {
	}
}

/**
 * Mars Simulation Project
 * ResearchingSpaceApplication.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class ResearchingHealthHazards implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(ResearchingHealthHazards.class.getName());
	// RKA's goal
	private final String name = "Researching Short and Long Term Health Hazards";
	
	private final String[] agendas = new String[] {
			"Investigate biological hazards",
			"Associate mission operations with human factors and performance", 
//			"Characterize Radiation Countermeasures", 
			"Observe radiation risks, limits and exposures"};

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
			{0, 6, 2, 0, 0, 1, 0, 0, 0},
			{1, 1, 1, 1, 1, 1, 1, 1, 1},
			{1, 1, 1, 1, 1, 1, 1, 1, 1}
	};
	
	private Unit unit;
	
	public ResearchingHealthHazards(Unit unit) {
		this.unit = unit;
	}
	
	@Override	
	public int[][] getMissionModifiers() {
		return missionModifiers;
	}
	
	@Override
	public String[] getAgendas() {
		return agendas;
	}

	@Override
	public String getObjectiveName() {
		return name;
	}

	@Override
	public void reportFindings() {
		logger.info(unit, 20_000L, "Updating the report of the various health hazards for human beings on Mars.");
	}

	@Override
	public void gatherSamples() {
		logger.info(unit, 20_000L, "Analyzing the soil samples from various sites for possible human health hazards");
	}

}

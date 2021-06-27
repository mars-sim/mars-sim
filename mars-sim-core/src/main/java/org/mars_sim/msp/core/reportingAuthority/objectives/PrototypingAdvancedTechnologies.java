/**
 * Mars Simulation Project
 * DevelopingAdvancedTechnology.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class PrototypingAdvancedTechnologies implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(PrototypingAdvancedTechnologies.class.getName());
	// ISRO's goal
	private final String name = "Prototyping Advanced Technologies";
	
	private final String[] agendas = new String[] {
			"Improve rover and flyer range and performance",
			"Prototype new building material", 
			"Test out new propulsion systems"};

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
			{2, 2, 2, 2, 2, 2, 2, 3, 3},
			{0, 0, 0, 4, 0, 0, 4, 2, 0},
			{0, 0, 0, 0, 0, 0, 0, 1, 3}
	};
	
	private Unit unit;
	
	public PrototypingAdvancedTechnologies(Unit unit) {
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
		logger.info(unit, 20_000L, "Updating the report of how advanced technologies may be tested and successfully deployed here.");
	}

	@Override
	public void gatherSamples() {
		logger.info(unit, 20_000L, "Mappping the morphology of this local region and where to use as test bed for developing advanced technologies of interest.");
	}


}

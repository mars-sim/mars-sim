/**
 * Mars Simulation Project
 * DevelopingSpaceActivityISRO.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class ImprovingSurfaceOperations implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(ImprovingSurfaceOperations.class.getName());
	// ESA's goal
	private final String name = "Pushing Boundaries of Standard Surface Operations";
	
	private final String[] agendas = new String[] {
//			"Demonstrate Human Survivability for certain period of time",
			"Stress test on human endurance", 
			"Test return vehicle capability",
			"Characterize dynamics of human interactions",
			"Extend EVA operations to climbing caves"};

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
	public String[] getAgendas() {
		return agendas;
	}


	@Override
	public String getObjectiveName() {
		return name;
	}

	@Override
	public void reportFindings(Worker unit) {
		logger.info(unit, 20_000L, "Updating the report of the human factors in surface operations.");
//		logger.info(unit, 20_000L, "Updating the report of the seismic activity in this region.");
	}

	@Override
	public void gatherSamples(Worker unit) {
		logger.info(unit, 20_000L, "Analyzing the soil's strength in this local region for the suitability of the spaceport construction.");
	}


}

/**
 * Mars Simulation Project
 * AdvancingSpaceKnowledge.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class AdvancingScientificKnowledge implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(AdvancingScientificKnowledge.class.getName());
	// CSA's goal
	private final String name = "Advancing Scientific Knowledge";
	
	private final String[] agendas = new String[] {
			"Collect astronomical data",
			"Sample aerological data", 
			"Analyze bio-signature in rocks"};

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
		
	public AdvancingScientificKnowledge() {
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
	public void reportFindings(Worker unit) {
		logger.info(unit, 20_000L, "Updating the report of possible research opportunities in this region.");
	}

	@Override
	public void gatherSamples(Worker unit) {
		logger.info(unit, 20_000L, "Analyzing the variation of gravity and atmospheric conditions in this local region for the impact of deploying a laser communication array.");
	}


}

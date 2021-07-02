/**
 * Mars Simulation Project
 * MakingLifeMultiplanetary.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class MakingLifeMultiplanetary implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(MakingLifeMultiplanetary.class.getName());
	// SpaceX's goal
	private final String name = "Making Life Multiplanetary";

	private final String[] agendas = new String[] {
								"Conceptualize interplanetary transportation network",
								"Study terraforming Mars environment", 
								"Investigate environmentally sustainable energy solutions"
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
	public String[] getAgendas() {
		return agendas;
	}
	
	@Override
	public String getObjectiveName() {
		return name;
	}

	@Override
	public void reportFindings(Worker unit) {
		logger.info(unit, 20_000L, "Updating the report of the best practices in resource utilization.");
	}

	@Override
	public void gatherSamples(Worker unit) {
		logger.info(unit, 20_000L, "Analyzing various geological and environment factors affecting how we may transform Mars into a more hospitable environment to support lives.");
	}
}

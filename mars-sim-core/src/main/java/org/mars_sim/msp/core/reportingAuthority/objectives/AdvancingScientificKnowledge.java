/**
 * Mars Simulation Project
 * AdvancingSpaceKnowledge.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;
import java.util.Map;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;
import org.mars_sim.msp.core.reportingAuthority.MissionSubAgenda;

public class AdvancingScientificKnowledge implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(AdvancingScientificKnowledge.class.getName());
	// CSA's goal
	private final String name = "Advancing Scientific Knowledge";
	
	private final MissionSubAgenda[] subs = new MissionSubAgenda[] {
			new MissionSubAgenda("Collect astronomical data",
					Map.of(MissionType.AREOLOGY, 3,
							MissionType.METEOROLOGY, 6)),
			new MissionSubAgenda("Sample aerological data",
					Map.of(MissionType.AREOLOGY, 9,
							MissionType.EXPLORATION, 3)),
			new MissionSubAgenda("Analyze bio-signature in rocks",
					Map.of(MissionType.BIOLOGY, 6,
							MissionType.COLLECT_ICE, 2,
							MissionType.METEOROLOGY, 6,
							MissionType.MINING, 2))
	};

	@Override
	public MissionSubAgenda[] getAgendas() {
		return subs;
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

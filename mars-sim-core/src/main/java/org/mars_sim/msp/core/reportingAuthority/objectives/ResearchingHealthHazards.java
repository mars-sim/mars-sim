/**
 * Mars Simulation Project
 * ResearchingSpaceApplication.java
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

public class ResearchingHealthHazards implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(ResearchingHealthHazards.class.getName());
	// RKA's goal
	private final String name = "Researching Short and Long Term Health Hazards";

	private final MissionSubAgenda[] subs = new MissionSubAgenda[] {
			new MissionSubAgenda("Investigate biological hazards",
					Map.of(MissionType.BIOLOGY, 6,
							MissionType.COLLECT_ICE, 2,
							MissionType.METEOROLOGY, 1)),
			new MissionSubAgenda("Study underground water reserve",
					Map.of(MissionType.AREOLOGY, 1,
							MissionType.BIOLOGY, 1,
							MissionType.COLLECT_ICE, 1,
							MissionType.COLLECT_REGOLITH, 1,
							MissionType.EXPLORATION, 9,
							MissionType.MINING, 9)),
			new MissionSubAgenda("Observe radiation risks, limits and exposures",
					Map.of(MissionType.AREOLOGY, 1,
							MissionType.BIOLOGY, 1,
							MissionType.COLLECT_ICE, 1,
							MissionType.COLLECT_REGOLITH, 1,
							MissionType.TRADE, 1,
							MissionType.TRAVEL_TO_SETTLEMENT, 1))
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
		logger.info(unit, 20_000L, "Updating the report of the various health hazards for human beings on Mars.");
	}

	@Override
	public void gatherSamples(Worker unit) {
		logger.info(unit, 20_000L, "Analyzing the soil samples from various sites for possible human health hazards");
	}

}

/**
 * Mars Simulation Project
 * MakingLifeMultiplanetary.java
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

public class MakingLifeMultiplanetary implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(MakingLifeMultiplanetary.class.getName());
	// SpaceX's goal
	private final String name = "Making Life Multiplanetary";

	private final MissionSubAgenda[] subs = new MissionSubAgenda[] {
			new MissionSubAgenda("Conceptualize interplanetary transportation network",
					Map.of(MissionType.DELIVERY, 9,
							MissionType.EMERGENCY_SUPPLY, 3,
							MissionType.EXPLORATION, 3,
							MissionType.METEOROLOGY, 3,
							MissionType.RESCUE_SALVAGE_VEHICLE, 6,
							MissionType.TRAVEL_TO_SETTLEMENT, 3)),
			new MissionSubAgenda("Study terraforming Mars environment",
					Map.of(MissionType.AREOLOGY, 3,
							MissionType.BIOLOGY, 3,
							MissionType.EXPLORATION, 3)),
			new MissionSubAgenda("Investigate environmentally sustainable energy solutions",
					Map.of(MissionType.AREOLOGY, 2,
							MissionType.BIOLOGY, 2,
							MissionType.COLLECT_ICE, 2,
							MissionType.COLLECT_REGOLITH, 2,
							MissionType.DELIVERY, 2,
							MissionType.EXPLORATION, 2,
							MissionType.METEOROLOGY, 2,
							MissionType.MINING, 2,
							MissionType.TRADE, 2))
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
		logger.info(unit, 20_000L, "Updating the report of the best practices in resource utilization.");
	}

	@Override
	public void gatherSamples(Worker unit) {
		logger.info(unit, 20_000L, "Analyzing various geological and environment factors affecting how we may transform Mars into a more hospitable environment to support lives.");
	}
}

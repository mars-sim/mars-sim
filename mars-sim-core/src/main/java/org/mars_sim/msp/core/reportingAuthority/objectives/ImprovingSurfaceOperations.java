/**
 * Mars Simulation Project
 * DevelopingSpaceActivityISRO.java
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

public class ImprovingSurfaceOperations implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(ImprovingSurfaceOperations.class.getName());
	// ESA's goal
	private final String name = "Pushing Boundaries of Surface Operations";

	private final MissionSubAgenda[] subs = new MissionSubAgenda[] {
			new MissionSubAgenda("Stress test on human endurance",
					Map.of(MissionType.EMERGENCY_SUPPLY, 3,
							MissionType.EXPLORATION, 3,
							MissionType.TRAVEL_TO_SETTLEMENT, 2)),
			new MissionSubAgenda("Test return vehicle capability",
					Map.of(MissionType.DELIVERY, 3,
							MissionType.EMERGENCY_SUPPLY, 3,
							MissionType.RESCUE_SALVAGE_VEHICLE, 3,
							MissionType.TRAVEL_TO_SETTLEMENT, 2)),
			new MissionSubAgenda("Characterize dynamics of human interactions",
					Map.of(MissionType.AREOLOGY, 1,
							MissionType.BIOLOGY, 1,
							MissionType.COLLECT_ICE, 1,
							MissionType.COLLECT_REGOLITH, 1,
							MissionType.EXPLORATION, 1,
							MissionType.METEOROLOGY, 1,
							MissionType.MINING, 1,
							MissionType.TRADE, 1,
							MissionType.TRAVEL_TO_SETTLEMENT, 1)),
			new MissionSubAgenda("Extend EVA operations to climbing caves",
					Map.of(MissionType.AREOLOGY, 1,
							MissionType.BIOLOGY, 1,
							MissionType.COLLECT_ICE, 1,
							MissionType.COLLECT_REGOLITH, 1,
							MissionType.EXPLORATION, 1,
							MissionType.METEOROLOGY, 1,
							MissionType.MINING, 1,
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
		logger.info(unit, 20_000L, "Updating the report of the human factors in surface operations.");
//		logger.info(unit, 20_000L, "Updating the report of the seismic activity in this region.");
	}

	@Override
	public void gatherSamples(Worker unit) {
		logger.info(unit, 20_000L, "Analyzing the soil's strength in this local region for the suitability of the spaceport construction.");
	}


}

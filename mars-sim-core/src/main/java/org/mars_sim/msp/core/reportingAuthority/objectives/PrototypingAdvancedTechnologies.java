/**
 * Mars Simulation Project
 * DevelopingAdvancedTechnology.java
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

public class PrototypingAdvancedTechnologies implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(PrototypingAdvancedTechnologies.class.getName());
	// ISRO's goal
	private final String name = "Prototyping Advanced Technologies";

	private final MissionSubAgenda[] subs = new MissionSubAgenda[] {
			new MissionSubAgenda("Improve rover and flyer range and performance",
					Map.of(MissionType.AREOLOGY, 2,
							MissionType.BIOLOGY, 2,
							MissionType.EMERGENCY_SUPPLY, 6,
							MissionType.EXPLORATION, 2,
							MissionType.RESCUE_SALVAGE_VEHICLE, 6,
							MissionType.TRADE, 3,
							MissionType.TRAVEL_TO_SETTLEMENT, 3)),
			new MissionSubAgenda("Prototype new building material",
					Map.of(MissionType.COLLECT_REGOLITH, 4,
							MissionType.MINING, 4,
							MissionType.TRADE, 2)),
			new MissionSubAgenda("Test out new propulsion systems",
					Map.of(MissionType.DELIVERY, 1,
							MissionType.TRADE, 1,
							MissionType.TRAVEL_TO_SETTLEMENT, 3))
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
		logger.info(unit, 20_000L, "Updating the report of how advanced technologies may be tested and successfully deployed here.");
	}

	@Override
	public void gatherSamples(Worker unit) {
		logger.info(unit, 20_000L, "Mappping the morphology of this local region and where to use as test bed for developing advanced technologies of interest.");
	}


}

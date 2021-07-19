/**
 * Mars Simulation Project
 * SettlingMars.java
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

public class SettlingMars implements MissionAgenda, Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(SettlingMars.class.getName());
	// Mars Society's goal
	private final String name = "Settling Mars";

	private final MissionSubAgenda[] subs = new MissionSubAgenda[] {
			new MissionSubAgenda("Engineer soil capable of hosting and sustaining organic microbial life",
					Map.of(MissionType.COLLECT_REGOLITH, 3)),
			new MissionSubAgenda("Improve building structural integrity",
					Map.of(MissionType.COLLECT_REGOLITH, 1,
							MissionType.TRADE, 2)),
			new MissionSubAgenda("Minimize physiological effects of long term exposure to martian environment",
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
		logger.info(unit, 20_000L, "Updating the report of the local in-situ resources "
				+ "that one can collect and process for immediate uses.");
	}

	@Override
	public void gatherSamples(Worker unit) {
		logger.info(unit, 20_000L, "Analyzing structural integrity of geological features for building settlements in this region.");
	}
	
	public void setMissionModifiers() {
	}
}

/**
 * Mars Simulation Project
 * ProspectingMineral.java
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

public class ProspectingMinerals implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(ProspectingMinerals.class.getName());
	// CNSA's goal
	private final String name = "Prospercting Precious Minerals on Mars";

	private final MissionSubAgenda[] subs = new MissionSubAgenda[] {
			new MissionSubAgenda("Analyze various signatures of minerals",
					Map.of(MissionType.AREOLOGY, 3,
							MissionType.COLLECT_REGOLITH, 3,
							MissionType.EXPLORATION, 3,
							MissionType.METEOROLOGY, 3,
							MissionType.MINING, 3)),
			new MissionSubAgenda("Corroborate surface geological data with on-orbit scans",
					Map.of(MissionType.AREOLOGY, 3,
							MissionType.METEOROLOGY, 3)),
			new MissionSubAgenda("Core drill rock samples from selected locations",
					Map.of(MissionType.EXPLORATION, 4,
							MissionType.MINING, 6))
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
		logger.info(unit, 20_000L, "Updating the report of trace mineral content in the collected soil samples.");
	}

	@Override
	public void gatherSamples(Worker unit) {
		logger.info(unit, 20_000L, "Analyzing the wealth of mineral contents from the colleted soil samples.");
	}



}

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

public class DevelopingSpaceApplications implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(DevelopingSpaceApplications.class.getName());
	// JAXA's goal
	private final String name = "Developing Practical Space Applications";
	
	private final MissionSubAgenda[] subs = new MissionSubAgenda[] {
			new MissionSubAgenda("Test space-ground links",
					Map.of(MissionType.AREOLOGY, 3,
							MissionType.EXPLORATION, 3,
							MissionType.METEOROLOGY, 3,
							MissionType.TRADE, 1)),
			new MissionSubAgenda("Test new flexible space suit micro fabric",
					Map.of(MissionType.BIOLOGY, 3,
							MissionType.METEOROLOGY, 3,
							MissionType.TRADE, 1)),
			new MissionSubAgenda("Improve closed-loop life support system",
					Map.of(MissionType.COLLECT_REGOLITH, 9,
							MissionType.METEOROLOGY, 3,
							MissionType.MINING, 3))
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
		logger.info(unit, 20_000L, "Updating the report of possible applied space research in this frontier.");
	}

	@Override
	public void gatherSamples(Worker unit) {
		logger.info(unit, 20_000L, "Analyzing how this local region may impact the scope of our research of interest.");
	}
}

/**
 * Mars Simulation Project
 * FindingLife.java
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

public class FindingLife implements MissionAgenda, Serializable  {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(FindingLife.class.getName());
	// NASA's goal
	private final String name = "Finding Life Past and Present on Mars";

	private final MissionSubAgenda[] subs = new MissionSubAgenda[] {
			new MissionSubAgenda("Follow the water",
					Map.of(
							MissionType.BIOLOGY, 3,
							MissionType.COLLECT_ICE, 9)),
			new MissionSubAgenda("Search regions capable hosting/sustaining microbial life",
					Map.of(
							MissionType.BIOLOGY, 9)),
			new MissionSubAgenda("Core drill rock samples from selected locations",
					Map.of(
							MissionType.EXPLORATION, 9,
							MissionType.MINING, 3)),
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
		logger.info(unit, 20_000L, "Updating the report of the oxygen content in the soil samples.");
	}

	@Override
	public void gatherSamples(Worker unit) {
		logger.info(unit, 20_000L, "Analyzing the soil samples from various sites for the amount of oxygen and water contents.");
	}
}

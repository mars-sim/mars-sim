/**
 * Mars Simulation Project
 * DeterminingHabitability.java
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

public class BuildingSelfSustainingColonies implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(BuildingSelfSustainingColonies.class.getName());

	private final String name = "Building Self-Sustaining Colonies";

	private final MissionSubAgenda[] subs = new MissionSubAgenda[] {
			new MissionSubAgenda("Study meteorological environmental factors",
					Map.of(MissionType.AREOLOGY, 3,
							MissionType.METEOROLOGY, 9)),
			new MissionSubAgenda("Fortify building structural integrity",
					Map.of(MissionType.COLLECT_REGOLITH, 3,
							MissionType.MINING, 3)),
			new MissionSubAgenda("Refine techniques for ISRU polymer synthesis",
					Map.of(MissionType.COLLECT_REGOLITH, 9,
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
		logger.info(unit, 20_000L, "Updating the report of the best practices in resource utilization.");
	}

	@Override
	public void gatherSamples(Worker unit) {
		logger.info(unit, 20_000L, "Analyzing various geological and environment factors affecting how one may build several self-sustainable colonies in this region.");
	}
}

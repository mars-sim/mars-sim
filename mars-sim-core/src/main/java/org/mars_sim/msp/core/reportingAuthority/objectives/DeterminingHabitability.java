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

public class DeterminingHabitability implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(DeterminingHabitability.class.getName());

	private final String name = "Determining Human Habitability";

	private final MissionSubAgenda[] subs = new MissionSubAgenda[] {
			new MissionSubAgenda("Predict meteorological changes",
					Map.of(
							MissionType.AREOLOGY, 3,
							MissionType.METEOROLOGY, 9)),
			new MissionSubAgenda("Study underground water reserve",
					Map.of(
							MissionType.COLLECT_ICE, 9)),
			new MissionSubAgenda("Characterize radiation countermeasures",
					Map.of(
							MissionType.BIOLOGY, 3,
							MissionType.METEOROLOGY, 3)),
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
		logger.info(unit, 20_000L, "Updating the report of the habitability of this local region for human beings.");
	}

	@Override
	public void gatherSamples(Worker unit) {
		logger.info(unit, 20_000L, "Analyzing soil samples, atmospheric condition dataset and geographical factors of how suitable human beings are to live in this local region.");
	}





}

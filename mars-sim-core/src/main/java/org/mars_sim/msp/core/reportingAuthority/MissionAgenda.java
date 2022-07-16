/*
 * Mars Simulation Project
 * MissionAgenda.java
 * @date 2022-07-15
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;

/**
 * Mission agenda for a ReportingAuthority to follow. It defines a set
 * of subagendas that specific actual targets for the RA.
 */
public class MissionAgenda implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(MissionAgenda.class.getName());

	private List<MissionSubAgenda> subs;
	private String name;
	private String objective;
	private String findings;
	private String samples;
	
	MissionAgenda(String name, String objective, List<MissionSubAgenda> subs, String findings, String samples) {
		super();
		this.name = name;
		this.objective = objective;
		this.subs = Collections.unmodifiableList(subs);
		this.findings = findings;
		this.samples = samples;
	}

	/**
	 * Gets the agendas for this mission.
	 * 
	 * @return
	 */
	public List<MissionSubAgenda> getAgendas() {
		return subs;
	}

	/**
	 * Gets unique name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the overall objective.
	 * 
	 * @return
	 */
	public String getObjectiveName() {
		return objective;
	}

	/**
	 * Reports some findings by a Worker. This may adjust characteristics of the RA.
	 * 
	 * @param unit
	 */
	public void reportFindings(Worker unit) {
		logger.fine(unit, name + ": " + findings);
	}

	/**
	 * Gathers some samples as part of this agenda.
	 * 
	 * @param unit
	 */
	public void gatherSamples(Worker unit) {
		logger.fine(unit, name + ": " + samples);
	}
	
	public String toString() {
		return name;
	}
}

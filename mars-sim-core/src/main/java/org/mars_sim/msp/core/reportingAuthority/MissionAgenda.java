/*
 * Mars Simulation Project
 * MissionAgenda.java
 * @date 2023-05-31
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.task.util.Worker;

/**
 * Mission agenda for a ReportingAuthority to follow. Each agenda has a set
 * of capability to develop.
 */
public class MissionAgenda implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(MissionAgenda.class.getName());

	private List<MissionCapability> caps;
	private String name;
	private String objective;
	private String findings;
	private String data;
	
	MissionAgenda(String name, String objective, List<MissionCapability> caps, String findings, String data) {
		super();
		this.name = name;
		this.objective = objective;
		this.caps = Collections.unmodifiableList(caps);
		this.findings = findings;
		this.data = data;
	}

	/**
	 * Gets the agendas for this mission.
	 * 
	 * @return
	 */
	public List<MissionCapability> getCapabilities() {
		return caps;
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
	 * Returns the report findings.
	 * 
	 * @return
	 */
	public String getReports() {
		return findings;
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
	 * Returns the data collection.
	 * 
	 * @return
	 */
	public String getData() {
		return data;
	}

	/**
	 * Gathers data as part of this agenda.
	 * 
	 * @param unit
	 */
	public void gatherData(Worker unit) {
		logger.fine(unit, name + ": " + data);
	}
	
	public String toString() {
		return name;
	}
}

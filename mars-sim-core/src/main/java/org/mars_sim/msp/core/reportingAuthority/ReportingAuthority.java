/**
 * Mars Simulation Project
 * ReportingAuthority.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;
import java.util.List;

import org.mars_sim.msp.core.person.ai.task.utils.Worker;

public class ReportingAuthority
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private MissionAgenda missionAgenda;

	private String name;

	private String code;

	private List<String> countries;
 
	ReportingAuthority(String code, String name,
							  MissionAgenda agenda, List<String> countries) {
		this.name  = name;
		this.code = code;
		this.missionAgenda = agenda;
		this.countries = countries;
	}
	
	protected ReportingAuthority() {
	}
	
	/**
	 * Work ont he mission objectives conducted
	 * @param unit
	 */
	public void conductMissionObjective(Worker unit) {
		missionAgenda.reportFindings(unit);
		missionAgenda.gatherSamples(unit);
	}

	/**
	 * Get the Mission Agenda for this authority
	 * @return
	 */
	public MissionAgenda getMissionAgenda() {
		return missionAgenda;
	}

	/**
	 * Get the associated short code.
	 * @return
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * Get the full name of the authority
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the countries associated to this Authority.
	 * @return
	 */
	public List<String> getCountries() {
		return countries;
	}
}

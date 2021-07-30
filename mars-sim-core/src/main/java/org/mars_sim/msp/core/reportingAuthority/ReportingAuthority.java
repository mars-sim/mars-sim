/**
 * Mars Simulation Project
 * ReportingAuthority.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.person.ai.task.utils.Worker;

public class ReportingAuthority
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	protected MissionAgenda missionAgenda;

	private String name;

	private ReportingAuthorityType org;
 
	ReportingAuthority(ReportingAuthorityType org, String name,
							  MissionAgenda agenda) {
		this.name  = name;
		this.org = org;
		this.missionAgenda = agenda;
	}
	
	protected ReportingAuthority() {
	}
	

	public void conductMissionObjective(Worker unit) {
		missionAgenda.reportFindings(unit);
		missionAgenda.gatherSamples(unit);
	}

	public void setMissionAgenda(MissionAgenda missionAgenda) {
		this.missionAgenda = missionAgenda;
	}

	public MissionAgenda getMissionAgenda() {
		return missionAgenda;
	}

	public ReportingAuthorityType getOrg() {
		return org;
	}
	
	public String getToolTipStr() {
		return name;
	}
}

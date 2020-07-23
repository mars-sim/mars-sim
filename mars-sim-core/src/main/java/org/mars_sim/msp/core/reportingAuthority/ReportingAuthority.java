/**
 * Mars Simulation Project
 * ReportingAuthority.java
 * @version 3.1.1 2020-07-22
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

public abstract class ReportingAuthority
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	protected MissionAgenda missionAgenda;


	public ReportingAuthority() {
	}

	public void conductMissionObjective() {
		missionAgenda.reportFindings();
		missionAgenda.gatherSamples();
	}
	//public void setFindingLife(FindingLife findingLife) {
	//	this.findingLife = findingLife;
	//}

	public void setMissionAgenda(MissionAgenda missionAgenda) {
		this.missionAgenda = missionAgenda;
	}

	public MissionAgenda getMissionAgenda() {
		return missionAgenda;
	}

	//public abstract String getName();

	public abstract ReportingAuthorityType getOrg();
	
	public abstract String getToolTipStr();
}

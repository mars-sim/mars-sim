/**
 * Mars Simulation Project
 * CSAMissionControl.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.objectives.AdvancingScientificKnowledge;

/*
 * This class represents the Mission Control of the Canadian Space Agency (CSA)
 */
class CSAMissionControl extends ReportingAuthority
implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private final String name = "CSA";

	private final ReportingAuthorityType org = ReportingAuthorityType.CSA;

	private final String toolTipStr = "Canadian Space Agency";
	
	public String getToolTipStr() {
		return toolTipStr;
	}

	//public String getName() {
	//	return name;
	//}

	CSAMissionControl() {
		missionAgenda = new AdvancingScientificKnowledge();
	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

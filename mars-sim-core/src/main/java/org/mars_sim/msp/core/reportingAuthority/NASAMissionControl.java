/**
 * Mars Simulation Project
 * NASAMissionControl.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.objectives.FindingLife;

/*
 * This class represents the Mission Control of the National Aeronautics and Space Administration (NASA)
 */
class NASAMissionControl extends ReportingAuthority
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private final String name = "NASA";

	private final ReportingAuthorityType org = ReportingAuthorityType.NASA;

	private final String toolTipStr = "National Aeronautics and Space Administration";

	public String getToolTipStr() {
		return toolTipStr;
	}

	//public String getName() {
	//	return name;
	//}

	public ReportingAuthorityType getOrg() {
		return org;
	}


	NASAMissionControl() {
		missionAgenda = new FindingLife();
	}
}

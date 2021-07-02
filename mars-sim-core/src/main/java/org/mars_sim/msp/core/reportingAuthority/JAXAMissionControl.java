/**
 * Mars Simulation Project
 * JAXAMissionControl.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.objectives.DevelopingSpaceApplications;

/*
 * This class represents the Mission Control of the Japan Aerospace Exploration Agency
 */
class JAXAMissionControl extends ReportingAuthority
implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private final String name = "JAXA";

	private final ReportingAuthorityType org = ReportingAuthorityType.JAXA;

	private final String toolTipStr = "Japan Aerospace Exploration Agency";

	public String getToolTipStr() {
		return toolTipStr;
	}

	//public String getName() {
	//	return name;
	//}

	JAXAMissionControl() {
		missionAgenda = new DevelopingSpaceApplications();
	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

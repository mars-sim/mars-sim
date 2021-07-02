/**
 * Mars Simulation Project
 * RKAMissionControl.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.objectives.ResearchingHealthHazards;

/*
 * This class represents the Mission Control of the Roscosmos (RKA)
 */
class RKAMissionControl extends ReportingAuthority
implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private final String name = "RKA";

	private final ReportingAuthorityType org = ReportingAuthorityType.RKA;

	private final String toolTipStr = "Roscosmos";

	public String getToolTipStr() {
		return toolTipStr;
	}

	//public String getName() {
	//	return name;
	//}

	RKAMissionControl() {
		missionAgenda = new ResearchingHealthHazards();
	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

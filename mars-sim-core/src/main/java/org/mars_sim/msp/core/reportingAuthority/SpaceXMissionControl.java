/**
 * Mars Simulation Project
 * SpaceXMissionControl.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.objectives.MakingLifeMultiplanetary;

/*
 * This class represents the Mission Control of the Mars Society
 */
class SpaceXMissionControl extends ReportingAuthority
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private final String name = "MS";

	private final ReportingAuthorityType org = ReportingAuthorityType.SPACEX;

	private final String toolTipStr = "SpaceX";
	
	//public String getName() {
	//	return name;
	//}

	public String getToolTipStr() {
		return toolTipStr;
	}

	SpaceXMissionControl() {
		missionAgenda = new MakingLifeMultiplanetary();
	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

/**
 * Mars Simulation Project
 * MarsSocietyMissionControl.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.objectives.SettlingMars;

/*
 * This class represents the Mission Control of the Mars Society
 */
class MarsSocietyMissionControl extends ReportingAuthority
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private final String name = "MS";

	private final ReportingAuthorityType org = ReportingAuthorityType.MS;

	private final String toolTipStr = "Mars Society";

	//public String getName() {
	//	return name;
	//}

	public String getToolTipStr() {
		return toolTipStr;
	}

	MarsSocietyMissionControl() {
		missionAgenda = new SettlingMars();
	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

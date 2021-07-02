/**
 * Mars Simulation Project
 * CNSAissionControl.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.objectives.ProspectingMinerals;

/*
 * This class represents the Mission Control of the China National Space Administration (CNSA)
 */
class CNSAMissionControl extends ReportingAuthority
implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private final String name = "CNSA";

	private final ReportingAuthorityType org = ReportingAuthorityType.CNSA;

	private final String toolTipStr = "China National Space Administration";
	
	public String getToolTipStr() {
		return toolTipStr;
	}

	//public String getName() {
	//	return name;
	//}

	CNSAMissionControl() {
		missionAgenda = new ProspectingMinerals();
	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

/**
 * Mars Simulation Project
 * CSAMissionControl.java
 * @version 3.08 2015-10-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

/*
 * This class represents the Mission Control of the Canadian Space Agency (CSA)
 */
public class CSAMissionControl extends ReportingAuthority
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

	public CSAMissionControl() {
		missionAgenda = new AdvancingSpaceKnowledge();

	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

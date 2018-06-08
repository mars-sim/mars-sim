/**
 * Mars Simulation Project
 * CSAMissionControl.java
  * @version 3.1.0 2017-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.objectives.AdvancingScientificKnowledge;

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

	private CSAMissionControl() {
		missionAgenda = new AdvancingScientificKnowledge();
	}

	public static CSAMissionControl createMissionControl() {
		return new CSAMissionControl();
	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

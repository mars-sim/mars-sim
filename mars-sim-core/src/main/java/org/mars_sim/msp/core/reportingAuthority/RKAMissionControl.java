/**
 * Mars Simulation Project
 * RKAMissionControl.java
 * @version 3.08 2015-10-05
 * @author Manny Kung
 */
package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

/*
 * This class represents the Mission Control of the Roscosmos (RKA)
 */
public class RKAMissionControl extends ReportingAuthority
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

	public RKAMissionControl() {
		missionAgenda = new ResearchingHealthHazard();

	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

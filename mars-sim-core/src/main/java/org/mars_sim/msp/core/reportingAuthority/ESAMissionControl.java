/**
 * Mars Simulation Project
 * ESAMissionControl.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.objectives.ImprovingSurfaceOperations;

/*
 * This class represents the Mission Control of the European Space Agency (ESA)
 */
class ESAMissionControl extends ReportingAuthority
implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private final String name = "ESA";

	private final ReportingAuthorityType org = ReportingAuthorityType.ESA;

	private final String toolTipStr = "European Space Agency";

	public String getToolTipStr() {
		return toolTipStr;
	}

	//public String getName() {
	//	return name;
	//}

	ESAMissionControl() {
		missionAgenda = new ImprovingSurfaceOperations();
	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

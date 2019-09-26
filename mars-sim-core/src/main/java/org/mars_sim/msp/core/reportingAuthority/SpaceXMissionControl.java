/**
 * Mars Simulation Project
 * SpaceXMissionControl.java
 * @version 3.1.0 2017-06-17
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.objectives.MakingLifeMultiplanetary;

/*
 * This class represents the Mission Control of the Mars Society
 */
public class SpaceXMissionControl extends ReportingAuthority
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

	private SpaceXMissionControl() {
		missionAgenda = new MakingLifeMultiplanetary();
	}

	public static SpaceXMissionControl createMissionControl() {
		return new SpaceXMissionControl();
	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

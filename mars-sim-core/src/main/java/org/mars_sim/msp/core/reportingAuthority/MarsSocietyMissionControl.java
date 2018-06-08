/**
 * Mars Simulation Project
 * MarsSocietyMissionControl.java
 * @version 3.1.0 2017-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.objectives.DeterminingHabitability;

/*
 * This class represents the Mission Control of the Mars Society
 */
public class MarsSocietyMissionControl extends ReportingAuthority
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private final String name = "MS";

	private final ReportingAuthorityType org = ReportingAuthorityType.MARS_SOCIETY;

	private final String toolTipStr = "Mars Society";

	//public String getName() {
	//	return name;
	//}

	public String getToolTipStr() {
		return toolTipStr;
	}

	private MarsSocietyMissionControl() {
		missionAgenda = new DeterminingHabitability();
	}

	public static MarsSocietyMissionControl createMissionControl() {
		return new MarsSocietyMissionControl();
	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

/**
 * Mars Simulation Project
 * MarsSocietyMissionControl.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.reportingAuthority.objectives.SettlingMars;

/*
 * This class represents the Mission Control of the Mars Society
 */
public class MarsSocietyMissionControl extends ReportingAuthority
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private final String name = "MS";

	private final ReportingAuthorityType org = ReportingAuthorityType.MS;

	private final String toolTipStr = "Mars Society";

	private Unit unit;

	//public String getName() {
	//	return name;
	//}

	public String getToolTipStr() {
		return toolTipStr;
	}

	private MarsSocietyMissionControl(Unit unit) {
		this.unit = unit;
		missionAgenda = new SettlingMars(unit);
	}

	public static MarsSocietyMissionControl createMissionControl(Unit unit) {
		return new MarsSocietyMissionControl(unit);
	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

/**
 * Mars Simulation Project
 * SpaceXMissionControl.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;
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

	private Unit unit;
	
	//public String getName() {
	//	return name;
	//}

	public String getToolTipStr() {
		return toolTipStr;
	}

	private SpaceXMissionControl(Unit unit) {
		this.unit = unit;
		missionAgenda = new MakingLifeMultiplanetary(unit);
	}

	public static SpaceXMissionControl createMissionControl(Unit unit) {
		return new SpaceXMissionControl(unit);
	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

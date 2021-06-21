/**
 * Mars Simulation Project
 * NASAMissionControl.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.reportingAuthority.objectives.FindingLife;



/*
 * This class represents the Mission Control of the National Aeronautics and Space Administration (NASA)
 */
public class NASAMissionControl extends ReportingAuthority
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private final String name = "NASA";

	private final ReportingAuthorityType org = ReportingAuthorityType.NASA;

	private final String toolTipStr = "National Aeronautics and Space Administration";

	private Unit unit;

	public String getToolTipStr() {
		return toolTipStr;
	}

	//public String getName() {
	//	return name;
	//}

	public ReportingAuthorityType getOrg() {
		return org;
	}


	private NASAMissionControl(Unit unit) {
		this.unit = unit;
		missionAgenda = new FindingLife(unit);
	}

	public static NASAMissionControl createMissionControl(Unit unit) {
		return new NASAMissionControl(unit);
	}

}

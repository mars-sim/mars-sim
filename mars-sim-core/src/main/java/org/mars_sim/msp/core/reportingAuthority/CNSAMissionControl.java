/**
 * Mars Simulation Project
 * CNSAissionControl.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.reportingAuthority.objectives.ProspectingMinerals;

/*
 * This class represents the Mission Control of the China National Space Administration (CNSA)
 */
public class CNSAMissionControl extends ReportingAuthority
implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private final String name = "CNSA";

	private final ReportingAuthorityType org = ReportingAuthorityType.CNSA;

	private final String toolTipStr = "China National Space Administration";

	private Unit unit;
	
	public String getToolTipStr() {
		return toolTipStr;
	}

	//public String getName() {
	//	return name;
	//}

	private CNSAMissionControl(Unit unit) {
		this.unit = unit;
		missionAgenda = new ProspectingMinerals(unit);
	}

	public static CNSAMissionControl createMissionControl(Unit unit) {
		return new CNSAMissionControl(unit);
	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

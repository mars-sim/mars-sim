/**
 * Mars Simulation Project
 * ISROissionControl.java
 * @version 3.1.0 2017-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.objectives.PrototypingAdvancedTechnologies;

/*
 * This class represents the Mission Control of the Indian Space Research Organisation (ISRO)
 */
public class ISROMissionControl extends ReportingAuthority
implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private final String name = "ISRO";

	private final ReportingAuthorityType org = ReportingAuthorityType.ISRO;

	private final String toolTipStr = "The Indian Space Research Organisation";

	public String getToolTipStr() {
		return toolTipStr;
	}

	//public String getName() {
	//	return name;
	//}

	private ISROMissionControl() {
		missionAgenda = new PrototypingAdvancedTechnologies();
	}

	public static ISROMissionControl createMissionControl() {
		return new ISROMissionControl();
	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

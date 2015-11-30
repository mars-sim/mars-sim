/**
 * Mars Simulation Project
 * ISROissionControl.java
 * @version 3.08 2015-10-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

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

	public ISROMissionControl() {
		missionAgenda = new DevelopingAdvancedTechnology();

	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

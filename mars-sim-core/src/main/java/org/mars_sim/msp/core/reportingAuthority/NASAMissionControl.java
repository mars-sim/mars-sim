/**
 * Mars Simulation Project
 * NASAMissionControl.java
 * @version 3.08 2015-10-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;



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

	public String getToolTipStr() {
		return toolTipStr;
	}

	//public String getName() {
	//	return name;
	//}
	
	public ReportingAuthorityType getOrg() {
		return org;
	}
	

	public NASAMissionControl() {
		missionAgenda = new FindingLife();

	}


}

/**
 * Mars Simulation Project
 * RKAMissionControl.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.reportingAuthority.objectives.ResearchingHealthHazards;

/*
 * This class represents the Mission Control of the Roscosmos (RKA)
 */
public class RKAMissionControl extends ReportingAuthority
implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private final String name = "RKA";

	private final ReportingAuthorityType org = ReportingAuthorityType.RKA;

	private final String toolTipStr = "Roscosmos";

	private Unit unit;

	public String getToolTipStr() {
		return toolTipStr;
	}

	//public String getName() {
	//	return name;
	//}

	private RKAMissionControl(Unit unit) {
		this.unit = unit;
		missionAgenda = new ResearchingHealthHazards(unit);
	}

	public static RKAMissionControl createMissionControl(Unit unit) {
		return new RKAMissionControl(unit);
	}

	@Override
	public ReportingAuthorityType getOrg() {
		return org;
	}

}

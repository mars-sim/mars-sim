/**
 * Mars Simulation Project
 * ReportingAuthorityType.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import org.mars_sim.msp.core.Msg;

public enum ReportingAuthorityType {

	CNSA,
	CSA,
	ESA,
	ISRO,
	JAXA,
	MS,
	NASA,
	RKA,
	SPACEX;

	
	private final String shortName;
	
	private final String longName;
	

	/** hidden constructor. */
	private ReportingAuthorityType() {
		shortName = Msg.getString("ReportingAuthorityType." + name()); //$NON-NLS-1$
		longName = Msg.getString("ReportingAuthorityType.long." + name());
	}
	
	public final String getShortName() {
		return this.shortName;
	}

	public final String getLongName() {
		return this.longName;
	}
}

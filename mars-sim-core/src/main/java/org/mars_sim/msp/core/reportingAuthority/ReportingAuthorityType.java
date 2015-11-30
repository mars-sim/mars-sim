/**
 * Mars Simulation Project
 * ReportingAuthorityType.java
 * @version 3.08 2015-11-29
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import org.mars_sim.msp.core.Msg;

public enum ReportingAuthorityType {

	CNSA				(Msg.getString("ReportingAuthorityType.CNSA")), //$NON-NLS-1$
	CSA					(Msg.getString("ReportingAuthorityType.CSA")), //$NON-NLS-1$
	ESA					(Msg.getString("ReportingAuthorityType.ESA")), //$NON-NLS-1$
	ISRO				(Msg.getString("ReportingAuthorityType.ISRO")), //$NON-NLS-1$
	JAXA				(Msg.getString("ReportingAuthorityType.JAXA")), //$NON-NLS-1$
	MARS_SOCIETY		(Msg.getString("ReportingAuthorityType.MarsSociety")), //$NON-NLS-1$
	NASA				(Msg.getString("ReportingAuthorityType.NASA")), //$NON-NLS-1$
	RKA					(Msg.getString("ReportingAuthorityType.RKA")) //$NON-NLS-1$
	;

	private String name;

	/** hidden constructor. */
	private ReportingAuthorityType(String name) {
		this.name = name;
	}
	
	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
}

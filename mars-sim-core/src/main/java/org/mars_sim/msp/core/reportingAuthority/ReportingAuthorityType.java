/**
 * Mars Simulation Project
 * ReportingAuthorityType.java
 * @version 3.1.0 2017-10-23
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.mars_sim.msp.core.Msg;

public enum ReportingAuthorityType {

	CNSA				(Msg.getString("ReportingAuthorityType.CNSA")), //$NON-NLS-1$
	CSA					(Msg.getString("ReportingAuthorityType.CSA")), //$NON-NLS-1$
	ESA					(Msg.getString("ReportingAuthorityType.ESA")), //$NON-NLS-1$
	ISRO				(Msg.getString("ReportingAuthorityType.ISRO")), //$NON-NLS-1$
	JAXA				(Msg.getString("ReportingAuthorityType.JAXA")), //$NON-NLS-1$
	NASA				(Msg.getString("ReportingAuthorityType.NASA")), //$NON-NLS-1$
	RKA					(Msg.getString("ReportingAuthorityType.RKA")), //$NON-NLS-1$
	MARS_SOCIETY		(Msg.getString("ReportingAuthorityType.MarsSociety")), //$NON-NLS-1$
	SPACE_X				(Msg.getString("ReportingAuthorityType.SpaceX")) //$NON-NLS-1$
	;

	public static ReportingAuthorityType[] SPONSORS = new ReportingAuthorityType[]{
			CNSA,
			CSA,
			ESA,
			ISRO,
			JAXA,
			NASA,
			RKA,
			MARS_SOCIETY,
			SPACE_X};

	public static int numSponsors = SPONSORS.length;
	private String name;
	
	private static Set<ReportingAuthorityType> sponsorSet;

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
	
	public static ReportingAuthorityType str2enum(String name) {
		if (name != null) {
	    	for (ReportingAuthorityType ra : ReportingAuthorityType.values()) {
	    		if (name.equalsIgnoreCase(ra.name)) {
	    			return ra;
	    		}
	    	}
		}
		
		return null;
	}

	public static Set<ReportingAuthorityType> getSponsorSet() {
		if (sponsorSet == null) {
			for (ReportingAuthorityType ra : ReportingAuthorityType.values()) {
				sponsorSet.add(ra);
			}
		}
		return sponsorSet;
	}

	/**
	 * gives back a list of all valid values for the ReportingAuthorityType enum.
	 */
	public static List<ReportingAuthorityType> valuesList() {
		return Arrays.asList(ReportingAuthorityType.values());
		// Arrays.asList() returns an ArrayList which is a private static class inside Arrays. 
		// It is not an java.util.ArrayList class.
		// Could possibly reconfigure this method as follows: 
		// public ArrayList<ReportingAuthorityType> valuesList() {
		// 	return new ArrayList<ReportingAuthorityType>(Arrays.asList(ReportingAuthorityType.values())); }
	}
}

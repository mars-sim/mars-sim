/**
 * Mars Simulation Project
 * ReportingAuthorityType.java
 * @version 3.1.0 2017-10-23
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mars_sim.msp.core.Msg;

public enum ReportingAuthorityType {

	CNSA				(Msg.getString("ReportingAuthorityType.CNSA")), //$NON-NLS-1$
	CSA					(Msg.getString("ReportingAuthorityType.CSA")), //$NON-NLS-1$
	ISRO				(Msg.getString("ReportingAuthorityType.ISRO")), //$NON-NLS-1$
	JAXA				(Msg.getString("ReportingAuthorityType.JAXA")), //$NON-NLS-1$
	NASA				(Msg.getString("ReportingAuthorityType.NASA")), //$NON-NLS-1$
	RKA					(Msg.getString("ReportingAuthorityType.RKA")), //$NON-NLS-1$
	ESA					(Msg.getString("ReportingAuthorityType.ESA")), //$NON-NLS-1$
	MS					(Msg.getString("ReportingAuthorityType.MS")), //$NON-NLS-1$
	SPACEX				(Msg.getString("ReportingAuthorityType.SpaceX")), //$NON-NLS-1$
	
	CNSA_L				(Msg.getString("ReportingAuthorityType.long.CNSA")), //$NON-NLS-1$
	CSA_L				(Msg.getString("ReportingAuthorityType.long.CSA")), //$NON-NLS-1$
	ISRO_L				(Msg.getString("ReportingAuthorityType.long.ISRO")), //$NON-NLS-1$
	JAXA_L				(Msg.getString("ReportingAuthorityType.long.JAXA")), //$NON-NLS-1$
	NASA_L				(Msg.getString("ReportingAuthorityType.long.NASA")), //$NON-NLS-1$
	RKA_L				(Msg.getString("ReportingAuthorityType.long.RKA")), //$NON-NLS-1$
	ESA_L				(Msg.getString("ReportingAuthorityType.long.ESA")), //$NON-NLS-1$
	MARS_SOCIETY_L		(Msg.getString("ReportingAuthorityType.long.MS")), //$NON-NLS-1$
	SPACEX_L			(Msg.getString("ReportingAuthorityType.long.SpaceX")) //$NON-NLS-1$
	
	;

	public static ReportingAuthorityType[] SPONSORS = new ReportingAuthorityType[]{
			CNSA,
			CSA,
			ISRO,
			JAXA,
			NASA,
			RKA,
			ESA,
			MS,
			SPACEX
			};

	public static ReportingAuthorityType[] SPONSORS_LONG = new ReportingAuthorityType[]{
			CNSA_L,
			CSA_L,
			ISRO_L,
			JAXA_L,
			NASA_L,
			RKA_L,
			ESA_L,
			MARS_SOCIETY_L,
			SPACEX_L
			};
	
	public static int numSponsors = SPONSORS.length;
	
	private String name;
	
//	private static Set<ReportingAuthorityType> sponsorSet;
	
	private static List<String> sponsorList;
	
	private static List<String> longSponsorList;

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
	
	public static ReportingAuthorityType getType(String name) {
		if (name != null) {
	    	for (ReportingAuthorityType ra : ReportingAuthorityType.values()) {
	    		if (name.equalsIgnoreCase(ra.name)) {
	    			return ra;
	    		}
	    	}
		}
		
		return null;
	}

//	public static Set<ReportingAuthorityType> getSponsorSet() {
//		if (sponsorSet == null) {
//			for (ReportingAuthorityType ra : ReportingAuthorityType.values()) {
//				sponsorSet.add(ra);
//			}
//		}
//		return sponsorSet;
//	}

//	/**
//	 * Returns a list of ReportingAuthorityType enum.
//	 */
//	public static List<ReportingAuthorityType> valuesList() {
//		return Arrays.asList(ReportingAuthorityType.values());
//		// Arrays.asList() returns an ArrayList which is a private static class inside Arrays. 
//		// It is not an java.util.ArrayList class.
//		// Could possibly reconfigure this method as follows: 
//		// public ArrayList<ReportingAuthorityType> valuesList() {
//		// 	return new ArrayList<ReportingAuthorityType>(Arrays.asList(ReportingAuthorityType.values())); }
//	}
	
	public static List<String> getSponsorList() {
		if (sponsorList == null  || sponsorList.isEmpty()) {
			sponsorList = new ArrayList<>();
			for (ReportingAuthorityType ra : SPONSORS) {
				sponsorList.add(ra.getName());
			}
		}
		return sponsorList;
	}

	public static List<String> getLongSponsorList() {
		if (longSponsorList == null || longSponsorList.isEmpty()) {
			longSponsorList = new ArrayList<>();
			for (ReportingAuthorityType ra : SPONSORS_LONG) {
				longSponsorList.add(ra.getName()); 
			}
		}
		return longSponsorList;
	}
	
	public static int getSponsorID(String longSponsor) {
		return getType(longSponsor).ordinal() - 9;
	}
	
	public static String convertSponsorNameShort2Long(String name) {
//		if (longSponsorList == null || longSponsorList.isEmpty()) {
//			longSponsorList = new ArrayList<>();
			for (ReportingAuthorityType ra : SPONSORS_LONG) {
				if (StringUtils.containsIgnoreCase(ra.getName(), name)) {
					return ra.getName();
				}
			}
//		}
		return null;
	}
	
//	public static List<String> StringList() {
//		String sponsor = (String) newValue;
//		// System.out.println("sponsor is " + sponsor);
//		int code = -1;
//		List<String> list = new ArrayList<>();
//
//		if (sponsor.contains("CNSA"))
//			list.add("China");
//		else if (sponsor.contains("CSA"))
//			list.add("Canada");
//		else if (sponsor.contains("ISRO"))
//			list.add("India"); // 2
//		else if (sponsor.contains("JAXA"))
//			list.add("Japan"); // 3
//		else if (sponsor.contains("MS"))
//			code = 0;
//		else if (sponsor.contains("NASA"))
//			list.add("US"); // 4
//		else if (sponsor.contains("RKA"))
//			list.add("Russia"); // 5
//		else if (sponsor.contains("ESA"))
//			code = 1;
//
//		if (code == 0) {
//			list = personConfig.createCountryList();
//		} else if (code == 1) {
//			list = personConfig.createESACountryList();
//		}
//
//		Collections.sort(list);
//		
//		return list;
//	}
}

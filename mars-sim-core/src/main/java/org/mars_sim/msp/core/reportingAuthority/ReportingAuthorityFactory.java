/**
 * Mars Simulation Project
 * MissionAgendaFactory.java
 * @version 3.2.0 2021-06-21
 * @author Barry Evans
 */
package org.mars_sim.msp.core.reportingAuthority;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;

public final class ReportingAuthorityFactory {
	
	private static Map<ReportingAuthorityType,ReportingAuthority> controls
			= new EnumMap<>(ReportingAuthorityType.class);
	
	private ReportingAuthorityFactory() {
		
	}
	
	/**
	 * Find the default country for an Authority
	 * @param sponsor
	 * @return
	 */
	public static String getDefaultCountry(ReportingAuthorityType sponsor) {
		// TODO get off Reporting Authority
		switch (sponsor) {
		case CNSA:
			return "China";
		case CSA:
			return "Canada";
		case ISRO:
			return "India";
		case JAXA:
			return "Japan";
		case NASA:
			return "USA";
		case RKA:
			return "Russia";
		case ESA:
			List<String> ESACountries = SimulationConfig.instance().getPersonConfig().createESACountryList();
			return ESACountries.get(RandomUtil.getRandomInt(0, ESACountries.size() - 1));
		case SPACEX:
		case MS:
			List<String> allCountries = SimulationConfig.instance().getPersonConfig().createAllCountryList();
			return allCountries.get(RandomUtil.getRandomInt(0, allCountries.size() - 1));	
		default:
			return "";
		}
	}

	/**
	 * Lookup the appropriate ReportingAuthority for a AuthorityType.
	 * @param authority
	 * @param unit
	 * @return
	 */
	public static ReportingAuthority getAuthority(ReportingAuthorityType authority) {
		if (controls.isEmpty()) {
			controls = GovernanceConfig.loadAuthorites();
		}
		ReportingAuthority ra = controls.get(authority);
		if (ra == null) {
			throw new IllegalArgumentException("Have no Reporting Authority for " + authority);
		}
		
		return ra;
	}
	
	/**
	 * Scan the known Settlement and get the load Reporting Authorities. This
	 * makes sure new units will get the same shared Reporting Authority
	 * @param mgr
	 */
	public static void discoverReportingAuthorities(UnitManager mgr) {
		for (Settlement s : mgr.getSettlements()) {
			ReportingAuthority ra = s.getReportingAuthority();
			controls.put(ra.getOrg(), ra);
		}
	}
}

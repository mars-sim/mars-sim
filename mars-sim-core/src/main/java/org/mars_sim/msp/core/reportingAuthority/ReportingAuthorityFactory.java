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
		ReportingAuthority ra = getAuthority(sponsor);
		List<String> countries = ra.getCountries();
		if (countries.size() == 1) {
			return countries.get(0);
		}
		else {
			return countries.get(RandomUtil.getRandomInt(0, countries.size() - 1));	
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

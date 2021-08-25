/**
 * Mars Simulation Project
 * MissionAgendaFactory.java
 * @version 3.2.0 2021-06-21
 * @author Barry Evans
 */
package org.mars_sim.msp.core.reportingAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Factory method for creating/managing Reporting Authorities.
 * This is loaded via the GovernanceConfig for new simulations
 * or derived from the Settlements in a loaded simulation. 
 */
public final class ReportingAuthorityFactory {
	
	/**
	 * The code for the Mars Society Reporting Authoritu which is considered the default.
	 * This society should always be created.
	 */
	public static final String MS_CODE = "MS";
	
	private static Map<String,ReportingAuthority> controls
			= new HashMap<>();
	
	private ReportingAuthorityFactory() {
		
	}

	/**
	 * Lookup the appropriate ReportingAuthority for a AuthorityType.
	 * @param authority
	 * @param unit
	 * @return
	 */
	public static ReportingAuthority getAuthority(String code) {
		if (controls.isEmpty()) {
			controls = GovernanceConfig.loadAuthorites();
		}
		ReportingAuthority ra = controls.get(code);
		if (ra == null) {
			throw new IllegalArgumentException("Have no Reporting Authority for " + code);
		}
		
		return ra;
	}
	
	/**
	 * Scan the known Settlement and get the load Reporting Authorities. This
	 * makes sure new units will get the same shared Reporting Authority
	 * What about pending arrivals of new Settlement with new RA ?
	 * @param mgr
	 */
	public static void discoverReportingAuthorities(UnitManager mgr) {
		// Load the defaults 
		if (controls.isEmpty()) {
			controls = GovernanceConfig.loadAuthorites();
		}
		
		// Then overwrite the loaded with those that are active in the simulation
		for (Settlement s : mgr.getSettlements()) {
			ReportingAuthority ra = s.getSponsor();
			controls.put(ra.getName(), ra);
		}
	}

	/**
	 * Get a list of the support Reporting Authority codes loaded.
	 * @return
	 */
	public static Collection<String> getSupportedCodes() {
		return Collections.unmodifiableCollection(controls.keySet());
	}
}

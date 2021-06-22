/**
 * Mars Simulation Project
 * MissionAgendaFactory.java
 * @version 3.2.0 2021-06-21
 * @author Barry Evans
 */
package org.mars_sim.msp.core.reportingAuthority;

import java.util.List;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.tool.RandomUtil;

public final class ReportingAuthorityFactory {
	private ReportingAuthorityFactory() {
		
	}
	

	/**
	 * Find the default country for an Authority
	 * @param sponsor
	 * @return
	 */
	public static String getDefaultCountry(ReportingAuthorityType sponsor) {

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
	 * @param sponsor
	 * @param unit
	 * @return
	 */
	public static ReportingAuthority getAuthority(String sponsor, Unit unit) { 
		ReportingAuthorityType authority = ReportingAuthorityType.valueOf(sponsor);
		
		ReportingAuthority ra = null;;
		switch (authority) {
		case CNSA:
			ra = CNSAMissionControl.createMissionControl(unit); // ProspectingMineral
			break;
			
		case CSA:
			ra = CSAMissionControl.createMissionControl(unit); // AdvancingSpaceKnowledge
			break;
			
		case ESA:
			ra = ESAMissionControl.createMissionControl(unit); // DevelopingSpaceActivity;
			break;
			
		case ISRO:
			ra = ISROMissionControl.createMissionControl(unit); // DevelopingAdvancedTechnology
			break;
			
		case JAXA:
			ra = JAXAMissionControl.createMissionControl(unit); // ResearchingSpaceApplication
			break;
			
		case NASA:
			ra = NASAMissionControl.createMissionControl(unit); // FindingLife
			break;
			
		case RKA:
			ra = RKAMissionControl.createMissionControl(unit); // ResearchingHealthHazard
			break;
			
		case MS:
			ra = MarsSocietyMissionControl.createMissionControl(unit); // SettlingMars
			break;
			
		case SPACEX:
			ra = SpaceXMissionControl.createMissionControl(unit); // BuildingSelfSustainingColonies
			break;
			
		default:
		}
		
		return ra;
	}
}

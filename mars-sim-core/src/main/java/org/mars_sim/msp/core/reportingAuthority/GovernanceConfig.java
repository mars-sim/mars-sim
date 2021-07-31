/**
 * Mars Simulation Project
 * GovernanceConfig.java
 * @version 3.2.0 2021-07-30
 * @author Barry Evans
 */
package org.mars_sim.msp.core.reportingAuthority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.ai.mission.MissionType;

/**
 * Provides configuration information about the Mars governance.
 */
public class GovernanceConfig {

	private static final String COUNTRY = "country";
	// Element or attribute names
	private static final String NAME = "name";
	
	/**
	 * Constructor
	 * 
	 * @param crewDoc the crew config DOM document.
	 */
	private GovernanceConfig() {

	}

	/**
	 * Load the Reporting authorities from an external XML
	 * @return
	 */
	static Map<ReportingAuthorityType,ReportingAuthority> loadAuthorites() {
		Document doc = SimulationConfig.instance().parseXMLFileAsJDOMDocument("governance", false);

		Map<String,MissionAgenda> agendas = new HashMap<>();
		
		// Load the Agendas into a temp Map
		Element agendasNode = doc.getRootElement().getChild("agendas");
		List<Element> agendaNodes = agendasNode.getChildren("agenda");
		for (Element agendaNode : agendaNodes) {
			String name = agendaNode.getAttributeValue(NAME);
			String objective = agendaNode.getAttributeValue("objective");
			String findings = agendaNode.getAttributeValue("findings");
			String samples = agendaNode.getAttributeValue("samples");

			// Load sub-agendas
			List<MissionSubAgenda> subs = new ArrayList<>();
			List<Element> subNodes = agendaNode.getChildren("sub-agenda");
			for (Element subNode : subNodes) {
				String description = subNode.getAttributeValue("description");
				
				// Get modifiers
				Map<MissionType, Integer> modifiers = new HashMap<>();
				List<Element> modNodes = agendaNode.getChildren("modifier");
				for (Element modNode : modNodes) {
					MissionType mission = MissionType.valueOf(modNode.getAttributeValue("mission"));
					int value = Integer.parseInt(modNode.getAttributeValue("value"));
					modifiers.put(mission, value);
				}
			
				subs.add(new MissionSubAgenda(description, modifiers));
			}	
				
			// Add the agenda
			agendas.put(name, new MissionAgenda(name, objective, subs, findings, samples));
		}
	
		// Load the Reporting authorities
		Map<ReportingAuthorityType, ReportingAuthority> authorites = new HashMap<>();
		Element authoritiesNode = doc.getRootElement().getChild("authorities");
		List<Element> authorityNodes = authoritiesNode.getChildren("authority");
		for (Element authorityNode : authorityNodes) {
			ReportingAuthorityType code = ReportingAuthorityType.valueOf(authorityNode.getAttributeValue("code"));
			String name = authorityNode.getAttributeValue(NAME);
			String agendaName = authorityNode.getAttributeValue("agenda");
			
			MissionAgenda agenda = agendas.get(agendaName);
			if (agenda == null) {
				 throw new IllegalArgumentException("Agenda called '" + agendaName + "' does not exist for RA " + code);
			}
			 
			// Get Countries
			List<String> countries = new ArrayList<>();
			List<Element> countryNodes = authorityNode.getChildren(COUNTRY);
			for (Element countryNode : countryNodes) {
				countries.add(countryNode.getAttributeValue(NAME));			 
			}
			 
			// Add to lookup
			authorites.put(code, new ReportingAuthority(code, name, agenda, countries ));
		}
		
		return authorites;
	}
}

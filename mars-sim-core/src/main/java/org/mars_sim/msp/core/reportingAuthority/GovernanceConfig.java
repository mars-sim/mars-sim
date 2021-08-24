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
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.ai.mission.MissionType;

/**
 * Provides configuration information about the Mars governance.
 */
public class GovernanceConfig {

	private static final String AUTHORITY_EL = "authority";
	private static final String AUTHORITIES_EL = "authorities";
	private static final String CODE_ATTR = "code";
	private static final String MISSION_ATTR = "mission";
	private static final String MODIFIER_EL = "modifier";
	private static final String VALUE_ATTR = "value";
	private static final String DESCRIPTION_ATTR = "description";
	private static final String SUB_AGENDA_EL = "sub-agenda";
	private static final String SAMPLES_ATTR = "samples";
	private static final String FINDINGS_ATTR = "findings";
	private static final String OBJECTIVE_ATTR = "objective";
	private static final String AGENDA_EL = "agenda";
	private static final String AGENDAS_EL = "agendas";
	private static final String COUNTRY_EL = "country";
	private static final String NAME_ATTR = "name";
	private static final String SETTLEMENTNAME_EL = "settlement-name";
	private static final String ROVERNAME_EL = "rover-name";
	
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
	static Map<String,ReportingAuthority> loadAuthorites() {
		Document doc = SimulationConfig.instance().parseXMLFileAsJDOMDocument("governance", false);

		Map<String,MissionAgenda> agendas = new HashMap<>();
		
		// Load the Agendas into a temp Map
		Element agendasNode = doc.getRootElement().getChild(AGENDAS_EL);
		List<Element> agendaNodes = agendasNode.getChildren(AGENDA_EL);
		for (Element agendaNode : agendaNodes) {
			String name = agendaNode.getAttributeValue(NAME_ATTR);
			String objective = agendaNode.getAttributeValue(OBJECTIVE_ATTR);
			String findings = agendaNode.getAttributeValue(FINDINGS_ATTR);
			String samples = agendaNode.getAttributeValue(SAMPLES_ATTR);

			// Load sub-agendas
			List<MissionSubAgenda> subs = new ArrayList<>();
			List<Element> subNodes = agendaNode.getChildren(SUB_AGENDA_EL);
			for (Element subNode : subNodes) {
				String description = subNode.getAttributeValue(DESCRIPTION_ATTR);
				
				// Get modifiers
				Map<MissionType, Integer> modifiers = new HashMap<>();
				List<Element> modNodes = agendaNode.getChildren(MODIFIER_EL);
				for (Element modNode : modNodes) {
					MissionType mission = MissionType.valueOf(modNode.getAttributeValue(MISSION_ATTR));
					int value = Integer.parseInt(modNode.getAttributeValue(VALUE_ATTR));
					modifiers.put(mission, value);
				}
			
				subs.add(new MissionSubAgenda(description, modifiers));
			}	
				
			// Add the agenda
			agendas.put(name, new MissionAgenda(name, objective, subs, findings, samples));
		}
	
		// Load the Reporting authorities
		Map<String, ReportingAuthority> authorites = new HashMap<>();
		Element authoritiesNode = doc.getRootElement().getChild(AUTHORITIES_EL);
		List<Element> authorityNodes = authoritiesNode.getChildren(AUTHORITY_EL);
		for (Element authorityNode : authorityNodes) {
			String code = authorityNode.getAttributeValue(CODE_ATTR);
			String name = authorityNode.getAttributeValue(NAME_ATTR);
			String agendaName = authorityNode.getAttributeValue(AGENDA_EL);			
			MissionAgenda agenda = agendas.get(agendaName);
			if (agenda == null) {
				 throw new IllegalArgumentException("Agenda called '" + agendaName + "' does not exist for RA " + code);
			}
			 
			// Get Countries
			List<String> countries = authorityNode.getChildren(COUNTRY_EL).stream()
									.map(a -> a.getAttributeValue(NAME_ATTR))
									.collect(Collectors.toList());
			 
			// Get Settlement names
			List<String> settlementNames = authorityNode.getChildren(SETTLEMENTNAME_EL).stream()
					.map(a -> a.getAttributeValue(NAME_ATTR))
					.collect(Collectors.toList());

			// Get Rover names
			List<String> roverNames = authorityNode.getChildren(ROVERNAME_EL).stream()
					.map(a -> a.getAttributeValue(NAME_ATTR))
					.collect(Collectors.toList());
			
			// Add to lookup
			authorites.put(code, new ReportingAuthority(code, name, agenda,
														countries, settlementNames,
														roverNames));
		}
		
		return authorites;
	}
}

/*
 * Mars Simulation Project
 * ReportingAuthorityFactory.java
 * @date 2022-07-15
 * @author Barry Evans
 */
package org.mars_sim.msp.core.reportingAuthority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.configuration.UserConfigurableConfig;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Factory method for creating/managing Reporting Authorities.
 * This is loaded via the GovernanceConfig for new simulations
 * or derived from the Settlements in a loaded simulation. 
 */
public final class ReportingAuthorityFactory extends UserConfigurableConfig<ReportingAuthority> {
	
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
	
	private Map<String,MissionAgenda> agendas;

	public ReportingAuthorityFactory() {
		super("authority");
		
		// Load the defaults
		loadGovernanceDetails();
		
		// Load user defined authorities
		loadUserDefined();
	}

	/**
	 * Loads the Reporting authorities from an external XML.
	 * 
	 * @param config 
	 * @return
	 */
	private void loadGovernanceDetails() {
		Document doc = SimulationConfig.instance().parseXMLFileAsJDOMDocument("governance", true);

		agendas = new HashMap<>();
		
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
				List<Element> modNodes = subNode.getChildren(MODIFIER_EL);
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
		Element authoritiesNode = doc.getRootElement().getChild(AUTHORITIES_EL);
		List<Element> authorityNodes = authoritiesNode.getChildren(AUTHORITY_EL);
		for (Element authorityNode : authorityNodes) {
			addItem(parseXMLAuthority(authorityNode, true));
		}
	}
	
	/**
	 * Parses an Authority XML Element and create a Reporting Authority object.
	 * 
	 * @param authorityNode
	 * @param predefined Is this a repdefined RA
	 * @return
	 */
	private ReportingAuthority parseXMLAuthority(Element authorityNode, boolean predefined) {
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
		
		return new ReportingAuthority(code, name, predefined, agenda,
									  countries, settlementNames,
									  roverNames);
	}
	
	/**
	 * Scans the known Settlement and get the load Reporting Authorities. This
	 * makes sure new units will get the same shared Reporting Authority.
	 * What about pending arrivals of new Settlement with new RA ?
	 * 
	 * @param mgr
	 */
	public void discoverReportingAuthorities(UnitManager mgr) {
		// Then overwrite the loaded with those that are active in the simulation
		for (Settlement s : mgr.getSettlements()) {
			ReportingAuthority ra = s.getSponsor();
			addItem(ra);
		}
	}

	/**
	 * Converts a Reporting Authority to an XML representation.
	 */
	@Override
	protected Document createItemDoc(ReportingAuthority item) {
		Element authorityNode = new Element(AUTHORITY_EL);
		authorityNode.setAttribute(CODE_ATTR, item.getName());
		authorityNode.setAttribute(NAME_ATTR, item.getDescription());
		authorityNode.setAttribute(AGENDA_EL, item.getMissionAgenda().getName());			
		 
		// Get Countries
		addList(authorityNode, COUNTRY_EL, item.getCountries());
		addList(authorityNode, SETTLEMENTNAME_EL, item.getSettlementNames());
		addList(authorityNode, ROVERNAME_EL, item.getVehicleNames());
		
		return new Document(authorityNode);
	}

	private void addList(Element authorityNode, String elName, List<String> items) {
		for (String s : items) {
			Element el = new Element(elName);
			el.setAttribute(NAME_ATTR, s);
			authorityNode.addContent(el);
		}
	}

	/**
	 * Parses a user created XML.
	 */
	@Override
	protected ReportingAuthority parseItemXML(Document doc, boolean predefined) {
		// User configured XML just contains the Authority node.
		return parseXMLAuthority(doc.getRootElement(), false);
	}

	/**
	 * Gets the names of the known Mission Agendas.
	 * 
	 * @return
	 */
	public List<String> getAgendaNames() {
		List<String> result = new ArrayList<>(agendas.keySet());
		Collections.sort(result);
		return result;
	}

	/**
	 * Finds a defined Mission Agenda by name.
	 * 
	 * @param name
	 * @return
	 */
	public MissionAgenda getAgenda(String name) {
		return agendas.get(name);
	}
}

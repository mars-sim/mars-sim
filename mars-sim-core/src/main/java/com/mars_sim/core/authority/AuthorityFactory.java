/*
 * Mars Simulation Project
 * AuthorityFactory.java
 * @date 2023-05-31
 * @author Barry Evans
 */
package com.mars_sim.core.authority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.UnitManager;
import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.configuration.UserConfigurableConfig;
import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterManager;
import com.mars_sim.core.person.ai.mission.MissionWeightParameters;
import com.mars_sim.core.person.ai.task.util.TaskParameters;
import com.mars_sim.core.science.ScienceParameters;
import com.mars_sim.core.structure.Settlement;

/**
 * Factory method for creating/managing Reporting Authorities.
 * This is loaded for new simulations or derived from settlements 
 * in a loaded simulation. 
 */
public final class AuthorityFactory extends UserConfigurableConfig<Authority> {
	
	private static final String FILE_NAME = "authority";
	private static final String AUTHORITY_EL = "authority";
	private static final String AUTHORITIES_EL = "authorities";
	private static final String CODE_ATTR = "code";
	private static final String CORPORATION_ATTR = "corporation";
	private static final String MODIFIER_ATTR = "modifier";
	private static final String DESCRIPTION_ATTR = "description";
	private static final String CAPABILITY_EL = "capability";
	private static final String DATA_ATTR = "data";
	private static final String FINDINGS_ATTR = "findings";
	private static final String OBJECTIVE_ATTR = "objective";
	private static final String AGENDA_EL = "agenda";
	private static final String AGENDAS_EL = "agendas";
	private static final String COUNTRY_EL = "country";
	private static final String NAME_ATTR = "name";
	private static final String SETTLEMENTNAME_EL = "settlement-name";
	private static final String ROVERNAME_EL = "rover-name";
	private static final String GENDER_ATTR = "gender-ratio";
	private static final String PERFERENCE_EL = "preference";
	private static final String TYPE_ATTR = "type";
	private static final String TRUE = "true";
	
	private Map<String, MissionAgenda> agendas;

	/**
	 * Constructor.
	 * 
	 * @param governanceDoc
	 */
	public AuthorityFactory(Document governanceDoc) {
		super(FILE_NAME);
		
		// Load the defaults
		loadGovernanceDetails(governanceDoc);
		
		// Load user defined authorities
		loadUserDefined();
	}

	/**
	 * Loads the Reporting authorities from an external XML.
	 */
	private synchronized void loadGovernanceDetails(Document doc) {
		if (agendas != null) {
			// just in case if another thread is being created
			return;
		}
			
		// Build the global list in a temp to avoid access before it is built
		Map<String, MissionAgenda> newAgendas = new HashMap<>();
		
		// Load the Agendas into a temp Map
		Element agendasNode = doc.getRootElement().getChild(AGENDAS_EL);
		List<Element> agendaNodes = agendasNode.getChildren(AGENDA_EL);
		for (Element agendaNode : agendaNodes) {
			String name = agendaNode.getAttributeValue(NAME_ATTR);
			String objective = agendaNode.getAttributeValue(OBJECTIVE_ATTR);
			String findings = agendaNode.getAttributeValue(FINDINGS_ATTR);
			String data = agendaNode.getAttributeValue(DATA_ATTR);

			// Load sub-agendas
			List<MissionCapability> subs = new ArrayList<>();
			List<Element> subNodes = agendaNode.getChildren(CAPABILITY_EL);
			for (Element subNode : subNodes) {
				String description = subNode.getAttributeValue(DESCRIPTION_ATTR);
	
				// Load the preferences
				var preferences = new ParameterManager();
				for (Element preNode : subNode.getChildren(PERFERENCE_EL)) {

					// Backward compatible with the old naming scheme
					String pTypeValue = preNode.getAttributeValue(TYPE_ATTR);
					ParameterCategory pType = switch(pTypeValue) {
						case "MISSION", "MISSION_WEIGHT" -> MissionWeightParameters.INSTANCE;
						case "TASK", "TASK_WEIGHT" -> TaskParameters.INSTANCE;
						case "SCIENCE" -> ScienceParameters.INSTANCE;
						default -> throw new IllegalArgumentException("Authority " + name
								+ " has an unsupport preference type " + pTypeValue);
					};
					
					Serializable value = Double.parseDouble(preNode.getAttributeValue(MODIFIER_ATTR));
					String pName = preNode.getAttributeValue(NAME_ATTR).toUpperCase();
					var key = pType.getKey(pName);
					preferences.putValue(key, value);
				}

				subs.add(new MissionCapability(description, preferences));
			}	
				
			// Add the agenda
			newAgendas.put(name, new MissionAgenda(name, objective, subs, findings, data));
		}
	
		// Load the Reporting authorities
		Element authoritiesNode = doc.getRootElement().getChild(AUTHORITIES_EL);
		List<Element> authorityNodes = authoritiesNode.getChildren(AUTHORITY_EL);
		for (Element authorityNode : authorityNodes) {
			addItem(parseXMLAuthority(newAgendas, authorityNode, true));
		}
		
		// Assign the agendas
		agendas = Collections.unmodifiableMap(newAgendas);
	}
	
	/**
	 * Parses an Authority XML Element and create a Reporting Authority object.
	 * 
	 * @param agendas
	 * @param authorityNode
	 * @param predefined Is this a redefined RA
	 * @return
	 */
	private Authority parseXMLAuthority(Map<String, MissionAgenda> agendas, Element authorityNode, boolean predefined) {
		String acronym = authorityNode.getAttributeValue(CODE_ATTR);
		String fullName = authorityNode.getAttributeValue(NAME_ATTR);
		String isCorporationString = authorityNode.getAttributeValue(CORPORATION_ATTR);
		String agendaName = authorityNode.getAttributeValue(AGENDA_EL);			
		MissionAgenda agenda = agendas.get(agendaName);
		if (agenda == null) {
			 throw new IllegalArgumentException("Agenda called '" + agendaName + "' does not exist for RA " + acronym);
		}
		double maleRatio = ConfigHelper.getOptionalAttributeDouble(authorityNode, GENDER_ATTR, 0.5D);

		
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
		
		// Check if it's a corporation (false if it's a space agency)
		boolean isCorporation = false;
		
		if (isCorporationString.equalsIgnoreCase(TRUE))
			isCorporation = true;
		
		return new Authority(acronym, fullName, isCorporation, predefined, maleRatio, agenda,
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
			addItem(s.getReportingAuthority());
		}
	}

	/**
	 * Converts a Reporting Authority to an XML representation.
	 */
	@Override
	protected Document createItemDoc(Authority item) {
		Element authorityNode = new Element(AUTHORITY_EL);
		authorityNode.setAttribute(CODE_ATTR, item.getName());
		authorityNode.setAttribute(NAME_ATTR, item.getDescription());
		authorityNode.setAttribute(AGENDA_EL, item.getMissionAgenda().getName());	
		authorityNode.setAttribute(GENDER_ATTR, Double.toString(item.getGenderRatio()));
		
		 
		// Get Countries
		addList(authorityNode, COUNTRY_EL, item.getCountries());
		addList(authorityNode, SETTLEMENTNAME_EL, item.getSettlementNames());
		addList(authorityNode, ROVERNAME_EL, item.getVehicleNames());
		
		return new Document(authorityNode);
	}

	/**
	 * Adds a list of names (countries, settlements, vehicles) into an authority node.
	 * 
	 * @param authorityNode
	 * @param elName
	 * @param items
	 */
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
	protected Authority parseItemXML(Document doc, boolean predefined) {
		// User configured XML just contains the Authority node.
		return parseXMLAuthority(agendas, doc.getRootElement(), false);
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

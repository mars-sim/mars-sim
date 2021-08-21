/*
 * Mars Simulation Project
 * ScenarioConfig.java
 * @date 2021-08-20
 * @author Barry Evans
 */
package org.mars_sim.msp.core.configuration;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.InitialSettlement;

/**
 * Loads and maintains a repository Scenario instances from XML files.
 */
public class ScenarioConfig extends UserConfigurableConfig<Scenario> {
	
	private static final String PREFIX = "scenario_";
	private static final String INITIAL_SETTLEMENT_LIST = "initial-settlement-list";
	private static final String SETTLEMENT_EL = "settlement";
	private static final String CREW_ATTR = "crew";
	private static final String NAME_ATTR = "name";
	private static final String DESCRIPTION_ATTR = "description";
	private static final String TEMPLATE_ATTR = "template";
	private static final String LOCATION_EL = "location";
	private static final String LONGITUDE_ATTR = "longitude";
	private static final String LATITUDE_ATTR = "latitude";
	private static final String PERSONS_ATTR = "persons";
	private static final String ROBOTS_ATTR = "robots";
	private static final String SPONSOR_ATTR = "sponsor";
	private static final String SCENARIO_CONFIG = "scenario-configuration";

	// Default scenario
	public static final String[] PREDEFINED_SCENARIOS = {"Default"};

	
	public ScenarioConfig() {
		super(PREFIX, PREDEFINED_SCENARIOS);
	}

	@Override
	protected Document createItemDoc(Scenario item) {
		Element root = new Element(SCENARIO_CONFIG);
		Document doc = new Document(root);
		
		saveOptionalAttribute(root, NAME_ATTR, item.getName());
		saveOptionalAttribute(root, DESCRIPTION_ATTR, item.getDescription());

		// Add the initial settlements
		Element initialSettlementList = new Element(INITIAL_SETTLEMENT_LIST);
		for (InitialSettlement settlement : item.getSettlements()) {
			Element settlementElement = new Element(SETTLEMENT_EL);
			saveOptionalAttribute(settlementElement, NAME_ATTR, settlement.getName());
			saveOptionalAttribute(settlementElement, TEMPLATE_ATTR, settlement.getSettlementTemplate());
			saveOptionalAttribute(settlementElement, PERSONS_ATTR, Integer.toString(settlement.getPopulationNumber()));
			saveOptionalAttribute(settlementElement, ROBOTS_ATTR, Integer.toString(settlement.getNumOfRobots()));
			saveOptionalAttribute(settlementElement, SPONSOR_ATTR, settlement.getSponsor());
			saveOptionalAttribute(settlementElement, CREW_ATTR, settlement.getCrew());

			Element locationElement = new Element(LOCATION_EL);
			saveOptionalAttribute(locationElement, LONGITUDE_ATTR, settlement.getLocation().getFormattedLongitudeString());
			saveOptionalAttribute(locationElement, LATITUDE_ATTR, settlement.getLocation().getFormattedLatitudeString());
			settlementElement.addContent(locationElement);
			
			initialSettlementList.addContent(settlementElement);
		}
		root.addContent(initialSettlementList);
		return doc;
	}

	@Override
	protected Scenario parseItemXML(Document doc, boolean predefined) {
		Element root = doc.getRootElement();
		String name = root.getAttributeValue(NAME_ATTR);
		String description = root.getAttributeValue(DESCRIPTION_ATTR);
		List<InitialSettlement> is = loadInitialSettlements(root);
		
		return new Scenario(name, description, is, predefined);
	}
	
	/**
	 * Load initial settlements.
	 * 
	 * @param settlementDoc DOM document with settlement configuration.
	 * @throws Exception if XML error.
	 */
	private List<InitialSettlement> loadInitialSettlements(Element scenarioElement) {
		Element initialSettlementList = scenarioElement.getChild(INITIAL_SETTLEMENT_LIST);
		List<Element> settlementNodes = initialSettlementList.getChildren(SETTLEMENT_EL);
		List<InitialSettlement> initialSettlements = new ArrayList<>();
		
		for (Element settlementElement : settlementNodes) {

			String settlementName = settlementElement.getAttributeValue(NAME_ATTR);
			String template = settlementElement.getAttributeValue(TEMPLATE_ATTR);

			Coordinates location = null;
			List<Element> locationNodes = settlementElement.getChildren(LOCATION_EL);
			if (locationNodes.size() > 0) {
				Element locationElement = locationNodes.get(0);

				String longitudeString = locationElement.getAttributeValue(LONGITUDE_ATTR);
				String latitudeString = locationElement.getAttributeValue(LATITUDE_ATTR);

				// take care to internationalize the coordinates
				longitudeString = longitudeString.replace("E", Msg.getString("direction.eastShort")); //$NON-NLS-1$ //$NON-NLS-2$
				longitudeString = longitudeString.replace("W", Msg.getString("direction.westShort")); //$NON-NLS-1$ //$NON-NLS-2$

				// take care to internationalize the coordinates
				latitudeString = latitudeString.replace("N", Msg.getString("direction.northShort")); //$NON-NLS-1$ //$NON-NLS-2$
				latitudeString = latitudeString.replace("S", Msg.getString("direction.southShort")); //$NON-NLS-1$ //$NON-NLS-2$

				location = new Coordinates(latitudeString, longitudeString);
			}

			String numberStr = settlementElement.getAttributeValue(PERSONS_ATTR);
			int popNumber = Integer.parseInt(numberStr);
			if (popNumber < 0) {
				throw new IllegalStateException("populationNumber cannot be less than zero: " + popNumber);
			}

			String numOfRobotsStr = settlementElement.getAttributeValue(ROBOTS_ATTR);
			int numOfRobots = Integer.parseInt(numOfRobotsStr);
			if (numOfRobots < 0) {
				throw new IllegalStateException("The number of robots cannot be less than zero: " + numOfRobots);
			}

			String sponsor = settlementElement.getAttributeValue(SPONSOR_ATTR);
			String crew = settlementElement.getAttributeValue(CREW_ATTR);
			
			initialSettlements .add(new InitialSettlement(settlementName, sponsor, template, popNumber, numOfRobots,
										location, crew));
		}
		
		return initialSettlements;
	}
}

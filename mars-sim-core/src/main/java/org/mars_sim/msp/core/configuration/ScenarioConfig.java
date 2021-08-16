package org.mars_sim.msp.core.configuration;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.InitialSettlement;

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
	private static final String POPULATION_EL = "population";
	private static final String NUMBER_ATTR = "number";
	private static final String ROBOTS_EL = "robots";
	private static final String SPONSOR_EL = "sponsor";
	
	// Default scenario
	public static final String DEFAULT = "Default";
	
	public ScenarioConfig() {
		super(PREFIX);
		
		// One scenario is bundled
		loadItem(getItemFilename(DEFAULT), true);
	}

	@Override
	protected Document createItemDoc(Scenario item) {
		// TODO Auto-generated method stub
		return null;
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

			Element populationElement = settlementElement.getChild(POPULATION_EL);
			String numberStr = populationElement.getAttributeValue(NUMBER_ATTR);
			int popNumber = Integer.parseInt(numberStr);
			if (popNumber < 0) {
				throw new IllegalStateException("populationNumber cannot be less than zero: " + popNumber);
			}

			Element numOfRobotsElement = settlementElement.getChild(ROBOTS_EL);
			String numOfRobotsStr = numOfRobotsElement.getAttributeValue(NUMBER_ATTR);
			int numOfRobots = Integer.parseInt(numOfRobotsStr);
			if (numOfRobots < 0) {
				throw new IllegalStateException("The number of robots cannot be less than zero: " + numOfRobots);
			}

			Element sponsorElement = settlementElement.getChild(SPONSOR_EL);
			String sponsor = sponsorElement.getAttributeValue(NAME_ATTR);
			String crew = settlementElement.getAttributeValue(CREW_ATTR);
			
			initialSettlements .add(new InitialSettlement(settlementName, sponsor, template, popNumber, numOfRobots,
										location, crew));
		}
		
		return initialSettlements;
	}
}

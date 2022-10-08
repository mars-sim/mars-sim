/*
 * Mars Simulation Project
 * SettlementConfig.java
 * @date 2021-11-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.BoundedObject;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.configuration.ConfigHelper;
import org.mars_sim.msp.core.configuration.UserConfigurableConfig;
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyMissionTemplate;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.PartPackageConfig;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.BuildingTemplate.BuildingConnectionTemplate;

/**
 * Provides configuration information about settlements templates. Uses a DOM document to
 * get the information.
 */
public class SettlementConfig extends UserConfigurableConfig<SettlementTemplate> {

	private static final Logger logger = Logger.getLogger(SettlementConfig.class.getName());

	// Element names
	private static final String ROVER_LIFE_SUPPORT_RANGE_ERROR_MARGIN = "rover-life-support-range-error-margin";
	private static final String ROVER_FUEL_RANGE_ERROR_MARGIN = "rover-fuel-range-error-margin";
	private static final String MISSION_CONTROL = "mission-control";
	private static final String LIFE_SUPPORT_REQUIREMENTS = "life-support-requirements";
	private static final String TOTAL_PRESSURE = "total-pressure";
	private static final String PARTIAL_PRESSURE_OF_O2 = "partial-pressure-of-oxygen"; 
	private static final String PARTIAL_PRESSURE_OF_N2 = "partial-pressure-of-nitrogen";
	private static final String PARTIAL_PRESSURE_OF_CO2 = "partial-pressure-of-carbon-dioxide"; 
	private static final String TEMPERATURE = "temperature";
	private static final String RELATIVE_HUMIDITY = "relative-humidity"; 
	private static final String VENTILATION = "ventilation";
	private static final String LOW = "low";
	private static final String HIGH = "high";
	private static final String SETTLEMENT_TEMPLATE_LIST = "settlement-template-list";
	private static final String TEMPLATE = "template";
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String DEFAULT_POPULATION = "default-population";
	private static final String DEFAULT_NUM_ROBOTS = "number-of-robots";
	private static final String BUILDING = "building";
	private static final String ID = "id";
	private static final String TYPE = "type";
	private static final String CONNECTION_LIST = "connection-list";
	private static final String CONNECTION = "connection";
	private static final String NUMBER = "number";
	private static final String VEHICLE = "vehicle";
	private static final String EQUIPMENT = "equipment";
	private static final String VALUE = "value";
	private static final String SPONSOR = "sponsor";
	private static final String RESUPPLY = "resupply";
	private static final String RESUPPLY_MISSION = "resupply-mission";
	private static final String ARRIVAL_TIME = "arrival-time";
	private static final String RESOURCE = "resource";
	private static final String AMOUNT = "amount";
	private static final String PART = "part";
	private static final String PART_PACKAGE = "part-package";

	private static final String EVA_AIRLOCK = "EVA Airlock";
	
	// Random value indicator.
	public static final String RANDOM = "random";


	private double[] rover_values = new double[] { 0, 0 };
	private double[][] life_support_values = new double[2][7];

	// Data members
	private PartPackageConfig partPackageConfig;

	/**
	 * Constructor.
	 *
	 * @param settlementDoc     DOM document with settlement configuration.
	 * @param partPackageConfig the part package configuration.
	 * @throws Exception if error reading XML document.
	 */
	public SettlementConfig(Document settlementDoc, PartPackageConfig partPackageConfig) {
		super("settlement");
		setXSDName("settlement.xsd");

		this.partPackageConfig = partPackageConfig;

		loadMissionControl(settlementDoc);
		loadLifeSupportRequirements(settlementDoc);
		String [] defaults = loadSettlementTemplates(settlementDoc);

		loadDefaults(defaults);

		loadUserDefined();
	}

	public double[] getRoverValues() {
		return rover_values;
	}

	/**
	 * Load the rover range margin error from the mission control parameters of a
	 * settlement from the XML document.
	 *
	 * @return range margin.
	 * @throws Exception if error reading XML document.
	 */
	private void loadMissionControl(Document settlementDoc) {
		if (rover_values[0] != 0 || rover_values[1] != 0) {
			return;
		}

		Element root = settlementDoc.getRootElement();
		Element missionControlElement = root.getChild(MISSION_CONTROL);
		Element lifeSupportRange = (Element) missionControlElement.getChild(ROVER_LIFE_SUPPORT_RANGE_ERROR_MARGIN);
		Element fuelRange = (Element) missionControlElement.getChild(ROVER_FUEL_RANGE_ERROR_MARGIN);

		rover_values[0] = Double.parseDouble(lifeSupportRange.getAttributeValue(VALUE));
		if (rover_values[0] < 1.0 || rover_values[0] > 3.0)
			throw new IllegalStateException(
					"Error in SettlementConfig.xml: rover life support range error margin is beyond acceptable range.");

		rover_values[1] = Double.parseDouble(fuelRange.getAttributeValue(VALUE));
		if (rover_values[1] < 1.0 || rover_values[1] > 3.0)
			throw new IllegalStateException(
					"Error in SettlementConfig.xml: rover fuel range error margin is beyond acceptable range.");
	}

	/**
	 * Load the life support requirements from the XML document.
	 *
	 * @return an array of double.
	 * @throws Exception if error reading XML document.
	 */
	public double[][] getLifeSupportRequirements() {
		return life_support_values;
	}

	/**
	 * Load the life support requirements from the XML document.
	 *
	 * @return an array of double.
	 * @throws Exception if error reading XML document.
	 */
	private void loadLifeSupportRequirements(Document settlementDoc) {
		if (life_support_values[0][0] != 0) {
			// testing only the value at [0][0]
			return;
		}

		Element root = settlementDoc.getRootElement();
		Element req = (Element) root.getChild(LIFE_SUPPORT_REQUIREMENTS);

		String[] types = new String[] {
				TOTAL_PRESSURE,
				PARTIAL_PRESSURE_OF_O2,
				PARTIAL_PRESSURE_OF_N2,
				PARTIAL_PRESSURE_OF_CO2,
				TEMPERATURE,
				RELATIVE_HUMIDITY,
				VENTILATION};

		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < types.length; i++) {
				double [] t = getLowHighValues(req, types[i]);
				life_support_values[j][i] = t[j];
			}
		}
	}

	private double[] getLowHighValues(Element element, String name) {
		Element el = (Element) element.getChild(name);

		double a = Double.parseDouble(el.getAttributeValue(LOW));
		double b = Double.parseDouble(el.getAttributeValue(HIGH));

		return new double[] { a, b };
	}

	/**
	 * Load the settlement templates from the XML document.
	 *
	 * @param settlementDoc     DOM document with settlement configuration.
	 * @param partPackageConfig the part package configuration.
	 * @throws Exception if error reading XML document.
	 */
	private String[] loadSettlementTemplates(Document settlementDoc) {

		Element root = settlementDoc.getRootElement();
		Element templateList = root.getChild(SETTLEMENT_TEMPLATE_LIST);

		List<Element> templateNodes = templateList.getChildren(TEMPLATE);

		List<String> names = new ArrayList<>();
		for (Element templateElement : templateNodes) {
			names.add(templateElement.getAttributeValue(NAME));
		}
		return names.toArray(new String[0]);

	}
		
	@Override
	protected SettlementTemplate parseItemXML(Document doc, boolean predefined) {
		Element templateElement = doc.getRootElement();

		String settlementTemplateName = templateElement.getAttributeValue(NAME);
		String description = templateElement.getAttributeValue(DESCRIPTION);
		String sponsor = templateElement.getAttributeValue(SPONSOR);

		// Obtains the default population
		int defaultPopulation = Integer.parseInt(templateElement.getAttributeValue(DEFAULT_POPULATION));
		// Obtains the default numbers of robots
		int defaultNumOfRobots = Integer.parseInt(templateElement.getAttributeValue(DEFAULT_NUM_ROBOTS));

		// Add templateID
		SettlementTemplate template = new SettlementTemplate(
				settlementTemplateName,
				description,
				predefined,
				sponsor,
				defaultPopulation,
				defaultNumOfRobots);

		Set<Integer> existingIDs = new HashSet<>();
		// Add buildingTypeIDMap
		Map<String, Integer> buildingTypeIDMap = new HashMap<>();

		List<Element> buildingNodes = templateElement.getChildren(BUILDING);
		for (Element buildingElement : buildingNodes) {

			BoundedObject bounds = ConfigHelper.parseBoundedObject(buildingElement);

			int bid = -1;

			if (buildingElement.getAttribute(ID) != null) {
				bid = Integer.parseInt(buildingElement.getAttributeValue(ID));
			}

			if (existingIDs.contains(bid)) {
				throw new IllegalStateException(
						"Error in SettlementConfig : building ID " + bid + " in settlement template "
								+ settlementTemplateName + " is not unique.");
			} else if (bid != -1)
				existingIDs.add(bid);

			String buildingType = buildingElement.getAttributeValue(TYPE);
			
			if (buildingTypeIDMap.containsKey(buildingType)) {
				int last = buildingTypeIDMap.get(buildingType);
				buildingTypeIDMap.put(buildingType, last + 1);
			} else
				buildingTypeIDMap.put(buildingType, 1);

			// Create a building nickname for every building
			// NOTE: i = sid + 1 since i must be > 1, if i = 0, s = null

			int buildingTypeID = buildingTypeIDMap.get(buildingType);

			String buildingNickName = buildingType + " " + buildingTypeID;

			BuildingTemplate buildingTemplate = new BuildingTemplate(bid,
					buildingType, buildingNickName, bounds);

			template.addBuildingTemplate(buildingTemplate);

			// Create building connection templates.
			Element connectionListElement = buildingElement.getChild(CONNECTION_LIST);
			if (connectionListElement != null) {
				List<Element> connectionNodes = connectionListElement.getChildren(CONNECTION);
				for (Element connectionElement : connectionNodes) {
					int connectionID = Integer.parseInt(connectionElement.getAttributeValue(ID));

					if (buildingType.equalsIgnoreCase(EVA_AIRLOCK)) {
						buildingTemplate.addEVAAttachedBuildingID(connectionID);
					}
					
					// Check that connection ID is not the same as the building ID.
					if (connectionID == bid) {
						throw new IllegalStateException(
								"Connection ID cannot be the same as building ID for building: " + buildingType
										+ " in settlement template: " + settlementTemplateName);
					}

					LocalPosition connectionLoc = ConfigHelper.parseLocalPosition(connectionElement);
					buildingTemplate.addBuildingConnection(connectionID, connectionLoc);
				}
			}
		}

		// Check that building connections point to valid building ID's.
		List<BuildingTemplate> buildingTemplates = template.getBuildingTemplates();
		for (BuildingTemplate buildingTemplate : buildingTemplates) {
			List<BuildingConnectionTemplate> connectionTemplates = buildingTemplate
					.getBuildingConnectionTemplates();
			for (BuildingConnectionTemplate connectionTemplate : connectionTemplates) {
				if (!existingIDs.contains(connectionTemplate.getID())) {
					throw new IllegalStateException("Connection ID: " + connectionTemplate.getID()
							+ " invalid for building: " + buildingTemplate.getBuildingName()
							+ " in settlement template: " + settlementTemplateName);
				}
			}
		}

		// Load vehicles
		List<Element> vehicleNodes = templateElement.getChildren(VEHICLE);
		for (Element vehicleElement : vehicleNodes) {
			String vehicleType = vehicleElement.getAttributeValue(TYPE);
			int vehicleNumber = Integer.parseInt(vehicleElement.getAttributeValue(NUMBER));
			template.addVehicles(vehicleType, vehicleNumber);
		}

		// Load equipment
		List<Element> equipmentNodes = templateElement.getChildren(EQUIPMENT);
		for (Element equipmentElement : equipmentNodes) {
			String equipmentType = equipmentElement.getAttributeValue(TYPE);
			int equipmentNumber = Integer.parseInt(equipmentElement.getAttributeValue(NUMBER));
			template.addEquipment(equipmentType, equipmentNumber);
		}

		// Load resources
		List<Element> resourceNodes = templateElement.getChildren(RESOURCE);
		for (Element resourceElement : resourceNodes) {
			String resourceType = resourceElement.getAttributeValue(TYPE);
			AmountResource resource = ResourceUtil.findAmountResource(resourceType);
			if (resource == null)
				logger.severe(resourceType + " shows up in settlements.xml but doesn't exist in resources.xml.");
			else {
				double resourceAmount = Double.parseDouble(resourceElement.getAttributeValue(AMOUNT));
				template.addAmountResource(resource, resourceAmount);
			}

		}

		// Load parts
		List<Element> partNodes = templateElement.getChildren(PART);
		for (Element partElement : partNodes) {
			String partType = partElement.getAttributeValue(TYPE);
			Part part = (Part) ItemResourceUtil.findItemResource(partType);
			if (part == null)
				logger.severe(partType + " shows up in settlements.xml but doesn't exist in parts.xml.");
			else {
				int partNumber = Integer.parseInt(partElement.getAttributeValue(NUMBER));
				template.addPart(part, partNumber);
			}
		}

		// Load part packages
		List<Element> partPackageNodes = templateElement.getChildren(PART_PACKAGE);
		for (Element partPackageElement : partPackageNodes) {
			String packageName = partPackageElement.getAttributeValue(NAME);
			int packageNumber = Integer.parseInt(partPackageElement.getAttributeValue(NUMBER));
			if (packageNumber > 0) {
				for (int z = 0; z < packageNumber; z++) {
					Map<Part, Integer> partPackage = partPackageConfig.getPartsInPackage(packageName);
					Iterator<Part> i = partPackage.keySet().iterator();
					while (i.hasNext()) {
						Part part = i.next();
						int partNumber = partPackage.get(part);
						template.addPart(part, partNumber);
					}
				}
			}
		}

		// Load resupplies
		Element resupplyList = templateElement.getChild(RESUPPLY);
		if (resupplyList != null) {
			List<Element> resupplyNodes = resupplyList.getChildren(RESUPPLY_MISSION);
			for (Element resupplyMissionElement : resupplyNodes) {
				String resupplyName = resupplyMissionElement.getAttributeValue(NAME);
				double arrivalTime = Double.parseDouble(resupplyMissionElement.getAttributeValue(ARRIVAL_TIME));
				ResupplyMissionTemplate resupplyMissionTemplate = new ResupplyMissionTemplate(resupplyName,
						arrivalTime);
				template.addResupplyMissionTemplate(resupplyMissionTemplate);
			}
		}

		return template;
	}

	/**
	 * It is not possible to creae new SettlementTemplates via the application.
	 */
	@Override
	protected Document createItemDoc(SettlementTemplate item) {
		throw new UnsupportedOperationException("Saving Settlement templates is not supported");
	}
}

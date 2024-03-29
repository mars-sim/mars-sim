/*
 * Mars Simulation Project
 * SettlementConfig.java
 * @date 2023-07-30
 * @author Scott Davis
 */
package com.mars_sim.core.structure;

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

import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.configuration.UserConfigurableConfig;
import com.mars_sim.core.interplanetary.transport.resupply.ResupplyConfig;
import com.mars_sim.core.interplanetary.transport.resupply.ResupplyConfig.SupplyManifest;
import com.mars_sim.core.interplanetary.transport.resupply.ResupplySchedule;
import com.mars_sim.core.person.ai.shift.ShiftPattern;
import com.mars_sim.core.person.ai.shift.ShiftSpec;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.PartPackageConfig;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.robot.RobotTemplate;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.building.BuildingPackageConfig;
import com.mars_sim.core.structure.building.BuildingTemplate;
import com.mars_sim.core.structure.building.BuildingTemplate.BuildingConnectionTemplate;
import com.mars_sim.mapdata.location.BoundedObject;
import com.mars_sim.mapdata.location.LocalPosition;

/**
 * Provides configuration information about settlements templates. Uses a DOM document to
 * get the information.
 */
public class SettlementConfig extends UserConfigurableConfig<SettlementTemplate> {

	public record ResourceLimits(int reserve, int max) {}

	private static final Logger logger = Logger.getLogger(SettlementConfig.class.getName());

	// Element names
	private static final String BUILDING_PACKAGE = "building-package";
	private static final String BUILDING = "building";
	private static final String CONNECTOR = "connector";
	private static final String STANDALONE = "standalone";
	
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
	private static final String OBJECTIVE = "objective";

	private static final String ID = "id";
	private static final String HATCH_FACE = "hatch-facing";
	private static final String ZONE = "zone";
	private static final String TYPE = "type";
	private static final String CONNECTION_LIST = "connection-list";
	private static final String CONNECTION = "connection";
	private static final String NUMBER = "number";
	private static final String VEHICLE = "vehicle";
	private static final String EQUIPMENT = "equipment";
	private static final String BIN = "bin";	
	private static final String VALUE = "value";
	private static final String SPONSOR = "sponsor";
	private static final String RESUPPLY = "resupply";
	private static final String RESUPPLY_MISSION = "resupply-mission";
	private static final String FIRST_ARRIVAL_TIME = "first-arrival-time";
	private static final String RESOURCE = "resource";
	private static final String AMOUNT = "amount";
	private static final String PART = "part";
	private static final String PART_PACKAGE = "part-package";
	
	private static final String EVA_AIRLOCK = "EVA Airlock";

	private static final String SHIFT_PATTERN = "shift-pattern";
	private static final String SHIFT_PATTERNS = "shifts";
	private static final String SHIFT_SPEC = "shift";
	private static final String SHIFT_START = "start";
	private static final String SHIFT_END = "end";
	private static final String SHIFT_PERC = "pop-percentage";
	private static final String LEAVE_PERC = "leave-perc";
	private static final String ROTATION_SOLS = "rotation-sols";
	private static final String MODEL = "model";
	private static final String ROBOT = "robot";

	/** These must be present in the settlements.xml */
	public static final String STANDARD_4_SHIFT = "Standard 4 Shift";
	public static final String STANDARD_3_SHIFT = "Standard 3 Shift";
	public static final String STANDARD_2_SHIFT = "Standard 2 Shift";
	public static final String LONG_3_SHIFT = "Long 3 Shift";
	public static final String SKELETON_NIGHT_SHIFT = "Skeleton Night Shift";

	
	private static final String FREQUENCY = "frequency-sols";
	private static final String MANIFEST_NAME = "manifest-name";

	private static final String ESSENTIAL_RESOURCES = "essential-resources";
	private static final String RESERVE = "reserve";
	private static final String MAX = "max";
	
	private double[] roverValues = new double[] { 0, 0 };
	private double[][] lifeSupportValues = new double[2][7];

	// Data members
	private String defaultShift;

	private PartPackageConfig partPackageConfig;	
	private BuildingPackageConfig buildingPackageConfig;
	private ResupplyConfig resupplyConfig;
	
	private Map<String, ShiftPattern> shiftDefinitions = new HashMap<>();

	private Map<Integer, ResourceLimits> resLimits = new HashMap<>();

	/**
	 * Constructor.
	 *
	 * @param settlementDoc     DOM document with settlement configuration.
	 * @param partPackageConfig the part package configuration.
	 * @throws Exception if error reading XML document.
	 */
	public SettlementConfig(Document settlementDoc, 
							PartPackageConfig partPackageConfig,
							BuildingPackageConfig buildingPackageConfig,
							ResupplyConfig resupplyConfig) {
		super("settlement");
		setXSDName("settlement.xsd");

		this.partPackageConfig = partPackageConfig;
		this.buildingPackageConfig = buildingPackageConfig;
		this.resupplyConfig = resupplyConfig;
		
		Element root = settlementDoc.getRootElement();
		loadMissionControl(root.getChild(MISSION_CONTROL));
		loadLifeSupportRequirements(root.getChild(LIFE_SUPPORT_REQUIREMENTS));
		loadResourceLimits(root.getChild(ESSENTIAL_RESOURCES));
		loadShiftPatterns(root.getChild(SHIFT_PATTERNS));
		String [] defaults = loadSettlementTemplates(settlementDoc);

		loadDefaults(defaults);

		loadUserDefined();
	}

	private void loadResourceLimits(Element limits) {
		
		List<Element> resources = limits.getChildren(RESOURCE);
		for(Element node : resources) {
			String resName = node.getAttributeValue(NAME);
			int resId = ResourceUtil.findIDbyAmountResourceName(resName);
			if (resId <= 0) {
				throw new IllegalArgumentException("Cannot find essential resource called " + resName);
			}

			int reserve = Integer.parseInt(node.getAttributeValue(RESERVE));
			int max = Integer.parseInt(node.getAttributeValue(MAX));

			resLimits.put(resId, new ResourceLimits(reserve, max));
		}
	}

	public Map<Integer,ResourceLimits> getEssentialResources() {
		return resLimits;
	}

	public double[] getRoverValues() {
		return roverValues;
	}

	/**
	 * Loads the shift patterns details.
	 * 
	 * @throws Exception if error reading XML document.
	 */
	private void loadShiftPatterns(Element shiftPatterns) {

		List<Element> shiftNodes = shiftPatterns.getChildren(SHIFT_PATTERN);
		for(Element node : shiftNodes) {
			String name = node.getAttributeValue(NAME);

			int rotSol = ConfigHelper.getOptionalAttributeInt(node, ROTATION_SOLS, 10);
			int leave = ConfigHelper.getOptionalAttributeInt(node, LEAVE_PERC, 10);
			List<ShiftSpec> shiftSpecs = new ArrayList<>();
			List<Element> specNodes = node.getChildren(SHIFT_SPEC);
			for(Element spec : specNodes) {
				String sname = spec.getAttributeValue(NAME);
				if (defaultShift == null) {
					defaultShift = sname;
				}
				int start = Integer.parseInt(spec.getAttributeValue(SHIFT_START));
				int end = Integer.parseInt(spec.getAttributeValue(SHIFT_END));
				int population = Integer.parseInt(spec.getAttributeValue(SHIFT_PERC));
				
				shiftSpecs.add(new ShiftSpec(sname, start, end, population));
			}

			shiftDefinitions.put(name.toLowerCase(), new ShiftPattern(name, shiftSpecs, rotSol, leave));
		}
	}

	/**
	 * Find a shift pattern by name.
	 * @param name
	 * @return
	 */
	public ShiftPattern getShiftPattern(String name) {
		ShiftPattern pattern = shiftDefinitions.get(name.toLowerCase());
		if (pattern == null) {
			throw new IllegalArgumentException("No shift pattern called " + name);
		}

		return pattern;
	}

	/**
	 * Loads the rover range margin error from the mission control parameters of a
	 * settlement from the XML document.
	 *
	 * @return range margin.
	 * @throws Exception if error reading XML document.
	 */
	private void loadMissionControl(Element missionControlElement) {
		if (roverValues[0] != 0 || roverValues[1] != 0) {
			return;
		}

		Element lifeSupportRange = missionControlElement.getChild(ROVER_LIFE_SUPPORT_RANGE_ERROR_MARGIN);
		Element fuelRange = missionControlElement.getChild(ROVER_FUEL_RANGE_ERROR_MARGIN);

		roverValues[0] = Double.parseDouble(lifeSupportRange.getAttributeValue(VALUE));
		if (roverValues[0] < 1.0 || roverValues[0] > 3.0)
			throw new IllegalStateException(
					"Error in SettlementConfig.xml: rover life support range error margin is beyond acceptable range.");

		roverValues[1] = Double.parseDouble(fuelRange.getAttributeValue(VALUE));
		if (roverValues[1] < 1.0 || roverValues[1] > 3.0)
			throw new IllegalStateException(
					"Error in SettlementConfig.xml: rover fuel range error margin is beyond acceptable range.");
	}

	/**
	 * Loads the life support requirements from the XML document.
	 *
	 * @return an array of double.
	 * @throws Exception if error reading XML document.
	 */
	public double[][] getLifeSupportRequirements() {
		return lifeSupportValues;
	}

	/**
	 * Loads the life support requirements from the XML document.
	 *
	 * @return an array of double.
	 * @throws Exception if error reading XML document.
	 */
	private void loadLifeSupportRequirements(Element req) {
		if (lifeSupportValues[0][0] != 0) {
			// testing only the value at [0][0]
			return;
		}

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
				lifeSupportValues[j][i] = t[j];
			}
		}
	}

	private double[] getLowHighValues(Element element, String name) {
		Element el = element.getChild(name);

		double a = Double.parseDouble(el.getAttributeValue(LOW));
		double b = Double.parseDouble(el.getAttributeValue(HIGH));

		return new double[] { a, b };
	}

	/**
	 * Loads the settlement templates from the XML document.
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

	/**
	 * Parses the building or connector list.
	 * 
	 * @param templateElement
	 * @param settlementTemplate
	 * @param settlementTemplateName
	 * @param existingStreetIDs
	 * @param typeNumMap
	 */
	private void parseBuildingORConnectorList(Element templateElement, SettlementTemplate settlementTemplate, 
			String elementName,
			String settlementTemplateName, 
			Set<String> existingStreetIDs, 
			Map<String, Integer> buildingTypeNumMap) {
		
		List<Element> buildingNodes = templateElement.getChildren(elementName);
		for (Element buildingElement : buildingNodes) {

			BoundedObject bounds = ConfigHelper.parseBoundedObject(buildingElement);

			// Track the id
			String id = "";

			if (buildingElement.getAttribute(ID) != null) {
				id = buildingElement.getAttributeValue(ID);
			}

			if (existingStreetIDs.contains(id)) {
				throw new IllegalStateException(
						"Error in SettlementConfig: the id " + id + " in settlement template "
								+ settlementTemplateName + " is not unique.");
			} else if (!id.equalsIgnoreCase("")) {
				existingStreetIDs.add(id);
			}
			
			// Assume the zone as 0
			int zone = ConfigHelper.getOptionalAttributeInt(buildingElement, ZONE, 0);
			
			// Get the building type
			String buildingType = buildingElement.getAttributeValue(TYPE);
			
			int last = getNextBuildingTypeID(buildingType, buildingTypeNumMap);

			// e.g. Lander Hab 1, Lander Hab 2
			String uniqueName = buildingType + " " + last;

			BuildingTemplate buildingTemplate = new BuildingTemplate(id, zone, 
					buildingType, uniqueName, bounds);

			// Need to check for collision with previous building templates
			for (BuildingTemplate t: settlementTemplate.getBuildings()) {
				BoundedObject o0 = buildingTemplate.getBounds();
				BoundedObject o1 = t.getBounds();
				if (BoundedObject.isCollided(o0, o1)) {
					throw new IllegalStateException(uniqueName + " collided with " + t.getBuildingName() 
						+ " in settlement template: " + settlementTemplateName + ".");
				}
			}
			
			settlementTemplate.addBuildingTemplate(buildingTemplate);

			// Create building connection templates.
			Element connectionListElement = buildingElement.getChild(CONNECTION_LIST);
			if (connectionListElement != null) {
				List<Element> connectionNodes = connectionListElement.getChildren(CONNECTION);
				for (Element connectionElement : connectionNodes) {
					String connectionID = connectionElement.getAttributeValue(ID);

					if (buildingType.equalsIgnoreCase(EVA_AIRLOCK)) {
						buildingTemplate.addEVAAttachedBuildingID(connectionID);
					}
					
					// Check that connection ID is not the same as the building ID.
					if (connectionID.equalsIgnoreCase(id)) {
						throw new IllegalStateException(
								"Connection ID cannot be the same as id for this building/connector " 
								+ buildingType
								+ " in settlement template: " + settlementTemplateName + ".");
					}

					String hatchFace = connectionElement.getAttributeValue(HATCH_FACE);
					
					if (hatchFace == null) {
						LocalPosition connectionLoc = ConfigHelper.parseLocalPosition(connectionElement);
						buildingTemplate.addBuildingConnection(connectionID, connectionLoc);
					}
					else {
						buildingTemplate.addBuildingConnection(connectionID, hatchFace);
					}
				}
			}
		}
	}
	
	/**
	 * Gets an available building type suffix ID for a new building.
	 *
	 * @param buildingType
	 * @return type ID (starting from 1, not zero)
	 */
	private int getNextBuildingTypeID(String buildingType, Map<String, Integer> buildingTypeIDMap) {
		int last = 1;
		if (buildingTypeIDMap.containsKey(buildingType)) {
			last = buildingTypeIDMap.get(buildingType);
			buildingTypeIDMap.put(buildingType, ++last);
		} else {
			buildingTypeIDMap.put(buildingType, last);
		}
		return last;
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

		// Look up the shift pattern
		String shiftPattern = templateElement.getAttributeValue(SHIFT_PATTERN);
		if (shiftPattern == null) {
			if (defaultPopulation >= 12) {
				shiftPattern = LONG_3_SHIFT;
			}
			else if (defaultPopulation >= 24) {
				shiftPattern = STANDARD_3_SHIFT;
			}
			else if (defaultPopulation >= 36) {
				shiftPattern = STANDARD_4_SHIFT;
			}	
			else {
				shiftPattern = STANDARD_2_SHIFT;
			}
		}
		ShiftPattern pattern = getShiftPattern(shiftPattern);

		// Add templateID
		SettlementTemplate settlementTemplate = new SettlementTemplate(
				settlementTemplateName,
				description,
				predefined,
				sponsor,
				pattern,
				defaultPopulation,
				defaultNumOfRobots);

		// Check the objective
		String objectiveText = templateElement.getAttributeValue(OBJECTIVE);
		if (objectiveText != null) {
			var oType = ObjectiveType.valueOf(ConfigHelper.convertToEnumName(objectiveText));
			settlementTemplate.setObjective(oType);
		}


		Set<String> existingBuildingIDs = new HashSet<>();		
		Map<String, Integer> buildingTypeNumMap = new HashMap<>();
		
		// Process a list of buildings
		parseBuildingORConnectorList(templateElement, settlementTemplate, 
				BUILDING,
				settlementTemplateName, 
				existingBuildingIDs, 
				buildingTypeNumMap);
		
		// Process a list of connectors
		Set<String> existingConnectorIDs = new HashSet<>();
		parseBuildingORConnectorList(templateElement, settlementTemplate, 
				CONNECTOR,
				settlementTemplateName, 
				existingConnectorIDs, 
				buildingTypeNumMap);

		// Process a list of standalone buildings
		parseBuildingORConnectorList(templateElement, settlementTemplate, 
				STANDALONE,
				settlementTemplateName, 
				existingBuildingIDs, 
				buildingTypeNumMap);
		
		
		// Check that building connections point to valid building ID's.
		List<BuildingTemplate> buildingTemplates = settlementTemplate.getBuildings();
		
		for (BuildingTemplate buildingTemplate : buildingTemplates) {	
			
			List<BuildingConnectionTemplate> connectionTemplates = buildingTemplate
					.getBuildingConnectionTemplates();

			for (BuildingConnectionTemplate connectionTemplate : connectionTemplates) {
				
				if (!existingBuildingIDs.contains(connectionTemplate.getID())
					&& !existingConnectorIDs.contains(connectionTemplate.getID())) {
					throw new IllegalStateException("XML issues with settlement template: " 
							+ settlementTemplateName 
							+ " in " + buildingTemplate.getBuildingName() 
							+ " at connection id " + connectionTemplate.getID()
							+ ". existingBuildingIDs: " + existingBuildingIDs
							+ ". existingConnectorIDs: " + existingConnectorIDs
							);
				}
			}
		}

		// Load vehicles
		List<Element> vehicleNodes = templateElement.getChildren(VEHICLE);
		for (Element vehicleElement : vehicleNodes) {
			String vehicleType = vehicleElement.getAttributeValue(TYPE);
			int vehicleNumber = Integer.parseInt(vehicleElement.getAttributeValue(NUMBER));
			settlementTemplate.addVehicles(vehicleType, vehicleNumber);
		}

		// Load robots
		List<Element> robotNodes = templateElement.getChildren(ROBOT);
		for (Element robotElement : robotNodes) {
			RobotType rType = RobotType.valueOf(ConfigHelper.convertToEnumName(robotElement.getAttributeValue(TYPE)));
			String name = robotElement.getAttributeValue(NAME);
			String model = robotElement.getAttributeValue(MODEL);
			settlementTemplate.addRobot(new RobotTemplate(name, rType, model));
		}
		
		// Load equipment
		List<Element> equipmentNodes = templateElement.getChildren(EQUIPMENT);
		for (Element equipmentElement : equipmentNodes) {
			String equipmentType = equipmentElement.getAttributeValue(TYPE);
			int equipmentNumber = Integer.parseInt(equipmentElement.getAttributeValue(NUMBER));
			settlementTemplate.addEquipment(equipmentType, equipmentNumber);
		}

		// Load bins
		List<Element> binNodes = templateElement.getChildren(BIN);
		for (Element binElement : binNodes) {
			String binType = binElement.getAttributeValue(TYPE);
			int binNumber = Integer.parseInt(binElement.getAttributeValue(NUMBER));
			settlementTemplate.addBins(binType, binNumber);
		}
		
		// Load resources
		List<Element> resourceNodes = templateElement.getChildren(RESOURCE);
		for (Element resourceElement : resourceNodes) {
			String resourceType = resourceElement.getAttributeValue(TYPE);
			AmountResource resource = ResourceUtil.findAmountResource(resourceType);
			if (resource == null)
				logger.severe(resourceType + " shows up in settlement template "
						+ settlementTemplateName
						+ " but doesn't exist in resources.xml.");
			else {
				double resourceAmount = Double.parseDouble(resourceElement.getAttributeValue(AMOUNT));
				settlementTemplate.addAmountResource(resource, resourceAmount);
			}
		}

		// Load parts
		List<Element> partNodes = templateElement.getChildren(PART);
		for (Element partElement : partNodes) {
			String partType = partElement.getAttributeValue(TYPE);
			Part part = (Part) ItemResourceUtil.findItemResource(partType);
			if (part == null)
				logger.severe(partType + " shows up in settlement template "
						+ settlementTemplateName
						+ " but doesn't exist in parts.xml.");
			else {
				int partNumber = Integer.parseInt(partElement.getAttributeValue(NUMBER));
				settlementTemplate.addPart(part, partNumber);
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
						settlementTemplate.addPart(part, partNumber);
					}
				}
			}
		}

		// Load building packages
		List<Element> buildingPackageNodes = templateElement.getChildren(BUILDING_PACKAGE);
		for (Element buildingPackageElement : buildingPackageNodes) {
			String packageName = buildingPackageElement.getAttributeValue(NAME);
			
			List<BuildingTemplate> buildingPackages = buildingPackageConfig.getBuildingsInPackage(packageName);

			for (BuildingTemplate buildingTemplate: buildingPackages) {

				// Get the building type
				String buildingType = buildingTemplate.getBuildingType();
				
				int last = getNextBuildingTypeID(buildingType, buildingTypeNumMap);

				String uniqueName = buildingType + " " + last;
	
				// Overwrite with a new building nick name
				buildingTemplate.setBuildingName(uniqueName);
	
				settlementTemplate.addBuildingTemplate(buildingTemplate);
			}
		}
		
		// Load resupplies
		Element resupplyList = templateElement.getChild(RESUPPLY);
		if (resupplyList != null) {
			List<Element> resupplyNodes = resupplyList.getChildren(RESUPPLY_MISSION);
			for (Element resupplyMissionElement : resupplyNodes) {
				String resupplyName = resupplyMissionElement.getAttributeValue(NAME);
				String manifestName = resupplyMissionElement.getAttributeValue(MANIFEST_NAME);
				SupplyManifest manifest = resupplyConfig.getSupplyManifest(manifestName);
				double arrivalTime = ConfigHelper.getOptionalAttributeDouble(resupplyMissionElement, FIRST_ARRIVAL_TIME, 0.1);
				int frequency = ConfigHelper.getOptionalAttributeInt(resupplyMissionElement,
												FREQUENCY, -1);			
				ResupplySchedule resupplyMissionTemplate = new ResupplySchedule(resupplyName,
						arrivalTime, manifest, frequency);
				settlementTemplate.addResupplyMissionTemplate(resupplyMissionTemplate);
			}
		}

		return settlementTemplate;
	}

	/**
	 * It is not possible to create new SettlementTemplates via the application.
	 */
	@Override
	protected Document createItemDoc(SettlementTemplate item) {
		throw new UnsupportedOperationException("Saving Settlement templates is not supported.");
	}
}

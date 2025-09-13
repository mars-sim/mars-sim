/*
 * Mars Simulation Project
 * BuildingConfig.java
 * @date 2023-05-31
 * @author Scott Davis
 */
package com.mars_sim.core.building.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.ConstructionType;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.manufacture.ManufactureConfig;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resourceprocess.ResourceProcessConfig;
import com.mars_sim.core.resourceprocess.ResourceProcessEngine;
import com.mars_sim.core.science.ScienceType;

/**
 * Provides configuration information about settlement buildings. Uses a DOM
 * document to get the information.
 */
public class BuildingConfig {

	// Element and attribute names
	private static final String CATEGORY = "category";
	private static final String DESCRIPTION = "description";
	private static final String BUILDING = "building";
	private static final String NAME = "name";
	private static final String BUILDING_TYPE = "type";
	private static final String WIDTH = "width";
	private static final String LENGTH = "length";
	private static final String N_S_ALIGNMENT = "north-south-alignment";
	private static final String CONSTRUCTION = "construction";
	private static final String BASE_LEVEL = "base-level";

	private static final String WEAR_LIFETIME = "wear-lifetime";
	private static final String MAINTENANCE_TIME = "maintenance-time";
	private static final String ROOM_TEMPERATURE = "room-temperature";

	private static final String FUNCTIONS = "functions";
	private static final String CAPACITY = "capacity";

	private static final String RESEARCH_SPECIALTY = "research-specialty";

	private static final String NUMBER_MODULES = "number-modules";

	// The common power required XML attribute
	public static final String POWER_REQUIRED = "power-required";

	private static final String BASE_POWER = "base-power";
	private static final String BASE_POWER_DOWN_POWER = "base-power-down-power";
	
	private static final String PROCESS_ENGINE = "process-engine";
	private static final String STORAGE = "storage";
	private static final String RESOURCE_CAPACITY = "resource-capacity";
	private static final String RESOURCE_INITIAL = "resource-initial";
	private static final String TYPE = "type";
	private static final String AMOUNT = "amount";
	private static final String NUMBER = "number";
	private static final String MODULES = "modules";
	private static final String CONVERSION = "thermal-conversion-efficiency";
	private static final String PERCENT_LOADING = "percent-loading";

	private static final String ROVER = "rover";
	private static final String FLYER = "flyer";
	private static final String UTILITY = "utility";

	private static final String ACTIVITY = "activity";

	private static final String TOOLING = "tooling";

	private static final String HEAT_SOURCE = "heat-source";
	private static final String THERMAL_GENERATION = "thermal-generation";

	private static final String SCOPES = "scopes";
	private static final String SCOPE = "scope";
	
	// Power source types
	private static final String POWER_GENERATION = "power-generation";
	private static final String POWER_SOURCE = "power-source";
	private static final String POWER = "power";
	private static final Set<String> DEFAULT_SOURCE_ATTR = Set.of(TYPE, MODULES, CONVERSION, PERCENT_LOADING);

	private Map<String, BuildingSpec> buildSpecMap = new HashMap<>();

	/** A collection of building level system scopes (as defined for each building in buldings.xml. */
	private Map<String, Set<String>> buildingScopes = new HashMap<>();
	
	private Set<FunctionType> activityFunctions  = new HashSet<>();
	
	/**
	 * Constructor.
	 *
	 * @param buildingDoc DOM document with building configuration
	 * @param resProcConfig 
	 */
	public BuildingConfig(Document buildingDoc, ResourceProcessConfig resProcConfig, ManufactureConfig manuConfig) {

		List<Element> buildingNodes = buildingDoc.getRootElement().getChildren(BUILDING);
		for (Element buildingElement : buildingNodes) {
			String buildingType = buildingElement.getAttributeValue(BUILDING_TYPE);
			String key = generateSpecKey(buildingType);
			buildSpecMap.put(key, parseBuilding(buildingType, buildingElement, resProcConfig, manuConfig));
		}
	}

	/**
	 * Gets a set of all building specs.
	 *
	 * @return set of building types.
	 */
	public Collection<BuildingSpec> getBuildingSpecs() {
		return buildSpecMap.values();
	}

	/**
	 * Gets a set of all building types.
	 *
	 * @return set of building types.
	 */
	public Collection<String> getBuildingTypes() {
		return buildSpecMap.keySet();
	}
	
	/**
	 * Parses a full building spec node.
	 *
	 * @param buildingTypeName
	 * @param buildingElement
	 * @param resProcConfig 
	 * @param manuConfig 
	 * @return
	 */
	private BuildingSpec parseBuilding(String buildingTypeName, Element buildingElement,
								ResourceProcessConfig resProcConfig, ManufactureConfig manuConfig) {

		var newSpec = parseBuildingSpec(buildingElement, buildingTypeName, resProcConfig, manuConfig);
		
		String construction = buildingElement.getAttributeValue(CONSTRUCTION);
		if (construction != null) {
			newSpec.setConstruction(ConstructionType.valueOf(ConfigHelper.convertToEnumName(construction)));
		}

		// Get Storage
		Element functionsElement = buildingElement.getChild(FUNCTIONS);
		Element storageElement = functionsElement.getChild(STORAGE);
		if (storageElement != null) {
			parseStorage(newSpec, storageElement);
		}
		return newSpec;
	}

	/**
	 * Parse and create the basic BuildingSpec
	 * @param buildingElement
	 * @param buildingTypeName
	 * @param manuConfig
	 * @return
	 */
	private BuildingSpec parseBuildingSpec(Element buildingElement, String buildingTypeName,
									ResourceProcessConfig resProcConfig, ManufactureConfig manuConfig) {
		double width = Double.parseDouble(buildingElement.getAttributeValue(WIDTH));
		double length = Double.parseDouble(buildingElement.getAttributeValue(LENGTH));
		String alignment = buildingElement.getAttributeValue(N_S_ALIGNMENT);
		int baseLevel = Integer.parseInt(buildingElement.getAttributeValue(BASE_LEVEL));
		double presetTemp = Double.parseDouble(buildingElement.getAttributeValue(ROOM_TEMPERATURE));
		int maintenanceTime = Integer.parseInt(buildingElement.getAttributeValue(MAINTENANCE_TIME));
		int wearLifeTime = Integer.parseInt(buildingElement.getAttributeValue(WEAR_LIFETIME));

		Element powerElement = buildingElement.getChild(POWER_REQUIRED);
		double basePowerRequirement = Double.parseDouble(powerElement.getAttributeValue(BASE_POWER));
		double basePowerDownPowerRequirement = Double.parseDouble(powerElement.getAttributeValue(BASE_POWER_DOWN_POWER));
		
		// Process description
		Element descElement = buildingElement.getChild(DESCRIPTION);
		String desc = descElement.getValue().trim();
		desc = desc.replaceAll("\t+", "").replaceAll("\s+", " ").replace("   ", " ").replace("  ", " ");
		
		// Process the scopes
		Set<String> scopeNames = new HashSet<>();
		Element scopesElement = buildingElement.getChild(SCOPES);
		if (scopesElement != null) {
			for (Element element : scopesElement.getChildren(SCOPE)) {
				String name = element.getAttributeValue(NAME);
				scopeNames.add(name);
			}		
			buildingScopes.put(buildingTypeName, scopeNames);
		}
		
		// Get functions
		Map<FunctionType, FunctionSpec> supportedFunctions = new EnumMap<>(FunctionType.class);
		Element funcElement = buildingElement.getChild(FUNCTIONS);
		for (Element element : funcElement.getChildren()) {
			
			// Parse extras
			FunctionSpec fspec = parseFunctionSpec(buildingTypeName, element, manuConfig, resProcConfig, width, length);

			supportedFunctions.put(fspec.getType(), fspec);
		}

		String categoryString = buildingElement.getAttributeValue(CATEGORY);
		BuildingCategory category = null;
		if (categoryString != null) {
			category = BuildingCategory.valueOf(ConfigHelper.convertToEnumName(categoryString));
		}
		else {
			// Derive category from Functions
			category = deriveCategory(supportedFunctions.keySet());
		}

		return new BuildingSpec(this, buildingTypeName, desc, category, 
				width, length, alignment, baseLevel,
			 	presetTemp, maintenanceTime, wearLifeTime,
			 	basePowerRequirement, basePowerDownPowerRequirement,
			 	supportedFunctions);
	}

	/**
	 * Gets the spot name that is best associated with a certain FunctionType.
	 * 
	 * @param function
	 * @return
	 */
	private static String getDefaultSpotName(FunctionType function) {
		return switch(function) {
			case ASTRONOMICAL_OBSERVATION -> "Post";
			case ADMINISTRATION -> "Desk";
			case COMMUNICATION -> "Node";
			case COMPUTATION -> "Rack";
			case COOKING -> "Cook";
			case EVA -> "Room";
			case DINING -> "Dine";
			case LIVING_ACCOMMODATION -> "Bed";
			case MANAGEMENT -> "Mgt";
			case MANUFACTURE -> "Slot";
			case MEDICAL_CARE -> "Bay";
			case RECREATION -> "Rec";
			case RESEARCH -> "Lab";
			case RESOURCE_PROCESSING -> "Pipe";
			case WASTE_PROCESSING -> "Bin";
			case ROBOTIC_STATION -> "Plug";
			default -> "";
		};
	}

	/**
	 * Derives the category from the types of Functions.
	 * 
	 * @param functions
	 * @return
	 */
	private static BuildingCategory deriveCategory(Set<FunctionType> functions) {

		// Get a set of categories
		Set<BuildingCategory> cats = functions.stream()
						.map(f -> f.getCategory())
						.filter(c -> c != null)
						.collect(Collectors.toSet());

		BuildingCategory category = BuildingCategory.CONNECTION;
		if (!cats.isEmpty()) {
			// Find the category with the lowest ordinal as that is the best to represent
			// this set of Functions
			int lowestOrdinal = 999;
			for (BuildingCategory c : cats) {
				if (c.ordinal() < lowestOrdinal) {
					lowestOrdinal = c.ordinal();
					category = c;
				}
			}
		}
		return category;
	}

	/**
	 * Creates factory method to create FunctionSpecs.
	 * 
	 * @param width 
	 * @return
	 */
	private FunctionSpec parseFunctionSpec(String context, Element element, ManufactureConfig manuConfig,
											ResourceProcessConfig resProcConfig,
											double width, double length) {
		String name = element.getName();
		context += " - " + name;
		FunctionType function = FunctionType.valueOf(ConfigHelper.convertToEnumName(name));

		// Get activity spots
		String spotName = getDefaultSpotName(function);
		Set<NamedPosition> spots = parseNamedPositions(element, ACTIVITY,
												spotName, width, length);

		// Record that this Function has activity spots
		if (!spots.isEmpty()) {
			activityFunctions.add(function);
		}

		// Get attributes as basic properties
		Map<String, Object> props = new HashMap<>();
		for (Attribute attr : element.getAttributes()) {
			props.put(attr.getName(), attr.getValue());
		}

		// Check for extra function specifics	
		if (function == FunctionType.MANUFACTURE) {
			var tools = ConfigHelper.parseIntList(context, element.getChildren(TOOLING), NAME,
											manuConfig::getTooling, NUMBER);
			props.put(TOOLING, tools);
		}

		var base = new FunctionSpec(function, props, spots);

		// Some function needs extra properties
		switch(function) {
			case EVA -> base = createEVASpec(base, element);
			case MEDICAL_CARE -> base = createMedicalCareSpec(base, element, width, length);
			case POWER_GENERATION -> base = createSourcesSpec(base, element, POWER_SOURCE, POWER);
			case RESEARCH -> base = createResearchSpec(base, element);
			case THERMAL_GENERATION -> base = createSourcesSpec(base, element, HEAT_SOURCE, CAPACITY);
			case WASTE_PROCESSING, RESOURCE_PROCESSING -> base = createResourceProcessingSpec(base, element, resProcConfig);
			case VEHICLE_MAINTENANCE -> base = createVehicleMaintenanceSpec(base, element, width, length);
			default -> { // No need to do anything
						}
		}


		return base;
	}

	/**
	 * Create a spec for an EVA function
	 */
	private FunctionSpec createEVASpec(FunctionSpec base, Element evaElement) {
		var central = ConfigHelper.parseRelativePosition(evaElement.getChild("center-position"));
		var interior = ConfigHelper.parseRelativePosition(evaElement.getChild("interior-position"));
		var exterior = ConfigHelper.parseRelativePosition(evaElement.getChild("exterior-position"));

		return new EVASpec(base, central, interior, exterior);
	}

	/**
	 * Create a medical care by further passing specific medical properties
	 */
	private FunctionSpec createMedicalCareSpec(FunctionSpec base, Element medElement, double width, double length) {
		var beds = parseNamedPositions(medElement,  "bed", "Bed", width, length);
		return new MedicalCareSpec(base, beds);
	}
		
	/**
	 * Parse the vehicle maintenance specific elements
	 */
	private FunctionSpec createVehicleMaintenanceSpec(FunctionSpec base, Element maintElement, double width, double length) {

		var utility = parseNamedPositions(maintElement, UTILITY, UTILITY, width, length);
		var rover = parseNamedPositions(maintElement, ROVER, ROVER, width, length);
		var flyer = parseNamedPositions(maintElement, FLYER, FLYER, width, length);
		
		return new VehicleMaintenanceSpec(base, rover, utility, flyer);
	}

	/**
	 * Parses the specific Resource processing process-engine nodes and create a list of ResourceProcessingEngine
	 * 
	 * @param resourceProcessingElement
	 * @return 
	 */
	private ResourceProcessingSpec createResourceProcessingSpec(FunctionSpec base, Element resourceProcessingElement,
													ResourceProcessConfig resProcConfig) {
		List<ResourceProcessEngine> resourceProcesses = new ArrayList<>();

		List<Element> resourceProcessNodes = resourceProcessingElement.getChildren(PROCESS_ENGINE);

		for (Element processElement : resourceProcessNodes) {
			String name = processElement.getAttributeValue(NAME);
			int modules = ConfigHelper.getOptionalAttributeInt(processElement, NUMBER_MODULES, 1);
			resourceProcesses.add(new ResourceProcessEngine(resProcConfig.getProcessSpec(name), modules));
		}

		return new ResourceProcessingSpec(base, resourceProcesses);
	}

	/**
	 * Parses a specific research details.
	 * 
	 * @param newSpec
	 * @param researchElement
	 */
	private ResearchSpec createResearchSpec(FunctionSpec base, Element researchElement) {
		List<ScienceType> result = new ArrayList<>();
		List<Element> researchSpecialities = researchElement.getChildren(RESEARCH_SPECIALTY);
		for (Element researchSpecialityElement : researchSpecialities) {
			String value = researchSpecialityElement.getAttributeValue(NAME);
			result.add(ScienceType.valueOf(ConfigHelper.convertToEnumName(value)));
		}
		
		return new ResearchSpec(base, result);
	}

	/**
	 * Parses a sources element.
	 * 
	 * @param list
	 * @param unitName
	 * @return
	 */
	private GenerationSpec createSourcesSpec(FunctionSpec base, Element element, String sourceName, String unitName) {
		List<SourceSpec> sourceList = new ArrayList<>();
		for (Element sourceElement : element.getChildren(sourceName)) {
			Properties attrs = new Properties();
			String type = sourceElement.getAttributeValue(TYPE);
			double unitCapacity = ConfigHelper.getOptionalAttributeDouble(sourceElement, unitName, 0);
			int numModules = ConfigHelper.getOptionalAttributeInt(sourceElement, MODULES, 1);
			double stirlingConversion =  ConfigHelper.getOptionalAttributeDouble(sourceElement, CONVERSION, 100D);
			double percentLoadCapacity = ConfigHelper.getOptionalAttributeDouble(sourceElement, PERCENT_LOADING, 100D);
			
			// Add optional attributes.
			for(Attribute attr : sourceElement.getAttributes()) {
				String attrName = attr.getName();
				if (!DEFAULT_SOURCE_ATTR.contains(attrName) && !attrName.equals(unitName)) {
					attrs.put(attr.getName(), attr.getValue());
				}
			}
			sourceList.add(new SourceSpec(type, attrs, numModules, 
					unitCapacity, stirlingConversion, percentLoadCapacity));
		}
		return new GenerationSpec(base, sourceList);
	}

	/**
	 * Parses the specific Storage properties.
	 * 
	 * @param newSpec
	 * @param storageElement
	 */
	private void parseStorage(BuildingSpec newSpec, Element storageElement) {
		List<Element> resourceStorageNodes = storageElement.getChildren(RESOURCE_CAPACITY);
		var storageMap = parseResourceList("Storage capacity in building " + newSpec.getName(),
										resourceStorageNodes);
		
		List<Element> resourceInitialNodes = storageElement.getChildren(RESOURCE_INITIAL);
		var initialMap = parseResourceList("Initial storage in building " + newSpec.getName(),
										resourceInitialNodes);

		newSpec.setStorage(storageMap, initialMap);
	}

	private static Map<Integer, Double> parseResourceList(String context, List<Element> resourceList) {
		return ConfigHelper.parseDoubleList(context, resourceList, 
							            TYPE, k -> ResourceUtil.findAmountResource(k).getID(),
							            AMOUNT);
	}

	/**
	 * Parses an set of named position for a building's function. These have a xloc & yloc structure.
	 *
	 * @param functionElement Element holding locations
	 * @param childrenName Name of the position elements
	 * @param positionName Name of the actual position element
	 * @param namePrefix The default name prefix to assign to the spots
	 * @param buildingWidth
	 * @param buildingLength
	 * @return set of activity spots as Point2D objects.
	 */
	private Set<NamedPosition> parseNamedPositions(Element functionElement, String childrenName,
												   String namePrefix,
										 		   double buildingWidth, double buildingLength) {
		Set<NamedPosition> result = new HashSet<>();

		// Maximum coord is half the width or length
		double maxX = buildingWidth/2D;
		double maxY = buildingLength/2D;
		boolean hasMax = (maxX > 0 && maxY > 0);

		Element activityElement = functionElement.getChild(childrenName);
		if (activityElement != null) {
			int i = 1;
			for(Element activitySpot : activityElement.getChildren()) {
				var point = ConfigHelper.parseRelativePosition(activitySpot);

				// Check location is within the building. Check as long as the maximum
				// is defined
				if (hasMax && !point.isWithin(maxX, maxY)) {
					// Roughly walk back over the XPath
					StringBuilder name = new StringBuilder();
					do {
						name.append(functionElement.getName()).append(' ');
						functionElement = functionElement.getParentElement();
					} while (!functionElement.getName().equals(BUILDING));
					name.append("in building '").append(functionElement.getAttributeValue(TYPE)).append("'");

					throw new IllegalArgumentException("Locations '" + childrenName
							+ "' of " + name.toString()
							+ " are outside building");
				}

				// Identify name
				String name = activitySpot.getAttributeValue(NAME);
				if (name == null) {
					name = namePrefix + " " + i;
					i++;
				}
				result.add(new NamedPosition(name, point));
			}
		}
		return result;
	}
	
	/**
	 * Finds a building spec according to the name.
	 * 
	 * @param buildingType
	 * @return
	 */
	public BuildingSpec getBuildingSpec(String buildingType) {
		BuildingSpec result = buildSpecMap.get(generateSpecKey(buildingType));
		if (result == null) {
			throw new IllegalArgumentException("Building Type not known: " + buildingType);
		}
		return result;
	}

	/**
	 * Checks if building has a certain function capability.
	 *
	 * @param buildingType the type of the building.
	 * @param function Type of service.
	 * @return true if function supported.
	 * @throws Exception if building type cannot be found.
	 */
	public boolean hasFunction(String buildingType, FunctionType function) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(function);
	}

	/**
	 * Gets a list of the building's resource capacities.
	 *
	 * @param buildingType the type of the building.
	 * @return list of storage capacities
	 * @thrList<ResourceProcessEngine>ing type cannot be found or XML parsing error.
	 */
	public Map<Integer, Double> getStorageCapacities(String buildingType) {
		return getBuildingSpec(buildingType).getStorage();
	}

	/**
	 * Gets a map of the initial resources stored in this building.
	 *
	 * @param buildingType the type of the building
	 * @return map of initial resources
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public Map<Integer, Double> getInitialResources(String buildingType) {
		return getBuildingSpec(buildingType).getInitialResources();
	}
	
	private static final String generateSpecKey(String buildingType) {
		return buildingType.toLowerCase().replace(" ", "-");
	}

	/**
	 * Gets the Function spec from a Building Type.
	 * 
	 * @param type Building type
	 * @param functionType Type of function
	 * @return
	 */
	public FunctionSpec getFunctionSpec(String type, FunctionType functionType) {
		return getBuildingSpec(type).getFunctionSpec(functionType);
	}

	/**
	 * Gets the FunctionTypes that have ActivitySpots assigned.
	 * 
	 * @return
	 */
    public Set<FunctionType> getActivitySpotFunctions() {
        return activityFunctions;
    }
    
    /**
     * Gets a map of building scopes.
     * 
     * @return
     */
    public Map<String, Set<String>> getBuildingScopes() {
    	return buildingScopes;
    }
}

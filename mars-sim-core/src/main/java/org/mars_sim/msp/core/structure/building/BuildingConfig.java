/*
 * Mars Simulation Project
 * BuildingConfig.java
 * @date 2022-07-07
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building;

import java.io.Serializable;
import java.util.ArrayList;
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
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.configuration.ConfigHelper;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.building.BuildingSpec.FunctionSpec;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * Provides configuration information about settlement buildings. Uses a DOM
 * document to get the information.
 */
public class BuildingConfig implements Serializable {


	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Element and attribute names
	private static final String DESCRIPTION = "description";
	private static final String BUILDING = "building";
	private static final String NAME = "name";
	private static final String BUILDING_TYPE = "type";
	private static final String WIDTH = "width";
	private static final String LENGTH = "length";
	private static final String THICKNESS = "thickness";
	private static final String BASE_LEVEL = "base-level";
	private static final String BASE_MASS = "base-mass";

	private static final String WEAR_LIFETIME = "wear-lifetime";
	private static final String MAINTENANCE_TIME = "maintenance-time";
	private static final String ROOM_TEMPERATURE = "room-temperature";

	private static final String FUNCTIONS = "functions";
	private static final String CAPACITY = "capacity";

	private static final String RESEARCH = "research";
	private static final String TECH_LEVEL = "tech-level";
	private static final String RESEARCH_SPECIALTY = "research-specialty";

	private static final String RESOURCE_PROCESSING = "resource-processing";

	private static final String NUMBER_MODULES = "number-modules";
	private static final String POWER_REQUIRED = "power-required";
	private static final String BASE_POWER = "base-power";
	private static final String BASE_POWER_DOWN_POWER = "base-power-down-power";
	private static final String POWER_DOWN_LEVEL = "power-down-level";

	private static final String CONCURRENT_PROCESSES = "concurrent-processes";
	private static final String DEFAULT = "default";
	
	private static final String PROCESS = "process";
	private static final String INPUT = "input";
	private static final String OUTPUT = "output";
	private static final String TOGGLE_PERIODICITY = "toggle-periodicity";
	private static final String TOGGLE_DURATION = "toggle-duration";
	private static final String RATE = "rate";
	private static final String AMBIENT = "ambient";
	private static final String STORAGE = "storage";
	private static final String STOCK_CAPACITY = "stock-capacity";
	private static final String RESOURCE_STORAGE = "resource-storage";
	private static final String RESOURCE_INITIAL = "resource-initial";
	private static final String RESOURCE = "resource";
	private static final String AMOUNT = "amount";
	private static final String TYPE = "type";
	private static final String MEDICAL_CARE = "medical-care";
	private static final String BEDS = "beds";
	private static final String CROPS = "crops";
	private static final String POWER_GROWING_CROP = "power-growing-crop";
	private static final String POWER_SUSTAINING_CROP = "power-sustaining-crop";
	private static final String GROWING_AREA = "growing-area";
	private static final String GROUND_VEHICLE_MAINTENANCE = "ground-vehicle-maintenance";
	private static final String PARKING_LOCATION = "parking-location";

	private static final String WASTE_PROCESSING = "waste-processing";

	private static final String POPULATION_SUPPORT = "population-support";
	private static final String ACTIVITY = "activity";
	private static final String ACTIVITY_SPOT = "activity-spot";
	private static final String BED_LOCATION = "bed-location";

	private static final String HEAT_REQUIRED = "heat-required";
	private static final String HEAT_SOURCE = "heat-source";
	private static final String THERMAL_GENERATION = "thermal-generation";

	// Power source types
	private static final String POWER_GENERATION = "power-generation";
	private static final String POWER_SOURCE = "power-source";
	private static final String POWER = "power";

	// Computation
	private static final String COMPUTATION = "computation";
	private static final String COMPUTING_UNIT = "computing-unit";
	private static final String POWER_DEMAND = "power-demand";
	private static final String COOLING_DEMAND = "cooling-demand";

	private static final String POSITION = "-position";

	private transient Map<String, BuildingSpec> buildSpecMap = new HashMap<>();

	/**
	 * Constructor.
	 *
	 * @param buildingDoc DOM document with building configuration
	 */
	public BuildingConfig(Document buildingDoc) {

		List<Element> buildingNodes = buildingDoc.getRootElement().getChildren(BUILDING);
		for (Element buildingElement : buildingNodes) {
			String buildingType = buildingElement.getAttributeValue(BUILDING_TYPE);
			String key = generateSpecKey(buildingType);
			buildSpecMap.put(key, parseBuilding(buildingType, buildingElement));
		}
	}

	/**
	 * Gets a set of all building types.
	 *
	 * @return set of building types.
	 */
	public Set<String> getBuildingTypes() {
		return  buildSpecMap.values().stream().map(BuildingSpec::getBuildingType).collect(Collectors.toSet());
	}

	/**
	 * Parses a building spec node.
	 *
	 * @param buildingTypeName
	 * @param buildingElement
	 * @return
	 */
	private BuildingSpec parseBuilding(String buildingTypeName, Element buildingElement) {
		Element descElement = buildingElement.getChild(DESCRIPTION);
		String desc = descElement.getValue().trim();
		desc = desc.replaceAll("\\t+", "").replaceAll("\\s+", " ").replace("   ", " ").replace("  ", " ");

		double width = Double.parseDouble(buildingElement.getAttributeValue(WIDTH));
		double length = Double.parseDouble(buildingElement.getAttributeValue(LENGTH));
		int baseLevel = Integer.parseInt(buildingElement.getAttributeValue(BASE_LEVEL));
		double roomTemp = Double.parseDouble(buildingElement.getAttributeValue(ROOM_TEMPERATURE));
		int maintenanceTime = Integer.parseInt(buildingElement.getAttributeValue(MAINTENANCE_TIME));
		int wearLifeTime = Integer.parseInt(buildingElement.getAttributeValue(WEAR_LIFETIME));

		Element powerElement = buildingElement.getChild(POWER_REQUIRED);
		double basePowerRequirement = Double.parseDouble(powerElement.getAttributeValue(BASE_POWER));
		double basePowerDownPowerRequirement = Double.parseDouble(powerElement.getAttributeValue(BASE_POWER_DOWN_POWER));

		// Get functions
		Map<FunctionType, BuildingSpec.FunctionSpec> supportedFunctions = new EnumMap<>(FunctionType.class);
		Element funcElement = buildingElement.getChild(FUNCTIONS);
		for (Element element : funcElement.getChildren()) {
			String name = element.getName().toUpperCase().trim().replace("-", "_");
			FunctionType function = FunctionType.valueOf(name.toUpperCase());

			// Has any Activity spots ?
			List<LocalPosition> spots = parsePositions(element, ACTIVITY, ACTIVITY_SPOT,
													width, length);

			// Get attributes as basic properties
			Map<String,Object> props = new HashMap<>();
			for(Attribute attr : element.getAttributes()) {
				props.put(attr.getName(), attr.getValue());
			}

			// Any complex properties
			for (Element complexProperty : element.getChildren()) {
				if (complexProperty.getName().endsWith(POSITION)) {
					LocalPosition pos = ConfigHelper.parseLocalPosition(complexProperty);
					props.put(complexProperty.getName(), pos);
				}
			}

			
			BuildingSpec.FunctionSpec fspec = new BuildingSpec.FunctionSpec(props, spots);

			supportedFunctions.put(function, fspec);
		}

		String categoryString = buildingElement.getAttributeValue("category");
		BuildingCategory category = null;
		if (categoryString != null) {
			category = BuildingCategory.valueOf(categoryString.toUpperCase());
		}
		else {
			// Derive category from Functions
			category = deriveCategory(supportedFunctions.keySet());

		}

		BuildingSpec newSpec = new BuildingSpec(buildingTypeName, desc, category, width, length, baseLevel,
			 	roomTemp, maintenanceTime, wearLifeTime,
			 	basePowerRequirement, basePowerDownPowerRequirement,
			 	supportedFunctions);

		String thickness = buildingElement.getAttributeValue(THICKNESS);
		if (thickness != null) {
			newSpec.setWallThickness(Double.parseDouble(thickness));
		}

		String baseMass = buildingElement.getAttributeValue(BASE_MASS);
		if (baseMass != null) {
			newSpec.setBaseMass(Double.parseDouble(baseMass));
		}

		// Get Storage
		Element functionsElement = buildingElement.getChild(FUNCTIONS);
		Element storageElement = functionsElement.getChild(STORAGE);
		if (storageElement != null) {
			parseStorage(newSpec, storageElement);
		}

		Element thermalGenerationElement = functionsElement.getChild(THERMAL_GENERATION);
		if (thermalGenerationElement != null) {
			List<SourceSpec> heatSourceList = parseSources(thermalGenerationElement.getChildren(HEAT_SOURCE),
														   CAPACITY);
			newSpec.setHeatSource(heatSourceList);
		}

		Element powerGenerationElement = functionsElement.getChild(POWER_GENERATION);
		if (powerGenerationElement != null) {
			List<SourceSpec> powerSourceList = parseSources(powerGenerationElement.getChildren(POWER_SOURCE),
															POWER);
			newSpec.setPowerSource(powerSourceList);
		}

		// Not needed. Done in the FunctionSpecs
		Element computationElement = functionsElement.getChild(COMPUTATION);
		if (computationElement != null) {
			parseComputation(newSpec, computationElement);
		}

		Element researchElement = functionsElement.getChild(RESEARCH);
		if (researchElement != null) {
			parseResearch(newSpec, researchElement);
		}

		Element resourceProcessingElement = functionsElement.getChild(RESOURCE_PROCESSING);
		if (resourceProcessingElement != null) {
			parseResourceProcessing(newSpec, resourceProcessingElement);
		}

		Element wasteProcessingElement = functionsElement.getChild(WASTE_PROCESSING);
		if (wasteProcessingElement != null) {
			parseWasteProcessing(newSpec, wasteProcessingElement);
		}

		Element vehicleElement = functionsElement.getChild(GROUND_VEHICLE_MAINTENANCE);
		if (vehicleElement != null) {
			List<LocalPosition> parking = parsePositions(vehicleElement, "parking", PARKING_LOCATION,
												   width, length);
			newSpec.setParking(parking);
		}

		Element medicalElement = functionsElement.getChild(MEDICAL_CARE);
		if (medicalElement != null) {
			List<LocalPosition> beds = parsePositions(medicalElement, BEDS, BED_LOCATION,
												width, length);
			newSpec.setBeds(beds);
		}
		return newSpec;
	}

	/**
	 * Derives the category from the types of Functions.
	 * 
	 * @param functions
	 * @return
	 */
	private BuildingCategory deriveCategory(Set<FunctionType> functions) {

		// Get a set of categories
		Set<BuildingCategory> cats = new HashSet<>();
		for (FunctionType fType : functions) {
			switch(fType) {
				case EARTH_RETURN:
					cats.add(BuildingCategory.ERV);
					break;

				case EVA:
					cats.add(BuildingCategory.EVA_AIRLOCK);
					break;

				case FARMING:
				case FISHERY:
					cats.add(BuildingCategory.FARMING);
					break;

				case BUILDING_CONNECTION:
					cats.add(BuildingCategory.HALLWAY);
					break;

				case ADMINISTRATION:
				case COMMUNICATION:
				case COMPUTATION:
				case MANAGEMENT:
					cats.add(BuildingCategory.HABITAT);
					break;

				case ASTRONOMICAL_OBSERVATION:
				case FIELD_STUDY:
				case RESEARCH:
					cats.add(BuildingCategory.LABORATORY);
					break;

				case COOKING:
				case DINING:
				case EXERCISE:
				case FOOD_PRODUCTION:
				case LIVING_ACCOMMODATIONS:
				case RECREATION:
					cats.add(BuildingCategory.LIVING);
					break;

				case STORAGE:
					cats.add(BuildingCategory.STORAGE);
					break;
					
				case MEDICAL_CARE:
					cats.add(BuildingCategory.MEDICAL);
					break;

				case POWER_GENERATION:
				case POWER_STORAGE:
				case THERMAL_GENERATION:
					cats.add(BuildingCategory.POWER);
					break; 

				case RESOURCE_PROCESSING:
				case WASTE_PROCESSING:
					cats.add(BuildingCategory.PROCESSING);
					break;

				case GROUND_VEHICLE_MAINTENANCE:
					cats.add(BuildingCategory.VEHICLE);
					break;

				case MANUFACTURE:
					cats.add(BuildingCategory.WORKSHOP);
					break;

				default:
					// Not important
			}
		}

		BuildingCategory category = BuildingCategory.HALLWAY;
		if (!cats.isEmpty()) {
			// Find the category with the lowest Ordinal as that is the best to represent
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
	 * Parses the specific Resource processing function details.
	 * 
	 * @param newSpec
	 * @param resourceProcessingElement
	 */
	private void parseResourceProcessing(BuildingSpec newSpec, Element resourceProcessingElement) {
		List<ResourceProcessSpec> resourceProcesses = new ArrayList<>();

		List<Element> resourceProcessNodes = resourceProcessingElement.getChildren(PROCESS);

		for (Element processElement : resourceProcessNodes) {

			String defaultString = processElement.getAttributeValue(DEFAULT);
			boolean defaultOn = !defaultString.equals("off");

            double powerRequired = Double.parseDouble(processElement.getAttributeValue(POWER_REQUIRED));

            int modules = 1;

            String mods = processElement.getAttributeValue(NUMBER_MODULES);

            if (mods != null)
            	modules = Integer.parseInt(mods);

			ResourceProcessSpec process = new ResourceProcessSpec(processElement.getAttributeValue(NAME), modules, powerRequired,
					defaultOn);

			// Check optional attrs
			String duration = processElement.getAttributeValue(TOGGLE_DURATION);
			if (duration != null) {
				process.setToggleDuration(Integer.parseInt(duration));
			}
			String periodicity = processElement.getAttributeValue(TOGGLE_PERIODICITY);
			if (periodicity != null) {
				process.setTogglePeriodicity(Integer.parseInt(periodicity));
			}

			// Get input resources.
			List<Element> inputNodes = processElement.getChildren(INPUT);
			for (Element inputElement : inputNodes) {
				String resourceName = inputElement.getAttributeValue(RESOURCE).toLowerCase();
				// AmountResource resource = ResourceUtil.findAmountResource(resourceName);
				Integer id = ResourceUtil.findIDbyAmountResourceName(resourceName);
				double rate = Double.parseDouble(inputElement.getAttributeValue(RATE)) / 1000D;
				boolean ambient = Boolean.valueOf(inputElement.getAttributeValue(AMBIENT));
				process.addMaxInputResourceRate(id, rate, ambient);
			}

			// Get output resources.
			List<Element> outputNodes = processElement.getChildren(OUTPUT);
			for (Element outputElement : outputNodes) {
				String resourceName = outputElement.getAttributeValue(RESOURCE).toLowerCase();
				// AmountResource resource = ResourceUtil.findAmountResource(resourceName);
				Integer id = ResourceUtil.findIDbyAmountResourceName(resourceName);
				double rate = Double.parseDouble(outputElement.getAttributeValue(RATE)) / 1000D;
				boolean ambient = Boolean.valueOf(outputElement.getAttributeValue(AMBIENT));
				process.addMaxOutputResourceRate(id, rate, ambient);
			}

			resourceProcesses.add(process);
		}
		newSpec.setResourceProcess(resourceProcesses);
	}

	/**
	 * Parses the specific Waste processing function details.
	 * 
	 * @param newSpec
	 * @param wasteProcessingElement
	 */
	private void parseWasteProcessing(BuildingSpec newSpec, Element wasteProcessingElement) {
		List<WasteProcessSpec> processes = new ArrayList<>();

		List<Element> processNodes = wasteProcessingElement.getChildren(PROCESS);

		for (Element processElement : processNodes) {

			String defaultString = processElement.getAttributeValue(DEFAULT);
			boolean defaultOn = !defaultString.equals("off");

            double powerRequired = Double.parseDouble(processElement.getAttributeValue(POWER_REQUIRED));

            int modules = 1;

            String mods = processElement.getAttributeValue(NUMBER_MODULES);

            if (mods != null)
            	modules = Integer.parseInt(mods);

			WasteProcessSpec process = new WasteProcessSpec(processElement.getAttributeValue(NAME), modules, powerRequired,
					defaultOn);

			// Check optional attrs
			String duration = processElement.getAttributeValue(TOGGLE_DURATION);
			if (duration != null) {
				process.setToggleDuration(Integer.parseInt(duration));
			}
			String periodicity = processElement.getAttributeValue(TOGGLE_PERIODICITY);
			if (periodicity != null) {
				process.setTogglePeriodicity(Integer.parseInt(periodicity));
			}

			// Get input resources.
			List<Element> inputNodes = processElement.getChildren(INPUT);
			for (Element inputElement : inputNodes) {
				String resourceName = inputElement.getAttributeValue(RESOURCE).toLowerCase();
				Integer id = ResourceUtil.findIDbyAmountResourceName(resourceName);
				double rate = Double.parseDouble(inputElement.getAttributeValue(RATE)) / 1000D;
				boolean ambient = Boolean.parseBoolean(inputElement.getAttributeValue(AMBIENT));
				process.addMaxInputRate(id, rate, ambient);
			}

			// Get output resources.
			List<Element> outputNodes = processElement.getChildren(OUTPUT);
			for (Element outputElement : outputNodes) {
				String resourceName = outputElement.getAttributeValue(RESOURCE).toLowerCase();
				Integer id = ResourceUtil.findIDbyAmountResourceName(resourceName);
				double rate = Double.parseDouble(outputElement.getAttributeValue(RATE)) / 1000D;
				boolean ambient = Boolean.parseBoolean(outputElement.getAttributeValue(AMBIENT));
				process.addMaxOutputRate(id, rate, ambient);
			}

			processes.add(process);
		}
		newSpec.setWasteProcess(processes);
	}
	
	/**
	 * Parses a specific computation details.
	 * 
	 * @param newSpec
	 * @param computationElement
	 */
	private void parseComputation(BuildingSpec newSpec, Element computationElement) {
		double computingPower = Double.parseDouble(computationElement.getAttributeValue(COMPUTING_UNIT));
		double powerDemand = Double.parseDouble(computationElement.getAttributeValue(POWER_DEMAND));
		double coolingDemand = Double.parseDouble(computationElement.getAttributeValue(COOLING_DEMAND));

		newSpec.setComputation(computingPower, powerDemand, coolingDemand);
	}

	/**
	 * Parses a specific research details.
	 * 
	 * @param newSpec
	 * @param researchElement
	 */
	private void parseResearch(BuildingSpec newSpec, Element researchElement) {
		List<ScienceType> result = new ArrayList<>();
		List<Element> researchSpecialities = researchElement.getChildren(RESEARCH_SPECIALTY);
		for (Element researchSpecialityElement : researchSpecialities) {
			String value = researchSpecialityElement.getAttributeValue(NAME);
			// The name of research-specialty in buildings.xml must conform to enum values of {@link
			// ScienceType}
			result.add(ScienceType.valueOf(ScienceType.class, value.toUpperCase().replace(" ", "_")));
		}
		newSpec.setScienceType(result);
	}

	/**
	 * Parses a sources element.
	 * 
	 * @param list
	 * @param capacityName
	 * @return
	 */
	private List<SourceSpec> parseSources(List<Element> list, String capacityName) {
		List<SourceSpec> sourceList = new ArrayList<>();
		for (Element sourceElement : list) {
			Properties attrs = new  Properties();
			String type = null;
			double capacity = 0D;
			for(Attribute attr : sourceElement.getAttributes()) {
				if (attr.getName().equals(TYPE)) {
					type = attr.getValue();
				}
				else if (attr.getName().equals(capacityName)) {
					capacity = Double.parseDouble(attr.getValue());
				}
				else {
					attrs.put(attr.getName(), attr.getValue());
				}
			}
			sourceList.add(new SourceSpec(type, capacity, attrs));
		}
		return sourceList;
	}

	/**
	 * Parses the specific Storage properties.
	 * 
	 * @param newSpec
	 * @param storageElement
	 */
	private void parseStorage(BuildingSpec newSpec, Element storageElement) {
		Map<Integer, Double> storageMap = new HashMap<>();
		Map<Integer, Double> initialMap = new HashMap<>();
		double stockCapacity = Double.parseDouble(storageElement.getAttributeValue(STOCK_CAPACITY));

		List<Element> resourceStorageNodes = storageElement.getChildren(RESOURCE_STORAGE);
		for (Element resourceStorageElement : resourceStorageNodes) {
			String resourceName = resourceStorageElement.getAttributeValue(RESOURCE).toLowerCase();
			Integer resource = ResourceUtil.findIDbyAmountResourceName(resourceName);
			Double capacity = Double.valueOf(resourceStorageElement.getAttributeValue(CAPACITY));
			storageMap.put(resource, capacity);
		}

		List<Element> resourceInitialNodes = storageElement.getChildren(RESOURCE_INITIAL);
		for (Element resourceInitialElement : resourceInitialNodes) {
			String resourceName = resourceInitialElement.getAttributeValue(RESOURCE).toLowerCase();
			Integer resource = ResourceUtil.findIDbyAmountResourceName(resourceName);
			Double amount = Double.valueOf(resourceInitialElement.getAttributeValue(AMOUNT));
			initialMap.put(resource, amount);
		}

		newSpec.setStorage(stockCapacity, storageMap, initialMap);
	}

	/**
	 * Parses an list of position for a building's function. These have a <xloc> & <yloc> structure.
	 *
	 * @param functionElement Element holding locations
	 * @param locations Name of the location elements
	 * @param pointName Nmae of the point item
	 * @return list of activity spots as Point2D objects.
	 */
	private List<LocalPosition> parsePositions(Element functionElement, String locations, String pointName,
										 double buildingWidth, double buildingLength) {
		List<LocalPosition> result = new ArrayList<>();

		// Maximum coord is half the width or length
		double maxX = buildingWidth/2D;
		double maxY = buildingLength/2D;
		boolean hasMax = (maxX > 0 && maxY > 0);
		
		Element activityElement = functionElement.getChild(locations);
		if (activityElement != null) {
			for(Element activitySpot : activityElement.getChildren(pointName)) {
				LocalPosition point = ConfigHelper.parseLocalPosition(activitySpot);

				// Check location is within the building. Check as long as the maximum
				// is defined
				if (hasMax && !point.isWithin(maxX, maxY)) {
					// Roughly walk back over the XPath
					StringBuilder name = new StringBuilder();
					do {
						name.append(functionElement.getName()).append(' ');
						functionElement = functionElement.getParentElement();
					} while (!functionElement.getName().equals(BUILDING));
					name.append(" in building '").append(functionElement.getAttributeValue(TYPE)).append("'");

					throw new IllegalArgumentException("Locations '" + locations
							+ "' of " + name.toString()
							+ " are outside building");
				}

				result.add(point);
			}
		}
		return result;
	}

	/**
	 * Finds a Building spec according to the name.
	 * 
	 * @param buildingType
	 * @return
	 */
	public BuildingSpec getBuildingSpec(String buildingType) {
		BuildingSpec result = buildSpecMap.get(generateSpecKey(buildingType));
		if (result == null) {
			throw new IllegalArgumentException("Building Type not known :" + buildingType);
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
	 * Get the activity spots for a function.
	 * 
	 * @param buildingType
	 * @param function
	 * @return
	 */
	public List<LocalPosition> getActivitySpots(String buildingType, FunctionType function) {
		FunctionSpec fs = getBuildingSpec(buildingType).getFunctionSpec(function);
		List<LocalPosition> result = null;
		if (fs != null) {
			result = fs.getActivitySpots();
		}

		return result;
	}

	/**
	 * Gets a property for a Function from a building type.
	 * 
	 * @param buildingType Building type name
	 * @param function Function type
	 * @param name Property name
	 * @return
	 */
	private int getFunctionIntProperty(String buildingType, FunctionType function, String name) {
		return Integer.parseInt((String) getBuildingSpec(buildingType).getFunctionSpec(function).getProperty(name));
	}

	/**
	 * Gets a property for a Function from a building type.
	 * 
	 * @param buildingType Building type name
	 * @param function Function type
	 * @param name Property name
	 * @return
	 */
	public double getFunctionDoubleProperty(String buildingType, FunctionType function, String name) {
		return Double.parseDouble((String) getBuildingSpec(buildingType).getFunctionSpec(function).getProperty(name));
	}

	/**
	 * Gets the capacity for a Function. This capacity usually reference to the number of Unit
	 * supported by a function but it could be used differently.
	 *
	 * @param buildingType the type of the building
	 * @param function Type of function
	 * @return number of people
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getFunctionCapacity(String buildingType, FunctionType function) {
		return getFunctionIntProperty(buildingType, function, CAPACITY);
	}

	/**
	 * Gets the capacity for a Function. This capacity usually reference an amount.
	 *
	 * @param buildingType the type of the building
	 * @param function Type of function
	 * @return number of people
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public double getFunctionCapacityDouble(String buildingType, FunctionType function) {
		return getFunctionDoubleProperty(buildingType, function, CAPACITY);
	}

	/**
	 * Gets the tech level for a Function.
	 *
	 * @param buildingType the type of the building
	 * @param function Type of function
	 * @return number of people
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getFunctionTechLevel(String buildingType, FunctionType function) {
		return getFunctionIntProperty(buildingType, function, TECH_LEVEL);
	}

	/**
	 * Gets the building width.
	 *
	 * @param buildingType the type of the building.
	 * @return building width (meters).
	 * @throws Exception if building type cannot be found.
	 * @deprecated Use {@link #getBuildingSpec(String)}
	 */
	public double getWidth(String buildingType) {
		return getBuildingSpec(buildingType).getWidth();
	}

	/**
	 * Gets the building length.
	 *
	 * @param buildingType the type of the building.
	 * @return building length (meters).
	 * @throws Exception if building type cannot be found or XML parsing error.
	 * @deprecated Use {@link #getBuildingSpec(String)}
	 */
	public double getLength(String buildingType) {
		return getBuildingSpec(buildingType).getLength();
	}

	/**
	 * Gets the base level of the building.
	 *
	 * @param buildingType the type of the building.
	 * @return -1 for in-ground, 0 for above-ground.
	 * @deprecated Use {@link #getBuildingSpec(String)}
	 */
	public int getBaseLevel(String buildingType) {
		return getBuildingSpec(buildingType).getBaseLevel();
	}

	/**
	 * Gets the power required for life support.
	 *
	 * @param buildingType the type of the building
	 * @return power required (kW)
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public double getLifeSupportPowerRequirement(String buildingType) {
		return getFunctionDoubleProperty(buildingType, FunctionType.LIFE_SUPPORT, POWER_REQUIRED);
	}

	/**
	 * Gets the heat required for life support.
	 *
	 * @param buildingType the type of the building
	 * @return heat required (J)
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public double getLifeSupportHeatRequirement(String buildingType) {
		return getFunctionDoubleProperty(buildingType, FunctionType.LIFE_SUPPORT, HEAT_REQUIRED);
	}

	/**
	 * @deprecated  For individual properties use {@link #getBuildingSpec(String)}
	 */
	public double getComputingUnit(String buildingType) {
		return getBuildingSpec(buildingType).getComputingUnit();
	}

	/**
	 * @deprecated For individual properties use {@link #getBuildingSpec(String)}
	 */
	public double getPowerDemand(String buildingType) {
		return getBuildingSpec(buildingType).getPowerDemand();
	}

	/**
	 * @deprecated For individual properties use {@link #getBuildingSpec(String)}
	 */
	public double getCoolingDemand(String buildingType) {
		return getBuildingSpec(buildingType).getCoolingDemand();
	}

	/**
	 * Gets a list of research specialties for the building's lab.
	 *
	 * @param buildingType the type of the building
	 * @return list of research specialties as {@link ScienceType}.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public List<ScienceType> getResearchSpecialties(String buildingType) {
		return getBuildingSpec(buildingType).getScienceType();

	}

	/**
	 * Gets the relative position of airlock position.
	 *
	 * @param buildingType the type of the building.
	 * @param positionName The type of positions of the airlock
	 * @return relative X location.
	 */
	public LocalPosition getAirlockPosition(String buildingType, String positionName) {
		return (LocalPosition) getBuildingSpec(buildingType).getFunctionSpec(FunctionType.EVA).getProperty(positionName);
	}

	/**
	 * Gets the level of resource processing when the building is in power down
	 * mode.
	 *
	 * @param buildingType the type of the building
	 * @return power down level
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public double getResourceProcessingPowerDown(String buildingType) {
		return getFunctionDoubleProperty(buildingType, FunctionType.RESOURCE_PROCESSING, POWER_DOWN_LEVEL);
	}

	/**
	 * Gets the building's resource processes.
	 *
	 * @param buildingType the type of the building.
	 * @return a list of resource processes.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public List<ResourceProcessSpec> getResourceProcesses(String buildingType) {
		return getBuildingSpec(buildingType).getResourceProcess();
	}

	/**
	 * Gets the level of waste processing when the building is in power down
	 * mode.
	 *
	 * @param buildingType the type of the building
	 * @return power down level
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public double getWasteProcessingPowerDown(String buildingType) {
		return getFunctionDoubleProperty(buildingType, FunctionType.WASTE_PROCESSING, POWER_DOWN_LEVEL);
	}

	/**
	 * Gets the building's waste processes.
	 *
	 * @param buildingType the type of the building.
	 * @return a list of waste processes.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public List<WasteProcessSpec> getWasteProcesses(String buildingType) {
		return getBuildingSpec(buildingType).getWasteProcess();
	}

	
	/**
	 * Gets a list of the building's resource capacities.
	 *
	 * @param buildingType the type of the building.
	 * @return list of storage capacities
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public Map<Integer, Double> getStorageCapacities(String buildingType) {
		return getBuildingSpec(buildingType).getStorage();
	}

	public double getStockCapacity(String buildingType) {
		return getBuildingSpec(buildingType).getStockCapacity();
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

	/**
	 * Gets a list of the building's heat sources.
	 *
	 * @param buildingType the type of the building.
	 * @return list of heat sources
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public List<SourceSpec> getHeatSources(String buildingType) {
		return getBuildingSpec(buildingType).getHeatSource();
	}

	/**
	 * Gets a list of the building's power sources.
	 *
	 * @param buildingType the type of the building.
	 * @return list of power sources
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public List<SourceSpec> getPowerSources(String buildingType) {
		return getBuildingSpec(buildingType).getPowerSource();

	}

	/**
	 * Gets the number of crops in the building.
	 *
	 * @param buildingType the type of the building.
	 * @return number of crops
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getCropNum(String buildingType) {
		return getFunctionIntProperty(buildingType, FunctionType.FARMING, CROPS);
	}

	/**
	 * Gets the power required to grow a crop.
	 *
	 * @param buildingType the type of the building.
	 * @return power (kW)
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public double getPowerForGrowingCrop(String buildingType) {
		return getFunctionDoubleProperty(buildingType, FunctionType.FARMING, POWER_GROWING_CROP);
	}

	/**
	 * Gets the power required to sustain a crop.
	 *
	 * @param buildingType the type of the building.
	 * @return power (kW)
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public double getPowerForSustainingCrop(String buildingType) {
		return getFunctionDoubleProperty(buildingType, FunctionType.FARMING, POWER_SUSTAINING_CROP);
	}

	/**
	 * Gets the crop growing area in the building.
	 *
	 * @param buildingType the type of the building.
	 * @return crop growing area (square meters)
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public double getCropGrowingArea(String buildingType) {
		return getFunctionDoubleProperty(buildingType, FunctionType.FARMING, GROWING_AREA);
	}

	/**
	 * Gets the number of parking locations in the building.
	 *
	 * @param buildingType the type of the building.
	 * @return number of parking locations.
	 */
	public int getParkingLocationNumber(String buildingType) {
		return getBuildingSpec(buildingType).getParking().size();
	}

	/**
	 * Gets the relative location in the building of a parking location.
	 *
	 * @param buildingType the type of the building.
	 * @return Positions containing the relative X & Y position from the building
	 *         center.
	 */
	public List<LocalPosition> getParkingLocations(String buildingType) {
		return getBuildingSpec(buildingType).getParking();
	}

	/**
	 * Gets the concurrent process limit of the Food Production facility in the
	 * building.
	 *
	 * @param buildingType the type of the building.
	 * @return concurrent process limit.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getFoodProductionConcurrentProcesses(String buildingType) {
		return getFunctionIntProperty(buildingType, FunctionType.FOOD_PRODUCTION, CONCURRENT_PROCESSES);
	}

	/**
	 * Gets the power required by the astronomical observation function.
	 *
	 * @param buildingType the type of the building.
	 * @return power required (kW).
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public double getAstronomicalObservationPowerRequirement(String buildingType) {
		return getFunctionDoubleProperty(buildingType, FunctionType.ASTRONOMICAL_OBSERVATION, POWER_REQUIRED);
	}

	/**
	 * Gets the concurrent process limit of the manufacture facility in the
	 * building.
	 *
	 * @param buildingType the type of the building.
	 * @return concurrent process limit.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getManufactureConcurrentProcesses(String buildingType) {
		return getFunctionIntProperty(buildingType, FunctionType.MANUFACTURE, CONCURRENT_PROCESSES);
	}

	public int getFishTankSize(String buildingType) {
		return getFunctionIntProperty(buildingType, FunctionType.FISHERY, "volume");
	}

	private static final String generateSpecKey(String buildingType) {
		return buildingType.toLowerCase().replace(" ", "-");
	}

	/**
	 * Gets the number of people supported by an Administration function in a building.
	 * 
	 * @param buildingType
	 * @return
	 */
	public int getAdministrationPopulationSupport(String buildingType) {
		return getFunctionIntProperty(buildingType, FunctionType.ADMINISTRATION, POPULATION_SUPPORT);
	}
}

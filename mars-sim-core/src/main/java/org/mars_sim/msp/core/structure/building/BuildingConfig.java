/**
 * Mars Simulation Project
 * BuildingConfig.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * Provides configuration information about settlement buildings. Uses a DOM
 * document to get the information.
 */
public class BuildingConfig implements Serializable {


	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// private static final Logger logger =
	// Logger.getLogger(BuildingConfig.class.getName());

	// Element and attribute names
	private static final String DESCRIPTION = "description";
	private static final String BUILDING = "building";
	private static final String NAME = "name";
	private static final String BUILDING_TYPE = "type";
	private static final String WIDTH = "width";
	private static final String LENGTH = "length";
	private static final String THICKNESS = "thickness";
	private static final String BASE_LEVEL = "base-level";

	private static final String WEAR_LIFETIME = "wear-lifetime";
	private static final String MAINTENANCE_TIME = "maintenance-time";
	private static final String ROOM_TEMPERATURE = "room-temperature";

	private static final String FUNCTIONS = "functions";
	private static final String LIFE_SUPPORT = "life-support";
	private static final String CAPACITY = "capacity";
	private static final String LIVING_ACCOMMODATIONS = "living-accommodations";

	private static final String ROBOTIC_STATION = "robotic-station";
	private static final String SLOTS = "slots";

	private static final String WASTE_DISPOSAL = "waste-disposal";
	private static final String WASTE_SPECIALTY = "waste-specialty";

	private static final String RESEARCH = "research";
	private static final String TECH_LEVEL = "tech-level";
	private static final String RESEARCH_SPECIALTY = "research-specialty";
	private static final String COMMUNICATION = "communication";
	private static final String EVA = "EVA";
	private static final String AIRLOCK_CAPACITY = "airlock-capacity";
	private static final String INTERIOR_X_LOCATION = "interior-xloc";
	private static final String INTERIOR_Y_LOCATION = "interior-yloc";
	private static final String EXTERIOR_X_LOCATION = "exterior-xloc";
	private static final String EXTERIOR_Y_LOCATION = "exterior-yloc";
	private static final String RECREATION = "recreation";
	private static final String DINING = "dining";

	private static final String RESOURCE_PROCESSING = "resource-processing";

	private static final String POWER_REQUIRED = "power-required";
	private static final String BASE_POWER = "base-power";
	private static final String BASE_POWER_DOWN_POWER = "base-power-down-power";
	private static final String POWER_DOWN_LEVEL = "power-down-level";

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
	private static final String FARMING = "farming";
	private static final String CROPS = "crops";
	private static final String POWER_GROWING_CROP = "power-growing-crop";
	private static final String POWER_SUSTAINING_CROP = "power-sustaining-crop";
	private static final String GROWING_AREA = "growing-area";
	private static final String EXERCISE = "exercise";
	private static final String GROUND_VEHICLE_MAINTENANCE = "ground-vehicle-maintenance";
	private static final String PARKING_LOCATION = "parking-location";
	private static final String X_LOCATION = "xloc";
	private static final String Y_LOCATION = "yloc";
	private static final String VEHICLE_CAPACITY = "vehicle-capacity";
	private static final String COOKING = "cooking";
	private static final String DEFAULT = "default";
	private static final String MANUFACTURE = "manufacture";
	private static final String CONCURRENT_PROCESSES = "concurrent-processes";

	private static final String FOOD_PRODUCTION = "food-production";

	private static final String ASTRONOMICAL_OBSERVATION = "astronomical-observation";
	private static final String EARTH_RETURN = "earth-return";
	private static final String CREW_CAPACITY = "crew-capacity";
	private static final String MANAGEMENT = "management";
	private static final String POPULATION_SUPPORT = "population-support";
	private static final String ACTIVITY = "activity";
	private static final String ACTIVITY_SPOT = "activity-spot";
	private static final String BED = "bed";
	private static final String BED_LOCATION = "bed-location";
	
	private static final String ADMINISTRATION = "administration";

	private static final String HEAT_REQUIRED = "heat-required";
	private static final String HEAT_SOURCE = "heat-source";
	private static final String THERMAL_GENERATION = "thermal-generation";

	// Power source types
	private static final String POWER_GENERATION = "power-generation";
	private static final String POWER_SOURCE = "power-source";
	private static final String POWER_STORAGE = "power-storage";
	private static final String POWER = "power";


	private Element root;
	
	private Map<String, List<ScienceType>> wasteSpecialties;

	private Map<String, BuildingSpec> buildSpecMap = new HashMap<>();
	
	/**
	 * Constructor
	 * 
	 * @param buildingDoc DOM document with building configuration
	 */
	public BuildingConfig(Document buildingDoc) {

		root = buildingDoc.getRootElement();
		
		List<Element> buildingNodes = root.getChildren(BUILDING);
		for (Element buildingElement : buildingNodes) {
			String buildingType = buildingElement.getAttributeValue(BUILDING_TYPE);
			String key = generateSpecKey(buildingType);
			buildSpecMap.put(key, parseBuilding(buildingType, buildingElement));
		}

		if (wasteSpecialties == null) {
			wasteSpecialties = new ConcurrentHashMap<String, List<ScienceType>>();
		}
		
//		for (String type : getBuildingTypes()) {
//			if (!wasteSpecialties.containsKey(type))
//				wasteSpecialties.put(type, getWasteSpecialties(type));
//		}
	}
	
	/**
	 * Gets a set of all building types.
	 * 
	 * @return set of building types.
	 */
	public Set<String> getBuildingTypes() {
		return  buildSpecMap.values().stream().map(bs -> bs.getName()).collect(Collectors.toSet());
	}

	private static BuildingSpec parseBuilding(String buildingTypeName, Element buildingElement) {
		Element descElement = buildingElement.getChild(DESCRIPTION);
		String desc = descElement.getValue().trim();
		desc = desc.replaceAll("\\t+", "").replaceAll("\\s+", " ").replaceAll("   ", " ").replaceAll("  ", " ");
		
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
		Set<FunctionType> supportedFunctions = new HashSet<>();
		Element funcElement = buildingElement.getChild(FUNCTIONS);
		for (Element element : funcElement.getChildren()) {
			String name = element.getName().toUpperCase().trim().replaceAll("-", "_");
			FunctionType function = FunctionType.valueOf(name.toUpperCase());
			supportedFunctions.add(function);
		}
		BuildingSpec newSpec = new BuildingSpec(buildingTypeName, desc, width, length, baseLevel,
			 	roomTemp, maintenanceTime, wearLifeTime,
			 	basePowerRequirement, basePowerDownPowerRequirement,
			 	supportedFunctions);
		
		String thickness = buildingElement.getAttributeValue(THICKNESS);
		if (thickness != null) {
			newSpec.setWallThickness(Double.parseDouble(thickness));
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

		Element researchElement = functionsElement.getChild(RESEARCH);
		if (researchElement != null) {
			parseResearch(newSpec, researchElement);
		}
		
		Element resourceProcessingElement = functionsElement.getChild(RESOURCE_PROCESSING);
		if (resourceProcessingElement != null) {
			parseResourceProcessing(newSpec, resourceProcessingElement);
		}

		return newSpec;
	}

	private static void parseResourceProcessing(BuildingSpec newSpec, Element resourceProcessingElement) {
		List<ResourceProcessSpec> resourceProcesses = new ArrayList<>();

		List<Element> resourceProcessNodes = resourceProcessingElement.getChildren(PROCESS);

		for (Element processElement : resourceProcessNodes) {

			String defaultString = processElement.getAttributeValue(DEFAULT);
			boolean defaultOn = true;
			if (defaultString.equals("off"))
				defaultOn = false;

			double powerRequired = Double.parseDouble(processElement.getAttributeValue(POWER_REQUIRED));

			ResourceProcessSpec process = new ResourceProcessSpec(processElement.getAttributeValue(NAME), powerRequired,
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

	private static void parseResearch(BuildingSpec newSpec, Element researchElement) {
		List<ScienceType> result = new ArrayList<ScienceType>();
		List<Element> researchSpecialities = researchElement.getChildren(RESEARCH_SPECIALTY);		
		for (Element researchSpecialityElement : researchSpecialities) {
			String value = researchSpecialityElement.getAttributeValue(NAME);
			// take care that entries in buildings.xml conform to enum values of {@link
			// ScienceType}
			result.add(ScienceType.valueOf(ScienceType.class, value.toUpperCase().replace(" ", "_")));
		}
		newSpec.setScienceType(result);
	}

	private static List<SourceSpec> parseSources(List<Element> list, String capacityName) {
		List<SourceSpec> sourceList = new ArrayList<SourceSpec>();
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

	private static void parseStorage(BuildingSpec newSpec, Element storageElement) {
		Map<Integer, Double> storageMap = new HashMap<Integer, Double>();
		Map<Integer, Double> initialMap = new HashMap<Integer, Double>();
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
	 * Gets a building DOM element for a particular building type.
	 * 
	 * @param buildingType the building type
	 * @return building element
	 * @throws Exception if building type could not be found.
	 */
	private Element getBuildingElement(String buildingType) {
		Element result = null;

		// Element root = buildingDoc.getRootElement();
		List<Element> buildingNodes = root.getChildren(BUILDING);
		for (Element buildingElement : buildingNodes) {
			String buidingType = buildingElement.getAttributeValue(BUILDING_TYPE);
			if (buildingType.equalsIgnoreCase(buidingType)) {
				result = buildingElement;
				break;
			}
		}

		if (result == null)
			throw new IllegalStateException("Building type: " + buildingType + " could not be found in buildings.xml.");

		return result;
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
	 * Checks if the building has life support.
	 * 
	 * @param buildingType the type of the building
	 * @return true if life support
	 * @throws Exception if building type cannot be found.
	 */
	public boolean hasLifeSupport(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.LIFE_SUPPORT);
	}

	/**
	 * Gets the number of inhabitants the building's life support can handle.
	 * 
	 * @param buildingType the type of the building
	 * @return number of people
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getLifeSupportCapacity(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, LIFE_SUPPORT, CAPACITY);
	}

	/**
	 * Gets the power required for life support.
	 * 
	 * @param buildingType the type of the building
	 * @return power required (kW)
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public double getLifeSupportPowerRequirement(String buildingType) {
		return getValueAsDouble(buildingType, FUNCTIONS, LIFE_SUPPORT, POWER_REQUIRED);
	}

	/**
	 * Gets the heat required for life support.
	 * 
	 * @param buildingType the type of the building
	 * @return heat required (J)
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public double getLifeSupportHeatRequirement(String buildingType) {
		return getValueAsDouble(buildingType, FUNCTIONS, LIFE_SUPPORT, HEAT_REQUIRED);
	}

	/**
	 * Checks if the building provides living accommodations.
	 * 
	 * @param buildingType the type of the building
	 * @return true if living accommodations
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasLivingAccommodations(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.LIVING_ACCOMMODATIONS);
	}

	/**
	 * Gets the number of beds in the building's living accommodations.
	 * 
	 * @param buildingType the type of the building.
	 * @return number of beds.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getLivingAccommodationBeds(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, LIVING_ACCOMMODATIONS, BEDS);
	}

	/**
	 * Checks if the building provides robotic slots.
	 * 
	 * @param buildingType the type of the building
	 * @return true if robotic slots
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasRoboticStation(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.ROBOTIC_STATION);
	}

	/**
	 * Gets the number of slots in the building's robotic slots.
	 * 
	 * @param buildingType the type of the building.
	 * @return number of slots.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getRoboticStationSlots(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, ROBOTIC_STATION, SLOTS);
	}

	/**
	 * Checks if the building has a research lab.
	 * 
	 * @param buildingType the type of the building
	 * @return true if research lab.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasResearchLab(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.RESEARCH);
	}

	/**
	 * Gets the research tech level of the building.
	 * 
	 * @param buildingType the type of the building
	 * @return tech level
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getResearchTechLevel(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, RESEARCH, TECH_LEVEL);
	}

	/**
	 * Gets the number of researchers who can use the building's lab at once.
	 * 
	 * @param buildingType the type of the building
	 * @return number of researchers
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getResearchCapacity(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, RESEARCH, CAPACITY);
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
	 * Checks if the building has waste disposal
	 * 
	 * @param buildingType the type of the building
	 * @return true if waste disposal
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasWasteDisposal(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.WASTE_DISPOSAL);
	}

	/**
	 * Gets the waste disposal tech level of the building.
	 * 
	 * @param buildingType the type of the building
	 * @return tech level
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getWasteDisposalTechLevel(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, WASTE_DISPOSAL, TECH_LEVEL);
	}

	/**
	 * Gets the number of people who can use the building's waste disposal at once.
	 * 
	 * @param buildingType the type of the building
	 * @return number of people
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getWasteDisposalCapacity(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, WASTE_DISPOSAL, CAPACITY);
	}

	/**
	 * Gets a list of waste specialties for the building's lab.
	 * 
	 * @param buildingType the type of the building
	 * @return list of waste specialties as {@link ScienceType}.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public List<ScienceType> getWasteSpecialties(String buildingType) {
		
		if (!wasteSpecialties.containsKey(buildingType)) {
			
			List<ScienceType> result = new CopyOnWriteArrayList<ScienceType>();
			Element buildingElement = getBuildingElement(buildingType);
			Element functionsElement = buildingElement.getChild(FUNCTIONS);
			Element wasteElement = functionsElement.getChild(WASTE_DISPOSAL);
//			System.out.println(buildingType + " : " + wasteElement);
			if (wasteElement != null) {
				List<Element> wasteSpecialities = wasteElement.getChildren(WASTE_SPECIALTY);
				for (Element wasteSpecialityElement : wasteSpecialities) {
					String value = wasteSpecialityElement.getAttributeValue(NAME);
					// Take care that entries in buildings.xml conform to enum values of {@link
					// ScienceType}
					result.add(ScienceType.valueOf(ScienceType.class, value.toUpperCase().replace(" ", "_")));
				}
				return result;
			}
		}
		
		return wasteSpecialties.get(buildingType);
	}

	/**
	 * Checks if the building has communication capabilities.
	 * 
	 * @param buildingType the type of the building
	 * @return true if communication
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasCommunication(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.COMMUNICATION);
	}

	/**
	 * Checks if the building has EVA capabilities.
	 * 
	 * @param buildingType the type of the building
	 * @return true if EVA
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasEVA(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.EVA);
	}

	/**
	 * Gets the number of people who can use the building's airlock at once.
	 * 
	 * @param buildingType the type of the building
	 * @return airlock capacity
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getAirlockCapacity(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, EVA, AIRLOCK_CAPACITY);
	}

	/**
	 * Gets the relative X location of the airlock.
	 * 
	 * @param buildingType the type of the building.
	 * @return relative X location.
	 */
	public double getAirlockXLoc(String buildingType) {
		return getValueAsDouble(buildingType, FUNCTIONS, EVA, X_LOCATION);
	}

	/**
	 * Gets the relative Y location of the airlock.
	 * 
	 * @param buildingType the type of the building.
	 * @return relative Y location.
	 */
	public double getAirlockYLoc(String buildingType) {
		return getValueAsDouble(buildingType, FUNCTIONS, EVA, Y_LOCATION);
	}

	/**
	 * Gets the relative X location of the interior side of the airlock.
	 * 
	 * @param buildingType the type of the building.
	 * @return relative X location.
	 */
	public double getAirlockInteriorXLoc(String buildingType) {
		return getValueAsDouble(buildingType, FUNCTIONS, EVA, INTERIOR_X_LOCATION);
	}

	/**
	 * Gets the relative Y location of the interior side of the airlock.
	 * 
	 * @param buildingType the type of the building.
	 * @return relative Y location.
	 */
	public double getAirlockInteriorYLoc(String buildingType) {
		return getValueAsDouble(buildingType, FUNCTIONS, EVA, INTERIOR_Y_LOCATION);
	}

	/**
	 * Gets the relative X location of the exterior side of the airlock.
	 * 
	 * @param buildingType the type of the building.
	 * @return relative X location.
	 */
	public double getAirlockExteriorXLoc(String buildingType) {
		return getValueAsDouble(buildingType, FUNCTIONS, EVA, EXTERIOR_X_LOCATION);
	}

	/**
	 * Gets the relative Y location of the exterior side of the airlock.
	 * 
	 * @param buildingType the type of the building.
	 * @return relative Y location.
	 */
	public double getAirlockExteriorYLoc(String buildingType) {
		return getValueAsDouble(buildingType, FUNCTIONS, EVA, EXTERIOR_Y_LOCATION);
	}

	/**
	 * Checks if the building has a recreation facility.
	 * 
	 * @param buildingType the type of the building
	 * @return true if recreation
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasRecreation(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.RECREATION);
	}

	/**
	 * Gets the population number supported by the building's recreation function.
	 * 
	 * @param buildingType the type of the building.
	 * @return population support.
	 */
	public int getRecreationPopulationSupport(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, RECREATION, POPULATION_SUPPORT);
	}

	/**
	 * Checks if the building has a dining facility.
	 * 
	 * @param buildingType the type of the building
	 * @return true if dining
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasDining(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.DINING);
	}

	/**
	 * Gets the capacity for dining at the building.
	 * 
	 * @param buildingType the type of the building.
	 * @return capacity.
	 */
	public int getDiningCapacity(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, DINING, CAPACITY);
	}

	/**
	 * Checks if the building has resource processing capability.
	 * 
	 * @param buildingType the type of the building
	 * @return true if resource processing
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasResourceProcessing(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.RESOURCE_PROCESSING);
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
		return getValueAsDouble(buildingType, FUNCTIONS, RESOURCE_PROCESSING, POWER_DOWN_LEVEL);
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
	 * Checks if building has storage capability.
	 * 
	 * @param buildingType the type of the building.
	 * @return true if storage.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasStorage(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.STORAGE);
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
	 * Checks if building has power generation capability.
	 * 
	 * @param buildingType the type of the building
	 * @return true if power generation
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasThermalGeneration(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.THERMAL_GENERATION);
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
	 * Checks if building has heat generation capability.
	 * 
	 * @param buildingType the type of the building
	 * @return true if heat generation
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasPowerGeneration(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.POWER_GENERATION);
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
	 * Checks if building has heat storage capability.
	 * 
	 * @param buildingType the type of the building
	 * @return true if power storage
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasPowerStorage(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.POWER_STORAGE);
	}

	/**
	 * Gets the power storage capacity of the building.
	 * 
	 * @param buildingType the type of the building.
	 * @return power storage capacity (kW hr).
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public double getPowerStorageCapacity(String buildingType) {
		return getValueAsDouble(buildingType, FUNCTIONS, POWER_STORAGE, CAPACITY);
	}

	/**
	 * Checks if building has medical care capability.
	 * 
	 * @param buildingType the type of the building.
	 * @return true if medical care
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasMedicalCare(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.MEDICAL_CARE);
	}

	/**
	 * Gets the tech level of the building's medical care.
	 * 
	 * @param buildingType the type of the building.
	 * @return tech level
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getMedicalCareTechLevel(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, MEDICAL_CARE, TECH_LEVEL);
	}

	/**
	 * Gets the number of beds in the building's medical care.
	 * 
	 * @param buildingType the type of the building.
	 * @return tech level
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getMedicalCareBeds(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, MEDICAL_CARE, BEDS);
	}

	/**
	 * Checks if building has the farming function.
	 * 
	 * @param buildingType the type of the building.
	 * @return true if farming
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasFarming(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.FARMING);
	}

	/**
	 * Gets the number of crops in the building.
	 * 
	 * @param buildingType the type of the building.
	 * @return number of crops
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getCropNum(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, FARMING, CROPS);
	}

	/**
	 * Gets the power required to grow a crop.
	 * 
	 * @param buildingType the type of the building.
	 * @return power (kW)
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public double getPowerForGrowingCrop(String buildingType) {
		return getValueAsDouble(buildingType, FUNCTIONS, FARMING, POWER_GROWING_CROP);
	}

	/**
	 * Gets the power required to sustain a crop.
	 * 
	 * @param buildingType the type of the building.
	 * @return power (kW)
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public double getPowerForSustainingCrop(String buildingType) {
		return getValueAsDouble(buildingType, FUNCTIONS, FARMING, POWER_SUSTAINING_CROP);
	}

	/**
	 * Gets the crop growing area in the building.
	 * 
	 * @param buildingType the type of the building.
	 * @return crop growing area (square meters)
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public double getCropGrowingArea(String buildingType) {
		return getValueAsDouble(buildingType, FUNCTIONS, FARMING, GROWING_AREA);
	}

	/**
	 * Checks if the building has the exercise function.
	 * 
	 * @param buildingType the type of the building.
	 * @return true if exercise
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasExercise(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.EXERCISE);
	}

	/**
	 * Gets the capacity of the exercise facility in the building.
	 * 
	 * @param buildingType the type of the building.
	 * @return capacity for exercise
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getExerciseCapacity(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, EXERCISE, CAPACITY);
	}

	/**
	 * Checks if the building has the ground vehicle maintenance function.
	 * 
	 * @param buildingType the type of the building.
	 * @return true if ground vehicle maintenance
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasGroundVehicleMaintenance(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.GROUND_VEHICLE_MAINTENANCE);
	}

	/**
	 * Gets the vehicle capacity of the building.
	 * 
	 * @param buildingType the type of the building.
	 * @return vehicle capacity
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getVehicleCapacity(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, GROUND_VEHICLE_MAINTENANCE, VEHICLE_CAPACITY);
	}

	/**
	 * Gets the number of parking locations in the building.
	 * 
	 * @param buildingType the type of the building.
	 * @return number of parking locations.
	 */
	public int getParkingLocationNumber(String buildingType) {
		Element buildingElement = getBuildingElement(buildingType);
		Element functionsElement = buildingElement.getChild(FUNCTIONS);
		Element groundVehicleMaintenanceElement = functionsElement.getChild(GROUND_VEHICLE_MAINTENANCE);
		return groundVehicleMaintenanceElement.getChildren(PARKING_LOCATION).size();
	}

	/**
	 * Gets the relative location in the building of a parking location.
	 * 
	 * @param buildingType the type of the building.
	 * @param parkingIndex the parking location index.
	 * @return Point object containing the relative X & Y position from the building
	 *         center.
	 */
	public Point2D.Double getParkingLocation(String buildingType, int parkingIndex) {
		Element buildingElement = getBuildingElement(buildingType);
		Element functionsElement = buildingElement.getChild(FUNCTIONS);
		Element groundVehicleMaintenanceElement = functionsElement.getChild(GROUND_VEHICLE_MAINTENANCE);
		List<?> parkingLocations = groundVehicleMaintenanceElement.getChildren(PARKING_LOCATION);
		if ((parkingIndex >= 0) && (parkingIndex < parkingLocations.size())) {
			Element parkingLocation = (Element) parkingLocations.get(parkingIndex);
			try {
				Point2D.Double point = new Point2D.Double();
				double xLocation = parkingLocation.getAttribute(X_LOCATION).getDoubleValue();
				double yLocation = parkingLocation.getAttribute(Y_LOCATION).getDoubleValue();
				point.setLocation(xLocation, yLocation);
				return point;
			} catch (DataConversionException e) {
				throw new IllegalStateException(e);
			}
		} else {
			return null;
		}
	}

	/**
	 * Checks if the building has the cooking function.
	 * 
	 * @param buildingType the type of the building.
	 * @return true if cooking
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasCooking(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.COOKING);
	}

	/**
	 * Checks if the building has the Food Production function.
	 * 
	 * @param buildingType the type of the building.
	 * @return true if it has Food Production.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasFoodProduction(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.FOOD_PRODUCTION);
	}

	/**
	 * Gets the tech level of the Food Production facility in the building.
	 * 
	 * @param buildingType the type of the building.
	 * @return tech level.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getFoodProductionTechLevel(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, FOOD_PRODUCTION, TECH_LEVEL);
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
		return getValueAsInteger(buildingType, FUNCTIONS, FOOD_PRODUCTION, CONCURRENT_PROCESSES);
	}

	/**
	 * Gets the capacity of the cooking facility in the building.
	 * 
	 * @param buildingType the type of the building.
	 * @return capacity for cooking
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getCookCapacity(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, COOKING, CAPACITY);
	}

	/**
	 * Checks if the building has the manufacture function.
	 * 
	 * @param buildingType the type of the building.
	 * @return true if manufacture.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasManufacture(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.MANUFACTURE);
	}

	/**
	 * Gets the tech level of the manufacture facility in the building.
	 * 
	 * @param buildingType the type of the building.
	 * @return tech level.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getManufactureTechLevel(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, MANUFACTURE, TECH_LEVEL);
	}

	/**
	 * Checks if the building has an astronomical observation function.
	 * 
	 * @param buildingType the type of the building.
	 * @return true if building has astronomical observation function.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public boolean hasAstronomicalObservation(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.ASTRONOMICAL_OBSERVATION);
	}

	/**
	 * Gets the tech level of the astronomy facility in the building.
	 * 
	 * @param buildingType the type of the building.
	 * @return tech level.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getAstronomicalObservationTechLevel(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, ASTRONOMICAL_OBSERVATION, TECH_LEVEL);
	}

	/**
	 * Gets capacity of the astronomy facility in the building.
	 * 
	 * @param buildingType the type of the building.
	 * @return tech level.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public int getAstronomicalObservationCapacity(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, ASTRONOMICAL_OBSERVATION, CAPACITY);
	}

	/**
	 * Gets the power required by the astronomical observation function.
	 * 
	 * @param buildingType the type of the building.
	 * @return power required (kW).
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public double getAstronomicalObservationPowerRequirement(String buildingType) {
		return getValueAsDouble(buildingType, FUNCTIONS, ASTRONOMICAL_OBSERVATION, POWER_REQUIRED);
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
		return getValueAsInteger(buildingType, FUNCTIONS, MANUFACTURE, CONCURRENT_PROCESSES);
	}

	/**
	 * Checks if the building has the management function.
	 * 
	 * @param buildingType the type of the building.
	 * @return true if building has management function.
	 */
	public boolean hasManagement(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.MANAGEMENT);
	}

	/**
	 * Gets the management population support for a building.
	 * 
	 * @param buildingType the type of the building.
	 * @return population support
	 */
	public int getManagementPopulationSupport(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, MANAGEMENT, POPULATION_SUPPORT);
	}

	/**
	 * Checks if the building has the administration function.
	 * 
	 * @param buildingType the type of the building.
	 * @return true if building has administration function.
	 */
	public boolean hasAdministration(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.ADMINISTRATION);
	}

	/**
	 * Gets the administration population support for a building.
	 * 
	 * @param buildingType the type of the building.
	 * @return population support
	 */
	public int getAdministrationPopulationSupport(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, ADMINISTRATION, POPULATION_SUPPORT);
	}

	/**
	 * Checks if the building has an Earth return function.
	 * 
	 * @param buildingType the type of the building.
	 * @return true if building has earth return function.
	 */
	public boolean hasEarthReturn(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.EARTH_RETURN);
	}

	/**
	 * Gets the Earth return crew capacity of a building.
	 * 
	 * @param buildingType the type of the building.
	 * @return the crew capacity.
	 */
	public int getEarthReturnCrewCapacity(String buildingType) {
		return getValueAsInteger(buildingType, FUNCTIONS, EARTH_RETURN, CREW_CAPACITY);
	}

	/**
	 * Checks if the building has a building connection function.
	 * 
	 * @param buildingType the type of the building.
	 * @return true if building has a building connection function.
	 */
	public boolean hasBuildingConnection(String buildingType) {
		return getBuildingSpec(buildingType).getFunctionSupported().contains(FunctionType.BUILDING_CONNECTION);
	}

	/**
	 * Gets a list of activity spots for the administration building function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getAdministrationActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, ADMINISTRATION);
	}

	/**
	 * Gets a list of activity spots for the astronomical observation building
	 * function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getAstronomicalObservationActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, ASTRONOMICAL_OBSERVATION);
	}

	/**
	 * Gets a list of activity spots for the communication building function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getCommunicationActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, COMMUNICATION);
	}

	/**
	 * Gets a list of activity spots for the cooking building function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getCookingActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, COOKING);
	}

	/**
	 * Gets a list of activity spots for the dining building function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getDiningActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, DINING);
	}

	/**
	 * Gets a list of activity spots for the exercise building function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getExerciseActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, EXERCISE);
	}

	/**
	 * Gets a list of activity spots for the EVA building function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getEVAActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, EVA);
	}
	
	/**
	 * Gets a list of activity spots for the farming building function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getFarmingActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, FARMING);
	}

	/**
	 * Gets a list of activity spots for the Food Production building function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	// 2014-11-24 Added getFoodProductionActivitySpots
	public List<Point2D> getFoodProductionActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, FOOD_PRODUCTION);
	}

	/**
	 * Gets a list of activity spots for the ground vehicle maintenance building
	 * function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getGroundVehicleMaintenanceActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, GROUND_VEHICLE_MAINTENANCE);
	}

	/**
	 * Gets a list of activity spots for the living accommodations building
	 * function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getLivingAccommodationsActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, LIVING_ACCOMMODATIONS);
	}

	/**
	 * Gets a list of activity spots for the management building function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getManagementActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, MANAGEMENT);
	}

	/**
	 * Gets a list of activity spots for the manufacture building function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getManufactureActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, MANUFACTURE);
	}

	/**
	 * Gets a list of activity spots for the medical care building function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getMedicalCareActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, MEDICAL_CARE);
	}

	/**
	 * Gets a list of beds for the medical care building function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getMedicalCareBedLocations(String buildingType) {
		return getBedLocations(buildingType, MEDICAL_CARE);
	}
	
	/**
	 * Gets a list of activity spots for the recreation building function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getRecreationActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, RECREATION);
	}

	/**
	 * Gets a list of activity spots for the research building function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getResearchActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, RESEARCH);
	}

	/**
	 * Gets a list of activity spots for the waste disposal building function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getWasteDisposalActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, WASTE_DISPOSAL);
	}

	/**
	 * Gets a list of activity spots for the resource processing building function.
	 * 
	 * @param buildingType the type of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getResourceProcessingActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, RESOURCE_PROCESSING);
	}

	public List<Point2D> getRoboticStationActivitySpots(String buildingType) {
		return getActivitySpots(buildingType, ROBOTIC_STATION);
	}

	/**
	 * Checks if the building function has activity spots.
	 * 
	 * @param buildingType the type of the building.
	 * @param functionName the type of the building function.
	 * @return true if building function has activity spots.
	 */
	private boolean hasActivitySpots(String buildingType, String functionName) {
		Element buildingElement = getBuildingElement(buildingType);
		Element functionsElement = buildingElement.getChild(FUNCTIONS);
		Element functionElement = functionsElement.getChild(functionName);
		List<?> activityElements = functionElement.getChildren(ACTIVITY);
		return (activityElements.size() > 0);
	}

	/**
	 * Gets a list of activity spots for a building's function.
	 * 
	 * @param buildingType the type of the building.
	 * @param functionName the type of the building function.
	 * @return list of activity spots as Point2D objects.
	 */
	private List<Point2D> getActivitySpots(String buildingType, String functionName) {
		List<Point2D> result = new CopyOnWriteArrayList<Point2D>();

		if (hasActivitySpots(buildingType, functionName)) {
			Element buildingElement = getBuildingElement(buildingType);
			Element functionsElement = buildingElement.getChild(FUNCTIONS);
			Element functionElement = functionsElement.getChild(functionName);
			Element activityElement = functionElement.getChild(ACTIVITY);
			Iterator<Element> i = activityElement.getChildren(ACTIVITY_SPOT).iterator();
			while (i.hasNext()) {
				Element activitySpot = i.next();
				double xLocation = Double.parseDouble(activitySpot.getAttributeValue(X_LOCATION));
				double yLocation = Double.parseDouble(activitySpot.getAttributeValue(Y_LOCATION));
				result.add(new Point2D.Double(xLocation, yLocation));
			}
		}

		return result;
	}

	/**
	 * Checks if the building function has beds.
	 * 
	 * @param buildingType the type of the building.
	 * @param functionName the type of the building function.
	 * @return true if building function has beds.
	 */
	private boolean hasBedsLocations(String buildingType, String functionName) {
		Element buildingElement = getBuildingElement(buildingType);
		Element functionsElement = buildingElement.getChild(FUNCTIONS);
		Element functionElement = functionsElement.getChild(functionName);
		List<?> bedElements = functionElement.getChildren(BEDS);
		return (bedElements.size() > 0);
	}
	
	/**
	 * Gets a list of bed locations for a building's function.
	 * 
	 * @param buildingType the type of the building.
	 * @param functionName the type of the building function.
	 * @return list of bed locations as Point2D objects.
	 */
	private List<Point2D> getBedLocations(String buildingType, String functionName) {
		List<Point2D> result = new CopyOnWriteArrayList<Point2D>();

		if (hasBedsLocations(buildingType, functionName)) {
			Element buildingElement = getBuildingElement(buildingType);
			Element functionsElement = buildingElement.getChild(FUNCTIONS);
			Element functionElement = functionsElement.getChild(functionName);
			Element activityElement = functionElement.getChild(BED);
			Iterator<Element> i = activityElement.getChildren(BED_LOCATION).iterator();
			while (i.hasNext()) {
				Element activitySpot = i.next();
				double xLocation = Double.parseDouble(activitySpot.getAttributeValue(X_LOCATION));
				double yLocation = Double.parseDouble(activitySpot.getAttributeValue(Y_LOCATION));
				result.add(new Point2D.Double(xLocation, yLocation));
			}
		}

		return result;
	}
	
	private int getValueAsInteger(String buildingType, String child, String subchild, String param) {
		Element element1 = getBuildingElement(buildingType);
		Element element2 = element1.getChild(child);
		Element element3 = element2.getChild(subchild);
		return Integer.parseInt(element3.getAttributeValue(param));
	}

	private double getValueAsDouble(String buildingType, String child, String subchild, String param) {
		Element element1 = getBuildingElement(buildingType);
		Element element2 = element1.getChild(child);
		Element element3 = element2.getChild(subchild);
		return Double.parseDouble(element3.getAttributeValue(param));
	}

	/**
	 * Find a Buliding spec according to the name.
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
	
	private static final String generateSpecKey(String buildingType) {
		return buildingType.toLowerCase().replaceAll(" ", "-");
	}
}

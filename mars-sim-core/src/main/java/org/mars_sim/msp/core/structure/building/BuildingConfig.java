/**
 * Mars Simulation Project
 * BuildingConfig.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
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

	private static final String WEAR_LIFETIME = "wear-lifetime";
	private static final String MAINTENANCE_TIME = "maintenance-time";
	private static final String ROOM_TEMPERATURE = "room-temperature";

	private static final String FUNCTIONS = "functions";
	private static final String CAPACITY = "capacity";

	private static final String WASTE_DISPOSAL = "waste-disposal";
	private static final String WASTE_SPECIALTY = "waste-specialty";

	private static final String RESEARCH = "research";
	private static final String TECH_LEVEL = "tech-level";
	private static final String RESEARCH_SPECIALTY = "research-specialty";
	private static final String INTERIOR_X_LOCATION = "interior-xloc";
	private static final String INTERIOR_Y_LOCATION = "interior-yloc";
	private static final String EXTERIOR_X_LOCATION = "exterior-xloc";
	private static final String EXTERIOR_Y_LOCATION = "exterior-yloc";

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
	private static final String CROPS = "crops";
	private static final String POWER_GROWING_CROP = "power-growing-crop";
	private static final String POWER_SUSTAINING_CROP = "power-sustaining-crop";
	private static final String GROWING_AREA = "growing-area";
	private static final String GROUND_VEHICLE_MAINTENANCE = "ground-vehicle-maintenance";
	private static final String PARKING_LOCATION = "parking-location";
	private static final String X_LOCATION = "xloc";
	private static final String Y_LOCATION = "yloc";
	private static final String DEFAULT = "default";
	private static final String CONCURRENT_PROCESSES = "concurrent-processes";

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

	private transient Map<String, BuildingSpec> buildSpecMap = new HashMap<>();
	
	/**
	 * Constructor
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
		return  buildSpecMap.values().stream().map(BuildingSpec::getName).collect(Collectors.toSet());
	}

	/**
	 * Parse a building spec node
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
			List<Point2D> spots = parseLocations(element, ACTIVITY, ACTIVITY_SPOT,
													width, length);
			
			// Get attributes
			Properties props = new Properties();
			for(Attribute attr : element.getAttributes()) {
				props.setProperty(attr.getName(), attr.getValue());
			}
			
			BuildingSpec.FunctionSpec fspec = new BuildingSpec.FunctionSpec(props, spots);
			
			supportedFunctions.put(function, fspec);
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

		Element wasteElement = functionsElement.getChild(WASTE_DISPOSAL);
		if (wasteElement != null) {
			parseWaste(newSpec, wasteElement);
		}
		
		Element vehicleElement = functionsElement.getChild(GROUND_VEHICLE_MAINTENANCE);
		if (vehicleElement != null) {
			List<Point2D> parking = parseLocations(vehicleElement, "parking", PARKING_LOCATION, 	
												   width, length);
			newSpec.setParking(parking);
		}
		
		Element medicalElement = functionsElement.getChild(MEDICAL_CARE);
		if (medicalElement != null) {
			List<Point2D> beds = parseLocations(medicalElement, BEDS, BED_LOCATION,
												width, length);
			newSpec.setBeds(beds);
		}
		return newSpec;
	}

	/**
	 * Parse the specific Resoruce processing function details
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

	/**
	 * Parse a specific research details
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
	 * Parse a sources element
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
	 * Parse the specific Storage properties.
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
	 * Parse an list of locations for a building's function. These have a <xloc> & <yloc> structure.
	 * 
	 * @param functionElement Element holding locations
	 * @param locations Name of the location elements
	 * @param pointName Nmae of the point item
	 * @return list of activity spots as Point2D objects.
	 */
	private List<Point2D> parseLocations(Element functionElement, String locations, String pointName,
										 double buildingWidth, double buildingLength) {
		List<Point2D> result = new ArrayList<>();

		// Maximum coord is half the width or length
		double maxX = buildingWidth/2D;
		double maxY = buildingLength/2D;
		
		Element activityElement = functionElement.getChild(locations);
		if (activityElement != null) {
			for(Element activitySpot : activityElement.getChildren(pointName)) {
				double xLocation = Double.parseDouble(activitySpot.getAttributeValue(X_LOCATION));
				double yLocation = Double.parseDouble(activitySpot.getAttributeValue(Y_LOCATION));
				
				// Check location is within the building. Check as long as the maximum 
				// is defined
				if (((maxX > 0) && (maxX < Math.abs(xLocation)))
						|| ((maxY > 0) && (maxY < Math.abs(yLocation)))) {
					
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
				
				result.add(new Point2D.Double(xLocation, yLocation));
			}
		}
		return result;
	}

	/**
	 * Parse the waste function.
	 * @param newSpec
	 * @param wasteElement
	 */
	private void parseWaste(BuildingSpec newSpec, Element wasteElement) {
		List<ScienceType> result = new ArrayList<>();

//		The ScienceType in buildings.xml does not match current ScienceTypes
//		List<Element> wasteSpecialities = wasteElement.getChildren(WASTE_SPECIALTY);
//		for (Element wasteSpecialityElement : wasteSpecialities) {
//			String value = wasteSpecialityElement.getAttributeValue(NAME);
//			// Take care that entries in buildings.xml conform to enum values of {@linkcScienceType}
//			result.add(ScienceType.valueOf(ScienceType.class, value.toUpperCase().replace(" ", "_")));
//		}
		newSpec.setWasteSpecialties(result);
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
	 * Get teh activity spots for a function.
	 * @param buildingType
	 * @param function
	 * @return
	 */
	public List<Point2D> getActivitySpots(String buildingType, FunctionType function) {
		FunctionSpec fs = getBuildingSpec(buildingType).getFunctionSpec(function);
		List<Point2D> result = null;
		if (fs != null) {
			result = fs.getActivitySpots();
		}
		
		return result;
	}
	
	/**
	 * Get a property for a Function from a building type.
	 * @param buildingType Building type name
	 * @param function Function type
	 * @param name Property name
	 * @return
	 */
	public int getFunctionIntProperty(String buildingType, FunctionType function, String name) {
		return Integer.parseInt(getBuildingSpec(buildingType).getFunctionSpec(function).getProperty(name));
	}
	
	/**
	 * Get a property for a Function from a building type.
	 * @param buildingType Building type name
	 * @param function Function type
	 * @param name Property name
	 * @return
	 */
	public double getFunctionDoubleProperty(String buildingType, FunctionType function, String name) {
		return Double.parseDouble(getBuildingSpec(buildingType).getFunctionSpec(function).getProperty(name));
	}
	
	/**
	 * Gets the capacity for a Function. This capacity usually reference to the number of Unit
	 * supported by a function but it could be used differently
	 * 
	 * @param buildingType the type of the building
	 * @param function Typ eof function
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
	 * @param function Typ eof function
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
	 * @param function Typ eof function
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
	 * Gets a list of waste specialties for the building's lab.
	 * 
	 * @param buildingType the type of the building
	 * @return list of waste specialties as {@link ScienceType}.
	 * @throws Exception if building type cannot be found or XML parsing error.
	 */
	public List<ScienceType> getWasteSpecialties(String buildingType) {
		return getBuildingSpec(buildingType).getWasteSpecialties();

	}

	/**
	 * Gets the relative X location of the airlock.
	 * 
	 * @param buildingType the type of the building.
	 * @return relative X location.
	 */
	public double getAirlockXLoc(String buildingType) {
		return getFunctionDoubleProperty(buildingType, FunctionType.EVA, X_LOCATION);
	}

	/**
	 * Gets the relative Y location of the airlock.
	 * 
	 * @param buildingType the type of the building.
	 * @return relative Y location.
	 */
	public double getAirlockYLoc(String buildingType) {
		return getFunctionDoubleProperty(buildingType, FunctionType.EVA, Y_LOCATION);
	}

	/**
	 * Gets the relative X location of the interior side of the airlock.
	 * 
	 * @param buildingType the type of the building.
	 * @return relative X location.
	 */
	public double getAirlockInteriorXLoc(String buildingType) {
		return getFunctionDoubleProperty(buildingType, FunctionType.EVA, INTERIOR_X_LOCATION);
	}

	/**
	 * Gets the relative Y location of the interior side of the airlock.
	 * 
	 * @param buildingType the type of the building.
	 * @return relative Y location.
	 */
	public double getAirlockInteriorYLoc(String buildingType) {
		return getFunctionDoubleProperty(buildingType, FunctionType.EVA, INTERIOR_Y_LOCATION);
	}

	/**
	 * Gets the relative X location of the exterior side of the airlock.
	 * 
	 * @param buildingType the type of the building.
	 * @return relative X location.
	 */
	public double getAirlockExteriorXLoc(String buildingType) {
		return getFunctionDoubleProperty(buildingType, FunctionType.EVA, EXTERIOR_X_LOCATION);
	}

	/**
	 * Gets the relative Y location of the exterior side of the airlock.
	 * 
	 * @param buildingType the type of the building.
	 * @return relative Y location.
	 */
	public double getAirlockExteriorYLoc(String buildingType) {
		return getFunctionDoubleProperty(buildingType, FunctionType.EVA, EXTERIOR_Y_LOCATION);
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
	 * @param parkingIndex the parking location index.
	 * @return Point object containing the relative X & Y position from the building
	 *         center.
	 */
	public Point2D getParkingLocation(String buildingType, int parkingIndex) {
		List<Point2D> parking = getBuildingSpec(buildingType).getParking();
		Point2D result = null;
		if ((parking != null) && (parkingIndex < parking.size())) {
			result  = parking.get(parkingIndex);
		}
		return result;
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
	 * How many people can be supported by an Administration function in a building
	 * @param buildingType
	 * @return
	 */
	public int getAdministrationPopulationSupport(String buildingType) {
		return getFunctionIntProperty(buildingType, FunctionType.ADMINISTRATION, POPULATION_SUPPORT);
	}
}

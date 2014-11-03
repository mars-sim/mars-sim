/**
 * Mars Simulation Project
 * BuildingConfig.java
 * @version 3.07 2014-11-02
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
import java.util.Set;
//import java.util.logging.Logger;


import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.building.function.AreothermalPowerSource;
import org.mars_sim.msp.core.structure.building.function.ElectricHeatSource;
import org.mars_sim.msp.core.structure.building.function.FuelHeatSource;
import org.mars_sim.msp.core.structure.building.function.FuelPowerSource;
import org.mars_sim.msp.core.structure.building.function.HeatSource;
import org.mars_sim.msp.core.structure.building.function.PowerSource;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.SolarHeatSource;
import org.mars_sim.msp.core.structure.building.function.SolarPowerSource;
import org.mars_sim.msp.core.structure.building.function.SolarThermalPowerSource;
import org.mars_sim.msp.core.structure.building.function.StandardPowerSource;
import org.mars_sim.msp.core.structure.building.function.WindPowerSource;

/**
 * Provides configuration information about settlement buildings.
 * Uses a DOM document to get the information. 
 */
public class BuildingConfig implements Serializable {
    
    /** default serial id. */
    private static final long serialVersionUID = 1L;

    //private static final Logger logger = Logger.getLogger(BuildingConfig.class.getName());

    
	// Element and attribute names
	private static final String BUILDING = "building";
	//2014-10-27 mkung: Added nickName 	
	private static final String NICKNAME = "nickName";	
	private static final String NAME = "name";
	private static final String WIDTH = "width";
	private static final String LENGTH = "length";
	private static final String BASE_LEVEL = "base-level";

	private static final String FUNCTIONS = "functions";
	private static final String LIFE_SUPPORT = "life-support";
	private static final String CAPACITY = "capacity";
	private static final String LIVING_ACCOMMODATIONS = "living-accommodations";
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
	private static final String RATE = "rate";
	private static final String AMBIENT = "ambient";
	private static final String STORAGE = "storage";
	private static final String RESOURCE_STORAGE = "resource-storage";
	private static final String RESOURCE_INITIAL = "resource-initial";
	private static final String RESOURCE = "resource";
	private static final String AMOUNT = "amount";
	private static final String TYPE = "type";
	private static final String POWER = "power";
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
	private static final String FUEL_TYPE = "fuel-type";
	private static final String COMSUMPTION_RATE = "consumption-rate";
	private static final String TOGGLE = "toggle";

    private static final String ASTRONOMICAL_OBSERVATION = "astronomical-observation";
    private static final String EARTH_RETURN = "earth-return";
    private static final String CREW_CAPACITY = "crew-capacity";
    private static final String MANAGEMENT = "management";
    private static final String POPULATION_SUPPORT = "population-support";
    private static final String BUILDING_CONNECTION = "building-connection";
	private static final String ACTIVITY = "activity";
	private static final String ACTIVITY_SPOT = "activity-spot";
	private static final String ADMINISTRATION = "administration";
  
	// 2014-10-17 mkung: Added heat source and heat related types
	private static final String HEAT_REQUIRED = "heat-required";
	private static final String BASE_HEAT = "base-heat";
	private static final String BASE_POWER_DOWN_HEAT = "base-power-down-heat";
	//private static final String HEAT_DOWN_LEVEL = "heat-down-level";
	private static final String HEAT_SOURCE = "heat-source";
	private static final String THERMAL_GENERATION = "thermal-generation";
    private static final String THERMAL_STORAGE = "thermal-storage";

	private static final String ELECTRIC_HEAT_SOURCE = "Electric Heat Source";
	private static final String SOLAR_HEAT_SOURCE = "Solar Heat Source";
	private static final String FUEL_HEAT_SOURCE = "Fuel Heat Source";
    
	
	// Power source types
	private static final String POWER_GENERATION = "power-generation";
	private static final String POWER_SOURCE = "power-source";
    private static final String POWER_STORAGE = "power-storage";

	private static final String STANDARD_POWER_SOURCE = "Standard Power Source";
	private static final String SOLAR_POWER_SOURCE = "Solar Power Source";
    private static final String SOLAR_THERMAL_POWER_SOURCE = "Solar Thermal Power Source";
	private static final String FUEL_POWER_SOURCE = "Fuel Power Source";
    private static final String WIND_POWER_SOURCE = "Wind Power Source";
    private static final String AREOTHERMAL_POWER_SOURCE = "Areothermal Power Source";
	
	private Document buildingDoc;
	private Set<String> buildingNames;
	
	/**
	 * Constructor
	 * @param buildingDoc DOM document with building configuration
	 */
	public BuildingConfig(Document buildingDoc) {
		this.buildingDoc = buildingDoc;	
	}
	
	/**
	 * Gets a set of all building names.
	 * @return set of building names.
	 */
	@SuppressWarnings("unchecked")
	public Set<String> getBuildingNames() {
	    
	    if (buildingNames == null) {
	        buildingNames = new HashSet<String>();
	        Element root = buildingDoc.getRootElement();
	        List<Element> buildingNodes = root.getChildren(BUILDING);
	        for (Element buildingElement : buildingNodes) {
	            buildingNames.add(buildingElement.getAttributeValue(NAME));
	        }
	    }
	    
	    return buildingNames;
	}
	
	/**
	 * Gets a building DOM element for a particular building name.
	 * @param buildingName the building name
	 * @return building element
	 * @throws Exception if building name could not be found.
	 */
    @SuppressWarnings("unchecked")
	private Element getBuildingElement(String buildingName) {
		Element result = null;
		
		Element root = buildingDoc.getRootElement();
		List<Element> buildingNodes = root.getChildren(BUILDING);
		for (Element buildingElement : buildingNodes) {
			String name = buildingElement.getAttributeValue(NAME);
			if (buildingName.equalsIgnoreCase(name)) { 
				result = buildingElement;
				break;
			}
		}
		
		if (result == null) throw new IllegalStateException("Building type: " + buildingName +
			" could not be found in buildings.xml.");
		
		return result;
	}
    
    /**
     * Gets the building width.
     * @param buildingName the name of the building.
     * @return building width (meters).
     * @throws Exception if building name cannot be found or XML parsing error.
     */
    public double getWidth(String buildingName) {
        Element buildingElement = getBuildingElement(buildingName);
        double width = Double.parseDouble(buildingElement.getAttributeValue(WIDTH));
        	//logger.info("calling getWidth() : width is "+ width); 
        return width;
    }
    
    /**
     * Gets the building length.
     * @param buildingName the name of the building.
     * @return building length (meters).
     * @throws Exception if building name cannot be found or XML parsing error.
     */
    public double getLength(String buildingName) {
        Element buildingElement = getBuildingElement(buildingName);
        double length = Double.parseDouble(buildingElement.getAttributeValue(LENGTH));
        	//logger.info("calling getLength() : length is "+ length); 
        return length;
    }
    
    /**
     * Gets the base level of the building.
     * @param buildingName the name of the building.
     * @return -1 for in-ground, 0 for above-ground.
     */
    public int getBaseLevel(String buildingName) {
        Element buildingElement = getBuildingElement(buildingName);
        return Integer.parseInt(buildingElement.getAttributeValue(BASE_LEVEL));
    }
	
	/**
	 * Gets the base heat requirement for the building.
	 * @param buildingName the name of the building
	 * @return base heat requirement (J)
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public double getBaseHeatRequirement(String buildingName) {
        Element buildingElement = getBuildingElement(buildingName);
        Element heatElement = buildingElement.getChild(HEAT_REQUIRED);
        return Double.parseDouble(heatElement.getAttributeValue(BASE_HEAT));
	}
	
	/**
	 * Gets the base heat-down heat requirement for the building.
	 * @param buildingName the name of the building
	 * @return base heat-down heat (J)
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public double getBasePowerDownHeatRequirement(String buildingName) {
        Element buildingElement = getBuildingElement(buildingName);
        Element heatElement = buildingElement.getChild(HEAT_REQUIRED);
        return Double.parseDouble(heatElement.getAttributeValue(BASE_POWER_DOWN_HEAT));
	}
	
    
	/**
	 * Gets the base power requirement for the building.
	 * @param buildingName the name of the building
	 * @return base power requirement (kW)
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public double getBasePowerRequirement(String buildingName) {
        Element buildingElement = getBuildingElement(buildingName);
        Element powerElement = buildingElement.getChild(POWER_REQUIRED);
        return Double.parseDouble(powerElement.getAttributeValue(BASE_POWER));
	}
	
	/**
	 * Gets the base power-down power requirement for the building.
	 * @param buildingName the name of the building
	 * @return base power-down power (kW)
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public double getBasePowerDownPowerRequirement(String buildingName) {
        Element buildingElement = getBuildingElement(buildingName);
        Element powerElement = buildingElement.getChild(POWER_REQUIRED);
        return Double.parseDouble(powerElement.getAttributeValue(BASE_POWER_DOWN_POWER));
	}
	
	/**
	 * Checks if the building has life support.
	 * @param buildingName the name of the building
	 * @return true if life support
	 * @throws Exception if building name cannot be found.
	 */
	public boolean hasLifeSupport(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,LIFE_SUPPORT);
	}
	
	/**
	 * Gets the number of inhabitants the building's life support can handle.
	 * @param buildingName the name of the building
	 * @return number of people
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public int getLifeSupportCapacity(String buildingName) {
		return getValueAsInteger(buildingName,FUNCTIONS,LIFE_SUPPORT,CAPACITY);
	}
	
	/**
	 * Gets the power required for life support.
	 * @param buildingName the name of the building
	 * @return power required (kW)
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public double getLifeSupportPowerRequirement(String buildingName) {
		return getValueAsDouble(buildingName,FUNCTIONS,LIFE_SUPPORT,POWER_REQUIRED);
	}
	

	/**
	 * Gets the heat required for life support.
	 * @param buildingName the name of the building
	 * @return heat required (J)
	 * @throws Exception if building name cannot be found or XML parsing error.
	*/
	public double getLifeSupportHeatRequirement(String buildingName) {
		return getValueAsDouble(buildingName,FUNCTIONS,LIFE_SUPPORT,HEAT_REQUIRED);
	}
	 
	
	/**
	 * Checks if the building provides living accommodations.
	 * @param buildingName the name of the building
	 * @return true if living accommodations
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasLivingAccommodations(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,LIVING_ACCOMMODATIONS);
	}
	
	/**
	 * Gets the number of beds in the building's living accommodations.
	 * @param buildingName the name of the building.
	 * @return number of beds.
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public int getLivingAccommodationBeds(String buildingName) {
		return getValueAsInteger(buildingName,FUNCTIONS,LIVING_ACCOMMODATIONS,BEDS);
	}
	
	/**
	 * Checks if the building has a research lab.
	 * @param buildingName the name of the building
	 * @return true if research lab.
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasResearchLab(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,RESEARCH);
	}
	
	/**
	 * Gets the research tech level of the building.
	 * @param buildingName the name of the building
	 * @return tech level
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public int getResearchTechLevel(String buildingName) {
		return getValueAsInteger(buildingName,FUNCTIONS,RESEARCH,TECH_LEVEL);
	}
	
	/**
	 * Gets the number of researchers who can use the building's lab at once.
	 * @param buildingName the name of the building
	 * @return number of researchers
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */	
	public int getResearchCapacity(String buildingName) {
		return getValueAsInteger(buildingName,FUNCTIONS,RESEARCH,CAPACITY);		
	}
	
	/**
	 * Gets a list of research specialties for the building's lab.
	 * @param buildingName the name of the building
	 * @return list of research specialties as {@link ScienceType}.
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	@SuppressWarnings("unchecked")
	public List<ScienceType> getResearchSpecialties(String buildingName) {
	    List<ScienceType> result = new ArrayList<ScienceType>();
	    Element buildingElement = getBuildingElement(buildingName);
	    Element functionsElement = buildingElement.getChild(FUNCTIONS);
	    Element researchElement = functionsElement.getChild(RESEARCH);
	    List<Element> researchSpecialities = researchElement.getChildren(RESEARCH_SPECIALTY);

	    for (Element researchSpecialityElement : researchSpecialities ) {
	        String value = researchSpecialityElement.getAttributeValue(NAME);
	        // take care that entries in buildings.xml conform to enum values of {@link ScienceType}
	        result.add(ScienceType.valueOf(ScienceType.class, value.toUpperCase().replace(" ","_")));
	    }
	    return result;
	}
	
	/**
	 * Checks if the building has communication capabilities.
	 * @param buildingName the name of the building
	 * @return true if communication
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasCommunication(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,COMMUNICATION);
	}
	
	/**
	 * Checks if the building has EVA capabilities.
	 * @param buildingName the name of the building
	 * @return true if EVA
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasEVA(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,EVA);
	}
	
	/**
	 * Gets the number of people who can use the building's airlock at once.
	 * @param buildingName the name of the building
	 * @return airlock capacity
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public int getAirlockCapacity(String buildingName) {
		return getValueAsInteger(buildingName,FUNCTIONS,EVA,AIRLOCK_CAPACITY);
	}
	
	/**
	 * Gets the relative X location of the airlock.
	 * @param buildingName the name of the building.
	 * @return relative X location.
	 */
	public double getAirlockXLoc(String buildingName) {
	    return getValueAsDouble(buildingName, FUNCTIONS, EVA, X_LOCATION);
	}
	
	/**
     * Gets the relative Y location of the airlock.
     * @param buildingName the name of the building.
     * @return relative Y location.
     */
    public double getAirlockYLoc(String buildingName) {
        return getValueAsDouble(buildingName, FUNCTIONS, EVA, Y_LOCATION);
    }
    
    /**
     * Gets the relative X location of the interior side of the airlock.
     * @param buildingName the name of the building.
     * @return relative X location.
     */
    public double getAirlockInteriorXLoc(String buildingName) {
        return getValueAsDouble(buildingName, FUNCTIONS, EVA, INTERIOR_X_LOCATION);
    }
    
    /**
     * Gets the relative Y location of the interior side of the airlock.
     * @param buildingName the name of the building.
     * @return relative Y location.
     */
    public double getAirlockInteriorYLoc(String buildingName) {
        return getValueAsDouble(buildingName, FUNCTIONS, EVA, INTERIOR_Y_LOCATION);
    }
    
    /**
     * Gets the relative X location of the exterior side of the airlock.
     * @param buildingName the name of the building.
     * @return relative X location.
     */
    public double getAirlockExteriorXLoc(String buildingName) {
        return getValueAsDouble(buildingName, FUNCTIONS, EVA, EXTERIOR_X_LOCATION);
    }
    
    /**
     * Gets the relative Y location of the exterior side of the airlock.
     * @param buildingName the name of the building.
     * @return relative Y location.
     */
    public double getAirlockExteriorYLoc(String buildingName) {
        return getValueAsDouble(buildingName, FUNCTIONS, EVA, EXTERIOR_Y_LOCATION);
    }
	
	/**
	 * Checks if the building has a recreation facility.
	 * @param buildingName the name of the building
	 * @return true if recreation
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasRecreation(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,RECREATION);
	}
	
	/**
	 * Gets the population number supported by the building's recreation function.
	 * @param buildingName the name of the building.
	 * @return population support.
	 */
	public int getRecreationPopulationSupport(String buildingName) {
	    return getValueAsInteger(buildingName, FUNCTIONS, RECREATION, POPULATION_SUPPORT);
	}
	
	/**
	 * Checks if the building has a dining facility.
	 * @param buildingName the name of the building
	 * @return true if dining
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasDining(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,DINING);
	}
	
	/**
	 * Gets the capacity for dining at the building.
	 * @param buildingName the name of the building.
	 * @return capacity.
	 */
	public int getDiningCapacity(String buildingName) {
	    return getValueAsInteger(buildingName, FUNCTIONS, DINING, CAPACITY);
	}
	
	/**
	 * Checks if the building has resource processing capability.
	 * @param buildingName the name of the building
	 * @return true if resource processing
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasResourceProcessing(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,RESOURCE_PROCESSING);
	}
	
	/**
	 * Gets the level of resource processing when the building is in power down mode.
	 * @param buildingName the name of the building
	 * @return power down level
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public double getResourceProcessingPowerDown(String buildingName) {
		return getValueAsDouble(buildingName,FUNCTIONS,RESOURCE_PROCESSING,POWER_DOWN_LEVEL);
	}
	
	
	/**
	 * Gets the building's resource processes. 
	 * @param buildingName the name of the building.
	 * @return a list of resource processes.
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
    @SuppressWarnings("unchecked")
	public List<ResourceProcess> getResourceProcesses(String buildingName) {
		List<ResourceProcess> resourceProcesses = new ArrayList<ResourceProcess>();
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = buildingElement.getChild(FUNCTIONS);
		Element resourceProcessingElement = functionsElement.getChild(RESOURCE_PROCESSING);
		List<Element> resourceProcessNodes = resourceProcessingElement.getChildren(PROCESS);
		
		for (Element processElement : resourceProcessNodes) {
	
			String defaultString = processElement.getAttributeValue(DEFAULT);
			boolean defaultOn = true;
			if (defaultString.equals("off")) defaultOn = false;
            
            double powerRequired = Double.parseDouble(processElement.getAttributeValue(POWER_REQUIRED));
			
			ResourceProcess process = new ResourceProcess(processElement.getAttributeValue(NAME), 
                    powerRequired, defaultOn);
			
			// Get input resources.
			List<Element> inputNodes = processElement.getChildren(INPUT);
			
			for (Element inputElement : inputNodes) {
				String resourceName = inputElement.getAttributeValue(RESOURCE).toLowerCase();
				AmountResource resource = AmountResource.findAmountResource(resourceName);
				double rate = Double.parseDouble(inputElement.getAttributeValue(RATE)) / 1000D;
				boolean ambient = Boolean.valueOf(inputElement.getAttributeValue(AMBIENT));
				process.addMaxInputResourceRate(resource, rate, ambient);
			}
			
			// Get output resources.
			List<Element> outputNodes = processElement.getChildren(OUTPUT);
			for (Element outputElement : outputNodes) {
				String resourceName = outputElement.getAttributeValue(RESOURCE).toLowerCase();
				AmountResource resource = AmountResource.findAmountResource(resourceName);
				double rate = Double.parseDouble(outputElement.getAttributeValue(RATE)) / 1000D;
				boolean ambient = Boolean.valueOf(outputElement.getAttributeValue(AMBIENT));
				process.addMaxOutputResourceRate(resource, rate, ambient);
			}
			
			resourceProcesses.add(process);
		}
		
		return resourceProcesses;
	}
	
	/**
	 * Checks if building has storage capability.
	 * @param buildingName the name of the building.
	 * @return true if storage.
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasStorage(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,STORAGE);
	}
	
	/**
	 * Gets a list of the building's resource capacities. 
	 * @param buildingName the name of the building.
	 * @return list of storage capacities
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
    @SuppressWarnings("unchecked")
	public Map<AmountResource, Double> getStorageCapacities(String buildingName) {
		Map<AmountResource, Double> capacities = new HashMap<AmountResource, Double>();
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = buildingElement.getChild(FUNCTIONS);
		Element storageElement = functionsElement.getChild(STORAGE);
		List<Element> resourceStorageNodes = storageElement.getChildren(RESOURCE_STORAGE);
		
		for (Element resourceStorageElement : resourceStorageNodes) {
			String resourceName = resourceStorageElement.getAttributeValue(RESOURCE).toLowerCase();
            AmountResource resource = AmountResource.findAmountResource(resourceName);
			Double capacity = new Double(resourceStorageElement.getAttributeValue(CAPACITY));
			capacities.put(resource, capacity);
		}
		return capacities;
	}
	
	/**
	 * Gets a map of the initial resources stored in this building.
	 * @param buildingName the name of the building
	 * @return map of initial resources
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
    @SuppressWarnings("unchecked")
	public Map<AmountResource, Double> getInitialStorage(String buildingName) {
		Map<AmountResource, Double> resourceMap = new HashMap<AmountResource, Double>();
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = buildingElement.getChild(FUNCTIONS);
		Element storageElement = functionsElement.getChild(STORAGE);
		List<Element> resourceInitialNodes = storageElement.getChildren(RESOURCE_INITIAL);
		for (Element resourceInitialElement : resourceInitialNodes) {
			String resourceName = resourceInitialElement.getAttributeValue(RESOURCE).toLowerCase();
            AmountResource resource = AmountResource.findAmountResource(resourceName);
			Double amount = new Double(resourceInitialElement.getAttributeValue(AMOUNT));
			resourceMap.put(resource, amount);
		}
		return resourceMap;
	}
	
	/**
	 * Checks if building has power generation capability.
	 * @param buildingName the name of the building
	 * @return true if power generation
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasThermalGeneration(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,THERMAL_GENERATION);
	}
	
	/**
	 * Gets a list of the building's heat sources.
	 * @param buildingName the name of the building.
	 * @return list of heat sources
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
    @SuppressWarnings("unchecked")
	public List<HeatSource> getHeatSources(String buildingName) {
		List<HeatSource> heatSourceList = new ArrayList<HeatSource>();
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = buildingElement.getChild(FUNCTIONS);
		Element thermalGenerationElement = functionsElement.getChild(THERMAL_GENERATION);
			//logger.info("getHeatSources() : just finished reading heat-generation");
		List<Element> heatSourceNodes = thermalGenerationElement.getChildren(HEAT_SOURCE);	
			//logger.info("getHeatSources() : just finished reading heat-source");
		for (Element heatSourceElement : heatSourceNodes) {
			String type = heatSourceElement.getAttributeValue(TYPE);			
				//logger.info("getHeatSources() : finished reading type");
			double heat = Double.parseDouble(heatSourceElement.getAttributeValue(CAPACITY));			
				//logger.info("getHeatSources() : finished reading capacity");
			HeatSource heatSource = null;
			if (type.equalsIgnoreCase(ELECTRIC_HEAT_SOURCE)) {
				heatSource = new ElectricHeatSource(heat);	
				//logger.info("getHeatSources() : just called ElectricHeatSource");
			} else if (type.equalsIgnoreCase(SOLAR_HEAT_SOURCE)) {
				heatSource = new SolarHeatSource(heat);
				//logger.info("getHeatSources() : just called SolarHeatSource");
			} else if (type.equalsIgnoreCase(FUEL_HEAT_SOURCE)) {
				    boolean toggleStafe = Boolean.parseBoolean(heatSourceElement.getAttributeValue(TOGGLE));
				    String fuelType = heatSourceElement.getAttributeValue(FUEL_TYPE);
				    double consumptionSpeed = Double.parseDouble(heatSourceElement.getAttributeValue(COMSUMPTION_RATE));
				    heatSource = new FuelHeatSource(heat ,toggleStafe, fuelType, consumptionSpeed);
			} else throw new IllegalStateException("Heat source: " + type + " not a valid heat source.");
				//logger.info("getHeatSources() : finished reading electric heat source and solar heat source");
			heatSourceList.add(heatSource); 
				//logger.info("getHeatSources() : just added that heatSource");
		}
		return heatSourceList;
	}
    
    
    /**
     * Checks if building has heat storage capability.
     * @param buildingName the name of the building
     * @return true if heat storage
     * @throws Exception if building name cannot be found or XML parsing error.
     */
    public boolean hasThermalStorage(String buildingName) {
    	return hasElements(buildingName,FUNCTIONS,THERMAL_STORAGE);
    }
    
    /**
     * Gets the heat storage capacity of the building.
     * @param buildingName the name of the building.
     * @return heat storage capacity (kW hr).
     * @throws Exception if building name cannot be found or XML parsing error.
     */
    public double getThermalStorageCapacity(String buildingName) {
    	return getValueAsDouble(buildingName,FUNCTIONS,POWER_STORAGE,CAPACITY);
    }
	

    
	/**
	 * Checks if building has heat generation capability.
	 * @param buildingName the name of the building
	 * @return true if heat generation
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasPowerGeneration(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,POWER_GENERATION);
	}
	

	/**
	 * Gets a list of the building's power sources.
	 * @param buildingName the name of the building.
	 * @return list of power sources
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
    @SuppressWarnings("unchecked")
	public List<PowerSource> getPowerSources(String buildingName) {
		List<PowerSource> powerSourceList = new ArrayList<PowerSource>();
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = buildingElement.getChild(FUNCTIONS);
		Element powerGenerationElement = functionsElement.getChild(POWER_GENERATION);
		List<Element> powerSourceNodes = powerGenerationElement.getChildren(POWER_SOURCE);
		for (Element powerSourceElement : powerSourceNodes) {
			String type = powerSourceElement.getAttributeValue(TYPE);
			double power = Double.parseDouble(powerSourceElement.getAttributeValue(POWER));
			PowerSource powerSource = null;
			if (type.equalsIgnoreCase(STANDARD_POWER_SOURCE)) powerSource = new StandardPowerSource(power);

			else if (type.equalsIgnoreCase(SOLAR_POWER_SOURCE)) powerSource = new SolarPowerSource(power);
            else if (type.equalsIgnoreCase(SOLAR_THERMAL_POWER_SOURCE)) powerSource = new SolarThermalPowerSource(power);
			else if (type.equalsIgnoreCase(FUEL_POWER_SOURCE)) {
			    boolean toggleStafe = Boolean.parseBoolean(powerSourceElement.getAttributeValue(TOGGLE));
			    String fuelType = powerSourceElement.getAttributeValue(FUEL_TYPE);
			    double consumptionSpeed = Double.parseDouble(powerSourceElement.getAttributeValue(COMSUMPTION_RATE));
			    powerSource = new FuelPowerSource(power ,toggleStafe, fuelType, consumptionSpeed);
			}
            else if (type.equalsIgnoreCase(WIND_POWER_SOURCE)) powerSource = new WindPowerSource(power);
            else if (type.equalsIgnoreCase(AREOTHERMAL_POWER_SOURCE)) powerSource = new AreothermalPowerSource(power);
			else throw new IllegalStateException("Power source: " + type + " not a valid power source.");
			powerSourceList.add(powerSource); 
		}
		return powerSourceList;

	}
    
    
    /**
     * Checks if building has heat storage capability.
     * @param buildingName the name of the building
     * @return true if power storage
     * @throws Exception if building name cannot be found or XML parsing error.
     */
    public boolean hasPowerStorage(String buildingName) {
    	return hasElements(buildingName,FUNCTIONS,POWER_STORAGE);
    }
    
    /**
     * Gets the power storage capacity of the building.
     * @param buildingName the name of the building.
     * @return power storage capacity (kW hr).
     * @throws Exception if building name cannot be found or XML parsing error.
     */
    public double getPowerStorageCapacity(String buildingName) {
    	return getValueAsDouble(buildingName,FUNCTIONS,POWER_STORAGE,CAPACITY);
    }
	
	/**
	 * Checks if building has medical care capability.
	 * @param buildingName the name of the building.
	 * @return true if medical care
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasMedicalCare(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,MEDICAL_CARE);
	}
	
	/**
	 * Gets the tech level of the building's medical care.
	 * @param buildingName the name of the building.
	 * @return tech level
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public int getMedicalCareTechLevel(String buildingName) {
		return getValueAsInteger(buildingName,FUNCTIONS,MEDICAL_CARE,TECH_LEVEL);
	}
	
	/**
	 * Gets the number of beds in the building's medical care.
	 * @param buildingName the name of the building.
	 * @return tech level
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public int getMedicalCareBeds(String buildingName) {
		return getValueAsInteger(buildingName,FUNCTIONS,MEDICAL_CARE,BEDS);
	}
	
	/**
	 * Checks if building has the farming function.
	 * @param buildingName the name of the building.
	 * @return true if farming
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasFarming(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,FARMING);
	}
	
	/**
	 * Gets the number of crops in the building.
	 * @param buildingName the name of the building.
	 * @return number of crops
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public int getCropNum(String buildingName) {
		return getValueAsInteger(buildingName,FUNCTIONS,FARMING,CROPS);
	}
	
	/**
	 * Gets the power required to grow a crop.
	 * @param buildingName the name of the building.
	 * @return power (kW)
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public double getPowerForGrowingCrop(String buildingName) {
		return getValueAsDouble(buildingName,FUNCTIONS,FARMING,POWER_GROWING_CROP);
	}
	
	/**
	 * Gets the power required to sustain a crop.
	 * @param buildingName the name of the building.
	 * @return power (kW)
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public double getPowerForSustainingCrop(String buildingName) {
		return getValueAsDouble(buildingName,FUNCTIONS,FARMING,POWER_SUSTAINING_CROP);
	}
	
	/**
	 * Gets the crop growing area in the building.
	 * @param buildingName the name of the building.
	 * @return crop growing area (square meters)
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public double getCropGrowingArea(String buildingName) {
		return getValueAsDouble(buildingName,FUNCTIONS,FARMING,GROWING_AREA);
	}
	
	/**
	 * Checks if the building has the exercise function.
	 * @param buildingName the name of the building.
	 * @return true if exercise
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasExercise(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,EXERCISE);
	}
	
	/**
	 * Gets the capacity of the exercise facility in the building.
	 * @param buildingName the name of the building.
	 * @return capacity for exercise
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public int getExerciseCapacity(String buildingName) {
		return getValueAsInteger(buildingName,FUNCTIONS,EXERCISE,CAPACITY);
	}
	
	/**
	 * Checks if the building has the ground vehicle maintenance function.
	 * @param buildingName the name of the building.
	 * @return true if ground vehicle maintenance
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasGroundVehicleMaintenance(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,GROUND_VEHICLE_MAINTENANCE);
	}
	
	/**
	 * Gets the vehicle capacity of the building.
	 * @param buildingName the name of the building.
	 * @return vehicle capacity
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public int getVehicleCapacity(String buildingName) {
		return getValueAsInteger(buildingName,FUNCTIONS,GROUND_VEHICLE_MAINTENANCE,VEHICLE_CAPACITY);
	}
	
	/**
	 * Gets the number of parking locations in the building.
	 * @param buildingName the name of the building.
	 * @return number of parking locations.
	 */
	public int getParkingLocationNumber(String buildingName) {
	    Element buildingElement = getBuildingElement(buildingName);
	    Element functionsElement = buildingElement.getChild(FUNCTIONS);
        Element groundVehicleMaintenanceElement = functionsElement.getChild(GROUND_VEHICLE_MAINTENANCE);
        return groundVehicleMaintenanceElement.getChildren(PARKING_LOCATION).size();
	}
	
	/**
	 * Gets the relative location in the building of a parking location.
	 * @param buildingName the name of the building.
	 * @param parkingIndex the parking location index.
	 * @return Point object containing the relative X & Y position from the building center.
	 */
	public Point2D.Double getParkingLocation(String buildingName, int parkingIndex) {
	    Element buildingElement = getBuildingElement(buildingName);
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
            }
            catch (DataConversionException e) {
                throw new IllegalStateException(e);
            }
        }
        else {
            return null;
        }
	}
	
	/**
	 * Checks if the building has the cooking function.
	 * @param buildingName the name of the building.
	 * @return true if cooking
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasCooking(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,COOKING);
	}
	
	/**
	 * Gets the capacity of the cooking facility in the building.
	 * @param buildingName the name of the building.
	 * @return capacity for cooking
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public int getCookCapacity(String buildingName) {
		return getValueAsInteger(buildingName,FUNCTIONS,COOKING,CAPACITY);
	}
	
	/**
	 * Checks if the building has the manufacture function.
	 * @param buildingName the name of the building.
	 * @return true if manufacture.
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasManufacture(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,MANUFACTURE);
	}
	
	/**
	 * Gets the tech level of the manufacture facility in the building.
	 * @param buildingName the name of the building.
	 * @return tech level.
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public int getManufactureTechLevel(String buildingName) {
		return getValueAsInteger(buildingName,FUNCTIONS,MANUFACTURE,TECH_LEVEL);
	}
	
	/**
	 * Checks if the building has an astronomical observation function.
	 * @param buildingName the name of the building.
	 * @return true if building has astronomical observation function.
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public boolean hasAstronomicalObservation(String buildingName) {
		return hasElements(buildingName,FUNCTIONS,ASTRONOMICAL_OBSERVATION);
	}
	
	/**
	 * Gets the tech level of the astronomy  facility in the building.
	 * @param buildingName the name of the building.
	 * @return tech level.
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public int getAstronomicalObservationTechLevel(String buildingName) {
		return getValueAsInteger(buildingName,FUNCTIONS,ASTRONOMICAL_OBSERVATION,TECH_LEVEL);
	}
	
	/**
	 * Gets capacity of the astronomy  facility in the building.
	 * @param buildingName the name of the building.
	 * @return tech level.
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public int getAstronomicalObservationCapacity(String buildingName) {
		return getValueAsInteger(buildingName,FUNCTIONS,ASTRONOMICAL_OBSERVATION,CAPACITY);
	}
	
    /**
     * Gets the power required by the astronomical observation function.
     * @param buildingName the name of the building.
     * @return power required (kW).
     * @throws Exception if building name cannot be found or XML parsing error.
     */
    public double getAstronomicalObservationPowerRequirement(String buildingName) {
    	return getValueAsDouble(buildingName,FUNCTIONS,ASTRONOMICAL_OBSERVATION,POWER_REQUIRED);
    }
	
	/**
	 * Gets the concurrent process limit of the manufacture facility in the building.
	 * @param buildingName the name of the building.
	 * @return concurrent process limit.
	 * @throws Exception if building name cannot be found or XML parsing error.
	 */
	public int getManufactureConcurrentProcesses(String buildingName) {
		return getValueAsInteger(buildingName,FUNCTIONS,MANUFACTURE,CONCURRENT_PROCESSES);
	}
	
	/**
	 * Checks if the building has the management function.
	 * @param buildingName the name of the building.
	 * @return true if building has management function.
	 */
	public boolean hasManagement(String buildingName) {
	    return hasElements(buildingName, FUNCTIONS, MANAGEMENT);
	}
	
	/**
	 * Gets the management population support for a building. 
	 * @param buildingName the name of the building.
	 * @return population support
	 */
	public int getManagementPopulationSupport(String buildingName) {
	    return getValueAsInteger(buildingName, FUNCTIONS, MANAGEMENT, POPULATION_SUPPORT);
	}
	
	/**
	 * Checks if the building has the administration function.
	 * @param buildingName the name of the building.
	 * @return true if building has administration function.
	 */
	public boolean hasAdministration(String buildingName) {
	    return hasElements(buildingName, FUNCTIONS, ADMINISTRATION);
	}
	
	/**
     * Gets the administration population support for a building. 
     * @param buildingName the name of the building.
     * @return population support
     */
    public int getAdministrationPopulationSupport(String buildingName) {
        return getValueAsInteger(buildingName, FUNCTIONS, ADMINISTRATION, POPULATION_SUPPORT);
    }
	
	/**
	 * Checks if the building has an Earth return function.
	 * @param buildingName the name of the building.
	 * @return true if building has earth return function.
	 */
	public boolean hasEarthReturn(String buildingName) {
	    return hasElements(buildingName, FUNCTIONS, EARTH_RETURN);
	}
	
	/**
	 * Gets the Earth return crew capacity of a building.
	 * @param buildingName the name of the building.
	 * @return the crew capacity.
	 */
	public int getEarthReturnCrewCapacity(String buildingName) {
	    return getValueAsInteger(buildingName, FUNCTIONS, EARTH_RETURN, CREW_CAPACITY);
	}
	
	/**
	 * Checks if the building has a building connection function.
	 * @param buildingName the name of the building.
	 * @return true if building has a building connection function.
	 */
	public boolean hasBuildingConnection(String buildingName) {
	    return hasElements(buildingName, FUNCTIONS, BUILDING_CONNECTION);
	}
	
    /**
     * Gets a list of activity spots for the administration building function.
     * @param buildingName the name of the building.
     * @return list of activity spots as Point2D objects.
     */
    public List<Point2D> getAdministrationActivitySpots(String buildingName) {
        return getActivitySpots(buildingName, ADMINISTRATION);
    }
	
	/**
     * Gets a list of activity spots for the astronomical observation building function.
     * @param buildingName the name of the building.
     * @return list of activity spots as Point2D objects.
     */
    public List<Point2D> getAstronomicalObservationActivitySpots(String buildingName) {
        return getActivitySpots(buildingName, ASTRONOMICAL_OBSERVATION);
    }
	
	/**
	 * Gets a list of activity spots for the communication building function.
	 * @param buildingName the name of the building.
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getCommunicationActivitySpots(String buildingName) {
	    return getActivitySpots(buildingName, COMMUNICATION);
	}
	
	/**
     * Gets a list of activity spots for the cooking building function.
     * @param buildingName the name of the building.
     * @return list of activity spots as Point2D objects.
     */
    public List<Point2D> getCookingActivitySpots(String buildingName) {
        return getActivitySpots(buildingName, COOKING);
    }
    
    /**
     * Gets a list of activity spots for the dining building function.
     * @param buildingName the name of the building.
     * @return list of activity spots as Point2D objects.
     */
    public List<Point2D> getDiningActivitySpots(String buildingName) {
        return getActivitySpots(buildingName, DINING);
    }
    
    /**
     * Gets a list of activity spots for the exercise building function.
     * @param buildingName the name of the building.
     * @return list of activity spots as Point2D objects.
     */
    public List<Point2D> getExerciseActivitySpots(String buildingName) {
        return getActivitySpots(buildingName, EXERCISE);
    }
    
    /**
     * Gets a list of activity spots for the farming building function.
     * @param buildingName the name of the building.
     * @return list of activity spots as Point2D objects.
     */
    public List<Point2D> getFarmingActivitySpots(String buildingName) {
        return getActivitySpots(buildingName, FARMING);
    }
    
    /**
     * Gets a list of activity spots for the ground vehicle maintenance building function.
     * @param buildingName the name of the building.
     * @return list of activity spots as Point2D objects.
     */
    public List<Point2D> getGroundVehicleMaintenanceActivitySpots(String buildingName) {
        return getActivitySpots(buildingName, GROUND_VEHICLE_MAINTENANCE);
    }
    
    /**
     * Gets a list of activity spots for the living accommodations building function.
     * @param buildingName the name of the building.
     * @return list of activity spots as Point2D objects.
     */
    public List<Point2D> getLivingAccommodationsActivitySpots(String buildingName) {
        return getActivitySpots(buildingName, LIVING_ACCOMMODATIONS);
    }
    
    /**
     * Gets a list of activity spots for the management building function.
     * @param buildingName the name of the building.
     * @return list of activity spots as Point2D objects.
     */
    public List<Point2D> getManagementActivitySpots(String buildingName) {
        return getActivitySpots(buildingName, MANAGEMENT);
    }
    
    /**
     * Gets a list of activity spots for the manufacture building function.
     * @param buildingName the name of the building.
     * @return list of activity spots as Point2D objects.
     */
    public List<Point2D> getManufactureActivitySpots(String buildingName) {
        return getActivitySpots(buildingName, MANUFACTURE);
    }
    
    /**
     * Gets a list of activity spots for the medical care building function.
     * @param buildingName the name of the building.
     * @return list of activity spots as Point2D objects.
     */
    public List<Point2D> getMedicalCareActivitySpots(String buildingName) {
        return getActivitySpots(buildingName, MEDICAL_CARE);
    }
    
    /**
     * Gets a list of activity spots for the recreation building function.
     * @param buildingName the name of the building.
     * @return list of activity spots as Point2D objects.
     */
    public List<Point2D> getRecreationActivitySpots(String buildingName) {
        return getActivitySpots(buildingName, RECREATION);
    }
    
    /**
     * Gets a list of activity spots for the research building function.
     * @param buildingName the name of the building.
     * @return list of activity spots as Point2D objects.
     */
    public List<Point2D> getResearchActivitySpots(String buildingName) {
        return getActivitySpots(buildingName, RESEARCH);
    }
    
    /**
     * Gets a list of activity spots for the resource processing building function.
     * @param buildingName the name of the building.
     * @return list of activity spots as Point2D objects.
     */
    public List<Point2D> getResourceProcessingActivitySpots(String buildingName) {
        return getActivitySpots(buildingName, RESOURCE_PROCESSING);
    }
	
	/**
	 * Checks if the building function has activity spots.
	 * @param buildingName the name of the building.
	 * @param functionName the name of the building function.
	 * @return true if building function has activity spots.
	 */
	private boolean hasActivitySpots(String buildingName, String functionName) {
	    Element buildingElement = getBuildingElement(buildingName);
        Element functionsElement = buildingElement.getChild(FUNCTIONS);
        Element functionElement = functionsElement.getChild(functionName);
        List<?> activityElements = functionElement.getChildren(ACTIVITY);
        return (activityElements.size() > 0);
	}
	
	/**
	 * Gets a list of activity spots for a building's function.
	 * @param buildingName the name of the building.
	 * @param functionName the name of the building function.
	 * @return list of activity spots as Point2D objects.
	 */
	@SuppressWarnings("unchecked")
	private List<Point2D> getActivitySpots(String buildingName, String functionName) {
	    List<Point2D> result = new ArrayList<Point2D>();
	    
	    if (hasActivitySpots(buildingName, functionName)) {
	        Element buildingElement = getBuildingElement(buildingName);
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
	
	private int getValueAsInteger(String buildingName, String child, 
			                      String subchild, String param){
		Element element1 = getBuildingElement(buildingName);
		Element element2 = element1.getChild(child);
		Element element3 = element2.getChild(subchild);
		return Integer.parseInt(element3.getAttributeValue(param));
	}
	
	private double getValueAsDouble(String buildingName, String child, 
            String subchild, String param) {
		Element element1 = getBuildingElement(buildingName);
		Element element2 = element1.getChild(child);
		Element element3 = element2.getChild(subchild);
		return Double.parseDouble(element3.getAttributeValue(param));
	}
	
    @SuppressWarnings("unchecked")
	private boolean hasElements(String buildingName, String child, String children) {
		Element element1 = getBuildingElement(buildingName);
		Element element2 = element1.getChild(child);
		List<Element> elements = element2.getChildren(children);
		return (elements.size() > 0);
	}
    
    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
       buildingDoc = null; 
    }
}
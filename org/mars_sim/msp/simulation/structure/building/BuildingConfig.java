/**
 * Mars Simulation Project
 * BuildingConfig.java
 * @version 2.85 2008-11-26
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.building.function.FuelPowerSource;
import org.mars_sim.msp.simulation.structure.building.function.PowerSource;
import org.mars_sim.msp.simulation.structure.building.function.ResourceProcess;
import org.mars_sim.msp.simulation.structure.building.function.SolarPowerSource;
import org.mars_sim.msp.simulation.structure.building.function.SolarThermalPowerSource;
import org.mars_sim.msp.simulation.structure.building.function.StandardPowerSource;
import org.mars_sim.msp.simulation.structure.building.function.WindPowerSource;
import org.w3c.dom.NodeList;


/**
 * Provides configuration information about settlement buildings.
 * Uses a DOM document to get the information. 
 */
public class BuildingConfig implements Serializable {

	// Element names
	private static final String BUILDING = "building";
	private static final String NAME = "name";
	private static final String POWER_REQUIRED = "power-required";
	private static final String BASE_POWER = "base-power";
	private static final String BASE_POWER_DOWN_POWER = "base-power-down-power";
	private static final String FUNCTIONS = "functions";
	private static final String LIFE_SUPPORT = "life-support";
	private static final String CAPACITY = "capacity";
	private static final String LIVING_ACCOMMODATIONS = "living-accommodations";
	private static final String RESEARCH = "research";
	private static final String TECH_LEVEL = "tech-level";
	private static final String RESEARCH_SPECIALITY = "research-speciality";
	private static final String COMMUNICATION = "communication";
	private static final String EVA = "EVA";
	private static final String AIRLOCK_CAPACITY = "airlock-capacity";
	private static final String RECREATION = "recreation";
	private static final String DINING = "dining";
	private static final String RESOURCE_PROCESSING = "resource-processing";
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
	private static final String POWER_GENERATION = "power-generation";
	private static final String POWER_SOURCE = "power-source";
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
	private static final String VEHICLE_CAPACITY = "vehicle-capacity";
	private static final String COOKING = "cooking";
	private static final String DEFAULT = "default";
	private static final String MANUFACTURE = "manufacture";
	private static final String CONCURRENT_PROCESSES = "concurrent-processes";
	private static final String FUEL_TYPE = "fuel-type";
	private static final String COMSUMPTION_RATE = "consumption-rate";
	private static final String TOGGLE = "toggle";
    private static final String POWER_STORAGE = "power-storage";
	
	// Power source types
	private static final String STANDARD_POWER_SOURCE = "Standard Power Source";
	private static final String SOLAR_POWER_SOURCE = "Solar Power Source";
    private static final String SOLAR_THERMAL_POWER_SOURCE = "Solar Thermal Power Source";
	private static final String FUEL_POWER_SOURCE = "Fuel Power Source";
    private static final String WIND_POWER_SOURCE = "Wind Power Source";
	
	private Document buildingDoc;
	
	/**
	 * Constructor
	 * @param buildingDoc DOM document with building configuration
	 */
	public BuildingConfig(Document buildingDoc) {
		this.buildingDoc = buildingDoc;	
	}
	
	/**
	 * Gets a building DOM element for a particular building name.
	 * @param buildingName the building name
	 * @return building element
	 * @throws Exception if building name could not be found.
	 */
	private Element getBuildingElement(String buildingName) throws Exception {
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
		
		if (result == null) throw new Exception("Building type: " + buildingName + 
			" could not be found in buildings.xml.");
		
		return result;
	}
	
	/**
	 * Gets the base power requirement for the building.
	 * @param buildingName the name of the building
	 * @return base power requirement (kW)
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public double getBasePowerRequirement(String buildingName) throws Exception {
        try {
            Element buildingElement = getBuildingElement(buildingName);
            Element powerElement = buildingElement.getChild(POWER_REQUIRED);
            return Double.parseDouble(powerElement.getAttributeValue(BASE_POWER));
        }
        catch (Exception e) {
            throw new Exception("power-required: base-power attribute not found for building: " + buildingName);
        }
	}
	
	/**
	 * Gets the base power-down power requirement for the building.
	 * @param buildingName the name of the building
	 * @return base power-down power (kW)
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public double getBasePowerDownPowerRequirement(String buildingName) throws Exception {
        try {
            Element buildingElement = getBuildingElement(buildingName);
            Element powerElement = buildingElement.getChild(POWER_REQUIRED);
            return Double.parseDouble(powerElement.getAttributeValue(BASE_POWER_DOWN_POWER));
        }
        catch (Exception e) {
            throw new Exception("power-required: base-power-down-power attribute not found for building: " + buildingName);
        }
	}
	
	/**
	 * Checks if the building has life support.
	 * @param buildingName the name of the building
	 * @return true if life support
	 * @throws Exception if building name can not be found.
	 */
	public boolean hasLifeSupport(String buildingName) throws Exception {
		return hasElements(buildingName,FUNCTIONS,LIFE_SUPPORT);
	}
	
	/**
	 * Gets the number of inhabitants the building's life support can handle.
	 * @param buildingName the name of the building
	 * @return number of people
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getLifeSupportCapacity(String buildingName) throws Exception {
		return getValueAsInteger(buildingName,FUNCTIONS,LIFE_SUPPORT,CAPACITY);
	}
	
	/**
	 * Gets the power required for life support.
	 * @param buildingName the name of the building
	 * @return power required (kW)
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public double getLifeSupportPowerRequirement(String buildingName) throws Exception {
		return getValueAsDouble(buildingName,FUNCTIONS,LIFE_SUPPORT,POWER_REQUIRED);
	}
	
	/**
	 * Checks if the building provides living accommodations.
	 * @param buildingName the name of the building
	 * @return true if living accommodations
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasLivingAccommodations(String buildingName) throws Exception {
		return hasElements(buildingName,FUNCTIONS,LIVING_ACCOMMODATIONS);
	}
	
	/**
	 * Gets the number of beds in the building's living accommodations.
	 * @param buildingName the name of the building.
	 * @return number of beds.
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getLivingAccommodationBeds(String buildingName) throws Exception {
		return getValueAsInteger(buildingName,FUNCTIONS,LIVING_ACCOMMODATIONS,BEDS);
	}
	
	/**
	 * Checks if the building has a research lab.
	 * @param buildingName the name of the building
	 * @return true if research lab.
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasResearchLab(String buildingName) throws Exception {
		return hasElements(buildingName,FUNCTIONS,RESEARCH);
	}
	
	/**
	 * Gets the research tech level of the building.
	 * @param buildingName the name of the building
	 * @return tech level
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getResearchTechLevel(String buildingName) throws Exception {
		return getValueAsInteger(buildingName,FUNCTIONS,RESEARCH,TECH_LEVEL);
	}
	
	/**
	 * Gets the number of researchers who can use the building's lab at once.
	 * @param buildingName the name of the building
	 * @return number of researchers
	 * @throws Exception if building name can not be found or XML parsing error.
	 */	
	public int getResearchCapacity(String buildingName) throws Exception {
		return getValueAsInteger(buildingName,FUNCTIONS,RESEARCH,CAPACITY);		
	}
	
	/**
	 * Gets a list of research specialities for the building's lab.
	 * @param buildingName the name of the building
	 * @return list of research specialities as strings.
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public List<String> getResearchSpecialities(String buildingName) throws Exception {
		List<String> result = new ArrayList<String>();
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = buildingElement.getChild(FUNCTIONS);
		Element researchElement = functionsElement.getChild(RESEARCH);
		List<Element> researchSpecialities = researchElement.getChildren(RESEARCH_SPECIALITY);
		
		for (Element researchSpecialityElement : researchSpecialities ) {
			result.add(researchSpecialityElement.getAttributeValue(NAME));
		}
		return result;
	}
	
	/**
	 * Checks if the building has communication capabilities.
	 * @param buildingName the name of the building
	 * @return true if communication
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasCommunication(String buildingName) throws Exception {
		return hasElements(buildingName,FUNCTIONS,COMMUNICATION);
	}
	
	/**
	 * Checks if the building has EVA capabilities.
	 * @param buildingName the name of the building
	 * @return true if EVA
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasEVA(String buildingName) throws Exception {
		return hasElements(buildingName,FUNCTIONS,EVA);
	}
	
	/**
	 * Gets the number of people who can use the building's airlock at once.
	 * @param buildingName the name of the building
	 * @return airlock capacity
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getAirlockCapacity(String buildingName) throws Exception {
		return getValueAsInteger(buildingName,FUNCTIONS,EVA,AIRLOCK_CAPACITY);
	}
	
	/**
	 * Checks if the building has a recreation facility.
	 * @param buildingName the name of the building
	 * @return true if recreation
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasRecreation(String buildingName) throws Exception {
		return hasElements(buildingName,FUNCTIONS,RECREATION);
	}	
	
	/**
	 * Checks if the building has a dining facility.
	 * @param buildingName the name of the building
	 * @return true if dining
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasDining(String buildingName) throws Exception {
		return hasElements(buildingName,FUNCTIONS,DINING);
	}
	
	/**
	 * Checks if the building has resource processing capability.
	 * @param buildingName the name of the building
	 * @return true if resource processing
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasResourceProcessing(String buildingName) throws Exception {
		return hasElements(buildingName,FUNCTIONS,RESOURCE_PROCESSING);
	}
	
	/**
	 * Gets the level of resource processing when the building is in power down mode.
	 * @param buildingName the name of the building
	 * @return power down level
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public double getResourceProcessingPowerDown(String buildingName) throws Exception {
		return getValueAsDouble(buildingName,FUNCTIONS,RESOURCE_PROCESSING,POWER_DOWN_LEVEL);
	}
	
	
	/**
	 * Gets the building's resource processes. 
	 * @param buildingName the name of the building.
	 * @return a list of resource processes.
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public List<ResourceProcess> getResourceProcesses(String buildingName) throws Exception {
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
				boolean ambient = Boolean.valueOf(inputElement.getAttributeValue(AMBIENT)).booleanValue();
				process.addMaxInputResourceRate(resource, rate, ambient);
			}
			
			// Get output resources.
			List<Element> outputNodes = processElement.getChildren(OUTPUT);
			for (Element outputElement : outputNodes) {
				String resourceName = outputElement.getAttributeValue(RESOURCE).toLowerCase();
				AmountResource resource = AmountResource.findAmountResource(resourceName);
				double rate = Double.parseDouble(outputElement.getAttributeValue(RATE)) / 1000D;
				boolean ambient = Boolean.valueOf(outputElement.getAttributeValue(AMBIENT)).booleanValue();
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
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasStorage(String buildingName) throws Exception {
		return hasElements(buildingName,FUNCTIONS,STORAGE);
	}
	
	/**
	 * Gets a list of the building's resource capacities. 
	 * @param buildingName the name of the building.
	 * @return list of storage capacities
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public Map<AmountResource, Double> getStorageCapacities(String buildingName) throws Exception {
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
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public Map<AmountResource, Double> getInitialStorage(String buildingName) throws Exception {
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
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasPowerGeneration(String buildingName) throws Exception {
		return hasElements(buildingName,FUNCTIONS,POWER_GENERATION);
	}
	
	/**
	 * Gets a list of the building's power sources.
	 * @param buildingName the name of the building.
	 * @return list of power sources
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public List<PowerSource> getPowerSources(String buildingName) throws Exception {
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
			    
			else throw new Exception("Power source: " + type + " not a valid power source.");
			powerSourceList.add(powerSource); 
		}
		return powerSourceList;
	}
    
    /**
     * Checks if building has power storage capability.
     * @param buildingName the name of the building
     * @return true if power storage
     * @throws Exception if building name can not be found or XML parsing error.
     */
    public boolean hasPowerStorage(String buildingName) throws Exception {
    	return hasElements(buildingName,FUNCTIONS,POWER_STORAGE);
    }
    
    /**
     * Gets the power storage capacity of the building.
     * @param buildingName the name of the building.
     * @return power storage capacity (kW hr).
     * @throws Exception if building name can not be found or XML parsing error.
     */
    public double getPowerStorageCapacity(String buildingName) throws Exception {
    	return getValueAsDouble(buildingName,FUNCTIONS,POWER_STORAGE,CAPACITY);
    }
	
	/**
	 * Checks if building has medical care capability.
	 * @param buildingName the name of the building.
	 * @return true if medical care
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasMedicalCare(String buildingName) throws Exception {
		return hasElements(buildingName,FUNCTIONS,MEDICAL_CARE);
	}
	
	/**
	 * Gets the tech level of the building's medical care.
	 * @param buildingName the name of the building.
	 * @return tech level
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getMedicalCareTechLevel(String buildingName) throws Exception {
		return getValueAsInteger(buildingName,FUNCTIONS,MEDICAL_CARE,TECH_LEVEL);
	}
	
	/**
	 * Gets the number of beds in the building's medical care.
	 * @param buildingName the name of the building.
	 * @return tech level
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getMedicalCareBeds(String buildingName) throws Exception {
		return getValueAsInteger(buildingName,FUNCTIONS,MEDICAL_CARE,BEDS);
	}
	
	/**
	 * Checks if building has the farming function.
	 * @param buildingName the name of the building.
	 * @return true if farming
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasFarming(String buildingName) throws Exception {
		return hasElements(buildingName,FUNCTIONS,FARMING);
	}
	
	/**
	 * Gets the number of crops in the building.
	 * @param buildingName the name of the building.
	 * @return number of crops
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getCropNum(String buildingName) throws Exception {
		return getValueAsInteger(buildingName,FUNCTIONS,FARMING,CROPS);
	}
	
	/**
	 * Gets the power required to grow a crop.
	 * @param buildingName the name of the building.
	 * @return power (kW)
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public double getPowerForGrowingCrop(String buildingName) throws Exception {
		return getValueAsDouble(buildingName,FUNCTIONS,FARMING,POWER_GROWING_CROP);
	}
	
	/**
	 * Gets the power required to sustain a crop.
	 * @param buildingName the name of the building.
	 * @return power (kW)
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public double getPowerForSustainingCrop(String buildingName) throws Exception {
		return getValueAsDouble(buildingName,FUNCTIONS,FARMING,POWER_SUSTAINING_CROP);
	}
	
	/**
	 * Gets the crop growing area in the building.
	 * @param buildingName the name of the building.
	 * @return crop growing area (square meters)
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public double getCropGrowingArea(String buildingName) throws Exception {
		return getValueAsDouble(buildingName,FUNCTIONS,FARMING,GROWING_AREA);
	}
	
	/**
	 * Checks if the building has the exercise function.
	 * @param buildingName the name of the building.
	 * @return true if exercise
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasExercise(String buildingName) throws Exception {
		return hasElements(buildingName,FUNCTIONS,EXERCISE);
	}
	
	/**
	 * Gets the capacity of the exercise facility in the building.
	 * @param buildingName the name of the building.
	 * @return capacity for exercise
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getExerciseCapacity(String buildingName) throws Exception {
		return getValueAsInteger(buildingName,FUNCTIONS,EXERCISE,CAPACITY);
	}
	
	/**
	 * Checks if the building has the ground vehicle maintenance function.
	 * @param buildingName the name of the building.
	 * @return true if ground vehicle maintenance
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasGroundVehicleMaintenance(String buildingName) throws Exception {
		return hasElements(buildingName,FUNCTIONS,GROUND_VEHICLE_MAINTENANCE);
	}
	
	/**
	 * Gets the vehicle capacity of the building.
	 * @param buildingName the name of the building.
	 * @return vehicle capacity
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getVehicleCapacity(String buildingName) throws Exception {
		return getValueAsInteger(buildingName,FUNCTIONS,GROUND_VEHICLE_MAINTENANCE,VEHICLE_CAPACITY);
	}
	
	/**
	 * Checks if the building has the cooking function.
	 * @param buildingName the name of the building.
	 * @return true if cooking
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasCooking(String buildingName) throws Exception {
		return hasElements(buildingName,FUNCTIONS,COOKING);
	}
	
	/**
	 * Gets the capacity of the cooking facility in the building.
	 * @param buildingName the name of the building.
	 * @return capacity for cooking
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getCookCapacity(String buildingName) throws Exception {
		return getValueAsInteger(buildingName,FUNCTIONS,COOKING,CAPACITY);
	}
	
	/**
	 * Checks if the building has the manufacture function.
	 * @param buildingName the name of the building.
	 * @return true if manufacture.
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasManufacture(String buildingName) throws Exception {
		return hasElements(buildingName,FUNCTIONS,MANUFACTURE);
	}
	
	/**
	 * Gets the tech level of the manufacture facility in the building.
	 * @param buildingName the name of the building.
	 * @return tech level.
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getManufactureTechLevel(String buildingName) throws Exception {
		return getValueAsInteger(buildingName,FUNCTIONS,MANUFACTURE,TECH_LEVEL);
	}
	
	/**
	 * Gets the concurrent process limit of the manufacture facility in the building.
	 * @param buildingName the name of the building.
	 * @return concurrent process limit.
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getManufactureConcurrentProcesses(String buildingName) throws Exception {
		return getValueAsInteger(buildingName,FUNCTIONS,MANUFACTURE,CONCURRENT_PROCESSES);
	}
	
	private int getValueAsInteger(String buildingName, String child, 
			                      String subchild, String param) throws Exception{
		Element element1 = getBuildingElement(buildingName);
		Element element2 = element1.getChild(child);
		Element element3 = element2.getChild(subchild);
		return Integer.parseInt(element3.getAttributeValue(param));
	}
	
	private double getValueAsDouble(String buildingName, String child, 
            String subchild, String param) throws Exception{
		Element element1 = getBuildingElement(buildingName);
		Element element2 = element1.getChild(child);
		Element element3 = element2.getChild(subchild);
		return Double.parseDouble(element3.getAttributeValue(param));
	}
	
	private boolean hasElements(String buildingName, String child, String children) throws Exception {
		Element element1 = getBuildingElement(buildingName);
		Element element2 = element1.getChild(child);
		List<Element> elements = element2.getChildren(children);
		return (elements.size() > 0);
	}
}
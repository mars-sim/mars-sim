/**
 * Mars Simulation Project
 * BuildingConfig.java
 * @version 2.85 2008-07-12
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building;

import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.building.function.*;

import org.w3c.dom.*;

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
	private static final String MAX_CAPACITY = "capacity";
	
	// Power source types
	private static final String STANDARD_POWER_SOURCE = "Standard Power Source";
	private static final String SOLAR_POWER_SOURCE = "Solar Power Source";
	private static final String FUEL_POWER_SOURCE = "Fuel Power Source";
	
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
		
		Element root = buildingDoc.getDocumentElement();
		NodeList buildingNodes = root.getElementsByTagName(BUILDING);
		for (int x=0; x < buildingNodes.getLength(); x++) {
			Element buildingElement = (Element) buildingNodes.item(x);
			String name = buildingElement.getAttribute(NAME);
			if (buildingName.equals(name)) result = buildingElement;
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
		Element buildingElement = getBuildingElement(buildingName);
		Element powerElement = (Element) buildingElement.getElementsByTagName(POWER_REQUIRED).item(0);
		return Double.parseDouble(powerElement.getAttribute(BASE_POWER));
	}
	
	/**
	 * Gets the base power-down power requirement for the building.
	 * @param buildingName the name of the building
	 * @return base power-down power (kW)
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public double getBasePowerDownPowerRequirement(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element powerElement = (Element) buildingElement.getElementsByTagName(POWER_REQUIRED).item(0);
		return Double.parseDouble(powerElement.getAttribute(BASE_POWER_DOWN_POWER));
	}
	
	/**
	 * Checks if the building has life support.
	 * @param buildingName the name of the building
	 * @return true if life support
	 * @throws Exception if building name can not be found.
	 */
	public boolean hasLifeSupport(String buildingName) throws Exception {
		boolean result = false;
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		NodeList lifeSupportNodes = functionsElement.getElementsByTagName(LIFE_SUPPORT);
		if (lifeSupportNodes.getLength() > 0) result = true;
		return result;
	}
	
	/**
	 * Gets the number of inhabitants the building's life support can handle.
	 * @param buildingName the name of the building
	 * @return number of people
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getLifeSupportCapacity(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element lifeSupportElement = (Element) functionsElement.getElementsByTagName(LIFE_SUPPORT).item(0);
		return Integer.parseInt(lifeSupportElement.getAttribute(CAPACITY));
	}
	
	/**
	 * Gets the power required for life support.
	 * @param buildingName the name of the building
	 * @return power required (kW)
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public double getLifeSupportPowerRequirement(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element lifeSupportElement = (Element) functionsElement.getElementsByTagName(LIFE_SUPPORT).item(0);
		return Double.parseDouble(lifeSupportElement.getAttribute(POWER_REQUIRED));
	}
	
	/**
	 * Checks if the building provides living accommodations.
	 * @param buildingName the name of the building
	 * @return true if living accommodations
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasLivingAccommodations(String buildingName) throws Exception {
		boolean result = false;
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		NodeList livingAccommodationsNodes = functionsElement.getElementsByTagName(LIVING_ACCOMMODATIONS);
		if (livingAccommodationsNodes.getLength() > 0) result = true;
		return result;
	}
	
	/**
	 * Gets the number of beds in the building's living accommodations.
	 * @param buildingName the name of the building.
	 * @return number of beds.
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getLivingAccommodationBeds(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element livingAccommodationsElement = (Element) functionsElement.getElementsByTagName(LIVING_ACCOMMODATIONS).item(0);
		return Integer.parseInt(livingAccommodationsElement.getAttribute(BEDS));
	}
	
	/**
	 * Checks if the building has a research lab.
	 * @param buildingName the name of the building
	 * @return true if research lab.
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasResearchLab(String buildingName) throws Exception {
		boolean result = false;
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		NodeList researchNodes = functionsElement.getElementsByTagName(RESEARCH);
		if (researchNodes.getLength() > 0) result = true;
		return result;
	}
	
	/**
	 * Gets the research tech level of the building.
	 * @param buildingName the name of the building
	 * @return tech level
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getResearchTechLevel(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element researchElement = (Element) functionsElement.getElementsByTagName(RESEARCH).item(0);
		return Integer.parseInt(researchElement.getAttribute(TECH_LEVEL));
	}
	
	/**
	 * Gets the number of researchers who can use the building's lab at once.
	 * @param buildingName the name of the building
	 * @return number of researchers
	 * @throws Exception if building name can not be found or XML parsing error.
	 */	
	public int getResearchCapacity(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element researchElement = (Element) functionsElement.getElementsByTagName(RESEARCH).item(0);
		return Integer.parseInt(researchElement.getAttribute(CAPACITY));		
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
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element researchElement = (Element) functionsElement.getElementsByTagName(RESEARCH).item(0);
		NodeList researchSpecialities = researchElement.getElementsByTagName(RESEARCH_SPECIALITY);
		for (int x=0; x < researchSpecialities.getLength(); x++) {
			Element researchSpecialityElement = (Element) researchSpecialities.item(x);
			result.add(researchSpecialityElement.getAttribute(NAME));
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
		boolean result = false;
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		NodeList communicationNodes = functionsElement.getElementsByTagName(COMMUNICATION);
		if (communicationNodes.getLength() > 0) result = true;
		return result;
	}
	
	/**
	 * Checks if the building has EVA capabilities.
	 * @param buildingName the name of the building
	 * @return true if EVA
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasEVA(String buildingName) throws Exception {
		boolean result = false;
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		NodeList evaNodes = functionsElement.getElementsByTagName(EVA);
		if (evaNodes.getLength() > 0) result = true;
		return result;
	}
	
	/**
	 * Gets the number of people who can use the building's airlock at once.
	 * @param buildingName the name of the building
	 * @return airlock capacity
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getAirlockCapacity(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element evaElement = (Element) functionsElement.getElementsByTagName(EVA).item(0);
		return Integer.parseInt(evaElement.getAttribute(AIRLOCK_CAPACITY));
	}
	
	/**
	 * Checks if the building has a recreation facility.
	 * @param buildingName the name of the building
	 * @return true if recreation
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasRecreation(String buildingName) throws Exception {
		boolean result = false;
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		NodeList recreationNodes = functionsElement.getElementsByTagName(RECREATION);
		if (recreationNodes.getLength() > 0) result = true;
		return result;
	}	
	
	/**
	 * Checks if the building has a dining facility.
	 * @param buildingName the name of the building
	 * @return true if dining
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasDining(String buildingName) throws Exception {
		boolean result = false;
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		NodeList diningNodes = functionsElement.getElementsByTagName(DINING);
		if (diningNodes.getLength() > 0) result = true;
		return result;
	}
	
	/**
	 * Checks if the building has resource processing capability.
	 * @param buildingName the name of the building
	 * @return true if resource processing
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasResourceProcessing(String buildingName) throws Exception {
		boolean result = false;
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		NodeList resourceProcessingNodes = functionsElement.getElementsByTagName(RESOURCE_PROCESSING);
		if (resourceProcessingNodes.getLength() > 0) result = true;
		return result;
	}
	
	/**
	 * Gets the level of resource processing when the building is in power down mode.
	 * @param buildingName the name of the building
	 * @return power down level
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public double getResourceProcessingPowerDown(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element resourceProcessingElement = (Element) functionsElement.getElementsByTagName(RESOURCE_PROCESSING).item(0);
		return Double.parseDouble(resourceProcessingElement.getAttribute(POWER_DOWN_LEVEL));
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
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element resourceProcessingElement = (Element) functionsElement.getElementsByTagName(RESOURCE_PROCESSING).item(0);
		NodeList resourceProcessNodes = resourceProcessingElement.getElementsByTagName(PROCESS);
		for (int x=0; x < resourceProcessNodes.getLength(); x++) {
			Element processElement = (Element) resourceProcessNodes.item(x);
			
			String defaultString = processElement.getAttribute(DEFAULT);
			boolean defaultOn = true;
			if (defaultString.equals("off")) defaultOn = false;
			
			ResourceProcess process = new ResourceProcess(processElement.getAttribute(NAME), defaultOn);
			
			// Get input resources.
			NodeList inputNodes = processElement.getElementsByTagName(INPUT);
			for (int y=0; y < inputNodes.getLength(); y++) {
				Element inputElement = (Element) inputNodes.item(y);
				String resourceName = inputElement.getAttribute(RESOURCE).toLowerCase();
				AmountResource resource = AmountResource.findAmountResource(resourceName);
				double rate = Double.parseDouble(inputElement.getAttribute(RATE)) / 1000D;
				boolean ambient = Boolean.valueOf(inputElement.getAttribute(AMBIENT)).booleanValue();
				process.addMaxInputResourceRate(resource, rate, ambient);
			}
			
			// Get output resources.
			NodeList outputNodes = processElement.getElementsByTagName(OUTPUT);
			for (int y=0; y < outputNodes.getLength(); y++) {
				Element outputElement = (Element) outputNodes.item(y);
				String resourceName = outputElement.getAttribute(RESOURCE).toLowerCase();
				AmountResource resource = AmountResource.findAmountResource(resourceName);
				double rate = Double.parseDouble(outputElement.getAttribute(RATE)) / 1000D;
				boolean ambient = Boolean.valueOf(outputElement.getAttribute(AMBIENT)).booleanValue();
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
		boolean result = false;
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		NodeList storageNodes = functionsElement.getElementsByTagName(STORAGE);
		if (storageNodes.getLength() > 0) result = true;
		return result;	
	}
	
	/**
	 * Gets a list of the building's resource capacities. 
	 * @param buildingName the name of the building.
	 * @return list of storage capacities
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public Map<String, Double> getStorageCapacities(String buildingName) throws Exception {
		Map<String, Double> capacities = new HashMap<String, Double>();
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element storageElement = (Element) functionsElement.getElementsByTagName(STORAGE).item(0);
		NodeList resourceStorageNodes = storageElement.getElementsByTagName(RESOURCE_STORAGE);
		for (int x=0; x < resourceStorageNodes.getLength(); x++) {
			Element resourceStorageElement = (Element) resourceStorageNodes.item(x);
			String resource = resourceStorageElement.getAttribute(RESOURCE).toLowerCase();
			Double capacity = new Double(resourceStorageElement.getAttribute(CAPACITY));
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
	public Map<String, Double> getInitialStorage(String buildingName) throws Exception {
		Map<String, Double> resourceMap = new HashMap<String, Double>();
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element storageElement = (Element) functionsElement.getElementsByTagName(STORAGE).item(0);
		NodeList resourceInitialNodes = storageElement.getElementsByTagName(RESOURCE_INITIAL);
		for (int x=0; x < resourceInitialNodes.getLength(); x++) {
			Element resourceInitialElement = (Element) resourceInitialNodes.item(x);
			String resource = resourceInitialElement.getAttribute(RESOURCE).toLowerCase();
			Double amount = new Double(resourceInitialElement.getAttribute(AMOUNT));
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
		boolean result = false;
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		NodeList powerGenerationNodes = functionsElement.getElementsByTagName(POWER_GENERATION);
		if (powerGenerationNodes.getLength() > 0) result = true;
		return result;
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
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element powerGenerationElement = (Element) functionsElement.getElementsByTagName(POWER_GENERATION).item(0);
		NodeList powerSourceNodes = powerGenerationElement.getElementsByTagName(POWER_SOURCE);
		for (int x=0; x < powerSourceNodes.getLength(); x++) {
			Element powerSourceElement = (Element) powerSourceNodes.item(x);
			String type = powerSourceElement.getAttribute(TYPE);
			double power = Double.parseDouble(powerSourceElement.getAttribute(POWER));
			PowerSource powerSource = null;
			if (type.equals(STANDARD_POWER_SOURCE)) powerSource = new StandardPowerSource(power);
			else if (type.equals(SOLAR_POWER_SOURCE)) powerSource = new SolarPowerSource(power);
			else if (type.equals(FUEL_POWER_SOURCE)) {
			    double capacity = Double.parseDouble(powerSourceElement.getAttribute(MAX_CAPACITY));
			    String fuelType = powerSourceElement.getAttribute(FUEL_TYPE);
			    double consumptionSpeed = Double.parseDouble(powerSourceElement.getAttribute(COMSUMPTION_RATE));
			    powerSource = new FuelPowerSource(power, capacity, fuelType, consumptionSpeed);
			}
			    
			else throw new Exception("Power source: " + type + " not a valid power source.");
			powerSourceList.add(powerSource); 
		}
		return powerSourceList;
	}
	
	/**
	 * Checks if building has medical care capability.
	 * @param buildingName the name of the building.
	 * @return true if medical care
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasMedicalCare(String buildingName) throws Exception {
		boolean result = false;
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		NodeList medicalCareNodes = functionsElement.getElementsByTagName(MEDICAL_CARE);
		if (medicalCareNodes.getLength() > 0) result = true;
		return result;
	}
	
	/**
	 * Gets the tech level of the building's medical care.
	 * @param buildingName the name of the building.
	 * @return tech level
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getMedicalCareTechLevel(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element medicalCareElement = (Element) functionsElement.getElementsByTagName(MEDICAL_CARE).item(0);
		return Integer.parseInt(medicalCareElement.getAttribute(TECH_LEVEL));
	}
	
	/**
	 * Gets the number of beds in the building's medical care.
	 * @param buildingName the name of the building.
	 * @return tech level
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getMedicalCareBeds(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element medicalCareElement = (Element) functionsElement.getElementsByTagName(MEDICAL_CARE).item(0);
		return Integer.parseInt(medicalCareElement.getAttribute(BEDS));
	}
	
	/**
	 * Checks if building has the farming function.
	 * @param buildingName the name of the building.
	 * @return true if farming
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasFarming(String buildingName) throws Exception {
		boolean result = false;
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		NodeList farmingNodes = functionsElement.getElementsByTagName(FARMING);
		if (farmingNodes.getLength() > 0) result = true;
		return result;
	}
	
	/**
	 * Gets the number of crops in the building.
	 * @param buildingName the name of the building.
	 * @return number of crops
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getCropNum(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element farmElement = (Element) functionsElement.getElementsByTagName(FARMING).item(0);
		return Integer.parseInt(farmElement.getAttribute(CROPS));
	}
	
	/**
	 * Gets the power required to grow a crop.
	 * @param buildingName the name of the building.
	 * @return power (kW)
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public double getPowerForGrowingCrop(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element farmElement = (Element) functionsElement.getElementsByTagName(FARMING).item(0);
		return Double.parseDouble(farmElement.getAttribute(POWER_GROWING_CROP));
	}
	
	/**
	 * Gets the power required to sustain a crop.
	 * @param buildingName the name of the building.
	 * @return power (kW)
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public double getPowerForSustainingCrop(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element farmElement = (Element) functionsElement.getElementsByTagName(FARMING).item(0);
		return Double.parseDouble(farmElement.getAttribute(POWER_SUSTAINING_CROP));
	}
	
	/**
	 * Gets the crop growing area in the building.
	 * @param buildingName the name of the building.
	 * @return crop growing area (square meters)
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public double getCropGrowingArea(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element farmElement = (Element) functionsElement.getElementsByTagName(FARMING).item(0);
		return Double.parseDouble(farmElement.getAttribute(GROWING_AREA));
	}
	
	/**
	 * Checks if the building has the exercise function.
	 * @param buildingName the name of the building.
	 * @return true if exercise
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasExercise(String buildingName) throws Exception {
		boolean result = false;
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		NodeList exerciseNodes = functionsElement.getElementsByTagName(EXERCISE);
		if (exerciseNodes.getLength() > 0) result = true;
		return result;
	}
	
	/**
	 * Gets the capacity of the exercise facility in the building.
	 * @param buildingName the name of the building.
	 * @return capacity for exercise
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getExerciseCapacity(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element exerciseElement = (Element) functionsElement.getElementsByTagName(EXERCISE).item(0);
		return Integer.parseInt(exerciseElement.getAttribute(CAPACITY));
	}
	
	/**
	 * Checks if the building has the ground vehicle maintenance function.
	 * @param buildingName the name of the building.
	 * @return true if ground vehicle maintenance
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasGroundVehicleMaintenance(String buildingName) throws Exception {
		boolean result = false;
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		NodeList maintenanceNodes = functionsElement.getElementsByTagName(GROUND_VEHICLE_MAINTENANCE);
		if (maintenanceNodes.getLength() > 0) result = true;
		return result;		
	}
	
	/**
	 * Gets the vehicle capacity of the building.
	 * @param buildingName the name of the building.
	 * @return vehicle capacity
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getVehicleCapacity(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element maintenanceElement = (Element) functionsElement.getElementsByTagName(GROUND_VEHICLE_MAINTENANCE).item(0);
		return Integer.parseInt(maintenanceElement.getAttribute(VEHICLE_CAPACITY));
	}
	
	/**
	 * Checks if the building has the cooking function.
	 * @param buildingName the name of the building.
	 * @return true if cooking
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasCooking(String buildingName) throws Exception {
		boolean result = false;
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		NodeList cookingNodes = functionsElement.getElementsByTagName(COOKING);
		if (cookingNodes.getLength() > 0) result = true;
		return result;
	}
	
	/**
	 * Gets the capacity of the cooking facility in the building.
	 * @param buildingName the name of the building.
	 * @return capacity for cooking
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getCookCapacity(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element cookingElement = (Element) functionsElement.getElementsByTagName(COOKING).item(0);
		return Integer.parseInt(cookingElement.getAttribute(CAPACITY));
	}
	
	/**
	 * Checks if the building has the manufacture function.
	 * @param buildingName the name of the building.
	 * @return true if manufacture.
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public boolean hasManufacture(String buildingName) throws Exception {
		boolean result = false;
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		NodeList manufactureNodes = functionsElement.getElementsByTagName(MANUFACTURE);
		if (manufactureNodes.getLength() > 0) result = true;
		return result;
	}
	
	/**
	 * Gets the tech level of the manufacture facility in the building.
	 * @param buildingName the name of the building.
	 * @return tech level.
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getManufactureTechLevel(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element manufactureElement = (Element) functionsElement.getElementsByTagName(MANUFACTURE).item(0);
		return Integer.parseInt(manufactureElement.getAttribute(TECH_LEVEL));
	}
	
	/**
	 * Gets the concurrent process limit of the manufacture facility in the building.
	 * @param buildingName the name of the building.
	 * @return concurrent process limit.
	 * @throws Exception if building name can not be found or XML parsing error.
	 */
	public int getManufactureConcurrentProcesses(String buildingName) throws Exception {
		Element buildingElement = getBuildingElement(buildingName);
		Element functionsElement = (Element) buildingElement.getElementsByTagName(FUNCTIONS).item(0);
		Element manufactureElement = (Element) functionsElement.getElementsByTagName(MANUFACTURE).item(0);
		return Integer.parseInt(manufactureElement.getAttribute(CONCURRENT_PROCESSES));
	}
}
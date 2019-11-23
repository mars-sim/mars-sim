/**
 * Mars Simulation Project
 * SettlementTemplate.java
 * @version 3.1.0 2017-11-04
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.logging.Logger;

import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyMissionTemplate;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;

/**
 * This class defines a template for modeling the initial conditions and building configurations of a settlement. 
 * Called by ConstructionConfig and ResupplyConfig
 */
public class SettlementTemplate implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// * default logger.
	// private static Logger logger = Logger.getLogger(SettlementTemplate.class.getName());

	// Data members.
	private int defaultPopulation;
	private int defaultNumOfRobots;
	private int id;
	
	private String name;
	
	private List<BuildingTemplate> buildings;
	private List<ResupplyMissionTemplate> resupplies;
	
	private Map<String, Integer> vehicles;
	private Map<String, Integer> equipment;
	private Map<AmountResource, Double> resources;
	private Map<Part, Integer> parts;

	/**
	 * Constructor 1. TODO: pending for deletion (use constructor 2 instead)
	 * 
	 * @param name
	 * @param defaultPopulation
	 * @param defaultNumOfRobots
	 */
	public SettlementTemplate(String name, int defaultPopulation, int defaultNumOfRobots) {
		this.name = name;
		this.defaultPopulation = defaultPopulation;
		this.defaultNumOfRobots = defaultNumOfRobots;
		
		buildings = new ArrayList<BuildingTemplate>();
		vehicles = new HashMap<String, Integer>();
		equipment = new HashMap<String, Integer>();
		resources = new HashMap<AmountResource, Double>();
		parts = new HashMap<Part, Integer>();
		resupplies = new ArrayList<ResupplyMissionTemplate>();
	}

	/**
	 * Constructor 2. Called by SettlementConfig.java
	 * 
	 * @param name
	 * @param id
	 * @param defaultPopulation
	 * @param defaultNumOfRobots
	 */
	public SettlementTemplate(String name, int id, int defaultPopulation, int defaultNumOfRobots) {
		this.name = name;
		this.id = id;
		this.defaultPopulation = defaultPopulation;
		this.defaultNumOfRobots = defaultNumOfRobots;

		buildings = new ArrayList<BuildingTemplate>();
		vehicles = new HashMap<String, Integer>();
		equipment = new HashMap<String, Integer>();
		resources = new HashMap<AmountResource, Double>();
		parts = new HashMap<Part, Integer>();
		resupplies = new ArrayList<ResupplyMissionTemplate>();
	}

	/**
	 * Gets the name of the template.
	 * 
	 * @return name.
	 */
	public String getTemplateName() {
		return name;
	}

	/**
	 * Gets the template's unique ID.
	 * 
	 * @return ID number.
	 */
	public int getID() {
		return id;
	}

	/**
	 * Gets the default robot capacity of the template.
	 * 
	 * @return robot capacity.
	 */
	public int getDefaultNumOfRobots() {
		return defaultNumOfRobots;
	}

	/**
	 * Gets the default population capacity of the template.
	 * 
	 * @return population capacity.
	 */
	public int getDefaultPopulation() {
		return defaultPopulation;
	}

	/**
	 * Adds a building template.
	 * 
	 * @param buildingTemplate the building template.
	 */
	public void addBuildingTemplate(BuildingTemplate buildingTemplate) {
		buildings.add(buildingTemplate);
	}

	/**
	 * Gets the list of building templates.
	 * 
	 * @return list of building templates.
	 */
	public List<BuildingTemplate> getBuildingTemplates() {
		return new ArrayList<BuildingTemplate>(buildings);
	}

	/**
	 * Adds a number of vehicles of a given type.
	 * 
	 * @param vehicleType the vehicle type.
	 * @param number      the number of vehicles to add.
	 */
	public void addVehicles(String vehicleType, int number) {
		if (vehicles.containsKey(vehicleType)) {
			number += vehicles.get(vehicleType);
		}
		vehicles.put(vehicleType, number);
	}

	/**
	 * Gets a map of vehicle types and number.
	 * 
	 * @return map.
	 */
	public Map<String, Integer> getVehicles() {
		return new HashMap<String, Integer>(vehicles);
	}

	/**
	 * Adds a number of equipment of a given type.
	 * 
	 * @param equipmentType the equipment type.
	 * @param number        the number of equipment to add.
	 */
	public void addEquipment(String equipmentType, int number) {
		if (equipment.containsKey(equipmentType)) {
			number += equipment.get(equipmentType);
		}
		equipment.put(equipmentType, number);
	}

	/**
	 * Gets a map of equipment types and number.
	 * 
	 * @return map.
	 */
	public Map<String, Integer> getEquipment() {
		return new HashMap<String, Integer>(equipment);
	}

	/**
	 * Adds an amount of a type of resource.
	 * 
	 * @param resource the resource.
	 * @param amount   the amount (kg).
	 */
	public void addAmountResource(AmountResource resource, double amount) {
		if (resources.containsKey(resource)) {
			amount += resources.get(resource);
		}
		resources.put(resource, amount);
	}

	/**
	 * Gets a map of resources and amounts.
	 * 
	 * @return map.
	 */
	public Map<AmountResource, Double> getResources() {
		return new HashMap<AmountResource, Double>(resources);
	}

	/**
	 * Adds a number of a type of part.
	 * 
	 * @param part   the part.
	 * @param number the number of parts.
	 */
	public void addPart(Part part, int number) {
		if (parts.containsKey(part)) {
			number += parts.get(part);
		}
		parts.put(part, number);
	}

	/**
	 * Gets a map of parts and numbers.
	 * 
	 * @return map.
	 */
	public Map<Part, Integer> getParts() {
		return new HashMap<Part, Integer>(parts);
	}

	/**
	 * Adds a resupply mission template.
	 * 
	 * @param resupplyMissionTemplate the resupply mission template.
	 */
	public void addResupplyMissionTemplate(ResupplyMissionTemplate resupplyMissionTemplate) {
		resupplies.add(resupplyMissionTemplate);
	}

	/**
	 * Gets the list of resupply mission templates.
	 * 
	 * @return list of resupply mission templates.
	 */
	public List<ResupplyMissionTemplate> getResupplyMissionTemplates() {
		return new ArrayList<ResupplyMissionTemplate>(resupplies);
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		name = null;
		buildings.clear();
		buildings = null;
		vehicles.clear();
		vehicles = null;
		equipment.clear();
		equipment = null;
		resources.clear();
		resources = null;
		parts.clear();
		parts = null;
		resupplies.clear();
		resupplies = null;
	}
}
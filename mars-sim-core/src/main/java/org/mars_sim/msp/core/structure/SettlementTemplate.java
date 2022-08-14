/**
 * Mars Simulation Project
 * SettlementTemplate.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.configuration.UserConfigurable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyMissionTemplate;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;

/**
 * This class defines a template for modeling the initial conditions and building configurations of a settlement. 
 * Called by ConstructionConfig and ResupplyConfig
 */
public class SettlementTemplate implements Serializable, UserConfigurable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private int defaultPopulation;
	private int defaultNumOfRobots;
	private int id;
	
	private String name;
	private String sponsor;
	
	private List<BuildingTemplate> buildings;
	private List<ResupplyMissionTemplate> resupplies;
	
	private Map<String, Integer> vehicles;
	private Map<String, Integer> equipment;
	private Map<AmountResource, Double> resources;
	private Map<Part, Integer> parts;

	private String description;

	private boolean bundled;


	/**
	 * Constructor. Called by SettlementConfig.java
	 * 
	 * @param name
	 * @param id
	 * @param defaultPopulation
	 * @param defaultNumOfRobots
	 */
	public SettlementTemplate(int id, String name, String desription, boolean bundled,
								String sponsor, int defaultPopulation, int defaultNumOfRobots) {
		this.id = id;
		this.name = name;
		this.description = desription;
		this.bundled = bundled;
		this.sponsor = sponsor;
		this.defaultPopulation = defaultPopulation;
		this.defaultNumOfRobots = defaultNumOfRobots;

		buildings = new ArrayList<>();
		vehicles = new HashMap<>();
		equipment = new HashMap<>();
		resources = new HashMap<>();
		parts = new HashMap<>();
		resupplies = new ArrayList<>();
	}

	/**
	 * Gets the name of the template.
	 * 
	 * @return name.
	 */
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Is this template bundled with the simualtion build?
	 */
	@Override
	public boolean isBundled() {
		return bundled;
	}

	/**
	 * Gets the name of the template.
	 * 
	 * @return name.
	 */
	public String toString() {
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
	 * Get the Reporting Authority code that defines this template.
	 * @return
	 */
	public String getSponsor() {
		return sponsor;
	}
	
	/**
	 * Adds a building template.
	 * 
	 * @param buildingTemplate the building template.
	 */
	void addBuildingTemplate(BuildingTemplate buildingTemplate) {
		buildings.add(buildingTemplate);
	}

	/**
	 * Gets the list of building templates.
	 * 
	 * @return list of building templates.
	 */
	public List<BuildingTemplate> getBuildingTemplates() {
		return Collections.unmodifiableList(buildings);
	}

	/**
	 * Adds a number of vehicles of a given type.
	 * 
	 * @param vehicleType the vehicle type.
	 * @param number      the number of vehicles to add.
	 */
	void addVehicles(String vehicleType, int number) {
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
		return Collections.unmodifiableMap(vehicles);
	}

	/**
	 * Adds a number of equipment of a given type.
	 * 
	 * @param equipmentType the equipment type.
	 * @param number        the number of equipment to add.
	 */
	void addEquipment(String equipmentType, int number) {
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
		return Collections.unmodifiableMap(equipment);
	}

	/**
	 * Adds an amount of a type of resource.
	 * 
	 * @param resource the resource.
	 * @param amount   the amount (kg).
	 */
	void addAmountResource(AmountResource resource, double amount) {
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
		return Collections.unmodifiableMap(resources);
	}

	/**
	 * Adds a number of a type of part.
	 * 
	 * @param part   the part.
	 * @param number the number of parts.
	 */
	void addPart(Part part, int number) {
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
		return Collections.unmodifiableMap(parts);
	}

	/**
	 * Adds a resupply mission template.
	 * 
	 * @param resupplyMissionTemplate the resupply mission template.
	 */
	void addResupplyMissionTemplate(ResupplyMissionTemplate resupplyMissionTemplate) {
		resupplies.add(resupplyMissionTemplate);
	}

	/**
	 * Gets the list of resupply mission templates.
	 * 
	 * @return list of resupply mission templates.
	 */
	public List<ResupplyMissionTemplate> getResupplyMissionTemplates() {
		return Collections.unmodifiableList(resupplies);
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

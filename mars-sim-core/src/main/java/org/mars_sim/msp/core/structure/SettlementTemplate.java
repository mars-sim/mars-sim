/*
 * Mars Simulation Project
 * SettlementTemplate.java
 * @date 2022-09-15
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
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplySchedule;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;

/**
 * This class defines a template for modeling the initial conditions and building configurations of a settlement. 
 * Called by ConstructionConfig and ResupplyConfig
 */
public class SettlementTemplate implements Serializable, UserConfigurable, SettlementSupplies {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private int defaultPopulation;
	private int defaultNumOfRobots;
	
	private String name;
	private String sponsor;
	
	private List<BuildingTemplate> buildings;
	private List<ResupplySchedule> resupplies;
	
	private Map<String, Integer> vehicles;
	private Map<String, Integer> equipment;
	private Map<String, Integer> bins;
	private Map<AmountResource, Double> resources;
	private Map<Part, Integer> parts;

	private String description;

	private boolean bundled;

	private ShiftPattern shiftDefn;

	private List<RobotTemplate> robots;


	/**
	 * Constructor. Called by SettlementConfig.java
	 * 
	 * @param name
	 * @param pattern
	 * @param defaultPopulation
	 * @param defaultNumOfRobots
	 */
	public SettlementTemplate(String name, String desription, boolean bundled,
								String sponsor, ShiftPattern shiftDefn, int defaultPopulation, int defaultNumOfRobots) {
		this.name = name;
		this.description = desription;
		this.bundled = bundled;
		this.sponsor = sponsor;
		this.defaultPopulation = defaultPopulation;
		this.defaultNumOfRobots = defaultNumOfRobots;
		this.shiftDefn = shiftDefn;

		buildings = new ArrayList<>();
		vehicles = new HashMap<>();
		bins = new HashMap<>();
		equipment = new HashMap<>();
		resources = new HashMap<>();
		parts = new HashMap<>();
		resupplies = new ArrayList<>();
		robots = new ArrayList<>();
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

	/**
	 * Gets the description of the template.
	 * 
	 * @return description.
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Is this template bundled with the simulation build?
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
	 * Gets the Shift definition for this settlement.
	 * 
	 * @return ShiftPattern
	 */
	public ShiftPattern getShiftDefinition() {
		return shiftDefn;
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
	 * Adds the robot template.
	 * 
	 * @param newRobot
	 */
	void addRobot(RobotTemplate newRobot) {
		robots.add(newRobot);
	}

	/**
	 * Gets a list of robot templates.
	 * 
	 * @return
	 */
	public List<RobotTemplate> getPredefinedRobots() {
		return Collections.unmodifiableList(robots);
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
	 * Gets the Reporting Authority code that defines this template.
	 * 
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
	@Override
	public List<BuildingTemplate> getBuildings() {
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
	@Override
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
	 * Adds a number of bins of a given type.
	 * 
	 * @param binType the bin type.
	 * @param number        the number of bins to add.
	 */
	void addBins(String binType, int number) {
		if (bins.containsKey(binType)) {
			number += bins.get(binType);
		}
		bins.put(binType, number);
	}
	
	
	/**
	 * Gets a map of equipment types and number.
	 * 
	 * @return map.
	 */
	@Override
	public Map<String, Integer> getEquipment() {
		return Collections.unmodifiableMap(equipment);
	}

	/**
	 * Gets a map of bin types and number.
	 * 
	 * @return map.
	 */
	@Override
	public Map<String, Integer> getBins() {
		return Collections.unmodifiableMap(bins);
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
	@Override
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
	@Override
	public Map<Part, Integer> getParts() {
		return Collections.unmodifiableMap(parts);
	}

	/**
	 * Adds a resupply mission template.
	 * 
	 * @param resupplyMissionTemplate the resupply mission template.
	 */
	void addResupplyMissionTemplate(ResupplySchedule resupplyMissionTemplate) {
		resupplies.add(resupplyMissionTemplate);
	}

	/**
	 * Gets the list of resupply mission templates.
	 * 
	 * @return list of resupply mission templates.
	 */
	public List<ResupplySchedule> getResupplyMissionTemplates() {
		return Collections.unmodifiableList(resupplies);
	}
}

/*
 * Mars Simulation Project
 * SettlementTemplate.java
 * @date 2022-09-15
 * @author Scott Davis
 */
package com.mars_sim.core.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mars_sim.core.configuration.UserConfigurable;
import com.mars_sim.core.interplanetary.transport.resupply.ResupplySchedule;
import com.mars_sim.core.person.ai.shift.ShiftPattern;
import com.mars_sim.core.robot.RobotTemplate;

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
	
	private String name;
	private String sponsor;
	
	private List<ResupplySchedule> resupplies = new ArrayList<>();
	
	private SettlementSupplies supplies;

	private String description;

	private boolean bundled;

	private ShiftPattern shiftDefn;

	private List<RobotTemplate> robots = new ArrayList<>();

	private ObjectiveType objective = ObjectiveType.CROP_FARM;

	private GroupActivitySchedule activitySchedule;

	/**
	 * Constructor. Called by SettlementConfig.java
	 * 
	 * @param name
	 * @param activitySchedule
	 * @param defaultPopulation
	 * @param defaultNumOfRobots
	 */
	public SettlementTemplate(String name, String desription, boolean bundled,
								String sponsor, ShiftPattern shiftDefn,
								GroupActivitySchedule activitySchedule,
								int defaultPopulation, int defaultNumOfRobots,
								SettlementSupplies supplies) {
		this.name = name;
		this.description = desription;
		this.bundled = bundled;
		this.sponsor = sponsor;
		this.defaultPopulation = defaultPopulation;
		this.defaultNumOfRobots = defaultNumOfRobots;
		this.shiftDefn = shiftDefn;
		this.activitySchedule = activitySchedule;
		this.supplies = supplies;
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
	 * Gets the Activity scheduled for this settlement.
	 * 
	 * @return ShiftPattern
	 */
	public GroupActivitySchedule getActivitySchedule() {
		return activitySchedule;
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
	
	public SettlementSupplies getSupplies() {
		return supplies;
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

	void setObjective(ObjectiveType objective) {
		this.objective = objective;
	}
	
	/**
	 * Get the optional objective type for this settlement.
	 * @return
	 */
    public ObjectiveType getObjective() {
        return objective;
    }
}

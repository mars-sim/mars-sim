/*
 * Mars Simulation Project
 * RobotSpec.java
 * @date 2023-04-11
 * @author Manny Kung
 */
package com.mars_sim.core.robot;

import java.io.Serializable;
import java.util.Map;

import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;

/** 
 * The Specification of a robot loaded from the external configuration.
 */
public class RobotSpec implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	private RobotType robotType;
	private String makeModel;
	
	private Map<NaturalAttributeType, Integer> attributeMap;
	private Map<SkillType, Integer> skillMap;

	private double rate;
	private double standbyPower;
	private double lowPowerModePercent;
	private double maxCapacity;

	private int height;
	private int mass;
	
	private String description;

	public RobotSpec(RobotType robotType, String makeModel, String description,
			double standbyPower, double rate, double lowPowerMode, double maxCapacity,
			int height, int mass,
			Map<NaturalAttributeType, Integer> attributeMap, Map<SkillType, Integer> skillMap) {
		this.robotType = robotType;
		this.makeModel = makeModel;
		this.description = description;
		this.attributeMap = attributeMap;
		this.skillMap = skillMap;
		this.standbyPower = standbyPower;
		this.lowPowerModePercent = lowPowerMode;
		this.rate = rate;
		this.maxCapacity = maxCapacity;
		this.height = height;
		this.mass = mass;
	}

	/**
	 * Gets the robot type.
	 * 
	 * @return
	 */
	public final RobotType getRobotType() {
		return robotType;
	}
	
	/**
	 * Gets the Make & Model value of this Robot Spec
	 * 
	 * @return
	 */
	public final String getMakeModel() {
		return makeModel;
	}
	
	/**
	 * Gets the description.
	 * 
	 * @return
	 */
	public final String getDescription() {
		return description;
	}
	
	/**
	 * Get the stand by power consumption
	 */
	public double getStandbyPowerConsumption() {
		return standbyPower;
	}

	/**
	 * Get the consumption rate for this Robot type.
	 */
	public double getConsumptionRate() {
		return rate;
	}

	/**
	 * Gets the percentage that triggers the low power mode for this robot model.
	 * 
	 * @return
	 */
	public double getLowPowerModePercent() {
		return lowPowerModePercent;
	}

	/**
	 * Gets the attribute map.
	 * 
	 * @return
	 */
	public final Map<NaturalAttributeType, Integer> getAttributeMap() {
		return attributeMap;
	}

	/**
	 * Gets the skill map.
	 * 
	 * @return
	 */
	public final Map<SkillType, Integer> getSkillMap() {
		return skillMap;
	}

	/**
	 * Gets the maximum battery capacity of this robot.
	 * 
	 * @return
	 */
    public double getMaxCapacity() {
        return maxCapacity;
    }

	public int getMass() {
		return mass;
	}

    public int getHeight() {
        return height;
    }
}

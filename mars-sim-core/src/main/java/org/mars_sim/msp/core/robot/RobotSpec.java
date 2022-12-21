/*
 * Mars Simulation Project
 * RobotSpec.java
 * @date 2022-06-16
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot;

import java.io.Serializable;
import java.util.Map;

import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;

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

	private double fcr;
	private double standbyPower;
	private double lowPowerMode;

	public RobotSpec(RobotType robotType, String makeModel,
			double standbyPower, double fcr, double lowPowerMode,
			Map<NaturalAttributeType, Integer> attributeMap, Map<SkillType, Integer> skillMap) {
		this.robotType = robotType;
		this.makeModel = makeModel;
		this.attributeMap = attributeMap;
		this.skillMap = skillMap;
		this.standbyPower = standbyPower;
		this.lowPowerMode = lowPowerMode;
		this.fcr = fcr;
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
	 * Get the stand by power consumption
	 */
	public double getStandbyPowerConsumption() {
		return standbyPower;
	}

	/**
	 * Get teh fuel consumption for this Robot type.
	 */
	public double getFuelConsumption() {
		return fcr;
	}

	/**
	 * When is the low power mode enabked for this robot model
	 * @return
	 */
	public double getLowPowerModePercent() {
		return lowPowerMode;
	}

	/**
	 * Gets the attpribute map.
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
}

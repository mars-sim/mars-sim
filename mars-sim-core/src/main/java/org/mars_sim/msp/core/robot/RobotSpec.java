/*
 * Mars Simulation Project
 * RobotSpec.java
 * @date 2022-06-16
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot;

import java.io.Serializable;
import java.util.Map;

/** 
 * The Specification of a robot loaded from the external configuration.
 */
public class RobotSpec implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	private String name;
	private RobotType robotType;
	private String jobName;
	private String settlementName;
	
	private Map<String, Double> attributeMap;
	private Map<String, Integer> skillMap;

//	private double powerConsumptionRate;
//	private double fuelConsumptionRate;
//	private double lowPowerModeStartTime;
//	private double minAirPressure;
//	private double minTemperature;
//	private double maxTemperature;
//	private double freezingTime;
	
	public RobotSpec(String name, RobotType robotType, String settlementName, String jobName) {
		this.name = name;
		this.robotType = robotType;
		this.settlementName = settlementName;
		this.jobName = jobName;
	}

	/**
	 * Gets the name.
	 * 
	 * @return
	 */
	public final String getName() {
		return name;
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
	 * Gets the settlement name.
	 * 
	 * @return
	 */
	public final String getSettlementName() {
		return settlementName;
	}
	
	/**
	 * Gets the robot job name.
	 * 
	 * @return
	 */
	public final String getJobName() {
		return jobName;
	}
	
	/**
	 * Gets the attribute map.
	 * 
	 * @return
	 */
	public final Map<String, Double> getAttributeMap() {
		return attributeMap;
	}

	/**
	 * Gets the skill map.
	 * 
	 * @return
	 */
	public final Map<String, Integer> getSkillMap() {
		return skillMap;
	}
}

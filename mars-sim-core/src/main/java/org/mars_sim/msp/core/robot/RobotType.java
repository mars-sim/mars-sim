/*
 * Mars Simulation Project
 * RobotType.java
 * @date 2021-12-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

public enum RobotType {

	CHEFBOT				(Msg.getString("RobotType.chefBot")), //$NON-NLS-1$
	CONSTRUCTIONBOT		(Msg.getString("RobotType.constructionBot")), //$NON-NLS-1$
	DELIVERYBOT			(Msg.getString("RobotType.deliveryBot")), //$NON-NLS-1$ )
	GARDENBOT			(Msg.getString("RobotType.gardenBot")), //$NON-NLS-1$
	MAKERBOT			(Msg.getString("RobotType.makerBot")), //$NON-NLS-1$
	MEDICBOT			(Msg.getString("RobotType.medicBot")), //$NON-NLS-1$
	REPAIRBOT			(Msg.getString("RobotType.repairBot")), //$NON-NLS-1$
	UNKNOWN				(Msg.getString("unknown")); //$NON-NLS-1$

	private String name;

	/** hidden constructor. */
	private RobotType(String name) {
		this.name = name;
	}

	/**
	 * Returns the internationalized name for display in user interface.
	 *
	 * @return {@link String}
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the display name.
	 *
	 * @return {@link String}
	 */
	public String getDisplayName() {
		return this.name;
	}

	public static RobotType valueOfIgnoreCase(String s) {
		return valueOf(s.toUpperCase().replace(' ', '_'));
	}

	/**
	 * Gets an array of internationalized robot type in alphabetical order.
	 * 
	 * @return a string array
	 */
	public static String[] getNames() {
		List<String> list = new ArrayList<String>();
		for (RobotType value : RobotType.values()) {
			list.add(value.getName());
		}
		Collections.sort(list);
		return list.toArray(new String[] {});
	}

	/**
	 * What is the default Function for a Robot Type.
	 * Not sure this is the best place for this implementation.
	 * 
	 * @return FunctionType
	 */
	public FunctionType getDefaultFunction() {
		switch (this) {
		case CHEFBOT:
			return FunctionType.COOKING;
		
		case CONSTRUCTIONBOT:
			return FunctionType.MANUFACTURE;
			
		case DELIVERYBOT:
			return FunctionType.GROUND_VEHICLE_MAINTENANCE;
			
		case GARDENBOT:
			return FunctionType.FARMING;
			
		case MAKERBOT:
			return FunctionType.MANUFACTURE;
			
		case MEDICBOT:
			return FunctionType.MEDICAL_CARE;
			
		case REPAIRBOT:
			return FunctionType.LIFE_SUPPORT;
			
		default:
			return FunctionType.ROBOTIC_STATION;
		}
	}
}

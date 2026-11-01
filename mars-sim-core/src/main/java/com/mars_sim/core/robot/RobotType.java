/*
 * Mars Simulation Project
 * RobotType.java
 * @date 2022-06-21
 * @author Manny Kung
 */
package com.mars_sim.core.robot;

import com.mars_sim.core.resource.ResourceType;
import com.mars_sim.core.tool.Msg;

public enum RobotType {

	CHEFBOT				(Msg.getString("RobotType.chefBot")), //$NON-NLS-1$
	CONSTRUCTIONBOT		(Msg.getString("RobotType.constructionBot")), //$NON-NLS-1$
	DELIVERYBOT			(Msg.getString("RobotType.deliveryBot")), //$NON-NLS-1$ )
	GARDENBOT			(Msg.getString("RobotType.gardenBot")), //$NON-NLS-1$
	MAKERBOT			(Msg.getString("RobotType.makerBot")), //$NON-NLS-1$
	MEDICBOT			(Msg.getString("RobotType.medicBot")), //$NON-NLS-1$
	REPAIRBOT			(Msg.getString("RobotType.repairBot")), //$NON-NLS-1$
	;
	
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
	 * Get the global Resource ID for this robot type.
	 * @return The global Resource ID for this robot type.
	 */
	public int getResourceID() {
		return ordinal() + ResourceType.FIRST_ROBOT_RESOURCE_ID;
	}

	/**
	 * Convert an robot type to the associated resourceID.
	 * Use the RobotType.getResourceID() method to get the resource ID for a specific robot type.
	 */
	public static int getResourceID(RobotType type) {
		return type.getResourceID();
	}

	/**
	 * Obtains the enum type of the robot with its type id.
	 */
	public static RobotType convertID2Type(int resourceId) {
		return RobotType.values()[resourceId - ResourceType.FIRST_ROBOT_RESOURCE_ID];
	}	
}

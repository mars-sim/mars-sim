/*
 * Mars Simulation Project
 * RobotType.java
 * @date 2022-06-21
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.resource.ResourceUtil;

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
	 * Returns the display name.
	 *
	 * @return {@link String}
	 */
	public String getDisplayName() {
		return this.name;
	}

	/**
	 * Convert an robot type to the associated resourceID.
	 * Note : Needs revisiting. Equipment should be referenced by the RobotType enum.
	 * 
	 * @return
	 */
	public static int getResourceID(RobotType type) {
		return type.ordinal() + ResourceUtil.FIRST_ROBOT_RESOURCE_ID;
	}
	
}

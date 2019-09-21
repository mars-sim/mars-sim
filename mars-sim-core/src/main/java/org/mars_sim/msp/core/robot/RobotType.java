/**
 * Mars Simulation Project
 * RobotType.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mars_sim.msp.core.Msg;

public enum RobotType {

	CHEFBOT(Msg.getString("RobotType.chefBot")), //$NON-NLS-1$
	CONSTRUCTIONBOT(Msg.getString("RobotType.constructionBot")), //$NON-NLS-1$
	DELIVERYBOT(Msg.getString("RobotType.deliveryBot")), //$NON-NLS-1$ )
	GARDENBOT(Msg.getString("RobotType.gardenBot")), //$NON-NLS-1$
	MAKERBOT(Msg.getString("RobotType.makerBot")), //$NON-NLS-1$
	MEDICBOT(Msg.getString("RobotType.medicBot")), //$NON-NLS-1$
	REPAIRBOT(Msg.getString("RobotType.repairBot")), //$NON-NLS-1$
	UNKNOWN(Msg.getString("unknown")); //$NON-NLS-1$

	private String name;

	/** hidden constructor. */
	private RobotType(String name) {
		this.name = name;
	}

	/**
	 * an internationalized translation for display in user interface.
	 * 
	 * @return {@link String}
	 */
	public String getName() {
		return this.name;
	}

	public String getDisplayName() {
		return this.name;
		// return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
	}

	public static RobotType valueOfIgnoreCase(String s) {
		return valueOf(s.toUpperCase().replace(' ', '_'));
	}

	/**
	 * Gets an array of internationalized robottype in alphabetical order.
	 */
	public static String[] getNames() {
		List<String> list = new ArrayList<String>();
		for (RobotType value : RobotType.values()) {
			list.add(value.getName());
		}
		Collections.sort(list);
		return list.toArray(new String[] {});
	}

}

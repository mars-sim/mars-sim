/*
 * Mars Simulation Project
 * HeatMode.java
 * @date 2024-07-03
 * @author Manny Kung
 */

package com.mars_sim.core.building.utility.heating;

import java.util.List;

import com.mars_sim.core.tool.Msg;

public enum HeatMode {

	HEAT_OFF (0),
	ONE_EIGHTH_HEAT (12.5),
	QUARTER_HEAT (25),
	THREE_EIGHTH_HEAT (37.5),
	HALF_HEAT (50),
	FIVE_EIGHTH_HEAT (62.5),
	THREE_QUARTER_HEAT (75),
	SEVEN_EIGHTH_HEAT (87.5),
	FULL_HEAT (100),
	OFFLINE (0);

	private String name;
	private double percentage;

	/** 
	 * Hidden constructor. 
	 */
	private HeatMode(double percentage) {
		this.name = Msg.getStringOptional("HeatType", name());
		this.percentage = percentage;
	}

	/**
	 * Returns a list of all heat modes.
	 */
	public static final List<HeatMode> ALL_HEAT_MODES = 
		List.of(HeatMode.HEAT_OFF, HeatMode.ONE_EIGHTH_HEAT, 
				HeatMode.QUARTER_HEAT, HeatMode.THREE_EIGHTH_HEAT, 
				HeatMode.HALF_HEAT, HeatMode.FIVE_EIGHTH_HEAT, 
				HeatMode.THREE_QUARTER_HEAT, HeatMode.SEVEN_EIGHTH_HEAT, 
				HeatMode.FULL_HEAT, HeatMode.OFFLINE);

	/** 
	 * Returns an internationalized {@link String} for display in user interface. 
	 */
	public String getName() {
		return this.name;
	}
	
	public double getPercentage() {
		return this.percentage;
	}
}

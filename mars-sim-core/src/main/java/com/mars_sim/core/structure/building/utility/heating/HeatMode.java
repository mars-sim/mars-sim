/*
 * Mars Simulation Project
 * HeatMode.java
 * @date 2024-07-03
 * @author Manny Kung
 */

package com.mars_sim.core.structure.building.utility.heating;

import java.util.List;

import com.mars_sim.core.tool.Msg;

public enum HeatMode {

	HEAT_OFF (Msg.getString("HeatMode.heatOff"), 0), //$NON-NLS-1$
	ONE_EIGHTH_HEAT (Msg.getString("HeatMode.oneEighthHeat"), 12.5), //$NON-NLS-1$
	QUARTER_HEAT (Msg.getString("HeatMode.quarterHeat"), 25), //$NON-NLS-1$
	THREE_EIGHTH_HEAT (Msg.getString("HeatMode.threeEighthHeat"), 37.5), //$NON-NLS-1$
	HALF_HEAT (Msg.getString("HeatMode.halfHeat"), 50), //$NON-NLS-1$
	FIVE_EIGHTH_HEAT (Msg.getString("HeatMode.fiveEighthHeat"), 62.5), //$NON-NLS-1$
	THREE_QUARTER_HEAT (Msg.getString("HeatMode.threeQuarterHeat"), 75), //$NON-NLS-1$
	SEVEN_EIGHTH_HEAT (Msg.getString("HeatMode.sevenEighthHeat"), 87.5), //$NON-NLS-1$
	FULL_HEAT (Msg.getString("HeatMode.fullHeat"), 100), //$NON-NLS-1$
	OFFLINE (Msg.getString("HeatMode.offline"), 0); //$NON-NLS-1$

	private String name;
	private double percentage;

	/** 
	 * Hidden constructor. 
	 */
	private HeatMode(String name, double percentage) {
		this.name = name;
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

/*
 * Mars Simulation Project
 * MineralConcentration.java
 * @date 2022-07-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.environment;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;

/**
 * Internal class representing a mineral concentration.
 */
public class MineralConcentration implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** The center coordinate of this site. */
	private Coordinates location;
	/** The mineral concentration in percentage (0% - 100%). */
	private double concentration;
	/** The string name of this mineral. */
	private String mineralType;

	public MineralConcentration(Coordinates location, double concentration, String mineralType) {
		this.location = location;
		this.concentration = concentration;
		this.mineralType = mineralType;
	}

	public Coordinates getLocation() {
		return location;
	}

	public double getConcentration() {
		return concentration;
	}

	public String getMineralType() {
		return mineralType;
	}
	
	public void destroy() {
		location = null;
	}
}

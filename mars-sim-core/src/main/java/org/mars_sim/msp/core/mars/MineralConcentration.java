package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;

/**
 * Internal class representing a mineral concentration.
 */
//@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = As.PROPERTY, property = "@class")
public class MineralConcentration implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Coordinates location;
	private double concentration;
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
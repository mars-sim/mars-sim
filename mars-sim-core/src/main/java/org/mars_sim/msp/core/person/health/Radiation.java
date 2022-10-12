/*
 * Mars Simulation Project
 * Radiation.java
 * @date 2022-09-24
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.health;

import java.io.Serializable;

public class Radiation implements Serializable {

	/** default serial id.*/
	private static final long serialVersionUID = 1L;

	private RadiationType type;
	private BodyRegionType region;
	private double amount;

	/**
	 * Create a radiation hit.
	 * 
	 * @param region {@link BodyRegionType} the body region affected.
	 * @param amount the dosage amount.
	 */
	public Radiation(RadiationType radiationType, BodyRegionType region, double amount) {
		this.type = radiationType;
		this.region = region;
		this.amount = amount;
	}

	public double getAmount() {
		return amount;
	}

	public BodyRegionType getBodyRegion() {
		return region;
	}
	
	public RadiationType getradiationType() {
		return type;
	}
	
	@Override
	public String toString() {
		String typeStr = type.getName();
		String regionStr = region.getName();
		return typeStr + " - " + regionStr + " - Dose: " + Math.round(amount * 10_000.0)/10_000.0;
	}
}

/*
 * Mars Simulation Project
 * PreparedDessert.java
 * @date 2022-08-30
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;

import org.mars_sim.msp.core.time.MarsTime;

/**
 * This class represents servings of prepared dessert from a kitchen.
 */
public class PreparedDessert implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * The time (millisols) between when the dessert is made and when it expires.
	 */
	private static final double SHELF_LIFE = 500D;

	// Data members
	private double quality;
	private double dryMass;

	private String name;

	private MarsTime expirationTime;

	/**
	 * Constructor.
	 * 
	 * @param quality      the quality of the dessert
	 * @param creationTime the time the dessert was cooked.
	 */
	public PreparedDessert(String name, double quality, double dryMass, MarsTime creationTime) {
		this.quality = quality;
		this.name = name;
		this.dryMass = dryMass;
		expirationTime = creationTime.addTime(SHELF_LIFE);
	}

	/**
	 * Gets the name of the dessert.
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the quality of the dessert.
	 * 
	 * @return quality
	 */
	public double getQuality() {
		return quality;
	}

	/**
	 * Gets the dry mass of the meal.
	 * 
	 * @return dry mass
	 */
	public double getDryMass() {
		return dryMass;
	}

	/**
	 * Gets the expiration time of the meal.
	 * 
	 * @return expiration time
	 */
	public MarsTime getExpirationTime() {
		return expirationTime;
	}
}

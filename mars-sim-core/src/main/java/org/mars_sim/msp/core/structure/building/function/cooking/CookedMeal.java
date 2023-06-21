/*
 * Mars Simulation Project
 * CookedMeal.java
 * @date 2022-08-30
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import org.mars_sim.msp.core.time.MarsTime;
import java.io.Serializable;

/**
 * This class represents a cooked meal from a kitchen.
 */
public class CookedMeal implements Serializable, Cloneable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** The time (millisols) between when a meal is cooked and when it expires. */
	private static final double SHELF_LIFE = 150D; // note: 100 mSol ~= 2.5 hrs

	// Data members
	private double quality;
	private double dryMass;

	private String mealName;
	private MarsTime expirationTime;

	/**
	 * Constructor.
	 * 
	 * @param quality      the quality of the food
	 * @param creationTime the time the food was cooked.
	 */
	public CookedMeal(String mealName, double quality, double dryMass, MarsTime creationTime) {
		this.quality = quality;
		this.mealName = mealName;
		this.dryMass = dryMass;
		expirationTime = creationTime.addTime(SHELF_LIFE);
	}

	public String getName() {
		return mealName;
	}

	/**
	 * Gets the quality of the meal.
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

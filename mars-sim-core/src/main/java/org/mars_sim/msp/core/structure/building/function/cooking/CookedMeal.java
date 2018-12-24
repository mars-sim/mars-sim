/**
 * Mars Simulation Project
 * CookedMeal.java
 * @version 3.1.0 2017-04-26
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import org.mars_sim.msp.core.time.MarsClock;

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
	private String producerName;
	private String consumerName;

	private Cooking kitchen;
	private MarsClock expirationTime;

	/**
	 * Constructor.
	 * 
	 * @param quality      the quality of the food
	 * @param creationTime the time the food was cooked.
	 */
	public CookedMeal(String mealName, double quality, double dryMass, MarsClock creationTime, String producerName,
			Cooking kitchen) {
		this.quality = quality;
		this.mealName = mealName;
		this.dryMass = dryMass;
		expirationTime = (MarsClock) creationTime.clone();
		expirationTime.addTime(SHELF_LIFE);
		this.producerName = producerName;
		this.kitchen = kitchen;
	}

	// constructor
	public CookedMeal(CookedMeal cookedMeal, String consumerName) {
		this.quality = cookedMeal.quality;
		this.mealName = cookedMeal.mealName;
		this.expirationTime = cookedMeal.expirationTime;
		this.consumerName = consumerName;
	}

	// copy constructor
	public CookedMeal(CookedMeal cookedMeal) {
		this.quality = cookedMeal.quality;
		this.mealName = cookedMeal.mealName;
		this.expirationTime = cookedMeal.expirationTime;
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
	public MarsClock getExpirationTime() {
		return expirationTime;
	}

	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}
}
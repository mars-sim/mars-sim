/**
 * Mars Simulation Project
 * CookedMeal.java
 * @version 3.07 2014-12-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;

/**
 * This class represents a cooked meal from a kitchen.
 */
public class CookedMeal
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** The time (millisols) between when a meal is cooked and when it expires. */
	private static final double SHELF_LIFE = 200D; // note: 100 mSol ~= 2.5 hrs

	// Data members
	private int quality;
	private MarsClock expirationTime;
	// 2014-11-28 Added name
	private String name;
	
	/**
	 * Constructor.
	 * @param quality the quality of the food
	 * @param creationTime the time the food was cooked.
	 */
	public CookedMeal(String name, int quality, MarsClock creationTime) {
		this.quality = quality;
		this.name = name;
		expirationTime = (MarsClock) creationTime.clone();
		expirationTime.addTime(SHELF_LIFE);
	}
	
	// 2014-11-28 Added getName()
	public String getName() {
		return name;
	}

	/**
	 * Gets the quality of the meal.
	 * @return quality
	 */
	public int getQuality() {
		return quality;
	}

	/**
	 * Gets the expiration time of the meal.
	 * @return expiration time
	 */
	public MarsClock getExpirationTime() {
		return expirationTime;
	}
}
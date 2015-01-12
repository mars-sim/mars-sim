/**
 * Mars Simulation Project
 * PreparedDessert.java
 * @version 3.07 2014-11-28
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;
//import java.util.logging.Logger;

/**
 * This class represents servings of prepared dessert from a kitchen.
 */
public class PreparedDessert
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	//private static Logger logger = Logger.getLogger(PreparedDessert.class.getName());

	/** The time (millisols) between when the dessert is made and when it expires. */
	private static final double SHELF_LIFE = 150D;

	// Data members
	private int quality;
	private MarsClock expirationTime;

	// 2014-11-28 Added name
	private String name;
	/**
	 * Constructor.
	 * @param quality the quality of the dessert
	 * @param creationTime the time the dessert was cooked.
	 */
	public PreparedDessert(String name, int quality, MarsClock creationTime) {
		this.quality = quality;
		this.name = name;
		expirationTime = (MarsClock) creationTime.clone();
		expirationTime.addTime(SHELF_LIFE);
	
	      //logger.info("just called the constructor");

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
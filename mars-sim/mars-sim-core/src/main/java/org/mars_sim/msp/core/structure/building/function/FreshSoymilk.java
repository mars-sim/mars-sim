/**
 * Mars Simulation Project
 * FreshSoymilk.java
 * @version 3.07 2014-11-06
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * This class represents a cooked meal from a kitchen.
 */
public class FreshSoymilk
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(FreshSoymilk.class.getName());

	/** The time (millisols) between when the soymilk is made and when it expires. */
	// Good for 5 sols (refrigeration already included). 
	// On Mars, no industrial quality vacuum sealing yet)
	private static final double SHELF_LIFE = 5000D;

	// Data members
	private int quality;
	private MarsClock expirationTime;

	/**
	 * Constructor.
	 * @param quality the quality of the food
	 * @param creationTime the time the food was cooked.
	 */
	public FreshSoymilk(int quality, MarsClock creationTime) {
		this.quality = quality;
		expirationTime = (MarsClock) creationTime.clone();
		expirationTime.addTime(SHELF_LIFE);
		
	      //logger.info("just called FreshSoymilk's constructor");

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
/**
 * Mars Simulation Project
 * PreparedDessert.java
 * @version 3.1.0 2017-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;

import org.mars_sim.msp.core.time.MarsClock;

/**
 * This class represents servings of prepared dessert from a kitchen.
 */
public class PreparedDessert implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	// private static Logger logger = Logger.getLogger(PreparedDessert.class.getName());

	/**
	 * The time (millisols) between when the dessert is made and when it expires.
	 */
	private static final double SHELF_LIFE = 150D;

	// Data members
	private double quality;

	private double dryMass;

	private String name;

	private String producerName;
	// private String consumerName;
	
	private PreparingDessert kitchen;

	private MarsClock expirationTime;

	/**
	 * Constructor.
	 * 
	 * @param quality      the quality of the dessert
	 * @param creationTime the time the dessert was cooked.
	 */
	public PreparedDessert(String name, double quality, double dryMass, MarsClock creationTime, String producerName,
			PreparingDessert kitchen) {
		this.quality = quality;
		this.name = name;
		this.dryMass = dryMass;
		expirationTime = (MarsClock) creationTime.clone();
		expirationTime.addTime(SHELF_LIFE);
		this.producerName = producerName;
		this.kitchen = kitchen;
	}

//	public PreparedDessert(PreparedDessert preparedDessert, String consumerName) {
//		this.quality = preparedDessert.quality;
//		this.name = preparedDessert.name;
//		this.expirationTime = preparedDessert.expirationTime;
//		this.consumerName = consumerName;
//	}

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
	public MarsClock getExpirationTime() {
		return expirationTime;
	}

//	public void setConsumerName(String consumerName) {
//		this.consumerName = consumerName; 
//	}
	 
}
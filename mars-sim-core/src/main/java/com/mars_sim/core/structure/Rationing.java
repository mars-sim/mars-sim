/*
 * Mars Simulation Project
 * Rationing.java
 * @date 2025-08-12
 * @author Manny Kung
 */


package com.mars_sim.core.structure;

import java.io.Serializable;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.ResourceUtil;

/**
* The Rationing class handles the rationing level of a resource.
*/
public class Rationing implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Rationing.class.getName());
	
	/** The flag to see if a rationing review is due. */
	private boolean reviewFlag = false;
	/** The previous rationing level of the settlement. The higher the more urgent for that resource. */
	private int levelCache = 1;
	/** The current rationing level of the settlement. */
	private int currentLevel = 1;
	/** The player adjustable rationing level that would trigger the state of emergency for the settlement. */
	private int emergencyLevel = 10;
	/** The name of the resource to be ration. */
//	private String resource;
	/** The associated settlement. */
	private Settlement settlement;
	
	
	// Future: as soon as we apply rationining to other resources,
	// this class will become like a RationingManager and will 
	// track more than one resource
	
	public Rationing(Settlement settlement)  {
		this.settlement = settlement;
	}
	
	/** 
	 * Gets the current rationing level at the settlement. 
	 */
	public int getRationingLevel() {
		return levelCache;
	}
	
	/**
	 * Returns the difference between the new and old rationing level. 
	 *
	 * @return difference of rationing level
	 */
	public int getLevelDiff() {
		return currentLevel - levelCache;
	}

	/**
	 * Enforces the new rationing level.
	 */
	public void enforceNewRationingLevel() {
		levelCache = currentLevel;
	}
	
	/**
	 * Sets the flag for reviewing rationing.
	 * 
	 * @param value
	 */
	public void setReviewFlag(boolean value) {
		reviewFlag = value;
	}
	
	/**
	 * Returns if the rationing has been reviewed.
	 * 
	 * @return
	 */
	public boolean canReviewRationing() {
		return reviewFlag;
	}
	
	/**
	 * Sets the emergency level.
	 * 
	 * @param level
	 */
	public void setEmergencyLevel(int level) {
		emergencyLevel = level;
	}
	
	/**
	 * Gets the emergency level.
	 * 
	 * @return
	 */
	public int getEmergencyLevel() {
		return emergencyLevel;
	}
	
	/**
	 * Checks if the emergency level has been reached.
	 * 
	 * @return
	 */
	public boolean isAtEmergency() {
		return currentLevel >= emergencyLevel;
	}
	
	/**
	 * Computes the rationing level at the settlement.
	 *
	 * @return level of rationing level.
	 */
	public boolean reviewRationingLevel() {
		double stored = settlement.getSpecificAmountResourceStored(ResourceUtil.WATER_ID);
		int reserve = settlement.getNumCitizens() * Settlement.MIN_WATER_RESERVE;
		// Assuming a 90-day supply of this resource
		double required = settlement.getWaterConsumptionRate() * settlement.getNumCitizens() * 90;

		int newLevel = (int)((required + reserve) / (1 + stored));
		if (newLevel < 1)
			newLevel = 0;
		else if (newLevel > 0) {
			 // Note: once other resources are starting to adopt this class, 
			 //       this method will be changed	
			logger.info(settlement, 20_000L, "New Rationing Level for water: " + newLevel);
		}
		else if (newLevel > 1000)
			newLevel = 1000;

		currentLevel = newLevel;
		
		return levelCache != newLevel;
	}

}

/*
 * Mars Simulation Project
 * Rationing.java
 * @date 2025-08-12
 * @author Manny Kung
 */

package com.mars_sim.core.structure;

import java.io.Serializable;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.ResourceUtil;

/**
* The Rationing class handles the rationing level of a resource.
*/
public class Rationing implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	// May add back private static SimLogger logger = SimLogger.getLogger(Rationing.class.getName());
	
	private static final double WASH_WATER_USAGE = SimulationConfig.instance().getPersonConfig().getWaterUsageRate();
	
	/** The flag to see if a rationing approval is due. */
	private boolean approvalDue = false;
	/** The flag to see if a rationing review is due. */
	private boolean reviewDue = false;
	/** The current rationing level of the settlement. */
	private int currentLevel = 0;
	/** The newly recommended level just being computed. */
	private int recommendedLevel;
	
	/** The player adjustable rationing level that would trigger the state of emergency for the settlement. */
	private int emergencyLevel = 100;
	/** The name of the resource to be ration. */
//	private String resource;
	/** The associated settlement. */
	private Settlement settlement;
	
	
	// Future: as soon as we apply rationing to other resources,
	// this class will become like a RationingManager and will 
	// track more than one resource
	
	public Rationing(Settlement settlement)  {
		this.settlement = settlement;
	}
	
	/** 
	 * Gets the current rationing level at the settlement. 
	 */
	public int getRationingLevel() {
		return currentLevel;
	}
	
	/**
	 * Returns the difference between cache level and the recommended level. 
	 *
	 * @return difference of rationing level
	 */
	public int getLevelDiff() {
		return recommendedLevel - currentLevel;
	}
	
	/**
	 * Enforces the new rationing level.
	 */
	public void enforceNewRationingLevel() {
		// Update the current level to the newly recommended level
		currentLevel = recommendedLevel;
		// Set the approval due back to false if it hasn't happened
		setApprovalDue(false);
	}
	
	/**
	 * Sets if the review is due.
	 * 
	 * @param value
	 */
	public void setReviewDue(boolean value) {
		reviewDue = value;
	}
	
	/**
	 * Returns if the review is due.
	 * 
	 * @return
	 */
	public boolean isReviewDue() {
		return reviewDue;
	}

	/**
	 * Sets if the approval is due.
	 * 
	 * @param value
	 */
	public void setApprovalDue(boolean value) {
		approvalDue = value;
	}
	
	/**
	 * Returns if the approval is due.
	 * 
	 * @return
	 */
	public boolean isApprovalDue() {
		return approvalDue;
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
	 * Note: do NOT approve the change of level in this method.
	 *
	 * @return level difference
	 */
	public int reviewRationingLevel() {
		double stored = settlement.getSpecificAmountResourceStored(ResourceUtil.WATER_ID);
		int reserve = settlement.getNumCitizens() * Settlement.MIN_WATER_RESERVE;
		
		// Assuming a 90-day supply of this resource and including industrial usage 
		// of WASH_WATER_USAGE
		double required = (5 * WASH_WATER_USAGE + settlement.getWaterConsumptionRate())
				* settlement.getNumCitizens() * 120;
	
		int newLevel = (int)((required + reserve) / (1 + stored));
		if (newLevel < 1)
			newLevel = 0;
//		else if (newLevel > 0) {
			 // Note: once other resources are starting to adopt this class, 
			 //       this method will be changed	
//			logger.info(settlement, 30_000, "New Water Rationing Level: " + newLevel);
//		}
		else if (newLevel > 1000)
			newLevel = 1000;
		
		// Record it as the newly recommended level
		recommendedLevel = newLevel;
		
		return newLevel - currentLevel;
	}

}

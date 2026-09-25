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
	private static SimLogger logger = SimLogger.getLogger(Rationing.class.getName());
	
	private static final double WASH_WATER_USAGE = SimulationConfig.instance().getPersonConfig().getWaterUsageRate();
	
	public enum EmergencyLevel {
		NONE (0),
		ALPHA (1),
		BRAVO (21),
		CHARLIE (61),
		DELTA (141),
		ECHO (301);
		
		private int rationLevel = 0;

		EmergencyLevel(int rationLevel) {
			this.rationLevel = rationLevel;
		}

		public int getRationLevel() {
			return rationLevel;
		}

		/**
		 * Convert from ration leveling to emergency Level
		 * 
		 * @param name
		 * @return type id
		 */
		private static EmergencyLevel convertInt2Enum(int level) {
		    for (int i = 0; i < 6; i++) {
		    	EmergencyLevel e = EmergencyLevel.values()[i];
		    	if (level <= e.rationLevel) {
		    		if (i > 0)
		    			return EmergencyLevel.values()[i - 1];
		    		else {
		    			return EmergencyLevel.NONE;
		    		}
		    	}
		    }
			return EmergencyLevel.ECHO;
		}
	}
	
	/** The flag to see if a rationing approval is due. */
	private boolean approvalDue = false;
	/** The flag to see if a rationing review is due. */
	private boolean reviewDue = false;
	/** The current rationing level of the settlement. */
	private int currentLevel = 0;
	/** The newly recommended level just being computed. */
	private int recommendedLevel;
	
	/** The player adjustable rationing level that would trigger the state of emergency for the settlement. */
//	private EmergencyLevel emergencyLevel = EmergencyLevel.NONE;
	
	/** The associated settlement. */
	private Settlement settlement;
	
	
	// Future: as soon as we apply rationing to other resources,
	// this class will become like a RationingManager and will 
	// track more than one resource
	
	public Rationing(Settlement settlement)  {
		this.settlement = settlement;
	}
	
	/**
	 * Is the settlement at above level 40 in water rationing ?
	 * 
	 * @return
	 */
	public boolean isAboveEmergency40() {
		if (currentLevel <= 40) {
			// 40 is in between BRAVO and CHARLIE
			return false;
		}
		return true;
	}
	
	/**
	 * Gets the Emergency Level in enum.
	 * 
	 * @return
	 */
	public EmergencyLevel getEmergencyLevel() {
		return EmergencyLevel.convertInt2Enum(currentLevel);
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
		
		logger.info(settlement, 30_000L, "Current Water Rationing Level: " + currentLevel + "  recommendedLevel: " + recommendedLevel);
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
	 * Computes the rationing level at the settlement.
	 * Note: do NOT approve the change of level in this method.
	 *
	 * @return level difference
	 */
	public int reviewRationingLevel() {
		var rh = settlement.getEquipmentInventory();
		double storedWater = rh.getSpecificAmountResourceStored(ResourceUtil.WATER_ID);
		double storedBrine = rh.getSpecificAmountResourceStored(ResourceUtil.BRINE_WATER_ID);
		double storedIce = rh.getSpecificAmountResourceStored(ResourceUtil.ICE_ID);
		// In future, consider how to vary industrialReserve according to the specific industrial need of the settlement
		double industrialReserve = Settlement.MIN_WATER_RESERVE;
		
		double personReserve = settlement.getGoodsManager().getReserveLimit(ResourceUtil.WATER_ID);
		
		// Assuming a 90-day supply of this resource and including industrial usage 
		// of WASH_WATER_USAGE
		double required = 90 * (5 * WASH_WATER_USAGE + settlement.getWaterConsumptionRate());
	
		int newLevel = (int)(settlement.getSqrtPopFactor() * (required + industrialReserve + personReserve) 
				/ (1 + storedWater + .75 * storedBrine + .5 * storedIce));
		if (newLevel < 1)
			newLevel = 0;
		else if (newLevel > 1000)
			newLevel = 1000;
		
		// Record it as the newly recommended level
		recommendedLevel = newLevel;
		
		return newLevel - currentLevel;
	}

}

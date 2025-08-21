/*
 * Mars Simulation Project
 * Measure.java
 * @date 2025-08-17
 * @author Manny Kung
 */

package com.mars_sim.core.structure;

import java.io.Serializable;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.structure.Settlement.MeasureType;

/**
 * The Measure class handles how the review and approval of a measure in a settlement.
 * Note: Not being used for now 
 */
public class Measure implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Measure.class.getName());
	
	/** The flag to see if a rationing approval is due. */
	private boolean approvalDue = false;
	/** The flag to see if a rationing review is due. */
	private boolean reviewDue = false;
	/** The previous value of this measure. */
	private int cacheValue = 0;
	/** The current value of this measure. */
	private int currentValue = 0;
	/** The new value just being computed. */
	private int newValue;
	
	/** The player adjustable level that would trigger the state of emergency for the settlement. */
	private int emergencyLevel = 0;
	
	/** The name of the resource to be ration. */
	private MeasureType measureType;
	/** The associated settlement. */
	private Settlement settlement;
	
	
	/**
	 * Constructor.
	 * 
	 * @param settlement
	 * @param measureType
	 */
	public Measure(Settlement settlement, MeasureType measureType)  {
		this.settlement = settlement;
		this.measureType = measureType;
	}
	
	public MeasureType getMeasureType() {
		return measureType;
	}
	
	/** 
	 * Gets the previous value. 
	 */
	public int getCacheValue() {
		return cacheValue;
	}
	
	/**
	 * Returns the difference between cache value and the new value. 
	 *
	 * @return the difference 
	 */
	public int getValueDiff() {
		return cacheValue - newValue;
	}
	
	/**
	 * Enforces the new value.
	 */
	public void enforceNewValue() {
		// Back up the current level to the cache
		cacheValue = currentValue;
		// Update the current level to the newly recommended level
		currentValue = newValue;
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
		return currentValue >= emergencyLevel;
	}
	
	/**
	 * Review this measure in terms of its value
	 * Note: do NOT approve the change here.
	 *
	 * @return level difference
	 */
	public int reviewMeasure(double previou) {
		
		int newLevel = 0;
		if (newLevel < 1)
			newLevel = 0;
		else if (newLevel > 0) {
			 // Note: once other resources are starting to adopt this class, 
			 //       this method will be changed	
			logger.info(settlement, 0, "New Water Rationing Level: " + newLevel);
		}
		else if (newLevel > 1000)
			newLevel = 1000;
		
		// Record it as the newly recommended level
		newValue = newLevel;
		
		return cacheValue - newLevel;
	}

}

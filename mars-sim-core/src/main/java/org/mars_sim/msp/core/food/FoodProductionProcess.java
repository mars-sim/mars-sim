/*
 * Mars Simulation Project
 * FoodProductionProcess.java
 * @date 2022-07-26
 * @author Manny Kung
 */

package org.mars_sim.msp.core.food;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.building.function.FoodProduction;

/**
 * A food production process.
 */
public class FoodProductionProcess implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private double totalWorkTimeReq;
	private double workTimeRemaining;
	private double processTimeRemaining;
	
	private FoodProduction kitchen;
	private FoodProductionProcessInfo info;

	/**
	 * Constructor
	 * 
	 * @param info    information about the process.
	 * @param kitchen the kitchen where the process is taking place.
	 */
	public FoodProductionProcess(FoodProductionProcessInfo info, FoodProduction kitchen) {
		this.info = info;
		this.kitchen = kitchen;
		workTimeRemaining = info.getWorkTimeRequired();
		processTimeRemaining = info.getProcessTimeRequired();
		
		totalWorkTimeReq = workTimeRemaining;
	}

	/**
	 * Gets the information about the process.
	 * 
	 * @return process information
	 */
	public FoodProductionProcessInfo getInfo() {
		return info;
	}

	/**
	 * Gets the remaining work time.
	 * 
	 * @return work time (millisols)
	 */
	public double getWorkTimeRemaining() {
		return workTimeRemaining;
	}

	/**
	 * Adds work time to the process.
	 * 
	 * @param workTime work time (millisols)
	 */
	public void addWorkTime(double workTime) {
		workTimeRemaining -= workTime;
		if (workTimeRemaining < 0D)
			workTimeRemaining = 0D;
	}

	/**
	 * Gets the remaining process time.
	 * 
	 * @return process time (millisols)
	 */
	public double getProcessTimeRemaining() {
		return processTimeRemaining;
	}

	/**
	 * Adds process time to the process.
	 * 
	 * @param processTime process time (millisols)
	 */
	public void addProcessTime(double processTime) {
		processTimeRemaining -= processTime;
		if (processTimeRemaining < 0D)
			processTimeRemaining = 0D;
	}

	@Override
	public String toString() {
		return info.getName();
	}

	/**
	 * Gets the food production building function.
	 * 
	 * @return food production building function.
	 */
	public FoodProduction getKitchen() {
		return kitchen;
	}

	/**
	 * Gets the total work time required
	 * 
	 * @return
	 */
	public double getTotalWorkTime() {
		return totalWorkTimeReq;
	}
	
	/**
	 * Compares this object with the specified object for order.
	 * 
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	public int compareTo(FoodProductionProcess p) {
		return info.getName().compareToIgnoreCase(p.info.getName());
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		kitchen = null;
		info.destroy();
		info = null;
	}
}

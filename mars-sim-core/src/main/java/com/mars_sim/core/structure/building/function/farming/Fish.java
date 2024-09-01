/*
 * Mars Simulation Project
 * Fish.java
 * @date 2024-08-13
 * @author Manny Kung
 */

package com.mars_sim.core.structure.building.function.farming;

public class Fish extends Herbivore {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	// May add back: private static SimLogger logger = SimLogger.getLogger(Fish.class.getName());
	
	/** Typical adult fish size, in ounces. */ 
	public static final double MAX_FISH_OUNCES = 60; 
	
	public Fish(double initSize, double initRate, double initNeed) {
		super(initSize, initRate, initNeed);
	}

	@Override
	public void alterSize(double amount) {
		double newSize = getSize() + amount;
		
		if (newSize > MAX_FISH_OUNCES) {
			newSize = MAX_FISH_OUNCES;
		}
		
		setSize(newSize);
	}
}

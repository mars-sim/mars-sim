/*
 * Mars Simulation Project
 * StorableItem.java
 * @date 2022-10-03
 * @author Manny Kung
 */
package com.mars_sim.core.resource;

import com.mars_sim.core.goods.GoodType;

/**
 * This class enables a part to store amount resources.
 * PLEASE DO NOT DELETE. IT'S A PART OF EXPERIMENTAL WORK
 */
public class StorableItem extends Part {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private double loadingLimit = 100;
	private double amountLoaded;
	
	private AmountResource resourceLoaded;
	 
	public StorableItem(String name, int id, String description, GoodType type, double mass, int sol) {
		// Use Part constructor.
		super(name, id, description, type, mass, sol);
	}
	
	public AmountResource getResourceLoaded() {
		return resourceLoaded;
	}
	
	public double getAmountLoaded() {
		return amountLoaded;
	}
	
	public double getRemainingAmount() {
		return loadingLimit- amountLoaded;
	}
	
	/**
	 * Loads the resource.
	 * 
	 * @param resource
	 * @param amount
	 * @return the excess amount of resources that cannot be loaded
	 */
	public double loadResource(AmountResource resource, double amount) {
		resourceLoaded = resource;
		if (amount > loadingLimit) {
			amountLoaded = loadingLimit;
			return amount - amountLoaded;
		}
		return 0;
	}
	
	public void setLoadingLimit(double loadingLimit) {
		this.loadingLimit = loadingLimit;
	}
}

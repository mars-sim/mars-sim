/*
 * Mars Simulation Project
 * StorableItem.java
 * @date 2022-10-03
 * @author Manny Kung
 */
package org.mars_sim.msp.core.resource;

import org.mars_sim.msp.core.goods.GoodType;

public class StorableItem extends Part {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private double loadingLimit = 100;
	private double amountLoaded;
	
	private AmountResource resourceLoaded;
	
//	Map<Integer, SimpleContainer> containers = new HashMap<>();
	 
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
	
//	public Map<Integer, SimpleContainer> getContainers() {
//		return containers;
//	}
	
//	public void removeContainer(int id) {
		// Note: when a SimpleContainer is needed on-demand, it's created. 
		// If no longer in use, remove that SimpleContainer 
//		containers.remove(id);
//	}
	
	
}

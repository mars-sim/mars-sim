/*
 * Mars Simulation Project
 * SimpleContainer.java
 * @date 2022-10-03
 * @author Manny Kung
 */
package org.mars_sim.msp.core.resource;

public class SimpleContainer {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private int ownerId;
	private Resource resource;
	private double quantity;
	
	public SimpleContainer(int ownerId, Resource resource, double quantity) {
		this.ownerId = ownerId;
		this.resource = resource;
		this.quantity = quantity;
	}
	
	public int getOwnerId() {
		return this.ownerId;
	}
	
	public Resource getResource() {
		return this.resource;
	}
	
	public double getQuantity() {
		return this.quantity;
	}
	
}

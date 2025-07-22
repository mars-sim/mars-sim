/*
 * Mars Simulation Project
 * SimpleContainer.java
 * @date 2022-10-03
 * @author Manny Kung
 */
package com.mars_sim.core.resource;

/**
 * This class defines a container that holds a particular amount resource or a part.
 * PLEASE DO NOT DELETE. IT'S A PART OF EXPERIMENTAL WORK
 */
public class SimpleContainer {
	
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

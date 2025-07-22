/*
 * Mars Simulation Project
 * ResourceHolder.java
 * @date 2025-07-21
 * @author Barry Evans
 */
package com.mars_sim.core.equipment;

import java.util.Set;

import com.mars_sim.core.Entity;

/**
 * Represents an entity that can hold resources.
 *
 */
public interface ResourceHolder extends Entity {

	/**
	 * Gets the specific amount resources stored, NOT including those inside equipment.
	 *
	 * @param resource
	 * @return amount
	 */
	double getSpecificAmountResourceStored(int resource);

	
	/**
	 * Gets all the specific amount resources stored, including inside equipment
	 *
	 * @param resource
	 * @return quantity
	 */
	double getAllSpecificAmountResourceStored(int resource);
	
	/**
	 * Stores the amount resource
	 *
	 * @param resource the amount resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	double storeAmountResource(int resource, double quantity);


	/**
	 * Retrieves the resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	double retrieveAmountResource(int resource, double quantity);

	/**
	 * Gets the capacity of a particular amount resource
	 *
	 * @param resource
	 * @return capacity
	 */
	double getSpecificCapacity(int resource);

	/**
	 * Obtains the combined capacity of remaining storage space for a particular amount resource
	 *
	 * @param resource
	 * @return quantity
	 */
	double getRemainingCombinedCapacity(int resource);

	/**
	 * Obtains the specific capacity of the remaining storage space of a particular amount resource
	 *
	 * @param resource
	 * @return quantity
	 */
	double getRemainingSpecificCapacity(int resource);
	
	/**
	 * Gets the total capacity of resource that this container can hold.
	 *
	 * @return total capacity (kg).
	 */
	double getCargoCapacity();

	/**
	 * Gets a collection of supported resources
	 *
	 * @return a collection of resource ids
	 */
	Set<Integer> getSpecificResourceStoredIDs();

	/**
	 * Gets a collection of all supported resources, including inside equipment
	 *
	 * @return a collection of resource ids
	 */
	Set<Integer> getAllAmountResourceStoredIDs();
	
	/**
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	public boolean hasAmountResourceRemainingCapacity(int resource);


	/**
	 * Gets the quantity of all stock and specific amount resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	public double getAllAmountResourceStored(int resource);
}

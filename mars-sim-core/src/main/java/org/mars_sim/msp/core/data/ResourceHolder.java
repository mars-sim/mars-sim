/*
 * Mars Simulation Project
 * ResourceHolder.java
 * @date 2021-10-14
 * @author Barry Evans
 */
package org.mars_sim.msp.core.data;

import java.util.Set;

import org.mars_sim.msp.core.Unit;

/**
 * Represents an entity that can hold resources.
 *
 */
public interface ResourceHolder {

	/**
	 * Gets the amount resource stored
	 * 
	 * @param resource
	 * @return quantity
	 */
	double getAmountResourceStored(int resource);
		
	/**
	 * Stores the amount resource
	 * 
	 * @param resource the amount resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	double storeAmountResource(int resource, double quantity);
	
	/**
	 * Retrieves the resource 
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
	double getAmountResourceCapacity(int resource);
	
	/**
	 * Obtains the remaining storage space of a particular amount resource
	 * 
	 * @param resource
	 * @return quantity
	 */
	double getAmountResourceRemainingCapacity(int resource);
	
	/**
	 * Gets the total capacity of resource that this container can hold.
	 * 
	 * @return total capacity (kg).
	 */
	double getTotalCapacity();
	
	/**
	 * Gets a collection of supported resources
	 * 
	 * @return a collection of resource ids
	 */
	Set<Integer> getAmountResourceIDs();
	
	/**
	 * Gets the holder's unit instance
	 * 
	 * @return the holder's unit instance
	 */
	public Unit getHolder();
}

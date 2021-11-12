/*
 * Mars Simulation Project
 * ItemHolder.java
 * @date 2021-11-12
 * @author Manny Kung
 */
package org.mars_sim.msp.core.equipment;

import java.util.Set;

import org.mars_sim.msp.core.Unit;

/**
 * Represents an entity that can hold item resources.
 *
 */
public interface ItemHolder {

	/**
	 * Gets the quantity of an item resource stored
	 *
	 * @param resource
	 * @return quantity
	 */
	int getItemResourceStored(int resource);

	/**
	 * Gets the remaining quantity of an item resource
	 *
	 * @param resource
	 * @return quantity
	 */
	int getItemResourceRemainingQuantity(int resource);

	/**
	 * Gets the cargo/general/shared capacity of resource
	 *
	 * @return capacity (kg).
	 */
	double getCargoCapacity();

	/**
	 * Gets a collection of supported item resources
	 *
	 * @return a collection of resource ids
	 */
	Set<Integer> getItemResourceIDs();

	/**
	 * Stores the item resource
	 *
	 * @param resource the item resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	int storeItemResource(int resource, int quantity);

	/**
	 * Retrieves the item resource
	 *
	 * @param resource the item resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	int retrieveItemResource(int resource, int quantity);

	/**
	 * Gets the holder's unit instance
	 *
	 * @return the holder's unit instance
	 */
	public Unit getHolder();
}

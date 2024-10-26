/*
 * Mars Simulation Project
 * ItemHolder.java
 * @date 2021-11-12
 * @author Manny Kung
 */
package com.mars_sim.core.equipment;

import java.util.Set;

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
}

/*
 * Mars Simulation Project
 * ContainerHolder.java
 * @date 2023-07-12
 * @author Manny Kung
 */
package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.Unit;

/**
 * Represents an entity that can hold resources.
 *
 */
public interface ContainerHolder {

//	/**
//	 * Adds an amount resource container to this container holder
//	 * 
//	 * @param container
//	 * @param type
//	 * @param resource
//	 */
//	void addAmountResourceContainer(AmountResourceContainer container, ContainerType type, int resource);
	
	/**
	 * Gets the amount resource stored.
	 *
	 * @param type
	 * @param id
	 * @param resource
	 * @return quantity
	 */
	double getAmountResourceStored(ContainerType type, int id, int resource);

	
	/**
	 * Stores the amount resource.
	 *
	 * @param type
	 * @param id
	 * @param resource the amount resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	double storeAmountResource(ContainerType type, int id, int resource, double quantity);


	/**
	 * Retrieves the amount resource.
	 *
	 * @param type
	 * @param id
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	double retrieveAmountResource(ContainerType type, int id, int resource, double quantity);

	/**
	 * Gets the capacity of a particular amount resource.
	 *
	 * @param type
	 * @param id
	 * @param resource
	 * @return capacity
	 */
	double getAmountResourceCapacity(ContainerType type, int id, int resource);

	/**
	 * Obtains the remaining storage space of a particular amount resource.
	 *
	 * @param type
	 * @param id
	 * @param resource
	 * @return quantity
	 */
	double getAmountResourceRemainingCapacity(ContainerType type, int id, int resource);

	/**
	 * Does it have unused space or capacity for a particular resource ?
	 *
	 * @param type
	 * @param id
	 * @param resource
	 * @return
	 */
	public boolean hasAmountResourceRemainingCapacity(ContainerType type, int id, int resource);
	
	/**
	 * Gets the total capacity of resource that this container can hold.
	 *
	 * @param type
	 * @param id
	 * @return total capacity (kg).
	 */
	double getCargoCapacity(ContainerType type, int id);

	/**
	 * Gets the supported amount resource.
	 *
	 * @return resource id
	 */
	int getAmountResource(ContainerType type, int id);

	/**
	 * Gets the owner unit instance.
	 *
	 * @return
	 */
	public Unit getOwner();
	
}

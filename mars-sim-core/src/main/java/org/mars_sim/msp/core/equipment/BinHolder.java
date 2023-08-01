/*
 * Mars Simulation Project
 * BinHolder.java
 * @date 2023-07-30
 * @author Manny Kung
 */
package org.mars_sim.msp.core.equipment;

import java.util.Collection;
import java.util.Set;

import org.mars_sim.msp.core.Unit;

/**
 * Represents an entity that can hold resources.
 *
 */
public interface BinHolder {

//	/**
//	 * Adds an amount resource container to this container holder
//	 * 
//	 * @param container
//	 * @param type
//	 * @param resource
//	 */
//	void addAmountResourceContainer(AmountResourceContainer container, ContainerType type, int resource);
	
	/**
	 * Finds the number of bins of a particular type.
	 *
	 * @param containerType the bin type.
	 * @return number of empty bins.
	 */
	public int findNumBinsOfType(BinType binType);
	
	/**
	 * Finds all of the bins of a particular type.
	 *
	 * @return collection of bins or empty collection if none.
	 */
	public Collection<Bin> findBinsOfType(BinType binType);
	
	/**
	 * Gets the locally held amount resource bin set.
	 * 
	 * @return
	 */
	public Set<AmountResourceBin> getAmountResourceBinSet();
	
	/**
	 * Gets the amount resource stored.
	 *
	 * @param type
	 * @param id
	 * @param resource
	 * @return quantity
	 */
	double getAmountResourceStored(BinType type, int id, int resource);

	
	/**
	 * Stores the amount resource.
	 *
	 * @param type
	 * @param id
	 * @param resource the amount resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	double storeAmountResource(BinType type, int id, int resource, double quantity);


	/**
	 * Retrieves the amount resource.
	 *
	 * @param type
	 * @param id
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	double retrieveAmountResource(BinType type, int id, int resource, double quantity);

	/**
	 * Gets the capacity of a particular amount resource.
	 *
	 * @param type
	 * @param id
	 * @param resource
	 * @return capacity
	 */
	double getAmountResourceCapacity(BinType type, int id, int resource);

	/**
	 * Obtains the remaining storage space of a particular amount resource.
	 *
	 * @param type
	 * @param id
	 * @param resource
	 * @return quantity
	 */
	double getAmountResourceRemainingCapacity(BinType type, int id, int resource);

	/**
	 * Does it have unused space or capacity for a particular resource ?
	 *
	 * @param type
	 * @param id
	 * @param resource
	 * @return
	 */
	public boolean hasAmountResourceRemainingCapacity(BinType type, int id, int resource);
	
	/**
	 * Gets the total capacity of resource that this container can hold.
	 *
	 * @param type
	 * @param id
	 * @return total capacity (kg).
	 */
	double getCargoCapacity(BinType type, int id);

	/**
	 * Gets the supported amount resource.
	 *
	 * @return resource id
	 */
	int getAmountResource(BinType type, int id);

	/**
	 * Gets the owner unit instance.
	 *
	 * @return
	 */
	public Unit getOwner();

	/**
	 * Adds a bin to be owned by the settlement.
	 *
	 * @param bin the bin
	 * @return true if this settlement can carry it
	 */
	boolean addBin(Bin bin);
	
}

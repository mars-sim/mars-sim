/*
 * Mars Simulation Project
 * ContainerInterface.java
 * @date 2021-10-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.Unit;

/**
 * This interface accounts for units that are considered container for resources
 */
public interface Container {

	public EquipmentType getEquipmentType();

	/**
	 * Gets the total capacity of resource that this container can hold.
	 * 
	 * @return total capacity (kg).
	 */
	public double getTotalCapacity();

	public double getBaseMass();

	public boolean transfer(Unit currentOwner, Unit newOwner);
	

	public double storeAmountResource(int resourceID, double collected);

	public double getAmountResourceStored(int resourceID);
	
	public double getAmountResourceRemainingCapacity(int resourceID);

}

/*
 * Mars Simulation Project
 * Container.java
 * @date 2021-10-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.Unit;

/**
 * This interface accounts for units that are considered container for resources
 */
public interface Container extends ResourceHolder {

	public EquipmentType getEquipmentType();

	/**
	 * Containers only support a single resource.
	 * @return Resource ID assigned to the container.
	 */
	public int getResource();
	
	public double getBaseMass();

	public boolean transfer(Unit newOwner);

	public double getStoredMass();

	/**
	 * Clean the container if empty. This will reset the assigned Resource
	 */
    public void clean();
	
}

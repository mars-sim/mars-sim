/*
 * Mars Simulation Project
 * Container.java
 * @date 2021-10-03
 * @author Scott Davis
 */
package com.mars_sim.core.equipment;

import com.mars_sim.core.unit.UnitHolder;

/**
 * This interface accounts for units that are considered container for resources
 */
public interface Container extends ResourceHolder {

	EquipmentType getEquipmentType();

	/**
	 * Containers only support a single resource.
	 * 
	 * @return Resource ID assigned to the container.
	 */
	int getResource();
	
	double getBaseMass();

	boolean transfer(UnitHolder newOwner);

	double getStoredMass();

	/**
	 * Cleans the container if empty. This will reset the assigned Resource.
	 */
    void clean();
	
	boolean isEmpty(boolean brandNew);
}

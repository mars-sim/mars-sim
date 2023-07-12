/*
 * Mars Simulation Project
 * BaseContainer.java
 * @date 2023-07-12
 * @author Manny Kung
 */

package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.Unit;


public class BaseContainer {

	private double capacity;
	
	private Unit unit;
	
	private ContainerType containerType;
	
	public BaseContainer(Unit unit, double cap) {
		this.unit = unit;
		this.capacity = cap;
	}
	
	public ContainerType getContainerType() {
		return containerType;
	}
	
	public void setContainerType(ContainerType type) {
		this.containerType = type;
	}
	
	public double getCapacity() {
		return capacity;
	}
	
	public void setCapacity(double cap) {
		capacity = cap;
	}
	
	public Unit getUnit() {
		return unit;
	}
	
	public void setOwner(Unit owner) {
		this.unit = owner;
	}
}

/*
 * Mars Simulation Project
 * BaseBin.java
 * @date 2023-07-30
 * @author Manny Kung
 */

package com.mars_sim.core.equipment;

import java.io.Serializable;

import com.mars_sim.core.Unit;


public class BaseBin implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private double capacity;
		
	private Unit owner;
	
	private BinType binType;

	public BaseBin(Unit owner, double cap, BinType type) {
		this.owner = owner;
		this.capacity = cap;
		this.binType = type;
	}
	
	public BinType getBinType() {
		return binType;
	}
	
	public double getCapacity() {
		return capacity;
	}
	
	public void setCapacity(double cap) {
		capacity = cap;
	}
	
	public Unit getOwner() {
		return owner;
	}
}

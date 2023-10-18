/*
 * Mars Simulation Project
 * BaseBin.java
 * @date 2023-07-30
 * @author Manny Kung
 */

package com.mars_sim.core.equipment;

import java.io.Serializable;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Unit;


public class BaseBin implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private double capacity;
	
	private Entity entity;
	
	private Unit owner;
	
	private BinType binType;

	public BaseBin(Entity entity, double cap) {
		this.entity = entity;
		this.capacity = cap;
	}
	
	public BinType getBinType() {
		return binType;
	}
	
	public void setBinType(BinType type) {
		this.binType = type;
	}
	
	public double getCapacity() {
		return capacity;
	}
	
	public void setCapacity(double cap) {
		capacity = cap;
	}
	
	public Entity getBinEntity() {
		return entity;
	}
	
	public void setOwner(Unit owner) {
		this.owner = owner;
	}
	
	public Unit getOwner() {
		return owner;
	}
}

/**
 * Mars Simulation Project
 * ManufactureProcessItem.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */

package org.mars_sim.msp.core.manufacture;

import java.io.Serializable;

import org.mars_sim.msp.core.resource.Type;

/**
 * A manufacturing process input or output item.
 */
public class ManufactureProcessItem implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private String name;
	private Type type;
	private double amount;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public void setAmount(double amount) {
		this.amount = amount;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
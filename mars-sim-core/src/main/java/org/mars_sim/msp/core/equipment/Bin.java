/*
 * Mars Simulation Project
 * Bin.java
 * @date 2023-07-30
 * @author Manny Kung
 */

package org.mars_sim.msp.core.equipment;

import java.io.Serializable;

import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.tool.Conversion;

public class Bin implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private int id;

	private double amount;

	private String name;

	private BinType type;
	
	private AmountResource ar;

	public Bin(BinType type, int id, AmountResource ar, double amount) {
		this.type = type;
		this.id = id;
		this.ar = ar;
		this.amount = amount;
		
		generateName();
	}
	
	public void generateName() {
		name = String.format("%s %03d", Conversion.capitalize(type.getName()), id);
	}
		
	public int getID() {
		return id;
	}
	
	public BinType getBinType() {
		return type;
	}
	
	public AmountResource getAmountResource() {
		return ar;
	}
	
	public void setAmountResource(AmountResource ar) {
		this.ar = ar;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public void setAmount(double value) {
		amount = value;
	}
	
	public boolean isEmpty() {
		return amount == 0;
	}
	
	public String getName() {
		return name;
	}
}

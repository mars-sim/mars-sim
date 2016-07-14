package org.mars_sim.msp.restws.model;

import org.mars_sim.msp.core.resource.AmountResource;

public class StoredAmount {
	private double amount;
	private double capacity;
	private	String name;
	private String phase;
	
	public StoredAmount(AmountResource resource, double amount, double capacity) {
		this.name = resource.getName();
		this.phase = resource.getPhase().getName();
		this.amount = amount;
		this.capacity = capacity;
	}
	
	public String getName() {
		return name;
	}
	public String getPhase() {
		return phase;
	}
	public double getCapacity() {
		return capacity;
	}
	public double getAmount() {
		return amount;
	}

}

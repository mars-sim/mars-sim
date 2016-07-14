package org.mars_sim.msp.restws.model;

import org.mars_sim.msp.core.resource.ItemResource;

public class StoredItem {
	private int	amount;
	private String name;
	
	public StoredItem(ItemResource item, int amount) {
		this.amount = amount;
		this.name = item.getName();
	}
	
	public String getName() {
		return name;
	}
	
	public int getAmount() {
		return amount;
	}
	
}

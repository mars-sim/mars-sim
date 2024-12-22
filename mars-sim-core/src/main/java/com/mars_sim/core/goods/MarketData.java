/*
 * Mars Simulation Project
 * MarketData.java
 * @date 2024-09-21
 * @author Manny Kung
 */

package com.mars_sim.core.goods;

import java.io.Serializable;

public class MarketData implements Serializable {
	
	private static final long serialVersionUID = 1L;
    
	private Good good;
	
	private double value = -1;
	private double price = -1;
	private double cost = -1;
	private double demand = -1;
	
	
	public MarketData(Good good) {
		this.good = good;
	}

	public Good getGood() {
		return good;
	}
	
	public void setValue(double data) {
		this.value = data;
	}

	public void setPrice(double data) {
		this.price = data;
	}

	public void setDemand(double data) {
		this.demand = data;
	}

	public void setCost(double data) {
		this.cost = data;
	}
	
	public double getValue() {
		return value;
	}

	public double getPrice() {
		return price;
	}

	public double getDemand() {
		return demand;
	}

	public double getCost() {
		return cost;
	}
}

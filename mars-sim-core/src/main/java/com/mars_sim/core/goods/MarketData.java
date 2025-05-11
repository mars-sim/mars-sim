/*
 * Mars Simulation Project
 * MarketData.java
 * @date 2024-09-21
 * @author Manny Kung
 */

package com.mars_sim.core.goods;

import java.io.Serializable;

import com.mars_sim.core.tool.MathUtils;

public class MarketData implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private double value = 0;
	private double price = 0;
	private double cost = 0;
	private double demand = 0;
	
	MarketData() {
	}
	
	/**
	 * This adjusts a new value to smooth out the transition from one value to another.
	 * @param newValue
	 * @param oldValue
	 * @return
	 */
	private static double smoothValue(double newValue, double oldValue) {
		if (oldValue != -1) {
			newValue = (0.95 * oldValue + 0.05 * newValue);
		}
		return newValue;
	}

	double setValue(double data) {
		var oldValue = value;
		var newValue = smoothValue(data, oldValue);
		this.value = MathUtils.between(newValue, GoodsManager.MIN_VP, GoodsManager.MAX_FINAL_VP);

		return value - oldValue;
	}

	void setPrice(double data) {
		this.price = smoothValue(data, price);
	}

	double setDemand(double data) {
		var oldDemand = demand;
		var newDemand = smoothValue(data, oldDemand);		
		demand = MathUtils.between(newDemand, GoodsManager.MIN_DEMAND, GoodsManager.MAX_DEMAND);
		return demand - oldDemand;
	}

	void setCost(double data) {
		this.cost = smoothValue(data, cost);
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

/*
 * Mars Simulation Project
 * MarketData.java
 * @date 2025-08-26
 * @author Manny Kung
 */

package com.mars_sim.core.goods;

import java.io.Serializable;

import com.mars_sim.core.tool.MathUtils;

public class MarketData implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private double goodValue = 0.0;
	private double price = 0.0;
	private double cost = 0.0;
	private double demand = 0.0;
	
	public MarketData() {
	}
	
	/**
	 * This adjusts a new value to smooth out the transition from one value to another.
	 * 
	 * @param newValue
	 * @param oldValue
	 * @return
	 */
	private static double smoothValue(double newValue, double oldValue) {
		if (!Double.isNaN(oldValue) && !Double.isNaN(newValue)) {
			newValue = (0.95 * oldValue + 0.05 * newValue);
		}
		else {
			newValue = 0.01;
		}
		return newValue;
	}

	/**
	 * Updates the Good Value here.
	 * 
	 * @param data
	 * @return 
	 */
	public double updateGoodValue(double data) {
		double oldValue = goodValue;
		if (oldValue == 0.0) {
			goodValue = data;
			return goodValue;
		}
		var newValue = smoothValue(data, oldValue);
		newValue = MathUtils.between(newValue, GoodsManager.MIN_VP, GoodsManager.MAX_FINAL_VP);
		goodValue = newValue;
		return newValue;
	}

	/**
	 * Updates the demand here.
	 * 
	 * @param data
	 * @return 
	 */
	public double updateDemand(double data) {
		double oldDemand = demand;
		if (oldDemand == 0.0) {
			demand = data;
			return data;
		}
		var newDemand = smoothValue(data, oldDemand);		
		newDemand = MathUtils.between(newDemand, GoodsManager.MIN_DEMAND, GoodsManager.MAX_DEMAND);
		demand = newDemand;
		return newDemand;
	}

	/**
	 * Updates the cost here.
	 * 
	 * @param data
	 * @return 
	 */
	public double updateCost(double data) {
		double oldC = cost;
		if (oldC == 0.0) {
			cost = data;
			return data;
		}
		var newC = smoothValue(data, oldC);		
		newC = MathUtils.between(newC, 0.01, 10_000);
		cost = newC;
		return newC;
	}
	
	/**
	 * Updates the price here.
	 * 
	 * @param data
	 * @return 
	 */
	public double updatePrice(double data) {
		double oldP = price;
		if (oldP == 0.0) {
			price = data;
			return data;
		}
		var newP = smoothValue(data, oldP);		
		newP = MathUtils.between(newP, 0.01, 10_000);
		price = newP;
		return newP;
	}
	
	void setCost(double data) {
		this.cost = smoothValue(data, cost);
	}
	

	void setPrice(double data) {
		this.price = smoothValue(data, price);
	}

	
	public double getGoodValue() {
		return goodValue;
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

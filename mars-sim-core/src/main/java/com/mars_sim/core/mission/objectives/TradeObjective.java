/*
 * Mars Simulation Project
 * TradeObjective.java
 * @date 25-07-19
 * @author Barry Evans
 */
package com.mars_sim.core.mission.objectives;

import java.util.Collections;
import java.util.Map;

import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodCategory;
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.structure.Settlement;

/**
 * Represents the objectives of a Trading activity at a Settlement.
 */
public class TradeObjective implements MissionObjective {

    private static final long serialVersionUID = 1L;
    private Settlement venue;

    private double profit;
	private double desiredProfit;

	private Map<Good, Integer> sell;
	private Map<Good, Integer> bought;
    private Map<Good, Integer> desiredBuy;


    public TradeObjective(Settlement venue, Map<Good, Integer> toBuy,  Map<Good, Integer> sell, double profit) {
        this.venue = venue;
        this.desiredBuy = toBuy;
        this.bought = Collections.emptyMap();
        this.sell = sell;

        this.desiredProfit = profit;
        this.profit = 0D;
    }

    @Override
    public String getName() {
        return "Trade at " + venue.getName();
    }

    /**
     * Where is trading taking place
     * @return
     */
    public Settlement getTradingVenue() {
        return venue;
    }

    /**
     * Add a load to the resoruces map for loading
     * @param resources
     * @param isSelling
     * @return 
     */
    public Map<Integer,Number> addResourcesToLoad(Map<Integer, Number> resources, boolean isSelling) {

		// Add buy/sell load.
		Map<Good, Integer> load = null;
		if (isSelling) {
			load = sell;
		} else {
			load = bought;
		}

        for(var e : load.entrySet()) {
			Good good = e.getKey();
			if (good.getCategory() == GoodCategory.AMOUNT_RESOURCE) {
                resources.merge(good.getID(), e.getValue().doubleValue(), (a,b) -> (a.doubleValue() + b.doubleValue()));
			}
            else if (good.getCategory() == GoodCategory.ITEM_RESOURCE) {
                resources.merge(good.getID(), e.getValue().doubleValue(), (a,b) -> (a.intValue() + b.intValue()));
			}
		}

        return resources;
    }

    /**
     * Add a load to the equipment map for loading
     * @param resources
     * @param isSelling
     * @return 
     */
    public Map<Integer, Integer> addEquipmentToLoad(Map<Integer,Integer> resources, boolean isSelling) {
	    // Add buy/sell load.
		Map<Good, Integer> load = null;
		if (isSelling) {
			load = sell;
		} else {
			load = bought;
		}

        for(var e : load.entrySet()) {
			Good good = e.getKey();
			if (good.getCategory().equals(GoodCategory.EQUIPMENT)
					|| good.getCategory() == GoodCategory.CONTAINER) {
                resources.merge(good.getID(), e.getValue(), (a,b) -> (a + b));
			}
		}
		return resources;
    }

    /**
	 * Gets the type of vehicle in a load.
	 *
	 * @param isSelling true if selling load
	 * @return vehicle type or null if none.
	 */
	public String getLoadVehicleType(boolean isSelling) {
		String result = null;

		Map<Good, Integer> load = null;
		if (isSelling) {
			load = sell;
		} else {
			load = bought;
		}

		for(var e : load.keySet()) {
			if (e.getCategory() == GoodCategory.VEHICLE) {
				result = e.getName();
			}
		}

		return result;
	}

    /**
     * Update the objective which what has actually been bought
     * @param newBuy Bought goods
     * @param newProfit Profit
     */
    public void updateBought(Map<Good,Integer> newBuy, double newProfit) {
        this.bought = newBuy;
        this.profit = newProfit;
    }

    public Map<Good, Integer> getSell() {
        return sell;
    }

    public double getProfit() {
        return profit;
    }

    public double getDesiredProfit() {
        return desiredProfit;
    }

    public Map<Good, Integer> getBought() {
        return bought;
    }

    public Map<Good, Integer> getDesiredBuy() {
        return desiredBuy;
    }
}

/*
 * Mars Simulation Project
 * Deal.java
 * @date 2022-07-21
 * @author Barry Evans
 */
package org.mars_sim.msp.core.goods;

import java.util.Map;

import javax.swing.plaf.basic.BasicTreeUI.SelectionModelPropertyChangeHandler;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Represents a potential deal with another Settlement.
 */
public class Deal implements Comparable<Deal> {
	private Settlement buyer;
    private double buyingRevenue;
    private Map<Good, Integer> buyingLoad;
    private double sellingRevenue;
    private Map<Good, Integer> sellingLoad;
    private double tradeCost;
	private double profit;
	private MarsClock created;

    Deal(Settlement buyer, double sellingRevenue, Map<Good, Integer> sellLoad, double buyingRevenue, Map<Good, Integer> buyLoad, double cost, MarsClock created) {
		this.buyer = buyer;
		this.sellingRevenue = sellingRevenue;
        this.sellingLoad = sellLoad;
        this.buyingRevenue = buyingRevenue;
        this.buyingLoad = buyLoad;
        this.tradeCost = cost;

        // Profit is the money earn from the sell minus the money used in the return buy plus the delivery cost
        this.profit = sellingRevenue - (buyingRevenue + cost);
		this.created = created;
	}

	public Settlement getBuyer() {
        return buyer;
    }

    public double getTradeCost() {
        return tradeCost;
    }

    public double getProfit() {
        return profit;
    }

    public double getBuyingRevenue() {
        return buyingRevenue;
    }
    public Map<Good, Integer> getBuyingLoad() {
        return buyingLoad;
    }

    public double getSellingRevenue() {
        return sellingRevenue;
    }
    
    public Map<Good, Integer> getSellingLoad() {
        return sellingLoad;
    }

    public MarsClock getCreated() {
        return created;
    }

    /**
     * Order Deal according to increasing profit
     */
    @Override
    public int compareTo(Deal other) {
        return Double.compare(profit, other.profit);
    }
}
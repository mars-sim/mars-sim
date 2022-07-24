/*
 * Mars Simulation Project
 * Deal.java
 * @date 2022-07-21
 * @author Barry Evans
 */
package org.mars_sim.msp.core.goods;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Represents a potential deal with another Settlement.
 */
public class Deal implements Comparable<Deal> {
	private Settlement buyer;
    private double buyingRevenue;
    private double sellingRevenue;
    private double tradeCost;
	private double profit;
	private MarsClock created;

    Deal(Settlement buyer, double sellingRevenue, double buyingRevenue, double cost, MarsClock created) {
		this.buyer = buyer;
		this.sellingRevenue = sellingRevenue;
        this.buyingRevenue = buyingRevenue;
        this.tradeCost = cost;
        this.profit = (sellingRevenue + buyingRevenue) - cost;
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

    public double getSellingRevenue() {
        return sellingRevenue;
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
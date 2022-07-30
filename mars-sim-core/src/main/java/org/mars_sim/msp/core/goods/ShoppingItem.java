package org.mars_sim.msp.core.goods;

import java.io.Serializable;

public class ShoppingItem implements Serializable {
    private Good good;
    private int quantity;
    private double buyPrice;

    ShoppingItem(Good good, int quantity, double buyPrice) {
        this.good = good;
        this.quantity = quantity;
        this.buyPrice = buyPrice;
    }

    public Good getGood() {
        return good;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getBuyPrice() {
        return buyPrice;
    }
}

/*
 * Mars Simulation Project
 * ShoppingItem.java
 * @date 2022-07-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.goods;

/**
 * An item in the shopping list holding the value and quantity
 */
public class ShoppingItem {
    private int quantity;
    private double price;

    ShoppingItem(int quantity, double price) {
        this.quantity = quantity;
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }
}

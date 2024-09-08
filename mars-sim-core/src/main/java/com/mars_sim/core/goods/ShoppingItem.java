/*
 * Mars Simulation Project
 * ShoppingItem.java
 * @date 2022-07-30
 * @author Scott Davis
 */
package com.mars_sim.core.goods;

import java.io.Serializable;

/**
 * An item in the shopping list holding the value and quantity
 */
public record ShoppingItem(int quantity, double price) implements Serializable
{}
   
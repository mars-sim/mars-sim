/*
 * Mars Simulation Project
 * Food.java
 * @date 2022-09-26
 * @author Manny Kung
 */

package com.mars_sim.core.food;

import java.io.Serializable;

/**
 * A meta class describing an economic food in the simulation.
 */
public class Food
implements Serializable, Comparable<Food> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	// Data members
	private int id;	
	private String name;
	private FoodType foodType;

	/**
	 * Constructor with object.
	 * 
	 * @param name the name of the food.
	 * @param id The resoruce id of the assoicated AmountResource
	 * @param foodType the food's category.
	 */
	Food(String name, int id, FoodType foodType) {
		if (name != null)
			this.name = name;
		else throw new IllegalArgumentException("name cannot be null.");

		this.foodType = foodType;
		this.id = id;
	}

	/**
	 * Gets the food's name.
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the food type enum.
	 * 
	 * @return foodType.
	 */
	public FoodType getFoodType() {
		return foodType;
	}

	/**
	 * Gets a string representation of the food.
	 * 
	 * @return string.
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Gets the good's id.
	 * 
	 * @return
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * Checks if an object is equal to this object.
	 * 
	 * @param object the object to compare.
	 * @return true if equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Food f = (Food) obj;
		return this.id == f.getID();
	}

	/**
	 * Gets the hash code value.
	 */
	@Override
	public int hashCode() {
		return id % 64;
	}

	/**
	 * Compares this object with the specified object for order.
	 * 
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less than, 
	 * equal to, or greater than the specified object.
	 */
	@Override
	public int compareTo(Food o) {
		return name.compareTo(o.name);
	}
}

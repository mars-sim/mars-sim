/*
 * Mars Simulation Project
 * Food.java
 * @date 2022-09-26
 * @author Manny Kung
 */

package org.mars_sim.msp.core.food;

import java.io.Serializable;

import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Resource;

/**
 * A meta class describing an economic food in the simulation.
 */
public class Food
implements Serializable, Comparable<Food> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	// Data members
	private int id;
	private double demandMultiplier;
	
	private String name;
	private String type;
	
	private Resource resource;
	
	private FoodType foodType;

	/**
	 * Constructor with object.
	 * 
	 * @param name the name of the food.
	 * @param object the food's object if any.
	 * @param foodType the food's category.
	 */
	Food(String name, AmountResource ar, FoodType foodType, double demandMultiplier) {
		if (name != null)
			this.name = name;
		else throw new IllegalArgumentException("name cannot be null.");

		if (ar != null) {
			this.resource = ar;
			this.foodType = foodType;
			this.type = foodType.getName();
			this.demandMultiplier = demandMultiplier;
			this.id = ar.getID();
		}
		
		else throw new IllegalArgumentException("ar cannot be null.");
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
	 * Gets the food's resource instance if any.
	 * 
	 * @return object or null if none.
	 */
	public Resource getResource() {
		return resource;
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
	public String toString() {
		return name;
	}

	/**
	 * Gets the type of food.
	 * 
	 * @return string.
	 */
	public String getType() {
		return type;
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
	public int compareTo(Food o) {
		return name.compareTo(o.name);
	}
}

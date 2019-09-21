/**
 * Mars Simulation Project
 * Food.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.foodProduction;

import java.io.Serializable;

//import org.mars_sim.msp.core.equipment.Equipment;

/**
 * A meta class describing an economic food in the simulation.
 */
public class Food
implements Serializable, Comparable<Food> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// Data members
	private String name;
	private Class<?> classType;
	private Object object;
	private FoodType category;

	/**
	 * Constructor with object.
	 * @param name the name of the food.
	 * @param object the food's object if any.
	 * @param category the food's category.
	 */
	Food(String name, Object object, FoodType category) {
		if (name != null) this.name = name.trim().toLowerCase();
		else throw new IllegalArgumentException("name cannot be null.");

		if (object != null) {
			this.object = object;
			this.classType = object.getClass();
		}
		else throw new IllegalArgumentException("object cannot be null.");

		if (isValidCategory(category)) this.category = category;
		else throw new IllegalArgumentException("category: " + category + " not valid.");
	}

	/**
	 * Constructor with class.
	 * @param name the name of the food.
	 * @param classType the food's class.
	 * @param category {@link FoodType} the food's category.
	 */
	Food(String name, Class<?> classType, FoodType category) {
		if (name != null) this.name = name.trim().toLowerCase();
		else throw new IllegalArgumentException("name cannot be null.");

		if (classType != null) this.classType = classType;
		else throw new IllegalArgumentException("classType cannot be null.");

		if (isValidCategory(category)) this.category = category;
		else throw new IllegalArgumentException("category: " + category + " not valid.");
	}

	/**
	 * Checks if a category string is valid.
	 * @param category the category enum to check.
	 * @return true if valid category.
	 */
	private static boolean isValidCategory(FoodType category) {
		boolean result = false;

		if (FoodType.AMOUNT_RESOURCE == category) result = true;
		//else if (FoodType.ITEM_RESOURCE == category) result = true;
		//else if (FoodType.EQUIPMENT == category) result = true;
		//else if (FoodType.VEHICLE == category) result = true;

		return result;
	}

	/**
	 * Gets the food's name.
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the food's class.
	 * @return class
	 */
	public Class<?> getClassType() {
		return classType;
	}

	/**
	 * Gets the food's object if any.
	 * @return object or null if none.
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * Gets the food's category enum.
	 * @return category.
	 */
	public FoodType getCategory() {
		return category;
	}

	/**
	 * Gets a string representation of the food.
	 * @return string.
	 */
	public String toString() {
		return name;
	}

	/**
	 * Checks if an object is equal to this object.
	 * @param object the object to compare.
	 * @return true if equal
	 */
	public boolean equals(Object object) {
		boolean result = true;
		if (object instanceof Food) {
			Food food = (Food) object;
			if (!name.equals(food.name)) result = false;
			if (!classType.equals(food.classType)) result = false;
			if (this.object != null) {
				if (!this.object.equals(food.object)) result = false;
			}
			if (!category.equals(food.category)) result = false;
		}
		else result = false;

		return result;
	}

	/**
	 * Gets the hash code value.
	 */
	public int hashCode() {
		int hashCode = name.hashCode() * getClass().hashCode();
		if (object != null) hashCode *= object.hashCode();
		hashCode *= category.hashCode();
		return hashCode;
	}

	/**
	 * Compares this object with the specified object for order.
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less than, 
	 * equal to, or greater than the specified object.
	 */
	public int compareTo(Food o) {
		return name.compareTo(o.name);
	}
}
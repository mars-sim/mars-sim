/**
 * Mars Simulation Project
 * Food.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.foodProduction;

import java.io.Serializable;

import org.mars_sim.msp.core.resource.AmountResource;

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
	private String type;
	private Class<?> classType;
	private Object object;
	private FoodType foodType;

	/**
	 * Constructor with object.
	 * @param name the name of the food.
	 * @param object the food's object if any.
	 * @param foodType the food's category.
	 */
	Food(String name, AmountResource object, FoodType foodType) {
		if (name != null) this.name = name.trim().toLowerCase();
		else throw new IllegalArgumentException("name cannot be null.");

		if (object != null) {
			this.object = object;
			this.classType = object.getClass();		
			this.type = foodType.getName(); //((AmountResource) object).getType();	
		}
		
		else throw new IllegalArgumentException("object cannot be null.");

//		if (isValidCategory(category)) this.category = category;
//		else throw new IllegalArgumentException("category: " + category + " not valid.");
	}

//	/**
//	 * Constructor with class.
//	 * @param name the name of the food.
//	 * @param classType the food's class.
//	 * @param category {@link FoodType} the food's category.
//	 */
//	Food(String name, Class<?> classType, FoodType category) {
//		if (name != null) this.name = name.trim().toLowerCase();
//		else throw new IllegalArgumentException("name cannot be null.");
//
//		if (classType != null) this.classType = classType;
//		else throw new IllegalArgumentException("classType cannot be null.");
//
//		if (isValidCategory(category)) this.category = category;
//		else throw new IllegalArgumentException("category: " + category + " not valid.");
//	}

//	/**
//	 * Checks if a FoodType is valid.
//	 * 
//	 * @param category the category enum to check.
//	 * @return true if valid category.
//	 */
//	private static boolean isValidCategory(FoodType category) {
//		boolean result = false;
//
//		if (FoodType.ANIMAL == category
//			|| FoodType.CHEMICAL == category
//			|| FoodType.CROP == category
//			|| FoodType.DERIVED == category
//			|| FoodType.INSECT == category
//			|| FoodType.OIL == category
//			|| FoodType.ORGANISM == category
//			|| FoodType.SOY_BASED == category
//			|| FoodType.TISSUE == category) 
//			result = true;
//
//		return result;
//	}

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
	 * Gets the food type enum.
	 * @return foodType.
	 */
	public FoodType getFoodType() {
		return foodType;
	}

	/**
	 * Gets a string representation of the food.
	 * @return string.
	 */
	public String toString() {
		return name;
	}

	/**
	 * Gets the type of food.
	 * @return string.
	 */
	public String getType() {
		return type;
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
			if (!foodType.equals(food.foodType)) result = false;
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
		hashCode *= foodType.hashCode();
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

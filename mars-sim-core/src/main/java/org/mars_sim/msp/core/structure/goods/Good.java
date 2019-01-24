/**
 * Mars Simulation Project
 * Good.java
 * @version 3.1.0 2018-12-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.goods;

import java.io.Serializable;

import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;

/**
 * A meta class describing an economic good in the simulation.
 */
public class Good implements Serializable, Comparable<Good> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// Data members
	private String name;
	//
//	private Class<?> classType;
//	private Object object;
	private int id;
	private GoodType category;

	/**
	 * Constructor with object.
	 * 
	 * @param name     the name of the good.
	 * @param object   the good's object if any.
	 * @param category the good's category.
	 */
	Good (String name, int id, GoodType category) {
		if (name != null)
			this.name = name.trim().toLowerCase();
		else
			throw new IllegalArgumentException("name cannot be null.");
		this.id = id;
//		if (object != null) {
//			this.object = object;
//			this.classType = object.getClass();
//		} else
//			throw new IllegalArgumentException("object cannot be null.");

		if (isValidCategory(category))
			this.category = category;
		else
			throw new IllegalArgumentException("category: " + category + " not valid.");
	}

//	/**
//	 * Constructor with class.
//	 * 
//	 * @param name      the name of the good.
//	 * @param classType the good's class.
//	 * @param category  {@link GoodType} the good's category.
//	 */
//	Good (String name, Class<?> classType, GoodType category) {
//		if (name != null)
//			this.name = name.trim().toLowerCase();
//		else
//			throw new IllegalArgumentException("name cannot be null.");
//
//		if (classType != null)
//			this.classType = classType;
//		else
//			throw new IllegalArgumentException("classType cannot be null.");
//
//		if (isValidCategory(category))
//			this.category = category;
//		else
//			throw new IllegalArgumentException("category: " + category + " not valid.");
//	}

	/**
	 * Checks if a category string is valid.
	 * 
	 * @param category the category enum to check.
	 * @return true if valid category.
	 */
	private static boolean isValidCategory(GoodType category) {
		boolean result = false;

		if (GoodType.AMOUNT_RESOURCE == category)
			result = true;
		else if (GoodType.ITEM_RESOURCE == category)
			result = true;
		else if (GoodType.EQUIPMENT == category)
			result = true;
		else if (GoodType.VEHICLE == category)
			result = true;

		return result;
	}

	/**
	 * Gets the good's name.
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the good's equipment class.
	 * 
	 * @return equipment class
	 */
	public Class<? extends Equipment> getClassType() {
		return EquipmentFactory.getEquipmentClass(id);
	}

//	/**
//	 * Gets the good's object if any.
//	 * 
//	 * @return object or null if none.
//	 */
//	public Object getObject() {
//		return object;
//	}

	public int getID() {
		return id;
	}
	
	/**
	 * Gets the good's category enum.
	 * 
	 * @return category.
	 */
	public GoodType getCategory() {
		return category;
	}

	/**
	 * Gets a string representation of the good.
	 * 
	 * @return string.
	 */
	public String toString() {
		return name;
	}

//	/**
//	 * Checks if an object is equal to this object.
//	 * 
//	 * @param object the object to compare.
//	 * @return true if equal
//	 */
//	public boolean equals(Object object) {
//		boolean result = true;
//		if (object instanceof Good) {
//			Good good = (Good) object;
//			if (!name.equals(good.name))
//				result = false;
//			if (!classType.equals(good.classType))
//				result = false;
//			if (this.object != null) {
//				if (!this.object.equals(good.object))
//					result = false;
//			}
//			if (!category.equals(good.category))
//				result = false;
//		} else
//			result = false;
//
//		return result;
//	}
	
	/**
	 * Gets the hash code value.
	 */
	public int hashCode() {
		int hashCode = name.hashCode();// * getClass().hashCode();
//		hashCode *= id;
//		if (object != null)
//			hashCode *= object.hashCode();
		hashCode *= category.hashCode();
		return hashCode;
	}

	/**
	 * Compares this object with the specified object for order.
	 * 
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	public int compareTo(Good o) {
		return name.compareTo(o.name);
	}
}
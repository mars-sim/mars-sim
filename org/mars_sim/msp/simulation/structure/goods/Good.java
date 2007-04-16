/**
 * Mars Simulation Project
 * Good.java
 * @version 2.81 2007-04-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.goods;

import java.io.Serializable;

/**
 * A meta class describing an economic good in the simulation.
 */
public class Good implements Serializable{

	// Data members
	private String name;
	private Class classType;
	private Object object;
	
	/**
	 * Constructor with object.
	 * @param name the name of the good.
	 * @param object the good's object if any.
	 */
	Good(String name, Object object) {
		if (name != null) this.name = name;
		else throw new IllegalArgumentException("name cannot be null.");
			
		if (object != null) {
			this.object = object;
			this.classType = object.getClass();
		}
		else throw new IllegalArgumentException("object cannot be null.");
	}
	
	/**
	 * Constructor with class.
	 * @param name the name of the good.
	 * @param classType the goods class.
	 */
	Good(String name, Class classType) {
		if (name != null) this.name = name;
		else throw new IllegalArgumentException("name cannot be null.");
		
		if (classType != null) this.classType = classType;
		else throw new IllegalArgumentException("classType cannot be null.");
	}
	
	/**
	 * Gets the good's name.
	 * @return name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the good's class.
	 * @return class
	 */
	public Class getClassType() {
		return classType;
	}
	
	/**
	 * Gets the good's object if any.
	 * @return object or null if none.
	 */
	public Object getObject() {
		return object;
	}
	
	/**
	 * Gets a string representation of the good.
	 * @return string.
	 */
	public String toString() {
		return getName();
	}
	
	/**
	 * Checks if an object is equal to this object.
	 * @param object the object to compare.
	 * @return true if equal
	 */
	public boolean equals(Object object) {
		boolean result = true;
		if (object instanceof Good) {
			Good good = (Good) object;
			if (!getName().equals(good.getName())) result = false;
			if (!getClassType().equals(good.getClassType())) result = false;
			if (getObject() != null) {
				if (!getObject().equals(good.getObject())) result = false;
			}
		}
		else result = false;
		
		return result;
	}
	
	/**
	 * Gets the hash code value.
	 */
	public int hashCode() {
		int hashCode = getClass().hashCode();
		if (getObject() != null) hashCode *= getObject().hashCode();
		return hashCode;
	}
}
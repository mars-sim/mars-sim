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
	 * Constructor
	 * @param name the name of the good.
	 * @param classType the good's class.
	 * @param object the good's object if any.
	 */
	Good(String name, Class classType, Object object) {
		this.name = name;
		this.classType = classType;
		this.object = object;
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
}
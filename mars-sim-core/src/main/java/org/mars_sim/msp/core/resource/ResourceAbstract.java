/**
 * Mars Simulation Project
 * ResourceAbstract.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;

/**
 * to avoid code repetition in implementors of {@link Resource}.
 * 
 * @author stpa 2014-01-30
 */
public abstract class ResourceAbstract implements Resource, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 123L;

	private int id;
	private String name;
	private String description;
	
	
	protected ResourceAbstract(String name, int id, String description) {
		super();
		this.name = name;
		this.id = id;
		this.description = description;
	}

	/**
	 * Gets the resource's id.
	 * 
	 * @return resource id.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * Gets the resource's name.
	 * 
	 * @return name of resource.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Gets the description of the resource
	 * 
	 * @return description
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Compares this object with the specified object for order.
	 * 
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	@Override
	public final int compareTo(Resource o) {
		return name.compareToIgnoreCase(o.getName());
	}

	/**
	 * Returns the resource as a string.
	 */
	@Override
	public final String toString() {
		return getName();
	}
}

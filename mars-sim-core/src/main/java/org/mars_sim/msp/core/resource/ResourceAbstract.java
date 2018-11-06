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

	/**
	 * Compares this object with the specified object for order.
	 * 
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	@Override
	public final int compareTo(Resource o) {
		return getName().compareToIgnoreCase(o.getName());
	}

	/**
	 * Returns the resource as a string.
	 */
	@Override
	public final String toString() {
		return getName();
	}
}

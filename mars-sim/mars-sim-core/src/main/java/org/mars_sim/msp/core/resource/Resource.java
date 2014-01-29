/**
 * Mars Simulation Project
 * AmountResource.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;

/**
 * A resource used in the simulation.
 */
public interface Resource extends Comparable<Resource> {
	
	/**
	 * Gets the resource's name
	 * @return name
	 */
	public String getName();
}
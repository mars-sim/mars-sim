/**
 * Mars Simulation Project
 * AmountResource.java
 * @version 3.00 2010-08-10
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
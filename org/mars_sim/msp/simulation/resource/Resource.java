/**
 * Mars Simulation Project
 * AmountResource.java
 * @version 2.86 2009-03-22
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.resource;

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
/**
 * Mars Simulation Project
 * Phase.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;


/**
 * A phase of an amount resource.
 */
public enum Phase {

	GAS ("gas"),
	LIQUID ("liquid"),
	SOLID ("solid");
	
	private String name;

	/**
	 * Private constructor.
	 * @param name the name of the phase.
	 */
	private Phase(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of the phase.
	 * @return name
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
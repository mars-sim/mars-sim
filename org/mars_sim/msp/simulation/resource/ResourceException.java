/**
 * Mars Simulation Project
 * ResourceException.java
 * @version 2.79 2005-12-08
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation.resource;

/**
 * An exception related to resources.
 */
public class ResourceException extends Exception {

	/**
	 * Constructor
	 * @param message exception message.
	 */
	public ResourceException(String message) {
		super(message);
	}
	
	/**
	 * Constructor
	 * @param message exception message
	 * @param arg cause
	 */
	public ResourceException(String message, Throwable arg) {
		super(message, arg);
	}

	/**
	 * Constructor
	 * @param arg cause
	 */
	public ResourceException(Throwable arg) {
		super(arg);
	}
}
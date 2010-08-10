/**
 * Mars Simulation Project
 * ResourceException.java
 * @version 3.00 2010-08-10
 * @author Scott Davis 
 */

package org.mars_sim.msp.core.resource;

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
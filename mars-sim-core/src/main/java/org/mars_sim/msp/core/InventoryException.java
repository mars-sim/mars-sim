/**
 * Mars Simulation Project
 * InventoryException.java
 * @version 3.1.0 2017-11-06
 * @author Scott Davis
 */

package org.mars_sim.msp.core;

/**
 * An exception thrown by the inventory class.
 */
public class InventoryException extends Exception {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param message the exception message.
	 */
	public InventoryException(String message) {
		super(message);
	}
	
	/**
	 * Constructor
	 * @param message the exception message.
	 * @param arg cause
	 */
	public InventoryException(String message, Throwable arg) {
		super(message, arg);
	}
	
	/**
	 * Constructor
	 * @param arg cause
	 */
	public InventoryException(Throwable arg) {
		super(arg);
	}
}
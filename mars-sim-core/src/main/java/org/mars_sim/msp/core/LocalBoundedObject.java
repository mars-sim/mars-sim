/**
 * Mars Simulation Project
 * LocalBoundedObject.java
 * @version 3.1.0 2017-11-06
 * @author Scott Davis
 */

package org.mars_sim.msp.core;

/**
 * Interface for a rectangle-bounded object in the local area.
 */
public interface LocalBoundedObject {

	/**
	 * Gets the X location of the object from the local area's center point.
	 * 
	 * @return X location (meters from local center point - West: positive, East:
	 *         negative).
	 */
	public double getXLocation();

	/**
	 * Gets the Y location of the object from the local area's center point.
	 * 
	 * @return Y location (meters from local center point - North: positive, South:
	 *         negative).
	 */
	public double getYLocation();

	/**
	 * Gets the object's width.
	 * 
	 * @return width (meters).
	 */
	public double getWidth();

	/**
	 * Gets the object's length.
	 * 
	 * @return length (meters).
	 */
	public double getLength();

	/**
	 * Gets the object's facing.
	 * 
	 * @return facing (degrees from North clockwise).
	 */
	public double getFacing();
}
/**
 * Mars Simulation Project
 * LocalBoundedObject.java
 * @date 2022-06-09
 * @author Scott Davis
 */

package org.mars_sim.msp.core;

import java.io.Serializable;

/**
 * Interface for a rectangle-bounded object in the local area.
 */
public interface LocalBoundedObject extends Serializable {

	/**
	 * Gets the X location of the object from the local area's center point.
	 * 
	 * @return X location (meters from local center point - West: positive, East:
	 *         negative).
	 */
	default double getXLocation() {
		return getPosition().getX();
	}

	/**
	 * Gets the Y location of the object from the local area's center point.
	 * 
	 * @return Y location (meters from local center point - North: positive, South:
	 *         negative).
	 */
	default double getYLocation() {
		return getPosition().getY();
	}

	/**
	 * Get the position of this object relative to the local area's center point
	 */
	public LocalPosition getPosition();
	
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

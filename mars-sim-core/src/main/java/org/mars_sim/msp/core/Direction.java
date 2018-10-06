/**
 * Mars Simulation Project
 * Direction.java
 * @version 3.1.0 2017-10-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.Serializable;

/**
 * Direction. Represents an angular direction. It provides some useful static
 * methods involving directions.
 */
public class Direction implements Cloneable, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** Direction in radians. */
	private double direction;

	// Static data members
	private static double TWO_PI = 2 * Math.PI;

	/**
	 * Constructs a Direction object, hence a constructor. 0 = North, clockwise
	 * 
	 * @param direction direction in radians
	 */
	public Direction(double direction) {
		this.direction = cleanDirection(direction);
	}

	/**
	 * Clones this Direction object.
	 */
	public Object clone() {
		return new Direction(direction);
	}

	/**
	 * Returns true if object is an equal direction.
	 * 
	 * @param obj an Object instance
	 * @return true if obj is an equal direction
	 */
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof Direction) {
			if (((Direction) obj).direction == direction)
				result = true;
		}
		return result;
	}

	/**
	 * Gets the hash code for this object.
	 * 
	 * @return hash code.
	 */
	public int hashCode() {
		return (int) (direction * 1000D);
	}

	/**
	 * Gets the string value of the object.
	 */
	public String toString() {
		return "" + direction;
	}

	/**
	 * Returns direction in radians. 0 = North, clockwise
	 * 
	 * @return direction in radians
	 */
	public double getDirection() {
		return direction;
	}

	/**
	 * Sets the direction. 0 = North, clockwise
	 * 
	 * @param direction new direction
	 */
	public void setDirection(double direction) {
		this.direction = cleanDirection(direction);
	}

	/**
	 * Returns the sine of the direction.
	 * 
	 * @return the sine of the direction
	 */
	public double getSinDirection() {
		return Math.sin(direction);
	}

	/**
	 * Returns the cosine of the direction.
	 * 
	 * @return the cosine of the direction
	 */
	public double getCosDirection() {
		return Math.cos(direction);
	}

	/**
	 * Makes sure a direction isn't above 2PI or less than zero.
	 * 
	 * @param raw direction
	 * @return cleaned direction
	 */
	private double cleanDirection(double direction) {

		while (direction < 0D) {
			direction += TWO_PI;
		}

		while (direction > TWO_PI) {
			direction -= TWO_PI;
		}

//        if ((direction < 0.0) || (direction > TWO_PI)) {
//            direction = Math.IEEEremainder(direction, TWO_PI);
//        }

		return direction;
	}
}

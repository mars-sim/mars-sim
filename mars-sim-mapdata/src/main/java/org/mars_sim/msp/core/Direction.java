/*
 * Mars Simulation Project
 * Direction.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.Serializable;

/**
 * Direction. Represents an angular direction. It provides some useful static
 * methods involving directions.
 */
public class Direction implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** Direction in radians. */
	private double dir;

	// Static data members
	private static final double TWO_PI = 2 * Math.PI;

	/**
	 * Constructs a Direction object, hence a constructor. 0 = North, clockwise
	 * 
	 * @param direction direction in radians
	 */
	public Direction(double direction) {
		this.dir = cleanDirection(direction);
	}
	
	/**
	 * Returns true if object is an equal direction.
	 * 
	 * @param obj an Object instance
	 * @return true if obj is an equal direction
	 */
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof Direction
			&& ((Direction) obj).dir == dir) {
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
		return (int) (dir * 1000D);
	}

	/**
	 * Gets the string value of the object.
	 */
	public String toString() {
		return "" + dir;
	}

	/**
	 * Returns direction in radians. 0 = North, clockwise
	 * 
	 * @return direction in radians
	 */
	public double getDirection() {
		return dir;
	}

	/**
	 * Returns the sine of the direction.
	 * 
	 * @return the sine of the direction
	 */
	public double getSinDirection() {
		return Math.sin(dir);
	}

	/**
	 * Returns the cosine of the direction.
	 * 
	 * @return the cosine of the direction
	 */
	public double getCosDirection() {
		return Math.cos(dir);
	}

	/**
	 * Makes sure a direction isn't above 2PI or less than zero.
	 * 
	 * @param raw direction
	 * @return cleaned direction
	 */
	private static double cleanDirection(double direction) {

		while (direction < 0D) {
			direction += TWO_PI;
		}

		while (direction > TWO_PI) {
			direction -= TWO_PI;
		}

		return direction;
	}
}

/*
 * Mars Simulation Project
 * IntPoint.java
 * @date 2022-08-02
 * @author Greg Whelan
 */
package com.mars_sim.core.map.location;
import java.io.Serializable;

/**
 * The IntPoint class is a replacement of
 * java.awt.Point that returns int typed
 * X and Y coordinates.
 */
public final class IntPoint implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	private int x;
	private int y;

	/** Constructor. */
	public IntPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Returns the X coordinate of the point as int.
	 * 
	 * @return the X coordinate of the point as int
	 */
	public int getX() {
		return x;
	}

	public int getiX() {
		return getX();
	}

	/**
	 * Returns the Y coordinate of the point as int. 
	 * 
	 * @return the Y coordinate of the point as int
	 */
	public int getY() {
		return y;
	}

	public int getiY() {
		return getY();
	}

	/**
	 * Gets the distance between 2 points.
	 * 
	 * @param position
	 * @return
	 */
	public int getDistance(IntPoint position) {
		// Note: need to case position.getX() and position.getY() to double
		return (int) Math.round(Math.sqrt(Math.pow(getX() - 1.0 * position.getX(), 2D) +
			        Math.pow(getY() - 1.0 * position.getY(), 2D)));
	}

	/**
	 * Gets the angle in radians between this point and another point.
	 * @param location
	 * @return Radions of the angle.
	 */
	public double getRadians(IntPoint location) {
		return Math.atan2(location.getY() - y, location.getX() - x);
	}
	
	@Override
	public String toString() {
		return "IntPoint [" + x + ", " + y + "]";
	}
}

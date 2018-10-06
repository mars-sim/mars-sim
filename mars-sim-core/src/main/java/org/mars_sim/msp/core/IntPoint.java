/**
 * Mars Simulation Project
 * IntPoint.java
 * @version 3.1.0 2017-11-06
 * @author Greg Whelan
 */
package org.mars_sim.msp.core;

import java.awt.Point;

/**
 * The IntPoint class is an extension of
 * java.awt.Point that returns int typed
 * X and Y coordinates.
 */

public final class IntPoint
extends Point {

//	protected
//
//	public static class Create
//	{
//		public static IntPoint point(int x, int y)
//		{
//			return null;
//		}
//	}

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Constructor. */
	public IntPoint(int x, int y) {
		super(x, y);
	}

	/**
	 * Returns the X coordinate of the point as int. 
	 * @return the X coordinate of the point as int
	 */
	public int getiX() {
		return x;
	}

	/**
	 * Returns the Y coordinate of the point as int. 
	 * @return the Y coordinate of the point as int
	 */
	public int getiY() {
		return y;
	}
}

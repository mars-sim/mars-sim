/**
 * Mars Simulation Project
 * IntPoint.java
 * @version 2.71 2000-09-26
 * @author Greg Whelan
 */

package org.mars_sim.msp.simulation;

import java.awt.Point;

/** The IntPoint class is an extension of
 *  of java.awt.Point that returns int typed
 *  X and Y coordinates.
 */

public class IntPoint extends Point {

    public IntPoint(int x, int y) {
        super(x, y);
    }

    /** Returns the X coordinate of the point as int. */
    public int getiX() {
        return x;
    }

    /** Returns the Y coordinate of the point as int. */
    public int getiY() {
        return y;
    }
}

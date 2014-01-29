/**
 * Mars Simulation Project
 * IntPoint.java
 * @version 3.06 2014-01-29
 * @author Greg Whelan
 */

package org.mars_sim.msp.core;

import java.awt.*;

/** The IntPoint class is an extension of
 *  java.awt.Point that returns int typed
 *  X and Y coordinates.
 */

public final class IntPoint extends Point {

//    protected

//    public static class Create
//    {
//        public static IntPoint point(int x, int y)
//        {
//            return null;
//        }
//    }

    /** Construct an IntPoint object */
    public IntPoint(int x, int y) {
        super(x, y);
    }

    /** Returns the X coordinate of the point as int. 
     *  @return the X coordinate of the point as int
     */
    public int getiX() {
        return x;
    }

    /** Returns the Y coordinate of the point as int. 
     *  @return the Y coordinate of the point as int
     */
    public int getiY() {
        return y;
    }
}

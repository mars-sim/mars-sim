/**
 * Mars Simulation Project
 * IntPoint.java
 * @version 2.70 2000-08-31
 * @author Greg Whelan
 */

import java.awt.Point;

public class IntPoint extends Point {

    public IntPoint(int x, int y) {
	super(x, y);
    }

    /**
     * Returns the X coordinate of the point as int.
     */
    public int getiX() {
	return x;
    }

    /**
     * Returns the Y coordinate of the point as int.
     */
    public int getiY() {
	return y;
    }

}

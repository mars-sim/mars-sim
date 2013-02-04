/**
 * Mars Simulation Project
 * LocalAreaUtil.java
 * @version 3.04 2013-02-02
 * @author Scott Davis
 */

package org.mars_sim.msp.core;

import java.awt.geom.Point2D;

/**
 * A utility class for calculating locations in a local area with a center point, 
 * such as around a settlement or rover.
 */
public class LocalAreaUtil {

    /** Distance from edge of boundary when determining internal locations. */
    private static final double INNER_BOUNDARY_DISTANCE = 1.5D;
    
    /**
     * Private empty constructor for utility class.
     */
    private LocalAreaUtil() {
        // Do nothing
    }
    
    /**
     * Gets a local relative location from a location relative to this bounded object.
     * @param xLoc the X location relative to this bounded object.
     * @param yLoc the Y location relative to this bounded object.
     * @param boundedObject the local bounded object.
     * @return Point containing the X and Y locations relative to the local area's center point.
     */
    public static Point2D.Double getLocalRelativeLocation(double xLoc, double yLoc, LocalBoundedObject boundedObject) {
        Point2D.Double result = new Point2D.Double();
        
        double radianRotation = (boundedObject.getFacing() * (Math.PI / 180D));
        double rotateX = (xLoc * Math.cos(radianRotation)) - (yLoc * Math.sin(radianRotation));
        double rotateY = (xLoc * Math.sin(radianRotation)) + (yLoc * Math.cos(radianRotation));
        
        double translateX = rotateX + boundedObject.getXLocation();
        double translateY = rotateY + boundedObject.getYLocation();
        
        result.setLocation(translateX, translateY);
        
        return result;
    }
    
    /**
     * Gets a random location inside a local bounded object.
     * @param boundedObject the local bounded object.
     * @return random X/Y location relative to the center of the bounded object.
     */
    public static Point2D.Double getRandomInteriorLocation(LocalBoundedObject boundedObject) {
        
        Point2D.Double result = new Point2D.Double(0D, 0D);
        
        double xRange = boundedObject.getWidth() - (INNER_BOUNDARY_DISTANCE * 2D);
        if (xRange > 0D) {
            result.x = RandomUtil.getRandomDouble(xRange) - (xRange / 2D);
        }
        else {
            result.x = 0D;
        }
        
        double yRange = boundedObject.getLength() - (INNER_BOUNDARY_DISTANCE * 2D);
        if (yRange > 0D) {
            result.y = RandomUtil.getRandomDouble(yRange) - (yRange / 2D);
        }
        else {
            result.y = 0D;
        }
        
        return result;
    }
}
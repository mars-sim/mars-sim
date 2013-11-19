/**
 * Mars Simulation Project
 * Hatch.java
 * @version 3.06 2013-11-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.connection;

import java.io.Serializable;

import org.mars_sim.msp.core.LocalBoundedObject;

/**
 * A hatch on one side of a building connection.
 */
public class Hatch implements Serializable, LocalBoundedObject {

    // Static members.
    private static final double LENGTH = .6D;
    private static final double WIDTH = 2.76D;
    
    // Data members
    private double xLoc;
    private double yLoc;
    private double facing;
    
    /**
     * Constructor.
     * @param xLoc The X location of the center point of the hatch.
     * @param yLoc The Y location of the center point of the hatch.
     * @param facing The facing of the hatch (degrees).
     */
    public Hatch(double xLoc, double yLoc, double facing) {
        this.xLoc = xLoc;
        this.yLoc = yLoc;
        this.facing = facing;
    }
    
    @Override
    public double getXLocation() {
        return xLoc;
    }
    
    /**
     * Sets the X location of the center point of the hatch.
     * @param xLoc the X location (meters).
     */
    void setXLocation(double xLoc) {
        this.xLoc = xLoc;
    }

    @Override
    public double getYLocation() {
        return yLoc;
    }
    
    /**
     * Sets the X location of the center point of the hatch.
     * @param xLoc the X location (meters).
     */
    void setYLocation(double yLoc) {
        this.yLoc = yLoc;
    }

    @Override
    public double getWidth() {
        return WIDTH;
    }

    @Override
    public double getLength() {
        return LENGTH;
    }

    @Override
    public double getFacing() {
        return facing;
    }
    
    @Override
    public boolean equals(Object other) {
        
        boolean result = false;
        
        if (other instanceof Hatch) {
            Hatch otherHatch = (Hatch) other;
            
            if ((xLoc == otherHatch.getXLocation()) && 
                    (yLoc == otherHatch.getYLocation()) &&
                    (facing == otherHatch.getFacing())) {
                result = true;
            }
        }
        
        return result;
    }
}
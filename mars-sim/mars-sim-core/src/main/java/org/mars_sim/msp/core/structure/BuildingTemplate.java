/**
 * Mars Simulation Project
 * BuildingTemplate.java
 * @version 3.06 2013-10-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure;

import java.io.Serializable;

/**
 * A building template information.
 */
public class BuildingTemplate implements Serializable {

    // Data members
    private String type;
    private double width;
    private double length;
    private double xLoc;
    private double yLoc;
    private double facing;
    
    public BuildingTemplate(String type, double width, double length, double xLoc, 
            double yLoc, double facing) {
        this.type = type;
        this.width = width;
        this.length = length;
        this.xLoc = xLoc;
        this.yLoc = yLoc;
        this.facing = facing;
    }

    /**
     * Gets the building type.
     * @return building type.
     */
    public String getType() {
        return type;
    }
    
    /**
     * Gets the width of the building.
     * Note: value is -1 if not set in template.
     * @return width (meters) of building or -1 if not set.
     */
    public double getWidth() {
        return width;
    }
    
    /**
     * Gets the length of the building.
     * Note: value is -1 if not set in template.
     * @return length (meters) of building or -1 if not set.
     */
    public double getLength() {
        return length;
    }

    /**
     * Gets the x location of the building in the settlement.
     * @return x location (meters from settlement center - West: positive, East: negative).
     */
    public double getXLoc() {
        return xLoc;
    }

    /**
     * Gets the y location of the building in the settlement.
     * @return y location (meters from settlement center - North: positive, South: negative).
     */
    public double getYLoc() {
        return yLoc;
    }

    /**
     * Gets the facing of the building.
     * @return facing (degrees from North clockwise).
     */
    public double getFacing() {
        return facing;
    }
}
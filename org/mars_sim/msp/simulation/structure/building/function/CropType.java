/**
 * Mars Simulation Project
 * CropType.java
 * @version 2.75 2004-03-18
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;

/**
 * The CropType class is a type of crop.
 */
public class CropType implements Serializable {
    
    // Data members
    private String name; // The name of the type of crop.
    private double growingTime; // The length of the crop type's growing phase.
   
    /**
     * Constructor
     * @param name - The name of the type of crop.
     * @param growingTime - Length of growing phase for crop. (millisols)
     */
    public CropType(String name, double growingTime) {
        this.name = name;
        this.growingTime = growingTime;
    }
    
    /**
     * Gets the crop type's name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the length of the crop type's growing phase.
     *
     * @return crop type's growing time in millisols.
     */
    public double getGrowingTime() {
        return growingTime;
    }
}
    

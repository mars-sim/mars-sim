/**
 * Mars Simulation Project
 * Farming.java
 * @version 2.75 2003-06-08
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function;

import java.util.List;
 
public interface Farming extends Function {
        
    public static final double HARVEST_MULTIPLIER = 10D;
    public static final double BASE_CROP_GROWING_TIME = 90000D;
    
    /**
     * Gets the farm's current crops.
     * @return collection of crops
     */
    public List getCrops();
    
    /**
     * Checks if farm currently requires work.
     * @return true if farm requires work
     */
    public boolean requiresWork();
    
    /**
     * Adds work time to the crops current phase.
     * @param workTime - Work time to be added (millisols)
     * @return workTime remaining after working on crop (millisols)
     */
    public double addWork(double workTime);
    
    /**
     * Adds harvested food to the farm.
     * @param harvest harvested food to add (kg.)
     */
    public void addHarvest(double harvest);
    
    /**
     * Gets the number of farmers currently working at the farm.
     *
     * @return number of farmers
     */
    public int getFarmerNum();
}

/**
 * Mars Simulation Project
 * InflatableGreenhouse.java
 * @version 2.75 2003-02-05
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.building.function.Farming;

/**
 * The InflatableGreenhouse class represents a 
 * generic inflatable greenhouse building.
 */
public class InflatableGreenhouse extends InhabitableBuilding implements Farming {
    
    // Power required for growing crops per kg of crop.
    private static final double POWER_GROWING_CROP = .1D;
    
    // Power required for sustaining crops per kg of crop.
    private static final double POWER_SUSTAINING_CROP = .05D;
    
    private Collection crops;
    private int numCrops;
    private double maxHarvest;
    
    /**
     * Constructor
     * @param manager - building manager.
     */
    public InflatableGreenhouse(BuildingManager manager) {
        // User InhabitableBulding constructor
        super("Inflatable Greenhouse", manager, 3);
        
        double floorSpace = 75;
        
        // Determine maximum harvest.
        maxHarvest = floorSpace * .8D * Farming.HARVEST_MULTIPLIER;
        
        // Determine number of crops.
        // One crop for every 5 square meters of floor space.
        // Minimum of one crop.
        int numCrops = (int) (floorSpace / 5D);
        if (numCrops == 0) numCrops = 1;
      
        // Create crops;
        crops = new ArrayList();
        for (int x=0; x < numCrops; x++) {
            crops.add(new Crop(Crop.getRandomCropType(), (maxHarvest / numCrops), this));
        }    
    }
    
    /**
     * Adds harvested food to the farm.
     * @param harvest harvested food to add (kg.)
     */
    public void addHarvest(double harvest) {
        getBuildingManager().getSettlement().getInventory().addResource(Resource.FOOD, harvest);
    }
    
    /**
     * Gets the farm's current crops.
     * @return collection of crops
     */
    public Collection getCrops() {
        return crops;
    }
    
    /**
     * Checks if farm currently requires work.
     * @return true if farm requires work
     */
    public boolean requiresWork() {
        boolean result = false;
        Iterator i = crops.iterator();
        while (i.hasNext()) {
            Crop crop = (Crop) i.next();
            if (crop.requiresWork()) result = true;
        }
        return result;
    }
    
    /**
     * Adds work time to the crops current phase.
     * @param workTime - Work time to be added (millisols)
     * @return workTime remaining after working on crop (millisols)
     */
    public double addWork(double workTime) {
        double workTimeRemaining = workTime;
        
        int needyCrops = 0;
        while (((needyCrops = getNeedyCrops()) > 0) && (workTimeRemaining > 0D)) {
            double maxCropTime = workTimeRemaining / needyCrops;
            Iterator i = crops.iterator();
            while (i.hasNext()) {
                Crop crop = (Crop) i.next();
                workTimeRemaining -= maxCropTime - crop.addWork(maxCropTime);
            }
        }
        
        return workTimeRemaining;
    }
    
    /**
     * Gets the number of crops that currently need work.
     * @return number of crops requiring work
     */
    private int getNeedyCrops() {
        int result = 0;
        Iterator i = crops.iterator();
        while (i.hasNext()) {
            Crop crop = (Crop) i.next();
            if (crop.requiresWork()) result++;
        }
        return result;
    }
    
    /**
     * Time passing for building.
     * Child building should override this method for things
     * that happen over time for the building.
     *
     * @param time amount of time passing (in millisols)
     */
    public void timePassing(double time) {

        // Add time to each crop.
        Iterator i = crops.iterator();
        int newCrops = 0;
        while (i.hasNext()) {
            Crop crop = (Crop) i.next();
            crop.timePassing(time);
            
            // Remove old crops.
            if (crop.getPhase().equals(Crop.FINISHED)) {
                i.remove();
                newCrops++;
            }
        }
        
        // Add any new crops.
        for (int x=0; x < newCrops; x++) {
            crops.add(new Crop(Crop.getRandomCropType(), (maxHarvest / numCrops), this));
        }    
    }  
    
    /**
     * Gets the power this building currently requires for full-power mode.
     * @return power in kW.
     */
    public double getFullPowerRequired() {
        
        // Power (kW) required for normal operations.
        double powerRequired = getPoweredDownPowerRequired();
        
        Iterator i = crops.iterator();
        while (i.hasNext()) {
            Crop crop = (Crop) i.next();
            if (crop.getPhase().equals(Crop.GROWING))
                powerRequired += (crop.getMaxHarvest() * POWER_GROWING_CROP);
        }
        
        return powerRequired;
    }
    
    /**
     * Gets the power the building requires for power-down mode.
     * @return power in kW.
     */
    public double getPoweredDownPowerRequired() {
        
        // Get power required for occupant life support.
        double powerRequired = getLifeSupportPowerRequired();
        
        // Add power required to sustain growing or harvest-ready crops.
        Iterator i = crops.iterator();
        while (i.hasNext()) {
            Crop crop = (Crop) i.next();
            if (crop.getPhase().equals(Crop.GROWING) || crop.getPhase().equals(Crop.HARVESTING)) {
                powerRequired += (crop.getMaxHarvest() * POWER_SUSTAINING_CROP);
            }
        }
        
        return powerRequired;
    } 
}

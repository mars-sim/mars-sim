/**
 * Mars Simulation Project
 * InflatableGreenhouse.java
 * @version 2.75 2003-05-30
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.malfunction.MalfunctionManager;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.function.*;
import org.mars_sim.msp.simulation.structure.building.function.impl.*;

/**
 * The InflatableGreenhouse class represents a 
 * generic inflatable greenhouse building.
 */
public class InflatableGreenhouse extends InhabitableBuilding implements Farming, Storage, ResourceProcessing {
    
    // Power down level for processes.
    private final static double POWER_DOWN_LEVEL = .5D;
    
    // Power required for growing crops per kg of crop.
    private static final double POWER_GROWING_CROP = .05D;
    
    // Power required for sustaining crops per kg of crop.
    private static final double POWER_SUSTAINING_CROP = .05D;
    
    private Collection crops;
    private int numCrops;
    private double maxHarvest;
    private Map resourceStorageCapacity;
    private ResourceProcessing processor;
    
    /**
     * Constructor
     * @param manager - building manager.
     */
    public InflatableGreenhouse(BuildingManager manager) {
        // User InhabitableBulding constructor
        super("Inflatable Greenhouse", manager, 3);
        
        // Create processor
        processor = new StandardResourceProcessing(this, POWER_DOWN_LEVEL);
        ResourceProcessManager processManager = processor.getResourceProcessManager();
        
        Inventory inv = getInventory();
        
        // Create carbon dioxide pump process
        ResourceProcess carbonDioxidePump = new ResourceProcess("carbon dioxide pump", inv);
        carbonDioxidePump.addMaxInputResourceRate(Resource.CARBON_DIOXIDE, .0001D, true);
        carbonDioxidePump.addMaxOutputResourceRate(Resource.CARBON_DIOXIDE, .0001D, false);
        processManager.addResourceProcess(carbonDioxidePump);
        
        double floorSpace = 75;
        
        // Determine maximum harvest.
        maxHarvest = floorSpace * .8D * Farming.HARVEST_MULTIPLIER;
        
        // Determine number of crops.
        // One crop for every 5 square meters of floor space.
        // Minimum of one crop.
        numCrops = (int) (floorSpace / 5D);
        if (numCrops == 0) numCrops = 1;
      
        // Create crops;
        crops = new ArrayList();
        Settlement settlement = manager.getSettlement();
        for (int x=0; x < numCrops; x++) {
            crops.add(new Crop(Crop.getRandomCropType(), (maxHarvest / numCrops), this, settlement.getMars(), settlement));
        }  
        
        // Set up resource storage capacity map.
        resourceStorageCapacity = new HashMap();
        resourceStorageCapacity.put(Resource.WATER, new Double(500D));
        resourceStorageCapacity.put(Resource.WASTE_WATER, new Double(500D));
        resourceStorageCapacity.put(Resource.CARBON_DIOXIDE, new Double(500D));
        resourceStorageCapacity.put(Resource.FOOD, new Double(500D));
        
        // Add resource storage capacity to settlement inventory.
        Iterator i = resourceStorageCapacity.keySet().iterator();
        while (i.hasNext()) {
            String resourceName = (String) i.next();
            double capacity = ((Double) resourceStorageCapacity.get(resourceName)).doubleValue();
            inv.setResourceCapacity(resourceName, inv.getResourceCapacity(resourceName) + capacity);
        }
        
        // Add scope string to malfunction manager.
        MalfunctionManager malfunctionManager = getMalfunctionManager();
        malfunctionManager.addScopeString("Farming");
        malfunctionManager.addScopeString("Storage");
        malfunctionManager.addScopeString("Resource Processing");
        malfunctionManager.addScopeString("Inhabitable Building");
        malfunctionManager.addScopeString("Inflatable Greenhouse");
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
        // Scott - I used the comparison criteria 00001D rather than 0D
        // because sometimes math anomolies result in workTimeRemaining
        // becoming very small double values and an endless loop occurs.
        while (((needyCrops = getNeedyCrops()) > 0) && (workTimeRemaining > 00001D)) {
            double maxCropTime = workTimeRemaining / (double) needyCrops;
            Iterator i = crops.iterator();
            while (i.hasNext()) {
                Crop crop = (Crop) i.next();
                workTimeRemaining -= (maxCropTime - crop.addWork(maxCropTime));
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
        super.timePassing(time);
        
        // Determine resource processing production level.
        double productionLevel = 0D;
        if (getPowerMode().equals(FULL_POWER)) productionLevel = 1D;
        else if (getPowerMode().equals(POWER_DOWN)) productionLevel = POWER_DOWN_LEVEL;
        
        // Process resources
        processor.getResourceProcessManager().processResources(time, productionLevel);
        
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
        Settlement settlement = getBuildingManager().getSettlement();
        for (int x=0; x < newCrops; x++) {
            crops.add(new Crop(Crop.getRandomCropType(), (maxHarvest / (double) numCrops), 
                this, settlement.getMars(), settlement));
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
    
    /**
     * Gets the building's resource process manager.
     * @return resource process manager
     */
    public ResourceProcessManager getResourceProcessManager() {
        return processor.getResourceProcessManager();
    }
    
    /**
     * Gets the power down mode resource processing level.
     * @return proportion of max processing rate (0D - 1D)
     */
    public double getPowerDownResourceProcessingLevel() {
        return processor.getPowerDownResourceProcessingLevel();
    }
    
    /** 
     * Gets a map of the resources this building is capable of
     * storing and their amounts in kg.
     * @return Map of resource keys and amount Double values.
     */
    public Map getResourceStorageCapacity() {
        return resourceStorageCapacity;
    }
}

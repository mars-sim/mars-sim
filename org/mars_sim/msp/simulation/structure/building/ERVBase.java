/**
 * Mars Simulation Project
 * ERVBase.java
 * @version 2.75 2003-02-10
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.building.function.*;

/**
 * The ERVBase class represents the base structure of an Earth Return Vehicle (ERV).
 * It has a Sebatier reactor to generate oxygen, water and methane from Martian air.
 */
public class ERVBase extends Building implements ResourceProcessing {
    
    private boolean processing; // True if ERVBase is processing chemicals.
    private double powerDownLevel;
    private ResourceProcessManager processManager;
    
    /**
     * Constructor
     * @param manager - building manager.
     */
    public ERVBase(BuildingManager manager) {
        // Use Bulding constructor
        super("Earth Return Vehicle (ERV) Base", manager);
        
        processing = true;
        powerDownLevel = .5D;
        
        // Set up resource processes.
        Inventory inv = manager.getSettlement().getInventory();
        processManager = new ResourceProcessManager(this, inv);
        
        // Create sabatier resource process.
        // CO2 + 4H2 = CH4 + 2H2O
        // Carbon Dioxide is pumped in from outside air.
        ResourceProcess sabatier = new ResourceProcess("Sebatier", inv);
        sabatier.addMaxInputResourceRate(Resource.CARBON_DIOXIDE, .00055D, true);
        sabatier.addMaxInputResourceRate(Resource.HYDROGEN, .0001D, false);
        sabatier.addMaxOutputResourceRate(Resource.METHANE, .0002D, false);
        sabatier.addMaxOutputResourceRate(Resource.WATER, .00045D, false);
        processManager.addResourceProcess(sabatier);
        
        // Create electrolysis resource process.
        // 2H2O = 2H2 + O2
        ResourceProcess electrolysis = new ResourceProcess("Electrolysis", inv);
        electrolysis.addMaxInputResourceRate(Resource.WATER, .0001D, false);
        electrolysis.addMaxOutputResourceRate(Resource.HYDROGEN, .000011D, false);
        electrolysis.addMaxOutputResourceRate(Resource.OXYGEN, .000089D, false);
        processManager.addResourceProcess(electrolysis);
        
        // Create CO2 reduction resource process.
        // 2CO2 = 2CO + O2
        // Carbon Dioxide is pumped in from outside air.
        // Carbon Monoxide is pumped out to outside air.
        ResourceProcess co2Reduction = new ResourceProcess("CO2 Reduction", inv);
        co2Reduction.addMaxInputResourceRate(Resource.CARBON_DIOXIDE, .000825D, true);
        co2Reduction.addMaxOutputResourceRate(Resource.OXYGEN, .0003D, false);
        co2Reduction.addMaxOutputResourceRate(Resource.CARBON_MONOXIDE, .000525D, true);
        processManager.addResourceProcess(co2Reduction);
        
        // ERVBase starts with 6 tonnes of hydrogen.
        inv.addResource(Resource.HYDROGEN, 6000D);
    }
    
    /**
     * Gets the power this building currently uses.
     * @return power in kW.
     */
    public double getPowerUsed() {
        // ERVBase has its own power supply.
        return 5D;
    }
    
    /**
     * Gets the power this building currently requires for full-power mode.
     * @return power in kW.
     */
    public double getFullPowerRequired() {
        double result = 0D;
        if (processing) result = 10D;
        return result;
    }
    
    /**
     * Gets the power the building requires for power-down mode.
     * @return power in kW.
     */
    public double getPoweredDownPowerRequired() {
        double result = 0D;
        if (processing) result = 5D;
        return result;
    }
    
    /**
     * Gets the building's resource process manager.
     * @return resource process manager
     */
    public ResourceProcessManager getResourceProcessManager() {
        return processManager;
    }
    
    /**
     * Gets the power down mode resource processing level.
     * @return proportion of max processing rate (0D - 1D)
     */
    public double getPowerDownResourceProcessingLevel() {
        return powerDownLevel;
    }
     
    /**
     * Time passing for building.
     * Child building should override this method for things
     * that happen over time for the building.
     *
     * @param time amount of time passing (in millisols)
     */
    public void timePassing(double time) {
        
        // Check for valid argument.
        if (time < 0D) throw new IllegalArgumentException("Time must be > 0D");
        
        // Determine resource processing production level.
        double productionLevel = 0D;
        if (powerMode.equals(FULL_POWER)) productionLevel = 1D;
        else if (powerMode.equals(POWER_DOWN)) productionLevel = powerDownLevel;
        
        Inventory inv = manager.getSettlement().getInventory();
        double hydrogenStart = inv.getResourceMass(Resource.HYDROGEN);
        double waterStart = inv.getResourceMass(Resource.WATER);
        double oxygenStart = inv.getResourceMass(Resource.OXYGEN);
        double methaneStart = inv.getResourceMass(Resource.METHANE);
        
        // Process resources
        processManager.processResources(time, productionLevel);
        
        double hydrogenEnd = inv.getResourceMass(Resource.HYDROGEN);
        double waterEnd = inv.getResourceMass(Resource.WATER);
        double oxygenEnd = inv.getResourceMass(Resource.OXYGEN);
        double methaneEnd = inv.getResourceMass(Resource.METHANE);
    } 
}

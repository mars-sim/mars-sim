/**
 * Mars Simulation Project
 * LivingAccommodationsImpl.java
 * @version 2.75 2003-04-15
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function.impl;

import java.io.Serializable;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.function.LivingAccommodations;
 
/**
 * Standard implementation of the LivingAccomodations function.
 */
public class StandardLivingAccommodations implements LivingAccommodations, Serializable {
        
    private Building building;
    private int capacity;
    
    /**
     * Constructor
     *
     * @param building the building this is implemented for.
     * @param capacity the number of people the building can accommodate.
     */
    public StandardLivingAccommodations(Building building, int capacity) {
        this.building = building;
        this.capacity = capacity;
    }
        
    /**
     * Gets the accommodation capacity of this building.
     *
     * @return number of accomodations.
     */
    public int getAccommodationCapacity() {
        return capacity;
    }
 
    /** 
     * Utilizes water for bathing, washing, etc. based on population.
     * @param time amount of time passing (millisols)
     */
    public void waterUsage(double time) {
        Settlement settlement = building.getBuildingManager().getSettlement();
        double waterUsagePerPerson = (LivingAccommodations.WASH_WATER_USAGE_PERSON_SOL / 1000D) * time;
        double waterUsageSettlement = waterUsagePerPerson * settlement.getCurrentPopulationNum();
        double buildingProportionCap = (double) capacity / 
            (double) settlement.getPopulationCapacity();
        double waterUsageBuilding = waterUsageSettlement * buildingProportionCap;
        
        Inventory inv = settlement.getInventory();
        double waterUsed = inv.removeResource(Resource.WATER, waterUsageBuilding);
        inv.addResource(Resource.WASTE_WATER, waterUsed);
    }
}

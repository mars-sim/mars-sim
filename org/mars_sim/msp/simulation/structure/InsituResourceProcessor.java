/**
 * Mars Simulation Project
 * InsituResourceProcessor.java
 * @version 2.74 2002-04-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure;

import org.mars_sim.msp.simulation.*;
import java.io.Serializable;

/** The InsituResourceProcessor class represents 
 *  a settlement's INSITU (on site) resource processor
 *  which chemically processes Martian air to generate 
 *  oxygen, water and methane (fuel).
 */
public class InsituResourceProcessor extends Facility 
                implements Serializable {

    /** Constructs a InsituResourceProcessor object.
     *  @param manager the manager of the processor facility.
     */
    public InsituResourceProcessor(FacilityManager manager) {
        
        // User Facility's constructor
        super(manager, "INSITU Resource Processor");

	// Add scope string to malfunction manager.
	malfunctionManager.addScopeString("InsituResourceProcessor");
    }
    
    /** Returns number of oxygen units that processor is generatng in a millisol.
     *  @return amount of oxygen (kg)
     */
    public double getOxygenRate() {
        int currentPop = manager.getSettlement().getCurrentPopulationNum();
        SimulationProperties properties = manager.getMars().getSimulationProperties();
        double result = (currentPop * properties.getPersonOxygenConsumption()) + 6D;
        result /= 1000D;
        return result;
    }
    
    /** Returns number of water units that processor is generating in a millisol.
     *  @return amount of water (kg)
     */
    public double getWaterRate() {
        int currentPop = manager.getSettlement().getCurrentPopulationNum();
        SimulationProperties properties = manager.getMars().getSimulationProperties();
        double result = (currentPop * properties.getPersonWaterConsumption()) + 6D;
        result /= 1000D;
        return result;
    }
    
    /** Returns number of fuel units that processor is generating in a millisol.
     *  @return amount of fuel (kg) 
     */
    public double getFuelRate() {
        int currentPop = manager.getSettlement().getCurrentPopulationNum();
        double result = (currentPop * 10D) + 6D;
        result /= 1000D;
        return result;
    }
    
    /** Override Facility's timePassing method to allow for resource processing.
     *  @param amount of time passing (in millisols) 
     */
    void timePassing(double time) {
        Inventory inv = manager.getSettlement().getInventory();
        inv.addResource(Inventory.OXYGEN, getOxygenRate() * time);
        inv.addResource(Inventory.WATER, getWaterRate() * time);
        inv.addResource(Inventory.FUEL, getFuelRate() * time);

	malfunctionManager.activeTimePassing(time);
    }
}


/**
 * Mars Simulation Project
 * InsituResourceProcessorFacility.java
 * @version 2.73 2001-12-21
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.Serializable;
/** The InsituResourceProcessorFacility class represents 
 *  a settlement's INSITU (on site) resource processor
 *  which chemically processes Martian air to generate 
 *  oxygen, water and methane (fuel).
 */
public class InsituResourceProcessorFacility extends Facility 
                implements Serializable {

    private StoreroomFacility stores;  // The settlement's storerooms

    /** Constructs a InsituResourceProcessorFacility object.
     *  @param manager the manager of the processor facility.
     */
    public InsituResourceProcessorFacility(FacilityManager manager) {
        
        // User Facility's constructor
        super(manager, "INSITU Resource Processor");

        stores = (StoreroomFacility) manager.getFacility("Storerooms");
    }
    
    /** Returns number of oxygen units that processor is generatng in a millisol.
     *  @return amount of oxygen (kg)
     */
    public double getOxygenRate() {
        int currentPop = manager.getSettlement().getCurrentPopulation();
        SimulationProperties properties = manager.getVirtualMars().getSimulationProperties();
        double result = (currentPop * properties.getPersonOxygenConsumption()) + 6D;
        result /= 1000D;
        return result;
    }
    
    /** Returns number of water units that processor is generating in a millisol.
     *  @return amount of water (kg)
     */
    public double getWaterRate() {
        int currentPop = manager.getSettlement().getCurrentPopulation();
        SimulationProperties properties = manager.getVirtualMars().getSimulationProperties();
        double result = (currentPop * properties.getPersonWaterConsumption()) + 6D;
        result /= 1000D;
        return result;
    }
    
    /** Returns number of fuel units that processor is generating in a millisol.
     *  @return amount of fuel (kg) 
     */
    public double getFuelRate() {
        int currentPop = manager.getSettlement().getCurrentPopulation();
        double result = (currentPop * 10D) + 6D;
        result /= 1000D;
        return result;
    }
    
    /** Override Facility's timePassing method to allow for resource processing.
     *  @param amount of time passing (in millisols) 
     */
    void timePassing(double time) {
        stores.addOxygen(getOxygenRate() * time);
        stores.addWater(getWaterRate() * time);
        stores.addFuel(getFuelRate() * time);
    }
}


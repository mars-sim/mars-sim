/**
 * Mars Simulation Project
 * InsituResourceProcessorFacility.java
 * @version 2.72 2001-07-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The InsituResourceProcessorFacility class represents 
 *  a settlement's INSITU (on site) resource processor
 *  which chemically processes Martian air to generate 
 *  oxygen, water and methane (fuel).
 */
public class InsituResourceProcessorFacility extends Facility {

    /** Constructs a InsituResourceProcessorFacility object.
     *  @param manager the manager of the processor facility.
     */
    public InsituResourceProcessorFacility(FacilityManager manager) {
        
        // User Facility's constructor
        super(manager, "INSITU Resource Processor");
    }
    
    /** Returns number of oxygen units that processor can generate in a millisol.
     *  @return amount of oxygen (in units)
     */
    public double getOxygenRate() {
        LivingQuartersFacility quarters = (LivingQuartersFacility) manager.getFacility("Living Quarters");
        int normalPop = quarters.getNormalCapacity();
        int currentPop = quarters.getCurrentPopulation();
        double result = normalPop;
        if (currentPop > normalPop) result = currentPop;
        result /= 1000D;
        return result;
    }
    
    /** Returns number of water units that processor can generate in a millisol.
     *  @return amount of water (in units)
     */
    public double getWaterRate() {
        LivingQuartersFacility quarters = (LivingQuartersFacility) manager.getFacility("Living Quarters");
        int normalPop = quarters.getNormalCapacity();
        int currentPop = quarters.getCurrentPopulation();
        double result = normalPop;
        if (currentPop > normalPop) result = currentPop;
        result /= 1000D;
        return result;
    }
    
    /** Returns number of fuel units that processor can generate in a millisol.
     *  @return amount of fuel (in units) 
     */
    public double getFuelRate() {
        LivingQuartersFacility quarters = (LivingQuartersFacility) manager.getFacility("Living Quarters");
        int normalPop = quarters.getNormalCapacity();
        int currentPop = quarters.getCurrentPopulation();
        double result = normalPop;
        if (currentPop > normalPop) result = currentPop;
        result /= 1000D;
        return result;
    }
    
    /** Override Facility's timePassing method to allow for resource processing.
     *  @param amount of time passing (in millisols) 
     */
    void timePassing(double time) {
        StoreroomFacility stores = (StoreroomFacility) manager.getFacility("Storerooms");
        stores.addOxygen(getOxygenRate() * time);
        stores.addWater(getWaterRate() * time);
        stores.addFuel(getFuelRate() * time);
    }
}


/**
 * Mars Simulation Project
 * InsituResourceProcessorFacility.java
 * @version 2.71 2000-12-04
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
    
    /** Returns number of oxygen units that processor can generate in a day.
     *  @return number of oxygen units that processor can generate in a day.
     */
    public double getOxygenRate() {
        LivingQuartersFacility quarters = (LivingQuartersFacility) manager.getFacility("Living Quarters");
        int normalPop = quarters.getNormalCapacity();
        int currentPop = quarters.getCurrentPopulation();
        int result = normalPop;
        if (currentPop > normalPop) result = normalPop;
        return result;
    }
    
    /** Returns number of water units that processor can generate in a day.
     *  @return number of water units that processor can generate in a day.
     */
    public double getWaterRate() {
        LivingQuartersFacility quarters = (LivingQuartersFacility) manager.getFacility("Living Quarters");
        int normalPop = quarters.getNormalCapacity();
        int currentPop = quarters.getCurrentPopulation();
        int result = normalPop;
        if (currentPop > normalPop) result = normalPop;
        return result;
    }
    
    /** Returns number of fuel units that processor can generate in a day.
     *  @return number of fuel units that processor can generate in a day.
     */
    public double getFuelRate() {
        LivingQuartersFacility quarters = (LivingQuartersFacility) manager.getFacility("Living Quarters");
        int normalPop = quarters.getNormalCapacity();
        int currentPop = quarters.getCurrentPopulation();
        int result = normalPop;
        if (currentPop > normalPop) result = normalPop;
        return result;
    }
    
    /** Override Facility's timePasses method to allow for resource processing.
     *  @param number of seconds of time passing
     */
    void timePasses(int seconds) {
        StoreroomFacility stores = (StoreroomFacility) manager.getFacility("Storerooms");
        stores.addOxygen(getOxygenRate() / 24D / 60D / 60D * (double) seconds);
        stores.addWater(getWaterRate() / 24D / 60D / 60D * (double) seconds);
        stores.addFuel(getFuelRate() / 24D / 60D / 60D * (double) seconds);
    }
}


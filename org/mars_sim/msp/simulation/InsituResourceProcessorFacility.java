/**
 * Mars Simulation Project
 * InsituResourceProcessorFacility.java
 * @version 2.71 2000-12-03
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The InsituResourceProcessorFacility class represents 
 *  a settlement's INSITU (on site) resource processor
 *  which chemically processes Martian air to generate 
 *  oxygen, water and methane (fuel).
 */
public class InsituResourceProcessorFacility extends Facility {
    
    // Data members
    private double oxygenUnits;  /* Number of oxygen units that processor 
                                    can generate in a day. */
    private double waterUnits;   /* Number of water units that processor
                                    can generate in a day. */
    private double fuelUnits;    /* Number of fuel units that processor
                                    can generate in a day. */

    /** Constructs a InsituResourceProcessorFacility object.
     *  @param manager the manager of the processor facility.
     */
    public InsituResourceProcessorFacility(FacilityManager manager) {
        
        // User Facility's constructor
        super(manager, "INSITU Resource Processor");
        
        LivingQuartersFacility quarters = (LivingQuartersFacility) manager.getFacility("Living Quarters");
        int normalPop = quarters.getNormalCapacity();
        
        // Initialize data members
        oxygenUnits = (double) normalPop;
        waterUnits = (double) normalPop;
        fuelUnits = (double) normalPop;
    }
    
    /** Returns number of oxygen units that processor can generate in a day.
     *  @return number of oxygen units that processor can generate in a day.
     */
    public double getOxygenRate() {
        return oxygenUnits;
    }
    
    /** Returns number of water units that processor can generate in a day.
     *  @return number of water units that processor can generate in a day.
     */
    public double getWaterRate() {
        return waterUnits;
    }
    
    /** Returns number of fuel units that processor can generate in a day.
     *  @return number of fuel units that processor can generate in a day.
     */
    public double getFuelRate() {
        return fuelUnits;
    }
    
    /** Override Facility's timePasses method to allow for resource processing.
     *  @param number of seconds of time passing
     */
    void timePasses(int seconds) {
        StoreroomFacility stores = (StoreroomFacility) manager.getFacility("Storerooms");
        stores.addOxygen(oxygenUnits / 24D / 60D / 60D * (double) seconds);
        stores.addWater(waterUnits / 24D / 60D / 60D * (double) seconds);
        stores.addFuel(fuelUnits / 24D / 60D / 60D * (double) seconds);
    }
}


/**
 * Mars Simulation Project
 * LivingQuartersFacility.java
 * @version 2.74 2002-01-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure;

import java.io.Serializable;
/**
 * The LivingQuartersFacility class represents the living quarters in a settlement.
 */

public class LivingQuartersFacility extends Facility implements Serializable {

    /** Constructor for random creation. 
     *  @param manager the living quarter's facility manager
     */
    LivingQuartersFacility(FacilityManager manager) {

        // Use Facility's constructor.
        super(manager, "Living Quarters");
    }
}

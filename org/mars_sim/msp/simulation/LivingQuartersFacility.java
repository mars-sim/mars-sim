/**
 * Mars Simulation Project
 * LivingQuartersFacility.java
 * @version 2.73 2001-11-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

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

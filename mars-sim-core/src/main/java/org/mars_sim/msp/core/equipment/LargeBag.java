/**
 * Mars Simulation Project
 * LargeBag.java
 * @version 2.85 2008-09-13
 * @author Scott Davis
 */

package org.mars_sim.msp.core.equipment;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.resource.Phase;

/**
 * A large bag container for holding solid amount resources.
 */
public class LargeBag extends Equipment implements Container, Serializable {

    // Static data members
    public static final String TYPE = "Large Bag";
    public static final double CAPACITY = 200D;
    public static final double EMPTY_MASS = .4D;

    /**
     * Constructor
     * @param location the location of the large bag.
     * @throws Exception if error creating large bag.
     */
    public LargeBag(Coordinates location) throws Exception {
        // Use Equipment constructor
        super(TYPE, location);
        
        // Sets the base mass of the bag.
        setBaseMass(EMPTY_MASS);
        
        // Set the solid capacity.
        getInventory().addAmountResourcePhaseCapacity(Phase.SOLID, CAPACITY);
    }
    
    /**
     * Gets the phase of resources this container can hold.
     * @return resource phase.
     */
    public Phase getContainingResourcePhase() {
        return Phase.SOLID;
    }
}
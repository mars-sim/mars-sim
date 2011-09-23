/**
 * Mars Simulation Project
 * MockUnit2.java
 * @version 3.01 2011-09-23
 * @author Scott Davis
 */

package org.mars_sim.msp.core;

import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.resource.Phase;

/**
 * A mock unit for testing that implements the container interface.
 */
public class MockUnit3 extends Unit implements Container {

    // Data members.
    private Phase resourcePhase;
    
    public MockUnit3(Phase resourcePhase) {
        // Use Unit constructor.
        super("Mock Unit 3", new Coordinates(0D, 0D));
        
        this.resourcePhase = resourcePhase;
        setBaseMass(30D);
    }

    @Override
    public Phase getContainingResourcePhase() {
        return resourcePhase;
    }
}
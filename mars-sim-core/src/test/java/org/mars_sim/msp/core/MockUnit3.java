/**
 * Mars Simulation Project
 * MockUnit3.java
 * @version 3.1.0 2018-06-27
 * @author Scott Davis
 */

package org.mars_sim.msp.core;

import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.resource.PhaseType;

/**
 * A mock unit for testing that implements the container interface.
 */
public class MockUnit3 extends Unit implements Container {

    // Data members.
    private PhaseType resourcePhase;

    public MockUnit3(PhaseType resourcePhase) {
        // Use Unit constructor.
        super("Mock Unit 3", new Coordinates(0D, 0D));
        
        this.resourcePhase = resourcePhase;
        setBaseMass(30D);
    }

    @Override
    public PhaseType getContainingResourcePhase() {
        return resourcePhase;
    }

    @Override
    public double getTotalCapacity() {
        return 50D;
    }

	@Override
	protected UnitType getUnitType() {
		return UnitType.VEHICLE;
	}

}
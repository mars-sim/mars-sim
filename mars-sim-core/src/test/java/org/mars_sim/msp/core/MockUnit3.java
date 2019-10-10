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
    
	/** The unit count for this settlement. */
	private static int uniqueCount = Unit.FIRST_SETTLEMENT_UNIT_ID;
	/** Unique identifier for this settlement. */
	private int identifier;

	/**
	 * Must be synchronised to prevent duplicate ids being assigned via different
	 * threads.
	 * 
	 * @return
	 */
	private static synchronized int getNextIdentifier() {
		return uniqueCount++;
	}
	
	/**
	 * Get the unique identifier for this settlement
	 * 
	 * @return Identifier
	 */
	public int getIdentifier() {
		return identifier;
	}
	
	public void incrementID() {
		// Gets the identifier
		this.identifier = getNextIdentifier();
	}
	

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


}
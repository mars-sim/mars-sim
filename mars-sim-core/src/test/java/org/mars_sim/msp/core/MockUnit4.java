/**
 * Mars Simulation Project
 * MockUnit4.java
 * @version 3.1.0 2019-10-08
 * @author Manny Kung
 */

package org.mars_sim.msp.core;

/**
 * A mock unit used for unit testing.
 */
public class MockUnit4 extends Unit {
	
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
	
	
	public MockUnit4(String name) {
		// Use Unit constructor.
		super(name, new Coordinates(0D, 0D));
		
		setBaseMass(10D);
	}

}
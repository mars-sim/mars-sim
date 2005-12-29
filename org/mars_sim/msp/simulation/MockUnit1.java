/**
 * Mars Simulation Project
 * MockUnit1.java
 * @version 2.79 2005-12-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/**
 * A mock unit used for unit testing.
 */
public class MockUnit1 extends Unit {
	
	public MockUnit1() {
		// Use Unit constructor.
		super("Mock Unit 1", new Coordinates(0D, 0D));
		
		baseMass = 10D;
		inventory = new Inventory(this);
	}
}
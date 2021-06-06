/**
 * Mars Simulation Project
 * MockUnit2.java
 * @version 3.1.0 2018-06-27
 * @author Scott Davis
 */

package org.mars_sim.msp.core;

/**
 * A mock unit used for unit testing.
 */
public class MockUnit2 extends Unit {

	public MockUnit2() {
		// Use Unit constructor.
		super("Mock Unit 2", new Coordinates(0D, 0D));
		
		setBaseMass(20D);
	}

	@Override
	protected UnitType getUnitType() {
		return UnitType.VEHICLE;
	}

}
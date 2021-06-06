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
	
	public MockUnit4(String name) {
		// Use Unit constructor.
		super(name, new Coordinates(0D, 0D));
		
		setBaseMass(10D);
	}

	@Override
	protected UnitType getUnitType() {
		return UnitType.VEHICLE;
	}
}
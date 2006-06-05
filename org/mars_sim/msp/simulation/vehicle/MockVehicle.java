package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;

public class MockVehicle extends Vehicle {

	public MockVehicle(Settlement settlement) throws Exception {
		// Use Vehicle constructor
		super("Mock Vehicle", "Mock Vehicle", settlement, 10D, 100D, 1D);
	}
	
	public boolean isAppropriateOperator(VehicleOperator operator) {
		// TODO Auto-generated method stub
		return false;
	}

	public AmountResource getFuelType() {
		// TODO Auto-generated method stub
		return null;
	}
}
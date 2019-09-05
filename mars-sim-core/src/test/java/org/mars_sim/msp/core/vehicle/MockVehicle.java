package org.mars_sim.msp.core.vehicle;

import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;

public class MockVehicle extends Vehicle {

	public MockVehicle(Settlement settlement) throws Exception {
		// Use Vehicle constructor
		super("Mock Vehicle", "Mock Vehicle", settlement, 10D, 5000D, 57D, 100D);
	}

	public boolean isAppropriateOperator(VehicleOperator operator) {
		return false;
	}

	public int getFuelType() {
		return ResourceUtil.methaneID;
	}

    @Override
    public void determinedSettlementParkedLocationAndFacing() {
    	// Do nothing
    }

	@Override
	public String getNickName() {
		return getName();
	}
	
	@Override
	public String getImmediateLocation() {
		return getLocationTag().getSettlementName();
	}

}
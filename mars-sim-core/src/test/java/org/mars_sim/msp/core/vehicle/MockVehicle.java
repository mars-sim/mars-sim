package org.mars_sim.msp.core.vehicle;

import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;

public class MockVehicle extends Vehicle {

	public MockVehicle(Settlement settlement) throws Exception {
		// Use Vehicle constructor
		super("Mock Vehicle", "Mock Vehicle", settlement, 10D, 100D, 1D, 100D);
	}

	public boolean isAppropriateOperator(VehicleOperator operator) {
		return false;
	}

	public AmountResource getFuelType() {
		return null;
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

	@Override
	public Settlement getBuriedSettlement() {
		// TODO Auto-generated method stub
		return null;
	}

}
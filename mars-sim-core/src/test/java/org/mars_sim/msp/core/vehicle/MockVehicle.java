package org.mars_sim.msp.core.vehicle;

import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;

@SuppressWarnings("serial")
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
    public void findNewParkingLoc() {
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
	public double getBaseWearLifetime() {
		return 668_000;
	}

	@Override
	public double getTerrainGrade() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getElevation() {
		// TODO Auto-generated method stub
		return 0;
	}

}
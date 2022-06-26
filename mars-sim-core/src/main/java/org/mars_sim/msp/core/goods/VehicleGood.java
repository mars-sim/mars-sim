package org.mars_sim.msp.core.goods;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.vehicle.VehicleConfig;
import org.mars_sim.msp.core.vehicle.VehicleType;

class VehicleGood extends Good {

    // TODO, should load of an instance and not a static
    private static VehicleConfig vehicleConfig = SimulationConfig.instance().getVehicleConfiguration();

    private GoodType goodType;

    public VehicleGood(String name) {
        super(name, VehicleType.convertName2ID(name));

        VehicleType vehicleType = VehicleType.convertNameToVehicleType(name);
        switch(vehicleType) {
		case DELIVERY_DRONE:
		case LUV:
			goodType = GoodType.VEHICLE_HEAVY;
            break;

		case EXPLORER_ROVER:
            goodType = GoodType.VEHICLE_MEDIUM;
            break;

		case TRANSPORT_ROVER:
            goodType =  GoodType.VEHICLE_HEAVY;
            break;

		case CARGO_ROVER:
            goodType = GoodType.VEHICLE_HEAVY;
            break;

        default:
            throw new IllegalArgumentException(name + " has unknown vehicle type.");
        }

    }

    @Override
    public GoodCategory getCategory() {
        return GoodCategory.VEHICLE;
    }

    @Override
    public double getMassPerItem() {
        return vehicleConfig.getVehicleSpec(getName()).getEmptyMass();
    }

    @Override
    public GoodType getGoodType() {
        return goodType;
    }
}

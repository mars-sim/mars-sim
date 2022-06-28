/*
 * Mars Simulation Project
 * VehicleGood.java
 * @date 2022-06-26
 * @author Barry Evans
 */
package org.mars_sim.msp.core.goods;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.VehicleConfig;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * This represents a type of Vehicle that can be traded.
 */
class VehicleGood extends Good {
	
	private static final long serialVersionUID = 1L;
	
	private static final int VEHICLE_VALUE = 20;
	private static final int LUV_VALUE = 750;
	private static final int DRONE_VALUE = 50;

    // TODO, should load of an instance and not a static
    private static VehicleConfig vehicleConfig = SimulationConfig.instance().getVehicleConfiguration();

    private GoodType goodType;
    private VehicleType vehicleType;

    public VehicleGood(String name) {
        super(name, VehicleType.convertName2ID(name));

        this.vehicleType = VehicleType.convertNameToVehicleType(name);
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

    @Override
    protected double computeCostModifier() {
        switch(vehicleType) {
            case LUV:
                return LUV_VALUE;
            case DELIVERY_DRONE:
                return DRONE_VALUE;
            default:
                return VEHICLE_VALUE;
        }
    }

    @Override
    public double getNumberForSettlement(Settlement settlement) {
        final String vName = getName();
		double number = settlement.getAllAssociatedVehicles().stream()
			                    .filter(v -> vName.equalsIgnoreCase(v.getDescription()))
                                .count();

		// Get the number of vehicles that will be produced by ongoing manufacturing
		// processes.
		number += getManufacturingProcessOutput(settlement);

		return number;
    }

    @Override
    double getPrice(Settlement settlement, double value) {
        double mass = CollectionUtils.getVehicleTypeBaseMass(vehicleType);
        double quantity = settlement.findNumVehiclesOfType(vehicleType);
        double factor = Math.log(mass/1600.0 + 1) / (5 + Math.log(quantity + 1));
        return getCostOutput() * (1 + 2 * factor * Math.log(value + 1));
    }
}

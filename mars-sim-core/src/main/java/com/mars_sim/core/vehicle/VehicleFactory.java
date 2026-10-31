/*
 * Mars Simulation Project
 * VehicleFactory.java
 * @date 2023-04-17
 * @author Barry Evans
 */
package com.mars_sim.core.vehicle;

import java.util.List;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.structure.Settlement;

/**
 * Static class to create Vehicles
 */
public final class VehicleFactory {
	
	// Name format for numbers units
	private static final String VEHICLE_TAG_NAME = "%s %03d";
		
    private VehicleFactory() {
        // Prevent instantiation
    }
    
    /**
     * Builds a new Vehicle for a vehicle specification at a particular settlement.
     * 
     * @param unitMgr Owning manager of Units
     * @param owner Owning settlement for the new Vehicle
     * @param specName Specification to create.
     * @return
     */
    public static Vehicle createVehicle(UnitManager unitMgr, Settlement owner, String specName) {
        Authority sponsor = owner.getReportingAuthority();

        var vehicleConfig = SimulationConfig.instance().getVehicleConfiguration();
        
        VehicleSpec spec = vehicleConfig.getVehicleSpec(specName);
        VehicleType type = spec.getType();
     
		String name = generateName(unitMgr, type, sponsor);

		Vehicle vehicle = switch(spec.getType()) {
			case LUV -> new LightUtilityVehicle(name, spec, owner);
			case PASSENGER_DRONE, DELIVERY_DRONE, CARGO_DRONE -> new Drone(name, spec, owner);
			case EXPLORER_ROVER, LONG_RANGE_EXPLORER, TRANSPORT_ROVER, CARGO_ROVER -> new Rover(name, spec, owner);
        };

        unitMgr.addUnit(vehicle);
        
        return vehicle;
    }

	/**
	 * Generates a new name for the Vehicle; potentially this may be a preconfigured name.
	 * 
	 * or an auto-generated one.
	 * @param type
	 * @param sponsor Sponsor.
	 * @return
	 */
	private static String generateName(UnitManager unitMgr, VehicleType type, Authority sponsor) {
		String result = null;
		String baseName = null;

		switch (type) {
			case VehicleType.LUV -> baseName = "LUV";
			case VehicleType.PASSENGER_DRONE -> baseName = "P-Drone";
			case VehicleType.DELIVERY_DRONE -> baseName = "D-Drone";
			case VehicleType.CARGO_DRONE -> baseName = "C-Drone";
			default -> {
					List<String> usedName = unitMgr.getVehicles().stream()
									.map(Vehicle::getName).toList();
					result = sponsor.getVehicleNames().generateName(usedName);
					baseName = type.name();
				}
  		}

		if (result == null) {
			int number = unitMgr.incrementTypeCount(type.name());
			result = String.format(VEHICLE_TAG_NAME, baseName, number);
		}
		return result;
	}
	
	/**
	 * Gets the default mass for a VehicleType. This default is fixed and the mass in the VehicleSpec
	 * should always be used first.
	 */
	public static double getUsualMass(VehicleType type) {
		return switch(type) {
			case LUV -> 500;
			case PASSENGER_DRONE -> 200;
			case DELIVERY_DRONE -> 150;
			case CARGO_DRONE -> 150;
			case EXPLORER_ROVER -> 500;
			case LONG_RANGE_EXPLORER -> 900;
			case TRANSPORT_ROVER -> 1300;
			case CARGO_ROVER -> 1300;
		};
	}
}
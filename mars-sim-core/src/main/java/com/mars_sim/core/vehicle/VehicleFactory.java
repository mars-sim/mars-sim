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
	private static VehicleConfig vehicleConfig = SimulationConfig.instance().getVehicleConfiguration();

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

        Vehicle vehicle = null;
        VehicleSpec spec = vehicleConfig.getVehicleSpec(specName);

		String name = generateName(unitMgr, spec.getType(), sponsor);
        switch(spec.getType()) {
			case LUV:
            	vehicle = new LightUtilityVehicle(name, spec, owner);
				break;
			case PASSENGER_DRONE, DELIVERY_DRONE, CARGO_DRONE:
           		vehicle = new Drone(name, spec, owner);
				break;
			case EXPLORER_ROVER, TRANSPORT_ROVER, CARGO_ROVER:
            	vehicle = new Rover(name, spec, owner);
				break;
        }

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
}
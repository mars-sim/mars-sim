/*
 * Mars Simulation Project
 * VehicleFactory.java
 * @date 2023-04-17
 * @author Barry Evans
 */
package com.mars_sim.core.vehicle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.tools.util.RandomUtil;

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
			case DELIVERY_DRONE:
           		vehicle = new Drone(name, spec, owner);
				break;
			case EXPLORER_ROVER, TRANSPORT_ROVER, CARGO_ROVER:
            	vehicle = new Rover(name, spec, owner);
				break;
        }
        
        // Call addOwnedVehicle prior to addUnit()
        owner.addOwnedVehicle(vehicle);

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

		if (type == VehicleType.LUV) {
			baseName = "LUV";
		}
		else if (type == VehicleType.DELIVERY_DRONE) {
			baseName = "Drone";
		}
		else {
			List<String> possibleNames = sponsor.getVehicleNames();
			if (!possibleNames.isEmpty()) {
				List<String> availableNames = new ArrayList<>(possibleNames);
				Collection<Vehicle> vehicles = unitMgr.getVehicles();
				List<String> usedNames = vehicles.stream()
								.map(Vehicle::getName).toList();
				availableNames.removeAll(usedNames);

				if (!availableNames.isEmpty()) {
					result = availableNames.get(RandomUtil.getRandomInt(availableNames.size() - 1));
				}
			}
		}

		if (result == null) {
			int number = unitMgr.incrementTypeCount(type.name());
			result = String.format(VEHICLE_TAG_NAME, baseName, number);
		}
		return result;
	}
}
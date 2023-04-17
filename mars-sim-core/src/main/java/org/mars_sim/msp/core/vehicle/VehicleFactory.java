package org.mars_sim.msp.core.vehicle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;

/*
 * Mars Simulation Project
 * VehicleFactory.java
 * @date 2023-04-17
 * @author Barry Evans
 */
public final class VehicleFactory {
	
	// Name format for numbers units
	private static final String VEHICLE_TAG_NAME = "%s %03d";

    private VehicleFactory() {
        // Prevent instantiation
    }
    
    /**
     * Build a new Vehicle for a vehicle specification at a particular Settlement.
     * @param unitMgr Owning manager of Units
     * @param owner Owning Settlemetn fo the new Vehicle
     * @param specName Specification to create.
     * @return
     */
    public static Vehicle createVehicle(UnitManager unitMgr, Settlement owner, String specName) {
        ReportingAuthority sponsor = owner.getSponsor();

        Vehicle vehicle = null;
        String name = generateName(unitMgr, specName, sponsor);
        if (LightUtilityVehicle.NAME.equalsIgnoreCase(specName)) {
            vehicle = new LightUtilityVehicle(name, specName, owner);
        } 
        else if (VehicleType.DELIVERY_DRONE.getName().equalsIgnoreCase(specName)) {
            vehicle = new Drone(name, specName, owner);
        }
        else {
            vehicle = new Rover(name, specName, owner);
        }
        unitMgr.addUnit(vehicle);
        owner.addOwnedVehicle(vehicle);

        return vehicle;
    }

	/**
	 * Generate a new name for the Vehicle; potentially this may be a preconfigured name
	 * or an auto-generated one.
	 * @param type
	 * @param sponsor Sponsor.
	 * @return
	 */
	private static String generateName(UnitManager unitMgr, String type, ReportingAuthority sponsor) {
		String result = null;
		String baseName = type;

		if (type != null && type.equalsIgnoreCase(LightUtilityVehicle.NAME)) {
			baseName = "LUV";
		}
		else if (type != null && type.equalsIgnoreCase(VehicleType.DELIVERY_DRONE.getName())) {
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
			int number = unitMgr.incrementTypeCount(type);
			result = String.format(VEHICLE_TAG_NAME, baseName, number);
		}
		return result;
	}
}
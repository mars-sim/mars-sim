/*
 * Mars Simulation Project
 * VehicleFactory.java
 * @date 2023-04-17
 * @author Barry Evans
 */
package com.mars_sim.core.vehicle;

import java.util.EnumMap;
import java.util.List;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.unit.AbstractMobileUnit;

/**
 * Static class to create Vehicles
 */
public final class VehicleFactory {
	
	private static final SimLogger logger = SimLogger.getLogger(VehicleFactory.class.getName());
	// Default mass for uncalculated types
	protected static final double DEFAULT_MASS = 0.0001;
	// Name format for numbers units
	private static final String VEHICLE_TAG_NAME = "%s %03d";

	private static final EnumMap<VehicleType, Double> massMap = new EnumMap<>(VehicleType.class);
	
	private static VehicleConfig vehicleConfig;
	
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
        if (vehicleConfig == null)
        	vehicleConfig = SimulationConfig.instance().getVehicleConfiguration();
        
        VehicleSpec spec = vehicleConfig.getVehicleSpec(specName);
        VehicleType type = spec.getType();
     
		String name = generateName(unitMgr, type, sponsor);
        switch(spec.getType()) {
			case LUV:
            	vehicle = new LightUtilityVehicle(name, spec, owner);
				break;
			case PASSENGER_DRONE, DELIVERY_DRONE, CARGO_DRONE:
           		vehicle = new Drone(name, spec, owner);
				break;
			case EXPLORER_ROVER, LONG_RANGE_EXPLORER, TRANSPORT_ROVER, CARGO_ROVER:
            	vehicle = new Rover(name, spec, owner);
				break;
        }

        double usualMass = 0; 

        if (massMap.isEmpty() || !massMap.containsKey(type)) {
    		usualMass = calculateUsualMass(type);
    		massMap.put(type, usualMass);
    	}
    	else {
    		usualMass = massMap.get(type);
    	}
    	
        if (vehicle instanceof AbstractMobileUnit amu) {
    		amu.setBaseMass(usualMass);
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
	
	/**
	 * Calculates usual mass by accounting for the mass of all input parts.
	 * 
	 * @param type
	 * @return
	 */
	public static double calculateUsualMass(VehicleType type) {
	
		ManufactureProcessInfo manufactureProcessInfo = null;
		
		var manufactureConfig = SimulationConfig.instance().getManufactureConfiguration();
		
		for (ManufactureProcessInfo info : manufactureConfig.getManufactureProcessList()) {
			if (info.getName().equalsIgnoreCase("Assemble " + type.getName())) {
	        	manufactureProcessInfo = info;
	        	return info.getInputList().stream().mapToDouble(ProcessItem::getMass).sum();
			}
		}

		if (manufactureProcessInfo == null)
			logger.config("Unable to find EVA suit part IDs.");
		
		return 0;
	}
	
	/**
	 * Gets the empty/usual mass.
	 * 
	 * @param type
	 * @return
	 */
	public static double getUsualMass(VehicleType type) {
		if (massMap.containsKey(type))
			return massMap.get(type);
		return DEFAULT_MASS;
	}
}
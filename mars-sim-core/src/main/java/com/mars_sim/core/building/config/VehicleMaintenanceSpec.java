/*
 * Mars Simulation Project
 * VehicleMaintenanceSpec.java
 * @date 2025-09-07
 * @author Barry Evans
 */
package com.mars_sim.core.building.config;

import java.util.Set;

/**
 * Defines the specifications for a VehicleMaintenance function
 * by adding in the positions for parking.
 */
public class VehicleMaintenanceSpec extends FunctionSpec {

    private Set<NamedPosition> utility;
    private Set<NamedPosition> flyer;
    private Set<NamedPosition> rover;

    VehicleMaintenanceSpec(FunctionSpec base, Set<NamedPosition> roverParking,
                    Set<NamedPosition> utilityParking, Set<NamedPosition> flyerParking) {
        super(base);

        this.rover = roverParking;
        this.flyer = flyerParking;
        this.utility = utilityParking;
    }

    public Set<NamedPosition> getUtilityParking() {
        return utility;
    }

    public Set<NamedPosition> getFlyerParking() {
        return flyer;
    }

    public Set<NamedPosition> getRoverParking() {
        return rover;
    }
}

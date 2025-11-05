package com.mars_sim.core.building.function;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;


import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.config.VehicleMaintenanceSpec;
import com.mars_sim.core.map.location.LocalPosition;

public class VehicleMaintenanceTest extends MarsSimUnitTest {
    public void createGarage() {
        var s = buildSettlement("mock");
        var g = buildGarage(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);

        var f = (VehicleMaintenanceSpec) getConfig().getBuildingConfiguration().getFunctionSpec("Garage", FunctionType.VEHICLE_MAINTENANCE);

        assertEquals(f.getRoverParking().size(), g.getRoverCapacity(), "Rover parking");
        assertEquals(f.getRoverParking().size(), g.getAvailableRoverCapacity(), "Rover Capacity");

        assertEquals(f.getFlyerParking().size(), g.getFlyerCapacity(), "Flyer parking");
        assertEquals(f.getFlyerParking().size(), g.getAvailableFlyerCapacity(), "Flyer Capacity");

        assertEquals(f.getUtilityParking().size(), g.getUtilityVehicleCapacity(), "LUV parking");
        assertEquals(f.getUtilityParking().size(), g.getAvailableUtilityVehicleCapacity(), "LUV Capacity");
    }

    @Test
    public void testAddRover() {
        var s = buildSettlement("mock");
        var r = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);

        assertFalse(r.isInGarage(), "Rover initially in garage");

        // Rover is added to the garage by default
        var g = buildGarage(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);
        int cap = g.getRoverCapacity();

        assertFalse(g.containsRover(r), "Rover found");
        assertTrue(g.addRover(r), "Add Rover");
        assertTrue(r.isInGarage(), "Rover in garage");
        assertTrue(g.getRovers().contains(r), "Garage contains rover");
        assertFalse(g.addRover(r), "2nd Add Rover");
        assertEquals(cap-1, g.getAvailableRoverCapacity(), "Capacity reduced");

        // Remove it
        assertTrue(g.removeRover(r, false), "Removed rover");
        assertFalse(g.removeRover(r, false), "Double remove");
        assertFalse(r.isInGarage(), "No longer in garage");
        assertEquals(cap, g.getAvailableRoverCapacity(), "Capacity increaed");

    }
}

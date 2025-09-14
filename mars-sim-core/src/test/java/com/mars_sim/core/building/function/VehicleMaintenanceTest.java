package com.mars_sim.core.building.function;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.config.VehicleMaintenanceSpec;
import com.mars_sim.core.map.location.LocalPosition;

public class VehicleMaintenanceTest extends AbstractMarsSimUnitTest {
    public void createGarage() {
        var s = buildSettlement();
        var g = buildGarage(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);

        var f = (VehicleMaintenanceSpec) getConfig().getBuildingConfiguration().getFunctionSpec("Garage", FunctionType.VEHICLE_MAINTENANCE);

        assertEquals("Rover parking", f.getRoverParking().size(), g.getRoverCapacity());
        assertEquals("Rover Capacity", f.getRoverParking().size(), g.getAvailableRoverCapacity());

        assertEquals("Flyer parking", f.getFlyerParking().size(), g.getFlyerCapacity());
        assertEquals("Flyer Capacity", f.getFlyerParking().size(), g.getAvailableFlyerCapacity());

        assertEquals("LUV parking", f.getUtilityParking().size(), g.getUtilityVehicleCapacity());
        assertEquals("LUV Capacity", f.getUtilityParking().size(), g.getAvailableUtilityVehicleCapacity());
    }

    public void testAddRover() {
        var s = buildSettlement();
        var r = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);

        assertFalse("Rover initially in garage", r.isInGarage());

        // Rover is added to the garage by default
        var g = buildGarage(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);
        int cap = g.getRoverCapacity();

        assertFalse("Rover found", g.containsRover(r));
        assertTrue("Add Rover", g.addRover(r));
        assertTrue("Rover in garage", r.isInGarage());
        assertTrue("Garage contains rover", g.getRovers().contains(r));
        assertFalse("2nd Add Rover", g.addRover(r));
        assertEquals("Capacity reduced", cap-1, g.getAvailableRoverCapacity());

        // Remove it
        assertTrue("Removed rover", g.removeRover(r, false));
        assertFalse("Double remove", g.removeRover(r, false));
        assertFalse("No longer in garage", r.isInGarage());
        assertEquals("Capacity increaed", cap, g.getAvailableRoverCapacity());

    }
}

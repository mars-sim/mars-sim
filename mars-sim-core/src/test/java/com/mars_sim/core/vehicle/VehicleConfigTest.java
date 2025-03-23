package com.mars_sim.core.vehicle;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.utility.power.PowerSourceType;
import com.mars_sim.core.resource.ResourceUtil;

public class VehicleConfigTest extends AbstractMarsSimUnitTest {
    public void testGetVehicleSpec() {
        var vConfig = simConfig.getVehicleConfiguration();

        var found = vConfig.getVehicleSpec("Explorer Rover");
        
        assertNotNull("Exploer Rover founf", found);
        
        assertEquals("Type", VehicleType.EXPLORER_ROVER, found.getType());
        assertTrue("Description", found.getDescription().startsWith("The Explorer Rover "));

        assertEquals("Model", "A", found.getModelName());
        assertEquals("Width", 3.5D, found.getWidth());
        assertEquals("Length", 8D, found.getLength());
        assertEquals("Crew size", 4, found.getCrewSize());

        assertEquals("Cargo capacity", 2000D, found.getTotalCapacity());
        assertEquals("Methanol capacity", 100D, found.getCargoCapacity(ResourceUtil.methanolID));
        assertEquals("Oxygen capacity", 180D, found.getCargoCapacity(ResourceUtil.oxygenID));
        assertEquals("Water capacity", 200D, found.getCargoCapacity(ResourceUtil.waterID));

        assertEquals("Power source", PowerSourceType.FUEL_POWER, found.getPowerSourceType());
        
        assertEquals("Base speed", 40D, found.getBaseSpeed());
        assertEquals("Base power", 90D, found.getBasePower());

        assertEquals("Lab level", 1, found.getLabTechLevel());
        assertEquals("Lab Capacity", 2, found.getLabCapacity());
        assertEquals("Lab Speciality", 2, found.getLabTechSpecialties().size());

        assertEquals("Passenger Activity Spots", 3, found.getPassengerActivitySpots().size());
    }

    public void testGetVehicleSpecs() {
        var vConfig = simConfig.getVehicleConfiguration();

        var all = vConfig.getVehicleSpecs();
        assertTrue("Has vehicle specs", VehicleType.values().length <= all.size());
    }
}

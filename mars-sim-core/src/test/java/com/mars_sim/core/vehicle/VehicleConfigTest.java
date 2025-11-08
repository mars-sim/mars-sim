package com.mars_sim.core.vehicle;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.building.utility.power.PowerSourceType;
import com.mars_sim.core.resource.ResourceUtil;

public class VehicleConfigTest {

    private SimulationConfig config;

    @BeforeEach
    public void setUp() {
        config = SimulationConfig.loadConfig();
    }

    @Test
    void testGetVehicleSpec() {
        var vConfig = config.getVehicleConfiguration();

        var found = vConfig.getVehicleSpec("Explorer Rover");
        
        assertNotNull(found);
        
        assertEquals(VehicleType.EXPLORER_ROVER, found.getType());
        assertTrue(found.getDescription().startsWith("The Explorer Rover "), "Description");

        assertEquals(found.getModelName(), "A");
        assertEquals(3.5D, found.getWidth());
        assertEquals(8D, found.getLength());
        assertEquals(4, found.getCrewSize());

        assertEquals(2000D, found.getTotalCapacity());
        assertEquals(120D, found.getCargoCapacity(ResourceUtil.METHANOL_ID));
        assertEquals(180D, found.getCargoCapacity(ResourceUtil.OXYGEN_ID));
        assertEquals(200D, found.getCargoCapacity(ResourceUtil.WATER_ID));

        assertEquals(PowerSourceType.FUEL_POWER, found.getPowerSourceType());

        assertEquals(40D, found.getBaseSpeed());
        assertEquals(90D, found.getBasePower(), "Base power");

        assertEquals(1, found.getLabTechLevel(), "Lab level");
        assertEquals(2, found.getLabCapacity(), "Lab Capacity");
        assertEquals(2, found.getLabTechSpecialties().size(),"Lab Speciality");

        assertEquals(3, found.getPassengerActivitySpots().size(), "Passenger Activity Spots");
    }

    @Test
    void testGetVehicleSpecs() {
        var vConfig = config.getVehicleConfiguration();

        var all = vConfig.getVehicleSpecs();
        assertTrue(VehicleType.values().length <= all.size());
    }
}

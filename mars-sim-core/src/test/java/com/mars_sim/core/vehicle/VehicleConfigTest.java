package com.mars_sim.core.vehicle;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.building.utility.power.PowerSourceType;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;

class VehicleConfigTest {

    private SimulationConfig config;

    @BeforeEach
    void setUp() {
        config = SimulationConfig.loadConfig();
    }

    @Test
    void testExploreRoverSpec() {
        var vConfig = config.getVehicleConfiguration();

        var found = vConfig.getVehicleSpec("Explorer Rover");
        
        assertNotNull(found);
        
        assertEquals(VehicleType.EXPLORER_ROVER, found.getType());
        assertTrue(found.getDescription().startsWith("The Explorer Rover "), "Description");

        assertEquals("A", found.getModelName());
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

        // Check the associated Vehicle Spec has derived properties
        assertTrue(!found.getParts().isEmpty(), "Explorer rover should have parts after assembly");
        assertNotEquals(VehicleFactory.getUsualMass(found.getType()),
                found.getEmptyMass(), "Explorer rover mass should change after assembly");

        assertEquals(1, found.getLabTechLevel(), "Lab level");
        assertEquals(2, found.getLabCapacity(), "Lab Capacity");
        assertEquals(4, found.getLabTechSpecialties().size(),"Lab Speciality");

        assertEquals(3, found.getPassengerActivitySpots().size(), "Passenger Activity Spots");
    }

    @Test
    void testLUVSpec() {
        var vConfig = config.getVehicleConfiguration();

        var found = vConfig.getVehicleSpec("Light Utility Vehicle");
        
        assertNotNull(found);
        
        assertEquals(VehicleType.LUV, found.getType());

        var attachments = found.getAttachableParts();
        assertFalse(attachments.isEmpty(), "LUV should have attachable parts list");

        var drill = ItemResourceUtil.findItemResource(ItemResourceUtil.PNEUMATIC_DRILL_ID);
        var backhoe = ItemResourceUtil.findItemResource(ItemResourceUtil.BACKHOE_ID);
        assertTrue(attachments.contains(backhoe), "Should contain backhoe");
        assertTrue(attachments.contains(drill), "Should contain pneumatic drill");

        assertEquals(3, found.getAttachmentSlots(), "LUV should have attachable slots");


        // Check the associated Vehicle Spec has derived properties
        assertTrue(!found.getParts().isEmpty(), "LUV should have parts after assembly");
        assertNotEquals(VehicleFactory.getUsualMass(found.getType()),
                found.getEmptyMass(), "LUV mass should change after assembly");

    }

    @Test
    void testGetVehicleSpecs() {
        var vConfig = config.getVehicleConfiguration();

        var all = vConfig.getVehicleSpecs();
        assertTrue(VehicleType.values().length <= all.size());
    }
}
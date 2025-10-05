/**
 * Mars Simulation Project
 * ConstructionVehicleTypeTest.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */

package com.mars_sim.core.building.construction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * Unit test for ConstructionVehicleType.
 */
public class ConstructionVehicleTypeTest {

    private ConstructionVehicleType vehicleType;

    @BeforeEach
    void setUp() throws Exception {

        List<Integer> attachments = new ArrayList<Integer>(1);
        attachments.add(ItemResourceUtil.BACKHOE_ID);

        vehicleType = new ConstructionVehicleType(VehicleType.LUV, attachments);
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionVehicleType.getAttachmentParts()'
     */
    @Test
    public void testGetAttachmentParts() {
        List<Integer> parts = vehicleType.getAttachmentParts();
        assertNotNull(parts);
        assertEquals(1, parts.size());

        var part = parts.get(0);
        assertEquals(ItemResourceUtil.BACKHOE_ID, part);
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionVehicleType.getVehicleType()'
     */
    @Test
    public void testGetVehicleType() {
        assertEquals(VehicleType.LUV, vehicleType.getVehicleType());
    }
}
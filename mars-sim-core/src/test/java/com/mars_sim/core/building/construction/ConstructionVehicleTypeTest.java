/**
 * Mars Simulation Project
 * ConstructionVehicleTypeTest.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */

package com.mars_sim.core.building.construction;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.goods.GoodType;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.vehicle.VehicleType;

import junit.framework.TestCase;

/**
 * Unit test for ConstructionVehicleType.
 */
public class ConstructionVehicleTypeTest extends TestCase {

    private ConstructionVehicleType vehicleType;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        List<Integer> attachments = new ArrayList<Integer>(1);
        GoodType type = GoodType.CONSTRUCTION;
        Part p = ItemResourceUtil.createItemResource("attachment part", 1, "test resource description", type, 1D, 1);
        attachments.add(p.getID());

        vehicleType = new ConstructionVehicleType(VehicleType.LUV, attachments);
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionVehicleType.getAttachmentParts()'
     */
    public void testGetAttachmentParts() {
        List<Integer> parts = vehicleType.getAttachmentParts();
        assertNotNull(parts);
        assertEquals(1, parts.size());

        Part part = ItemResourceUtil.findItemResource(parts.get(0));
        assertNotNull(part);
        assertEquals("attachment part", part.getName());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionVehicleType.getVehicleType()'
     */
    public void testGetVehicleType() {
        assertEquals(VehicleType.LUV, vehicleType.getVehicleType());
    }
}
/**
 * Mars Simulation Project
 * ConstructionVehicleTypeTest.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.construction;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;

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
        
        Part p = ItemResourceUtil.createItemResource("attachment part", 1, "test resource description", 1D, 1);  		    
        attachments.add(p.getID());
        
        vehicleType = new ConstructionVehicleType("Light Utility Vehicle", 
                LightUtilityVehicle.class, attachments);
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.construction.
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
     * Test method for 'org.mars_sim.msp.simulation.structure.construction.
     * ConstructionVehicleType.getVehicleClass()'
     */
    public void testGetVehicleClass() {
        assertEquals(LightUtilityVehicle.class, vehicleType.getVehicleClass());
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure.construction.
     * ConstructionVehicleType.getVehicleType()'
     */
    public void testGetVehicleType() {
        assertEquals("Light Utility Vehicle", vehicleType.getVehicleType());
    }
}
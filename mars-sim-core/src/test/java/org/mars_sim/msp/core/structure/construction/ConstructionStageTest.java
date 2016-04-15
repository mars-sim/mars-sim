/**
 * Mars Simulation Project
 * ConstructionStageTest.java
 * @version 3.07 2014-08-22
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.construction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Phase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;

import junit.framework.TestCase;

/**
 * Test case for the ConstructionStage class.
 */
public class ConstructionStageTest extends TestCase {

    // Data members
    private ConstructionStageInfo foundationInfo;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Map<Part, Integer> parts = new HashMap<Part, Integer>(1);
        parts.put(new Part("test part","test resource description", 1D), 1);

        Map<AmountResource, Double> resources = new HashMap<AmountResource, Double>(1);
        resources.put(new AmountResource("test resource","test resource description", Phase.SOLID, false, false), 1D);

        List<ConstructionVehicleType> vehicles =
            new ArrayList<ConstructionVehicleType>(1);
        List<Part> attachments = new ArrayList<Part>(1);
        attachments.add(new Part("attachment part","test resource description", 1D));
        vehicles.add(new ConstructionVehicleType("Light Utility Vehicle", LightUtilityVehicle.class,
                attachments));

        foundationInfo = new ConstructionStageInfo("test foundation info",
                ConstructionStageInfo.FOUNDATION, 10D, 10D, false, 0, false, false, 10000D, 0, null,
                parts, resources, vehicles);
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure
     * .construction.ConstructionStage.ConstructionStage(ConstructionStageInfo)'
     */
    public void testConstructionStage() {
        ConstructionSite site = new ConstructionSite(new Settlement());
        ConstructionStage stage = new ConstructionStage(foundationInfo, site);
        assertNotNull(stage);
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure
     * .construction.ConstructionStage.getInfo()'
     */
    public void testGetInfo() {
        ConstructionSite site = new ConstructionSite(new Settlement());
        ConstructionStage stage = new ConstructionStage(foundationInfo, site);
        assertEquals(foundationInfo, stage.getInfo());
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure
     * .construction.ConstructionStage.getCompletedWorkTime()'
     */
    public void testGetCompletedWorkTime() {
        ConstructionSite site = new ConstructionSite(new Settlement());
        ConstructionStage stage = new ConstructionStage(foundationInfo, site);
        assertEquals(0D, stage.getCompletedWorkTime());
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure
     * .construction.ConstructionStage.addWorkTime(double)'
     */
    public void testAddWorkTime() {
        ConstructionSite site = new ConstructionSite(new Settlement());
        ConstructionStage stage = new ConstructionStage(foundationInfo, site);
        stage.addWorkTime(5000D);
        assertEquals(5000D, stage.getCompletedWorkTime());
        stage.addWorkTime(5000D);
        assertEquals(10000D, stage.getCompletedWorkTime());
        stage.addWorkTime(5000D);
        assertEquals(10000D, stage.getCompletedWorkTime());
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure
     * .construction.ConstructionStage.isComplete()'
     */
    public void testIsComplete() {
        ConstructionSite site = new ConstructionSite(new Settlement());
        ConstructionStage stage = new ConstructionStage(foundationInfo, site);
        stage.addWorkTime(5000D);
        assertFalse(stage.isComplete());
        stage.addWorkTime(10000D);
        assertTrue(stage.isComplete());
    }
}
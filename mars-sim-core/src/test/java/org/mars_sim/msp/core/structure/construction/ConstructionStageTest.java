/**
 * Mars Simulation Project
 * ConstructionStageTest.java
 * @version 3.1.0 2017-09-11
 * @author Scott Davis
 */

//package org.mars_sim.msp.core.structure.construction;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.mars_sim.msp.core.resource.AmountResource;
//import org.mars_sim.msp.core.resource.Part;
//import org.mars_sim.msp.core.resource.PhaseType;
//import org.mars_sim.msp.core.structure.Settlement;
//import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
//
//import junit.framework.TestCase;
//
///**
// * Test case for the ConstructionStage class.
// */
//public class ConstructionStageTest extends TestCase {
//
//    // Data members
//    private ConstructionStageInfo foundationInfo;
//
//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//
//        Map<Integer, Integer> parts = new HashMap<Integer, Integer>(1);
//        parts.put(new Part("test part", 1, "test resource description", 1D, 1).getID(), 1);
//
//        Map<Integer, Double> resources = new HashMap<Integer, Double>(1);
//        resources.put(new AmountResource(1, "test resource", "test type","test resource description", PhaseType.SOLID, false, false).getID(), 1D);
//
//        List<ConstructionVehicleType> vehicles =
//            new ArrayList<ConstructionVehicleType>(1);
//        List<Integer> attachments = new ArrayList<Integer>(1);
//        attachments.add(new Part("attachment part", 2, "test resource description", 1D, 1).getID());
//        vehicles.add(new ConstructionVehicleType("Light Utility Vehicle", LightUtilityVehicle.class,
//                attachments));
//
//        foundationInfo = new ConstructionStageInfo("test foundation info",
//                ConstructionStageInfo.FOUNDATION, 10D, 10D, false, 0, false, false, 10000D, 0, null,
//                parts, resources, vehicles);
//    }
//
//    /*
//     * Test method for 'org.mars_sim.msp.simulation.structure
//     * .construction.ConstructionStage.ConstructionStage(ConstructionStageInfo)'
//     */
//    public void testConstructionStage() {
//        ConstructionSite site = new ConstructionSite(Settlement.createConstructionStage());
//        ConstructionStage stage = new ConstructionStage(foundationInfo, site);
//        assertNotNull(stage);
//    }
//
//    /*
//     * Test method for 'org.mars_sim.msp.simulation.structure
//     * .construction.ConstructionStage.getInfo()'
//     */
//    public void testGetInfo() {
//        ConstructionSite site = new ConstructionSite(Settlement.createConstructionStage());
//        ConstructionStage stage = new ConstructionStage(foundationInfo, site);
//        assertEquals(foundationInfo, stage.getInfo());
//    }
//
//    /*
//     * Test method for 'org.mars_sim.msp.simulation.structure
//     * .construction.ConstructionStage.getCompletedWorkTime()'
//     */
//    public void testGetCompletedWorkTime() {
//        ConstructionSite site = new ConstructionSite(Settlement.createConstructionStage());
//        ConstructionStage stage = new ConstructionStage(foundationInfo, site);
//        assertEquals(0D, stage.getCompletedWorkTime());
//    }
//
//    /*
//     * Test method for 'org.mars_sim.msp.simulation.structure
//     * .construction.ConstructionStage.addWorkTime(double)'
//     */
//    public void testAddWorkTime() {
//        ConstructionSite site = new ConstructionSite(Settlement.createConstructionStage());
//        ConstructionStage stage = new ConstructionStage(foundationInfo, site);
//        stage.addWorkTime(5000D);
//        assertEquals(5000D, stage.getCompletedWorkTime());
//        stage.addWorkTime(5000D);
//        assertEquals(10000D, stage.getCompletedWorkTime());
//        stage.addWorkTime(5000D);
//        assertEquals(10000D, stage.getCompletedWorkTime());
//    }
//
//    /*
//     * Test method for 'org.mars_sim.msp.simulation.structure
//     * .construction.ConstructionStage.isComplete()'
//     */
//    public void testIsComplete() {
//        ConstructionSite site = new ConstructionSite(Settlement.createConstructionStage());
//        ConstructionStage stage = new ConstructionStage(foundationInfo, site);
//        stage.addWorkTime(5000D);
//        assertFalse(stage.isComplete());
//        stage.addWorkTime(10000D);
//        assertTrue(stage.isComplete());
//    }
//}
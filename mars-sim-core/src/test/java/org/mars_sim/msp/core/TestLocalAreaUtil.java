package org.mars_sim.msp.core;

import java.awt.geom.Point2D;
import java.util.Iterator;

import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.MockBuilding;

import junit.framework.TestCase;

/**
 * Unit test suite for the LocalAreaUtil class.
 */
public class TestLocalAreaUtil extends TestCase {
    
    // Comparison to indicate a small but non-zero amount.
    private static final double SMALL_AMOUNT_COMPARISON = .0000001D;
    
//    /**
//     * Test the checkLinePathCollisionAtSettlement method.
//     */
//    public void testCheckLinePathCollisionAtSettlement() {
//        
//        // Create new simulation instance.
//        SimulationConfig.loadConfig();
//        Simulation.testRun();
//        
//        // Clear out existing settlements in simulation.
//        UnitManager unitManager = Simulation.instance().getUnitManager();
//        Iterator<Settlement> i = unitManager.getSettlements().iterator();
//        while (i.hasNext()) {
//            unitManager.removeUnit(i.next());
//        }
//        
//        // Create test settlement.
//        Settlement settlement = new MockSettlement();
//        
//        BuildingManager buildingManager = settlement.getBuildingManager();
//        
//		// Removes all mock buildings and building functions in the settlement.
//		buildingManager.removeAllMockBuildings();
//
//		unitManager.addUnit(settlement);
//        Coordinates loc = settlement.getCoordinates();   
//        
//        // Create test building.
//        MockBuilding building = new MockBuilding(buildingManager);
//        building.setWidth(10D);
//        building.setLength(10D);
//        building.setXLocation(0D);
//        building.setYLocation(0D);
//        building.setFacing(0D);
////        settlement.getBuildingManager().addBuilding(building, true);
////        buildingManager.addMockBuilding(building);
////		settlement.getBuildingConnectorManager().createBuildingConnections(building);
//
//		// Clear obstacle cache.
//		LocalAreaUtil.clearObstacleCache();
//	
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, 0D, 10D, 0D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(10D, 0D, 0D, 0D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, 5D, 10D, 5D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, -5D, 10D, -5D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, -5D, 10D, 5D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, 5D, 10D, -5D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(5D, 0D, 5D, 10D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(-5D, 10D, -5D, 0D), loc, true));
////        assertTrue(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(-6D, 10D, -6D, 0D), loc, true));
////        assertTrue(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(6D, 10D, 6D, 0D), loc, true));
////        assertTrue(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, 6D, 10D, 6D), loc, true));
////        assertTrue(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, -6D, 10D, -6D), loc, true));
//        
//        building.setFacing(45D);
//        
//        // Clear obstacle cache.
//        LocalAreaUtil.clearObstacleCache();
//        
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, 0D, 10D, 0D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(10D, 0D, 0D, 0D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, 5D, 10D, 5D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, -5D, 10D, -5D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, -5D, 10D, 5D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, 5D, 10D, -5D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(5D, 0D, 5D, 10D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(-5D, 10D, -5D, 0D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(-6D, 10D, -6D, 0D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(6D, 10D, 6D, 0D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, 6D, 10D, 6D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, -6D, 10D, -6D), loc, true));
//        
//        building.setFacing(0D);
//        building.setXLocation(10D);
//        
//        // Clear obstacle cache.
//        LocalAreaUtil.clearObstacleCache();
//        
////        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(10D, 0D, 20D, 0D), loc, true));
////        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(20D, 0D, 10D, 0D), loc, true));
////        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(10D, 5D, 20D, 5D), loc, true));
////        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(10D, -5D, 20D, -5D), loc, true));
////        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(10D, -5D, 20D, 5D), loc, true));
////        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(10D, 5D, 20D, -5D), loc, true));
////        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(15D, 0D, 15D, 10D), loc, true));
////        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(5D, 10D, 5D, 0D), loc, true));
////        assertTrue(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(4D, 10D, 4D, 0D), loc, true));
////        assertTrue(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(16D, 10D, 16D, 0D), loc, true));
////        assertTrue(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(10D, 6D, 20D, 6D), loc, true));
////        assertTrue(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(10D, -6D, 20D, -6D), loc, true));
//        
//        building.setXLocation(0D);
//        building.setYLocation(10D);
//        
//        // Clear obstacle cache.
//        LocalAreaUtil.clearObstacleCache();
//        
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, 10D, 20D, 0D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(10D, 10D, 0D, 10D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, 15D, 10D, 15D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, 5D, 10D, 5D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, 5D, 10D, 15D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, 15D, 10D, 5D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(5D, 10D, 5D, 20D), loc, true));
//        assertFalse(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(-5D, 20D, -5D, 10D), loc, true));
//        assertTrue(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(-6D, 20D, -6D, 10D), loc, true));
//        assertTrue(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(6D, 20D, 6D, 10D), loc, true));
//        assertTrue(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, 16D, 10D, 16D), loc, true));
//        assertTrue(LocalAreaUtil.checkLinePathCollision(new Line2D.Double(0D, 4D, 10D, 4D), loc, true));
//        
//        // Clear obstacle cache.
//        LocalAreaUtil.clearObstacleCache();
//    }
    
//    /**
//     * Test the getObjectRelativeLocation.
//     */
//    public void testGetObjectRelativeLocation() {
//        // Create new simulation instance.
//        SimulationConfig.instance().loadConfig();
//        Simulation.instance().testRun();
//        
//        // Clear out existing settlements in simulation.
//        UnitManager unitManager = Simulation.instance().getUnitManager();
//        Iterator<Settlement> i = unitManager.getSettlements().iterator();
//        while (i.hasNext()) {
//            unitManager.removeUnit(i.next());
//        }
//        
//        
//        Settlement settlement = new MockSettlement();
//        BuildingManager buildingManager = settlement.getBuildingManager();
//        
//        MockBuilding building = new MockBuilding(buildingManager);
//        building.setWidth(10D);
//        building.setLength(10D);
//        building.setXLocation(0D);
//        building.setYLocation(0D);
//        building.setFacing(0D);
//        
//        Point2D point0 = new Point2D.Double(12D, -7D);
//        
//        Point2D point1 = LocalAreaUtil.getLocalRelativeLocation(point0.getX(), point0.getY(), building);
//        
//        Point2D point2 = LocalAreaUtil.getObjectRelativeLocation(point1.getX(), point1.getY(), building);
//        
//        assertTrue(Math.abs(point0.getX() - point2.getX()) < SMALL_AMOUNT_COMPARISON);
//        assertTrue(Math.abs(point0.getY() - point2.getY()) < SMALL_AMOUNT_COMPARISON);
//        
//        building.setXLocation(15D);
//        building.setYLocation(-8D);
//        
//        Point2D point3 = new Point2D.Double(5D, 12D);
//        
//        Point2D point4 = LocalAreaUtil.getLocalRelativeLocation(point3.getX(), point3.getY(), building);
//        
//        Point2D point5 = LocalAreaUtil.getObjectRelativeLocation(point4.getX(), point4.getY(), building);
//        
//        assertTrue(Math.abs(point3.getX() - point5.getX()) < SMALL_AMOUNT_COMPARISON);
//        assertTrue(Math.abs(point3.getY() - point5.getY()) < SMALL_AMOUNT_COMPARISON);
//        
//        building.setFacing(135D);
//        
//        Point2D point6 = new Point2D.Double(-3D, -7D);
//        
//        Point2D point7 = LocalAreaUtil.getLocalRelativeLocation(point6.getX(), point6.getY(), building);
//        
//        Point2D point8 = LocalAreaUtil.getObjectRelativeLocation(point7.getX(), point7.getY(), building);
//        
//        assertTrue(Math.abs(point6.getX() - point8.getX()) < SMALL_AMOUNT_COMPARISON);
//        assertTrue(Math.abs(point6.getY() - point8.getY()) < SMALL_AMOUNT_COMPARISON);
//        
//        building.setXLocation(-32D);
//        building.setYLocation(12.7D);
//        building.setFacing(290.2D);
//        
//        Point2D point9 = new Point2D.Double(-11.2D, 33.56D);
//        
//        Point2D point10 = LocalAreaUtil.getLocalRelativeLocation(point9.getX(), point9.getY(), building);
//        
//        Point2D point11 = LocalAreaUtil.getObjectRelativeLocation(point10.getX(), point10.getY(), building);
//        
//        assertTrue(Math.abs(point9.getX() - point11.getX()) < SMALL_AMOUNT_COMPARISON);
//        assertTrue(Math.abs(point9.getY() - point11.getY()) < SMALL_AMOUNT_COMPARISON);
//    }
    
    /**
     * Test the locationWithinLocalBoundedObject method.
     */
    public void testLocationWithinLocalBoundedObject() {
        // Create new simulation instance.
        SimulationConfig.instance().loadConfig();
        Simulation.instance().testRun();
        
        // Clear out existing settlements in simulation.
        UnitManager unitManager = Simulation.instance().getUnitManager();
        Iterator<Settlement> i = unitManager.getSettlements().iterator();
        while (i.hasNext()) {
            unitManager.removeUnit(i.next());
        }
        
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        
        MockBuilding building = new MockBuilding(buildingManager);
        building.setWidth(10D);
        building.setLength(10D);
        building.setXLocation(0D);
        building.setYLocation(0D);
        building.setFacing(0D);
        
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(0D, 0D, building));
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(4.99D, 4.99D, building));
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(-4.99D, 4.99D, building));
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(4.99D, -4.99D, building));
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(-4.99D, -4.99D, building));
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(4.99D, 0D, building));
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(-4.99D, 0D, building));
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(0D, 4.99D, building));
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(0D, -4.99D, building));
        
        assertFalse(LocalAreaUtil.checkLocationWithinLocalBoundedObject(5.01D, 5.01D, building));
        assertFalse(LocalAreaUtil.checkLocationWithinLocalBoundedObject(-5.01D, 5.01D, building));
        assertFalse(LocalAreaUtil.checkLocationWithinLocalBoundedObject(5.01D, -5.01D, building));
        assertFalse(LocalAreaUtil.checkLocationWithinLocalBoundedObject(-5.01D, -5.01D, building));
        assertFalse(LocalAreaUtil.checkLocationWithinLocalBoundedObject(5.01D, 0D, building));
        assertFalse(LocalAreaUtil.checkLocationWithinLocalBoundedObject(-5.01D, 0D, building));
        assertFalse(LocalAreaUtil.checkLocationWithinLocalBoundedObject(0D, 5.01D, building));
        assertFalse(LocalAreaUtil.checkLocationWithinLocalBoundedObject(0D, -5.01D, building));
        
        // Rotate building 45 degrees.
        building.setFacing(45D);
        
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(0D, 0D, building));
        assertFalse(LocalAreaUtil.checkLocationWithinLocalBoundedObject(4.99D, 4.99D, building));
        assertFalse(LocalAreaUtil.checkLocationWithinLocalBoundedObject(-4.99D, 4.99D, building));
        assertFalse(LocalAreaUtil.checkLocationWithinLocalBoundedObject(4.99D, -4.99D, building));
        assertFalse(LocalAreaUtil.checkLocationWithinLocalBoundedObject(-4.99D, -4.99D, building));
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(4.99D, 0D, building));
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(-4.99D, 0D, building));
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(0D, 4.99D, building));
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(0D, -4.99D, building));
        
        assertFalse(LocalAreaUtil.checkLocationWithinLocalBoundedObject(5.01D, 5.01D, building));
        assertFalse(LocalAreaUtil.checkLocationWithinLocalBoundedObject(-5.01D, 5.01D, building));
        assertFalse(LocalAreaUtil.checkLocationWithinLocalBoundedObject(5.01D, -5.01D, building));
        assertFalse(LocalAreaUtil.checkLocationWithinLocalBoundedObject(-5.01D, -5.01D, building));
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(5.01D, 0D, building));
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(-5.01D, 0D, building));
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(0D, 5.01D, building));
        assertTrue(LocalAreaUtil.checkLocationWithinLocalBoundedObject(0D, -5.01D, building));
    }
}
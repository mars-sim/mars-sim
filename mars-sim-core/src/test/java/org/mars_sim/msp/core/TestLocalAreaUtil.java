package org.mars_sim.msp.core;

import java.util.Iterator;

import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.MockBuilding;

import junit.framework.TestCase;

/**
 * Unit test suite for the LocalAreaUtil class.
 */
public class TestLocalAreaUtil extends TestCase {
    
    /**
     * Test the checkLinePathCollisionAtSettlement method.
     */
    public void testCheckLinePathCollisionAtSettlement() {
        
        // Create new simulation instance.
        SimulationConfig.loadConfig();
        Simulation.createNewSimulation();
        
        // Clear out existing settlements in simulation.
        UnitManager unitManager = Simulation.instance().getUnitManager();
        Iterator<Settlement> i = unitManager.getSettlements().iterator();
        while (i.hasNext()) {
            unitManager.removeUnit(i.next());
        }
        
        // Create test settlement.
        Settlement settlement = new MockSettlement();
        unitManager.addUnit(settlement);
        Coordinates loc = settlement.getCoordinates();
        
        // Create test building.
        MockBuilding building = new MockBuilding(settlement.getBuildingManager());
        building.setWidth(10D);
        building.setLength(10D);
        building.setXLocation(0D);
        building.setYLocation(0D);
        building.setFacing(0D);
        settlement.getBuildingManager().addBuilding(building);
        
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, 0D, 10D, 0D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(10D, 0D, 0D, 0D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, 5D, 10D, 5D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, -5D, 10D, -5D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, -5D, 10D, 5D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, 5D, 10D, -5D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(5D, 0D, 5D, 10D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(-5D, 10D, -5D, 0D, loc));
        assertTrue(LocalAreaUtil.checkLinePathCollision(-6D, 10D, -6D, 0D, loc));
        assertTrue(LocalAreaUtil.checkLinePathCollision(6D, 10D, 6D, 0D, loc));
        assertTrue(LocalAreaUtil.checkLinePathCollision(0D, 6D, 10D, 6D, loc));
        assertTrue(LocalAreaUtil.checkLinePathCollision(0D, -6D, 10D, -6D, loc));
        
        building.setFacing(45D);
        
        // Clear obstacle cache.
        LocalAreaUtil.clearObstacleCache();
        
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, 0D, 10D, 0D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(10D, 0D, 0D, 0D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, 5D, 10D, 5D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, -5D, 10D, -5D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, -5D, 10D, 5D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, 5D, 10D, -5D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(5D, 0D, 5D, 10D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(-5D, 10D, -5D, 0D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(-6D, 10D, -6D, 0D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(6D, 10D, 6D, 0D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, 6D, 10D, 6D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, -6D, 10D, -6D, loc));
        
        building.setFacing(0D);
        building.setXLocation(10D);
        
        // Clear obstacle cache.
        LocalAreaUtil.clearObstacleCache();
        
        assertFalse(LocalAreaUtil.checkLinePathCollision(10D, 0D, 20D, 0D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(20D, 0D, 10D, 0D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(10D, 5D, 20D, 5D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(10D, -5D, 20D, -5D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(10D, -5D, 20D, 5D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(10D, 5D, 20D, -5D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(15D, 0D, 15D, 10D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(5D, 10D, 5D, 0D, loc));
        assertTrue(LocalAreaUtil.checkLinePathCollision(4D, 10D, 4D, 0D, loc));
        assertTrue(LocalAreaUtil.checkLinePathCollision(16D, 10D, 16D, 0D, loc));
        assertTrue(LocalAreaUtil.checkLinePathCollision(10D, 6D, 20D, 6D, loc));
        assertTrue(LocalAreaUtil.checkLinePathCollision(10D, -6D, 20D, -6D, loc));
        
        building.setXLocation(0D);
        building.setYLocation(10D);
        
        // Clear obstacle cache.
        LocalAreaUtil.clearObstacleCache();
        
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, 10D, 20D, 0D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(10D, 10D, 0D, 10D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, 15D, 10D, 15D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, 5D, 10D, 5D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, 5D, 10D, 15D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(0D, 15D, 10D, 5D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(5D, 10D, 5D, 20D, loc));
        assertFalse(LocalAreaUtil.checkLinePathCollision(-5D, 20D, -5D, 10D, loc));
        assertTrue(LocalAreaUtil.checkLinePathCollision(-6D, 20D, -6D, 10D, loc));
        assertTrue(LocalAreaUtil.checkLinePathCollision(6D, 20D, 6D, 10D, loc));
        assertTrue(LocalAreaUtil.checkLinePathCollision(0D, 16D, 10D, 16D, loc));
        assertTrue(LocalAreaUtil.checkLinePathCollision(0D, 4D, 10D, 4D, loc));
        
        // Clear obstacle cache.
        LocalAreaUtil.clearObstacleCache();
    }
}
package com.mars_sim.core;

import java.util.Iterator;

import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.MockBuilding;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;

import junit.framework.TestCase;

/**
 * Unit test suite for the LocalAreaUtil class.
 */
public class TestLocalAreaUtil extends TestCase {
    
    // Comparison to indicate a small but non-zero amount.
	private UnitManager unitManager;
    
	@Override
	public void setUp() {
	    // Create new simulation instance.
        SimulationConfig simConfig = SimulationConfig.loadConfig();
        
        Simulation sim = Simulation.instance();
        sim.testRun();
        
        // Clear out existing settlements in simulation.
        unitManager = sim.getUnitManager();
        Iterator<Settlement> i = unitManager.getSettlements().iterator();
        while (i.hasNext()) {
            unitManager.removeUnit(i.next());
        }
		
        Function.initializeInstances(simConfig.getBuildingConfiguration(), sim.getMasterClock(),
        							 simConfig.getPersonConfig(), simConfig.getCropConfiguration(), sim.getSurfaceFeatures(),
        							 sim.getWeather(), unitManager);
	}
	
    /**
     * Test the locationWithinLocalBoundedObject method.
     */
    public void testLocationWithinLocalBoundedObject() {
        
        Settlement settlement = new MockSettlement();
 
        var bounds = new BoundedObject(0D, 0D, 10, 10, 0);
        MockBuilding mb0 = new MockBuilding(settlement, "Mock B0", "1", bounds, "Mock", BuildingCategory.COMMAND, false);
        
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(0D, 0D), mb0));
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(4.99D, 4.99D), mb0));
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(-4.99D, 4.99D), mb0));
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(4.99D, -4.99D), mb0));
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(-4.99D, -4.99D), mb0));
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(4.99D, 0D), mb0));
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(-4.99D, 0D), mb0));
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(0D, 4.99D), mb0));
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(0D, -4.99D), mb0));
        
        assertFalse(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(5.01D, 5.01D), mb0));
        assertFalse(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(-5.01D, 5.01D), mb0));
        assertFalse(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(5.01D, -5.01D), mb0));
        assertFalse(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(-5.01D, -5.01D), mb0));
        assertFalse(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(5.01D, 0D), mb0));
        assertFalse(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(-5.01D, 0D), mb0));
        assertFalse(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(0D, 5.01D), mb0));
        assertFalse(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(0D, -5.01D), mb0));
    }

    public void testRotate45Degrees() {
            
        Settlement settlement = new MockSettlement();
 
        // Rotate building 45 degrees.
        var bounds = new BoundedObject(0D, 0D, 10, 10, 45D);
        var mb0 = new MockBuilding(settlement, "Mock B0", "1", bounds, "Mock", BuildingCategory.COMMAND, false);
                
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(0D, 0D), mb0));
        assertFalse(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(4.99D, 4.99D), mb0));
        assertFalse(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(-4.99D, 4.99D), mb0));
        assertFalse(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(4.99D, -4.99D), mb0));
        assertFalse(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(-4.99D, -4.99D), mb0));
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(4.99D, 0D), mb0));
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(-4.99D, 0D), mb0));
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(0D, 4.99D), mb0));
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(0D, -4.99D), mb0));
        
        assertFalse(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(5.01D, 5.01D), mb0));
        assertFalse(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(-5.01D, 5.01D), mb0));
        assertFalse(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(5.01D, -5.01D), mb0));
        assertFalse(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(-5.01D, -5.01D), mb0));
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(5.01D, 0D), mb0));
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(-5.01D, 0D), mb0));
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(0D, 5.01D), mb0));
        assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(new LocalPosition(0D, -5.01D), mb0));
    }
}
package org.mars_sim.msp.core.structure.building.connection;

import org.mars_sim.msp.core.structure.building.MockBuilding;

import junit.framework.TestCase;

public class HatchTest extends TestCase {

    public void testConstructor() {
        
        MockBuilding building1 = new MockBuilding();
        MockBuilding building2 = new MockBuilding();
        BuildingConnector connector = new BuildingConnector(building1, 
                0D, 0D, 0D, building2, 0D, 0D, 0D);
        
        Hatch hatch = new Hatch(building1, connector, 5D, 12D, 90D);
        assertNotNull(hatch);
        
        assertEquals(building1, hatch.getBuilding());
        assertEquals(connector, hatch.getBuildingConnector());
        assertEquals(5D, hatch.getXLocation());
        assertEquals(12D, hatch.getYLocation());
        assertEquals(90D, hatch.getFacing());
        assertEquals(Hatch.WIDTH, hatch.getWidth());
        assertEquals(Hatch.LENGTH, hatch.getLength());
    }
    
    public void testSetLocation() {
        
        MockBuilding building1 = new MockBuilding();
        MockBuilding building2 = new MockBuilding();
        BuildingConnector connector = new BuildingConnector(building1, 
                0D, 0D, 0D, building2, 0D, 0D, 0D);
        
        Hatch hatch = new Hatch(building1, connector, 5D, 12D, 90D);
        assertNotNull(hatch);
        
        assertEquals(5D, hatch.getXLocation());
        assertEquals(12D, hatch.getYLocation());
        
        hatch.setXLocation(7D);
        assertEquals(7D, hatch.getXLocation());
        
        hatch.setYLocation(10D);
        assertEquals(10D, hatch.getYLocation());
    }
    
    public void testEquals() {
        
        MockBuilding building1 = new MockBuilding();
        MockBuilding building2 = new MockBuilding();
        BuildingConnector connector = new BuildingConnector(building1, 
                0D, 0D, 0D, building2, 0D, 0D, 0D);
        
        Hatch hatch1 = new Hatch(building1, connector, 5D, 12D, 90D);
        assertNotNull(hatch1);
        
        Hatch hatch2 = new Hatch(building1, connector, 5D, 12D, 90D);
        assertNotNull(hatch2);
        
        assertEquals(hatch1, hatch2);
    }
}
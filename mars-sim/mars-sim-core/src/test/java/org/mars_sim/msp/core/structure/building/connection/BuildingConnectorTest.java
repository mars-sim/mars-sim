package org.mars_sim.msp.core.structure.building.connection;

import org.mars_sim.msp.core.structure.building.MockBuilding;

import junit.framework.TestCase;

public class BuildingConnectorTest extends TestCase {
    
    public void testNonSplitBuildingConnector() {
        
        MockBuilding building1 = new MockBuilding();
        building1.setWidth(10D);
        building1.setLength(10D);
        building1.setXLocation(0D);
        building1.setYLocation(0D);
        building1.setFacing(0D);
        
        MockBuilding building2 = new MockBuilding();
        building2.setWidth(10D);
        building2.setLength(10D);
        building2.setXLocation(0D);
        building2.setYLocation(10D);
        building2.setFacing(0D);
        
        BuildingConnector connector = new BuildingConnector(building1, 
                0D, 5D, 0D, building2, 0D, 5D, 180D);
        assertNotNull(connector);
        
        assertEquals(building1, connector.getBuilding1());
        assertEquals(building2, connector.getBuilding2());
        
        Hatch hatch1 = connector.getHatch1();
        assertNotNull(hatch1);
        assertEquals(building1, hatch1.getBuilding());
        assertEquals(connector, hatch1.getBuildingConnector());
        assertEquals(0D, hatch1.getXLocation());
        assertEquals(5D, hatch1.getYLocation());
        assertEquals(0D, hatch1.getFacing());
        
        Hatch hatch2 = connector.getHatch2();
        assertNotNull(hatch2);
        assertEquals(building2, hatch2.getBuilding());
        assertEquals(connector, hatch2.getBuildingConnector());
        assertEquals(0D, hatch2.getXLocation());
        assertEquals(5D, hatch2.getYLocation());
        assertEquals(180D, hatch2.getFacing());
        
        assertFalse(connector.isSplitConnection());
        assertEquals(0D, connector.getXLocation());
        assertEquals(5D, connector.getYLocation());
        
        BuildingConnector connector2 = new BuildingConnector(building2, 
                0D, 5D, 180D, building1, 0D, 5D, 0D);
        assertNotNull(connector2);
        
        assertEquals(connector, connector2);
    }
    
    public void testSplitBuildingConnector() {
        
        MockBuilding building1 = new MockBuilding();
        building1.setWidth(10D);
        building1.setLength(10D);
        building1.setXLocation(0D);
        building1.setYLocation(0D);
        building1.setFacing(0D);
        
        MockBuilding building2 = new MockBuilding();
        building2.setWidth(10D);
        building2.setLength(10D);
        building2.setXLocation(0D);
        building2.setYLocation(12D);
        building2.setFacing(0D);
        
        BuildingConnector connector = new BuildingConnector(building1, 
                0D, 5D, 0D, building2, 0D, 7D, 180D);
        assertNotNull(connector);
        
        assertEquals(building1, connector.getBuilding1());
        assertEquals(building2, connector.getBuilding2());
        
        Hatch hatch1 = connector.getHatch1();
        assertNotNull(hatch1);
        assertEquals(building1, hatch1.getBuilding());
        assertEquals(connector, hatch1.getBuildingConnector());
        assertEquals(0D, hatch1.getXLocation());
        assertEquals(5D, hatch1.getYLocation());
        assertEquals(0D, hatch1.getFacing());
        
        Hatch hatch2 = connector.getHatch2();
        assertNotNull(hatch2);
        assertEquals(building2, hatch2.getBuilding());
        assertEquals(connector, hatch2.getBuildingConnector());
        assertEquals(0D, hatch2.getXLocation());
        assertEquals(7D, hatch2.getYLocation());
        assertEquals(180D, hatch2.getFacing());
        
        assertTrue(connector.isSplitConnection());
        assertEquals(0D, connector.getXLocation());
        assertEquals(6D, connector.getYLocation());
        
        BuildingConnector connector2 = new BuildingConnector(building2, 
                0D, 7D, 180D, building1, 0D, 5D, 0D);
        assertNotNull(connector2);
        
        assertEquals(connector, connector2);
    }
}
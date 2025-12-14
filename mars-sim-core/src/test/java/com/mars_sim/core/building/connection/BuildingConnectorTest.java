package com.mars_sim.core.building.connection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.MockBuilding;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.MockSettlement;

public class BuildingConnectorTest extends MarsSimUnitTest{
    
    private static final LocalPosition BUILDING3_POSITION = new LocalPosition(0D, 7D);
    private static final LocalPosition BUILDING2_POSITION = new LocalPosition(0D, 5D);
    private static final LocalPosition BUILDING_POSITION = BUILDING2_POSITION;

    @Test
    public void testNonSplitBuildingConnector() {
        
        MockSettlement settlement = new MockSettlement();
        MockBuilding building1 = new MockBuilding(settlement, "1", new BoundedObject(0D, 0D, 10D, 10D, 0D));
        MockBuilding building2 = new MockBuilding(settlement, "2", new BoundedObject(0D, 0D, 10D, 10D, 0D));

        
        BuildingConnector connector = new BuildingConnector(building1, 
                BUILDING_POSITION, 0D, building2, BUILDING2_POSITION, 180D);
        assertNotNull(connector);
        
        assertEquals(building1, connector.getBuilding1());
        assertEquals(building2, connector.getBuilding2());
        
        Hatch hatch1 = connector.getHatch1();
        assertNotNull(hatch1);
        assertEquals(building1, hatch1.getBuilding());
        assertEquals(connector, hatch1.getBuildingConnector());
        assertEquals(BUILDING2_POSITION, hatch1.getPosition());
        assertEquals(0D, hatch1.getFacing());
        
        Hatch hatch2 = connector.getHatch2();
        assertNotNull(hatch2);
        assertEquals(building2, hatch2.getBuilding());
        assertEquals(connector, hatch2.getBuildingConnector());
        assertEquals(BUILDING2_POSITION, hatch2.getPosition());
        assertEquals(180D, hatch2.getFacing());
        
        assertFalse(connector.isSplitConnection());
        assertEquals(BUILDING2_POSITION, connector.getPosition());

        BuildingConnector connector2 = new BuildingConnector(building2, 
                BUILDING2_POSITION, 180D, building1, BUILDING2_POSITION, 0D);
        assertNotNull(connector2);
        
        assertEquals(connector, connector2);
    }
    
    @Test
    public void testSplitBuildingConnector() {
        
        MockSettlement settlement = new MockSettlement();
        MockBuilding building1 = new MockBuilding(settlement, "1", new BoundedObject(0D, 0D, 10D, 10D, 0D));
        MockBuilding building2 = new MockBuilding(settlement, "2", new BoundedObject(0D, 12D, 10D, 10D, 0D));

        BuildingConnector connector = new BuildingConnector(building1, 
                BUILDING2_POSITION, 0D, building2, BUILDING3_POSITION, 180D);
        assertNotNull(connector);
        
        assertEquals(building1, connector.getBuilding1());
        assertEquals(building2, connector.getBuilding2());
        
        Hatch hatch1 = connector.getHatch1();
        assertNotNull(hatch1);
        assertEquals(building1, hatch1.getBuilding());
        assertEquals(connector, hatch1.getBuildingConnector());
        assertEquals(BUILDING2_POSITION, hatch1.getPosition());
        assertEquals(0D, hatch1.getFacing());
        
        Hatch hatch2 = connector.getHatch2();
        assertNotNull(hatch2);
        assertEquals(building2, hatch2.getBuilding());
        assertEquals(connector, hatch2.getBuildingConnector());
        assertEquals(BUILDING3_POSITION, hatch2.getPosition());
        assertEquals(180D, hatch2.getFacing());
        
        assertTrue(connector.isSplitConnection());
        assertEquals(new LocalPosition(0D, 6D), connector.getPosition());
        
        BuildingConnector connector2 = new BuildingConnector(building2, 
                BUILDING3_POSITION, 180D, building1, BUILDING2_POSITION, 0D);
        assertNotNull(connector2);
        
        assertEquals(connector, connector2);
    }
}

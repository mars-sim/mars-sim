package com.mars_sim.core.building.connection;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.MockBuilding;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.MockSettlement;

public class HatchTest extends AbstractMarsSimUnitTest {

	
    private static final LocalPosition BUILDING_POSITION = new LocalPosition(0D, 0D);
    private static final LocalPosition HATCH_POSITION = new LocalPosition(5D, 12D);
    private static final BoundedObject BUILDING_POSN = new BoundedObject(0, 0, 10, 10, 0);


	public void testConstructor() {
        
        MockSettlement settlement = new MockSettlement();
        MockBuilding building1 = new MockBuilding(settlement, "1", BUILDING_POSN);
        MockBuilding building2 = new MockBuilding(settlement, "2", BUILDING_POSN);
        BuildingConnector connector = new BuildingConnector(building1, 
                BUILDING_POSITION, 0D, building2, BUILDING_POSITION, 0D);
        
        Hatch hatch = new Hatch(building1, connector, HATCH_POSITION, 90D);
        assertNotNull(hatch);
        
        assertEquals(building1, hatch.getBuilding());
        assertEquals(connector, hatch.getBuildingConnector());
        assertEquals(HATCH_POSITION, hatch.getPosition());
        assertEquals(90D, hatch.getFacing());
        assertEquals(Hatch.WIDTH, hatch.getWidth());
        assertEquals(Hatch.LENGTH, hatch.getLength());
    }
    
    public void testSetLocation() {
        MockSettlement settlement = new MockSettlement();
        MockBuilding building1 = new MockBuilding(settlement, "1", BUILDING_POSN);
        MockBuilding building2 = new MockBuilding(settlement, "2", BUILDING_POSN);
        BuildingConnector connector = new BuildingConnector(building1, 
        		BUILDING_POSITION, 0D, building2, BUILDING_POSITION, 0D);
        
        Hatch hatch = new Hatch(building1, connector, HATCH_POSITION, 90D);
        assertNotNull(hatch);
        
        assertEquals(HATCH_POSITION, hatch.getPosition());
    }
    
    public void testEquals() {
        
        MockSettlement settlement = new MockSettlement();
        MockBuilding building1 = new MockBuilding(settlement, "1", BUILDING_POSN);
        MockBuilding building2 = new MockBuilding(settlement, "2", BUILDING_POSN);
        BuildingConnector connector = new BuildingConnector(building1, 
        		BUILDING_POSITION, 0D, building2, BUILDING_POSITION, 0D);
        
        Hatch hatch1 = new Hatch(building1, connector, HATCH_POSITION, 90D);
        assertNotNull(hatch1);
        
        Hatch hatch2 = new Hatch(building1, connector, HATCH_POSITION, 90D);
        assertNotNull(hatch2);
        
        assertEquals(hatch1, hatch2);
    }
}
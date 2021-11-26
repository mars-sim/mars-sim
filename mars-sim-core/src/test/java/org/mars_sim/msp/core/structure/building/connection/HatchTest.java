package org.mars_sim.msp.core.structure.building.connection;

import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.structure.building.MockBuilding;

import junit.framework.TestCase;

public class HatchTest extends TestCase {

	
    private static final LocalPosition BUILDING_POSITION = new LocalPosition(0D, 0D);
    private static final LocalPosition HATCH_POSITION = new LocalPosition(5D, 12D);


	public void testConstructor() {
        
        MockBuilding building1 = new MockBuilding();
        MockBuilding building2 = new MockBuilding();
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
        
        MockBuilding building1 = new MockBuilding();
        MockBuilding building2 = new MockBuilding();
        BuildingConnector connector = new BuildingConnector(building1, 
        		BUILDING_POSITION, 0D, building2, BUILDING_POSITION, 0D);
        
        Hatch hatch = new Hatch(building1, connector, HATCH_POSITION, 90D);
        assertNotNull(hatch);
        
        assertEquals(HATCH_POSITION, hatch.getPosition());
    }
    
    public void testEquals() {
        
        MockBuilding building1 = new MockBuilding();
        MockBuilding building2 = new MockBuilding();
        BuildingConnector connector = new BuildingConnector(building1, 
        		BUILDING_POSITION, 0D, building2, BUILDING_POSITION, 0D);
        
        Hatch hatch1 = new Hatch(building1, connector, HATCH_POSITION, 90D);
        assertNotNull(hatch1);
        
        Hatch hatch2 = new Hatch(building1, connector, HATCH_POSITION, 90D);
        assertNotNull(hatch2);
        
        assertEquals(hatch1, hatch2);
    }
}
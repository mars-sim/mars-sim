package com.mars_sim.core.building.connection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.MockBuilding;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.MockSettlement;

public class InsideBuildingPathTest extends MarsSimUnitTest {

    private static final LocalPosition POSITION_5X5 = new LocalPosition(5D, 5D);
	private static final LocalPosition POSITION_10X5 = new LocalPosition(10D, 5D);
	private static final LocalPosition POSITION_10X10 = new LocalPosition(10D, 10D);
    private static final BoundedObject BUILDING_POSN = new BoundedObject(0, 0, 20, 20, 0);

	@Test
	public void testEmptyPath() {
        
        InsideBuildingPath path = new InsideBuildingPath(Collections.emptyList());
        assertEquals(0D, path.getPathLength());
        assertNull(path.getNextPathLocation());
        
        List<InsidePathLocation> remainingLocations = path.getRemainingPathLocations();
        assertNotNull(remainingLocations);
        assertEquals(0, remainingLocations.size());
    }
    
    @Test
    public void testAddPathLocation() {

        
        MockSettlement settlement = new MockSettlement();
        MockBuilding building = new MockBuilding(settlement, "1", BUILDING_POSN);
        InsidePathLocation location = new BuildingLocation(building, POSITION_5X5);
                
        List<InsidePathLocation> steps = new ArrayList<>();

        steps.add(location);
        InsideBuildingPath path = new InsideBuildingPath(steps);

        assertTrue(path.containsPathLocation(location));
        
        assertEquals(0D, path.getPathLength());
        assertNull(path.getNextPathLocation());
        
        List<InsidePathLocation> remainingLocations2 = path.getRemainingPathLocations();
        assertNotNull(remainingLocations2);
        assertTrue(remainingLocations2.isEmpty());
    }
    
    @Test
    public void testIteratePathLocation() {
        
        
        MockSettlement settlement = new MockSettlement();
        MockBuilding building = new MockBuilding(settlement, "1", BUILDING_POSN);
        InsidePathLocation location1 = new BuildingLocation(building, POSITION_5X5);

        List<InsidePathLocation> steps = new ArrayList<>();
        steps.add(location1);
        
        InsidePathLocation location2 = new BuildingLocation(building, POSITION_10X5);
        steps.add(location2);
        InsideBuildingPath path = new InsideBuildingPath(steps);
        
        assertEquals(location2, path.getNextPathLocation());
    }
    
    @Test
    public void testGetRemainingLocations() {
        
        
        MockSettlement settlement = new MockSettlement();
        MockBuilding building = new MockBuilding(settlement, "1", BUILDING_POSN);
        
        List<InsidePathLocation> steps = new ArrayList<>();

        InsidePathLocation location1 = new BuildingLocation(building, POSITION_5X5);
        steps.add(location1);
        
        InsidePathLocation location2 = new BuildingLocation(building, POSITION_10X5);
        steps.add(location2);
        
        InsidePathLocation location3 = new BuildingLocation(building, POSITION_10X10);
        steps.add(location3);
        
        InsideBuildingPath path = new InsideBuildingPath(steps);
        assertEquals(3, path.getPathLocations().size());

        List<InsidePathLocation> remainingLocations1 = path.getRemainingPathLocations();
        assertNotNull(remainingLocations1);
        assertEquals(2, remainingLocations1.size());
        assertEquals(location2, remainingLocations1.get(0));
        
        path.iteratePathLocation();
        
        List<InsidePathLocation> remainingLocations2 = path.getRemainingPathLocations();
        assertNotNull(remainingLocations2);
        assertEquals(1, remainingLocations2.size());
        assertEquals(location3, remainingLocations2.get(0));
        
        path.iteratePathLocation();
        
        List<InsidePathLocation> remainingLocations3 = path.getRemainingPathLocations();
        assertNotNull(remainingLocations3);
    }
    
    @Test
    public void testIsEndOfPath() {
        
        MockSettlement settlement = new MockSettlement();
        MockBuilding building = new MockBuilding(settlement, "1", BUILDING_POSN);
        
        List<InsidePathLocation> steps = new ArrayList<>();

        InsidePathLocation location1 = new BuildingLocation(building, POSITION_5X5);
        steps.add(location1);
        
        InsidePathLocation location2 = new BuildingLocation(building, POSITION_10X5);
        steps.add(location2);
                
        InsidePathLocation location3 = new BuildingLocation(building, POSITION_10X10);
        steps.add(location3);
        
        InsideBuildingPath path = new InsideBuildingPath(steps);

        assertFalse(path.isEndOfPath());
        
        path.iteratePathLocation();
        
        assertTrue(path.isEndOfPath());
    }
    
    @Test
    public void testGetPathLength() {
        
        
        MockSettlement settlement = new MockSettlement();
        MockBuilding building = new MockBuilding(settlement, "1", BUILDING_POSN);
        List<InsidePathLocation> steps = new ArrayList<>();


        InsidePathLocation location1 = new BuildingLocation(building, POSITION_5X5);
        steps.add(location1);
                
        InsidePathLocation location2 = new BuildingLocation(building, POSITION_10X5);
        steps.add(location2);
                
        InsidePathLocation location3 = new BuildingLocation(building, POSITION_10X10);
        steps.add(location3);
                
        InsidePathLocation location4 = new BuildingLocation(building, POSITION_5X5);
        steps.add(location4);
        
        InsideBuildingPath path = new InsideBuildingPath(steps);

        assertEquals(10D + Math.sqrt(50D), path.getPathLength());
    }
}

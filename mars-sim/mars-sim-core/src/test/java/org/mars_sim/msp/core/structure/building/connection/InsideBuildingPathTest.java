package org.mars_sim.msp.core.structure.building.connection;

import java.util.List;

import org.mars_sim.msp.core.structure.building.MockBuilding;

import junit.framework.TestCase;

public class InsideBuildingPathTest extends TestCase {

    public void testConstructor() {
        
        InsideBuildingPath path = new InsideBuildingPath();
        assertEquals(0D, path.getPathLength());
        assertNull(path.getNextPathLocation());
        
        List<InsidePathLocation> remainingLocations = path.getRemainingPathLocations();
        assertNotNull(remainingLocations);
        assertEquals(0, remainingLocations.size());
    }
    
    public void testAddPathLocation() {
        
        InsideBuildingPath path = new InsideBuildingPath();
        assertEquals(0D, path.getPathLength());
        assertNull(path.getNextPathLocation());
        
        List<InsidePathLocation> remainingLocations1 = path.getRemainingPathLocations();
        assertNotNull(remainingLocations1);
        assertEquals(0, remainingLocations1.size());
        
        MockBuilding building = new MockBuilding();
        InsidePathLocation location = new BuildingLocation(building, 5D, 5D);
        
        assertFalse(path.containsPathLocation(location));
        
        path.addPathLocation(location);
        
        assertTrue(path.containsPathLocation(location));
        
        assertEquals(0D, path.getPathLength());
        assertEquals(location, path.getNextPathLocation());
        
        List<InsidePathLocation> remainingLocations2 = path.getRemainingPathLocations();
        assertNotNull(remainingLocations2);
        assertEquals(1, remainingLocations2.size());
        assertEquals(location, remainingLocations2.get(0));
    }
    
    public void testIteratePathLocation() {
        
        InsideBuildingPath path = new InsideBuildingPath();
        
        MockBuilding building = new MockBuilding();
        InsidePathLocation location1 = new BuildingLocation(building, 5D, 5D);
        path.addPathLocation(location1);
        
        InsidePathLocation location2 = new BuildingLocation(building, 10D, 5D);
        path.addPathLocation(location2);
        
        assertEquals(location1, path.getNextPathLocation());
        
        path.iteratePathLocation();
        
        assertEquals(location2, path.getNextPathLocation());
    }
    
    public void testGetRemainingLocations() {
        
        InsideBuildingPath path = new InsideBuildingPath();
        
        MockBuilding building = new MockBuilding();
        
        InsidePathLocation location1 = new BuildingLocation(building, 5D, 5D);
        path.addPathLocation(location1);
        
        InsidePathLocation location2 = new BuildingLocation(building, 10D, 5D);
        path.addPathLocation(location2);
        
        InsidePathLocation location3 = new BuildingLocation(building, 10D, 10D);
        path.addPathLocation(location3);
        
        List<InsidePathLocation> remainingLocations1 = path.getRemainingPathLocations();
        assertNotNull(remainingLocations1);
        assertEquals(3, remainingLocations1.size());
        assertEquals(location1, remainingLocations1.get(0));
        
        path.iteratePathLocation();
        
        List<InsidePathLocation> remainingLocations2 = path.getRemainingPathLocations();
        assertNotNull(remainingLocations2);
        assertEquals(2, remainingLocations2.size());
        assertEquals(location2, remainingLocations2.get(0));
        
        path.iteratePathLocation();
        
        List<InsidePathLocation> remainingLocations3 = path.getRemainingPathLocations();
        assertNotNull(remainingLocations3);
        assertEquals(1, remainingLocations3.size());
        assertEquals(location3, remainingLocations3.get(0));
    }
    
    public void testIsEndOfPath() {
        
        InsideBuildingPath path = new InsideBuildingPath();
        
        MockBuilding building = new MockBuilding();
        
        InsidePathLocation location1 = new BuildingLocation(building, 5D, 5D);
        path.addPathLocation(location1);
        
        assertTrue(path.isEndOfPath());
        
        InsidePathLocation location2 = new BuildingLocation(building, 10D, 5D);
        path.addPathLocation(location2);
        
        assertFalse(path.isEndOfPath());
        
        InsidePathLocation location3 = new BuildingLocation(building, 10D, 10D);
        path.addPathLocation(location3);
        
        assertFalse(path.isEndOfPath());
        
        path.iteratePathLocation();
        
        assertFalse(path.isEndOfPath());
        
        path.iteratePathLocation();
        
        assertTrue(path.isEndOfPath());
    }
    
    public void testGetPathLength() {
        
        InsideBuildingPath path = new InsideBuildingPath();
        
        MockBuilding building = new MockBuilding();
        
        InsidePathLocation location1 = new BuildingLocation(building, 5D, 5D);
        path.addPathLocation(location1);
        
        assertEquals(0D, path.getPathLength());
        
        InsidePathLocation location2 = new BuildingLocation(building, 10D, 5D);
        path.addPathLocation(location2);
        
        assertEquals(5D, path.getPathLength());
        
        InsidePathLocation location3 = new BuildingLocation(building, 10D, 10D);
        path.addPathLocation(location3);
        
        assertEquals(10D, path.getPathLength());
        
        InsidePathLocation location4 = new BuildingLocation(building, 5D, 5D);
        path.addPathLocation(location4);
        
        assertEquals(10D + Math.sqrt(50D), path.getPathLength());
    }
    
    public void testClone() {
        
        InsideBuildingPath path1 = new InsideBuildingPath();
        
        MockBuilding building = new MockBuilding();
        
        InsidePathLocation location1 = new BuildingLocation(building, 5D, 5D);
        path1.addPathLocation(location1);
        
        InsidePathLocation location2 = new BuildingLocation(building, 10D, 5D);
        path1.addPathLocation(location2);
        
        InsidePathLocation location3 = new BuildingLocation(building, 10D, 10D);
        path1.addPathLocation(location3);
        
        InsideBuildingPath path2 = (InsideBuildingPath) path1.clone();
        assertNotNull(path2);
        
        assertEquals(location1, path2.getNextPathLocation());
        assertEquals(10D, path2.getPathLength());
        
        List<InsidePathLocation> remainingLocations2 = path2.getRemainingPathLocations();
        assertNotNull(remainingLocations2);
        assertEquals(3, remainingLocations2.size());
        assertEquals(location1, remainingLocations2.get(0));
        
        path2.iteratePathLocation();
        
        InsideBuildingPath path3 = (InsideBuildingPath) path2.clone();
        assertNotNull(path3);
        
        assertEquals(location2, path3.getNextPathLocation());
        assertEquals(10D, path3.getPathLength());
        
        List<InsidePathLocation> remainingLocations3 = path3.getRemainingPathLocations();
        assertNotNull(remainingLocations3);
        assertEquals(2, remainingLocations3.size());
        assertEquals(location2, remainingLocations3.get(0));
    }
}
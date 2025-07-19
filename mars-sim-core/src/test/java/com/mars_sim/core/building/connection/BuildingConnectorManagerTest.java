package com.mars_sim.core.building.connection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Before;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.BuildingTemplate;
import com.mars_sim.core.building.MockBuilding;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.task.WalkSettlementInterior;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;

import junit.framework.TestCase;

public class BuildingConnectorManagerTest extends TestCase {

    private static final double SMALL_DELTA = .0000001D;
    
	@Before
	public void setUp() {
	    // Create new simulation instance.
        SimulationConfig simConfig = SimulationConfig.loadConfig();
        
        Simulation sim = Simulation.instance();
        sim.testRun();
        
        Function.initializeInstances(simConfig.getBuildingConfiguration(), sim.getMasterClock(),
        							 simConfig.getPersonConfig(), simConfig.getCropConfiguration(), sim.getSurfaceFeatures(),
        							 sim.getWeather(), sim.getUnitManager());
	}
    
    public void testConstructorNoBuildingTemplates() {        
        Settlement settlement = new MockSettlement();

        List<BuildingTemplate> buildingTemplates = new ArrayList<>(0);

        BuildingConnectorManager manager = new BuildingConnectorManager(settlement, buildingTemplates);
        assertNotNull(manager);

        Set<BuildingConnector> connections = manager.getAllBuildingConnections();
        assertNotNull(connections);
        assertEquals(0, connections.size());
    }

    public void testConstructorWithBuildingTemplates() {

        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();

        MockBuilding building0 = new MockBuilding(settlement, 0, new BoundedObject(0D, 0D, 9D, 9D, 0D));
        BuildingTemplate buildingTemplate0 = new BuildingTemplate("0", 0, "building 0", "building 0", new BoundedObject(0D, 0D, 9D, 9D, 0D));
        buildingTemplate0.addBuildingConnection("2", new LocalPosition(-4.5D, 0D));
        buildingManager.addBuilding(building0, false);

        MockBuilding building1 = new MockBuilding(settlement, 1, new BoundedObject(-12D, 0D, 6D, 9D, 270D));
        BuildingTemplate buildingTemplate1 = new BuildingTemplate("1", 0, "building 1","building 1", new BoundedObject(-12D, 0D, 6D, 9D, 270D));
        buildingTemplate1.addBuildingConnection("2", new LocalPosition(0D, 4.5D));
        buildingManager.addBuilding(building1, false);

        MockBuilding building2 = new MockBuilding(settlement, 2, new BoundedObject(-6D, 0D, 2D, 3D, 270D));
        BuildingTemplate buildingTemplate2 = new BuildingTemplate("2", 0, "building 2","building 2", new BoundedObject(-6D, 0D, 6D, 9D, 270D));
        buildingTemplate2.addBuildingConnection("0", new LocalPosition(0D, 1.5D));
        buildingTemplate2.addBuildingConnection("1", new LocalPosition(0D, -1.5D));
        buildingManager.addBuilding(building2, false);

        List<BuildingTemplate> buildingTemplates = new ArrayList<BuildingTemplate>();
        buildingTemplates.add(buildingTemplate0);
        buildingTemplates.add(buildingTemplate1);
        buildingTemplates.add(buildingTemplate2);

        BuildingConnectorManager manager = new BuildingConnectorManager(settlement, buildingTemplates);
        assertNotNull(manager);

        assertEquals(settlement, manager.getSettlement());

        Set<BuildingConnector> connections = manager.getAllBuildingConnections();
        assertNotNull(connections);
        assertEquals(2, connections.size());
        Iterator<BuildingConnector> i = connections.iterator();
        while (i.hasNext()) {
            BuildingConnector connector = i.next();
            assertTrue(manager.containsBuildingConnector(connector));
        }

        Set<BuildingConnector> connections1 = manager.getBuildingConnections(building0, building2);
        assertNotNull(connections1);
        assertEquals(1, connections1.size());

        Set<BuildingConnector> connections2 = manager.getBuildingConnections(building2, building0);
        assertNotNull(connections2);
        assertEquals(1, connections2.size());

        Set<BuildingConnector> connections3 = manager.getBuildingConnections(building1, building2);
        assertNotNull(connections3);
        assertEquals(1, connections3.size());

        Set<BuildingConnector> connections4 = manager.getBuildingConnections(building2, building1);
        assertNotNull(connections4);
        assertEquals(1, connections4.size());

        Set<BuildingConnector> connections5 = manager.getConnectionsToBuilding(building0);
        assertNotNull(connections5);
        assertEquals(1, connections5.size());

        Set<BuildingConnector> connections6 = manager.getConnectionsToBuilding(building1);
        assertNotNull(connections6);
        assertEquals(1, connections6.size());

        Set<BuildingConnector> connections7 = manager.getConnectionsToBuilding(building2);
        assertNotNull(connections7);
        assertEquals(2, connections7.size());

        BuildingConnector[] origConnections = new BuildingConnector[2];
        connections.toArray(origConnections);

        manager.removeBuildingConnection(origConnections[0]);

        assertEquals(1, manager.getAllBuildingConnections().size());

        manager.removeBuildingConnection(origConnections[1]);

        assertEquals(0, manager.getAllBuildingConnections().size());
    }

    public void testDetermineShortestPath() {
        
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();

        MockBuilding building0 = new MockBuilding(settlement, 0, new BoundedObject(0D, 0D, 9D, 9D, 0D));
        BuildingTemplate buildingTemplate0 = new BuildingTemplate("0", 0, "building 0", "building 0", new BoundedObject(0D, 0D, 9D, 9D, 0D));
        buildingTemplate0.addBuildingConnection("2", new LocalPosition(-4.5D, 0D));
        buildingManager.addBuilding(building0, false);

        MockBuilding building1 = new MockBuilding(settlement, 1, new BoundedObject(-12D, 0D, 6D, 9D, 270D));
        BuildingTemplate buildingTemplate1 = new BuildingTemplate("1", 0, "building 1", "building 1", new BoundedObject(-12D, 0D, 6D, 9D, 270D));
        buildingTemplate1.addBuildingConnection("2", new LocalPosition(0D, 4.5D));
        buildingManager.addBuilding(building1, false);

        MockBuilding building2 = new MockBuilding(settlement, 2, new BoundedObject(-6D, 0D, 2D, 3D, 270D));
        BuildingTemplate buildingTemplate2 = new BuildingTemplate("2", 0, "building 2", "building 2", new BoundedObject(-6D, 0D, 6D, 9D, 270D));
        buildingTemplate2.addBuildingConnection("0", new LocalPosition(0D, 1.5D));
        buildingTemplate2.addBuildingConnection("1", new LocalPosition(0D, -1.5D));
        buildingManager.addBuilding(building2, false);

        List<BuildingTemplate> buildingTemplates = new ArrayList<BuildingTemplate>();
        buildingTemplates.add(buildingTemplate0);
        buildingTemplates.add(buildingTemplate1);
        buildingTemplates.add(buildingTemplate2);

        BuildingConnectorManager manager = new BuildingConnectorManager(settlement, buildingTemplates);

        InsideBuildingPath path1 = manager.determineShortestPath(100, //WalkSettlementInterior.NUM_ITERATION, 
        		building0, new LocalPosition(0D, 0D), building0, new LocalPosition(4.5D, 0D));
        assertNotNull(path1);
        assertEquals(4.5D, path1.getPathLength(), SMALL_DELTA);
        assertEquals(1, path1.getRemainingPathLocations().size());
        InsidePathLocation nextPath = path1.getNextPathLocation();
        assertNotNull(nextPath);
        assertEquals(4.5D, nextPath.getPosition().getX(), SMALL_DELTA);
        assertEquals(0D, nextPath.getPosition().getY(), SMALL_DELTA);
        assertTrue(path1.isEndOfPath());

        InsideBuildingPath path2 = manager.determineShortestPath(100, //WalkSettlementInterior.NUM_ITERATION, 
        		building0, new LocalPosition(2D, -1D), building2, new LocalPosition(-5D, 1D));

        // 2016-12-09 To pass maven test, change the code in getBuilding(int id) in BuildingManager to the non-java stream version
        assertNotNull(path2);
        assertEquals(7.694507207732848D, path2.getPathLength(), SMALL_DELTA);
        assertEquals(2, path2.getRemainingPathLocations().size());
        nextPath = path2.getNextPathLocation();
        assertNotNull(nextPath);
        assertEquals(-4.5D, nextPath.getPosition().getX(), SMALL_DELTA);
        assertEquals(0D, nextPath.getPosition().getY(), SMALL_DELTA);
        assertFalse(path2.isEndOfPath());
        path2.iteratePathLocation();
        assertEquals(1, path2.getRemainingPathLocations().size());
        nextPath = path2.getNextPathLocation();
        assertNotNull(nextPath);
        assertEquals(-5D, nextPath.getPosition().getX(), SMALL_DELTA);
        assertEquals(1D, nextPath.getPosition().getY(), SMALL_DELTA);
        assertTrue(path2.isEndOfPath());

        InsideBuildingPath path3 = manager.determineShortestPath(100, //WalkSettlementInterior.NUM_ITERATION, 
        		building0, new LocalPosition(2D, -1D), building1, new LocalPosition(-10D, 1D));
        assertNotNull(path3);
        assertEquals(12.269055622550205D, path3.getPathLength(), SMALL_DELTA);
        assertEquals(4, path3.getRemainingPathLocations().size());
        nextPath = path3.getNextPathLocation();
        assertNotNull(nextPath);
        assertEquals(-4.5D, nextPath.getPosition().getX(), SMALL_DELTA);
        assertEquals(0D, nextPath.getPosition().getY(), SMALL_DELTA);
        assertFalse(path3.isEndOfPath());
        path3.iteratePathLocation();
        assertEquals(3, path3.getRemainingPathLocations().size());
        nextPath = path3.getNextPathLocation();
        assertNotNull(nextPath);
        assertEquals(-6D, nextPath.getPosition().getX(), SMALL_DELTA);
        assertEquals(0D, nextPath.getPosition().getY(), SMALL_DELTA);
        assertFalse(path3.isEndOfPath());
        path3.iteratePathLocation();
        assertEquals(2, path3.getRemainingPathLocations().size());
        nextPath = path3.getNextPathLocation();
        assertNotNull(nextPath);
        assertEquals(-7.5D, nextPath.getPosition().getX(), SMALL_DELTA);
        assertEquals(0D, nextPath.getPosition().getY(), .0001D);
        assertFalse(path3.isEndOfPath());
        path3.iteratePathLocation();
        assertEquals(1, path3.getRemainingPathLocations().size());
        nextPath = path3.getNextPathLocation();
        assertNotNull(nextPath);
        assertEquals(-10D, nextPath.getPosition().getX(), SMALL_DELTA);
        assertEquals(1D, nextPath.getPosition().getY(), SMALL_DELTA);
        assertTrue(path3.isEndOfPath());
    }
}
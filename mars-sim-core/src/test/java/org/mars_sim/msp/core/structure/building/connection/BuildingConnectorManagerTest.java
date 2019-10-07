package org.mars_sim.msp.core.structure.building.connection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.MockBuilding;

import junit.framework.TestCase;

public class BuildingConnectorManagerTest extends TestCase {

    private static final double SMALL_DELTA = .0000001D;

    public void testConstructorNoBuildingTemplates() {
        SimulationConfig.instance().loadConfig();
        Simulation.instance().testRun();
        
        Settlement settlement = new MockSettlement();

        List<BuildingTemplate> buildingTemplates = new ArrayList<BuildingTemplate>(0);

        BuildingConnectorManager manager = new BuildingConnectorManager(settlement, buildingTemplates);
        assertNotNull(manager);

        Set<BuildingConnector> connections = manager.getAllBuildingConnections();
        assertNotNull(connections);
        assertEquals(0, connections.size());
    }

    public void testConstructorWithBuildingTemplates() {
        SimulationConfig.instance().loadConfig();
        Simulation.instance().testRun();
        
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();

        MockBuilding building0 = new MockBuilding(buildingManager);
        building0.setTemplateID(0);
        building0.setName("building 0");
        building0.setWidth(9D);
        building0.setLength(9D);
        building0.setXLocation(0D);
        building0.setYLocation(0D);
        building0.setFacing(0D);
        BuildingTemplate buildingTemplate0 = new BuildingTemplate(null, 0, "A", "building 0", "building 0", 9D, 9D, 0D, 0D, 0D);
        buildingTemplate0.addBuildingConnection(2, -4.5D, 0D);
        buildingManager.addBuilding(building0, false);

        MockBuilding building1 = new MockBuilding(buildingManager);
        building1.setTemplateID(1);
        building1.setName("building 1");
        building1.setWidth(6D);
        building1.setLength(9D);
        building1.setXLocation(-12D);
        building1.setYLocation(0D);
        building1.setFacing(270D);
        BuildingTemplate buildingTemplate1 = new BuildingTemplate(null, 1, null, "building 1","building 1",  6D, 9D, -12D, 0D, 270D);
        buildingTemplate1.addBuildingConnection(2, 0D, 4.5D);
        buildingManager.addBuilding(building1, false);

        MockBuilding building2 = new MockBuilding(buildingManager);
        building2.setTemplateID(2);
        building2.setName("building 2");
        building2.setWidth(2D);
        building2.setLength(3D);
        building2.setXLocation(-6D);
        building2.setYLocation(0D);
        building2.setFacing(270D);
        BuildingTemplate buildingTemplate2 = new BuildingTemplate(null, 2, null, "building 2","building 2", 6D, 9D, -6D, 0D, 270D);
        buildingTemplate2.addBuildingConnection(0, 0D, 1.5D);
        buildingTemplate2.addBuildingConnection(1, 0D, -1.5D);
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
        SimulationConfig.instance().loadConfig();
        Simulation.instance().testRun();
        
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();

        MockBuilding building0 = new MockBuilding(buildingManager);
        building0.setTemplateID(0);
        building0.setName("building 0");
        building0.setWidth(9D);
        building0.setLength(9D);
        building0.setXLocation(0D);
        building0.setYLocation(0D);
        building0.setFacing(0D);
        BuildingTemplate buildingTemplate0 = new BuildingTemplate(null, 0, null, "building 0", "building 0", 9D, 9D, 0D, 0D, 0D);
        buildingTemplate0.addBuildingConnection(2, -4.5D, 0D);
        buildingManager.addBuilding(building0, false);

        MockBuilding building1 = new MockBuilding(buildingManager);
        building1.setTemplateID(1);
        building1.setName("building 1");
        building1.setWidth(6D);
        building1.setLength(9D);
        building1.setXLocation(-12D);
        building1.setYLocation(0D);
        building1.setFacing(270D);
        BuildingTemplate buildingTemplate1 = new BuildingTemplate(null, 1, null, "building 1", "building 1",6D, 9D, -12D, 0D, 270D);
        buildingTemplate1.addBuildingConnection(2, 0D, 4.5D);
        buildingManager.addBuilding(building1, false);

        MockBuilding building2 = new MockBuilding(buildingManager);
        building2.setTemplateID(2);
        building2.setName("building 2");
        building2.setWidth(2D);
        building2.setLength(3D);
        building2.setXLocation(-6D);
        building2.setYLocation(0D);
        building2.setFacing(270D);
        BuildingTemplate buildingTemplate2 = new BuildingTemplate(null, 2, null, "building 2", "building 2",6D, 9D, -6D, 0D, 270D);
        buildingTemplate2.addBuildingConnection(0, 0D, 1.5D);
        buildingTemplate2.addBuildingConnection(1, 0D, -1.5D);
        buildingManager.addBuilding(building2, false);

        List<BuildingTemplate> buildingTemplates = new ArrayList<BuildingTemplate>();
        buildingTemplates.add(buildingTemplate0);
        buildingTemplates.add(buildingTemplate1);
        buildingTemplates.add(buildingTemplate2);

        BuildingConnectorManager manager = new BuildingConnectorManager(settlement, buildingTemplates);

        InsideBuildingPath path1 = manager.determineShortestPath(building0, 0D, 0D, building0, 4.5D, 0D);
        assertNotNull(path1);
        assertEquals(4.5D, path1.getPathLength(), SMALL_DELTA);
        assertEquals(1, path1.getRemainingPathLocations().size());
        assertNotNull(path1.getNextPathLocation());
        assertEquals(4.5D, path1.getNextPathLocation().getXLocation(), SMALL_DELTA);
        assertEquals(0D, path1.getNextPathLocation().getYLocation(), SMALL_DELTA);
        assertTrue(path1.isEndOfPath());

        InsideBuildingPath path2 = manager.determineShortestPath(building0, 2D, -1D, building2, -5D, 1D);

        // 2016-12-09 To pass maven test, change the code in getBuilding(int id) in BuildingManager to the non-java stream version
        assertNotNull(path2);
        assertEquals(7.694507207732848D, path2.getPathLength(), SMALL_DELTA);
        assertEquals(2, path2.getRemainingPathLocations().size());
        assertNotNull(path2.getNextPathLocation());
        assertEquals(-4.5D, path2.getNextPathLocation().getXLocation(), SMALL_DELTA);
        assertEquals(0D, path2.getNextPathLocation().getYLocation(), SMALL_DELTA);
        assertFalse(path2.isEndOfPath());
        path2.iteratePathLocation();
        assertEquals(1, path2.getRemainingPathLocations().size());
        assertNotNull(path2.getNextPathLocation());
        assertEquals(-5D, path2.getNextPathLocation().getXLocation(), SMALL_DELTA);
        assertEquals(1D, path2.getNextPathLocation().getYLocation(), SMALL_DELTA);
        assertTrue(path2.isEndOfPath());

        InsideBuildingPath path3 = manager.determineShortestPath(building0, 2D, -1D, building1, -10D, 1D);
        assertNotNull(path3);
        assertEquals(12.269055622550205D, path3.getPathLength(), SMALL_DELTA);
        assertEquals(4, path3.getRemainingPathLocations().size());
        assertNotNull(path3.getNextPathLocation());
        assertEquals(-4.5D, path3.getNextPathLocation().getXLocation(), SMALL_DELTA);
        assertEquals(0D, path3.getNextPathLocation().getYLocation(), SMALL_DELTA);
        assertFalse(path3.isEndOfPath());
        path3.iteratePathLocation();
        assertEquals(3, path3.getRemainingPathLocations().size());
        assertNotNull(path3.getNextPathLocation());
        assertEquals(-6D, path3.getNextPathLocation().getXLocation(), SMALL_DELTA);
        assertEquals(0D, path3.getNextPathLocation().getYLocation(), SMALL_DELTA);
        assertFalse(path3.isEndOfPath());
        path3.iteratePathLocation();
        assertEquals(2, path3.getRemainingPathLocations().size());
        assertNotNull(path3.getNextPathLocation());
        assertEquals(-7.5D, path3.getNextPathLocation().getXLocation(), SMALL_DELTA);
        assertEquals(0D, path3.getNextPathLocation().getYLocation(), .0001D);
        assertFalse(path3.isEndOfPath());
        path3.iteratePathLocation();
        assertEquals(1, path3.getRemainingPathLocations().size());
        assertNotNull(path3.getNextPathLocation());
        assertEquals(-10D, path3.getNextPathLocation().getXLocation(), SMALL_DELTA);
        assertEquals(1D, path3.getNextPathLocation().getYLocation(), SMALL_DELTA);
        assertTrue(path3.isEndOfPath());
    }
}
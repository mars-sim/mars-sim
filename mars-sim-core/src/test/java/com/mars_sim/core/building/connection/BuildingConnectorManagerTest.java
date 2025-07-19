package com.mars_sim.core.building.connection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.BuildingTemplate;
import com.mars_sim.core.building.MockBuilding;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;

import junit.framework.TestCase;

public class BuildingConnectorManagerTest extends TestCase {

    private static final double SMALL_DELTA = .0000001D;
    private SimulationConfig simConfig;
    
	@Override
	public void setUp() {
	    // Create new simulation instance.
        simConfig = SimulationConfig.loadConfig();
        
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

        BuildingTemplate buildingTemplate0 = new BuildingTemplate("0", 0, "building 0", "building 0", new BoundedObject(0D, 0D, 9D, 9D, 0D));
        buildingTemplate0.addBuildingConnection("2", new LocalPosition(-4.5D, 0D));

        BuildingTemplate buildingTemplate1 = new BuildingTemplate("1", 0, "building 1","building 1", new BoundedObject(-12D, 0D, 6D, 9D, 270D));
        buildingTemplate1.addBuildingConnection("2", new LocalPosition(0D, 4.5D));

        BuildingTemplate buildingTemplate2 = new BuildingTemplate("2", 0, "building 2","building 2", new BoundedObject(-6D, 0D, 6D, 9D, 270D));
        buildingTemplate2.addBuildingConnection("0", new LocalPosition(0D, 1.5D));
        buildingTemplate2.addBuildingConnection("1", new LocalPosition(0D, -1.5D));

        List<BuildingTemplate> buildingTemplates = new ArrayList<BuildingTemplate>();
        buildingTemplates.add(buildingTemplate0);
        buildingTemplates.add(buildingTemplate1);
        buildingTemplates.add(buildingTemplate2);

        List<Building> buildings = new ArrayList<>();
        for(var bt : buildingTemplates) {
            buildings.add(addBuildingFromTemplate(settlement, bt));
        }

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

        Set<BuildingConnector> connections1 = manager.getBuildingConnections(buildings.get(0), buildings.get(2));
        assertNotNull(connections1);
        assertEquals(1, connections1.size());

        Set<BuildingConnector> connections2 = manager.getBuildingConnections(buildings.get(2), buildings.get(0));
        assertNotNull(connections2);
        assertEquals(1, connections2.size());

        Set<BuildingConnector> connections3 = manager.getBuildingConnections(buildings.get(1), buildings.get(2));
        assertNotNull(connections3);
        assertEquals(1, connections3.size());

        Set<BuildingConnector> connections4 = manager.getBuildingConnections(buildings.get(2), buildings.get(1));
        assertNotNull(connections4);
        assertEquals(1, connections4.size());

        Set<BuildingConnector> connections5 = manager.getConnectionsToBuilding(buildings.get(0));
        assertNotNull(connections5);
        assertEquals(1, connections5.size());

        Set<BuildingConnector> connections6 = manager.getConnectionsToBuilding(buildings.get(1));
        assertNotNull(connections6);
        assertEquals(1, connections6.size());

        Set<BuildingConnector> connections7 = manager.getConnectionsToBuilding(buildings.get(2));
        assertNotNull(connections7);
        assertEquals(2, connections7.size());

        manager.removeAllConnectionsToBuilding(buildings.get(1));
        assertTrue("Nothing to building 1", manager.getConnectionsToBuilding(buildings.get(1)).isEmpty());
        assertEquals("Building 2 reduced", 1, manager.getConnectionsToBuilding(buildings.get(2)).size());

    }

    public void testShortestPathAdjacent() {
        
        Settlement settlement = new MockSettlement();

        BuildingTemplate buildingTemplate0 = new BuildingTemplate("0", 0, "building 0", "building 0", new BoundedObject(0D, 0D, 9D, 9D, 0D));
        var connectorB0B2 = new LocalPosition(-4.5D, 0D);
        buildingTemplate0.addBuildingConnection("2", connectorB0B2);

        BuildingTemplate buildingTemplate1 = new BuildingTemplate("1", 0, "building 1", "building 1", new BoundedObject(-12D, 0D, 6D, 9D, 270D));
        buildingTemplate1.addBuildingConnection("2", new LocalPosition(0D, 4.5D));

        BuildingTemplate buildingTemplate2 = new BuildingTemplate("2", 0, "building 2", "building 2", new BoundedObject(-6D, 0D, 6D, 9D, 270D));
        buildingTemplate2.addBuildingConnection("0", new LocalPosition(0D, 1.5D));
        buildingTemplate2.addBuildingConnection("1", new LocalPosition(0D, -1.5D));

        List<BuildingTemplate> buildingTemplates = new ArrayList<BuildingTemplate>();
        buildingTemplates.add(buildingTemplate0);
        buildingTemplates.add(buildingTemplate1);
        buildingTemplates.add(buildingTemplate2);

        List<Building> buildings = new ArrayList<>();
        for(var bt : buildingTemplates) {
            buildings.add(addBuildingFromTemplate(settlement, bt));
        }

        BuildingConnectorManager manager = new BuildingConnectorManager(settlement, buildingTemplates);

        var b0 = buildings.get(0);
        var b2 = buildings.get(2);

        var startPosn = new LocalPosition(2D, -1D);
        var endPosn = new LocalPosition(-5D, 1D);
        InsideBuildingPath path2 = manager.determineShortestPath( b0, startPosn, b2, endPosn);

        assertNotNull(path2);
        assertPathValidity(path2, b0, b2);
        assertEquals(3, path2.getPathLocations().size());

        assertEquals(7.694507207732848D, path2.getPathLength(), SMALL_DELTA);
        assertEquals(2, path2.getRemainingPathLocations().size());
        var nextPath = path2.getNextPathLocation();
        assertNotNull(nextPath);
        assertEquals(connectorB0B2.getX(), nextPath.getPosition().getX(), SMALL_DELTA);
        assertEquals(connectorB0B2.getY(), nextPath.getPosition().getY(), SMALL_DELTA);
        assertFalse(path2.isEndOfPath());

        path2.iteratePathLocation();
        assertEquals(1, path2.getRemainingPathLocations().size());
        nextPath = path2.getNextPathLocation();
        assertNotNull(nextPath);
        assertEquals(endPosn.getX(), nextPath.getPosition().getX(), SMALL_DELTA);
        assertEquals(endPosn.getY(), nextPath.getPosition().getY(), SMALL_DELTA);
        assertTrue(path2.isEndOfPath());

    }

     public void testShortestPathMiddleBuilding() {
        
        Settlement settlement = new MockSettlement();

        BuildingTemplate buildingTemplate0 = new BuildingTemplate("0", 0, "building 0", "building 0", new BoundedObject(0D, 0D, 9D, 9D, 0D));
        var connectorB0B2 = new LocalPosition(-4.5D, 0D);
        buildingTemplate0.addBuildingConnection("2", connectorB0B2);

        BuildingTemplate buildingTemplate1 = new BuildingTemplate("1", 0, "building 1", "building 1", new BoundedObject(-12D, 0D, 6D, 9D, 270D));
        var connectorB1B2 = new LocalPosition(0D, 4.5D);
        buildingTemplate1.addBuildingConnection("2", connectorB1B2);

        BuildingTemplate buildingTemplate2 = new BuildingTemplate("2", 0, "building 2", "building 2", new BoundedObject(-6D, 0D, 6D, 9D, 270D));
        var connectorB2B0 = new LocalPosition(0D, 1.5D);
        var connectorB2B1 = new LocalPosition(0D, -1.5D);
        buildingTemplate2.addBuildingConnection("0", connectorB2B0);
        buildingTemplate2.addBuildingConnection("1", connectorB2B1);

        List<BuildingTemplate> buildingTemplates = new ArrayList<>();
        buildingTemplates.add(buildingTemplate0);
        buildingTemplates.add(buildingTemplate1);
        buildingTemplates.add(buildingTemplate2);

        List<Building> buildings = new ArrayList<>();
        for(var bt : buildingTemplates) {
            buildings.add(addBuildingFromTemplate(settlement, bt));
        }

        BuildingConnectorManager manager = new BuildingConnectorManager(settlement, buildingTemplates);

        var b0 = buildings.get(0);
        var b1 = buildings.get(1);
        var b2 = buildings.get(2);

        var startPosn = new LocalPosition(2D, -1D);
        var endPosn = new LocalPosition(-10D, 1D);  
        InsideBuildingPath path3 = manager.determineShortestPath(b0, startPosn, b1, endPosn);
        assertNotNull(path3);
        assertPathValidity(path3, b0, b1);
        assertEquals(5, path3.getPathLocations().size());

        assertEquals(12.269055622550205D, path3.getPathLength(), SMALL_DELTA);
        assertEquals(4, path3.getRemainingPathLocations().size());
        
        var nextPath = path3.getNextPathLocation();
        assertNotNull(nextPath);
        assertEquals(connectorB0B2.getX(), nextPath.getPosition().getX(), SMALL_DELTA);
        assertEquals(connectorB0B2.getY(), nextPath.getPosition().getY(), SMALL_DELTA);
        assertFalse(path3.isEndOfPath());

        path3.iteratePathLocation();
        assertEquals(3, path3.getRemainingPathLocations().size());
        nextPath = path3.getNextPathLocation();
        assertNotNull(nextPath);
        assertEquals(b2.getPosition().getX(), nextPath.getPosition().getX(), SMALL_DELTA);
        assertEquals(b2.getPosition().getY(), nextPath.getPosition().getY(), SMALL_DELTA);
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
        assertEquals(endPosn.getX(), nextPath.getPosition().getX(), SMALL_DELTA);
        assertEquals(endPosn.getY(), nextPath.getPosition().getY(), SMALL_DELTA);
        assertTrue(path3.isEndOfPath());
    }

    public void testShortestPathSameBuilding() {
        
        Settlement settlement = new MockSettlement();

        BuildingTemplate buildingTemplate0 = new BuildingTemplate("0", 0, "building 0", "building 0", new BoundedObject(0D, 0D, 9D, 9D, 0D));

        List<BuildingTemplate> buildingTemplates = new ArrayList<BuildingTemplate>();
        buildingTemplates.add(buildingTemplate0);

        var b0 = addBuildingFromTemplate(settlement, buildingTemplate0);

        BuildingConnectorManager manager = new BuildingConnectorManager(settlement, buildingTemplates);

        var startPosn = new LocalPosition(0D, 0D);
        var endPosn = new LocalPosition(4.5D, 0D);
        InsideBuildingPath path1 = manager.determineShortestPath(b0, startPosn, b0, endPosn);
        assertNotNull(path1);
        assertEquals("Path1 length", 2, path1.getPathLocations().size());
        assertPathValidity(path1, b0, b0);

        assertEquals(4.5D, path1.getPathLength(), SMALL_DELTA);
        assertEquals(1, path1.getRemainingPathLocations().size());
        InsidePathLocation nextPath = path1.getNextPathLocation();
        assertNotNull(nextPath);
        assertEquals(endPosn.getX(), nextPath.getPosition().getX(), SMALL_DELTA);
        assertEquals(endPosn.getY(), nextPath.getPosition().getY(), SMALL_DELTA);
        assertTrue(path1.isEndOfPath());
    }

    private Building addBuildingFromTemplate(Settlement settlement, BuildingTemplate template) {
        BuildingManager buildingManager = settlement.getBuildingManager();

        MockBuilding newBuilding = new MockBuilding(settlement, template.getID(), template.getBounds());
        buildingManager.addBuilding(newBuilding, false);

        return newBuilding;
    }

    public void testLargeRoute() {
        var largeTemplate = "Sector Base 1-MS";
        List<BuildingTemplate> buildingTemplates = simConfig.getSettlementTemplateConfiguration().getItem(largeTemplate).getSupplies().getBuildings();
        Settlement settlement = new MockSettlement();

        for(var bt : buildingTemplates) {
            addBuildingFromTemplate(settlement, bt);
        }
        BuildingConnectorManager manager = new BuildingConnectorManager(settlement, buildingTemplates);

        var bMgr = settlement.getBuildingManager();
        var lHab = bMgr.getBuildingByTemplateID("000"); // Lander Hab
        var lab = bMgr.getBuildingByTemplateID("605"); // Laboratory
        var path = manager.determineShortestPath(lHab, lHab.getPosition(), lab, lab.getPosition());
        assertNotNull("Found route", path);
        assertPathValidity(path, lHab, lab);

        var core = bMgr.getBuildingByTemplateID("906n7"); 
        path = manager.determineShortestPath(lHab, lHab.getPosition(), core, core.getPosition());
        assertNotNull("Found route", path);
        assertPathValidity(path, lHab, core);
    }

    /**
     * Assert the path is valid.
     * 1. Path only changes Building at a Connector
     * 2. First item is start building
     * 3. Last item is end building
     * @param path
     * @param lHab
     * @param lab
     */
    public static void assertPathValidity(InsideBuildingPath path, Building start, Building end) {
        var steps = path.getPathLocations();

        assertEquals("First step is start building", start, ((BuildingLocation)steps.get(0)).getBuilding());
        assertEquals("Last step is end building", end, ((BuildingLocation)steps.get(steps.size()-1)).getBuilding());

        int i = 0;
        var currentBuilding = start;
        for(var step : steps) {
            switch(step) {
                case BuildingLocation bl -> assertEquals("Step " + i + " is in buildingLocation ", currentBuilding, bl.getBuilding());
                case Building b -> assertEquals("Step " + i + " is Building ", currentBuilding, b);
                case Hatch h -> assertEquals("Step " + i + " is Hatch ", currentBuilding, h.getBuilding());
                case BuildingConnector bc -> {
                    if (bc.getBuilding1().equals(currentBuilding)) {
                        currentBuilding = bc.getBuilding2();
                    }
                    else if (bc.getBuilding2().equals(currentBuilding)) {
                        currentBuilding = bc.getBuilding1();
                    }
                    else {
                        fail("Step " + i + " is a BuildingConnector but does not match current building: " + currentBuilding);
                    }
                }
                default -> fail("Step " + i + " is unknown type " + step.getClass().getSimpleName());
            }
            i++;
        }
    }
}
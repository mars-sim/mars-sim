/**
 * Mars Simulation Project
 * BuildingConnectorManager.java
 * @version 3.06 2013-11-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.connection;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.BuildingTemplate.BuildingConnectionTemplate;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

/**
 * Manages all the building connectors at a settlement.
 */
public class BuildingConnectorManager implements Serializable {

    // Data members.
    private Settlement settlement;
    private Set<BuildingConnector> buildingConnections;
    
    /**
     * Constructor
     * @param settlement the settlement.
     */
    public BuildingConnectorManager(Settlement settlement) {
        
        this(settlement, SimulationConfig.instance().getSettlementConfiguration().
                getSettlementTemplate(settlement.getTemplate()).getBuildingTemplates());
    }
    
    /**
     * Constructor
     * @param settlement the settlement.
     * @param buildingTemplates a list of building templates.
     */
    public BuildingConnectorManager(Settlement settlement, List<BuildingTemplate> buildingTemplates) {
        
        if (settlement == null) {
            throw new IllegalArgumentException("Settlement cannot be null");
        }
        
        this.settlement = settlement;
        buildingConnections = Collections.synchronizedSet(new HashSet<BuildingConnector>());
        
        BuildingManager buildingManager = settlement.getBuildingManager();
        
        // Create partial building connector list from building connection templates.
        List<PartialBuildingConnector> partialBuildingConnectorList = new ArrayList<PartialBuildingConnector>();
        Iterator<BuildingTemplate> i = buildingTemplates.iterator();
        while (i.hasNext()) {
            BuildingTemplate buildingTemplate = i.next();
            int buildingID = buildingTemplate.getID();
            Building building = buildingManager.getBuilding(buildingID);
            if (building == null) {
                throw new IllegalStateException("Building ID: " + buildingID + " does not exist for settlement " + 
                        settlement.getName());
            }
            
            Iterator<BuildingConnectionTemplate> j = buildingTemplate.getBuildingConnectionTemplates().iterator();
            while (j.hasNext()) {
                BuildingConnectionTemplate connectionTemplate = j.next();
                int connectionID = connectionTemplate.getID();
                Building connectionBuilding = buildingManager.getBuilding(connectionID);
                if (connectionBuilding == null) {
                    throw new IllegalStateException("Building ID: " + connectionID + " does not exist for settlement " + 
                            settlement.getName());
                }
                
                double connectionXLoc = connectionTemplate.getXLocation();
                double connectionYLoc = connectionTemplate.getYLocation();
                Point2D.Double connectionSettlementLoc = LocalAreaUtil.getLocalRelativeLocation(connectionXLoc, 
                        connectionYLoc, building);
                
                double connectionFacing = 0D;
                if (connectionXLoc == (building.getWidth() / 2D)) {
                    connectionFacing = building.getFacing() - 90D;
                }
                else if (connectionXLoc == (building.getWidth() / -2D)) {
                    connectionFacing = building.getFacing() + 90D;
                }
                else if (connectionYLoc == (building.getLength() / 2D)) {
                    connectionFacing = building.getFacing();
                }
                else if (connectionYLoc == (building.getLength() / -2D)) {
                    connectionFacing = building.getFacing() + 180D;
                }
                
                if (connectionFacing < 0D) {
                    connectionFacing += 360D;
                }
                
                if (connectionFacing > 360D) {
                    connectionFacing -= 360D;
                }
                
                PartialBuildingConnector partialConnector = new PartialBuildingConnector(building, 
                        connectionSettlementLoc.getX(), connectionSettlementLoc.getY(), connectionFacing, 
                        connectionBuilding);
                partialBuildingConnectorList.add(partialConnector);
            }
        }
        
        // Match up partial connectors to create building connectors.
        while (partialBuildingConnectorList.size() > 0) {
            PartialBuildingConnector partialConnector = partialBuildingConnectorList.get(0);
            Point2D.Double partialConnectorLoc = new Point2D.Double(partialConnector.xLocation, 
                    partialConnector.yLocation);
            List<PartialBuildingConnector> validPartialConnectors = new ArrayList<PartialBuildingConnector>();
            for (int x = 1; x < partialBuildingConnectorList.size(); x++) {
                PartialBuildingConnector potentialConnector = partialBuildingConnectorList.get(x);
                if (potentialConnector.building.equals(partialConnector.connectToBuilding) && 
                        (potentialConnector.connectToBuilding.equals(partialConnector.building))) {
                    validPartialConnectors.add(potentialConnector);
                }
            }
            
            if (validPartialConnectors.size() > 0) {
                PartialBuildingConnector bestFitConnector = null;
                double closestDistance = Double.MAX_VALUE;
                Iterator<PartialBuildingConnector> j = validPartialConnectors.iterator();
                while (j.hasNext()) {
                    PartialBuildingConnector validConnector = j.next();
                    Point2D.Double validConnectorLoc = new Point2D.Double(validConnector.xLocation, 
                            validConnector.yLocation);
                    double distance = LocalAreaUtil.getDistance(partialConnectorLoc, validConnectorLoc);
                    if (distance < closestDistance) {
                        bestFitConnector = validConnector;
                        closestDistance = distance;
                    }
                }
                
                if (bestFitConnector != null) {
                    
                    BuildingConnector buildingConnector = new BuildingConnector(partialConnector.building, 
                            partialConnector.xLocation, partialConnector.yLocation, partialConnector.facing, 
                            bestFitConnector.building, bestFitConnector.xLocation, bestFitConnector.yLocation, 
                            bestFitConnector.facing);
                    addBuildingConnection(buildingConnector);
                    partialBuildingConnectorList.remove(partialConnector);
                    partialBuildingConnectorList.remove(bestFitConnector);
                }
                else {
                    throw new IllegalStateException("Unable to find building connection for " + 
                            partialConnector.building.getName() + " in " + settlement.getName());
                }
            }
            else {
                throw new IllegalStateException("Unable to find building connection for " + 
                        partialConnector.building.getName() + " in " + settlement.getName());
            }
        }
    }
    
    /**
     * Adds a new building connector.
     * @param buildingConnector new building connector.
     */
    public void addBuildingConnection(BuildingConnector buildingConnector) {
        
        if (!buildingConnections.contains(buildingConnector)) {
            buildingConnections.add(buildingConnector);
        }
        else {
            throw new IllegalArgumentException("BuildingConnector already exists.");
        }
    }
    
    /**
     * Removes an old building connector.
     * @param buildingConnector old building connector.
     */
    public void removeBuildingConnection(BuildingConnector buildingConnector) {
        
        if (buildingConnections.contains(buildingConnector)) {
            buildingConnections.remove(buildingConnector);
        }
        else {
            throw new IllegalArgumentException("BuildingConnector does not exists.");
        }
    }
    
    /**
     * Gets all of the building connections between two buildings.
     * @param building1 the first building.
     * @param building2 the second building.
     * @return a set of building connectors.
     */
    public Set<BuildingConnector> getBuildingConnections(Building building1, Building building2) {
        
        Set<BuildingConnector> result = new HashSet<BuildingConnector>();
        
        Iterator<BuildingConnector> i = buildingConnections.iterator();
        while (i.hasNext()) {
            BuildingConnector connector = i.next();
            if ((building1.equals(connector.getBuilding1()) && building2.equals(connector.getBuilding2())) || 
                    (building1.equals(connector.getBuilding2()) && building2.equals(connector.getBuilding1()))) {
                result.add(connector);
            }
        }
        
        return result;
    }
    
    /**
     * Gets all of the building connections at a settlement.
     * @return set of building connectors.
     */
    public Set<BuildingConnector> getAllBuildingConnections() {
        
        return buildingConnections;
    }
    
    /**
     * Gets all of the connections to a given building.
     * @param building the building.
     * @return a set of building connectors.
     */
    public Set<BuildingConnector> getConnectionsToBuilding(Building building) {
        
        Set<BuildingConnector> result = new HashSet<BuildingConnector>();
        
        Iterator<BuildingConnector> i = buildingConnections.iterator();
        while (i.hasNext()) {
            BuildingConnector connector = i.next();
            if (building.equals(connector.getBuilding1()) || building.equals(connector.getBuilding2())) {
                result.add(connector);
            }
        }
        
        return result;
    }
    
    /**
     * Determines the shortest building path between a starting location and another building.
     * @param startPosition The starting location at the settlement.
     * @param building1 the first building.
     * @param building2 the second building.
     * @return shortest path or null if no path found.
     */
    public BuildingPath determineShortestPath(Point2D.Double startPosition, Building building1, 
            Building building2) {
        
        if (building1.equals(building2)) {
            throw new IllegalArgumentException("building1 and building2 are same building: " + building1.getName());
        }
        
        BuildingPath startingPath = new BuildingPath();
        
        // Add starting point to building path.
        startingPath.addPathPoint(startPosition);
        
        // Check shortest path to target building from this building.
        return determineShortestPath(startingPath, building1, building2);
    }
    
    /**
     * Recursive method to determine the shortest path between two buildings.
     * @param existingPath the current path.
     * @param currentBuilding the current building.
     * @param targetBuilding the target building.
     * @return shortest path or null if none found.
     */
    private BuildingPath determineShortestPath(BuildingPath existingPath, Building currentBuilding, 
            Building targetBuilding) {
        
        BuildingPath result = null;
        
        // Try each building connection from current building.
        Iterator<BuildingConnector> i = getConnectionsToBuilding(currentBuilding).iterator();
        while (i.hasNext()) {
            BuildingConnector connector = i.next();
            
            Building connectionBuilding = null;
            Point2D.Double connectorNearPoint = null;
            Point2D.Double connectorFarPoint = null;
            Hatch hatch1 = connector.getHatch1();
            Hatch hatch2 = connector.getHatch2();
            if (connector.getBuilding1().equals(currentBuilding)) {
                connectionBuilding = connector.getBuilding2();
                connectorNearPoint = new Point2D.Double(hatch1.getXLocation(), hatch1.getYLocation());
                connectorFarPoint = new Point2D.Double(hatch2.getXLocation(), hatch2.getYLocation());
            }
            else {
                connectionBuilding = connector.getBuilding1();
                connectorNearPoint = new Point2D.Double(hatch2.getXLocation(), hatch2.getYLocation());
                connectorFarPoint = new Point2D.Double(hatch1.getXLocation(), hatch1.getYLocation());
            }
            
            Point2D.Double connectionBuildingLoc = new Point2D.Double(connectionBuilding.getXLocation(), 
                    connectionBuilding.getYLocation());
            Point2D.Double connectorCenterLoc = new Point2D.Double(connector.getCenterXLocation(), 
                    connector.getCenterYLocation());
            
            // Make sure building or connection is not already in existing path.
            if (existingPath.containsPathPoint(connectionBuildingLoc) || 
                    existingPath.containsPathPoint(connectorCenterLoc)) {
                continue;
            }
            
            // Copy existing path to create new path.
            BuildingPath newPath = (BuildingPath) existingPath.clone();
            
            // Add building connector to new path.
            if (connector.isSplitConnection()) {
                newPath.addPathPoint(connectorNearPoint);
                newPath.addPathPoint(connectorCenterLoc);
                newPath.addPathPoint(connectorFarPoint);
            }
            else {
                newPath.addPathPoint(connectorCenterLoc);
            }
            
            // Add connection building to new path.
            newPath.addPathPoint(connectionBuildingLoc);
            
            BuildingPath bestPath = null;
            if (connectionBuilding.equals(targetBuilding)) { 
                bestPath = newPath;
            }
            else {
                // Recursively call this method with new path and connection building.
                bestPath = determineShortestPath(newPath, connectionBuilding, targetBuilding);
            }
            
            if (bestPath != null) {
                if ((result == null) || (bestPath.getPathLength() < result.getPathLength())) {
                    result = bestPath;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        settlement = null;
        Iterator<BuildingConnector> i = buildingConnections.iterator();
        while (i.hasNext()) {
            i.next().destroy();
        }
        buildingConnections.clear();
        buildingConnections = null;
    }
    
    /**
     * Inner class for representing a partial building connector.
     */
    private class PartialBuildingConnector {
        
        // Data members.
        private Building building;
        private double xLocation;
        private double yLocation;
        private double facing;
        private Building connectToBuilding;
        
        /**
         * Constructor.
         * @param building the building.
         * @param xLocation the X Location relative to the settlement.
         * @param yLocation the Y location relative to the settlement.
         * @param facing the facing (degrees).
         * @param connectToBuilding the building to connect to.
         */
        PartialBuildingConnector(Building building, double xLocation, double yLocation, 
                double facing, Building connectToBuilding) {
            this.building = building;
            this.xLocation = xLocation;
            this.yLocation = yLocation;
            this.facing = facing;
            this.connectToBuilding = connectToBuilding;
        }
    }
}
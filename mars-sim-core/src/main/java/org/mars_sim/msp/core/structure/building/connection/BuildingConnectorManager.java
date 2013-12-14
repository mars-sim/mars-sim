/**
 * Mars Simulation Project
 * BuildingConnectorManager.java
 * @version 3.06 2013-12-10
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
import java.util.logging.Logger;

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

    private static Logger logger = Logger.getLogger(BuildingConnectorManager.class.getName());
    
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
     * Get the settlement.
     * @return settlement.
     */
    public Settlement getSettlement() {
        return settlement;
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
     * Checks if the settlement contains a given building connector.
     * @param connector the building connector.
     * @return true if settlement contains building connector.
     */
    public boolean containsBuildingConnector(BuildingConnector connector) {
        
        return buildingConnections.contains(connector);
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
     * Checks if there is a valid interior walking path between two buildings.
     * @param building1 the first building.
     * @param building2 the second building.
     * @return true if valid interior walking path.
     */
    public boolean hasValidPath(Building building1, Building building2) {
        
        boolean result = false;
        
        if ((building1 == null) || (building2 == null)) {
            throw new IllegalArgumentException("Building arguments cannot be null");
        }
        
        InsideBuildingPath validPath = determineShortestPath(building1, building1.getXLocation(), 
                building1.getYLocation(), building2, building2.getXLocation(), building2.getYLocation());
        
        if (validPath != null) {
            result = true;
        }
        else {
            logger.fine("Unable to find valid interior walking path between " + building1 + 
                    " and " + building2);
        }
        
        return result;
    }
    
    /**
     * Determines the shortest building path between two locations in buildings.
     * @param building1 the first building.
     * @param building1XLoc the starting X location in the first building.
     * @param building1YLoc the starting Y location in the first building.
     * @param building2 the second building.
     * @param building2XLoc the ending X location in the second building.
     * @param building2YLoc the ending Y location in the second building.
     * @return shortest path or null if no path found.
     */
    public InsideBuildingPath determineShortestPath(Building building1, double building1XLoc, double building1YLoc, 
            Building building2, double building2XLoc, double building2YLoc) {
        
        if ((building1 == null) || (building2 == null)) {
            throw new IllegalArgumentException("Building arguments cannot be null");
        }
        
        BuildingLocation startingLocation = new BuildingLocation(building1, building1XLoc, building1YLoc);
        BuildingLocation endingLocation = new BuildingLocation(building2, building2XLoc, building2YLoc);
        
        InsideBuildingPath startingPath = new InsideBuildingPath();
        startingPath.addPathLocation(startingLocation);
        
        InsideBuildingPath finalPath = null;
        if (!building1.equals(building2)) {
            // Check shortest path to target building from this building.
            finalPath = determineShortestPath(startingPath, building1, building2, endingLocation);
        }
        else {
            finalPath = startingPath;
            finalPath.addPathLocation(endingLocation);
        }
        
        // Iterate path index.
        if (finalPath != null) {
            finalPath.iteratePathLocation();
        }
        
        return finalPath;
    }
    
    /**
     * Recursive method to determine the shortest path between two buildings.
     * @param existingPath the current path.
     * @param currentBuilding the current building.
     * @param targetBuilding the target building.
     * @param endingLocation the end building location.
     * @return shortest path or null if none found.
     */
    private InsideBuildingPath determineShortestPath(InsideBuildingPath existingPath, Building currentBuilding, 
            Building targetBuilding, BuildingLocation endingLocation) {
        
        InsideBuildingPath result = null;
        
        // Try each building connection from current building.
        Iterator<BuildingConnector> i = getConnectionsToBuilding(currentBuilding).iterator();
        while (i.hasNext()) {
            BuildingConnector connector = i.next();
            
            Building connectionBuilding = null;
            Hatch nearHatch = null;
            Hatch farHatch = null;
            if (connector.getBuilding1().equals(currentBuilding)) {
                connectionBuilding = connector.getBuilding2();
                nearHatch = connector.getHatch1();
                farHatch = connector.getHatch2();
            }
            else {
                connectionBuilding = connector.getBuilding1();
                nearHatch = connector.getHatch2();
                farHatch = connector.getHatch1();
            }
            
            // Make sure building or connection is not already in existing path.
            if (existingPath.containsPathLocation(connectionBuilding) || 
                    existingPath.containsPathLocation(connector)) {
                continue;
            }
            
            // Copy existing path to create new path.
            InsideBuildingPath newPath = (InsideBuildingPath) existingPath.clone();
            
            // Add building connector to new path.
            if (connector.isSplitConnection()) {
                newPath.addPathLocation(nearHatch);
                newPath.addPathLocation(connector);
                newPath.addPathLocation(farHatch);
            }
            else {
                newPath.addPathLocation(connector);
            }
            
            InsideBuildingPath bestPath = null;
            if (connectionBuilding.equals(targetBuilding)) { 
                // Add ending location within connection building.
                newPath.addPathLocation(endingLocation);
                bestPath = newPath;
            }
            else {
                // Add connection building to new path.
                newPath.addPathLocation(connectionBuilding);
                
                // Recursively call this method with new path and connection building.
                bestPath = determineShortestPath(newPath, connectionBuilding, targetBuilding, 
                        endingLocation);
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
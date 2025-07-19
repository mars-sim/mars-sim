/*
 * Mars Simulation Project
 * PathFinder.java
 * @date 2025-07-19
 * @author Barry Evans
 */
package com.mars_sim.core.building.connection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.building.Building;

/**
 * This is reponsible for finding a path between two positions in a settlement.
 * It uses a 2 phase approach
 * 1. It finds the best route between two buildings in a settlement.
 * 2. It converts the route into a specific path with connectors and hatches.
 */
public class PathFinder {

    private List<Building> bestRoute = null;
    private Set<Building> visitedBuildings = new HashSet<>();
    private BuildingLocation end;
    private BuildingConnectorManager connectMgr;
    private BuildingLocation start;
    
    public PathFinder(BuildingConnectorManager buildingConnectorManager, BuildingLocation startPosition,
            BuildingLocation endPosition) {
        this.connectMgr = buildingConnectorManager;
        this.end = endPosition;
        this.start = startPosition;

        // If different building find a path otherwise route is simple
        if (endPosition.getBuilding().equals(startPosition.getBuilding())) {
            // If the start and end are the same building, just return the start position
            bestRoute = new ArrayList<>();
            bestRoute.add(startPosition.getBuilding());
            bestRoute.add(endPosition.getBuilding());
        }
        else {
            var route = new ArrayList<Building>();
            visitBuilding(startPosition.getBuilding(), route);
        }
    }

    /**
     * Visit a building on the route search. Steps are:
     * 1. Check if the building has been visited.
     * 2. If it has, check see if the current route is a shortcut for the best route found so far.
     * 3. If it hasn't, add it to the visited list and current route.      
     * 4. If the building is the end building, check if the current route is shorter than the best route found so far.
     * 5. If it is, update the best route.     
     * 7. For each connection out of the current building, visit the other building connected.
     * 8. Remove the current building from the current route.
     * @param current
     * @param currentRoute
     */
    private void visitBuilding(Building current, List<Building> currentRoute) {
        if (visitedBuildings.contains(current)) {
            // Already visited this building
            checkShortCut(current, currentRoute);
            return; 
        }
        else if (bestRoute != null && currentRoute.size() >= bestRoute.size()) {
            // Current route is longer than the best route found so far, so skip it
            return;  
        }

        visitedBuildings.add(current);
        currentRoute.add(current);

        if (current.equals(end.getBuilding())) {
            // Found a valid route
            if (bestRoute == null || (currentRoute.size() < bestRoute.size())) {
                bestRoute = new ArrayList<>(currentRoute);
            }
        }
        else {
            // Potentially optimise the order of connected Buildings according to points on compass
            // between start and end. Would only save time buit seem fast currently
            for (var bc : connectMgr.getConnectionsToBuilding(current)) {
                var b = bc.getOtherBuilding(current);
                visitBuilding(b, currentRoute);
            }
        }
        currentRoute.removeLast();
    }

    /**
     * Check if the current route is a shortcut for the best route found so far.
     * Presence is best route is A,B,C,D,E,F but current partial Route is A,E
     * So this will change the Best route to be A,E,F
     * @param current
     * @param currentRoute
     */
    private void checkShortCut(Building current, List<Building> currentRoute) {
        if (bestRoute != null) {
            int currentInBest = bestRoute.indexOf(current);
            if (currentInBest >= 0 && currentRoute.size() < currentInBest) {
                // Current route is shorter than the best route found so far, so update the best route
                // add the best tail to the current route
                var newBest = new ArrayList<>(currentRoute);
                for(int i = currentInBest; i < bestRoute.size(); i++) {
                    newBest.add(bestRoute.get(i));
                }
                bestRoute = newBest;
            }
        }
    }

    /**
     * Has a valid route between teh  Buildoings been found
     * @return
     */
    public boolean isValidRoute() {
        return bestRoute != null;
    }

    /**
     * This converts an already found Buildoing Route into a specific walking path.
     * @return
     */
    public InsideBuildingPath toPath() {
        if (!isValidRoute()) {
            throw new IllegalStateException("No valid route found.");
        }

        InsideBuildingPath newPath = new InsideBuildingPath();
        newPath.addPathLocation(start);

        // Remove start building off the best route
        var finalRoute = new ArrayList<>(bestRoute);
        var current = finalRoute.remove(0); // Remove the start building

        // Visit each building in the best route to find the connectors and hatches.
        for(var b : finalRoute) {
            if (!b.equals(current)) {
                // Find valid connector between current and next
                final var fixedSeed = current;
                BuildingConnector connector = connectMgr.getConnectionsToBuilding(fixedSeed).stream()
                    .filter(c -> c.getOtherBuilding(fixedSeed).equals(b))
                    .findFirst().orElseThrow(() -> 
                        new IllegalStateException("No connector found between " + fixedSeed.getName() + " -> " + b.getName()));

                // Add building connector to new path with hatches if needed
                if (connector.isSplitConnection()) {
                    boolean isCurrentBuilding1 = connector.getBuilding1().equals(current);
                    newPath.addPathLocation(isCurrentBuilding1 ? connector.getHatch1() : connector.getHatch2());
                    newPath.addPathLocation(connector);
                    newPath.addPathLocation(!isCurrentBuilding1 ? connector.getHatch1() : connector.getHatch2());
                } else {
                    newPath.addPathLocation(connector);
                }
            }

            // Last building needs the end position
            if (!b.equals(end.getBuilding())) {
                newPath.addPathLocation(b);
            }

            current = b; // Update current building
        }

        // Add the explict end position
        newPath.addPathLocation(end);
    
        newPath.iteratePathLocation(); // This feels wrong but all the calling code is based on this
        return newPath;
    }

}

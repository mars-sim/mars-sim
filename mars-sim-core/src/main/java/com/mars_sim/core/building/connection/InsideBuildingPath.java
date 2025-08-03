/*
 * Mars Simulation Project
 * InsideBuildingPath.java
 * @date 2022-09-08
 * @author Scott Davis
 */
package com.mars_sim.core.building.connection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mars_sim.core.map.location.LocalPosition;

/**
 * A path of location navigation objects at a settlement.
 */
public class InsideBuildingPath implements Serializable {

	private static final long serialVersionUID = 1L;
	// Data members.
    private List<InsidePathLocation> pathLocations;
    
    private int nextLocationIndex;
    
    /**
     * Constructor.
     */
    public InsideBuildingPath(List<InsidePathLocation> path) {
        pathLocations = path;
        nextLocationIndex = 1;
    }

    /**
     * Create an instance by taking the base path and replacing the start and end position
     * @param basePath
     * @param startPosition
     * @param endPosition
     */
    public InsideBuildingPath(List<InsidePathLocation> basePath, LocalPosition startPosition,
            LocalPosition endPosition) {
        pathLocations = new ArrayList<>();
        var origStart = (BuildingLocation)basePath.get(0);
        pathLocations.add(new BuildingLocation(origStart.getBuilding(), startPosition));

        int endIndex = basePath.size()-1;

        // Copy over the middle section
        for(int idx = 1; idx < endIndex; idx++) {
            pathLocations.add(basePath.get(idx));
        }
        var origEnd = (BuildingLocation)basePath.get(endIndex);
        pathLocations.add(new BuildingLocation(origEnd.getBuilding(), endPosition));
        nextLocationIndex = 1;
    }


    /**
     * Checks if the building path contains a given location object.
     * 
     * @param point a location object.
     * @return true if path contains the location object.
     */
    public boolean containsPathLocation(InsidePathLocation location) {
        
        return pathLocations.contains(location);
    }
    
    /**
     * Gets the next location object in the path.
     * 
     * @return location location object.
     */
    public InsidePathLocation getNextPathLocation() {
        
        InsidePathLocation result = null;
        if (nextLocationIndex < pathLocations.size()) {
            result = pathLocations.get(nextLocationIndex);
        }
        
        return result;
    }
    
    /**
     * Gets a list of the remaining path locations.
     */
    public List<InsidePathLocation> getRemainingPathLocations() {
        
        List<InsidePathLocation> result = new ArrayList<>();
        for (int x = nextLocationIndex; x < pathLocations.size(); x++) {
            result.add(pathLocations.get(x));
        }
        
        return result;
    }
    
    /**
     * Iterates the next path location object.
     */
    public void iteratePathLocation() {
        
        if (nextLocationIndex < (pathLocations.size() - 1)) {
            nextLocationIndex++;
        }
    }
    
    /**
     * Checks if the next location navigation point is the end of the path.
     * 
     * @return true if end of path.
     */
    public boolean isEndOfPath() {
        
        return (nextLocationIndex == (pathLocations.size() - 1));
    }
    
    /**
     * Gets the total path length.
     * 
     * @return path length (meters).
     */
    public double getPathLength() {      
        double result = 0D;
        
        if (!pathLocations.isEmpty()) {
            InsidePathLocation previousLocation = pathLocations.get(0);
            Iterator<InsidePathLocation> i = pathLocations.iterator();
            while (i.hasNext()) {
                InsidePathLocation location = i.next();
                result += previousLocation.getPosition().getDistanceTo(location.getPosition());
                previousLocation = location;
            }
        }
        
        return result;
    }
    
    public List<InsidePathLocation> getPathLocations() {
    	return pathLocations;
    }
}

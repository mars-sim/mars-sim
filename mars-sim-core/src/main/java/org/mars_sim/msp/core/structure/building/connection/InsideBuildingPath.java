/*
 * Mars Simulation Project
 * InsideBuildingPath.java
 * @date 2022-09-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.connection;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A path of location navigation objects at a settlement.
 */
public class InsideBuildingPath implements Serializable {

	private static final long serialVersionUID = 1L;
	// Data members.
    private List<InsidePathLocation> pathLocations;
    
    private int nextLocationIndex;
    
    /**
     * Constructor
     */
    public InsideBuildingPath() {
        pathLocations = new CopyOnWriteArrayList<>();
        nextLocationIndex = 0;
    }
    
    /**
     * Copy Constructor.
     */
    public InsideBuildingPath(InsideBuildingPath insideBuildingPath) {
    	this();
    	
        Iterator<InsidePathLocation> i = insideBuildingPath.getPathLocations().iterator();
        while (i.hasNext()) {
        	addPathLocation(i.next());
        }
        
        nextLocationIndex = insideBuildingPath.getNextLocationIndex();
    }
    
    /**
     * Adds a new location object to the path.
     * 
     * @param newLocation new location object.
     */
    public void addPathLocation(InsidePathLocation newLocation){
        if (!pathLocations.contains(newLocation)) {
            pathLocations.add(newLocation);
        }
        else {
            throw new IllegalArgumentException("Path already includes new location object");
        }
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
        
//        int size = pathLocations.size() - nextLocationIndex;
        List<InsidePathLocation> result = new CopyOnWriteArrayList<>();
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
        
        if (pathLocations.size() > 0) {
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
    
    public int getNextLocationIndex() {
    	return nextLocationIndex;
    }
    
    /**
     * Destroys object.
     */
    public void destroy() {
        pathLocations.clear();
        pathLocations = null;
    }
}

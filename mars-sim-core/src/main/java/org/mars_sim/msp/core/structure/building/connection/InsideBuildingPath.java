/**
 * Mars Simulation Project
 * BuildingPath.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.connection;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A path of location navigation objects at a settlement.
 */
public class InsideBuildingPath implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	// Data members.
    private List<InsidePathLocation> pathLocations;
    private int nextLocationIndex;
    
    /**
     * Constructor
     */
    public InsideBuildingPath() {
        pathLocations = new ArrayList<InsidePathLocation>();
        nextLocationIndex = 0;
    }
    
    /**
     * Add a new location object to the path.
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
     * @param point a location object.
     * @return true if path contains the location object.
     */
    public boolean containsPathLocation(InsidePathLocation location) {
        
        return pathLocations.contains(location);
    }
    
    /**
     * Gets the next location object in the path.
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
        
        int size = pathLocations.size() - nextLocationIndex;
        List<InsidePathLocation> result = new ArrayList<InsidePathLocation>(size);
        for (int x = nextLocationIndex; x < pathLocations.size(); x++) {
            result.add(pathLocations.get(x));
        }
        
        return result;
    }
    
    /**
     * Iterate the next path location object.
     */
    public void iteratePathLocation() {
        
        if (nextLocationIndex < (pathLocations.size() - 1)) {
            nextLocationIndex++;
        }
    }
    
    /**
     * Checks if the next location navigation point is the end of the path.
     * @return true if end of path.
     */
    public boolean isEndOfPath() {
        
        return (nextLocationIndex == (pathLocations.size() - 1));
    }
    
    /**
     * Gets the total path length.
     * @return path length (meters).
     */
    public double getPathLength() {
        
        double result = 0D;
        
        if (pathLocations.size() > 0) {
            InsidePathLocation previousLocation = pathLocations.get(0);
            Iterator<InsidePathLocation> i = pathLocations.iterator();
            while (i.hasNext()) {
                InsidePathLocation location = i.next();
                result += Point2D.Double.distance(previousLocation.getXLocation(), 
                        previousLocation.getYLocation(), location.getXLocation(), 
                        location.getYLocation());
                previousLocation = location;
            }
        }
        
        return result;
    }
    
    @Override
    public Object clone() {
        
        InsideBuildingPath result = new InsideBuildingPath();
        
        Iterator<InsidePathLocation> i = pathLocations.iterator();
        while (i.hasNext()) {
            result.addPathLocation(i.next());
        }
        
        result.nextLocationIndex = nextLocationIndex;
        
        return result;
    }
    
    /**
     * Destroy object.
     */
    public void destroy() {
        pathLocations.clear();
        pathLocations = null;
    }
}
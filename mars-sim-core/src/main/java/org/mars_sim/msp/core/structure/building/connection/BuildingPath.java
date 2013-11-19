/**
 * Mars Simulation Project
 * BuildingPath.java
 * @version 3.06 2013-11-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.connection;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.LocalAreaUtil;

/**
 * A path of location navigation points at a settlement.
 */
public class BuildingPath implements Serializable, Cloneable {

    // Data members.
    private List<Point2D.Double> pathPoints;
    private int nextPointIndex;
    
    /**
     * Constructor
     */
    public BuildingPath() {
        pathPoints = new ArrayList<Point2D.Double>();
        nextPointIndex = 0;
    }
    
    /**
     * Add a new location nav point to the path.
     * @param newPoint new location nav point.
     */
    public void addPathPoint(Point2D.Double newPoint){
        if (!pathPoints.contains(newPoint)) {
            pathPoints.add(newPoint);
        }
        else {
            throw new IllegalArgumentException("Path already includes newPoint");
        }
    }
    
    /**
     * Checks if the building path contains a given location navigation point.
     * @param point a location navigation point.
     * @return true if path contains the point.
     */
    public boolean containsPathPoint(Point2D.Double point) {
        
        return pathPoints.contains(point);
    }
    
    /**
     * Gets the next navigation location point in the path.
     * @return location navigation point.
     */
    public Point2D.Double getNextPathPoint() {
        
        Point2D.Double result = null;
        if (nextPointIndex < pathPoints.size()) {
            result = pathPoints.get(nextPointIndex);
        }
        
        return result;
    }
    
    /**
     * Iterate the next location navigation point.
     */
    public void iteratePathPoint() {
        
        nextPointIndex++;
    }
    
    /**
     * Checks if the next location navigation point is the end of the path.
     * @return true if end of path.
     */
    public boolean isEndOfPath() {
        
        return (nextPointIndex >= pathPoints.size());
    }
    
    /**
     * Gets the total path length.
     * @return path length (meters).
     */
    public double getPathLength() {
        
        double result = 0D;
        
        if (pathPoints.size() > 0) {
            Point2D.Double previousPoint = pathPoints.get(0);
            Iterator<Point2D.Double> i = pathPoints.iterator();
            while (i.hasNext()) {
                Point2D.Double point = i.next();
                result += LocalAreaUtil.getDistance(previousPoint, point);
                previousPoint = point;
            }
        }
        
        return result;
    }
    
    @Override
    public Object clone() {
        
        BuildingPath result = new BuildingPath();
        
        Iterator<Point2D.Double> i = pathPoints.iterator();
        while (i.hasNext()) {
            result.addPathPoint(i.next());
        }
        
        result.nextPointIndex = nextPointIndex;
        
        return result;
    }
}
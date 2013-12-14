/**
 * Mars Simulation Project
 * LocalAreaUtil.java
 * @version 3.04 2013-02-05
 * @author Scott Davis
 */

package org.mars_sim.msp.core;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A utility class for calculating locations in a local area with a center point, 
 * such as around a settlement or rover.
 */
public class LocalAreaUtil {

    /** Distance from edge of boundary when determining internal locations. */
    private static final double INNER_BOUNDARY_DISTANCE = 1.5D;
    
    /**
     * Private empty constructor for utility class.
     */
    private LocalAreaUtil() {
        // Do nothing
    }
    
    /**
     * Gets a local relative location from a location relative to this bounded object.
     * @param xLoc the X location relative to this bounded object.
     * @param yLoc the Y location relative to this bounded object.
     * @param boundedObject the local bounded object.
     * @return Point containing the X and Y locations relative to the local area's center point.
     */
    public static Point2D.Double getLocalRelativeLocation(double xLoc, double yLoc, LocalBoundedObject boundedObject) {
        Point2D.Double result = new Point2D.Double();
        
        double radianRotation = (boundedObject.getFacing() * (Math.PI / 180D));
        double rotateX = (xLoc * Math.cos(radianRotation)) - (yLoc * Math.sin(radianRotation));
        double rotateY = (xLoc * Math.sin(radianRotation)) + (yLoc * Math.cos(radianRotation));
        
        double translateX = rotateX + boundedObject.getXLocation();
        double translateY = rotateY + boundedObject.getYLocation();
        
        result.setLocation(translateX, translateY);
        
        return result;
    }
    
    /**
     * Gets a random location inside a local bounded object.
     * @param boundedObject the local bounded object.
     * @return random X/Y location relative to the center of the bounded object.
     */
    public static Point2D.Double getRandomInteriorLocation(LocalBoundedObject boundedObject) {
        
        Point2D.Double result = new Point2D.Double(0D, 0D);
        
        double xRange = boundedObject.getWidth() - (INNER_BOUNDARY_DISTANCE * 2D);
        if (xRange > 0D) {
            result.x = RandomUtil.getRandomDouble(xRange) - (xRange / 2D);
        }
        else {
            result.x = 0D;
        }
        
        double yRange = boundedObject.getLength() - (INNER_BOUNDARY_DISTANCE * 2D);
        if (yRange > 0D) {
            result.y = RandomUtil.getRandomDouble(yRange) - (yRange / 2D);
        }
        else {
            result.y = 0D;
        }
        
        return result;
    }
    
    /**
     * Gets a random location outside a local bounded object at a given distance away.
     * @param boundedObject the local bounded object.
     * @param distance the distance away from the object.
     * @return random X/Y location relative to the center of the bounded object.
     */
    public static Point2D.Double getRandomExteriorLocation(LocalBoundedObject boundedObject, double distance) {
        
        Point2D.Double result = new Point2D.Double(0D, 0D);
        
        int side = RandomUtil.getRandomInt(3);
        
        switch (side) {
            // Front side.
            case 0: result.x = RandomUtil.getRandomDouble(boundedObject.getWidth() + (distance * 2D)) - 
                            ((boundedObject.getWidth() / 2D) + distance);
                    result.y = (boundedObject.getLength() / 2D) + distance;
                    break;
            
            // Back side.
            case 1: result.x = RandomUtil.getRandomDouble(boundedObject.getWidth() + (distance * 2D)) - 
                            (boundedObject.getWidth() + distance);
                    result.y = (boundedObject.getLength() / -2D) - distance;
                    break;
                   
            // Left side.
            case 2: result.x = (boundedObject.getWidth() / 2D) + distance;
                    result.y = RandomUtil.getRandomDouble(boundedObject.getLength() + (distance * 2D)) - 
                            ((boundedObject.getLength() / 2D) + distance);
                    break;
                    
            // Right side.
            case 3: result.x = (boundedObject.getWidth() / -2D) - distance;
                    result.y = RandomUtil.getRandomDouble(boundedObject.getLength() + (distance * 2D)) - 
                            ((boundedObject.getLength() / 2D) + distance);
        }
        
        return result;
    }
    
    /**
     * Checks if a point location does not collide with any existing vehicle, building, 
     * or construction site.
     * @param xLoc the new X location.
     * @param yLoc the new Y location.
     * @param coordinates the global coordinate location to check.
     * @return true if location doesn't collide with anything.
     */
    public static boolean checkLocationCollision(double xLoc, double yLoc, Coordinates coordinates) {
        
        boolean result = true;
        
        // Check all vehicles at location.
        Iterator<Vehicle> i = Simulation.instance().getUnitManager().getVehicles().iterator();
        while (i.hasNext() && result) {
            Vehicle vehicle = i.next();
            if (vehicle.getCoordinates().equals(coordinates)) {
                if (checkLocationWithinLocalBoundedObject(xLoc, yLoc, vehicle)) {
                    result = false;
                }
            }
        }
        
        // Check all settlements at coordinates.
        Iterator<Settlement> l = Simulation.instance().getUnitManager().getSettlements().iterator();
        while (l.hasNext() && result) {
            Settlement settlement = l.next();    
            if (settlement.getCoordinates().equals(coordinates)) {
                
                // Check all buildings at settlement.
                Iterator<Building> j = settlement.getBuildingManager().getBuildings().iterator();
                while (j.hasNext() && result) {
                    Building building = j.next();
                    if (checkLocationWithinLocalBoundedObject(xLoc, yLoc, building)) {
                        result = false;
                    }
                }

                // Check all construction sites at settlement.
                Iterator<ConstructionSite> k = settlement.getConstructionManager().
                        getConstructionSites().iterator();
                while (k.hasNext() && result) {
                    ConstructionSite site = k.next();
                    if (checkLocationWithinLocalBoundedObject(xLoc, yLoc, site)) {
                        result = false;
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Checks if a point location is within a local bounded object's bounds.
     * @param xLoc the new X location.
     * @param yLoc the new Y location.
     * @param object the local bounded object.
     * @return true if location is within object bounds.
     */
    public static boolean checkLocationWithinLocalBoundedObject(double xLoc, double yLoc, 
            LocalBoundedObject object) {
        boolean result = false;
        
        Rectangle2D rect = new Rectangle2D.Double(object.getXLocation() - 
                (object.getWidth() / 2D), object.getYLocation() - 
                (object.getLength() / 2D), object.getWidth(), object.getLength());
        Path2D path = getPathFromRectangleRotation(rect, 
                object.getFacing());
        Area area = new Area(path);
        if (area.contains(xLoc, yLoc)) {
            result = true;
        }
        
        return result;
    }
    
    /**
     * Checks if a bounded object can move to a given new location and facing without colliding 
     * with any existing vehicle, building, or construction site.
     * @param boundedObject the boundedObject to be moved.
     * @param newXLoc the new X location.
     * @param newYLoc the new Y location.
     * @param newFacing the new facing (degrees clockwise from North).
     * @param settlement the settlement the bounded object is located at.
     * @return true if new location and facing for bounded object doesn't collide with anything.
     */
    public static boolean checkBoundedObjectNewLocationCollision(LocalBoundedObject boundedObject, 
            double newXLoc, double newYLoc, double newFacing, Settlement settlement) {
        
        boolean result = true;
        
        // Create path for proposed new bounded object position.
        Rectangle2D newObjectRect = new Rectangle2D.Double(newXLoc - (boundedObject.getWidth() / 2D), 
                newYLoc - (boundedObject.getLength() / 2D), boundedObject.getWidth(), 
                boundedObject.getLength());
        Path2D newObjectPath = getPathFromRectangleRotation(newObjectRect, newFacing);
        
        // Check all parked vehicles at settlement.
        Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
        while (i.hasNext() && result) {
            Vehicle vehicle = i.next();
            if (vehicle != boundedObject) {
                if (checkPathLocalBoundedObjectCollision(newObjectPath, vehicle)) {
                    result = false;
                }
            }
        }
        
        // Check all buildings at settlement.
        Iterator<Building> j = settlement.getBuildingManager().getBuildings().iterator();
        while (j.hasNext() && result) {
            Building building = j.next();
            if (building != boundedObject) {
                if (checkPathLocalBoundedObjectCollision(newObjectPath, building)) {
                    result = false;
                }
            }
        }
        
        // Check all construction sites at settlement.
        Iterator<ConstructionSite> k = settlement.getConstructionManager().getConstructionSites().iterator();
        while (k.hasNext() && result) {
            ConstructionSite site = k.next();
            if (site != boundedObject) {
                if (checkPathLocalBoundedObjectCollision(newObjectPath, site)) {
                    result = false;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Checks if a path collides with a local bounded object.
     * @param path the path.
     * @param object the local bounded object.
     * @return true if the path and the local bounded object collide.
     */
    private static boolean checkPathLocalBoundedObjectCollision(Path2D path, LocalBoundedObject object) {
        boolean result = false;
        
        Rectangle2D objectRect = new Rectangle2D.Double(object.getXLocation() - 
                (object.getWidth() / 2D), object.getYLocation() - 
                (object.getLength() / 2D), object.getWidth(), object.getLength());
        Path2D objectPath = getPathFromRectangleRotation(objectRect, 
                object.getFacing());
        Area area = new Area(path);
        area.intersect(new Area(objectPath));
        if (!area.isEmpty()) {
            result = true;
        }
        
        return result;
    }
    
    /**
     * Creates a Path2D object from a rectangle with a given rotation.
     * @param rectangle the rectangle.
     * @param rotation the rotation (degrees clockwise from North).
     * @return path representing rotated rectangle.
     */
    private static Path2D getPathFromRectangleRotation(Rectangle2D rectangle, double rotation) {
        double radianRotation = rotation * (Math.PI / 180D);
        AffineTransform at = AffineTransform.getRotateInstance(radianRotation, rectangle.getCenterX(), 
                rectangle.getCenterY());
        return new Path2D.Double(rectangle, at);
    }
    
    /**
     * Gets the distance between two points.
     * @param point1 the first point.
     * @param point2 the second point.
     * @return distance (meters).
     */
    public static double getDistance(Point2D.Double point1, Point2D.Double point2) {
        
        return Point2D.Double.distance(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }
}
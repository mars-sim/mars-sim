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
     * @param settlement the settlement the bounded object is located at.
     * @return true if location doesn't collide with anything.
     */
    public static boolean checkLocationCollision(double xLoc, double yLoc, Settlement settlement) {
        
        boolean result = true;
        
        // Check all parked vehicles at settlement.
        Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
        while (i.hasNext() && result) {
            Vehicle vehicle = i.next();
            Rectangle2D vehicleRect = new Rectangle2D.Double(vehicle.getXLocation() - 
                    (vehicle.getWidth() / 2D), vehicle.getYLocation() - (vehicle.getLength() / 2D), 
                    vehicle.getWidth(), vehicle.getLength());
            Path2D vehiclePath = getPathFromRectangleRotation(vehicleRect, vehicle.getFacing());
            Area area = new Area(vehiclePath);
            if (area.contains(xLoc, yLoc)) {
                result = false;
            }
        }
        
        // Check all buildings at settlement.
        Iterator<Building> j = settlement.getBuildingManager().getBuildings().iterator();
        while (j.hasNext() && result) {
            Building building = j.next();
            Rectangle2D buildingRect = new Rectangle2D.Double(building.getXLocation() - 
                    (building.getWidth() / 2D), building.getYLocation() - 
                    (building.getLength() / 2D), building.getWidth(), building.getLength());
            Path2D buildingPath = getPathFromRectangleRotation(buildingRect, 
                    building.getFacing());
            Area area = new Area(buildingPath);
            if (area.contains(xLoc, yLoc)) {
                result = false;
            }
        }
        
        // Check all construction sites at settlement.
        Iterator<ConstructionSite> k = settlement.getConstructionManager().getConstructionSites().iterator();
        while (k.hasNext() && result) {
            ConstructionSite site = k.next();
            Rectangle2D siteRect = new Rectangle2D.Double(site.getXLocation() - 
                    (site.getWidth() / 2D), site.getYLocation() - 
                    (site.getLength() / 2D), site.getWidth(), site.getLength());
            Path2D sitePath = getPathFromRectangleRotation(siteRect, 
                    site.getFacing());
            Area area = new Area(sitePath);
            if (area.contains(xLoc, yLoc)) {
                result = false;
            }
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
                Rectangle2D tempVehicleRect = new Rectangle2D.Double(vehicle.getXLocation() - 
                        (vehicle.getWidth() / 2D), vehicle.getYLocation() - (vehicle.getLength() / 2D), 
                        vehicle.getWidth(), vehicle.getLength());
                Path2D tempVehiclePath = getPathFromRectangleRotation(tempVehicleRect, vehicle.getFacing());
                Area area = new Area(newObjectPath);
                area.intersect(new Area(tempVehiclePath));
                if (!area.isEmpty()) {
                    result = false;
                }
            }
        }
        
        // Check all buildings at settlement.
        Iterator<Building> j = settlement.getBuildingManager().getBuildings().iterator();
        while (j.hasNext() && result) {
            Building building = j.next();
            if (building != boundedObject) {
                Rectangle2D buildingRect = new Rectangle2D.Double(building.getXLocation() - 
                        (building.getWidth() / 2D), building.getYLocation() - 
                        (building.getLength() / 2D), building.getWidth(), building.getLength());
                Path2D buildingPath = getPathFromRectangleRotation(buildingRect, 
                        building.getFacing());
                Area area = new Area(newObjectPath);
                area.intersect(new Area(buildingPath));
                if (!area.isEmpty()) {
                    result = false;
                }
            }
        }
        
        // Check all construction sites at settlement.
        Iterator<ConstructionSite> k = settlement.getConstructionManager().getConstructionSites().iterator();
        while (k.hasNext() && result) {
            ConstructionSite site = k.next();
            if (site != boundedObject) {
                Rectangle2D siteRect = new Rectangle2D.Double(site.getXLocation() - 
                        (site.getWidth() / 2D), site.getYLocation() - 
                        (site.getLength() / 2D), site.getWidth(), site.getLength());
                Path2D sitePath = getPathFromRectangleRotation(siteRect, 
                        site.getFacing());
                Area area = new Area(newObjectPath);
                area.intersect(new Area(sitePath));
                if (!area.isEmpty()) {
                    result = false;
                }
            }
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
}
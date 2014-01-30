/**
 * Mars Simulation Project
 * LocalAreaUtil.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
    
    /** Cache for total area containing obstacles for a given coordinate location. */
    private static final Map<Coordinates, Area> obstacleAreaCache = new HashMap<Coordinates, Area>();
    
    /** Time stamps for obstacle area cache. */
    private static final Map<Coordinates, String> obstacleAreaTimestamps = new HashMap<Coordinates, String>();
    
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
        
        Iterator<LocalBoundedObject> i = getAllLocalBoundedObjectsAtLocation(coordinates).iterator();
        while (i.hasNext() && result) {
            LocalBoundedObject object = i.next();
            if (checkLocationWithinLocalBoundedObject(xLoc, yLoc, object)) {
                result = false;
            }
        }
        
        return result;
    }
    
    /**
     * Gets a set of local bounded objects at a given coordinate location.
     * @param coordinates the coordinate location.
     * @return set of local bounded objects at location (may be empty).
     */
    public static Set<LocalBoundedObject> getAllLocalBoundedObjectsAtLocation(Coordinates coordinates) {
        
        Set<LocalBoundedObject> result = new HashSet<LocalBoundedObject>();
        
        // Add all vehicles at location.
        Iterator<Vehicle> i = Simulation.instance().getUnitManager().getVehicles().iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();
            if (vehicle.getCoordinates().equals(coordinates)) {
                result.add(vehicle);
            }
        }
        
        // Check for any settlements at coordinates.
        Iterator<Settlement> l = Simulation.instance().getUnitManager().getSettlements().iterator();
        while (l.hasNext()) {
            Settlement settlement = l.next();    
            if (settlement.getCoordinates().equals(coordinates)) {
                
                // Add all buildings at settlement.
                Iterator<Building> j = settlement.getBuildingManager().getBuildings().iterator();
                while (j.hasNext()) {
                    result.add(j.next());
                }

                // Check all construction sites at settlement.
                Iterator<ConstructionSite> k = settlement.getConstructionManager().
                        getConstructionSites().iterator();
                while (k.hasNext()) {
                    result.add(k.next());
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
        Path2D path = getPathFromRectangleRotation(rect, object.getFacing());
        Area area = new Area(path);
        if (area.contains(xLoc, yLoc)) {
            result = true;
        }
        
        return result;
    }
    
    /**
     * Gets the bounding rectangle around a local bounded object with facing.
     * @param object the local bounded object.
     * @return bounding rectangle.
     */
    public static Rectangle2D getBoundingRectangle(LocalBoundedObject object) {
        
        Rectangle2D rect = new Rectangle2D.Double(object.getXLocation() - 
                (object.getWidth() / 2D), object.getYLocation() - 
                (object.getLength() / 2D), object.getWidth(), object.getLength());
        Path2D path = getPathFromRectangleRotation(rect, object.getFacing());
        
        return path.getBounds2D();
    }
    
    /**
     * Checks if a bounded object can move to a given new location and facing without colliding 
     * with any existing vehicle, building, or construction site.
     * @param boundedObject the boundedObject to be moved.
     * @param newXLoc the new X location.
     * @param newYLoc the new Y location.
     * @param newFacing the new facing (degrees clockwise from North).
     * @param coordinates the global coordinate location to check.
     * @return true if new location and facing for bounded object doesn't collide with anything.
     */
    public static boolean checkBoundedObjectNewLocationCollision(
            LocalBoundedObject boundedObject, double newXLoc, double newYLoc, double newFacing, 
            Coordinates coordinates) {
        
        boolean result = true;
        
        result = checkObjectCollision(boundedObject, boundedObject.getWidth(), 
                boundedObject.getLength(), newXLoc, newYLoc, newFacing, coordinates);
        
        return result;
    }
    
    /**
     * Checks if an object with a given position, facing, and dimensions collides  
     * with any existing vehicle, building, or construction site at a settlement.
     * @param object the boundedObject to be moved.
     * @param width the object's width.
     * @param length the object's length.
     * @param xLoc the object's X location.
     * @param yLoc the object's Y location.
     * @param facing the object's facing (degrees clockwise from North).
     * @param coordinates the global coordinate location to check.
     * @return true if object doesn't collide with anything.
     */
    public static boolean checkObjectCollision(Object object, double width, double length, 
            double xLoc, double yLoc, double facing, Coordinates coordinates) {
        
        boolean result = true;
        
        // Create path for object.
        Rectangle2D objectRect = new Rectangle2D.Double(xLoc - (width / 2D), 
                yLoc - (length / 2D), width, length);
        Path2D objectPath = getPathFromRectangleRotation(objectRect, facing);
        
        result = checkPathCollision(object, objectPath, coordinates, false);
        
        return result;
    }
    
    /**
     * Checks if a line path collides with any existing vehicle, building, or 
     * construction site at a settlement.
     * @param startXLoc the line path start X location.
     * @param startYLoc the line path start Y location.
     * @param endXLoc the line path end X location.
     * @param endYLoc the line path end Y location.
     * @param coordinates the global coordinate location to check.
     * @return true if line path doesn't collide with anything.
     */
    public static boolean checkLinePathCollision(double startXLoc, double startYLoc, 
            double endXLoc, double endYLoc, Coordinates coordinates) {
        
        boolean result = true;
        
        // Create line.
        Line2D line = new Line2D.Double(startXLoc, startYLoc, endXLoc, endYLoc);
        Path2D linePath = createLinePath(line);
        
        result = checkPathCollision(null, linePath, coordinates, true);
        
        return result;
    }
    
    /**
     * Create a thin (1 mm wide) rectangle path representing a line.
     * @param line the line.
     * @return rectangle path for the line.
     */
    private static Path2D createLinePath(Line2D line) {
        
        // Make rectangle width 1mm.
        double width = .001D;
        double length = line.getP1().distance(line.getP2());
        double centerX = (line.getX1() + line.getX2()) / 2D;
        double centerY = (line.getY1() + line.getY2()) / 2D;
        
        double x1 = centerX - (width / 2D);
        double y1 = centerY - (length / 2D);
        Rectangle2D lineRect = new Rectangle2D.Double(x1, y1, width, length);
        
        double facing = getDirection(line.getP1(), line.getP2());
        
        Path2D rectPath = getPathFromRectangleRotation(lineRect, facing);
        
        return rectPath;
    }
    
    /**
     * Gets the direction from point1 to point2.
     * @param point1 the first point.
     * @param point2 the second point.
     * @return direction in degrees clockwise from North.
     */
    private static double getDirection(Point2D point1, Point2D point2) {
        
        double radDir = Math.atan2(point1.getX() - point2.getX(), point2.getY() - point1.getY());
        
        while (radDir > (Math.PI * 2D)) {
            radDir -= (Math.PI * 2D);
        }
        
        while (radDir < 0D) {
            radDir += (Math.PI * 2D);
        }
        
        double degreeDir = radDir * (180D / Math.PI);
        
        return degreeDir;
    }
    
    /**
     * Checks if a path collides with an existing building, construction site, or vehicle at a location.
     * @param object the object being checked (may be null if no object).
     * @param path the path to check.
     * @param coordinates the global coordinate location to check.
     * @return true if path doesn't collide with anything.
     */
    private static boolean checkPathCollision(Object object, Path2D path, Coordinates coordinates, boolean useCache) {
        
        boolean result = true;
        
        // Check if obstacle area has been cached for this coordinate location if using cache.
        boolean cached = false;
        Area obstacleArea = null;
        if (useCache && obstacleAreaCache.containsKey(coordinates)) {
            String currentTimestamp = Simulation.instance().getMasterClock().getMarsClock().getTimeStamp();
            String cachedTimestamp = obstacleAreaTimestamps.get(coordinates);
            if (currentTimestamp.equals(cachedTimestamp)) {
                cached = true;
                obstacleArea = obstacleAreaCache.get(coordinates);
            }
        }
        
        if (!cached) {
            // Add all obstacle areas at location together to create a total obstacle area.
            Iterator<LocalBoundedObject> i = getAllLocalBoundedObjectsAtLocation(coordinates).iterator();
            while (i.hasNext()) {
                LocalBoundedObject lbo = i.next();
                if (lbo != object) {
                    Rectangle2D objectRect = new Rectangle2D.Double(lbo.getXLocation() - 
                            (lbo.getWidth() / 2D), lbo.getYLocation() - 
                            (lbo.getLength() / 2D), lbo.getWidth(), lbo.getLength());
                    Path2D objectPath = getPathFromRectangleRotation(objectRect, 
                            lbo.getFacing());
                    Area objectArea = new Area(objectPath);
                    if (obstacleArea == null) {
                        obstacleArea = objectArea;
                    }
                    else {
                        obstacleArea.add(objectArea);
                    }
                }
            }
        }
        
        if (obstacleArea != null) {
            // Check for intersection of obstacle and path bounding rectangles first (faster).
            Rectangle2D pathBounds = path.getBounds2D();
            Rectangle2D obstacleBounds = obstacleArea.getBounds2D();
            if (pathBounds.intersects(obstacleBounds)) {
                // If rectangles intersect, check for collision of path and obstacle areas (slower).
                Area pathArea = new Area(path);
                result = !doAreasCollide(pathArea, obstacleArea);
            }
        }
        
        // Store cached obstacle area for location with current timestamp if needed.
        if (useCache && !cached && (obstacleArea != null)) {
            obstacleAreaCache.put(coordinates, obstacleArea);
            String currentTimestamp = Simulation.instance().getMasterClock().getMarsClock().getTimeStamp();
            obstacleAreaTimestamps.put(coordinates, currentTimestamp);
        }
        
        return result;
    }
    
    /**
     * Clear the obstacle area cache and time stamps.
     */
    public static void clearObstacleCache() {
        if (obstacleAreaCache != null) {
            obstacleAreaCache.clear();
        }
        if (obstacleAreaTimestamps != null) {
            obstacleAreaTimestamps.clear();
        }
    }
    
    /**
     * Checks if two areas collide.
     * @param area1 the first area.
     * @param area2 the second area.
     * @return true if areas collide.
     */
    private static boolean doAreasCollide(Area area1, Area area2) {
        
        Area collide = new Area(area1);
        collide.intersect(area2);
        return !collide.isEmpty();
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
/**
 * Mars Simulation Project
 * GroundVehicle.java
 * @version 3.04 2012-12-05
 * @author Scott Davis
 */

package org.mars_sim.msp.core.vehicle;

import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.TerrainElevation;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.GroundVehicleMaintenance;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** The GroundVehicle class represents a ground-type vehicle.  It is
 *  abstract and should be extended to a particular type of ground
 *  vehicle.
*/
public abstract class GroundVehicle extends Vehicle implements Serializable {

    // Ground Vehicle Status Strings
    public final static String STUCK = "Stuck - using winch";
	
    // Data members
    private double elevation; // Current elevation in km.
    private double terrainHandlingCapability; // Ground vehicle's basic terrain handling capability.
    private boolean isStuck; // True if vehicle is stuck.
    
    /** 
     * Constructs a GroundVehicle object at a given settlement
     * @param name name of the ground vehicle
     * @param description the configuration description of the vehicle.
     * @param settlement settlement the ground vehicle is parked at
     * @throws an exception if ground vehicle could not be constructed.
     */
    GroundVehicle(String name, String description, Settlement settlement) {
        // use Vehicle constructor
        super(name, description, settlement);

        // Add scope to malfunction manager.
        malfunctionManager.addScopeString("GroundVehicle");
	    
        setTerrainHandlingCapability(0D); // Default terrain capability
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        elevation = surface.getSurfaceTerrain().getElevation(getCoordinates());
    }

    /** Returns vehicle's current status
     *  @return the vehicle's current status
     */
    public String getStatus() {
        String status = null;

        if (isStuck) status = STUCK;
        else status = super.getStatus();

        return status;
    }
    
    /** Returns the elevation of the vehicle in km. 
     *  @return elevation of the ground vehicle (in km)
     */
    public double getElevation() {
        return elevation;
    }

    /** Sets the elevation of the vehicle (in km.) 
     *  @param elevation new elevation for ground vehicle
     */
    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    /** Returns the vehicle's terrain capability 
     *  @return terrain handling capability of the ground vehicle
     */
    public double getTerrainHandlingCapability() {
        return terrainHandlingCapability;
    }

    /** Sets the vehicle's terrain capability 
     *  @param c sets the ground vehicle's terrain handling capability
     */
    public void setTerrainHandlingCapability(double c) {
        terrainHandlingCapability = c;
    }

    /** 
     * Gets the average angle of terrain over next 7.4km distance in direction vehicle is traveling.
     * @return ground vehicle's current terrain grade angle from horizontal (radians)
     */
    public double getTerrainGrade() {
    	return getTerrainGrade(getDirection());
    }
    
    /** 
     * Gets the average angle of terrain over next 7.4km distance in a given direction from the vehicle.
     * @return ground vehicle's current terrain grade angle from horizontal (radians)
     */
    public double getTerrainGrade(Direction direction) {
        // Determine the terrain grade in a given direction from the vehicle.
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        TerrainElevation terrain = surface.getSurfaceTerrain();
        return terrain.determineTerrainDifficulty(getCoordinates(), direction);
    }

    /** Returns true if ground vehicle is stuck 
     *  @return true if vehicle is currently stuck, false otherwise
     */
    public boolean isStuck() {
        return isStuck;
    }

    /** Sets the ground vehicle's stuck value 
     *  @param stuck true if vehicle is currently stuck, false otherwise
     */
    public void setStuck(boolean stuck) {
        isStuck = stuck;
        if (isStuck) setSpeed(0D);
    }
    
    /**
     * Gets the driver of the ground vehicle.
     * @return the vehicle driver.
     */
    public VehicleOperator getDriver() {
    	return getOperator();
    }
    
    /**
     * Sets the driver of the ground vehicle.
     * @param operator the driver
     */
    public void setDriver(VehicleOperator operator) {
    	setOperator(operator);
    }
    
    @Override
    public void determinedSettlementParkedLocationAndFacing() {
    	
    	Settlement settlement = getSettlement();
    	if (settlement == null) {
    		throw new IllegalStateException("Vehicle not parked at a settlement");
    	}
    	
    	double centerXLoc = 0D;
    	double centerYLoc = 0D;
    	
    	// If settlement has garages, place vehicle near a random garage.
    	// Otherwise place vehicle near settlement center.
    	List<Building> garageList = settlement.getBuildingManager().getBuildings(GroundVehicleMaintenance.NAME);
    	if (garageList.size() >= 1) {
    		Collections.shuffle(garageList);
    		Building garage = garageList.get(0);
    		centerXLoc = garage.getXLocation();
    		centerYLoc = garage.getYLocation();
    	}
    	
    	double newXLoc = 0D;
    	double newYLoc = 0D;
    	double newFacing = 0D;
    	boolean foundGoodLocation = false;
    	
    	// Try iteratively outward from 10m to 500m distance range.
    	for (int x = 15; (x < 500) && !foundGoodLocation; x+= 10) {
    		// Try ten random locations at each distance range.
    		for (int y = 0; (y < 10) && !foundGoodLocation; y++) {
    			double distance = RandomUtil.getRandomDouble(10D) + x;
    			double radianDirection = RandomUtil.getRandomDouble(Math.PI * 2D);
    			newXLoc = centerXLoc - (distance * Math.sin(radianDirection));
    			newYLoc = centerYLoc + (distance * Math.cos(radianDirection));
    		    newFacing = RandomUtil.getRandomDouble(360D);
    		    
    		    // Check if new vehicle location collides with anything.
    		    foundGoodLocation = checkVehicleLocation(newXLoc, newYLoc, newFacing);
    		}
    	}
    	
    	setXLocation(newXLoc);
    	setYLocation(newYLoc);
    	setFacing(newFacing);
    }
    
    /**
     * Checks if vehicle location and facing rectangle collides with any existing vehicle, building, 
     * or construction site.
     * @param xLoc the vehicle X location.
     * @param yLoc the vehicle Y location.
     * @param facing the vehicle facing (degrees clockwise from North).
     * @return true if vehicle location doesn't collide with anything.
     */
    private boolean checkVehicleLocation(double xLoc, double yLoc, double facing) {
    	boolean result = true;
    	
        // Create path for proposed new vehicle position.
        Rectangle2D newVehicleRect = new Rectangle2D.Double(xLoc - (getWidth() / 2D), 
                yLoc - (getLength() / 2D), getWidth(), getLength());
        Path2D newVehiclePath = getPathFromRectangleRotation(newVehicleRect, facing);
    	
    	// Check all other parked vehicles at settlement.
    	Iterator<Vehicle> i = getSettlement().getParkedVehicles().iterator();
    	while (i.hasNext() && result) {
    		Vehicle vehicle = i.next();
    		if (vehicle != this) {
    	        Rectangle2D tempVehicleRect = new Rectangle2D.Double(vehicle.getXLocation() - 
    	        		(vehicle.getWidth() / 2D), vehicle.getYLocation() - (vehicle.getLength() / 2D), 
    	        		vehicle.getWidth(), vehicle.getLength());
    	        Path2D tempVehiclePath = getPathFromRectangleRotation(tempVehicleRect, vehicle.getFacing());
    	        Area area = new Area(newVehiclePath);
                area.intersect(new Area(tempVehiclePath));
                if (!area.isEmpty()) {
                    result = false;
                }
    		}
    	}
    	
    	// Check all buildings at settlement.
        Iterator<Building> j = getSettlement().getBuildingManager().getBuildings().iterator();
        while (j.hasNext() && result) {
            Building building = j.next();
            Rectangle2D buildingRect = new Rectangle2D.Double(building.getXLocation() - 
                    (building.getWidth() / 2D), building.getYLocation() - 
                    (building.getLength() / 2D), building.getWidth(), building.getLength());
            Path2D buildingPath = getPathFromRectangleRotation(buildingRect, 
                    building.getFacing());
            Area area = new Area(newVehiclePath);
            area.intersect(new Area(buildingPath));
            if (!area.isEmpty()) {
                result = false;
            }
        }
    	
    	// Check all construction sites at settlement.
        Iterator<ConstructionSite> k = getSettlement().getConstructionManager().getConstructionSites().iterator();
        while (k.hasNext() && result) {
        	ConstructionSite site = k.next();
        	Rectangle2D siteRect = new Rectangle2D.Double(site.getXLocation() - 
        			(site.getWidth() / 2D), site.getYLocation() - 
        			(site.getLength() / 2D), site.getWidth(), site.getLength());
        	Path2D sitePath = getPathFromRectangleRotation(siteRect, 
        			site.getFacing());
        	Area area = new Area(newVehiclePath);
        	area.intersect(new Area(sitePath));
        	if (!area.isEmpty()) {
        		result = false;
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
    private Path2D getPathFromRectangleRotation(Rectangle2D rectangle, double rotation) {
        double radianRotation = rotation * (Math.PI / 180D);
        AffineTransform at = AffineTransform.getRotateInstance(radianRotation, rectangle.getCenterX(), 
                rectangle.getCenterY());
        return new Path2D.Double(rectangle, at);
    }
}
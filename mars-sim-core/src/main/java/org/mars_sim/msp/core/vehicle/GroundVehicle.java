/**
 * Mars Simulation Project
 * GroundVehicle.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.vehicle;

import org.mars_sim.msp.core.*;
import org.mars_sim.msp.core.mars.*;
import org.mars_sim.msp.core.structure.*;

import java.io.Serializable;

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
    GroundVehicle(String name, String description, Settlement settlement) throws Exception {
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
}
/**
 * Mars Simulation Project
 * GroundVehicle.java
 * @version 2.76 2004-06-01
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.mars.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
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
    private double terrainGrade; // Average angle of terrain over next 7.4km distance in direction vehicle is traveling.
    private boolean isStuck; // True if vehicle is stuck.
    
    /** Constructs a GroundVehicle object at a given settlement
     *  @param name name of the ground vehicle
     *  @param settlement settlement the ground vehicle is parked at
     */
    GroundVehicle(String name, Settlement settlement) {
        // use Vehicle constructor
        super(name, settlement);

        initGroundVehicleData();
        
        // Add to garage at settlement if available.
        try {
            BuildingManager.addToRandomBuilding(this, settlement);
        }
        catch (Exception e) {}
    }
    
    /** Initialize ground vehicle data */
    private void initGroundVehicleData() {
        
        // Add scope to malfunction manager.
        malfunctionManager.addScopeString("GroundVehicle");
	    
        setTerrainHandlingCapability(0D); // Default terrain capability
        setTerrainGrade(0D);
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        elevation = surface.getSurfaceTerrain().getElevation(location);
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

    /** Returns terrain steepness as an angle 
     *  @return ground vehicle's current terrain grade
     */
    public double getTerrainGrade() {
        return terrainGrade;
    }

    /** Sets the terrain grade with an angle 
     *  @param terrainGrade new terrain grade for the ground vehicle
     */
    public void setTerrainGrade(double terrainGrade) {
        this.terrainGrade = terrainGrade;
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
}

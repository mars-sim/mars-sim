/**
 * Mars Simulation Project
 * GroundVehicle.java
 * @version 2.72 2001-05-31
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The GroundVehicle class represents a ground-type vehicle.  It is
 *  abstract and should be extended to a particular type of ground
 *  vehicle.
*/
public abstract class GroundVehicle extends Vehicle {

    // Data members
    private double elevation; // Current elevation in km.
    private double terrainHandlingCapability; // Ground vehicle's basic terrain handling capability.
    private double terrainGrade; // Average angle of terrain over next 7.4km distance in direction vehicle is traveling.

    /** Constructs a GroundVehicle object
     *  @param name name of the ground vehicle
     *  @param location coordinate location of the ground vehicle
     *  @param mars simulated Mars
     *  @param manager unit manager for the ground vehicle
     */
    GroundVehicle(String name, Coordinates location, VirtualMars mars, UnitManager manager) {

        // use Vehicle constructor
        super(name, location, mars, manager);

        // initialize variables
        setTerrainHandlingCapability(0D); // Default terrain capability
        setTerrainGrade(0D);
        elevation = mars.getSurfaceFeatures().getSurfaceTerrain().getElevation(location);

        // initialize potential vehicle failures
        addPotentialFailure("Fuel Leak");
        addPotentialFailure("Air Leak");
        addPotentialFailure("Life Support Failure");
        addPotentialFailure("Engine Problems");
        addPotentialFailure("Battery Failure");
        addPotentialFailure("Flat Tire");
        addPotentialFailure("Transmission Failure");
        addPotentialFailure("Coolant Leak");
        addPotentialFailure("Navigation System Failure");
        addPotentialFailure("Communications Failure");
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
        return (getStatus().equals("Stuck - Using Winch"));
    }

    /** Sets the ground vehicle's stuck value 
     *  @param stuck true if vehicle is currently stuck, false otherwise
     */
    public void setStuck(boolean stuck) {
        if (stuck) setStatus("Stuck - Using Winch");
        else setStatus("Moving");
    }
}

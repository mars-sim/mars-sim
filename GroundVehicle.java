/**
 * Mars Simulation Project
 * GroundVehicle.java
 * @version 2.70 2000-09-01
 * @author Scott Davis
 */

/** The GroundVehicle class represents a ground-type vehicle.  It is
 *  abstract and should be extended to a particular type of ground
 *  vehicle.
*/
public abstract class GroundVehicle extends Vehicle {

    private double elevation;                 // Current elevation in km
    private double terrainHandlingCapability; // Ground vehicle's basic terrain handling capability	
    private double terrainGrade;              // Average angle of terrain over next 7.4km distance in direction vehicle is traveling

    public GroundVehicle(String name, Coordinates location, VirtualMars mars, UnitManager manager) {
		
	// use Vehicle constructor
	super(name, location, mars, manager); 
		
	// initialize variables
	setTerrainHandlingCapability(0D);  // Default terrain capability
	setTerrainGrade(0D);
	elevation = mars.getSurfaceTerrain().getElevation(location);
		
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
	
    /** Returns the elevation of the vehicle in km. */
    public double getElevation() {
	return elevation;
    }
	
    /** Sets the elevation of the vehicle (in km.) */
    public void setElevation(double elevation) {
	this.elevation = elevation;
    }

    /** Returns the vehicle's terrain capability */
    public double getTerrainHandlingCapability() {
	return terrainHandlingCapability;
    }

    /** Returns the vehicle's terrain capability */
    public void setTerrainHandlingCapability(double c) {
	terrainHandlingCapability = c;
    }

    /** Returns terrain steepness as angle */
    public double getTerrainGrade() {
	return terrainGrade;
    }

    /** Sets the terrain grade with an angle */
    public void setTerrainGrade(double terrainGrade) {
	this.terrainGrade = terrainGrade;
    }

    /** Returns true if ground vehicle is stuck */
    public boolean isStuck() { 
	return (getStatus().equals("Stuck - Using Winch"));
    }
	
    /** Sets the ground vehicle's stuck value */
    public void setStuck(boolean stuck) { 
	if (stuck) {
	    setStatus("Stuck - Using Winch");
	} else {
	    setStatus("Moving");
	}
    }
}

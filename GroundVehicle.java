/**
 * Mars Simulation Project
 * GroundVehicle.java
 * @version 2.70 2000-07-14
 * @author Scott Davis
 */

/** The GroundVehicle class represents a ground-type vehicle.  It is
 *  abstract and should be extended to a particular type of ground
 *  vehicle.
*/
public abstract class GroundVehicle extends Vehicle {

    protected double elevation;                 // Current elevation in km
    protected double terrainHandlingCapability; // Ground vehicle's basic terrain handling capability	
    protected double terrainGrade;              // Average angle of terrain over next 7.4km distance in direction vehicle is traveling

    public GroundVehicle(String name, Coordinates location, VirtualMars mars, UnitManager manager) {
		
	// Use Vehicle constructor
	super(name, location, mars, manager); 
		
	// Initialize public variables
	terrainHandlingCapability = 0D;  // Default terrain capability
	terrainGrade = 0D;
	elevation = mars.getSurfaceTerrain().getElevation(location);
		
	// Initialize potential vehicle failures.
	potentialFailures.put("Fuel Leak", new Integer(1));
	potentialFailures.put("Air Leak", new Integer(1));
	potentialFailures.put("Life Support Failure", new Integer(1));
	potentialFailures.put("Engine Problems", new Integer(1));
	potentialFailures.put("Battery Failure", new Integer(1));
	potentialFailures.put("Flat Tire", new Integer(1));
	potentialFailures.put("Transmission Failure", new Integer(1));
	potentialFailures.put("Coolant Leak", new Integer(1));
	potentialFailures.put("Navigation System Failure", new Integer(1));
	potentialFailures.put("Communications Failure", new Integer(1));
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

    /** Returns true if ground vehicle is stuck */
    public boolean getStuck() { 
	if (status.equals("Stuck - Using Winch")) {
	    return true;
	} else {
	    return false;
	}
    }
	
    /** Sets the ground vehicle's stuck value */
    public void setStuck(boolean stuck) { 
	if (stuck) {
	    status = "Stuck - Using Winch";
	} else {
	    status = "Moving";
	}
    }

    /** Returns terrain steepness as angle */
    public double getTerrainGrade() {
	return terrainGrade;
    }

    /** Sets the terrain grade with an angle */
    public void setTerrainGrade(double terrainGrade) {
	this.terrainGrade = terrainGrade;
    }
}

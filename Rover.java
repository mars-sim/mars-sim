/**
 * Mars Simulation Project
 * Rover.java
 * @version 2.70 2000-05-06
 * @author Scott Davis
 */

import java.awt.*;
import javax.swing.*;

/** The Rover class represents the rover type of ground vehicle.  It
 *  contains information about the rover.
 */
class Rover extends GroundVehicle {

    public Rover(String name, Coordinates location, VirtualMars mars, UnitManager manager) {
		
	// Use GroundVehicle constructor
	super(name, location, mars, manager); 
		
	// Set rover terrain modifier
	setTerrainHandlingCapability(0D);
		
	// Set the vehicle size of the rover.
	setSize(2);

	// Set default maximum passengers for a rover.
	setMaxPassengers(8);
		
	// Set default fuel capacity for a rover.
	setFuelCapacity(10D);
    }

    /** Returns a detail window for the unit */
    public UnitDialog getDetailWindow(MainDesktopPane parentDesktop) {
	return new RoverDialog(parentDesktop, this);
    } 
}

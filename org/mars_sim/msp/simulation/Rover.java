/**
 * Mars Simulation Project
 * Rover.java
 * @version 2.71 2000-10-17
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The Rover class represents the rover type of ground vehicle.  It
 *  contains information about the rover.
 */
public class Rover extends GroundVehicle {

    /** Constructs a Rover object 
     *  @param name the name of the rover
     *  @param location the location of the rover
     *  @param mars the virtual Mars
     *  @param manager the rover's unit manager
     */
    Rover(String name, Coordinates location, VirtualMars mars, UnitManager manager) {

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
}

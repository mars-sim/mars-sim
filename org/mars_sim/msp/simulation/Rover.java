/**
 * Mars Simulation Project
 * Rover.java
 * @version 2.73 2001-12-06
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.Serializable;

/** The Rover class represents the rover type of ground vehicle.  It
 *  contains information about the rover.
 */
public class Rover extends GroundVehicle implements Serializable {

    /** Constructs a Rover object at a given settlement
     *  @param name the name of the rover
     *  @param settlement the settlement the rover is parked at
     *  @param mars the virtual Mars
     */
    Rover(String name, Settlement settlement, VirtualMars mars) {
        // Use GroundVehicle constructor
        super(name, settlement, mars);
        
        initRoverData();
    }
    
    /** Constructs a Rover object
     *  @param name the name of the rover
     *  @param mars the virtual Mars
     *  @param manager the unit manager
     *  @throws Exception when there are no available settlements
     */
    Rover(String name, VirtualMars mars, UnitManager manager) throws Exception {
        // Use GroundVehicle constructor
        super(name, mars, manager);
        
        initRoverData();
    }
    
    /** Initialize rover data */
    private void initRoverData() {
        // Set rover terrain modifier
        setTerrainHandlingCapability(0D);

        // Set the vehicle size of the rover.
        setSize(2);

        // Set default maximum passengers for a rover.
        setMaxPassengers(8);

        // Get simulation properties.
        SimulationProperties properties = mars.getSimulationProperties();

        // Set default fuel capacity for a rover.
        setFuelCapacity(properties.getRoverFuelStorageCapacity());
        
        // Set default oxygen capacity for a rover.
        setOxygenCapacity(properties.getRoverOxygenStorageCapacity());
        
        // Set default water capacity for a rover.
        setWaterCapacity(properties.getRoverWaterStorageCapacity());
        
        // Set default food capacity for a rover.
        setFoodCapacity(properties.getRoverFoodStorageCapacity());

        // Set range of rover.
        range = properties.getRoverFuelEfficiency() * getFuelCapacity() * .8D;
 
        // Set base speed to 30kph.
        setBaseSpeed(30D);
    }
}

/**
 * Mars Simulation Project
 * Rover.java
 * @version 2.73 2001-12-09
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

        // Set base speed to 30kph.
        setBaseSpeed(30D);
    }

    /** Gets the range of the rover
     *  @return the range of the rover (km)
     */
    public double getRange() {
        SimulationProperties properties = mars.getSimulationProperties();
        return properties.getRoverRange();
    }

    /** Gets the fuel capacity of the rover
     *  @return the fuel capacity of the rover (kg)
     */
    public double getFuelCapacity() {
        SimulationProperties properties = mars.getSimulationProperties();
        return properties.getRoverFuelStorageCapacity();
    }

    /** Gets the oxygen capacity of the rover
     *  @return the oxygen capacity of the rover (kg)
     */
    public double getOxygenCapacity() {
        SimulationProperties properties = mars.getSimulationProperties();
        return properties.getRoverOxygenStorageCapacity();
    }

    /** Gets the water capacity of the rover
     *  @return the water capacity of the rover (kg)
     */
    public double getWaterCapacity() {
        SimulationProperties properties = mars.getSimulationProperties();
        return properties.getRoverWaterStorageCapacity();
    }

    /** Gets the food capacity of the rover
     *  @return the food capacity of the rover (kg)
     */
    public double getFoodCapacity() {
        SimulationProperties properties = mars.getSimulationProperties();
        return properties.getRoverFoodStorageCapacity();
    }
}

/**
 * Mars Simulation Project
 * Rover.java
 * @version 2.74 2002-01-30
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.*;
import java.io.Serializable;

/** The Rover class represents the rover type of ground vehicle.  It
 *  contains information about the rover.
 */
public class Rover extends GroundVehicle implements LifeSupport, Serializable {

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

        // Set the empty mass of the rover.
	baseMass = 10000D;
	
        // Set inventory total mass capacity.
	inventory.setTotalCapacity(10000D);
	
	// Set inventory resource capacities.
        SimulationProperties properties = mars.getSimulationProperties();
	inventory.setResourceCapacity(Inventory.FUEL, properties.getRoverFuelStorageCapacity());
	inventory.setResourceCapacity(Inventory.OXYGEN, properties.getRoverOxygenStorageCapacity());
	inventory.setResourceCapacity(Inventory.WATER, properties.getRoverWaterStorageCapacity());
	inventory.setResourceCapacity(Inventory.FOOD, properties.getRoverFoodStorageCapacity());
    }

    /** Gets the range of the rover
     *  @return the range of the rover (km)
     */
    public double getRange() {
        SimulationProperties properties = mars.getSimulationProperties();
        return properties.getRoverRange();
    }

    /** Returns true if life support is working properly and is not out
     *  of oxygen or water.
     *  @return true if life support is OK
     */
    public boolean lifeSupportCheck() {
        boolean result = true;

        if (inventory.getResourceMass(Inventory.OXYGEN) <= 0D) result = false;
        if (inventory.getResourceMass(Inventory.WATER) <= 0D) result = false;

        // need to also check for temp and air pressure

        return result;
    }

    /** Gets the number of people the life support can provide for.
     *  @return the capacity of the life support system
     */
    public int getLifeSupportCapacity() {
        return getMaxPassengers();
    }

    /** Gets oxygen from system.
     *  @param amountRequested the amount of oxygen requested from system (kg)
     *  @return the amount of oxgyen actually received from system (kg)
     */
    public double provideOxygen(double amountRequested) {
        return inventory.removeResource(Inventory.OXYGEN, amountRequested);
    }

    /** Gets water from system.
     *  @param amountRequested the amount of water requested from system (kg)
     *  @return the amount of water actually received from system (kg)
     */
    public double provideWater(double amountRequested) {
        return inventory.removeResource(Inventory.WATER, amountRequested);
    }

    /** Gets the air pressure of the life support system.
     *  @return air pressure (atm)
     */
    public double getAirPressure() {
        // Return 1 atm for now
        return 1D;
    }

    /** Gets the temperature of the life support system.
     *  @return temperature (degrees C)
     */
    public double getTemperature() {
        // Return 25 degrees celsius for now
        return 25D;
    }
}

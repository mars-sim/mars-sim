/**
 * Mars Simulation Project
 * Rover.java
 * @version 2.74 2002-03-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.equipment.*;

/** The Rover class represents the rover type of ground vehicle.  It
 *  contains information about the rover.
 */
public abstract class Rover extends GroundVehicle implements Crewable, LifeSupport {

    // Static data members
    private static final double BASE_SPEED = 30D; // Base speed of rover in kph.
    private static final double BASE_MASS = 10000D; // Base mass of rover in kg.
    private static final double CARGO_CAPACITY = 10000D; // Cargo capacity of rover in kg.
    private static final double FUEL_CAPACITY = 2500D; // Fuel capacity of rover in kg.
    private static final double OXYGEN_CAPACITY = 350D; // Oxygen capacity of rover in kg.
    private static final double WATER_CAPACITY = 1400D; // Water capacity of rover in kg.
    private static final double FOOD_CAPACITY = 525D; // Food capacity of rover in kg.
	
    // Data members
    protected int crewCapacity = 0; // The rover's capacity for crewmembers.
    protected Airlock airlock; // The rover's airlock.
	
    /** Constructs a Rover object at a given settlement
     *  @param name the name of the rover
     *  @param settlement the settlement the rover is parked at
     *  @param mars the virtual Mars
     */
    Rover(String name, Settlement settlement, Mars mars) {
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
    Rover(String name, Mars mars, UnitManager manager) throws Exception {
        // Use GroundVehicle constructor
        super(name, mars, manager);

        initRoverData();
    }
    
    /** Initialize rover data */
    private void initRoverData() {
        // Set rover terrain modifier
        setTerrainHandlingCapability(0D);

        // Set base speed to 30kph.
        setBaseSpeed(BASE_SPEED);

        // Set the empty mass of the rover.
	baseMass = BASE_MASS;
	
        // Set inventory total mass capacity.
	inventory.setTotalCapacity(CARGO_CAPACITY);
	
	// Set inventory resource capacities.
	inventory.setResourceCapacity(Inventory.FUEL, FUEL_CAPACITY);
	inventory.setResourceCapacity(Inventory.OXYGEN, OXYGEN_CAPACITY);
	inventory.setResourceCapacity(Inventory.WATER, WATER_CAPACITY);
	inventory.setResourceCapacity(Inventory.FOOD, FOOD_CAPACITY);

	// Create the rover's airlock.
	airlock = new Airlock(this, mars, 2);
    }

    /** 
     * Adds enough EVA suits to inventory to match crew capacity.
     */
    protected void addEVASuits() {
        for (int x=0; x < getCrewCapacity(); x++) {
	    EVASuit suit = new EVASuit(location, mars);
	    inventory.addUnit(suit);
	}
    }

    /** Gets the range of the rover
     *  @return the range of the rover (km)
     */
    public double getRange() {
        SimulationProperties properties = mars.getSimulationProperties();
        return properties.getRoverRange();
    }

    /**
     * Gets the number of crewmembers the vehicle can carry.
     * @return capacity
     */
    public int getCrewCapacity() {
        return crewCapacity;
    }
    
    /**
     * Gets the current number of crewmembers.
     * @return number of crewmembers
     */
    public int getCrewNum() {
        return getCrew().size();
    }

    /**
     * Gets a collection of the crewmembers.
     * @return crewmembers as PersonCollection
     */
    public PersonCollection getCrew() {
        return inventory.getContainedUnits().getPeople();
    }

    /**
     * Checks if person is a crewmember.
     * @param person the person to check
     * @return true if person is a crewmember
     */
    public boolean isCrewmember(Person person) {
        return inventory.containsUnit(person);
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
        if (!result) System.out.println(getName() + " failed its life support test!");
        return result;
    }

    /** Gets the number of people the life support can provide for.
     *  @return the capacity of the life support system
     */
    public int getLifeSupportCapacity() {
        return getCrewCapacity();
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

    /** 
     * Gets the rover's airlock.
     * @return rover's airlock
     */
    public Airlock getAirlock() {
        return airlock;
    }

    /** 
     * Perform time-related processes
     * @param time the amount of time passing (in millisols)
     */
    public void timePassing(double time) {
        airlock.timePassing(time);
    }
}

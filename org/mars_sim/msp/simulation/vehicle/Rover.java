/**
 * Mars Simulation Project
 * Rover.java
 * @version 2.75 2003-01-08
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
public abstract class Rover extends GroundVehicle implements Crewable, LifeSupport, Airlockable {

    // Static data members
    private static final double BASE_SPEED = 30D; // Base speed of rover in kph.
    private static final double BASE_MASS = 10000D; // Base mass of rover in kg.
    private static final double CARGO_CAPACITY = 10000D; // Cargo capacity of rover in kg.
    private static final double FUEL_CAPACITY = 2500D; // Fuel capacity of rover in kg.
    private static final double OXYGEN_CAPACITY = 350D; // Oxygen capacity of rover in kg.
    private static final double WATER_CAPACITY = 1400D; // Water capacity of rover in kg.
    private static final double FOOD_CAPACITY = 525D; // Food capacity of rover in kg.
    private double NORMAL_AIR_PRESSURE = 1D; // Normal air pressure (atm.)
    private double NORMAL_TEMP = 25D; // Normal temperature (celsius)
    
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
    
    /** Initialize rover data */
    private void initRoverData() {

        // Add scope to malfunction manager.
	malfunctionManager.addScopeString("Rover");
	malfunctionManager.addScopeString("Crewable");
	malfunctionManager.addScopeString("LifeSupport");
	    
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
        for (int x=0; x < getCrewCapacity(); x++) 
	    inventory.addUnit(new EVASuit(location, mars));
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
        if (malfunctionManager.getOxygenFlowModifier() < 100D) result = false;
        if (malfunctionManager.getWaterFlowModifier() < 100D) result = false;
        if (getAirPressure() != NORMAL_AIR_PRESSURE) result = false;
        if (getTemperature() != NORMAL_TEMP) result = false;
	
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
        return inventory.removeResource(Inventory.OXYGEN, amountRequested) *
	        (malfunctionManager.getOxygenFlowModifier() / 100D);
    }

    /** Gets water from system.
     *  @param amountRequested the amount of water requested from system (kg)
     *  @return the amount of water actually received from system (kg)
     */
    public double provideWater(double amountRequested) {
        return inventory.removeResource(Inventory.WATER, amountRequested)  *
	        (malfunctionManager.getWaterFlowModifier() / 100D);
    }

    /** Gets the air pressure of the life support system.
     *  @return air pressure (atm)
     */
    public double getAirPressure() {
        double result = NORMAL_AIR_PRESSURE * 
	        (malfunctionManager.getAirPressureModifier() / 100D);
        double ambient = mars.getWeather().getAirPressure(location);
        if (result < ambient) return ambient;
        else return result;
    }

    /** Gets the temperature of the life support system.
     *  @return temperature (degrees C)
     */
    public double getTemperature() {
        double result = NORMAL_TEMP *
	        (malfunctionManager.getTemperatureModifier() / 100D);
        double ambient = mars.getWeather().getTemperature(location);
        if (result < ambient) return ambient;
        else return result;
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
	super.timePassing(time);
        airlock.timePassing(time);
    }

    /**
     * Gets a collection of people affected by this entity.
     * @return person collection
     */
    public PersonCollection getAffectedPeople() {
        PersonCollection people = super.getAffectedPeople();
        
	PersonCollection crew = getCrew();
        PersonIterator i = crew.iterator();
        while (i.hasNext()) {
	    Person person = i.next();
	    if (!people.contains(person)) people.add(person);
	}

        return people;
    }
}

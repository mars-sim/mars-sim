/**
 * Mars Simulation Project
 * Rover.java
 * @version 2.75 2004-03-23
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.Settlement;

/** The Rover class represents the rover type of ground vehicle.  It
 *  contains information about the rover.
 */
public class Rover extends GroundVehicle implements Crewable, LifeSupport, Airlockable {

    // Static data members
    private double NORMAL_AIR_PRESSURE = 1D; // Normal air pressure (atm.)
    private double NORMAL_TEMP = 25D; // Normal temperature (celsius)
    
    // Data members
    private int crewCapacity = 0; // The rover's capacity for crewmembers.
    private Airlock airlock; // The rover's airlock.
    private double range; // Operating range of rover in km.
	private Lab lab; // The rover's lab.
	private SickBay sickbay; // The rover's sick bay.
	
    /** 
     * Constructs a Rover object at a given settlement
     * @param name the name of the rover
     * @param settlement the settlement the rover is parked at
     * @param mars the virtual Mars
     * @throws Exception if rover could not be constructed.
     */
    public Rover(String name, String description, Settlement settlement, Mars mars) throws Exception {
        // Use GroundVehicle constructor
        super(name, settlement, mars);

		this.description = description;
		
		// Get vehicle configuration.
		VehicleConfig config = mars.getSimulationConfiguration().getVehicleConfiguration();
		
		// Add scope to malfunction manager.
		malfunctionManager.addScopeString("Rover");
		malfunctionManager.addScopeString("Crewable");
		malfunctionManager.addScopeString("LifeSupport");
		malfunctionManager.addScopeString(description);
		if (config.hasLab(description)) malfunctionManager.addScopeString("Laboratory");
		if (config.hasSickbay(description)) malfunctionManager.addScopeString("Sickbay");
		
		// Set base speed to 30kph.
		setBaseSpeed(config.getBaseSpeed(description));

		// Set the empty mass of the rover.
		baseMass = config.getEmptyMass(description);
	    
		// Set the operating range of rover.
		range = config.getRange(description);
        
		// Set crew capacity
		crewCapacity = config.getCrewSize(description);

		// Set inventory total mass capacity.
		inventory.setTotalCapacity(config.getTotalCapacity(description));
	
		// Set inventory resource capacities.
		inventory.setResourceCapacity(Resource.METHANE, config.getCargoCapacity(description, Resource.METHANE));
		inventory.setResourceCapacity(Resource.OXYGEN, config.getCargoCapacity(description, Resource.OXYGEN));
		inventory.setResourceCapacity(Resource.WATER, config.getCargoCapacity(description, Resource.WATER));
		inventory.setResourceCapacity(Resource.FOOD, config.getCargoCapacity(description, Resource.FOOD));
		inventory.setResourceCapacity(Resource.ROCK_SAMPLES, config.getCargoCapacity(description, Resource.ROCK_SAMPLES));
		inventory.setResourceCapacity(Resource.ICE, config.getCargoCapacity(description, Resource.ICE));
	
		// Construct sickbay.
		if (config.hasSickbay(description)) 
			sickbay = new SickBay(this, config.getSickbayTechLevel(description), config.getSickbayBeds(description));
		
		// Construct lab.
		if (config.hasLab(description)) 
			lab = new MobileLaboratory(1, config.getLabTechLevel(description), config.getLabTechSpecialities(description));
		
		// Set rover terrain modifier
		setTerrainHandlingCapability(0D);

		// Create the rover's airlock.
		try { airlock = new VehicleAirlock(this, 2); }
		catch (Exception e) { System.out.println(e.getMessage()); }
		
		// Add EVA suits to inventory.
		try {
			int suitNum = mars.getSimulationConfiguration().getVehicleConfiguration().getEvaSuits(description);
			for (int x=0; x < suitNum; x++) 
			inventory.addUnit(new EVASuit(location, mars));
		}
		catch (Exception e) {
			throw new Exception("Could not add EVA suits.: " + e.getMessage());
		}
    }

    /** Gets the range of the rover
     *  @return the range of the rover (km)
     */
    public double getRange() {
        return range;
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

        if (inventory.getResourceMass(Resource.OXYGEN) <= 0D) result = false;
        if (inventory.getResourceMass(Resource.WATER) <= 0D) result = false;
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
        return inventory.removeResource(Resource.OXYGEN, amountRequested) *
	        (malfunctionManager.getOxygenFlowModifier() / 100D);
    }

    /** Gets water from system.
     *  @param amountRequested the amount of water requested from system (kg)
     *  @return the amount of water actually received from system (kg)
     */
    public double provideWater(double amountRequested) {
        return inventory.removeResource(Resource.WATER, amountRequested)  *
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
    
    /**
     * Checks if the rover has a laboratory.
     * @return true if lab.
     */
    public boolean hasLab() {
    	if (lab != null) return true;
    	else return false;
    }

	/**
	 * Gets the rover's laboratory
	 * @return lab
	 */
	public Lab getLab() {
		return lab;
	}
	
	/**
	 * Checks if the rover has a sickbay.
	 * @return true if sickbay
	 */
	public boolean hasSickbay() {
		if (sickbay != null) return true;
		else return false;
	}
	
	/**
	 * Gets the rover's sickbay.
	 * @return sickbay
	 */
	public SickBay getSickbay() {
		return sickbay;
	}
}
/**
 * Mars Simulation Project
 * Rover.java
 * @version 2.78 2005-05-09
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;

/** 
 * The Rover class represents the rover type of ground vehicle.  It
 * contains information about the rover.
 */
public class Rover extends GroundVehicle implements Crewable, LifeSupport, Airlockable, Medical {

    // Static data members
    private double NORMAL_AIR_PRESSURE = 1D; // Normal air pressure (atm.)
    private double NORMAL_TEMP = 25D; // Normal temperature (celsius)
    
    // Data members
    private int crewCapacity = 0; // The rover's capacity for crewmembers.
    private Airlock airlock; // The rover's airlock.
	private Lab lab; // The rover's lab.
	private SickBay sickbay; // The rover's sick bay.
	
    /** 
     * Constructs a Rover object at a given settlement
     * @param name the name of the rover
     * @param description the configuration description of the vehicle.
     * @param settlement the settlement the rover is parked at
     * @throws Exception if rover could not be constructed.
     */
    public Rover(String name, String description, Settlement settlement) throws Exception {
        // Use GroundVehicle constructor
        super(name, description, settlement);
		
		// Get vehicle configuration.
		SimulationConfig simConfig = Simulation.instance().getSimConfig();
		VehicleConfig config = simConfig.getVehicleConfiguration();
		
		// Add scope to malfunction manager.
		malfunctionManager.addScopeString("Rover");
		malfunctionManager.addScopeString("Crewable");
		malfunctionManager.addScopeString("LifeSupport");
		malfunctionManager.addScopeString(description);
		if (config.hasLab(description)) malfunctionManager.addScopeString("Laboratory");
		if (config.hasSickbay(description)) malfunctionManager.addScopeString("Sickbay");
        
		// Set crew capacity
		crewCapacity = config.getCrewSize(description);

		// Set inventory total mass capacity.
		inventory.addGeneralCapacity(config.getTotalCapacity(description));
	
		// Set inventory resource capacities.
		inventory.addAmountResourceTypeCapacity(AmountResource.METHANE, config.getCargoCapacity(description, AmountResource.METHANE.getName()));
		inventory.addAmountResourceTypeCapacity(AmountResource.OXYGEN, config.getCargoCapacity(description, AmountResource.OXYGEN.getName()));
		inventory.addAmountResourceTypeCapacity(AmountResource.WATER, config.getCargoCapacity(description, AmountResource.WATER.getName()));
		inventory.addAmountResourceTypeCapacity(AmountResource.FOOD, config.getCargoCapacity(description, AmountResource.FOOD.getName()));
		inventory.addAmountResourceTypeCapacity(AmountResource.ROCK_SAMPLES, config.getCargoCapacity(description, AmountResource.ROCK_SAMPLES.getName()));
		inventory.addAmountResourceTypeCapacity(AmountResource.ICE, config.getCargoCapacity(description, AmountResource.ICE.getName()));
	
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

        if (inventory.getAmountResourceStored(AmountResource.OXYGEN) <= 0D) result = false;
        if (inventory.getAmountResourceStored(AmountResource.WATER) <= 0D) result = false;
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
    	double oxygenTaken = amountRequested;
    	double oxygenLeft = inventory.getAmountResourceStored(AmountResource.OXYGEN);
    	if (oxygenTaken > oxygenLeft) oxygenTaken = oxygenLeft;
    	try {
    		inventory.retrieveAmountResource(AmountResource.OXYGEN, oxygenTaken);
    	}
    	catch (InventoryException e) {};
        return oxygenTaken * (malfunctionManager.getOxygenFlowModifier() / 100D);
    }

    /** Gets water from system.
     *  @param amountRequested the amount of water requested from system (kg)
     *  @return the amount of water actually received from system (kg)
     */
    public double provideWater(double amountRequested) {
    	double waterTaken = amountRequested;
    	double waterLeft = inventory.getAmountResourceStored(AmountResource.WATER);
    	if (waterTaken > waterLeft) waterTaken = waterLeft;
    	try {
    		inventory.retrieveAmountResource(AmountResource.WATER, waterTaken);
    	}
    	catch (InventoryException e) {};
        return waterTaken * (malfunctionManager.getWaterFlowModifier() / 100D);
    }

    /** Gets the air pressure of the life support system.
     *  @return air pressure (atm)
     */
    public double getAirPressure() {
        double result = NORMAL_AIR_PRESSURE * 
	        (malfunctionManager.getAirPressureModifier() / 100D);
        double ambient = Simulation.instance().getMars().getWeather().getAirPressure(location);
        if (result < ambient) return ambient;
        else return result;
    }

    /** Gets the temperature of the life support system.
     *  @return temperature (degrees C)
     */
    public double getTemperature() {
        double result = NORMAL_TEMP *
	        (malfunctionManager.getTemperatureModifier() / 100D);
        double ambient = Simulation.instance().getMars().getWeather().getTemperature(location);
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
     * @throws exception if error during time.
     */
    public void timePassing(double time) throws Exception {
        super.timePassing(time);
        
        try {
        	airlock.timePassing(time);
        }
        catch (Exception e) {
        	throw new Exception("Rover " + getName() + " timePassing(): " + e.getMessage());
        }
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
	public boolean hasSickBay() {
		if (sickbay != null) return true;
		else return false;
	}
	
	/**
	 * Gets the rover's sickbay.
	 * @return sickbay
	 */
	public SickBay getSickBay() {
		return sickbay;
	}
	
    /**
     * Checks if a particular operator is appropriate for a vehicle.
     * @param driver the operator to check
     * @return true if appropriate operator for this vehicle.
     */
    public boolean isAppropriateOperator(VehicleOperator operator) {
    	if ((operator instanceof Person) && (inventory.containsUnit((Unit) operator))) return true;
    	else return false;
    }
    
    /**
     * Gets the resource type that this vehicle uses for fuel.
     * @return resource type as a string
     * @see org.mars_sim.msp.simulation.Resource
     */
    public AmountResource getFuelType() {
    	return AmountResource.METHANE;
    }
    
    /**
     * Checks if the vehicle is loaded with at least a percentage maximum resources.
     * @param percentage the percentage of maximum
     * @return true if vehicle has at least percentage many resources.
     */
    public boolean isLoaded(double percentage) {
    	if (super.isLoaded(percentage)) {
    		// Make sure sufficient life support is loaded.
    		Inventory i = getInventory();
    		double oxygenPercentage = i.getAmountResourceStored(AmountResource.OXYGEN) / i.getAmountResourceCapacity(AmountResource.OXYGEN);
    		double waterPercentage = i.getAmountResourceStored(AmountResource.WATER) / i.getAmountResourceCapacity(AmountResource.WATER);
    		double foodPercentage = i.getAmountResourceStored(AmountResource.FOOD) / i.getAmountResourceCapacity(AmountResource.FOOD);
    	
    		return ((oxygenPercentage >= percentage) && (waterPercentage >= percentage) && (foodPercentage >= percentage));
    	}
    	else return false;
    }
}
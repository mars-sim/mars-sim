/**
 * Mars Simulation Project
 * Rover.java
 * @version 2.80 2006-12-03
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonCollection;
import org.mars_sim.msp.simulation.person.PersonConfig;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;

/** 
 * The Rover class represents the rover type of ground vehicle.  It
 * contains information about the rover.
 */
public class Rover extends GroundVehicle implements Crewable, LifeSupport, Airlockable, Medical, Towing {

    // Static data members
    private double NORMAL_AIR_PRESSURE = 1D; // Normal air pressure (atm.)
    private double NORMAL_TEMP = 25D; // Normal temperature (celsius)
    
    public static final double LIFE_SUPPORT_RANGE_ERROR_MARGIN = 3.0D;
    
    // Data members
    private int crewCapacity = 0; // The rover's capacity for crewmembers.
    private Airlock airlock; // The rover's airlock.
	private Lab lab; // The rover's lab.
	private SickBay sickbay; // The rover's sick bay.
	private Vehicle towedVehicle; // The vehicle the rover is currently towing.
	
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
		VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
		
		// Add scope to malfunction manager.
		malfunctionManager.addScopeString("Rover");
		malfunctionManager.addScopeString("Crewable");
		malfunctionManager.addScopeString("LifeSupport");
		malfunctionManager.addScopeString(description);
		if (config.hasLab(description)) malfunctionManager.addScopeString("Laboratory");
		if (config.hasSickbay(description)) malfunctionManager.addScopeString("Sickbay");
        
		// Set crew capacity
		crewCapacity = config.getCrewSize(description);

		Inventory inv = getInventory();
		
		// Set inventory total mass capacity.
		inv.addGeneralCapacity(config.getTotalCapacity(description));
	
		// Set inventory resource capacities.
		inv.addAmountResourceTypeCapacity(AmountResource.METHANE, config.getCargoCapacity(description, AmountResource.METHANE.getName()));
		inv.addAmountResourceTypeCapacity(AmountResource.OXYGEN, config.getCargoCapacity(description, AmountResource.OXYGEN.getName()));
		inv.addAmountResourceTypeCapacity(AmountResource.WATER, config.getCargoCapacity(description, AmountResource.WATER.getName()));
		inv.addAmountResourceTypeCapacity(AmountResource.FOOD, config.getCargoCapacity(description, AmountResource.FOOD.getName()));
		inv.addAmountResourceTypeCapacity(AmountResource.ROCK_SAMPLES, config.getCargoCapacity(description, AmountResource.ROCK_SAMPLES.getName()));
		inv.addAmountResourceTypeCapacity(AmountResource.ICE, config.getCargoCapacity(description, AmountResource.ICE.getName()));
	
		// Construct sickbay.
		if (config.hasSickbay(description)) 
			sickbay = new SickBay(this, config.getSickbayTechLevel(description), config.getSickbayBeds(description));
		
		// Construct lab.
		if (config.hasLab(description)) 
			lab = new MobileLaboratory(1, config.getLabTechLevel(description), config.getLabTechSpecialities(description));
		
		// Set rover terrain modifier
		setTerrainHandlingCapability(0D);

		// Create the rover's airlock.
		try { airlock = new VehicleAirlock(this, 1); }
		catch (Exception e) { e.printStackTrace(System.err); }
    }
    
    /**
     * Sets the vehicle this rover is currently towing.
     * @param towedVehicle the vehicle being towed.
     */
    public void setTowedVehicle(Vehicle towedVehicle) {
    	if (this == towedVehicle) throw new IllegalArgumentException("Rover cannot tow itself.");
    	this.towedVehicle = towedVehicle;
    }
    
    /**
     * Gets the vehicle this rover is currently towing.
     * @return towed vehicle.
     */
    public Vehicle getTowedVehicle() {
    	return towedVehicle;
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
        return getInventory().getContainedUnits().getPeople();
    }

    /**
     * Checks if person is a crewmember.
     * @param person the person to check
     * @return true if person is a crewmember
     */
    public boolean isCrewmember(Person person) {
        return getInventory().containsUnit(person);
    }
    
    /** Returns true if life support is working properly and is not out
     *  of oxygen or water.
     *  @return true if life support is OK
     *  @throws Exception if error checking life support.
     */
    public boolean lifeSupportCheck() throws Exception {
        boolean result = true;

        if (getInventory().getAmountResourceStored(AmountResource.OXYGEN) <= 0D) result = false;
        if (getInventory().getAmountResourceStored(AmountResource.WATER) <= 0D) result = false;
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
     *  @throws Exception if error providing oxygen.
     */
    public double provideOxygen(double amountRequested) throws Exception {
    	double oxygenTaken = amountRequested;
    	double oxygenLeft = getInventory().getAmountResourceStored(AmountResource.OXYGEN);
    	if (oxygenTaken > oxygenLeft) oxygenTaken = oxygenLeft;
    	try {
    		getInventory().retrieveAmountResource(AmountResource.OXYGEN, oxygenTaken);
    	}
    	catch (InventoryException e) {};
        return oxygenTaken * (malfunctionManager.getOxygenFlowModifier() / 100D);
    }

    /** Gets water from system.
     *  @param amountRequested the amount of water requested from system (kg)
     *  @return the amount of water actually received from system (kg)
     *  @throws Exception if error providing water.
     */
    public double provideWater(double amountRequested) throws Exception {
    	double waterTaken = amountRequested;
    	double waterLeft = getInventory().getAmountResourceStored(AmountResource.WATER);
    	if (waterTaken > waterLeft) waterTaken = waterLeft;
    	try {
    		getInventory().retrieveAmountResource(AmountResource.WATER, waterTaken);
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
        double ambient = Simulation.instance().getMars().getWeather().getAirPressure(getCoordinates());
        if (result < ambient) return ambient;
        else return result;
    }

    /** Gets the temperature of the life support system.
     *  @return temperature (degrees C)
     */
    public double getTemperature() {
        double result = NORMAL_TEMP *
	        (malfunctionManager.getTemperatureModifier() / 100D);
        double ambient = Simulation.instance().getMars().getWeather().getTemperature(getCoordinates());
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
     * @param operator the operator to check
     * @return true if appropriate operator for this vehicle.
     */
    public boolean isAppropriateOperator(VehicleOperator operator) {
    	if ((operator instanceof Person) && (getInventory().containsUnit((Unit) operator))) return true;
    	else return false;
    }
    
    /**
     * Gets the resource type that this vehicle uses for fuel.
     * @return resource type as a string
     */
    public AmountResource getFuelType() {
    	return AmountResource.METHANE;
    }
    
    /** 
     * Sets unit's location coordinates 
     * @param newLocation the new location of the unit
     */
    public void setCoordinates(Coordinates newLocation) {
    	super.setCoordinates(newLocation);
        
    	// Set towed vehicle (if any) to new location.
        if (towedVehicle != null) towedVehicle.setCoordinates(newLocation);
    }
    
    /** 
     * Gets the range of the vehicle
     * @return the range of the vehicle (in km)
     * @throws Exception if error getting range.
     */
    public double getRange() throws Exception {
    	double range = super.getRange();
    	
    	double distancePerSol = getEstimatedTravelDistancePerSol();
    	
    	PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
    		
    	// Check food capacity as range limit.
    	double foodConsumptionRate = config.getFoodConsumptionRate();
    	double foodCapacity = getInventory().getAmountResourceCapacity(AmountResource.FOOD);
    	double foodSols = foodCapacity / (foodConsumptionRate * getCrewCapacity());
    	double foodRange = distancePerSol * foodSols / LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	if (foodRange < range) range = foodRange;
    		
    	// Check water capacity as range limit.
    	double waterConsumptionRate = config.getWaterConsumptionRate();
    	double waterCapacity = getInventory().getAmountResourceCapacity(AmountResource.WATER);
    	double waterSols = waterCapacity / (waterConsumptionRate * getCrewCapacity());
    	double waterRange = distancePerSol * waterSols / LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	if (waterRange < range) range = waterRange;
    		
    	// Check oxygen capacity as range limit.
    	double oxygenConsumptionRate = config.getOxygenConsumptionRate();
    	double oxygenCapacity = getInventory().getAmountResourceCapacity(AmountResource.OXYGEN);
    	double oxygenSols = oxygenCapacity / (oxygenConsumptionRate * getCrewCapacity());
    	double oxygenRange = distancePerSol * oxygenSols / LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	if (oxygenRange < range) range = oxygenRange;
    	
    	return range;
    }
}
/**
 * Mars Simulation Project
 * Rover.java
 * @version 3.04 2013-02-02
 * @author Scott Davis
 */

package org.mars_sim.msp.core.vehicle;

import org.mars_sim.msp.core.*;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;

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
    public Rover(String name, String description, Settlement settlement) {
        // Use GroundVehicle constructor
        super(name, description, settlement);
		
		// Get vehicle configuration.
		VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
		
		// Add scope to malfunction manager.
		malfunctionManager.addScopeString("Rover");
		malfunctionManager.addScopeString("Crewable");
		malfunctionManager.addScopeString("Life Support");
		malfunctionManager.addScopeString(description);
		if (config.hasLab(description)) malfunctionManager.addScopeString("Laboratory");
		if (config.hasSickbay(description)) malfunctionManager.addScopeString("Sickbay");
        
		// Set crew capacity
		crewCapacity = config.getCrewSize(description);

		Inventory inv = getInventory();
		
		// Set inventory total mass capacity.
		inv.addGeneralCapacity(config.getTotalCapacity(description));
	
		// Set inventory resource capacities.
		AmountResource methane = AmountResource.findAmountResource("methane");
		inv.addAmountResourceTypeCapacity(methane, config.getCargoCapacity(description, "methane"));
		AmountResource oxygen = AmountResource.findAmountResource("oxygen");
		inv.addAmountResourceTypeCapacity(oxygen, config.getCargoCapacity(description, "oxygen"));
		AmountResource water = AmountResource.findAmountResource("water");
		inv.addAmountResourceTypeCapacity(water, config.getCargoCapacity(description, "water"));
		AmountResource food = AmountResource.findAmountResource("food");
		inv.addAmountResourceTypeCapacity(food, config.getCargoCapacity(description, "food"));
		AmountResource rockSamples = AmountResource.findAmountResource("rock samples");
		inv.addAmountResourceTypeCapacity(rockSamples, config.getCargoCapacity(description, "rock samples"));
		AmountResource ice = AmountResource.findAmountResource("ice");
		inv.addAmountResourceTypeCapacity(ice, config.getCargoCapacity(description, "ice"));
	
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
		catch (Exception e) { e.printStackTrace(System.err); }
    }
    
    /**
     * Sets the vehicle this rover is currently towing.
     * @param towedVehicle the vehicle being towed.
     */
    public void setTowedVehicle(Vehicle towedVehicle) {
    	if (this == towedVehicle) throw new IllegalArgumentException("Rover cannot tow itself.");
    	this.towedVehicle = towedVehicle;
    	updatedTowedVehicleSettlementLocation();
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
     * @return crewmembers as Collection
     */
    public Collection<Person> getCrew() {
        return CollectionUtils.getPerson(getInventory().getContainedUnits());
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
    public boolean lifeSupportCheck() {
        boolean result = true;

        AmountResource oxygen = AmountResource.findAmountResource("oxygen");
        if (getInventory().getAmountResourceStored(oxygen, false) <= 0D) result = false;
        AmountResource water = AmountResource.findAmountResource("water");
        if (getInventory().getAmountResourceStored(water, false) <= 0D) result = false;
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
        return crewCapacity;
    }

    /** Gets oxygen from system.
     *  @param amountRequested the amount of oxygen requested from system (kg)
     *  @return the amount of oxgyen actually received from system (kg)
     *  @throws Exception if error providing oxygen.
     */
    public double provideOxygen(double amountRequested) {
    	AmountResource oxygen = AmountResource.findAmountResource("oxygen");
    	double oxygenTaken = amountRequested;
    	double oxygenLeft = getInventory().getAmountResourceStored(oxygen, false);
    	if (oxygenTaken > oxygenLeft) oxygenTaken = oxygenLeft;
    	getInventory().retrieveAmountResource(oxygen, oxygenTaken);

        return oxygenTaken * (malfunctionManager.getOxygenFlowModifier() / 100D);
    }

    /** Gets water from system.
     *  @param amountRequested the amount of water requested from system (kg)
     *  @return the amount of water actually received from system (kg)
     *  @throws Exception if error providing water.
     */
    public double provideWater(double amountRequested) {
    	AmountResource water = AmountResource.findAmountResource("water");
    	double waterTaken = amountRequested;
    	double waterLeft = getInventory().getAmountResourceStored(water, false);
    	if (waterTaken > waterLeft) waterTaken = waterLeft;
    	getInventory().retrieveAmountResource(water, waterTaken);

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
    public void timePassing(double time) {
        super.timePassing(time);
        
        airlock.timePassing(time);
    }

    /**
     * Gets a collection of people affected by this entity.
     * @return person collection
     */
    public Collection<Person> getAffectedPeople() {
        Collection<Person> people = super.getAffectedPeople();
        
        Collection<Person> crew = getCrew();
        Iterator<Person> i = crew.iterator();
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
        return lab != null;
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
        return sickbay != null;
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
        return (operator instanceof Person) && (getInventory().containsUnit((Unit) operator));
    }
    
    /**
     * Gets the resource type that this vehicle uses for fuel.
     * @return resource type as a string
     */
    public AmountResource getFuelType() {
    	try {
    		return AmountResource.findAmountResource("methane");
    	}
    	catch (Exception e) {
    		return null;
    	}
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
    public double getRange() {
    	double range = super.getRange();
    	
    	double distancePerSol = getEstimatedTravelDistancePerSol();
    	
    	PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
    		
    	// Check food capacity as range limit.
    	AmountResource food = AmountResource.findAmountResource("food");
    	double foodConsumptionRate = config.getFoodConsumptionRate();
    	double foodCapacity = getInventory().getAmountResourceCapacity(food, false);
    	double foodSols = foodCapacity / (foodConsumptionRate * crewCapacity);
    	double foodRange = distancePerSol * foodSols / LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	if (foodRange < range) range = foodRange;
    		
    	// Check water capacity as range limit.
    	AmountResource water = AmountResource.findAmountResource("water");
    	double waterConsumptionRate = config.getWaterConsumptionRate();
    	double waterCapacity = getInventory().getAmountResourceCapacity(water, false);
    	double waterSols = waterCapacity / (waterConsumptionRate * crewCapacity);
    	double waterRange = distancePerSol * waterSols / LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	if (waterRange < range) range = waterRange;
    		
    	// Check oxygen capacity as range limit.
    	AmountResource oxygen = AmountResource.findAmountResource("oxygen");
    	double oxygenConsumptionRate = config.getOxygenConsumptionRate();
    	double oxygenCapacity = getInventory().getAmountResourceCapacity(oxygen, false);
    	double oxygenSols = oxygenCapacity / (oxygenConsumptionRate * crewCapacity);
    	double oxygenRange = distancePerSol * oxygenSols / LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	if (oxygenRange < range) range = oxygenRange;
    	
    	return range;
    }
    
    @Override
    public void setParkedLocation(double xLocation, double yLocation, double facing) {
        super.setParkedLocation(xLocation, yLocation, facing);
        
        // Update towed vehicle locations.
        updatedTowedVehicleSettlementLocation();
    }
    
    /**
     * Updates the settlement location of any towed vehicles.
     */
    private void updatedTowedVehicleSettlementLocation() {
        
        Vehicle towedVehicle = getTowedVehicle();
        if (towedVehicle != null) {
            if (towedVehicle instanceof Rover) {
                // Towed rovers should be located behind this rover with same facing.
                double distance = (getLength() + towedVehicle.getLength()) / 2D;
                double angle = getFacing() + 180D;
                double radAngle = angle * (Math.PI / 180D);
                double towedX = 0D - (distance * Math.sin(radAngle));
                double towedY = distance * Math.cos(radAngle);
                Point2D.Double towedLoc = LocalAreaUtil.getLocalRelativeLocation(towedX, towedY, this);
                towedVehicle.setParkedLocation(towedLoc.getX(), towedLoc.getY(), getFacing());
            }
            else if (towedVehicle instanceof LightUtilityVehicle) {
                // Towed light utility vehicles should be attached to back of the rover
                // sideways and facing to the right.
                double distance = (getLength() + towedVehicle.getWidth()) / 2D;
                double angle = getFacing() + 180D;
                double radAngle = angle * (Math.PI / 180D);
                double towedX = 0D - (distance * Math.sin(radAngle));
                double towedY = distance * Math.cos(radAngle);
                Point2D.Double towedLoc = LocalAreaUtil.getLocalRelativeLocation(towedX, towedY, this);
                towedVehicle.setParkedLocation(towedLoc.getX(), towedLoc.getY(), getFacing() + 90D);
            }
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        if (airlock != null) airlock.destroy();
        airlock = null;
        if (lab != null) lab.destroy();
        lab = null;
        if (sickbay != null) sickbay.destroy();
        sickbay = null;
        towedVehicle = null;
    }
}
/**
 * Mars Simulation Project
 * Settlement.java
 * @version 2.75 2003-02-20
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.*;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.simulation.malfunction.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;
import java.util.*;

/** The Settlement class represents a settlement unit on virtual Mars.
 *  It contains information related to the state of the settlement.
 */
public class Settlement extends Structure implements LifeSupport, Airlockable {

    private static final double NORMAL_AIR_PRESSURE = 1D; // Normal air pressure (atm.)
    private static final double NORMAL_TEMP = 25D;        // Normal temperature (celsius)

    // Data members
    private FacilityManager facilityManager; // The facility manager for the settlement
    protected Airlock airlock; // the settlement's airlock.
    private BuildingManager buildingManager; // The settlement's building manager.
    private PowerGrid powerGrid; // The settlement's building power grid.

    /** Constructs a Settlement object at a given location
     *  @param name the settlement's name
     *  @param location the settlement's location
     *  @param mars the virtual Mars
     */
    public Settlement(String name, Coordinates location, Mars mars) {
        // Use Unit constructor
        super(name, location, mars);
        
        // Initialize data members
        facilityManager = new FacilityManager(this, mars);
        buildingManager = new BuildingManager(this);
       
        // Initialize power grid
        powerGrid = new PowerGrid(this);
       
        // Add scope string to malfunction manager.
        malfunctionManager.addScopeString("Settlement");
        malfunctionManager.addScopeString("LifeSupport");
        
        // Set inventory total mass capacity.
        inventory.setTotalCapacity(Double.MAX_VALUE);
    
        // Set inventory resource capacities.
        SimulationProperties properties = mars.getSimulationProperties();
        double oxygenCap = properties.getSettlementOxygenStorageCapacity();
        inventory.setResourceCapacity(Resource.OXYGEN, oxygenCap);
        double waterCap = properties.getSettlementWaterStorageCapacity();
        inventory.setResourceCapacity(Resource.WATER, waterCap);
        double foodCap = properties.getSettlementFoodStorageCapacity();
        inventory.setResourceCapacity(Resource.FOOD, foodCap);
        
        inventory.setResourceCapacity(Resource.HYDROGEN, 10000D);
        inventory.setResourceCapacity(Resource.METHANE, 10000D);

        // Set random initial resources from 1/4 to total capacity.
        double oxygen = (oxygenCap / 4D) + RandomUtil.getRandomDouble(3D * oxygenCap / 4D);
        inventory.addResource(Resource.OXYGEN, oxygen); 
        double water = (waterCap / 4D) + RandomUtil.getRandomDouble(3D * waterCap / 4D);
        inventory.addResource(Resource.WATER, water); 
        double food = (foodCap / 4D) + RandomUtil.getRandomDouble(3D * foodCap / 4D);
        inventory.addResource(Resource.FOOD, food);

        // Set random initial rock samples from 0 to 500 kg.
        double rockSamples = RandomUtil.getRandomDouble(500D);
        inventory.addResource(Resource.ROCK_SAMPLES, rockSamples);

        // Create airlock for settlement.
        airlock = new Airlock(this, mars, 4);
    }
    
    /** Returns the facility manager for the settlement
     *  @return the settlement's facility manager
     */
    public FacilityManager getFacilityManager() {
        return facilityManager;
    }

    /** Returns the malfunction manager for the settlement.
     *  @return the malfunction manager
     */
    public MalfunctionManager getMalfunctionManager() {
        return malfunctionManager;
    }
    
    /** Gets the population capacity of the settlement
     *  @return the population capacity
     */
    public int getPopulationCapacity() {
        int result = 0;
        Iterator i = buildingManager.getBuildings(LivingAccommodations.class).iterator();
        while (i.hasNext()) {
            result += ((LivingAccommodations) i.next()).getAccommodationCapacity();
        }
        
        return result;
    }

    /** Gets the current population number of the settlement
     *  @return the number of inhabitants
     */
    public int getCurrentPopulationNum() {
        return getInhabitants().size();
    }

    /** Gets a collection of the inhabitants of the settlement.
     *  @return PersonCollection of inhabitants
     */
    public PersonCollection getInhabitants() {
        return inventory.getContainedUnits().getPeople();
    }
    
    /** Gets the current available population capacity
     *  of the settlement
     *  @return the available population capacity
     */
    public int getAvailablePopulationCapacity() {
        return getPopulationCapacity() - getCurrentPopulationNum();
    }

    /** Gets an array of current inhabitants of the settlement
     *  @return array of inhabitants
     */
    public Person[] getInhabitantArray() {
        PersonCollection people = getInhabitants();
        Person[] personArray = new Person[people.size()];
        PersonIterator i = people.iterator();
        int count = 0;
        while (i.hasNext()) {
            personArray[count] = i.next();
            count++;
        }
        return personArray;
    }    

    /** Gets a collection of vehicles parked at the settlement.
     *  @return VehicleCollection of parked vehicles
     */
    public VehicleCollection getParkedVehicles() {
        return inventory.getContainedUnits().getVehicles();
    }

    /** Gets the number of vehicles parked at the settlement.
     *  @return parked vehicles number
     */
    public int getParkedVehicleNum() {
        return getParkedVehicles().size();
    }
    
    /** Returns true if life support is working properly and is not out
     *  of oxygen or water.
     *  @return true if life support is OK
     */
    public boolean lifeSupportCheck() {
        boolean result = true;

        if (inventory.getResourceMass(Resource.OXYGEN) <= 0D) result = false;
        if (inventory.getResourceMass(Resource.WATER) <= 0D) result = false;
        if (getOxygenFlowModifier() < 100D) result = false;
        if (getWaterFlowModifier() < 100D) result = false;
        if (getAirPressure() != NORMAL_AIR_PRESSURE) result = false;
        if (getTemperature() != NORMAL_TEMP) result = false;
    
        return result;
    }

    /** Gets the number of people the life support can provide for.
     *  @return the capacity of the life support system
     */
    public int getLifeSupportCapacity() {
        return getPopulationCapacity();
    }
    
    /** Gets oxygen from system.
     *  @param amountRequested the amount of oxygen requested from system (kg)
     *  @return the amount of oxgyen actually received from system (kg)
     */
    public double provideOxygen(double amountRequested) {
        double oxygenProvided = inventory.removeResource(Resource.OXYGEN, amountRequested) *
            (getOxygenFlowModifier() / 100D);
        inventory.addResource(Resource.CARBON_DIOXIDE, oxygenProvided);
        return oxygenProvided;
    }

    /**
     * Gets the oxygen flow modifier for this settlement.
     * @return oxygen flow modifier
     */
    public double getOxygenFlowModifier() {

        double result = malfunctionManager.getOxygenFlowModifier();

        Iterator i = facilityManager.getFacilities();
        while (i.hasNext()) {
            Facility facility = (Facility) i.next();
            result *= (facility.getMalfunctionManager().getOxygenFlowModifier() / 100D);
        }

        return result;
    }
    
    /** Gets water from system.
     *  @param amountRequested the amount of water requested from system (kg)
     *  @return the amount of water actually received from system (kg)
     */
    public double provideWater(double amountRequested) {
        return inventory.removeResource(Resource.WATER, amountRequested) * 
            (getWaterFlowModifier() / 100D);
    }

    /**
     * Gets the water flow modifier for this settlement.
     * @return water flow modifier
     */
    public double getWaterFlowModifier() {

        double result = malfunctionManager.getWaterFlowModifier();

        Iterator i = facilityManager.getFacilities();
        while (i.hasNext()) {
            Facility facility = (Facility) i.next();
            result *= (facility.getMalfunctionManager().getWaterFlowModifier() / 100D);
        }

        return result;
    }
    
    /** Gets the air pressure of the life support system.
     *  @return air pressure (atm)
     */
    public double getAirPressure() {
        double result = NORMAL_AIR_PRESSURE * (getAirPressureModifier() / 100D);
        double ambient = mars.getWeather().getAirPressure(location);
        if (result < ambient) return ambient;
        else return result;
    }

    /**
     * Gets the air pressure modifier for this settlement.
     * @return air pressure flow modifier
     */
    public double getAirPressureModifier() {

        double result = malfunctionManager.getAirPressureModifier();

        Iterator i = facilityManager.getFacilities();
        while (i.hasNext()) {
            Facility facility = (Facility) i.next();
            result *= (facility.getMalfunctionManager().getAirPressureModifier() / 100D);
        }

        return result;
    }
    
    /** Gets the temperature of the life support system.
     *  @return temperature (degrees C)
     */
    public double getTemperature() {
        double result = NORMAL_TEMP * (getTemperatureModifier() / 100D);
        double ambient = mars.getWeather().getTemperature(location);
        if (result < ambient) return ambient;
        else return result;
    }

    /**
     * Gets the temperature modifier for this settlement.
     * @return temperature flow modifier
     */
    public double getTemperatureModifier() {

        double result = malfunctionManager.getTemperatureModifier();

        Iterator i = facilityManager.getFacilities();
        while (i.hasNext()) {
            Facility facility = (Facility) i.next();
            result *= (facility.getMalfunctionManager().getTemperatureModifier() / 100D);
        }

        return result;
    }
    
    /**
     * Gets the settlement's airlock.
     * @return settlement's airlock
     */
    public Airlock getAirlock() {
        return airlock;
    }
    
    /** 
     * Perform time-related processes
     * @param time the amount of time passing (in millisols)
     */
    public void timePassing(double time) {
        
        powerGrid.timePassing(time);
        facilityManager.timePassing(time);
        buildingManager.timePassing(time);
        airlock.timePassing(time);
        if (getCurrentPopulationNum() > 0)
            malfunctionManager.activeTimePassing(time);
        malfunctionManager.timePassing(time);
    }

    /**
     * Gets a collection of people affected by this entity.
     * @return person collection
     */
    public PersonCollection getAffectedPeople() {
        PersonCollection people = new PersonCollection(getInhabitants());

        // Check all people.
        PersonIterator i = mars.getUnitManager().getPeople().iterator();
        while (i.hasNext()) {
            Person person = i.next();
            Task task = person.getMind().getTaskManager().getTask();

            // Add all people maintaining this settlement. 
            if (task instanceof Maintenance) {
                if (((Maintenance) task).getEntity() == this) {
                    if (!people.contains(person)) people.add(person);
                }
            }

            // Add all people repairing this settlement.
            if (task instanceof Repair) {
                if (((Repair) task).getEntity() == this) {
                    if (!people.contains(person)) people.add(person);
                }
            }
        }

        return people;
    }
    
    /**
     * Gets the settlement's building manager.
     * 
     * @return building manager
     */
    public BuildingManager getBuildingManager() {
        return buildingManager;
    }
}

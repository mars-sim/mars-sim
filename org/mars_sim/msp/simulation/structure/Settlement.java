/**
 * Mars Simulation Project
 * Settlement.java
 * @version 2.76 2004-05-5
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure;

import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.task.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;
import org.mars_sim.msp.simulation.vehicle.VehicleCollection;

/** The Settlement class represents a settlement unit on virtual Mars.
 *  It contains information related to the state of the settlement.
 */
public class Settlement extends Structure implements org.mars_sim.msp.simulation.LifeSupport {

    private static final double NORMAL_AIR_PRESSURE = 1D; // Normal air pressure (atm.)
    private static final double NORMAL_TEMP = 25D;        // Normal temperature (celsius)

    // Data members
    private BuildingManager buildingManager; // The settlement's building manager.
    private PowerGrid powerGrid; // The settlement's building power grid.
    private String template; // The settlement template name.

    /** 
     * Constructs a Settlement object at a given location
     * @param name the settlement's name
     * @param template for the settlement
     * @param location the settlement's location
     * @param mars the virtual Mars
     * @throws Exception if settlement cannot be constructed.
     */
    public Settlement(String name, String template, Coordinates location, Mars mars) throws Exception { 
        // Use Unit constructor
        super(name, location, mars);
        
        this.template = template;
        
		// Set inventory total mass capacity.
		inventory.setTotalCapacity(Double.MAX_VALUE);
        
        // Initialize data members
        buildingManager = new BuildingManager(this);
       
        // Initialize power grid
        powerGrid = new PowerGrid(this);
       
        // Add scope string to malfunction manager.
        malfunctionManager.addScopeString("Settlement");
    }
    
    /** 
     * Gets the population capacity of the settlement
     * @return the population capacity
     */
    public int getPopulationCapacity() {
        int result = 0;
        Iterator i = buildingManager.getBuildings(LivingAccommodations.NAME).iterator();
        while (i.hasNext()) {
        	try {
        		Building building = (Building) i.next();
        		LivingAccommodations livingAccommodations = 
        			(LivingAccommodations) building.getFunction(LivingAccommodations.NAME);
        		result += livingAccommodations.getBeds();
        	} 
        	catch (BuildingException e) {}
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
        return malfunctionManager.getOxygenFlowModifier();
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
        return malfunctionManager.getWaterFlowModifier();
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
        return malfunctionManager.getAirPressureModifier();
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
        return malfunctionManager.getTemperatureModifier();
    }
    
    /** 
     * Perform time-related processes
     * @param time the amount of time passing (in millisols)
     * @throws Exception if error during time passing.
     */
    public void timePassing(double time) throws Exception {
        
		// If settlement is overcrowded, increase inhabitant's stress.
		int overCrowding = getCurrentPopulationNum() - getPopulationCapacity();
		if (overCrowding > 0) {
			System.out.println("Overcrowding at " + getName());
			double stressModifier = .1D * overCrowding * time;
			PersonIterator i = getInhabitants().iterator();
			while (i.hasNext()) {
				PhysicalCondition condition = i.next().getPhysicalCondition();
				condition.setStress(condition.getStress() + stressModifier);
			}
		}
        
        try {
        	// If no current population at settlement, power down buildings.
        	if (getCurrentPopulationNum() == 0) {
        		getPowerGrid().setPowerMode(PowerGrid.POWER_DOWN_MODE);
        	}
        	else {
        		getPowerGrid().setPowerMode(PowerGrid.POWER_UP_MODE);
        	}
        
        	powerGrid.timePassing(time);
        	buildingManager.timePassing(time);
        	if (getCurrentPopulationNum() > 0) malfunctionManager.activeTimePassing(time);
        	malfunctionManager.timePassing(time);
        }
        catch (Exception e) {
        	throw new Exception("Settlement " + getName() + " timePassing(): " + e.getMessage());
        }
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
    
    /**
     * Gets an available airlock for the settlement.
     *
     * @return airlock or null if none available.
     */
    public Airlock getAvailableAirlock() {
        Airlock result = null;
        List airlocks = new ArrayList();
        
        Iterator i = buildingManager.getBuildings(EVA.NAME).iterator();
        while (i.hasNext()) {
        	try {
        		Building building = (Building) i.next();
        		EVA eva = (EVA) building.getFunction(EVA.NAME);
        		airlocks.add(eva.getAirlock());
        	}
        	catch (BuildingException e) {}
        }
        
        if (airlocks.size() > 0) {
            // Pick random airlock from list.
            int rand = RandomUtil.getRandomInt(airlocks.size() - 1);
            result = (Airlock) airlocks.get(rand);
        }
        
        return result;
    }
    
    /**
     * Gets the settlement's power grid.
     * @return the power grid.
     */
    public PowerGrid getPowerGrid() {   
        return powerGrid;
    }
    
    /**
     * Gets the settlement template.
     * @return template as string.
     */
    public String getTemplate() {
    	return template;
    }
}

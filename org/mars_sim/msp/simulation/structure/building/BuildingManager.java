/**
 * Mars Simulation Project
 * BuildingManager.java
 * @version 2.75 2004-04-06
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.function.*;
import org.mars_sim.msp.simulation.vehicle.*;

/**
 * The BuildingManager manages the settlement's buildings.
 */
public class BuildingManager implements Serializable {
    
    private Settlement settlement; // The manager's settlement.
    private List buildings; // The settlement's buildings.
    
    /**
     * Constructor
     * @param settlement - the manager's settlement.
     */
    public BuildingManager(Settlement settlement) {
        
        this.settlement = settlement;
        buildings = new ArrayList();
    }
    
    /**
     * Gets the building manager's settlement.
     *
     * @return settlement
     */
    public Settlement getSettlement() {
        return settlement;
    }
    
    /**
     * Adds a new building to the settlement.
     *
     * @param newBuilding the building to add.
     */
    public void addBuilding(Building newBuilding) {
        if (!buildings.contains(newBuilding)) buildings.add(newBuilding);
    }
    
    /**
     * Gets the settlement's collection of buildings.
     *
     * @return collection of buildings
     */
    public List getBuildings() {
        return buildings;
    }
    
    /**
     * Gets the buildings in a settlement that has a given function.
     * @param functionName the name of the building.
     * @return list of buildings.
     */
    public List getBuildings(String functionName) {
    	List functionBuildings = new ArrayList();
    	Iterator i = buildings.iterator();
    	while (i.hasNext()) {
    		Building building = (Building) i.next();
    		if (building.hasFunction(functionName)) functionBuildings.add(building);
    	}
    	return functionBuildings;
    }
    
    /**
     * Gets the number of buildings at the settlement.
     *
     * @return number of buildings
     */
    public int getBuidingNum() {
        return buildings.size();
    }
    
    /**
     * Time passing for all buildings.
     *
     * @param time amount of time passing (in millisols)
     * @throws Exception if error.
     */
    public void timePassing(double time) throws Exception {
    	try {
        	Iterator i = buildings.iterator();
        	while (i.hasNext()) ((Building) i.next()).timePassing(time);
    	}
    	catch (BuildingException e) {
    		throw new Exception("BuildingManager.timePassing(): " + e.getMessage());
    	}
    }   

    /**
     * Adds a person to a random inhabitable building within a settlement.
     *
     * @param person the person to add.
     * @param settlement the settlement to find a building.
     * @throw BuildingException if person cannot be added to any building.
     */
    public static void addToRandomBuilding(Person person, Settlement settlement) throws BuildingException {
        
        List habs = settlement.getBuildingManager().getBuildings(LifeSupport.NAME);
        
        int rand = RandomUtil.getRandomInt(habs.size() - 1);
        
        Building building = null;
        int count = 0;
        Iterator i = habs.iterator();
        while (i.hasNext()) {
            Building hab = (Building) i.next();
            if (count == rand) building = hab;
            count++;
        }
        
        if (building != null) {
        	try {
        		LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
        		lifeSupport.addPerson(person); 
        	}
        	catch (Exception e) {
        		throw new BuildingException("Could not add person to building: " + e.getMessage());
        	}
        }
        else throw new BuildingException("No inhabitable buildings available for " + person.getName());
    }
    
    /**
     * Adds a ground vehicle to a random ground vehicle maintenance building within a settlement.
     * @param vehicle the ground vehicle to add.
     * @param settlement the settlement to find a building.
     * @throw BuildingException if vehicle cannot be added to any building.
     */
    public static void addToRandomBuilding(GroundVehicle vehicle, Settlement settlement) throws BuildingException {
        
        Collection garages = settlement.getBuildingManager().getBuildings(GroundVehicleMaintenance.NAME);
        List openGarages = new ArrayList();
        Iterator i = garages.iterator();
        while (i.hasNext()) {
            VehicleMaintenance garage = (VehicleMaintenance) i.next();
            double availableSpace = garage.getVehicleCapacity() - garage.getCurrentVehicleMass();
            if (availableSpace >= vehicle.getMass()) openGarages.add(garage);
        }
        
        if (openGarages.size() > 0) {
            int rand = RandomUtil.getRandomInt(openGarages.size() - 1);
            ((VehicleMaintenance) openGarages.get(rand)).addVehicle(vehicle);
        }
        else {
            throw new BuildingException("No available garage space for " + vehicle.getName());
        }
    }
        
    /**
     * Gets the building a given person is in.
     *
     * @return building or null if none.
     */
    public static Building getBuilding(Person person) {
        
        Building result = null;
        
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
            Iterator i = settlement.getBuildingManager().getBuildings(LifeSupport.NAME).iterator();
            while (i.hasNext()) {
            	Building building = (Building) i.next();
            	try {
                	LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
                	if (lifeSupport.containsPerson(person)) result = building;
            	}
            	catch (Exception e) {
            		System.err.println("BuildingManager.getBuilding(): " + e.getMessage());
            	}
            }
        }
        
        return result;
    }
    
    /**
     * Gets the vehicle maintenance building a given vehicle is in.
     *
     * @return building or null if none.
     */
    public static Building getBuilding(Vehicle vehicle) {
        
        Building result = null;
        
        Settlement settlement = vehicle.getSettlement();
        if (settlement != null) {
            Iterator i = settlement.getBuildingManager().getBuildings(GroundVehicleMaintenance.NAME).iterator();
            while (i.hasNext()) {
                Building garageBuilding = (Building) i.next();
                try {
                	VehicleMaintenance garage = (VehicleMaintenance) garageBuilding.getFunction(GroundVehicleMaintenance.NAME);
                	if (garage.containsVehicle(vehicle)) result = garageBuilding;
                }
                catch (Exception e) {
                	System.err.println("BuildingManager.getBuilding(): " + e.getMessage());
                }
            }
        }
        
        return result;
    }
}
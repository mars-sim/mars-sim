/**
 * Mars Simulation Project
 * BuildingManager.java
 * @version 2.76 2004-06-02
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.*;
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
     * @throws Exception if buildings cannot be constructed.
     */
    public BuildingManager(Settlement settlement) throws Exception {
        
        this.settlement = settlement;
        
        // Construct all buildings at settlement based on template.
        buildings = new ArrayList();
        
		SimulationConfig simConfig = Simulation.instance().getSimConfig();
        SettlementConfig config = simConfig.getSettlementConfiguration();
        
        Iterator i = config.getTemplateBuildingTypes(settlement.getTemplate()).iterator();
        while (i.hasNext()) {
        	String buildingType = (String) i.next();
        	buildings.add(new Building(buildingType, this));
        }
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
        List goodHabs = getLeastCrowdedBuildings(habs);
        
        int rand = RandomUtil.getRandomInt(goodHabs.size() - 1);
        
        Building building = null;
        int count = 0;
        Iterator i = goodHabs.iterator();
        while (i.hasNext()) {
            Building hab = (Building) i.next();
            if (count == rand) building = hab;
            count++;
        }
        
        if (building != null) addPersonToBuilding(person, building);
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
    
    /**
     * Gets a list of uncrowded buildings from a given list of buildings with life support.
     * @param buildingList list of buildings with the life support function.
     * @return list of buildings that are not at or above maximum occupant capacity.
     * @throws BuildingException if building in list does not have the life support function.
     */
    public static List getUncrowdedBuildings(List buildingList) throws BuildingException {
    	List result = new ArrayList();
    	
    	try {
    		Iterator i = buildingList.iterator();
    		while (i.hasNext()) {
    			Building building = (Building) i.next();
				LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
				if (lifeSupport.getAvailableOccupancy() > 0) result.add(building);
    		}
    	}
    	catch (ClassCastException e) {
    		throw new BuildingException("BuildingManager.getUncrowdedBuildings(): building isn't a life support building.");
    	}
    	
    	return result;
    }
    
    /**
     * Gets a list of the least crowded buildings from a given list of buildings with life support.
     * @param buildingList list of buildings with the life support function.
     * @return list of least crowded buildings.
     * @throws BuildingException if building in list does not have the life support function.
     */
    public static List getLeastCrowdedBuildings(List buildingList) throws BuildingException {
    	List result = new ArrayList();
    	
    	try {
    		// Find least crowded population.
    		int leastCrowded = Integer.MAX_VALUE;
    		Iterator i = buildingList.iterator();
    		while (i.hasNext()) {
    			Building building = (Building) i.next();
				LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
				int crowded = lifeSupport.getOccupantNumber() - lifeSupport.getOccupantCapacity();
				if (crowded < 0) crowded = 0;
				if (crowded < leastCrowded) leastCrowded = crowded;
			}
			
			// Add least crowded buildings to list.
			Iterator j = buildingList.iterator();
			while (j.hasNext()) {
				Building building = (Building) j.next();
				LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
				int crowded = lifeSupport.getOccupantNumber() - lifeSupport.getOccupantCapacity();
				if (crowded < 0) crowded = 0;
				if (crowded == leastCrowded) result.add(building);
			}
		}
		catch (ClassCastException e) {
			throw new BuildingException("BuildingManager.getUncrowdedBuildings(): building isn't a life support building.");
		}
    	
    	return result;
    }
    
    /**
     * Gets a list of buildings that don't have any malfunctions from a list of buildings.
     * @param buildingList the list of buildings.
     * @return list of buildings without malfunctions.
     */
    public static List getNonMalfunctioningBuildings(List buildingList) {
    	List result = new ArrayList();
    	
    	Iterator i = buildingList.iterator();
    	while (i.hasNext()) {
    		Building building = (Building) i.next();
			boolean malfunction = building.getMalfunctionManager().hasMalfunction();
			if (!malfunction) result.add(building);
    	}
    	
    	return result;
    }
    
    /**
     * Adds the person to the building if possible.
     * @param person the person to add.
     * @param building the building to add the person to.
     * @throws BuildingException if person could not be added to the building.
     */
    public static void addPersonToBuilding(Person person, Building building) throws BuildingException {
		if (building != null) {
			try {
				LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
				if (!lifeSupport.containsPerson(person)) lifeSupport.addPerson(person); 
			}
			catch (Exception e) {
				throw new BuildingException("BuildingManager.addPersonToBuilding(): " + e.getMessage());
			}
		}
		else throw new BuildingException("Building is null");
    }
}
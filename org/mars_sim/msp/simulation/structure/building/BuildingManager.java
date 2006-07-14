/**
 * Mars Simulation Project
 * BuildingManager.java
 * @version 2.79 2006-03-25
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.social.RelationshipManager;
import org.mars_sim.msp.simulation.resource.AmountResource;
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
     * Constructor to construct buildings from settlement config template.
     * @param settlement the manager's settlement.
     * @throws Exception if buildings cannot be constructed.
     */
    public BuildingManager(Settlement settlement) throws Exception {
        this(settlement, Simulation.instance().getSimConfig().getSettlementConfiguration()
        		.getTemplateBuildingTypes(settlement.getTemplate()));
    }
    
    /**
     * Constructor to construct buildings from name list.
     * @param settlement the manager's settlement
     * @param buildingNames the names of the settlement's buildings.
     * @throws Exception if buildings cannot be constructed.
     */
    public BuildingManager(Settlement settlement, List buildingNames) throws Exception {
    	
    	this.settlement = settlement;
    	
    	// Construct all buildings in the settlement.
    	buildings = new ArrayList();
    	if (buildingNames != null) {
    		Iterator i = buildingNames.iterator();
    		while (i.hasNext()) {
    			String buildingType = (String) i.next();
    			addBuilding(buildingType);
    		}
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
     * Adds a building of a specific building type to the settlement.
     * @param buildingType the type of building.
     * @throws Exception if error creating or adding building.
     */
    public void addBuilding(String buildingType) throws Exception {
    	Building newBuilding = new Building(buildingType, this);
    	addBuilding(newBuilding);
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
    public int getBuildingNum() {
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
     * @throws BuildingException if person cannot be added to any building.
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
     * @throws BuildingException if vehicle cannot be added to any building.
     */
    public static void addToRandomBuilding(GroundVehicle vehicle, Settlement settlement) throws BuildingException {
        
        Collection garages = settlement.getBuildingManager().getBuildings(GroundVehicleMaintenance.NAME);
        List openGarages = new ArrayList();
        Iterator i = garages.iterator();
        while (i.hasNext()) {
        	Building garageBuilding = (Building) i.next();
            VehicleMaintenance garage = (VehicleMaintenance) garageBuilding.getFunction(GroundVehicleMaintenance.NAME);
            if (garage.getCurrentVehicleNumber() < garage.getVehicleCapacity()) openGarages.add(garage);
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
    	if (vehicle == null) throw new IllegalArgumentException("vehicle is null");
        
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
				if (crowded < -1) crowded = -1;
				if (crowded < leastCrowded) leastCrowded = crowded;
			}
			
			// Add least crowded buildings to list.
			Iterator j = buildingList.iterator();
			while (j.hasNext()) {
				Building building = (Building) j.next();
				LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
				int crowded = lifeSupport.getOccupantNumber() - lifeSupport.getOccupantCapacity();
				if (crowded < -1) crowded = -1;
				if (crowded == leastCrowded) result.add(building);
			}
		}
		catch (ClassCastException e) {
			throw new BuildingException("BuildingManager.getUncrowdedBuildings(): building isn't a life support building.");
		}
    	
    	return result;
    }
    
    /**
     * Gets a list of buildings with the best relationships for a given person from a list of buildings.
     * @param person the person to check for.
     * @param buildingList the list of buildings to filter.
     * @return list of buildings with the best relationships.
     * @throws BuildingException if building in list does not have the life support function.if building in list does not have the life support function.
     */
    public static List getBestRelationshipBuildings(Person person, List buildingList) throws BuildingException {
    	List result = new ArrayList();
    	
    	RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
    	
		try {
			// Find best relationship buildings.
			double bestRelationships = Double.NEGATIVE_INFINITY;
			Iterator i = buildingList.iterator();
			while (i.hasNext()) {
				Building building = (Building) i.next();
				LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
				double buildingRelationships = 0D;
				PersonIterator j = lifeSupport.getOccupants().iterator();
				while (j.hasNext()) {
					Person occupant = j.next();
					if (person != occupant) buildingRelationships+= (relationshipManager.getOpinionOfPerson(person, occupant) - 50D);
				} 
				if (buildingRelationships > bestRelationships) bestRelationships = buildingRelationships;
			}
			
			// Add bestRelationships buildings to list.
			i = buildingList.iterator();
			while (i.hasNext()) {
				Building building = (Building) i.next();
				LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
				double buildingRelationships = 0D;
				PersonIterator j = lifeSupport.getOccupants().iterator();
				while (j.hasNext()) {
					Person occupant = j.next();
					if (person != occupant) buildingRelationships+= (relationshipManager.getOpinionOfPerson(person, occupant) - 50D);
				} 
				if (buildingRelationships == bestRelationships) result.add(building);
			}
		}
		catch (ClassCastException e) {
			throw new BuildingException("BuildingManager.getBestRelationshipBuildings(): building isn't a life support building.");
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
    
	/**
	 * Adds the amount resources in the addition map to the starting map.
	 * @param startingMap map of amount resources and their Double values.
	 * @param additionMap map of amount resources and their Double values.
	 */
	public static void sumAmountResourceChanges(Map startingMap, Map additionMap) {
		Iterator i = additionMap.keySet().iterator();
		while (i.hasNext()) {
			AmountResource resource = (AmountResource) i.next();
			double additionValue = ((Double) additionMap.get(resource)).doubleValue();
			if (startingMap.containsKey(resource)) {
				double startingValue = ((Double) startingMap.get(resource)).doubleValue();
				startingMap.put(resource, new Double(startingValue + additionValue));
			}
			else startingMap.put(resource, new Double(additionValue));
		}
	}
}
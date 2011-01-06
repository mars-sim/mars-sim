/**
 * Mars Simulation Project
 * BuildingManager.java
 * @version 3.00 2010-08-25
 * @author Scott Davis
 */
 
package org.mars_sim.msp.core.structure.building;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.function.*;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The BuildingManager manages the settlement's buildings.
 */
public class BuildingManager implements Serializable {
    
    private static String CLASS_NAME = 
	"org.mars_sim.msp.simulation.structure.building.BuildingManager";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);
    
	// Unit update events.
	public static final String ADD_BUILDING_EVENT = "add building";
	public static final String REMOVE_BUILDING_EVENT = "remove building";
	
    // Data members
    private Settlement settlement; // The manager's settlement.
    private List<Building> buildings; // The settlement's buildings.
    private Map<String, Double> buildingValuesNewCache;
    private Map<String, Double> buildingValuesOldCache;
    private MarsClock lastBuildingValuesUpdateTime;
    
    /**
     * Constructor to construct buildings from settlement config template.
     * @param settlement the manager's settlement.
     * @throws Exception if buildings cannot be constructed.
     */
    public BuildingManager(Settlement settlement) {
        this(settlement, SimulationConfig.instance().getSettlementConfiguration().
                getSettlementTemplate(settlement.getTemplate()).getBuildingTemplates());
    }
    
    /**
     * Constructor to construct buildings from name list.
     * @param settlement the manager's settlement
     * @param buildingTemplates the settlement's building templates.
     * @throws Exception if buildings cannot be constructed.
     */
    public BuildingManager(Settlement settlement, List<BuildingTemplate> buildingTemplates) 
{
    	
    	this.settlement = settlement;
    	
    	// Construct all buildings in the settlement.
    	buildings = new ArrayList<Building>();
    	if (buildingTemplates != null) {
    		Iterator<BuildingTemplate> i = buildingTemplates.iterator();
    		while (i.hasNext()) {
    		    BuildingTemplate template = i.next();
    			addBuilding(template);
    		}
        }
        
        // Initialize building value caches.
        buildingValuesNewCache = new HashMap<String, Double>();
        buildingValuesOldCache = new HashMap<String, Double>();
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
        if (!buildings.contains(newBuilding)) {
        	buildings.add(newBuilding);
        	settlement.fireUnitUpdate(ADD_BUILDING_EVENT, newBuilding);
        }
    }
    
    /**
     * Removes a building from the settlement.
     * @param oldBuilding the building to remove.
     */
    public void removeBuilding(Building oldBuilding) {
        if (buildings.contains(oldBuilding)) {
            buildings.remove(oldBuilding);
            settlement.fireUnitUpdate(REMOVE_BUILDING_EVENT, oldBuilding);
        }
    }
    
    /**
     * Adds a building with a template to the settlement.
     * @param template the building template.
     * @throws Exception if error creating or adding building.
     */
    public void addBuilding(BuildingTemplate template) {
    	Building newBuilding = new Building(template, this);
    	addBuilding(newBuilding);
    }
    
    /**
     * Gets the settlement's collection of buildings.
     *
     * @return collection of buildings
     */
    public List<Building> getBuildings() {
        return new ArrayList<Building>(buildings);
    }
    
    /**
     * Gets the buildings in a settlement that has a given function.
     * @param functionName the name of the building.
     * @return list of buildings.
     */
    public List<Building> getBuildings(String functionName) {
    	List<Building> functionBuildings = new ArrayList<Building>();
    	Iterator<Building> i = buildings.iterator();
    	while (i.hasNext()) {
    		Building building = i.next();
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
    public void timePassing(double time) {
//    	try {
        	Iterator<Building> i = buildings.iterator();
        	while (i.hasNext()) i.next().timePassing(time);
//    	}
//    	catch (BuildingException e) {
//    		throw new IllegalStateException("BuildingManager.timePassing(): " + e.getMessage());
//    	}
    }   

    /**
     * Adds a person to a random inhabitable building within a settlement.
     *
     * @param person the person to add.
     * @param settlement the settlement to find a building.
     * @throws BuildingException if person cannot be added to any building.
     */
    public static void addToRandomBuilding(Person person, Settlement settlement) {
        
        List<Building> habs = settlement.getBuildingManager().getBuildings(LifeSupport.NAME);
        List<Building> goodHabs = getLeastCrowdedBuildings(habs);
        
        int rand = RandomUtil.getRandomInt(goodHabs.size() - 1);
        
        Building building = null;
        int count = 0;
        Iterator<Building> i = goodHabs.iterator();
        while (i.hasNext()) {
        	Building tempBuilding = i.next();
            if (count == rand) building = tempBuilding;
            count++;
        }
        
        if (building != null) addPersonToBuilding(person, building);
        else throw new IllegalStateException("No inhabitable buildings available for " + person.getName());
    }
    
    /**
     * Adds a ground vehicle to a random ground vehicle maintenance building within a settlement.
     * @param vehicle the ground vehicle to add.
     * @param settlement the settlement to find a building.
     * @throws BuildingException if vehicle cannot be added to any building.
     */
    public static void addToRandomBuilding(GroundVehicle vehicle, Settlement settlement) {
        
        List<Building> garages = settlement.getBuildingManager().getBuildings(GroundVehicleMaintenance.NAME);
        List<VehicleMaintenance> openGarages = new ArrayList<VehicleMaintenance>();
        Iterator<Building> i = garages.iterator();
        while (i.hasNext()) {
        	Building garageBuilding = i.next();
            VehicleMaintenance garage = (VehicleMaintenance) garageBuilding.getFunction(GroundVehicleMaintenance.NAME);
            if (garage.getCurrentVehicleNumber() < garage.getVehicleCapacity()) openGarages.add(garage);
        }
        
        if (openGarages.size() > 0) {
            int rand = RandomUtil.getRandomInt(openGarages.size() - 1);
            openGarages.get(rand).addVehicle(vehicle);
        }
        else {
            logger.warning("No available garage space for " + vehicle.getName() + ", didn't add vehicle");
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
            Iterator<Building> i = settlement.getBuildingManager().getBuildings(LifeSupport.NAME).iterator();
            while (i.hasNext()) {
            	Building building = i.next();
            	try {
                	LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
                	if (lifeSupport.containsPerson(person)) result = building;
            	}
            	catch (Exception e) {
            		logger.log(Level.SEVERE,"BuildingManager.getBuilding(): " + e.getMessage());
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
            Iterator<Building> i = settlement.getBuildingManager().getBuildings(GroundVehicleMaintenance.NAME).iterator();
            while (i.hasNext()) {
                Building garageBuilding = i.next();
                try {
                	VehicleMaintenance garage = (VehicleMaintenance) garageBuilding.getFunction(GroundVehicleMaintenance.NAME);
                	if (garage.containsVehicle(vehicle)) result = garageBuilding;
                }
                catch (Exception e) {
                	logger.log(Level.SEVERE,"BuildingManager.getBuilding(): " + e.getMessage());
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
    public static List<Building> getUncrowdedBuildings(List<Building> buildingList) 
            {
    	List<Building> result = new ArrayList<Building>();
    	
    	try {
    		Iterator<Building> i = buildingList.iterator();
    		while (i.hasNext()) {
    			Building building = i.next();
				LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
				if (lifeSupport.getAvailableOccupancy() > 0) result.add(building);
    		}
    	}
    	catch (ClassCastException e) {
    		throw new IllegalStateException("BuildingManager.getUncrowdedBuildings(): " +
                    "building isn't a life support building.");
    	}
    	
    	return result;
    }
    
    /**
     * Gets a list of the least crowded buildings from a given list of buildings with life support.
     * @param buildingList list of buildings with the life support function.
     * @return list of least crowded buildings.
     * @throws BuildingException if building in list does not have the life support function.
     */
    public static List<Building> getLeastCrowdedBuildings(List<Building> buildingList) 
             {
    	List<Building> result = new ArrayList<Building>();
    	
//    	try {
    		// Find least crowded population.
    		int leastCrowded = Integer.MAX_VALUE;
    		Iterator<Building> i = buildingList.iterator();
    		while (i.hasNext()) {
				LifeSupport lifeSupport = (LifeSupport) i.next().getFunction(LifeSupport.NAME);
				int crowded = lifeSupport.getOccupantNumber() - lifeSupport.getOccupantCapacity();
				if (crowded < -1) crowded = -1;
				if (crowded < leastCrowded) leastCrowded = crowded;
			}
			
			// Add least crowded buildings to list.
			Iterator<Building> j = buildingList.iterator();
			while (j.hasNext()) {
				Building building = j.next();
				LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
				int crowded = lifeSupport.getOccupantNumber() - lifeSupport.getOccupantCapacity();
				if (crowded < -1) crowded = -1;
				if (crowded == leastCrowded) result.add(building);
			}
//		}
//		catch (ClassCastException e) {
//			throw new BuildingException("BuildingManager.getLeastCrowdedBuildings(): " +
//                    "building isn't a life support building.");
//		}
    	
    	return result;
    }
    
    /**
     * Gets a list of buildings with the best relationships for a given person from a list of buildings.
     * @param person the person to check for.
     * @param buildingList the list of buildings to filter.
     * @return list of buildings with the best relationships.
     * @throws BuildingException if building in list does not have the life support function.if building in list does not have the life support function.
     */
    public static List<Building> getBestRelationshipBuildings(Person person, List<Building> buildingList) {
    	List<Building> result = new ArrayList<Building>();
    	
    	RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
    	
//		try {
			// Find best relationship buildings.
			double bestRelationships = Double.NEGATIVE_INFINITY;
			Iterator<Building> i = buildingList.iterator();
			while (i.hasNext()) {
				LifeSupport lifeSupport = (LifeSupport) i.next().getFunction(LifeSupport.NAME);
				double buildingRelationships = 0D;
				Iterator<Person> j = lifeSupport.getOccupants().iterator();
				while (j.hasNext()) {
					Person occupant = j.next();
					if (person != occupant) buildingRelationships+= (relationshipManager.getOpinionOfPerson(person, occupant) - 50D);
				} 
				if (buildingRelationships > bestRelationships) bestRelationships = buildingRelationships;
			}
			
			// Add bestRelationships buildings to list.
			i = buildingList.iterator();
			while (i.hasNext()) {
				Building building = i.next();
				LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
				double buildingRelationships = 0D;
				Iterator<Person> j = lifeSupport.getOccupants().iterator();
				while (j.hasNext()) {
					Person occupant = j.next();
					if (person != occupant) buildingRelationships+= (relationshipManager.getOpinionOfPerson(person, occupant) - 50D);
				} 
				if (buildingRelationships == bestRelationships) result.add(building);
			}
//		}
//		catch (ClassCastException e) {
//			throw new BuildingException("BuildingManager.getBestRelationshipBuildings(): building isn't a life support building.");
//		}
    	
    	return result;
    }
    
    /**
     * Gets a list of buildings that don't have any malfunctions from a list of buildings.
     * @param buildingList the list of buildings.
     * @return list of buildings without malfunctions.
     */
    public static List<Building> getNonMalfunctioningBuildings(List<Building> buildingList) {
    	List<Building> result = new ArrayList<Building>();
    	
    	Iterator<Building> i = buildingList.iterator();
    	while (i.hasNext()) {
    		Building building = i.next();
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
    public static void addPersonToBuilding(Person person, Building building)  {
		if (building != null) {
			try {
				LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
				if (!lifeSupport.containsPerson(person)) lifeSupport.addPerson(person); 
			}
			catch (Exception e) {
				throw new IllegalStateException("BuildingManager.addPersonToBuilding(): " + e.getMessage());
			}
		}
		else throw new IllegalStateException("Building is null");
    }
    
    /**
     * Gets the value of a named building at the settlement.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @return building value (VP).
     * @throws Exception if error getting building value.
     */
    public double getBuildingValue(String buildingName, boolean newBuilding) {
        
        // Update building values cache once per Sol.
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        if ((lastBuildingValuesUpdateTime == null) || 
                (MarsClock.getTimeDiff(currentTime, lastBuildingValuesUpdateTime) > 1000D)) {
            buildingValuesNewCache.clear();
            buildingValuesOldCache.clear();
            lastBuildingValuesUpdateTime = (MarsClock) currentTime.clone();
        }
        
        if (newBuilding && buildingValuesNewCache.containsKey(buildingName)) 
            return buildingValuesNewCache.get(buildingName);
        else if (!newBuilding && buildingValuesOldCache.containsKey(buildingName))
            return buildingValuesOldCache.get(buildingName);
        else {
            double result = 0D;
        
            // Determine value of all building functions.
            BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
            if (config.hasCommunication(buildingName))
                result += Communication.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasCooking(buildingName))
                result += Cooking.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasDining(buildingName))
                result += Dining.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasEVA(buildingName))
                result += EVA.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasExercise(buildingName))
                result += Exercise.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasFarming(buildingName))
                result += Farming.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasGroundVehicleMaintenance(buildingName))
                result += GroundVehicleMaintenance.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasLifeSupport(buildingName))
                result += LifeSupport.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasLivingAccommodations(buildingName))
                result += LivingAccommodations.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasManufacture(buildingName))
                result += Manufacture.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasMedicalCare(buildingName))
                result += MedicalCare.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasPowerGeneration(buildingName)) 
                result += PowerGeneration.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasPowerStorage(buildingName))
                result += PowerStorage.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasRecreation(buildingName))
                result += Recreation.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasResearchLab(buildingName))
                result += Research.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasResourceProcessing(buildingName))
                result += ResourceProcessing.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasStorage(buildingName))
                result += Storage.getFunctionValue(buildingName, newBuilding, settlement);
            if (config.hasAstronomicalObservation(buildingName))
                result += AstronomicalObservation.getFunctionValue(buildingName, newBuilding, settlement);
        
            // Multiply value.
            result *= 1000D;
        
            // Subtract power costs per Sol.
            double power = config.getBasePowerRequirement(buildingName);
            double hoursInSol = MarsClock.convertMillisolsToSeconds(1000D) / 60D / 60D;
            double powerPerSol = power * hoursInSol;
            double powerValue = powerPerSol * settlement.getPowerGrid().getPowerValue();
            result -= powerValue;
        
            if (result < 0D) result = 0D;
        
            if (newBuilding) buildingValuesNewCache.put(buildingName, result);
            else buildingValuesOldCache.put(buildingName, result);
            
            return result;
        }
    }
    
    /**
     * Gets the value of a building at the settlement.
     * @param building the building.
     * @return building value (VP).
     * @throws Exception if error getting building value.
     */
    public double getBuildingValue(Building building) {
        double result = 0D;
        
        result = getBuildingValue(building.getName(), false);
        
        // Modify building value by its wear condition.
        double wearCondition = building.getMalfunctionManager().getWearCondition();
        result *= (wearCondition / 100D) * .75D + .25D;
        
        return result;
    }
}
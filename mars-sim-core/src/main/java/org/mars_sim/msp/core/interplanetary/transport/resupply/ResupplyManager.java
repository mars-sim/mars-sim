/**
 * Mars Simulation Project
 * ResupplyManager.java
 * @version 3.02 2012-04-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.resupply;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages resupply missions from Earth.
 */
public class ResupplyManager implements Serializable {
	
    // Average transit time for supplies from Earth to Mars (sols).
    public static int AVG_TRANSIT_TIME = 200;
    
	// Data members
	private Collection<Resupply> resupplies;
	
	/**
	 * Constructor
	 */
	public ResupplyManager() {
		
		//Initialize data
		resupplies = new ConcurrentLinkedQueue<Resupply>();
		
		// Create initial resupply missions.
		createInitialResupplyMissions();
	}
	
	/**
	 * Create the initial resupply missions from the configuration XML files.
	 */
	private void createInitialResupplyMissions() {
	    
	    SettlementConfig settlementConfig = SimulationConfig.instance().getSettlementConfiguration();
        Iterator<Settlement> i = Simulation.instance().getUnitManager().getSettlements().iterator();
        while (i.hasNext()) {
            Settlement settlement = i.next();
            String templateName = settlement.getTemplate();
            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
            
            Iterator<ResupplyMissionTemplate> j = 
                settlementConfig.getSettlementTemplate(templateName).getResupplyMissionTemplates().iterator();
            while (j.hasNext()) {
                ResupplyMissionTemplate template = j.next();
                MarsClock arrivalDate = (MarsClock) currentTime.clone();
                arrivalDate.addTime(template.getArrivalTime() * 1000D);
                Resupply resupply = new Resupply(arrivalDate, settlement);
                
                // Determine launch date.
                MarsClock launchDate = (MarsClock) arrivalDate.clone();
                launchDate.addTime(-1D * AVG_TRANSIT_TIME * 1000D);
                resupply.setLaunchDate(launchDate);
                
                // Set resupply state based on launch and arrival time.
                String state = Resupply.PLANNED;
                if (MarsClock.getTimeDiff(currentTime, launchDate) >= 0D) {
                    state = Resupply.IN_TRANSIT;
                    if (MarsClock.getTimeDiff(currentTime, arrivalDate) >= 0D) {
                        state = Resupply.DELIVERED;
                    }
                }
                resupply.setState(state);
                
                // Get resupply info from the config file.
                ResupplyConfig resupplyConfig = SimulationConfig.instance().getResupplyConfiguration();
                String resupplyName = template.getName();
                
                // Get new building types.
                resupply.setNewBuildings(resupplyConfig.getResupplyBuildings(resupplyName));
                    
                // Get new vehicle types.
                resupply.setNewVehicles(resupplyConfig.getResupplyVehicleTypes(resupplyName));
                
                // Get new equipment types.
                resupply.setNewEquipment(resupplyConfig.getResupplyEquipment(resupplyName));
                    
                // Get number of new immigrants.
                resupply.setNewImmigrantNum(resupplyConfig.getNumberOfResupplyImmigrants(resupplyName));
                    
                // Get new resources map.
                resupply.setNewResources(resupplyConfig.getResupplyResources(resupplyName));
                
                // Get new parts map.
                resupply.setNewParts(resupplyConfig.getResupplyParts(resupplyName));
                
                resupplies.add(resupply);
            }
        }
	}
	
	/**
	 * Gets all of the settlement resupply missions.
	 * @return list of all resupply missions.
	 */
	public List<Resupply> getAllResupplies() {
		return new ArrayList<Resupply>(resupplies);
	}
	
	/**
	 * Gets the resupply missions that are planned or in transit.
	 * @return resupply missions.
	 */
	public List<Resupply> getIncomingResupplies() {
	    List<Resupply> incoming = new ArrayList<Resupply>(resupplies.size());
	    Iterator<Resupply> i = resupplies.iterator();
	    while (i.hasNext()) {
	        Resupply resupply = i.next();
	        String state = resupply.getState();
	        if (Resupply.PLANNED.equals(state) || Resupply.IN_TRANSIT.equals(state)) {
	            incoming.add(resupply);
	        }
	    }
	    return incoming;
	}
	
	/**
	 * Gets the resupply missions that have been delivered.
	 * @return resupply missions.
	 */
	public List<Resupply> getDeliveredResupplies() {
	    List<Resupply> delivered = new ArrayList<Resupply>(resupplies.size());
        Iterator<Resupply> i = resupplies.iterator();
        while (i.hasNext()) {
            Resupply resupply = i.next();
            String state = resupply.getState();
            if (Resupply.DELIVERED.equals(state)) {
                delivered.add(resupply);
            }
        }
        return delivered;
	}
	
	/**
	 * Cancels a resupply mission.
	 * @param resupply the resupply mission.
	 */
	public void cancelResupplyMission(Resupply resupply) {
	    resupply.setState(Resupply.CANCELED);
	    HistoricalEvent cancelEvent = new ResupplyEvent(resupply, ResupplyEvent.RESUPPLY_CANCELLED,
                "Resupply mission cancelled");
	    Simulation.instance().getEventManager().registerNewEvent(cancelEvent);
	}
	
	/**
	 * Time passing.
	 *
	 * @param time amount of time passing (in millisols)
	 * @throws Exception if error.
	 */
	public void timePassing(double time) {
		Iterator<Resupply> i = resupplies.iterator();
		while (i.hasNext()) {
			Resupply resupply = i.next();
			MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
			if (Resupply.PLANNED.equals(resupply.getState())) {
			    if (MarsClock.getTimeDiff(currentTime, resupply.getLaunchDate()) >= 0D) {
			        // Resupply mission is launched.
			        resupply.setState(Resupply.IN_TRANSIT);
			        HistoricalEvent deliverEvent = new ResupplyEvent(resupply, ResupplyEvent.RESUPPLY_LAUNCHED, 
			                "Resupply mission launched");
			        Simulation.instance().getEventManager().registerNewEvent(deliverEvent);  
			        continue;
			    }
			}
			else if (Resupply.IN_TRANSIT.equals(resupply.getState())) {
			    if (MarsClock.getTimeDiff(currentTime, resupply.getArrivalDate()) >= 0D) {
                    // Resupply mission has arrived at settlement.
                    resupply.setState(Resupply.DELIVERED);
                    deliverSupplies(resupply);
                    HistoricalEvent deliverEvent = new ResupplyEvent(resupply, ResupplyEvent.RESUPPLY_ARRIVED,
                            "Resupply mission arrived at settlement");
                    Simulation.instance().getEventManager().registerNewEvent(deliverEvent);  
                }
			}
		}
	}
	
	/**
     * Delivers supplies to the settlement.
     * @throws Exception if problem delivering supplies.
     */
    private void deliverSupplies(Resupply resupply) {
        
        Settlement settlement = resupply.getSettlement();
        
        // Deliver buildings.
        BuildingManager buildingManager = settlement.getBuildingManager();
        Iterator<String> buildingI = resupply.getNewBuildings().iterator();
        while (buildingI.hasNext()) {
            String type = buildingI.next();
                
            // Determine location and facing for the new building.
            BuildingTemplate positionedTemplate = positionNewResupplyBuilding(type, settlement);
            buildingManager.addBuilding(positionedTemplate);
        }
        
        // Deliver vehicles.
        UnitManager unitManager = Simulation.instance().getUnitManager();
        Iterator<String> vehicleI = resupply.getNewVehicles().iterator();
        while (vehicleI.hasNext()) {
            String vehicleType = vehicleI.next();
            String vehicleName = unitManager.getNewName(UnitManager.VEHICLE, null, null);
            Rover rover = new Rover(vehicleName, vehicleType, settlement);
            unitManager.addUnit(rover);
        }
        
        Inventory inv = settlement.getInventory();
        
        // Deliver equipment.
        Iterator<String> equipmentI = resupply.getNewEquipment().keySet().iterator();
        while (equipmentI.hasNext()) {
            String equipmentType = equipmentI.next();
            int number = resupply.getNewEquipment().get(equipmentType);
            for (int x=0; x < number; x++) {
                Equipment equipment = EquipmentFactory.getEquipment(equipmentType, 
                        settlement.getCoordinates(), false);
                equipment.setName(unitManager.getNewName(UnitManager.EQUIPMENT, equipmentType, null));
                inv.storeUnit(equipment);
            }
        }
        
        // Deliver resources.
        Iterator<AmountResource> resourcesI = resupply.getNewResources().keySet().iterator();
        while (resourcesI.hasNext()) {
            AmountResource resource = resourcesI.next();
            double amount = resupply.getNewResources().get(resource);
            double capacity = inv.getAmountResourceRemainingCapacity(resource, true);
            if (amount > capacity) amount = capacity;
            inv.storeAmountResource(resource, amount, true);
        }
        
        // Deliver parts.
        Iterator<Part> partsI = resupply.getNewParts().keySet().iterator();
        while (partsI.hasNext()) {
            Part part = partsI.next();
            int number = resupply.getNewParts().get(part);
            inv.storeItemResources(part, number);
        }
        
        // Deliver immigrants.
        Collection<Person> immigrants = new ConcurrentLinkedQueue<Person>();
        RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
        for (int x = 0; x < resupply.getNewImmigrantNum(); x++) {
            PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
            String gender = Person.FEMALE;
            if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) gender = Person.MALE;
            Person immigrant = new Person(unitManager.getNewName(UnitManager.PERSON, null, gender), 
                    gender, settlement);
            unitManager.addUnit(immigrant);
            relationshipManager.addNewImmigrant(immigrant, immigrants);
            immigrants.add(immigrant);
        }
    }
    
    /**
     * Determines and sets the position of a new resupply building.
     * @param building type the new building type.
     * @param settlement the settlement to place the new building.
     * @return the repositioned building template.
     */
    private BuildingTemplate positionNewResupplyBuilding(String buildingType, Settlement settlement) {
        
        BuildingTemplate newPosition = null;
        
        boolean hasLifeSupport = SimulationConfig.instance().getBuildingConfiguration().
                hasLifeSupport(buildingType);
        if (hasLifeSupport) {
            // Try to put building next to another inhabitable building.
            List<Building> inhabitableBuildings = settlement.getBuildingManager().getBuildings(LifeSupport.NAME);
            Collections.shuffle(inhabitableBuildings);
            Iterator<Building> i = inhabitableBuildings.iterator();
            while (i.hasNext()) {
                newPosition = positionNextToBuilding(buildingType, i.next(), 0D, settlement);
                if (newPosition != null) break;
            }
        }
        else {
            // Try to put building next to the same building type.
            List<Building> sameBuildings = settlement.getBuildingManager().getBuildingsOfName(buildingType);
            Collections.shuffle(sameBuildings);
            Iterator<Building> j = sameBuildings.iterator();
            while (j.hasNext()) {
                newPosition = positionNextToBuilding(buildingType, j.next(), 0D, settlement);
                if (newPosition != null) break;
            }
        }
        
        if (newPosition == null) {
            // Try to put building next to another building.
            // If not successful, try again 10m from each building and continue out at 10m increments 
            // until a location is found.
            BuildingManager buildingManager = settlement.getBuildingManager();
            if (buildingManager.getBuildingNum() > 0) {
                for (int x = 10; newPosition == null; x+= 10) {
                    List<Building> allBuildings = buildingManager.getBuildings();
                    Collections.shuffle(allBuildings);
                    Iterator<Building> i = allBuildings.iterator();
                    while (i.hasNext()) {
                        newPosition = positionNextToBuilding(buildingType, i.next(), (double) x, settlement);
                        if (newPosition != null) break;
                    }
                }
            }
            else {
                // If no buildings at settlement, position new building at 0,0 with random facing.
                newPosition = new BuildingTemplate(buildingType, 0D, 0D, RandomUtil.getRandomDouble(360D));
            }
        }
        
        return newPosition;
    }
    
    /**
     * Positions a new building near an existing building.
     * @param newBuildingType the new building type.
     * @param building the existing building.
     * @param separationDistance the separation distance (meters) from the building.
     * @param settlement the settlement to position the building.
     * @return new building template with determined position, or null if none found.
     */
    private BuildingTemplate positionNextToBuilding(String newBuildingType, Building building, 
            double separationDistance, Settlement settlement) {
        BuildingTemplate newPosition = null;
        
        double width = SimulationConfig.instance().getBuildingConfiguration().getWidth(newBuildingType);
        double length = SimulationConfig.instance().getBuildingConfiguration().getLength(newBuildingType);
        
        final int front = 0;
        final int back = 1;
        final int right = 2;
        final int left = 3;
        
        List<Integer> directions = new ArrayList<Integer>(4);
        directions.add(front);
        directions.add(back);
        directions.add(right);
        directions.add(left);
        Collections.shuffle(directions);
        
        double direction = 0D;
        double structureDistance = 0D;
        
        for (int x = 0; x < directions.size(); x++) {
            switch (directions.get(x)) {
                case front: direction = building.getFacing();
                            structureDistance = (building.getLength() / 2D) + (length / 2D);
                            break;
                case back: direction = building.getFacing() + 180D;
                            structureDistance = (building.getLength() / 2D) + (length / 2D);
                            break;
                case right:  direction = building.getFacing() + 90D;
                            structureDistance = (building.getWidth() / 2D) + (width / 2D);
                            break;
                case left:  direction = building.getFacing() + 270D;
                            structureDistance = (building.getWidth() / 2D) + (width / 2D);
            }
            
            double distance = structureDistance + separationDistance;
            double radianDirection = Math.PI * direction / 180D;
            double rectCenterX = building.getXLocation() + (distance * Math.sin(radianDirection));
            double rectCenterY = building.getYLocation() + (distance * Math.cos(radianDirection));
            double rectRotation = building.getFacing();
            
            // Check to see if proposed new building position intersects with any existing buildings 
            // or construction sites.
            if (settlement.getBuildingManager().checkIfNewBuildingLocationOpen(rectCenterX, 
                    rectCenterY, width, length, rectRotation)) {
                // Set the new building here.
                newPosition = new BuildingTemplate(newBuildingType, rectCenterX, rectCenterY, 
                        building.getFacing());
                break;
            }
        }
        
        return newPosition;
    }

	/**
	 * Prepare object for garbage collection.
	 */
    public void destroy() {
        Iterator<Resupply> i = resupplies.iterator();
        while (i.hasNext()) i.next().destroy();
        resupplies.clear();
        resupplies = null;
    }   	
}
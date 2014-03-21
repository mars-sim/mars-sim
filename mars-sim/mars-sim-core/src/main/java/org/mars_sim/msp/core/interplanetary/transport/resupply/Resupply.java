/**
 * Mars Simulation Project
 * Resupply.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.resupply;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.interplanetary.transport.TransitState;
import org.mars_sim.msp.core.interplanetary.transport.TransportEvent;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PersonGender;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Resupply mission from Earth for a settlement.
 */
public class Resupply
implements Serializable, Transportable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Resupply.class.getName());
	
	// Data members
	private Settlement settlement;
	private TransitState state;
	private MarsClock launchDate;
	private MarsClock arrivalDate;
	private List<String> newBuildings;
	private List<String> newVehicles;
	private Map<String, Integer> newEquipment;
	private int newImmigrantNum;
	private Map<AmountResource, Double> newResources;
	private Map<Part, Integer> newParts;

	/**
	 * Constructor.
	 * @param arrivalDate the arrival date of the supplies. 
	 * @param settlement the settlement receiving the supplies.
	 */
	public Resupply(MarsClock arrivalDate, Settlement settlement) {
		
		// Initialize data members.
		this.arrivalDate = arrivalDate;
		this.settlement = settlement;
	}
	
	@Override
	public MarsClock getLaunchDate() {
	    return (MarsClock) launchDate.clone();
	}
	
	/**
	 * Sets the launch date of the resupply mission.
	 * @param launchDate the launch date.
	 */
	public void setLaunchDate(MarsClock launchDate) {
	    this.launchDate = (MarsClock) launchDate.clone();
	}

    /**
     * Gets a list of the resupply buildings.
     * @return list of building types.
     */
    public List<String> getNewBuildings() {
        return newBuildings;
    }

    /**
     * Sets the list of resupply buildings. 
     * @param newBuildings list of building types.
     */
    public void setNewBuildings(List<String> newBuildings) {
        this.newBuildings = newBuildings;
    }

    /**
     * Gets a list of the resupply vehicles.
     * @return list of vehicle types.
     */
    public List<String> getNewVehicles() {
        return newVehicles;
    }

    /**
     * Sets the list of resupply vehicles.
     * @param newVehicles list of vehicle types.
     */
    public void setNewVehicles(List<String> newVehicles) {
        this.newVehicles = newVehicles;
    }

    /**
     * Gets a map of the resupply equipment.
     * @return map of equipment type and number.
     */
    public Map<String, Integer> getNewEquipment() {
        return newEquipment;
    }

    /**
     * Sets the map of resupply equipment.
     * @param newEquipment map of equipment type and number.
     */
    public void setNewEquipment(Map<String, Integer> newEquipment) {
        this.newEquipment = newEquipment;
    }

    /**
     * Gets the number of immigrants in the resupply mission.
     * @return the number of immigrants.
     */
    public int getNewImmigrantNum() {
        return newImmigrantNum;
    }

    /**
     * Sets the number of immigrants in the resupply mission.
     * @param newImmigrantNum the number of immigrants.
     */
    public void setNewImmigrantNum(int newImmigrantNum) {
        this.newImmigrantNum = newImmigrantNum;
    }

    /**
     * Gets a map of the resupply resources.
     * @return map of resource and amount (kg).
     */
    public Map<AmountResource, Double> getNewResources() {
        return newResources;
    }

    /**
     * Sets the map of resupply resources.
     * @param newResources map of resource and amount (kg).
     */
    public void setNewResources(Map<AmountResource, Double> newResources) {
        this.newResources = newResources;
    }

    /**
     * Gets a map of resupply parts.
     * @return map of part and number. 
     */
    public Map<Part, Integer> getNewParts() {
        return newParts;
    }

    /**
     * Sets the map of resupply parts.
     * @param newParts map of part and number.
     */
    public void setNewParts(Map<Part, Integer> newParts) {
        this.newParts = newParts;
    }

    @Override
	public MarsClock getArrivalDate() {
		return (MarsClock) arrivalDate.clone();
	}
	
	/**
	 * Sets the arrival date of the resupply mission.
	 * @param arrivalDate the arrival date.
	 */
	public void setArrivalDate(MarsClock arrivalDate) {
	    this.arrivalDate = (MarsClock) arrivalDate.clone();
	}
	
	/**
	 * Gets the destination settlement.
	 * @return destination settlement.
	 */
	public Settlement getSettlement() {
	    return settlement;
	}
	
	/**
	 * Sets the destination settlement.
	 * @param settlement the destination settlement.
	 */
    public void setSettlement(Settlement settlement) {
        this.settlement = settlement;
    }
    
    /**
     * Commits a set of modifications for the resupply mission.
     */
    public void commitModification() {
        HistoricalEvent newEvent = new TransportEvent(this, EventType.TRANSPORT_ITEM_MODIFIED, 
                "Resupply mission modified");
        Simulation.instance().getEventManager().registerNewEvent(newEvent);  
    }

    @Override
    public void destroy() {
        settlement = null;
        launchDate = null;
        arrivalDate = null;
        newBuildings.clear();
        newBuildings = null;
        newVehicles.clear();
        newVehicles = null;
        newEquipment.clear();
        newEquipment = null;
        newResources.clear();
        newResources = null;
        newParts.clear();
        newParts = null;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(getSettlement().getName());
        buff.append(": ");
        buff.append(getArrivalDate().getDateString());
        return buff.toString();
    }
    
    @Override
    public int compareTo(Transportable o) {
        int result = 0;
        
        double arrivalTimeDiff = MarsClock.getTimeDiff(arrivalDate, o.getArrivalDate());
        if (arrivalTimeDiff < 0D) {
            result = -1;
        }
        else if (arrivalTimeDiff > 0D) {
            result = 1;
        }
        else {
            // If arrival time is the same, compare by name alphabetically.
            result = getName().compareTo(o.getName());
        }
        
        return result;
    }

	@Override
	public String getName() {
		return getSettlement().getName();
	}

	@Override
	public TransitState getTransitState() {
		return state;
	}

	@Override
	public void setTransitState(TransitState transitState) {
		this.state = transitState;
	}

	@Override
	public void performArrival() {
		// Deliver supplies to the destination settlement.
		deliverSupplies();
	}
	
	/**
     * Delivers supplies to the destination settlement.
     */
    private void deliverSupplies() {
        
        // Deliver buildings.
        BuildingManager buildingManager = settlement.getBuildingManager();
        Iterator<String> buildingI = getNewBuildings().iterator();
        while (buildingI.hasNext()) {
            String type = buildingI.next();
                
            // Determine location and facing for the new building.
            BuildingTemplate positionedTemplate = positionNewResupplyBuilding(type);
            buildingManager.addBuilding(positionedTemplate);
        }
        
        // Deliver vehicles.
        UnitManager unitManager = Simulation.instance().getUnitManager();
        Iterator<String> vehicleI = getNewVehicles().iterator();
        while (vehicleI.hasNext()) {
            String vehicleType = vehicleI.next();
            Vehicle vehicle = null;
            if (LightUtilityVehicle.NAME.equalsIgnoreCase(vehicleType)) {
                String name = unitManager.getNewName(UnitType.VEHICLE, "LUV", null);
                vehicle = new LightUtilityVehicle(name, vehicleType, settlement);
            } else {
                String name = unitManager.getNewName(UnitType.VEHICLE, null, null);
                vehicle = new Rover(name, vehicleType, settlement);
            }
            unitManager.addUnit(vehicle);
        }
        
        Inventory inv = settlement.getInventory();
        
        // Deliver equipment.
        Iterator<String> equipmentI = getNewEquipment().keySet().iterator();
        while (equipmentI.hasNext()) {
            String equipmentType = equipmentI.next();
            int number = getNewEquipment().get(equipmentType);
            for (int x=0; x < number; x++) {
                Equipment equipment = EquipmentFactory.getEquipment(equipmentType, 
                        settlement.getCoordinates(), false);
                equipment.setName(unitManager.getNewName(UnitType.EQUIPMENT, equipmentType, null));
                inv.storeUnit(equipment);
            }
        }
        
        // Deliver resources.
        Iterator<AmountResource> resourcesI = getNewResources().keySet().iterator();
        while (resourcesI.hasNext()) {
            AmountResource resource = resourcesI.next();
            double amount = getNewResources().get(resource);
            double capacity = inv.getAmountResourceRemainingCapacity(resource, true, false);
            if (amount > capacity) amount = capacity;
            inv.storeAmountResource(resource, amount, true);
        }
        
        // Deliver parts.
        Iterator<Part> partsI = getNewParts().keySet().iterator();
        while (partsI.hasNext()) {
            Part part = partsI.next();
            int number = getNewParts().get(part);
            inv.storeItemResources(part, number);
        }
        
        // Deliver immigrants.
        Collection<Person> immigrants = new ConcurrentLinkedQueue<Person>();
        RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
        for (int x = 0; x < getNewImmigrantNum(); x++) {
            PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
            PersonGender gender = PersonGender.FEMALE;
            if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) gender = PersonGender.MALE;
            String birthplace = "Earth"; //TODO: randomize from list of countries/federations
            String immigrantName = unitManager.getNewName(UnitType.PERSON, null, gender);
            Person immigrant = new Person(immigrantName, gender, birthplace, settlement); //TODO: read from file
            unitManager.addUnit(immigrant);
            relationshipManager.addNewImmigrant(immigrant, immigrants);
            immigrants.add(immigrant);
            logger.info(immigrantName + " arrives on Mars at " + settlement.getName());
        }
    }
    
    /**
     * Determines and sets the position of a new resupply building.
     * @param building type the new building type.
     * @return the repositioned building template.
     */
    private BuildingTemplate positionNewResupplyBuilding(String buildingType) {
        
        BuildingTemplate newPosition = null;
        
        boolean hasLifeSupport = SimulationConfig.instance().getBuildingConfiguration().
                hasLifeSupport(buildingType);
        if (hasLifeSupport) {
            // Try to put building next to another inhabitable building.
            List<Building> inhabitableBuildings = settlement.getBuildingManager().getBuildings(BuildingFunction.LIFE_SUPPORT);
            Collections.shuffle(inhabitableBuildings);
            Iterator<Building> i = inhabitableBuildings.iterator();
            while (i.hasNext()) {
                Building building = i.next();
                newPosition = positionNextToBuilding(buildingType, building, 0D);
                if (newPosition != null) break;
            }
        }
        else {
            // Try to put building next to the same building type.
            List<Building> sameBuildings = settlement.getBuildingManager().getBuildingsOfName(buildingType);
            Collections.shuffle(sameBuildings);
            Iterator<Building> j = sameBuildings.iterator();
            while (j.hasNext()) {
                Building building = j.next();
                newPosition = positionNextToBuilding(buildingType, building, 0D);
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
                        Building building = i.next();
                        newPosition = positionNextToBuilding(buildingType, building, (double) x);
                        if (newPosition != null) break;
                    }
                }
            }
            else {
                // TODO: Replace width and length defaults to deal with variable width and length buildings.
                double width = SimulationConfig.instance().getBuildingConfiguration().getWidth(buildingType);
                if (width <= 0D) {
                    width = 10D;
                }
                double length = SimulationConfig.instance().getBuildingConfiguration().getLength(buildingType);
                if (length <= 0D) {
                    length = 10D;
                }
                
                // If no buildings at settlement, position new building at 0,0 with random facing.
                newPosition = new BuildingTemplate(0, buildingType, width, length, 0D, 0D, 
                        RandomUtil.getRandomDouble(360D));
            }
        }
        
        return newPosition;
    }
    
    /**
     * Positions a new building near an existing building.
     * @param newBuildingType the new building type.
     * @param building the existing building.
     * @param separationDistance the separation distance (meters) from the building.
     * @return new building template with determined position, or null if none found.
     */
    private BuildingTemplate positionNextToBuilding(String newBuildingType, Building building, 
            double separationDistance) {
        BuildingTemplate newPosition = null;
        
        // TODO: Replace width and length defaults to deal with variable width and length buildings.
        double width = SimulationConfig.instance().getBuildingConfiguration().getWidth(newBuildingType);
        if (width <= 0D) {
            width = 10D;
        }
        double length = SimulationConfig.instance().getBuildingConfiguration().getLength(newBuildingType);
        if (length <= 0D) {
            length = 10D;
        }
        
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
            double rectCenterX = building.getXLocation() - (distance * Math.sin(radianDirection));
            double rectCenterY = building.getYLocation() + (distance * Math.cos(radianDirection));
            double rectRotation = building.getFacing();
            
            // Check to see if proposed new building position intersects with any existing buildings 
            // or construction sites.
            if (settlement.getBuildingManager().checkIfNewBuildingLocationOpen(rectCenterX, 
                    rectCenterY, width, length, rectRotation)) {
                // Set the new building here.
                newPosition = new BuildingTemplate(0, newBuildingType, width, length, rectCenterX, 
                        rectCenterY, building.getFacing());
                break;
            }
        }
        
        return newPosition;
    }
}
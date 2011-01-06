/**
 * Mars Simulation Project
 * Resupply.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure;

import org.mars_sim.msp.core.*;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Resupply mission from Earth for a settlement.
 */
public class Resupply implements Serializable {

	// Data members
	private String resupplyName;
	private Settlement settlement;
	private MarsClock arrivalDate;
	private boolean isDelivered;
	private List<BuildingTemplate> newBuildings;
	private List<String> newVehicles;
	private Map<String, Integer> newEquipment;
	private int newImmigrantNum;
	private Map<AmountResource, Double> newResources;
	private Map<Part, Integer> newParts;

	/**
	 * Constructor
	 * @param arrivalDate the arrival date of the supplies. 
	 * @param resupplyName the name of the resupply mission.
	 * @param settlement the settlement receiving the supplies.
	 */
	Resupply(MarsClock arrivalDate, String resupplyName, Settlement settlement) {
		
		// Initialize data members.
		this.arrivalDate = arrivalDate;
		this.resupplyName = resupplyName;
		this.settlement = settlement;
		isDelivered = false;
		
		// Get resupply info from the config file.
		ResupplyConfig config = SimulationConfig.instance().getResupplyConfiguration();
		
		// Get new building types.
		newBuildings = config.getResupplyBuildings(resupplyName);
			
		// Get new vehicle types.
		newVehicles = config.getResupplyVehicleTypes(resupplyName);
		
		// Get new equipment types.
		newEquipment = config.getResupplyEquipment(resupplyName);
			
		// Get number of new immigrants.
		newImmigrantNum = config.getNumberOfResupplyImmigrants(resupplyName);
			
		// Get new resources map.
		newResources = config.getResupplyResources(resupplyName);
		
		// Get new parts map.
		newParts = config.getResupplyParts(resupplyName);
	}
	
	/**
	 * Gets the arrival date of the resupply mission.
	 * @return arrival date as MarsClock instance.
	 */
	public MarsClock getArrivalDate() {
		return (MarsClock) arrivalDate.clone();
	}
	
	/**
	 * Gets the name of the resupply mission.
	 * @return name
	 */
	public String getResupplyName() {
		return resupplyName;
	}
	
	/**
	 * Checks if the supplies have been delivered to the settlement.
	 * @return true if delivered
	 */
	public boolean isDelivered() {
		return isDelivered;
	}
	
	/**
	 * Delivers supplies to the settlement.
	 * @throws Exception if problem delivering supplies.
	 */
	void deliverSupplies() {
		
		// Deliver buildings.
		BuildingManager buildingManager = settlement.getBuildingManager();
		Iterator<BuildingTemplate> buildingI = newBuildings.iterator();
		while (buildingI.hasNext()) buildingManager.addBuilding(buildingI.next());
		
		// Deliver vehicles.
		UnitManager unitManager = Simulation.instance().getUnitManager();
		Iterator<String> vehicleI = newVehicles.iterator();
		while (vehicleI.hasNext()) {
			String vehicleType = vehicleI.next();
			String vehicleName = unitManager.getNewName(UnitManager.VEHICLE, null, null);
			Rover rover = new Rover(vehicleName, vehicleType, settlement);
			unitManager.addUnit(rover);
		}
		
		Inventory inv = settlement.getInventory();
		
		// Deliver equipment.
		Iterator<String> equipmentI = newEquipment.keySet().iterator();
		while (equipmentI.hasNext()) {
			String equipmentType = equipmentI.next();
			int number = newEquipment.get(equipmentType);
			for (int x=0; x < number; x++) {
				Equipment equipment = EquipmentFactory.getEquipment(equipmentType, settlement.getCoordinates(), false);
				equipment.setName(unitManager.getNewName(UnitManager.EQUIPMENT, equipmentType, null));
				inv.storeUnit(equipment);
			}
		}
		
		// Deliver resources.
		Iterator<AmountResource> resourcesI = newResources.keySet().iterator();
		while (resourcesI.hasNext()) {
			AmountResource resource = resourcesI.next();
			double amount = newResources.get(resource);
			double capacity = inv.getAmountResourceRemainingCapacity(resource, true);
			if (amount > capacity) amount = capacity;
			inv.storeAmountResource(resource, amount, true);
		}
		
		// Deliver parts.
		Iterator<Part> partsI = newParts.keySet().iterator();
		while (partsI.hasNext()) {
			Part part = partsI.next();
			int number = newParts.get(part);
			inv.storeItemResources(part, number);
		}
		
		// Deliver immigrants.
		Collection<Person> immigrants = new ConcurrentLinkedQueue<Person>();
		RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
		for (int x = 0; x < newImmigrantNum; x++) {
			PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
			String gender = Person.FEMALE;
			if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) gender = Person.MALE;
			Person immigrant = new Person(unitManager.getNewName(UnitManager.PERSON, null, gender), gender, settlement);
			unitManager.addUnit(immigrant);
			relationshipManager.addNewImmigrant(immigrant, immigrants);
			immigrants.add(immigrant);
		}
		
		// Send resupply delivery event.
		HistoricalEvent newEvent = new ResupplyEvent(settlement, resupplyName);
		Simulation.instance().getEventManager().registerNewEvent(newEvent);		
		
		// Set isDelivered to true;
		isDelivered = true;
	}
}
/**
 * Mars Simulation Project
 * Resupply.java
 * @version 2.77 2004-09-01
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.events.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.social.RelationshipManager;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
import org.mars_sim.msp.simulation.time.MarsClock;
import org.mars_sim.msp.simulation.vehicle.Rover;

/**
 * Resupply mission from Earth for a settlement.
 */
public class Resupply implements Serializable {

	// Data members
	private String resupplyName;
	private Settlement settlement;
	private MarsClock arrivalDate;
	private boolean isDelivered;
	private List newBuildings;
	private List newVehicles;
	private int newImmigrantNum;
	private Map newResources;

	/**
	 * Constructor
	 * @param arrivalDate the arrival date of the supplies. 
	 * @param resupplyName the name of the resupply mission.
	 * @param settlement the settlement receiving the supplies.
	 */
	Resupply(MarsClock arrivalDate, String resupplyName, Settlement settlement) throws Exception {
		
		// Initialize data members.
		this.arrivalDate = arrivalDate;
		this.resupplyName = resupplyName;
		this.settlement = settlement;
		isDelivered = false;
		
		// Get resupply info from the config file.
		SimulationConfig simConfig = Simulation.instance().getSimConfig();
		SettlementConfig config = simConfig.getSettlementConfiguration();
		
		// Get new building types.
		newBuildings = config.getResupplyBuildingTypes(resupplyName);
			
		// Get new vehicle types.
		newVehicles = config.getResupplyVehicleTypes(resupplyName);
			
		// Get number of new immigrants.
		newImmigrantNum = config.getNumberOfResupplyImmigrants(resupplyName);
			
		// Get new resources map.
		newResources = config.getResupplyResources(resupplyName);
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
	void deliverSupplies() throws Exception {
		
		// Deliver buildings.
		BuildingManager buildingManager = settlement.getBuildingManager();
		Iterator buildingI = newBuildings.iterator();
		while (buildingI.hasNext()) {
			String buildingType = (String) buildingI.next();
			buildingManager.addBuilding(buildingType);
		}
		
		// Deliver vehicles.
		UnitManager unitManager = Simulation.instance().getUnitManager();
		Iterator vehicleI = newVehicles.iterator();
		while (vehicleI.hasNext()) {
			String vehicleType = (String) vehicleI.next();
			String vehicleName = unitManager.getNewName(UnitManager.VEHICLE, null);
			Rover rover = new Rover(vehicleName, vehicleType, settlement);
			unitManager.addUnit(rover);
		}
		
		// Deliver resources.
		Iterator resourcesI = newResources.keySet().iterator();
		while (resourcesI.hasNext()) {
			String resourceType = (String) resourcesI.next();
			double amount = ((Double) newResources.get(resourceType)).doubleValue();
			settlement.getInventory().addResource(resourceType, amount);
		}
		
		// Deliver immigrants.
		PersonCollection immigrants = new PersonCollection();
		RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
		for (int x = 0; x < newImmigrantNum; x++) {
			PersonConfig personConfig = Simulation.instance().getSimConfig().getPersonConfiguration();
			String gender = Person.FEMALE;
			if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) gender = Person.MALE;
			Person immigrant = new Person(unitManager.getNewName(UnitManager.PERSON, gender), gender, settlement);
			unitManager.addUnit(immigrant);
			relationshipManager.addNewImmigrant(immigrant, immigrants);
			immigrants.add(immigrant);
		}
		
		// Send resupply delivery event.
		HistoricalEvent newEvent = new ResupplyEvent(settlement, getResupplyName());
		Simulation.instance().getEventManager().registerNewEvent(newEvent);		
		
		// Set isDelivered to true;
		isDelivered = true;
	}
}
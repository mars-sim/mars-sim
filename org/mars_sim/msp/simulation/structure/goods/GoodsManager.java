/**
 * Mars Simulation Project
 * GoodsManager.java
 * @version 2.81 2007-04-28
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.goods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.equipment.Bag;
import org.mars_sim.msp.simulation.equipment.Container;
import org.mars_sim.msp.simulation.equipment.EquipmentFactory;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.equipment.SpecimenContainer;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.person.ai.job.Areologist;
import org.mars_sim.msp.simulation.person.ai.mission.Exploration;
import org.mars_sim.msp.simulation.person.ai.mission.CollectIce;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.VehicleMission;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ItemResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.function.Crop;
import org.mars_sim.msp.simulation.structure.building.function.Farming;
import org.mars_sim.msp.simulation.structure.building.function.ResourceProcess;
import org.mars_sim.msp.simulation.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.simulation.time.MarsClock;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleCollection;
import org.mars_sim.msp.simulation.vehicle.VehicleIterator;

/**
 * A manager for goods values at a settlement.
 */
public class GoodsManager implements Serializable {

	// Unit update events.
	public static final String GOODS_VALUE_EVENT = "goods values";
	
	// Data members
	private Settlement settlement;
	private Map goodsValues;
	
	/**
	 * Constructor
	 * @param settlement the settlement this manager is for.
	 * @throws Exception if errors constructing instance.
	 */
	public GoodsManager(Settlement settlement) throws Exception {
		this.settlement = settlement;
		
		populateGoodsValues();
		
		updateGoodsValues();
	}
	
	/**
	 * Populates the goods map with empty values.
	 */
	private void populateGoodsValues() {
		List goods = GoodsUtil.getGoodsList();
		goodsValues = new HashMap(goods.size());
		Iterator i = goods.iterator();
		while (i.hasNext()) goodsValues.put(i.next(), new Double(0D));
	}
	
	/**
	 * Gets the value of a good.
	 * @param good the good to check value for.
	 * @return the value (value points).
	 */
	public double getGoodValue(Good good) {
		if (goodsValues.containsKey(good)) 
			return ((Double) goodsValues.get(good)).doubleValue();
		else throw new IllegalArgumentException("Good: " + good + " not valid.");
	}
	
	/**
	 * Updates the values for all the goods at the settlement.
	 * @throws Exception if error updating goods values.
	 */
	private void updateGoodsValues() throws Exception {
		Iterator i = goodsValues.keySet().iterator();
		while (i.hasNext()) updateGoodValue((Good) i.next());
		settlement.fireUnitUpdate(GOODS_VALUE_EVENT);
	}
	
	/**
	 * Time passing
	 * @param time the amount of time passing (millisols).
	 * @throws Exception if error during time.
	 */
	public void timePassing(double time) throws Exception {
		updateGoodsValues();
	}

	/**
	 * Updates the value of a good at the settlement.
	 * @param good the good to update.
	 * @throws Exception if error updating good value.
	 */
	private void updateGoodValue(Good good) throws Exception {
		if (good != null) {
			double value = 0D;
			
			// Determine all amount resource good values.
			if (Good.AMOUNT_RESOURCE.equals(good.getCategory())) 
				value = determineAmountResourceGoodValue((AmountResource) good.getObject());
			
			// Determine all item resource values.
			if (Good.ITEM_RESOURCE.equals(good.getCategory()))
				value = determineItemResourceGoodValue((ItemResource) good.getObject());
			
			// Determine all equipment values.
			if (Good.EQUIPMENT.equals(good.getCategory()))
				value = determineEquipmentGoodValue(good.getClassType());
			
			// TODO: determine all vehicle values.
			
			goodsValues.put(good, new Double(value));
		}
		else throw new IllegalArgumentException("Good is null.");
	}
	
	/**
	 * Determines the value of an amount resource.
	 * @param resource the amount resource.
	 * @return value (value points / kg)
	 * @throws Exception if error determining resource value.
	 */
	private double determineAmountResourceGoodValue(AmountResource resource) throws Exception {
		double value = 0D;
		
		double supply = getAmountOfResourceForSettlement(resource) + 1D;
		double demand = 0D;
		
		// Add life support demand if applicable.
		demand += getLifeSupportDemand(resource);
		
		// Add vehicle demand if applicable.
		demand += getVehicleDemand(resource);
		
		// Add farming demand.
		demand += getFarmingDemand(resource);
		
		// Limit demand by storage capacity.
		double capacity = settlement.getInventory().getAmountResourceCapacity(resource);
		if (demand > capacity) demand = capacity;
		
		value = demand / supply;
		
		// Use resource processing value if higher.
		double resourceProcessingValue = getResourceProcessingValue(resource);
		if (resourceProcessingValue > value) value = resourceProcessingValue;
		
		return value;
	}
	
	/**
	 * Gets the life support demand for an amount resource.
	 * @param resource the resource to check.
	 * @return demand (kg)
	 * @throws Exception if error getting life support demand.
	 */
	private double getLifeSupportDemand(AmountResource resource) throws Exception {
		if (resource.isLifeSupport()) {
			double amountNeededSol = SimulationConfig.instance().getPersonConfiguration().getOxygenConsumptionRate();
			double amountNeededOrbit = amountNeededSol * MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR;
			int numPeople = settlement.getAllAssociatedPeople().size();
			return numPeople * amountNeededOrbit;
		}
		else return 0D;
	}
	
	/**
	 * Gets vehicle demand for an amount resource.
	 * @param resource the resource to check.
	 * @return demand (kg) for the resource.
	 * @throws Exception if error getting resource demand.
	 */
	private double getVehicleDemand(AmountResource resource) throws Exception {
		double demand = 0D;
		if (resource.isLifeSupport() || resource.equals(AmountResource.METHANE)) {
			VehicleIterator i = getAssociatedVehicles().iterator();
			while (i.hasNext()) demand += i.next().getInventory().getAmountResourceCapacity(resource);
		}
		return demand;
	}
	
	/**
	 * Gets all vehicles associated with the settlement.
	 * @return collection of vehicles.
	 */
	private VehicleCollection getAssociatedVehicles() {
		// Start with parked vehicles at settlement.
		VehicleCollection vehicles = settlement.getParkedVehicles();
		
		// Add associated vehicles out on missions.
		Iterator i = Simulation.instance().getMissionManager().getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = (Mission) i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !vehicles.contains(vehicle)) vehicles.add(vehicle);
			}
		}
		
		return vehicles;
	}
	
	/**
	 * Gets the farming demand for the resource.
	 * @param resource the resource to check.
	 * @return demand (kg) for the resource.
	 * @throws Exception if error determining demand.
	 */
	private double getFarmingDemand(AmountResource resource) throws Exception {
		double demand = 0D;
		if (resource.equals(AmountResource.WASTE_WATER) || resource.equals(AmountResource.CARBON_DIOXIDE)) {
			double foodValue = getGoodValue(GoodsUtil.getResourceGood(AmountResource.FOOD));
			
			Iterator i = settlement.getBuildingManager().getBuildings().iterator();
			while (i.hasNext()) {
				Building building = (Building) i.next();
				if (building.hasFunction(Farming.NAME)) {
					Farming farm = (Farming) building.getFunction(Farming.NAME);
					
					double amountNeeded = 0D;
					if (resource.equals(AmountResource.WASTE_WATER)) 
						amountNeeded = Crop.WASTE_WATER_NEEDED;
					else if (resource.equals(AmountResource.CARBON_DIOXIDE))
						amountNeeded = Crop.CARBON_DIOXIDE_NEEDED;
					
					demand += (farm.getEstimatedHarvestPerOrbit() * foodValue) / amountNeeded;
				}
			}
		}
		
		return demand;
	}
	
	/**
	 * Gets the value of a resource from all resource processes.
	 * @param resource the amount resource.
	 * @return value (value points / kg)
	 * @throws Exception if error getting value.
	 */
	private double getResourceProcessingValue(AmountResource resource) throws Exception {
		double value = 0D;
		
		// Get all resource processes at settlement.
		Iterator i = getResourceProcesses().iterator();
		while (i.hasNext()) {
			ResourceProcess process = (ResourceProcess) i.next();
			double processValue = getResourceProcessValue(process, resource);
			if (processValue > value) value = processValue;
		}
		
		return value;
	}
	
	/**
	 * Gets the value of a resource from a resource process.
	 * @param process the resource process.
	 * @param resource the amount resource.
	 * @return value (value points / kg)
	 */
	private double getResourceProcessValue(ResourceProcess process, AmountResource resource) {
		double value = 0D;
		
		Set inputResources = process.getInputResources();
		Set outputResources = process.getOutputResources();
		
		if (inputResources.contains(resource) && !process.isAmbientInputResource(resource)) {
			double outputValue = 0D;
			Iterator i = outputResources.iterator();
			while (i.hasNext()) {
				AmountResource output = (AmountResource) i.next();
				double outputRate = process.getMaxOutputResourceRate(output); 
				if (!process.isWasteOutputResource(resource))
					outputValue += (getGoodValue(GoodsUtil.getResourceGood(output)) * outputRate);
			}
			
			double otherInputValue = 0D;
			Iterator j = inputResources.iterator();
			while (j.hasNext()) {
				AmountResource input = (AmountResource) j.next();
				double inputRate = process.getMaxInputResourceRate(input);
				if (!input.equals(resource) && !process.isAmbientInputResource(input)) 
					otherInputValue += (getGoodValue(GoodsUtil.getResourceGood(input)) * inputRate);
			}
			
			double totalValue = outputValue - otherInputValue;
			double resourceInputRate = process.getMaxInputResourceRate(resource);
			
			if (resourceInputRate > 0D) value = totalValue / resourceInputRate;
		}
		
		return value;
	}
	
	/**
	 * Get all resource processes at settlement.
	 * @return list of resource processes.
	 * @throws BuildingException if error getting processes.
	 */
	private List getResourceProcesses() throws BuildingException {
		List processes = new ArrayList(0);
		Iterator i = settlement.getBuildingManager().getBuildings().iterator();
		while (i.hasNext()) {
			Building building = (Building) i.next();
			if (building.hasFunction(ResourceProcessing.NAME)) {
				ResourceProcessing processing = (ResourceProcessing) building.getFunction(ResourceProcessing.NAME);
				processes.addAll(processing.getProcesses());
			}
		}
		return processes;
	}
	
	/**
	 * Gets the amount of an amount resource for a settlement.
	 * @param resource the resource to check.
	 * @return amount (kg) of resource for the settlement.
	 * @throws InventoryException if error getting the amount of the resource.
	 */
	private double getAmountOfResourceForSettlement(AmountResource resource) throws InventoryException {
		double amount = 0D;
		
		// Get amount of resource in settlement storage.
		amount += settlement.getInventory().getAmountResourceStored(resource);
		
		// Get amount of resource out on mission vehicles.
		Iterator i = Simulation.instance().getMissionManager().getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = (Mission) i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement())) 
					amount += vehicle.getInventory().getAmountResourceStored(resource);
			}
		}
		
		// Get amount of resource carried by people on EVA.
		PersonIterator j = settlement.getAllAssociatedPeople().iterator();
		while (j.hasNext()) {
			Person person = j.next();
			if (person.getLocationSituation().equals(Person.OUTSIDE)) 
				amount += person.getInventory().getAmountResourceStored(resource);
		}
		
		return amount;
	}
	
	/**
	 * Determines the value of an item resource.
	 * @param resource the resource to check.
	 * @return value (Value Points / kg)
	 * @throws Exception if error determining value.
	 */
	private double determineItemResourceGoodValue(ItemResource resource) throws Exception {
		double value = 0D;
	
		double supply = getAmountOfResourceForSettlement(resource) + 1D;
		double demand = 0D;
		
		// TODO: Determine demand.
		
		value = demand / supply;
		
		return value;
	}
	
	/**
	 * Gets the amount (mass) of an item resource for a settlement.
	 * @param resource the resource to check.
	 * @return amount (kg) of resource for the settlement.
	 * @throws InventoryException if error getting the amount of the resource.
	 */
	private double getAmountOfResourceForSettlement(ItemResource resource) throws InventoryException {
		double amount = 0D;
		double mass = resource.getMassPerItem();
		
		// Get amount of resource in settlement storage.
		amount += settlement.getInventory().getItemResourceNum(resource) * mass;
		
		// Get amount of resource out on mission vehicles.
		Iterator i = Simulation.instance().getMissionManager().getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = (Mission) i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement())) 
					amount += vehicle.getInventory().getItemResourceNum(resource) * mass;
			}
		}
		
		// Get amount of resource carried by people on EVA.
		PersonIterator j = settlement.getAllAssociatedPeople().iterator();
		while (j.hasNext()) {
			Person person = j.next();
			if (person.getLocationSituation().equals(Person.OUTSIDE)) 
				amount += person.getInventory().getItemResourceNum(resource) * mass;
		}
		
		return amount;
	}
	
	/**
	 * Determines the value of an equipment.
	 * @param equipmentClass the equipment type to check.
	 * @return the value (value points / kg) 
	 * @throws Exception if error determining value.
	 */
	private double determineEquipmentGoodValue(Class equipmentClass) throws Exception {
		double value = 0D;
		
		// Determine supply amount.
		double supply = getAmountOfEquipmentForSettlement(equipmentClass);
		
		//Determine demand amount.
		double demand = determineEquipmentDemand(equipmentClass);
		
		value = demand / supply;
		
		return value;
	}
	
	/**
	 * Determines the demand for a type of equipment.
	 * @param equipmentClass the equipment class.
	 * @return demand (kg).
	 * @throws Exception if error getting demand.
	 */
	private double determineEquipmentDemand(Class equipmentClass) throws Exception {
		int numDemand = 0;
		
		// Gets the mass per item based on an example instance of the equipment class.
		double mass = EquipmentFactory.getEquipment(equipmentClass, new Coordinates(0, 0)).getMass();
		
		// Determine number of EVA suits that are needed
		if (EVASuit.class.equals(equipmentClass)) numDemand += 2 * settlement.getAllAssociatedPeople().size();
		
		// Determine the number of containers that are needed.
		if (Container.class.isAssignableFrom(equipmentClass)) numDemand = 10;
		
		int areologistNum = getAreologistNum();
		
		// Determine number of bags that are needed.
		if (Bag.class.equals(equipmentClass)) {
			double iceValue = getGoodValue(GoodsUtil.getResourceGood(AmountResource.ICE));
			numDemand +=  CollectIce.REQUIRED_BAGS * areologistNum * Math.round(iceValue);
		}
		
		// Determine number of specimen containers that are needed.
		if (SpecimenContainer.class.equals(equipmentClass)) 
			numDemand +=  Exploration.REQUIRED_SPECIMEN_CONTAINERS * areologistNum;
		
		return numDemand * mass;
	}
	
	/**
	 * Gets the number of areologists associated with the settlement.
	 * @return number of areologists
	 */
	private int getAreologistNum() {
		int result = 0;
		PersonIterator i = settlement.getAllAssociatedPeople().iterator();
		while (i.hasNext()) {
			if (i.next().getMind().getJob() instanceof Areologist) result ++;
		}
		return result;
	}
	
	/**
	 * Gets the amount (mass) of an equipment for a settlement.
	 * @param equipmentClass the equipmentType to check.
	 * @return amount (kg) of equipment for the settlement.
	 * @throws Exception if error getting the amount of the equipment.
	 */
	private double getAmountOfEquipmentForSettlement(Class equipmentClass) throws Exception {
		double amount = 0D;
		
		// Gets the mass per item based on an example instance of the equipment class.
		double mass = EquipmentFactory.getEquipment(equipmentClass, new Coordinates(0, 0)).getMass();
		
		// Get amount of the equipment in settlement storage.
		amount += settlement.getInventory().findNumUnitsOfClass(equipmentClass) * mass;
		
		// Get amount of resource out on mission vehicles.
		Iterator i = Simulation.instance().getMissionManager().getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = (Mission) i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement())) 
					amount += vehicle.getInventory().findNumUnitsOfClass(equipmentClass) * mass;
			}
		}
		
		// Get amount of resource carried by people on EVA.
		PersonIterator j = settlement.getAllAssociatedPeople().iterator();
		while (j.hasNext()) {
			Person person = j.next();
			if (person.getLocationSituation().equals(Person.OUTSIDE)) 
				amount += person.getInventory().findNumUnitsOfClass(equipmentClass) * mass;
		}
		
		return amount;
	}
}
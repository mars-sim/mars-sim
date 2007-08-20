/**
 * Mars Simulation Project
 * GoodsManager.java
 * @version 2.81 2007-08-19
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

import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.equipment.Bag;
import org.mars_sim.msp.simulation.equipment.Container;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.equipment.SpecimenContainer;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonConfig;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.person.ai.job.Areologist;
import org.mars_sim.msp.simulation.person.ai.job.Driver;
import org.mars_sim.msp.simulation.person.ai.job.Trader;
import org.mars_sim.msp.simulation.person.ai.mission.Exploration;
import org.mars_sim.msp.simulation.person.ai.mission.CollectIce;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.VehicleMission;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ItemResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.SettlementIterator;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.function.Crop;
import org.mars_sim.msp.simulation.structure.building.function.Farming;
import org.mars_sim.msp.simulation.structure.building.function.ResourceProcess;
import org.mars_sim.msp.simulation.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.simulation.time.MarsClock;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleCollection;
import org.mars_sim.msp.simulation.vehicle.VehicleConfig;
import org.mars_sim.msp.simulation.vehicle.VehicleIterator;

/**
 * A manager for goods values at a settlement.
 */
public class GoodsManager implements Serializable {

	// Unit update events.
	public static final String GOODS_VALUE_EVENT = "goods values";
	
	// Mission types.
	private static final String TRAVEL_TO_SETTLEMENT_MISSION = "travel to settlement";
	private static final String EXPLORATION_MISSION = "exploration";
	private static final String COLLECT_ICE_MISSION = "collect ice";
	private static final String RESCUE_SALVAGE_MISSION = "rescue/salvage mission";
	private static final String TRADE_MISSION = "trade";
	
	// Data members
	private Settlement settlement;
	private Map<Good, Double> goodsValues;
	private Map<Good, Double> goodsDemandCache;
	private Map<Good, Double> goodsTradeCache;
	private Map<AmountResource, Double> resourceProcessingCache;
	private Map<String, Double> vehicleBuyValueCache;
	private Map<String, Double> vehicleSellValueCache;
	
	/**
	 * Constructor
	 * @param settlement the settlement this manager is for.
	 * @throws Exception if errors constructing instance.
	 */
	public GoodsManager(Settlement settlement) throws Exception {
		this.settlement = settlement;
		populateGoodsValues();
	}
	
	/**
	 * Populates the goods cache maps with empty values.
	 */
	private void populateGoodsValues() {
		List<Good> goods = GoodsUtil.getGoodsList();
		goodsValues = new HashMap<Good, Double>(goods.size());
		goodsDemandCache = new HashMap<Good, Double>(goods.size());
		goodsTradeCache = new HashMap<Good, Double>(goods.size());
		
		Iterator<Good> i = goods.iterator();
		while (i.hasNext()) {
			Good good = i.next();
			goodsValues.put(good, new Double(0D));
			goodsDemandCache.put(good, new Double(0D));
			goodsTradeCache.put(good, new Double(0D));
		}
		
		// Create and populate resource processing cache with all amount resources.
		Set<AmountResource> amountResources = AmountResource.getAmountResources();
		resourceProcessingCache = new HashMap<AmountResource, Double>(amountResources.size());
		Iterator<AmountResource> j = amountResources.iterator();
		while (j.hasNext()) resourceProcessingCache.put(j.next(), new Double(0D));
	}
	
	/**
	 * Gets the value per mass of a good.
	 * @param good the good to check value for.
	 * @return the value per mass (value points / kg).
	 */
	public double getGoodValuePerMass(Good good) {
		if (goodsValues.containsKey(good)) return goodsValues.get(good).doubleValue();
		else throw new IllegalArgumentException("Good: " + good + " not valid.");
	}
	
	public double getGoodValuePerMass(Good good, double supply) throws Exception {
		if (goodsValues.containsKey(good)) return determineGoodValue(good, supply, true);
		else throw new IllegalArgumentException("Good: " + good + " not valid.");
	}
	
	/**
	 * Gets the value per item of a good.
	 * @param good the good to check.
	 * @return value (VP)
	 * @throws Exception if error getting value.
	 */
	public double getGoodValuePerItem(Good good) throws Exception {
		if (goodsValues.containsKey(good)) 
			return getGoodValuePerMass(good) * GoodsUtil.getGoodMassPerItem(good);
		else throw new IllegalArgumentException("Good: " + good + " not valid.");
	}
	
	public double getGoodValuePerItem(Good good, double supply) throws Exception {
		if (goodsValues.containsKey(good))
			return getGoodValuePerMass(good, supply) * GoodsUtil.getGoodMassPerItem(good);
		else throw new IllegalArgumentException("Good: " + good + " not valid.");
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
	 * Updates the values for all the goods at the settlement.
	 * @throws Exception if error updating goods values.
	 */
	private void updateGoodsValues() throws Exception {
		Iterator<Good> i = goodsValues.keySet().iterator();
		while (i.hasNext()) updateGoodValue(i.next());
		settlement.fireUnitUpdate(GOODS_VALUE_EVENT);
	}

	/**
	 * Updates the value of a good at the settlement.
	 * @param good the good to update.
	 * @throws Exception if error updating good value.
	 */
	private void updateGoodValue(Good good) throws Exception {
		if (good != null) goodsValues.put(good, determineGoodValue(good, getAmountOfGoodForSettlement(good), false));
		else throw new IllegalArgumentException("Good is null.");
	}
	
	/**
	 * Determines the value of a good.
	 * @param good the good to check.
	 * @param supply the current supply (kg) of the good.
	 * @param useCache use demand and trade caches to determine value?
	 * @return value of good.
	 * @throws Exception if problem determining good value.
	 */
	private double determineGoodValue(Good good, double supply, boolean useCache) throws Exception {
		if (good != null) {
			double value = 0D;
			
			// Determine all amount resource good values.
			if (Good.AMOUNT_RESOURCE.equals(good.getCategory())) 
				value = determineAmountResourceGoodValue(good, supply, useCache);
			
			// Determine all item resource values.
			if (Good.ITEM_RESOURCE.equals(good.getCategory()))
				value = determineItemResourceGoodValue(good, supply, useCache);
			
			// Determine all equipment values.
			if (Good.EQUIPMENT.equals(good.getCategory()))
				value = determineEquipmentGoodValue(good, supply, useCache);
			
			// Determine all vehicle values.
			if (Good.VEHICLE.equals(good.getCategory()))
				value = determineVehicleGoodValue(good, supply, useCache);
			
			// Determine trade value.
			double tradeValue = determineTradeValue(good, useCache);
			if (tradeValue > value) value = tradeValue;
			
			return value;
		}
		else throw new IllegalArgumentException("Good is null.");
	}
	
	/**
	 * Determines the value of an amount resource.
	 * @param resourceGood the amount resource good.
	 * @param supply the current supply (kg) of the good.
	 * @param useCache use the cache to determine value.
	 * @return value (value points / kg)
	 * @throws Exception if error determining resource value.
	 */
	private double determineAmountResourceGoodValue(Good resourceGood, double supply, boolean useCache) 
			throws Exception {
		double value = 0D;
		
		supply++;
		double demand = 0D;
		AmountResource resource = (AmountResource) resourceGood.getObject();
		
		if (useCache) {
			if (goodsDemandCache.containsKey(resourceGood)) demand = goodsDemandCache.get(resourceGood).doubleValue();
			else throw new IllegalArgumentException("Good: " + resourceGood + " not valid.");
		}
		else {
			// Add life support demand if applicable.
			demand += getLifeSupportDemand(resource);
		
			// Add vehicle demand if applicable.
			demand += getVehicleDemand(resource);
		
			// Add farming demand.
			demand += getFarmingDemand(resource);
		
			// Limit demand by storage capacity.
			double capacity = settlement.getInventory().getAmountResourceCapacity(resource);
			if (demand > capacity) demand = capacity;
			
			goodsDemandCache.put(resourceGood, new Double(demand));
		}
		
		value = demand / supply;
		
		// Use resource processing value if higher.
		double resourceProcessingValue = getResourceProcessingValue(resource, useCache);
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
			double foodValue = getGoodValuePerMass(GoodsUtil.getResourceGood(AmountResource.FOOD));
			
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
	 * @param useProcessingCache use processing cache to determine value.
	 * @return value (value points / kg)
	 * @throws Exception if error getting value.
	 */
	private double getResourceProcessingValue(AmountResource resource, boolean useProcessingCache) throws Exception {
		double value = 0D;
		
		if (useProcessingCache) {
			if (resourceProcessingCache.containsKey(resource)) value = resourceProcessingCache.get(resource);
			else throw new IllegalArgumentException("Amount Resource: " + resource + " not valid.");
		}
		else {
			// Get all resource processes at settlement.
			Iterator i = getResourceProcesses().iterator();
			while (i.hasNext()) {
				ResourceProcess process = (ResourceProcess) i.next();
				double processValue = getResourceProcessValue(process, resource);
				if (processValue > value) value = processValue;
			}
			resourceProcessingCache.put(resource, new Double(value));
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
					outputValue += (getGoodValuePerMass(GoodsUtil.getResourceGood(output)) * outputRate);
			}
			
			double otherInputValue = 0D;
			Iterator j = inputResources.iterator();
			while (j.hasNext()) {
				AmountResource input = (AmountResource) j.next();
				double inputRate = process.getMaxInputResourceRate(input);
				if (!input.equals(resource) && !process.isAmbientInputResource(input)) 
					otherInputValue += (getGoodValuePerMass(GoodsUtil.getResourceGood(input)) * inputRate);
			}
			
			double totalValue = outputValue - otherInputValue;
			double resourceInputRate = process.getMaxInputResourceRate(resource);
			
			if (resourceInputRate > 0D) value = totalValue / resourceInputRate;
		}
		
		return value / 2D;
	}
	
	/**
	 * Get all resource processes at settlement.
	 * @return list of resource processes.
	 * @throws BuildingException if error getting processes.
	 */
	private List<ResourceProcess> getResourceProcesses() throws BuildingException {
		List<ResourceProcess> processes = new ArrayList<ResourceProcess>(0);
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
	 * Gets the number of a good at the settlement.
	 * @param good the good to check.
	 * @return the number of the good (or amount (kg) if amount resource good).
	 * @throws InventoryException if error determining the number of the good.
	 */
	public double getNumberOfGoodForSettlement(Good good) throws InventoryException {
		if (good != null) {
			double result = 0D;
			
			if (Good.AMOUNT_RESOURCE.equals(good.getCategory())) 
				result = getAmountOfResourceForSettlement((AmountResource) good.getObject());
			else if (Good.ITEM_RESOURCE.equals(good.getCategory()))
				result = getNumberOfResourceForSettlement((ItemResource) good.getObject());
			else if (Good.EQUIPMENT.equals(good.getCategory()))
				result = getNumberOfEquipmentForSettlement(good.getClassType());
			else if (Good.VEHICLE.equals(good.getCategory()))
				result = getNumberOfVehiclesForSettlement(good.getName());
			
			return result;
		}
		else throw new IllegalArgumentException("Good is null.");
	}
	
	/**
	 * Gets the amount of a good at the settlement.
	 * @param good the good to check.
	 * @return the amount (kg) of the good at the settlement.
	 * @throws Exception if error determining amount of the good.
	 */
	public double getAmountOfGoodForSettlement(Good good) throws Exception {
		return getNumberOfGoodForSettlement(good) * GoodsUtil.getGoodMassPerItem(good);
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
	 * @param resourceGood the resource good to check.
	 * @param supply the current supply (kg) of the good.
	 * @param useCache use the cache to determine value.
	 * @return value (Value Points / kg)
	 * @throws Exception if error determining value.
	 */
	private double determineItemResourceGoodValue(Good resourceGood, double supply, boolean useCache) 
			throws Exception {
		double value = 0D;
	
		double demand = 0D;
		
		if (useCache) {
			if (goodsDemandCache.containsKey(resourceGood)) demand = goodsDemandCache.get(resourceGood).doubleValue();
			else throw new IllegalArgumentException("Good: " + resourceGood + " not valid.");
		}
		else {
			// TODO: Determine demand (in kg).
			goodsDemandCache.put(resourceGood, new Double(demand));
		}
		
		value = demand / (supply + 1D);
		
		return value;
	}
	
	/**
	 * Gets the number of an item resource for a settlement.
	 * @param resource the resource to check.
	 * @return number of resource for the settlement.
	 * @throws InventoryException if error getting the number of the resource.
	 */
	private double getNumberOfResourceForSettlement(ItemResource resource) throws InventoryException {
		double number = 0D;
		
		// Get number of resources in settlement storage.
		number += settlement.getInventory().getItemResourceNum(resource);
		
		// Get number of resources out on mission vehicles.
		Iterator i = Simulation.instance().getMissionManager().getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = (Mission) i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement())) 
					number += vehicle.getInventory().getItemResourceNum(resource);
			}
		}
		
		// Get number of resources carried by people on EVA.
		PersonIterator j = settlement.getAllAssociatedPeople().iterator();
		while (j.hasNext()) {
			Person person = j.next();
			if (person.getLocationSituation().equals(Person.OUTSIDE)) 
				number += person.getInventory().getItemResourceNum(resource);
		}
		
		return number;
	}
	
	/**
	 * Determines the value of an equipment.
	 * @param equipmentGood the equipment good to check.
	 * @param supply the current supply (kg) of the good.
	 * @param useCache use the cache to determine value.
	 * @return the value (value points / kg) 
	 * @throws Exception if error determining value.
	 */
	private double determineEquipmentGoodValue(Good equipmentGood, double supply, boolean useCache) 
			throws Exception {
		double value = 0D;
		double demand = 0D;
		
		if (useCache) {
			if (goodsDemandCache.containsKey(equipmentGood)) demand = goodsDemandCache.get(equipmentGood);
			else throw new IllegalArgumentException("Good: " + equipmentGood + " not valid.");
		}
		else {
			// Determine demand amount.
			demand = determineEquipmentDemand(equipmentGood.getClassType());
			goodsDemandCache.put(equipmentGood, new Double(demand));
		}
		
		value = demand / (supply + 1D);
		
		return value;
	}
	
	/**
	 * Determines the demand for a type of equipment.
	 * @param equipmentClass the equipment class.
	 * @return demand (kg).
	 * @throws Exception if error getting demand.
	 */
	private double determineEquipmentDemand(Class equipmentClass) throws Exception {
		double numDemand = 0D;
		
		// Determine number of EVA suits that are needed
		if (EVASuit.class.equals(equipmentClass)) numDemand += 2D * settlement.getAllAssociatedPeople().size();
		
		// Determine the number of containers that are needed.
		if (Container.class.isAssignableFrom(equipmentClass)) numDemand = 10D;
		
		int areologistNum = getAreologistNum();
		
		// Determine number of bags that are needed.
		if (Bag.class.equals(equipmentClass)) {
			double iceValue = getGoodValuePerMass(GoodsUtil.getResourceGood(AmountResource.ICE));
			numDemand +=  CollectIce.REQUIRED_BAGS * areologistNum * iceValue;
		}
		
		// Determine number of specimen containers that are needed.
		if (SpecimenContainer.class.equals(equipmentClass)) 
			numDemand +=  Exploration.REQUIRED_SPECIMEN_CONTAINERS * areologistNum;
		
		// Convert demand from number to mass.
		double demand = numDemand * GoodsUtil.getGoodMassPerItem(GoodsUtil.getEquipmentGood(equipmentClass));
		
		return demand;
	}
	
	/**
	 * Gets the number of areologists associated with the settlement.
	 * @return number of areologists.
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
	 * Gets the number of drivers associated with the settlement.
	 * @return number of drivers.
	 */
	private int getDriverNum() {
		int result = 0;
		PersonIterator i = settlement.getAllAssociatedPeople().iterator();
		while (i.hasNext()) {
			if (i.next().getMind().getJob() instanceof Driver) result ++;
		}
		return result;
	}
	
	private int getTraderNum() {
		int result = 0;
		PersonIterator i = settlement.getAllAssociatedPeople().iterator();
		while (i.hasNext()) {
			if (i.next().getMind().getJob() instanceof Trader) result ++;
		}
		return result;
	}
	
	/**
	 * Gets the number of equipment for a settlement.
	 * @param equipmentClass the equipmentType to check.
	 * @return number of equipment for the settlement.
	 * @throws InventoryException if error getting the number of the equipment.
	 */
	private double getNumberOfEquipmentForSettlement(Class equipmentClass) throws InventoryException {
		double number = 0D;
		
		// Get number of the equipment in settlement storage.
		number += settlement.getInventory().findNumUnitsOfClass(equipmentClass);
		
		// Get number of resource out on mission vehicles.
		Iterator i = Simulation.instance().getMissionManager().getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = (Mission) i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement())) 
					number += vehicle.getInventory().findNumUnitsOfClass(equipmentClass);
			}
		}
		
		// Get number of resource carried by people on EVA.
		PersonIterator j = settlement.getAllAssociatedPeople().iterator();
		while (j.hasNext()) {
			Person person = j.next();
			if (person.getLocationSituation().equals(Person.OUTSIDE)) 
				number += person.getInventory().findNumUnitsOfClass(equipmentClass);
		}
		
		return number;
	}
	
	/**
	 * Determines the value of a vehicle good.
	 * @param vehicleGood the vehicle good.
	 * @param supply the current supply (kg) of the good.
	 * @param useCache use the cache to determine value.
	 * @return the value (value points / kg).
	 * @throws Exception if error determining vehicle value.
	 */
	private double determineVehicleGoodValue(Good vehicleGood, double supply, boolean useCache) throws Exception {
		double value = 0D;
		
		String vehicleType = vehicleGood.getName();
		
		boolean buy = false;
		VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
		double currentNum = getNumberOfVehiclesForSettlement(vehicleType);
		double currentSupply = currentNum * config.getEmptyMass(vehicleType);
		if (supply == currentSupply) buy = true;
		
		if (vehicleBuyValueCache == null) vehicleBuyValueCache = new HashMap<String, Double>();
		if (vehicleSellValueCache == null) vehicleSellValueCache = new HashMap<String, Double>();
		
		if (useCache) {
			if (buy) {
				if (vehicleBuyValueCache.containsKey(vehicleType)) value = vehicleBuyValueCache.get(vehicleType);
				else determineVehicleGoodValue(vehicleGood, supply, false);
			}
			else {
				if (vehicleSellValueCache.containsKey(vehicleType)) value = vehicleSellValueCache.get(vehicleType);
				else determineVehicleGoodValue(vehicleGood, supply, false);
			}
		}
		else {
			double travelToSettlementMissionValue = determineMissionVehicleValue(TRAVEL_TO_SETTLEMENT_MISSION, vehicleType, buy);
			if (travelToSettlementMissionValue > value) value = travelToSettlementMissionValue;
		
			double explorationMissionValue = determineMissionVehicleValue(EXPLORATION_MISSION, vehicleType, buy);
			if (explorationMissionValue > value) value = explorationMissionValue;
		
			double collectIceMissionValue = determineMissionVehicleValue(COLLECT_ICE_MISSION, vehicleType, buy);
			if (collectIceMissionValue > value) value = collectIceMissionValue;
		
			double rescueMissionValue = determineMissionVehicleValue(RESCUE_SALVAGE_MISSION, vehicleType, buy);
			if (rescueMissionValue > value) value = rescueMissionValue;
		
			double tradeMissionValue = determineMissionVehicleValue(TRADE_MISSION, vehicleType, buy);
			if (tradeMissionValue > value) value = tradeMissionValue;
			
			if (buy) vehicleBuyValueCache.put(vehicleType, value);
			else vehicleSellValueCache.put(vehicleType, value);
		}
		
		/*
		// Determine demand amount.
		double demand = 0D;
		
		if (useCache) {
			if (goodsDemandCache.containsKey(vehicleGood)) demand = goodsDemandCache.get(vehicleGood).doubleValue();
			else throw new IllegalArgumentException("Good: " + vehicleGood + " not valid.");
		}
		else {
			demand = determineVehicleDemand(vehicleType);
			goodsDemandCache.put(vehicleGood, new Double(demand));
		}
		
		value = demand / (supply + 1D);
		*/
		return value;
	}
	
	private double determineMissionVehicleValue(String missionType, String vehicleType, boolean buy) throws Exception {
		
		double demand = determineMissionVehicleDemand(missionType);
		
		double currentCapacity = 0D;
		boolean soldFlag = false;
		VehicleIterator i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			String type = i.next().getDescription().toLowerCase();
			if (!buy && !soldFlag && (type.equals(vehicleType))) soldFlag = true;
			else currentCapacity += determineMissionVehicleCapacity(missionType, type);
		}
		
		double vehicleCapacity = determineMissionVehicleCapacity(missionType, vehicleType);
		
		return (demand / (currentCapacity + 1D)) * vehicleCapacity;
	}
	
	private double determineMissionVehicleDemand(String missionType) {
		double demand = 0D;
		
		if (TRAVEL_TO_SETTLEMENT_MISSION.equals(missionType)) {
			demand = getDriverNum() / 2D;
			demand *= ((double) settlement.getAllAssociatedPeople().size() / 
					(double) settlement.getPopulationCapacity());
		}
		else if (EXPLORATION_MISSION.equals(missionType)) {
			demand = getAreologistNum() / 2D;
		}
		else if (COLLECT_ICE_MISSION.equals(missionType)) {
			demand = getGoodValuePerMass(GoodsUtil.getResourceGood(AmountResource.ICE));
		}
		else if (RESCUE_SALVAGE_MISSION.equals(missionType)) {
			demand = getDriverNum() / 4D;
		}
		else if (TRADE_MISSION.equals(missionType)) {
			demand = getTraderNum();
		}
		
		return demand;
	}
	
	private double determineMissionVehicleCapacity(String missionType, String vehicleType) throws Exception {
		double capacity = 0D;
		
		VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
		int crewCapacity = config.getCrewSize(vehicleType);
		
		if (TRAVEL_TO_SETTLEMENT_MISSION.equals(missionType)) {
			if (crewCapacity >= 2) capacity = 1D;
			capacity *= crewCapacity / 4D;
			
			double range = getVehicleRange(vehicleType);
			capacity *= range / 5000D;
		}
		else if (EXPLORATION_MISSION.equals(missionType)) {
			if (crewCapacity >= 2) capacity = 1D;
			
			double cargoCapacity = config.getTotalCapacity(vehicleType);
			if (cargoCapacity < 500D) capacity = 0D;
			
			if (config.hasLab(vehicleType)) {
				if (config.getLabTechSpecialities(vehicleType).contains("Areology")) capacity *= 2D;
			}
		}
		else if (COLLECT_ICE_MISSION.equals(missionType)) {
			if (crewCapacity >= 2) capacity = 1D;
			
			double cargoCapacity = config.getTotalCapacity(vehicleType);
			if (cargoCapacity < 1250D) capacity = 0D;
		}
		else if (RESCUE_SALVAGE_MISSION.equals(missionType)) {
			if (crewCapacity >= 2) capacity = 1D;
			
			double range = getVehicleRange(vehicleType);
			capacity *= range / 5000D;
		}
		else if (TRADE_MISSION.equals(missionType)) {
			if (crewCapacity >= 2) capacity = 1D;
			
			double cargoCapacity = config.getTotalCapacity(vehicleType);
			capacity *= cargoCapacity / 2000D;
			
			double range = getVehicleRange(vehicleType);
			capacity *= range / 5000D;
		}
		
		return capacity;
	}
	
	private double getVehicleRange(String vehicleType) throws Exception {
		double range = 0D;
		
		VehicleConfig vehicleConfig = SimulationConfig.instance().getVehicleConfiguration();
		double fuelCapacity = vehicleConfig.getCargoCapacity(vehicleType, AmountResource.METHANE.getName());
		double fuelEfficiency = vehicleConfig.getFuelEfficiency(vehicleType);
		range = fuelCapacity * fuelEfficiency / 1.5D;
		
		double baseSpeed = vehicleConfig.getBaseSpeed(vehicleType);
		double distancePerSol = baseSpeed / 2D / 60D / 60D / MarsClock.convertSecondsToMillisols(1D) * 1000D;
		
		PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
		int crewSize = vehicleConfig.getCrewSize(vehicleType);
		
    	// Check food capacity as range limit.
    	double foodConsumptionRate = personConfig.getFoodConsumptionRate();
    	double foodCapacity = vehicleConfig.getCargoCapacity(vehicleType, AmountResource.FOOD.getName());
    	double foodSols = foodCapacity / (foodConsumptionRate * crewSize);
    	double foodRange = distancePerSol * foodSols / 3D;
    	if (foodRange < range) range = foodRange;
    		
    	// Check water capacity as range limit.
    	double waterConsumptionRate = personConfig.getWaterConsumptionRate();
    	double waterCapacity = vehicleConfig.getCargoCapacity(vehicleType, AmountResource.WATER.getName());
    	double waterSols = waterCapacity / (waterConsumptionRate * crewSize);
    	double waterRange = distancePerSol * waterSols / 3D;
    	if (waterRange < range) range = waterRange;
    		
    	// Check oxygen capacity as range limit.
    	double oxygenConsumptionRate = personConfig.getOxygenConsumptionRate();
    	double oxygenCapacity = vehicleConfig.getCargoCapacity(vehicleType, AmountResource.OXYGEN.getName());
    	double oxygenSols = oxygenCapacity / (oxygenConsumptionRate * crewSize);
    	double oxygenRange = distancePerSol * oxygenSols / 3D;
    	if (oxygenRange < range) range = oxygenRange;
		
		return range;
	}
	
	/**
	 * Gets the number of the vehicle for the settlement.
	 * @param vehicleType the vehicle type.
	 * @return the number of vehicles.
	 * @throws InventoryException if error getting the amount.
	 */
	private double getNumberOfVehiclesForSettlement(String vehicleType) throws InventoryException {
		double number = 0D;
		
		VehicleIterator i = settlement.getAllAssociatedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();
			if (vehicleType.equalsIgnoreCase(vehicle.getDescription())) number += 1D;
		}
		
		return number;
	}
	
	/**
	 * Determine the number vehicles needed by the settlement.
	 * @param vehicleType the vehicle type.
	 * @return the vehicle mass needed (kg).
	 * @throws Exception if error determining demand.
	 */
	/*
	private double determineVehicleDemand(String vehicleType) throws Exception {
		double numDemand = 0D;
		
		if (vehicleType.equals("explorer rover")) {
			numDemand += getAreologistNum() / 2D;
			numDemand += getGoodValuePerMass(GoodsUtil.getResourceGood(AmountResource.ICE));
		}
		else if (vehicleType.equals("transport rover")) {
			numDemand += getDriverNum() / 2D;
			numDemand *= ((double) settlement.getAllAssociatedPeople().size() / 
					(double) settlement.getPopulationCapacity());
		}
		
		// Convert demand from number to mass.
		double demand = numDemand * GoodsUtil.getGoodMassPerItem(GoodsUtil.getVehicleGood(vehicleType));
		
		return demand;
	}
	*/
	
	/**
	 * Determines the trade value for a good at a settlement.
	 * @param good the good.
	 * @param useTradeCache use the goods trade cache to determine trade value?
	 * @return the trade value (VP/kg).
	 * @throws Exception if error determining trade value.
	 */
	private double determineTradeValue(Good good, boolean useTradeCache) throws Exception {
		if (useTradeCache) {
			if (goodsTradeCache.containsKey(good)) return goodsTradeCache.get(good).doubleValue();
			else throw new IllegalArgumentException("good: " + good + " not valid.");
		}
		else {
			double bestTradeValue = 0D;
		
			SettlementIterator i = Simulation.instance().getUnitManager().getSettlements().iterator();
			while (i.hasNext()) {
				Settlement tempSettlement = i.next();
				if (tempSettlement != settlement) {
					double baseValue = tempSettlement.getGoodsManager().getGoodValuePerMass(good);
					double distance = settlement.getCoordinates().getDistance(tempSettlement.getCoordinates());
					double tradeValue = baseValue / (1D + (distance / 1000D));
					if (tradeValue > bestTradeValue) bestTradeValue = tradeValue;
				}
			}
			goodsTradeCache.put(good, new Double(bestTradeValue));
			return bestTradeValue;
		}
	}
}
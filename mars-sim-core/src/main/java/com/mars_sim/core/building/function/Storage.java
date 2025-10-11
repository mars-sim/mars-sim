/*
 * Mars Simulation Project
 * Storage.java
 * @date 2025-07-15
 * @author Scott Davis
 */
package com.mars_sim.core.building.function;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.config.FunctionSpec;
import com.mars_sim.core.building.config.StorageSpec;
import com.mars_sim.core.equipment.EquipmentInventory;
import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;

/**
 * The storage class is a building function for storing resources and units.
 */
public class Storage extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Storage.class.getName());
	
	/** The capacities of each resource. */
	private Map<Integer, Double> resourceCapacities;


	/**
	 * Constructor.
	 *
	 * @param building the building the function is for.
	 * @param spec Spec of the Storage capability
	 * @throws BuildingException if function cannot be constructed.
	 */
	public Storage(Building building, FunctionSpec spec) {
		// Use Function constructor.
		super(FunctionType.STORAGE, spec, building);

		// Get capacity for each resource.
		StorageSpec storageSpec = (StorageSpec) spec;
		resourceCapacities = storageSpec.getCapacityResources();

		// Note: Storing a resource in a building is equivalent to storing it in a settlement.

		// Get the owner of this building 
		Settlement owner = building.getSettlement();
		// Set up equipment inventory
		EquipmentInventory inv = owner.getEquipmentInventory();
		
		// Set the specific capacities of resources from this building.
		inv.setResourceCapacityMap(resourceCapacities, true);
		
		// Get the stock/general/cargo capacity of this building
		double stockCapacity = spec.getStockCapacity();
		
		// Add the stock/general/cargo capacity of this building to its owner
		inv.addCargoCapacity(stockCapacity);
	
		// Account for the initial specific resources available for each building
		Map<Integer, Double> initialResources = storageSpec.getInitialResources();
		
		double totalAmount = 0;
		
		// Add initial resources for this building.
		for (Entry<Integer, Double> i : initialResources.entrySet()) {

			double initialAmount = i.getValue();
			totalAmount += initialAmount;
			int resourceId = i.getKey();

			// Future: need to work out a way to store these excess resources into appropriate containers
			
			// Stores this resource in this building.
			inv.storeAmountResource(resourceId, initialAmount);
		}		

		if (stockCapacity < totalAmount)
			logger.warning(building, "Initial resources overloaded by " + Math.round((totalAmount - stockCapacity)*100.0)/100.0 + " kg ");
	}

	/**
	 * Gets the value of the function for a named building.
	 *
	 * @param buildingName the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding, Settlement settlement) {

		double result = 0D;

		var spec = buildingConfig.getFunctionSpec(buildingName, FunctionType.STORAGE);
		if (spec instanceof StorageSpec ss) { 
			for(Entry<Integer, Double> e : ss.getCapacityResources().entrySet()) {
				Integer resource = e.getKey();
				double storageAmount = e.getValue();

				double existingStorage = 0D;
				for(Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.STORAGE)) {
					Storage storageFunction = building.getStorage();
					double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
					existingStorage += storageFunction.resourceCapacities.getOrDefault(resource, 0D) * wearModifier;
				}

				if (!newBuilding) {
					existingStorage -= storageAmount;
					if (existingStorage < 0D)
						existingStorage = 0D;
				}

				double resourceValue = settlement.getGoodsManager().getGoodValuePoint(resource);
				double resourceStored = settlement.getSpecificAmountResourceStored(resource);
				double resourceDemand = resourceValue * (resourceStored + 1D);

				double currentStorageDemand = resourceDemand - existingStorage;
				if (currentStorageDemand < 0D)
					currentStorageDemand = 0D;

				// Determine amount of this building's resource storage is useful to the
				// settlement.
				double buildingStorageNeeded = Math.min(currentStorageDemand, storageAmount);

				double storageValue = buildingStorageNeeded / 1000D;

				result += storageValue;
			}
		}

		return result;
	}

	/**
	 * Gets a map of the resource capacities this building is capable of storing in kg.
	 *
	 * @return Map of resource keys and amount Double values.
	 */
	public Map<Integer, Double> getResourceStorageCapacity() {
		return resourceCapacities;
	}

	@Override
	public void removeFromSettlement() {
		// Remove excess amount resources that can no longer be stored.
		removeResources();
		// Remove storage capacity from settlement.
		removeStorageCapacity();
	}

	private void removeResources() {
		ResourceHolder s = getBuilding().getSettlement();

		// Remove excess amount resources that can no longer be stored.
		for (var e : resourceCapacities.entrySet()) {
			Integer resource = e.getKey();
			double storageCapacityAmount = e.getValue();
			double totalStorageCapacityAmount = s.getSpecificCapacity(resource);

			double remainingStorageCapacityAmount = totalStorageCapacityAmount - storageCapacityAmount;
			double totalStoredAmount = s.getSpecificAmountResourceStored(resource);
			if (remainingStorageCapacityAmount < totalStoredAmount) {
				double resourceAmountRemoved = totalStoredAmount - remainingStorageCapacityAmount;
				s.retrieveAmountResource(resource, resourceAmountRemoved);
			}
		}
	}

	public void removeStorageCapacity() {
		EquipmentInventory inv = getBuilding().getSettlement().getEquipmentInventory();

		// Remove storage capacity from settlement.
		Iterator<Integer> j = resourceCapacities.keySet().iterator();
		while (j.hasNext()) {
			Integer resource = j.next();
			double storageCapacityAmount = resourceCapacities.get(resource);
			inv.removeSpecificCapacity(resource, storageCapacityAmount);
		}
	}

	@Override
	public double getMaintenanceTime() {
		return resourceCapacities.size() * 2D;
	}

	/**
	 * Stores a resource.
	 *
	 * @param amount
	 * @param ar     {@link AmountResource}
	 * @param inv    {@link Inventory}
	 * @param method the name of the calling java method
	 * @return true if it is being stored properly
	 */
	public static boolean storeAnResource(double amount, int id, ResourceHolder rh, String method) {
		boolean result = false;

		if (amount > 0) {
			
			double excess = rh.storeAmountResource(id, amount);
			
			if (excess == 0.0) {
				return true;
			}
			else if (excess > 0) {
				logger.log(rh, Level.INFO, 60_000, method
		    		+ "Storage full for "
		    		+ ResourceUtil.findAmountResourceName(id) 
		    		+ ". To store: "
			    	+ Math.round(amount*10000.0)/10000.0
			    	+ " kg"
		    		+ ". Stored: "
			    	+ Math.round((amount-excess)*10000.0)/10000.0
			    	+ " kg"
			    	);
				
				return false;
			}
		}
		
		else if (!method.equals("")) {
			logger.log(rh, Level.SEVERE, 10_000,
				"Attempting to store non-positive amount of "
				+ ResourceUtil.findAmountResourceName(id) + " at " + method);
		}

		return result;
	}

	/**
	 * Retrieves a resource or test if a resource is available.
	 *
	 * @param requestedAmount
	 * @param id
	 * @param inv
	 * @param isRetrieving
	 * @return true if the 'full' amount can be retrieved.
	 */
	public static boolean retrieveAnResource(double amount, int id, ResourceHolder rh, boolean isRetrieving) {
		boolean result = false;
		if (amount > 0) {
			double amountStored = rh.getSpecificAmountResourceStored(id);

			if (amountStored < 0.00001) {
				result = false;

			} else if (amountStored < amount) {
				amount = amountStored;
				if (isRetrieving) {
					rh.retrieveAmountResource(id, amount);
				}
				logger.warning(rh, 30_000,
						"Ran out of "
						+ ResourceUtil.findAmountResourceName(id) + "."
						);
				result = false;

			} else {
				if (isRetrieving) {
					rh.retrieveAmountResource(id, amount);
				}
				result = true;
			}
		}
		else {
			result = false;
			logger.severe(rh, 10_000,
					"Attempting to retrieve non-positive amount of "
					+ ResourceUtil.findAmountResourceName(id));
		}

		return result;
	}

	@Override
	public void destroy() {
		super.destroy();
		resourceCapacities = null;
	}
}

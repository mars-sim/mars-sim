/*
 * Mars Simulation Project
 * Storage.java
 * @date 2022-07-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.mars_sim.msp.core.equipment.EquipmentInventory;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.FunctionSpec;

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
		resourceCapacities = buildingConfig.getStorageCapacities(building.getBuildingType());

		// Note: Storing a resource in a building is equivalent to storing it in a settlement.

		// Get the owner of this building 
		Settlement owner = building.getSettlement();
		// Set up equipment inventory
		EquipmentInventory inv = owner.getEquipmentInventory();
		
		// Set the specific capacities of resources from this building.
		inv.setResourceCapacityMap(resourceCapacities, true);
		
		// Get the stock/general/cargo capacity
		double stockCapacity = spec.getCapacity();
		// Add the stock/general/cargo capacity to this building.
		inv.addCargoCapacity(stockCapacity);

		// Account for the initial specific resources available for each building
		Map<Integer, Double> initialResources = buildingConfig.getInitialResources(building.getBuildingType());
		// Add initial resources to this building.
		for (Entry<Integer, Double> i : initialResources.entrySet()) {
			double initialAmount = i.getValue();
			int resourceId = i.getKey();
			// Stores this resource in this building.
			double excess = inv.storeAmountResource(resourceId, initialAmount);
			if (excess > 0D) {
				String resourceName = ResourceUtil.findAmountResourceName(resourceId);
				logger.warning(building, 
						"Still lacking " + excess + " kg "
						+ resourceName
						+ " storage space. " + (initialAmount - excess) + " kg stored. "
						);
			}
		}
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

		Map<Integer, Double> storageMap = buildingConfig.getStorageCapacities(buildingName);
		Iterator<Integer> i = storageMap.keySet().iterator();
		while (i.hasNext()) {
			Integer resource = i.next();
			double existingStorage = 0D;
			Iterator<Building> j = settlement.getBuildingManager().getBuildingSet(FunctionType.STORAGE).iterator();
			while (j.hasNext()) {
				Building building = j.next();
				Storage storageFunction = building.getStorage();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				if (storageFunction.resourceCapacities.containsKey(resource))
					existingStorage += storageFunction.resourceCapacities.get(resource) * wearModifier;
			}

			double storageAmount = storageMap.get(resource);

			if (!newBuilding) {
				existingStorage -= storageAmount;
				if (existingStorage < 0D)
					existingStorage = 0D;
			}

			double resourceValue = settlement.getGoodsManager().getGoodValuePoint(resource);
			double resourceStored = settlement.getAmountResourceStored(resource);
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

	public void removeResources() {
		// Remove excess amount resources that can no longer be stored.
		Iterator<Integer> i = resourceCapacities.keySet().iterator();
		while (i.hasNext()) {
			Integer resource = i.next();
			double storageCapacityAmount = resourceCapacities.get(resource);
			double totalStorageCapacityAmount = getBuilding().getSettlement().getAmountResourceCapacity(resource);
			double remainingStorageCapacityAmount = totalStorageCapacityAmount - storageCapacityAmount;
			double totalStoredAmount = getBuilding().getSettlement().getAmountResourceStored(resource);
			if (remainingStorageCapacityAmount < totalStoredAmount) {
				double resourceAmountRemoved = totalStoredAmount - remainingStorageCapacityAmount;
				getBuilding().getSettlement().retrieveAmountResource(resource, resourceAmountRemoved);
			}
		}
	}

	public void removeStorageCapacity() {
		// Remove storage capacity from settlement.
		Iterator<Integer> j = resourceCapacities.keySet().iterator();
		while (j.hasNext()) {
			Integer resource = j.next();
			double storageCapacityAmount = resourceCapacities.get(resource);
			getBuilding().getSettlement().getEquipmentInventory().removeCapacity(resource, storageCapacityAmount);
		}
	}

	@Override
	public double getMaintenanceTime() {
		return 10D;
	}


//	/**
//	 * Stores a resource.
//	 *
//	 * @param amount
//	 * @param ar
//	 * @param inv
//	 * @return true if all mounts is being stored properly
//	 */
//	public static boolean storeAnResource(double amount, AmountResource ar, ResourceHolder rh) {
//		return storeAnResource(amount, ar, rh, "");
//	}
//
//	/**
//	 * Stores a resource.
//	 *
//	 * @param amount
//	 * @param ar     {@link AmountResource}
//	 * @param inv    {@link Inventory}
//	 * @param method the name of the calling java method
//	 * @return true if all amounts is being stored properly
//	 */
//	public static boolean storeAnResource(double amount, AmountResource ar, ResourceHolder rh, String method) {
//		return storeAnResource(amount, ar.getID(), rh, method);
//	}
//
//	/**
//	 * Stores a resource.
//	 *
//	 * @param name
//	 * @param Amount
//	 * @param inv
//	 * @return true if all amounts is being stored properly
//	 */
//	public static boolean storeAnResource(double amount, String name, ResourceHolder rh) {
//		return storeAnResource(amount, ResourceUtil.findIDbyAmountResourceName(name), rh, "");
//	}

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
				logger.log(rh.getHolder(), Level.INFO, 60_000, method
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

//			try {
//				double remainingCapacity = rh.getAmountResourceRemainingCapacity(id);
//
//				if (remainingCapacity < amount) {
////					// TODO: how to increase VP of barrel/bag/gas canister for storage to prompt for
////					// manufacturing more of them
//
//					if (!method.equals(""))
//						method = " at " + method;
//					logger.log(rh.getHolder(), Level.SEVERE, 30_000, method
//				    		+ "The storage capacity for "
//				    		+ ResourceUtil.findAmountResourceName(id) + " has been reached. Only "
//					    	+ Math.round(remainingCapacity*10000.0)/10000.0
//					    	+ " kg can be stored."
//				    	);
//					rh.storeAmountResource(id, remainingCapacity);
//
//					result = false;
//				}
//
//				else {
//					rh.storeAmountResource(id, amount);
//					result = true;
//				}
//
//			} catch (Exception e) {
//				logger.log(rh.getHolder(), Level.SEVERE, 10_000,
//						"Issues with storeAmountResource on " + ResourceUtil.findAmountResourceName(id) + " : ", e);
//			}
		}
		
		else {
			result = false;
			if (!method.equals(""))
				method = " at " + method;
				logger.log(rh.getHolder(), Level.SEVERE, 10_000,
					"Attempting to store non-positive amount of "
					+ ResourceUtil.findAmountResourceName(id) + method);
		}

		return result;
	}

	/**
	 * Retrieves a resource or test if a resource is available.
	 *
	 * @param requestedAmount
	 * @param ar
	 * @param inv
	 * @param isRetrieving
	 * @return true if the full amount can be retrieved.
	 */
	public static boolean retrieveAnResource(double requestedAmount, AmountResource ar, ResourceHolder rh, boolean isRetrieving) {
		return retrieveAnResource(requestedAmount, ar.getID(), rh, isRetrieving);
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
			try {
				double amountStored = rh.getAmountResourceStored(id);

				if (amountStored < 0.00001) {
					result = false;

				} else if (amountStored < amount) {
					amount = amountStored;
					if (isRetrieving) {
						rh.retrieveAmountResource(id, amount);
					}
					logger.log(rh.getHolder(), Level.WARNING, 30_000,
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
			} catch (Exception e) {
				logger.log(rh.getHolder(), Level.SEVERE, 10_000,
						"Issues with retrieveAnResource(ar) on "
						+ ResourceUtil.findAmountResourceName(id) + " : " + e.getMessage(), e);
			}
		} else {
			result = false;
			logger.log(rh.getHolder(), Level.SEVERE, 10_000,
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

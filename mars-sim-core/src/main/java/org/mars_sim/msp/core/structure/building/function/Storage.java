/**
 * Mars Simulation Project
 * Storage.java
 * @version 3.1.0 2017-09-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;

/**
 * The storage class is a building function for storing resources and units.
 */
public class Storage extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
	private static Logger logger = Logger.getLogger(Storage.class.getName());
	// private static org.apache.log4j.Logger log4j = LogManager.getLogger(Storage.class);
	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	private Map<Integer, Double> resourceCapacities;
	
	private static final FunctionType FUNCTION = FunctionType.STORAGE;

	/**
	 * Constructor.
	 * 
	 * @param building the building the function is for.
	 * @throws BuildingException if function cannot be constructed.
	 */
	public Storage(Building building) {
		// Use Function constructor.
		super(FUNCTION, building);

		Inventory inv = building.getInventory();
		// Get capacity for each resource.
		resourceCapacities = buildingConfig.getStorageCapacities(building.getBuildingType());

		// Initialize resource capacities for this building.
		Set<Integer> capSet = resourceCapacities.keySet();
		for (Integer ar : capSet) {
			double capacity = resourceCapacities.get(ar);
			// Note : A capacity of a resource in a settlement is the sum of the capacity of
			// the same resource
			// in all buildings of that settlement
			inv.addAmountResourceTypeCapacity(ar, capacity);
		}

		double stockCapacity = buildingConfig.getStockCapacity(building.getBuildingType());
		inv.addGeneralCapacity(stockCapacity);

		// Initialize stock capacities for all resource
//		 for (AmountResource ar : ResourceUtil.getInstance().getAmountResources()) {
//			 if (!building.getBuildingType().toLowerCase().contains("storage bin"))
//				 inv.addAmountResourceTypeCapacity(ar, stockCapacity);
//		 }

		// Fill up initial resources for this building.
		Map<Integer, Double> initialResources = buildingConfig.getInitialResources(building.getBuildingType());
		Set<Integer> initialSet = initialResources.keySet();
		for (Integer ar : initialSet) {
			double initialAmount = initialResources.get(ar);
			double remainingCap = inv.getAmountResourceRemainingCapacity(ar, true, false);

			if (initialAmount > remainingCap)
				initialAmount = remainingCap;
			inv.addAmountResourceTypeCapacity(ar, initialAmount);
			inv.storeAmountResource(ar, initialAmount, true);
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
			Iterator<Building> j = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
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

//			Good resourceGood = GoodsUtil.getResourceGood(ResourceUtil.findIDbyAmountResourceName(resource.getName()));
			double resourceValue = settlement.getGoodsManager().getGoodValuePerItem(resource);
			double resourceStored = settlement.getInventory().getAmountResourceStored(resource, false);
			double resourceDemand = resourceValue * (resourceStored + 1D);

			double currentStorageDemand = resourceDemand - existingStorage;
			if (currentStorageDemand < 0D)
				currentStorageDemand = 0D;

			// Determine amount of this building's resource storage is useful to the
			// settlement.
			double buildingStorageNeeded = storageAmount;
			if (currentStorageDemand < storageAmount) {
				buildingStorageNeeded = currentStorageDemand;
			}

			double storageValue = buildingStorageNeeded / 1000D;

			result += storageValue;
		}

		return result;
	}

	/**
	 * Gets a map of the resources this building is capable of storing and their
	 * amounts in kg.
	 * 
	 * @return Map of resource keys and amount Double values.
	 */
	public Map<Integer, Double> getResourceStorageCapacity() {
		return resourceCapacities;
	}

	/**
	 * Time passing for the building.
	 * 
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return 0D;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * 
	 * @return power (kW)
	 */
	public double getPoweredDownPowerRequired() {
		return 0D;
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
			Inventory inv = getBuilding().getSettlementInventory();
			double totalStorageCapacityAmount = inv.getAmountResourceCapacity(resource, false);
			double remainingStorageCapacityAmount = totalStorageCapacityAmount - storageCapacityAmount;
			double totalStoredAmount = inv.getAmountResourceStored(resource, false);
			if (remainingStorageCapacityAmount < totalStoredAmount) {
				double resourceAmountRemoved = totalStoredAmount - remainingStorageCapacityAmount;
				inv.retrieveAmountResource(resource, resourceAmountRemoved);
			}
		}
	}
	
	public void removeStorageCapacity() {
		// Remove storage capacity from settlement.
		Iterator<Integer> j = resourceCapacities.keySet().iterator();
		while (j.hasNext()) {
			Integer resource = j.next();
			double storageCapacityAmount = resourceCapacities.get(resource);
			Inventory inv = getBuilding().getSettlementInventory();
			inv.removeAmountResourceTypeCapacity(resource, storageCapacityAmount);
		}
	}
	
	@Override
	public double getMaintenanceTime() {
		return 10D;
	}

	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Stores a resource
	 * 
	 * @param amount
	 * @param ar
	 * @param inv
	 * @return true if all mounts is being stored properly
	 */
	public static boolean storeAnResource(double amount, AmountResource ar, Inventory inv) {
		return storeAnResource(amount, ar, inv, "");
	}

	/**
	 * Stores a resource
	 * 
	 * @param amount
	 * @param ar     {@link AmountResource}
	 * @param inv    {@link Inventory}
	 * @param method the name of the calling java method
	 * @return true if all mounts is being stored properly
	 */
	public static boolean storeAnResource(double amount, AmountResource ar, Inventory inv, String method) {
		return storeAnResource(amount, ar.getID(), inv, method);
	}

	/**
	 * Stores a resource
	 * 
	 * @param name
	 * @param Amount
	 * @param inv
	 * @return true if all mounts is being stored properly
	 */
	public static boolean storeAnResource(double amount, String name, Inventory inv) {
		return storeAnResource(amount, ResourceUtil.findIDbyAmountResourceName(name), inv, "");
	}

	/**
	 * Stores a resource
	 * 
	 * @param amount
	 * @param ar     {@link AmountResource}
	 * @param inv    {@link Inventory}
	 * @param method the name of the calling java method
	 * @return true if all mounts is being stored properly
	 */
	public static boolean storeAnResource(double amount, int id, Inventory inv, String method) {
		boolean result = false;

		if (amount > 0) {
			try {
				double remainingCapacity = inv.getAmountResourceRemainingCapacity(id, true, false);
				// double stored = inv.getAmountResourceStored(ar, false);
				if (remainingCapacity < 0.00001) {
					result = false;
					// TODO: increase VP of barrel/bag/gas canister for storage to prompt for
					// manufacturing them
					
					// Vent or drain 1% of resource
					double ventAmount = 0.01 * inv.getAmountResourceCapacity(id, false);
					inv.retrieveAmountResource(id, ventAmount);
//					LogConsolidated.log(Level.WARNING, 10_000, sourceName + "::" + method, 
//							"[" + inv.getOwner()
//				    		+ "] No more room to store " + Math.round(amount*100.0)/100.0 + " kg of "
//				    		+ ResourceUtil.findAmountResourceName(id) + ". Venting ", null);
					
					// Adjust the grey water filtering rate
					if (id == ResourceUtil.greyWaterID && inv.getOwner() instanceof Settlement) {
						Settlement s = (Settlement)(inv.getOwner());
						s.increaseGreyWaterFilteringRate();
						double r = s.getGreyWaterFilteringRate();
						LogConsolidated.log(Level.WARNING, 10_000, sourceName + "::" + method, 
								"[" + s
					    		+ "] Updated the grey water filtering rate to " + Math.round(r*100.0)/100.0 + ".");
					}
				}

				else if (remainingCapacity < amount) {
					// double stored = inv.getAmountResourceStored(ar, false);
					// if the remaining capacity is smaller than the harvested amount, set remaining
					// capacity to full
					if (!method.equals(""))
						method = " at " + method;
				    LogConsolidated.log(Level.SEVERE, 30_000, sourceName + method, 
				    		"[" + inv.getOwner()
				    		+ "] The storage capacity for " 
				    		+ ResourceUtil.findAmountResourceName(id) + " has been reached. Only "
					    	+ Math.round(remainingCapacity*10000.0)/10000.0 
					    	+ " kg can be stored."  
					    	//+ " (Remaining capacity : " + Math.round(remainingCapacity*100.0)/100.0
					    	//+ " (Stored : " + Math.round(stored*100.0)/100.0
					    	//+ ")"
				    	);	
					amount = remainingCapacity;
					inv.storeAmountResource(id, amount, true);
//					inv.addAmountSupply(id, amount);
					result = false;
				}

				else {
					inv.storeAmountResource(id, amount, true);
//					inv.addAmountSupply(id, amount);
					result = true;
				}

			} catch (Exception e) {
				e.printStackTrace(System.err);
				LogConsolidated.log(Level.SEVERE, 10_000, sourceName,
						"[" + inv.getOwner()
			    		+ "] Issues with (int) storeAnResource on " + ResourceUtil.findAmountResourceName(id) + " : " + e.getMessage(), e);
			}
		} else {
			result = false;
			if (!method.equals(""))
				method = " at " + method;
			LogConsolidated.log(Level.SEVERE, 10_000, sourceName, "[" + inv.getOwner()
    		+ "] Attempting to store non-positive amount of "
					+ ResourceUtil.findAmountResourceName(id) + method);
		}

		return result;
	}

	/**
	 * Retrieves a resource or test if a resource is available
	 * 
	 * @param name
	 * @param requestedAmount
	 * @param inv
	 * @param isRetrieving
	 * @return true if the full amount can be retrieved.
	 */
	public static boolean retrieveAnResource(double requestedAmount, String name, Inventory inv, boolean isRetrieving) {
		return retrieveAnResource(requestedAmount, ResourceUtil.findIDbyAmountResourceName(name), inv, isRetrieving);
	}

	/**
	 * Retrieves a resource or test if a resource is available
	 * 
	 * @param requestedAmount
	 * @param ar
	 * @param inv
	 * @param isRetrieving
	 * @return true if the full amount can be retrieved.
	 */
	public static boolean retrieveAnResource(double requestedAmount, AmountResource ar, Inventory inv, boolean isRetrieving) {
		return retrieveAnResource(requestedAmount, ar.getID(), inv, isRetrieving);
	}

	/**
	 * Retrieves a resource or test if a resource is available
	 * 
	 * @param requestedAmount
	 * @param id
	 * @param inv
	 * @param isRetrieving
	 * @return true if the 'full' amount can be retrieved.
	 */
	public static boolean retrieveAnResource(double amount, int id, Inventory inv, boolean isRetrieving) {
		boolean result = false;
		if (amount > 0) {
			try {
				double amountStored = inv.getAmountResourceStored(id, false);
//				inv.addAmountDemandTotalRequest(id);

				if (amountStored < 0.00001) {
					result = false;
					if (id == ResourceUtil.greyWaterID && inv.getOwner() instanceof Settlement) {
						Settlement s = (Settlement)(inv.getOwner());
						// Adjust the grey water filtering rate
						s.decreaseGreyWaterFilteringRate();
						double r = s.getGreyWaterFilteringRate();
						LogConsolidated.log(Level.WARNING, 1_000, sourceName, 
								"[" + s
					    		+ "] Updated the new grey water filtering rate to " + Math.round(r*100.0)/100.0 + ".");
					}
				
				} else if (amountStored < amount) {
					amount = amountStored;
					if (isRetrieving) {
						inv.retrieveAmountResource(id, amount);
//						inv.addAmountDemand(id, amount);
					}
					LogConsolidated.log(Level.WARNING, 30_000, sourceName,
							"[" + inv.getOwner()
				    		+ "] ran out of "
							+ ResourceUtil.findAmountResourceName(id) + "."
							);
					result = false;
				
				} else {
					if (isRetrieving) {
						inv.retrieveAmountResource(id, amount);
//						inv.addAmountDemand(id, amount);
					}
					result = true;
				}
			} catch (Exception e) {
				e.printStackTrace(System.err);
				LogConsolidated.log(Level.SEVERE, 10_000, sourceName, "[" + inv.getOwner()
	    		+ "] Issues with retrieveAnResource(ar) on "
						+ ResourceUtil.findAmountResourceName(id) + " : " + e.getMessage(), e);
			}
		} else {
			result = false;
			LogConsolidated.log(Level.SEVERE, 10_000, sourceName, "[" + inv.getOwner()
    		+ "] Attempting to retrieve non-positive amount of "
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
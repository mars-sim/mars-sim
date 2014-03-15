/**
 * Mars Simulation Project
 * Storage.java
 * @version 3.06 2014-03-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;

/**
 * The storage class is a building function for storing resources and units.
 */
public class Storage
extends Function
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final BuildingFunction FUNCTION = BuildingFunction.STORAGE;

	private Map<AmountResource, Double> storageCapacity;

	/**
	 * Constructor.
	 * @param building the building the function is for.
	 * @throws BuildingException if function cannot be constructed.
	 */
	public Storage(Building building) {
		// Use Function constructor.
		super(FUNCTION, building);

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		Inventory inventory = building.getInventory();	

		// Get building resource capacity.
		storageCapacity = config.getStorageCapacities(building.getName());
		Iterator<AmountResource> i1 = storageCapacity.keySet().iterator();
		while (i1.hasNext()) {
			AmountResource resource = i1.next();
			double currentCapacity = inventory.getAmountResourceCapacity(resource, false);
			double buildingCapacity = (Double) storageCapacity.get(resource);
			inventory.addAmountResourceTypeCapacity(resource, currentCapacity + buildingCapacity);
		}

		// Get initial resources in building.
		Map<AmountResource, Double> initialResources = config.getInitialStorage(building.getName());
		Iterator<AmountResource> i2 = initialResources.keySet().iterator();
		while (i2.hasNext()) {
			AmountResource resource = i2.next();
			double initialResource = (Double) initialResources.get(resource);
			double resourceCapacity = inventory.getAmountResourceRemainingCapacity(resource, true, false);
			if (initialResource > resourceCapacity) initialResource = resourceCapacity;
			inventory.storeAmountResource(resource, initialResource, true);
		}
	}

	/**
	 * Gets the value of the function for a named building.
	 * @param buildingName the building name.
	 * @param newBuilding true if adding a new building.
	 * @param settlement the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding,
			Settlement settlement) {

		double result = 0D;

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

		Map<AmountResource, Double> storageMap = config.getStorageCapacities(buildingName);
		Iterator<AmountResource> i = storageMap.keySet().iterator();
		while (i.hasNext()) {
			AmountResource resource = i.next();

			double existingStorage = 0D;
			Iterator<Building> j = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
			while (j.hasNext()) {
				Building building = j.next();
				Storage storageFunction = (Storage) building.getFunction(FUNCTION);
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * 
						.75D + .25D;
				if (storageFunction.storageCapacity.containsKey(resource))
					existingStorage += storageFunction.storageCapacity.get(resource) * wearModifier;
			}

			double storageAmount = storageMap.get(resource);

			if (!newBuilding) {
				existingStorage -= storageAmount;
				if (existingStorage < 0D) existingStorage = 0D;
			}

			Good resourceGood = GoodsUtil.getResourceGood(resource);
			double resourceValue = settlement.getGoodsManager().getGoodValuePerItem(resourceGood);
			double resourceStored = settlement.getInventory().getAmountResourceStored(resource, false);
			double resourceDemand = resourceValue * (resourceStored + 1D);

			double currentStorageDemand = resourceDemand - existingStorage;
			if (currentStorageDemand < 0D) currentStorageDemand = 0D;

			double buildingStorageNeeded = currentStorageDemand;
			if (buildingStorageNeeded > storageAmount) buildingStorageNeeded = storageAmount;

			result += buildingStorageNeeded / 1000D;

			// Add overflow storage demand.
			double overflowAmount = resourceStored - existingStorage;
			if (overflowAmount > 0D) {
				if (overflowAmount > storageAmount) {
					overflowAmount = storageAmount;
				}
				result += overflowAmount;
			}
		}

		return result;
	}

	/** 
	 * Gets a map of the resources this building is capable of
	 * storing and their amounts in kg.
	 * @return Map of resource keys and amount Double values.
	 */
	public Map<AmountResource, Double> getResourceStorageCapacity() {
		return storageCapacity;
	}

	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {}

	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return 0D;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * @return power (kW)
	 */
	public double getPowerDownPowerRequired() {
		return 0D;
	}

	@Override
	public void removeFromSettlement() {

		// Remove excess amount resources that can no longer be stored.
		Iterator<AmountResource> i = storageCapacity.keySet().iterator();
		while (i.hasNext()) {
			AmountResource resource = i.next();
			double storageCapacityAmount = storageCapacity.get(resource);
			Inventory inv = getBuilding().getInventory();
			double totalStorageCapacityAmount = inv.getAmountResourceCapacity(resource, false);
			double remainingStorageCapacityAmount = totalStorageCapacityAmount - storageCapacityAmount;
			double totalStoredAmount = inv.getAmountResourceStored(resource, false);
			if (remainingStorageCapacityAmount < totalStoredAmount) {
				double resourceAmountRemoved = totalStoredAmount - remainingStorageCapacityAmount;
				inv.retrieveAmountResource(resource, resourceAmountRemoved);
			}
		}

		// Remove storage capacity from settlement.
		Iterator<AmountResource> j = storageCapacity.keySet().iterator();
		while (j.hasNext()) {
			AmountResource resource = j.next();
			double storageCapacityAmount = storageCapacity.get(resource);
			Inventory inv = getBuilding().getInventory();
			inv.removeAmountResourceTypeCapacity(resource, storageCapacityAmount);
		}
	}

	@Override
	public double getMaintenanceTime() {
		return 10D;
	}

	@Override
	public void destroy() {
		super.destroy();

		storageCapacity.clear();
		storageCapacity = null;
	}
}
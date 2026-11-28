/*
 * Mars Simulation Project
 * LoadingController.java
 * @date 2025-07-21
 * @author Barry Evans
 */
package com.mars_sim.core.vehicle.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.ResourceType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resource.SuppliesManifest;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * This class control the loading of a Vehicle of resources.
 * It creates a manifest that is depleted as resources are loaded.
 */
public class LoadingController implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The amount of resources (kg) one person of average strength can load per
	 * millisol.
	 */
	private static final double LOAD_RATE = 20D;

	// Resources that can be loaded in the background
	private static final int[] BACKGROUND_RESOURCES = {
			ResourceUtil.OXYGEN_ID,
			ResourceUtil.WATER_ID,
			ResourceUtil.METHANOL_ID
	};

	// Avoid transferring micro-small amount
	private static final double SMALLEST_RESOURCE_LOAD = 0.01;

	// Have to limit the precision of the amount loading to avoid
	// problem with the double precision
	private static final double AMOUNT_BASE = 1000000D;

	// The number of times to attempt to get mandatory resources from a settlement
	public static final int MAX_SETTLEMENT_ATTEMPTS = 5;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(LoadingController.class.getName());

	private boolean vehicleFull = false;
	// The number of times a settlement can not have the resources
	private int retryAttempts = MAX_SETTLEMENT_ATTEMPTS;

	private final Map<Integer, Double> amountManifest;
	private final Map<Integer, Double> optionalAmountManifest;
	private final Map<Integer, Integer> equipmentManifest;
	private final Map<Integer, Integer> optionalEquipmentManifest;
	private final Map<Integer, Integer> itemManifest;
	private final Map<Integer, Integer> optionalItemManifest;
	
	private final EquipmentOwner stores;
	private final EquipmentOwner vehicleStores;
	private final Settlement settlement;
	private final Vehicle vehicle;

	private int loadedItems;
	private final int totalItems;

	private int failedResourceId = -1;


	/**
	 * Loads a vehicle with a manifest from a Settlement.
	 * 
	 * @param settlement Source of resources for the load
	 * @param vehicle Vehicle to load
	 * @param manifest Defines what supplies are to be loaded
	 */
	public LoadingController(Settlement settlement, Vehicle vehicle,
							 SuppliesManifest manifest) {
		this.settlement = settlement;
		this.stores = settlement.getEquipmentInventory();
		this.vehicle = vehicle;
		this.vehicleStores = vehicle.getEquipmentInventory();

		// Take copies to form the manifest as the quantities will be reduced
		this.amountManifest = new HashMap<>(manifest.getAmounts(true));
		this.optionalAmountManifest = new HashMap<>(manifest.getAmounts(false));
		this.equipmentManifest = new HashMap<>(manifest.getEquipment(true));
		this.optionalEquipmentManifest = new HashMap<>(manifest.getEquipment(false));
		this.itemManifest = new HashMap<>(manifest.getItems(true));
		this.optionalItemManifest = new HashMap<>(manifest.getItems(false));

		// Reduce what is already in the Vehicle
		removeVehicleAmounts(amountManifest);
		removeVehicleAmounts(optionalAmountManifest);
		removeVehicleEquipment(equipmentManifest);
		removeVehicleEquipment(optionalEquipmentManifest);
		removeVehicleItems(itemManifest);
		removeVehicleItems(optionalItemManifest);

		// Only count the total after removing what is already in the vehicle
		totalItems = amountManifest.size() + optionalAmountManifest.size() +
					equipmentManifest.size() + optionalEquipmentManifest.size() +
					itemManifest.size() + optionalItemManifest.size();
	}

	private List<Equipment> getEquipmentSet(EquipmentOwner source, EquipmentType eType) {
		return source.getEquipmentSet().stream()
				.filter(e -> e.getEquipmentType() == eType)
				.toList();
	}

	/**
	 * Remove any equipment the Vehicle has from the manifest. Equipment completely loaded
	 * in the vehicle will be removed from the manifest.
	 * 
	 * @param equipment
	 */
	private void removeVehicleEquipment(Map<Integer, Integer> equipment) {
		Set<Integer> ids = new HashSet<>(equipment.keySet());
		for (Integer eqmId : ids) {
			EquipmentType eType = EquipmentType.convertID2Type(eqmId);
			int amountLoaded = getEquipmentSet(vehicleStores, eType).size();
			if (amountLoaded > 0) {
				int newAmount = equipment.get(eqmId).intValue() - amountLoaded;
				if (newAmount <= 0D) {
					equipment.remove(eqmId);
				}
				else {
					equipment.put(eqmId, newAmount);
				}
			}
		}
	}

	/**
	 * Remove any amount resources the Vehicle has from the manifest. Resource completely loaded
	 * in the vehicle will be removed from the manifest.
	 * 
	 * @param resources
	 */
	private void removeVehicleAmounts(Map<Integer, Double> resources) {
		Set<Integer> ids = new HashSet<>(resources.keySet());
		for (Integer resourceId : ids) {
			double amountLoaded = vehicleStores.getAllAmountResourceStored(resourceId);
			double capacity = vehicleStores.getSpecificCapacity(resourceId);
			double amountRequired = resources.get(resourceId).doubleValue();
			if (capacity < amountRequired) {
				// So the vehicle can not handle the Manifest volume
				// Adjust the manifest down
				resources.put(resourceId, (capacity - amountLoaded));
				logger.warning(vehicle, "Could not hold "
						+ amountRequired + " kg "
						+ ResourceUtil.findAmountResourceName(resourceId)
						+ " from the manifest "
						+ "(Capacity: " + capacity + " kg).");
				amountLoaded = 0;
			}

			if (amountLoaded > 0) {
				double newAmount = resources.get(resourceId).doubleValue() - amountLoaded;
				if (newAmount <= 0D) {
					resources.remove(resourceId);
				}
				else {
					resources.put(resourceId, newAmount);
				}
			}
		}
	}

	/*
	 * Remove any Items the Vehicle has from the manifest. Items completely loaded
	 * in the vehicle will be removed from the manifest.
	 */
	private void removeVehicleItems(Map<Integer, Integer> resources) {
		Set<Integer> ids = new HashSet<>(resources.keySet());
		for (Integer resourceId : ids) {
			int amountLoaded = vehicleStores.getItemResourceStored(resourceId);
			if (amountLoaded > 0) {
				int newAmount = resources.get(resourceId).intValue() - amountLoaded;
				if (newAmount <= 0D) {
					resources.remove(resourceId);
				}
				else {
					resources.put(resourceId, newAmount);
				}
			}
		}
	}

	/**
	 * Loads resources by a worker.
	 * 
	 * @param worker
	 * @param time How much time does the Worker have
	 * @return Load completed
	 */
	public boolean load(Worker worker, double time) {
		int strength = worker.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRENGTH);
		double strengthModifier = .1D + (strength * .018D);
		double amountLoading = Math.round((LOAD_RATE * strengthModifier * time / 12D) * AMOUNT_BASE) / AMOUNT_BASE;

		// Temporarily remove rover from settlement so that inventory doesn't get mixed
		// in.
		boolean vehicleInSettlement = false;
		if (settlement.containsParkedNGaragedVehicle(vehicle)) {
			vehicleInSettlement = true;
			settlement.removeParkedNGaragedVehicle(vehicle);
		}

		// Load mandatory first
		if ((amountLoading > 0D) && !equipmentManifest.isEmpty()) {
			amountLoading = loadEquipment(amountLoading, equipmentManifest, true);
		}
		if ((amountLoading > 0D) && !amountManifest.isEmpty()) {
			amountLoading = loadAmounts(worker, amountLoading, amountManifest, true);
		}
		if ((amountLoading > 0D) && !itemManifest.isEmpty()) {
			amountLoading = loadItems(worker, amountLoading, itemManifest, true);
		}

		// Load optionals last
		if ((amountLoading > 0D) && !optionalAmountManifest.isEmpty()) {
			amountLoading = loadAmounts(worker, amountLoading, optionalAmountManifest, false);
		}
		if ((amountLoading > 0D) && !optionalItemManifest.isEmpty()) {
			amountLoading = loadItems(worker, amountLoading, optionalItemManifest, false);
		}
		if ((amountLoading > 0D) && !optionalEquipmentManifest.isEmpty()) {
			amountLoading = loadEquipment(amountLoading, optionalEquipmentManifest, false);
		}

		// Put rover back into settlement.
		if (vehicleInSettlement) {
			settlement.addParkedNGaragedVehicle(vehicle);
		}

		// Should the load stop for this worker? Either fully loaded or did not
		// use load amount (that means load couldn't complete it
		boolean completed = isCompleted();
		
		// If not completed then check that the vehicle still has a capacity
		if (!completed) {
			vehicleFull = vehicleStores.getRemainingCargoCapacity() < 10D;
			if (vehicleFull) {
				completed = true;
				logger.warning(vehicle, "Vehicle full so loading completed");
			}
		}

		if (completed) {
			// Can remove assume fuel is reloaded
			vehicle.removeSecondaryStatus(StatusType.OUT_OF_FUEL);
			vehicle.setLoading(null);
			logger.fine(vehicle, "Loading completed by " + worker.getName());
		}
		return (amountLoading > 0D) || completed;
	}

	
	/**
	 * Loads the vehicle with required items from the settlement.
	 *
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @param manifest Manifest to load from
	 * @return the remaining amount (kg) the person can load in this time period.
	 * @throws Exception if problem loading resources.
	 */
	private double loadItems(Worker loader, double amountLoading, Map<Integer, Integer> manifest, boolean mandatory) {

		String  loaderName = loader.getName();

		// Load required resources. Take a cope as load will change the
		// manifest
		Set<Integer> resources = new HashSet<>(manifest.keySet());
		for(Integer resource : resources) {
			// Load amount resources
			amountLoading = loadItemResource(loaderName, amountLoading, resource, manifest, mandatory);

			// Exhausted the loading amount
			if (amountLoading <= 0D) {
				return 0;
			}
		}
		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
	}

	/**
	 * Loads the vehicle with required amount resources from the settlement.
	 *
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @param manifest Manifest to load from
	 * @return the remaining amount (kg) the person can load in this time period.
	 * @throws Exception if problem loading resources.
	 */
	private double loadAmounts(Worker loader, double amountLoading, Map<Integer, Double> manifest, boolean mandatory) {

		String  loaderName = loader.getName();

		// Load required resources. Take a cope as load will change the
		// manifest
		Set<Integer> resources = new HashSet<>(manifest.keySet());
		for(Integer resource : resources) {
			// Load amount resources
			amountLoading = loadAmountResource(loaderName, amountLoading, resource, manifest, mandatory);

			// Exhausted the loading amount
			if (amountLoading <= 0D) {
				return 0;
			}
		}
		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
	}

	private void logFailedResource(int resourceId, double amountToLoad) {
		this.retryAttempts--;

		// Note: Added timeBetweenLogs to avoid repetitive warnings
		logger.warning(vehicle, 10_000, "Not enough available for loading "
							+ Math.round(amountToLoad * 100D) / 100D
							+ " of " + ResourceType.getName(resourceId)
							+ " will try " + retryAttempts + " more times."
							);
		if (retryAttempts == 0) {
			this.failedResourceId = resourceId;
		}
	}

	/**
	 * Loads the vehicle with an amount resource from the settlement.
	 *
	 * @param loader		Entity doing the loading
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @param resource      the amount resource to be loaded.
	 * @param manifest		The Resources being loaded
	 * @param mandatory     true if the amount resource is required to load, false
	 *                      if optional.
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadAmountResource(String loader, double amountLoading, Integer resource,
									  Map<Integer, Double> manifest, boolean mandatory) {

		String resourceName = ResourceUtil.findAmountResourceName(resource);

		// Determine amount to load.
		boolean usedSupply = false;
		double amountNeeded = manifest.get(resource).doubleValue();
		double amountToLoad = Math.min(amountNeeded, amountLoading);

		if (amountToLoad > 0) {
			// Check if enough resource in settlement inventory.
			double settlementStored = stores.getAllAmountResourceStored(resource);

			// Does the settlement have enough stored resource ?
			if (settlementStored < amountToLoad) {
				if (mandatory) {
					logFailedResource(resource, amountToLoad);
					return amountLoading;
				}
				else {
					usedSupply = true;
					amountToLoad = settlementStored;
				}
			}

			// Check remaining capacity in vehicle inventory.
			double remainingCapacity = vehicleStores.getRemainingSpecificCapacity(resource);
			if (remainingCapacity < amountToLoad) {
				if (remainingCapacity < SMALLEST_RESOURCE_LOAD) {
					// Nothing left for this type so stop loading this resource
					amountNeeded = 0;
					logger.warning(vehicle, "Capacity exhausted in loading "
									+ resourceName);
				}
				else if (mandatory && ((amountToLoad - remainingCapacity) > SMALLEST_RESOURCE_LOAD)) {
					// Will load up as much required resource as possible
					logger.warning(vehicle, 10_000, "Not enough capacity for loading "
							+ Math.round(amountToLoad * 100D) / 100D + " kg "
							+ resourceName
							+ ". Vehicle remaining cap: "
							+ Math.round(remainingCapacity * 100D) / 100D + " kg.");
				}
				usedSupply = true;
				amountToLoad = remainingCapacity;
			}

			// Load resource from settlement inventory to vehicle inventory.
			if (amountToLoad > 0) {
				// Take resource from the settlement
				stores.retrieveAmountResource(resource, amountToLoad);
				// Store resource in the vehicle
				vehicleStores.storeAmountResource(resource, amountToLoad);
			}
		}

		// Check if this resource is complete
		amountNeeded -= amountToLoad;
		if (amountNeeded <= SMALLEST_RESOURCE_LOAD) {
			manifest.remove(resource);
			loadedItems++;
		}
		else if (!mandatory && usedSupply) {
			logger.info(vehicle, 10_000, loader + " could not load " + resourceName
						+ ", returning " + Math.round(amountNeeded * 100D) / 100D + " kg.");
			manifest.remove(resource);
			loadedItems++;
		}
		else {
			manifest.put(resource, amountNeeded);
		}

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading - amountToLoad;
	}


	/**
	 * Loads the vehicle with an item resource from the settlement.
	 *
	 * @param loader        Entity doing the loading
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @param manifest
	 * @param resource      the item resource to be loaded.
	 * @param mandatory      true if the item resource is required to load, false if
	 *                      optional.
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadItemResource(String loader, double amountLoading, Integer id, Map<Integer, Integer> manifest, boolean mandatory) {

		Part p = ItemResourceUtil.findItemResource(id);
		boolean usedSupply = false;
		boolean noCapacity = false;
		
		// Determine number to load. Could load at least one
		// Part if needed
		int amountNeeded = (int)manifest.get(id).doubleValue();
		int amountCouldLoad = Math.max(1, (int)(amountLoading/p.getMassPerItem()));
		int amountToLoad = Math.min(amountNeeded, amountCouldLoad);
		if (amountToLoad > 0) {
			// Check if enough resource in settlement inventory.
			int settlementStored = stores.getItemResourceStored(id);

			// Settlement has enough stored resource?
			if (settlementStored < amountToLoad) {
				if (mandatory) {
					logFailedResource(id, amountToLoad);
				}
				amountToLoad = settlementStored;
				usedSupply = true;
			}

			// Check remaining capacity in vehicle inventory.
			double remainingMassCapacity = vehicleStores.getRemainingCargoCapacity();
			if (remainingMassCapacity < 0D) {
				remainingMassCapacity = 0D;
			}
			double loadingMass = amountToLoad * p.getMassPerItem();
			if (remainingMassCapacity < loadingMass) {
				if (mandatory) {
					// Will load up as much required resource as possible
					logger.warning(vehicle, "Not enough capacity for loading "
							+ Math.round(loadingMass * 100D) / 100D + " kg "
							+ p.getName()
							+ ". Vehicle remaining cap: "
							+ Math.round(remainingMassCapacity * 100D) / 100D + " kg.");
				}
				amountToLoad = (int) Math.floor(remainingMassCapacity/p.getMassPerItem());
				noCapacity = (amountToLoad == 0);
			}

			// Load item from settlement inventory to vehicle inventory.
			try {
				// Take resource from the settlement
				stores.retrieveItemResource(id, amountToLoad);
				// Store resource in the vehicle
				vehicleStores.storeItemResource(id, amountToLoad);
			} catch (Exception e) {
				logger.severe(vehicle, "Cannot transfer Item from settlement to vehicle: ", e);
				return amountLoading;
			}
		}

		// Check if this resource is complete
		amountNeeded -= amountToLoad;
		if (amountNeeded == 0) {
			logger.fine(vehicle, loader + " completed loading item " + p.getName());
			manifest.remove(id);
			loadedItems++;
		}
		// If it's optional and attempted to load something then remove it.
		else if ((!mandatory && usedSupply) || noCapacity) {
			logger.warning(vehicle, loader + " item " + p.getName()
						+ ", " + amountNeeded + " not loaded ");
			manifest.remove(id);
			loadedItems++;
		}
		else {
			manifest.put(id, amountNeeded);
		}

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading - (amountToLoad * p.getMassPerItem());
	}

	/**
	 * Loads the vehicle with equipment out of a manifest from the settlement.
	 *
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @param mandatory Are these items needed
	 * @param manifest
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadEquipment(double amountLoading, Map<Integer, Integer> manifest, boolean mandatory) {

		Set<Integer> eqmIds = new HashSet<>(manifest.keySet());
		for (Integer equipmentType : eqmIds) {
			int amountNeeded = manifest.get(equipmentType);
			if (amountNeeded > 0) {
				// How many available ?
				EquipmentType eType = EquipmentType.convertID2Type(equipmentType);

				// Must take a copy
				List<Equipment> list = new ArrayList<>(getEquipmentSet(stores, eType));
				for (Equipment eq : list) {
					if (eq.isEmpty(true)) {
						// Put this equipment into a vehicle
						boolean done = eq.transfer(vehicle);

						if (!done) {
                			logger.warning(vehicle, "Cannot store Equipment " + eq.getName());
						}
						else {
							amountLoading -= eq.getMass();
							amountNeeded--;

							// Check can still keep going
							if ((amountNeeded == 0) || (amountLoading <= 0D)) {
								break;
							}
						}
					}
				}
			}

			// If everything needed load 
			if (amountNeeded == 0) {
				logger.fine(vehicle, "Completed loading equipment " + equipmentType);
				manifest.remove(equipmentType);
				loadedItems++;
			}
			else if (amountLoading <= 0) {
				// Just run out of loading capacity so update and try again later
				manifest.put(equipmentType, amountNeeded);
			}
			else {
				// Done with this equipment type
				if (mandatory) {
					// Mandatory just log it
					logFailedResource(equipmentType, amountNeeded);
				}
				else {
					// Move on
					logger.fine(vehicle, "Skipping loading optionalequipment " + equipmentType);
					manifest.remove(equipmentType);
					loadedItems++;
				}
			}

			// No more load effort left
			if (amountLoading <= 0D) {
				return 0D;
			}
		}

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
	}

	/**
	 * This is called in the background from the Drone/Rover time pulse method
	 * to load resources. But why is it different to the loadResources above,
	 * it uses a different definition of resources needed.
	 * 
	 * @param amountLoading
	 */
	public void backgroundLoad(double amountLoading) {

		for (Integer id: BACKGROUND_RESOURCES) {
			if (amountManifest.containsKey(id)) {
				// Load this resource
				loadAmountResource("Background", amountLoading, id, amountManifest, true);
			}
		}
	}

	/**
	 * Is the loading plan completed for the mandatory details ?
	 * 
	 * @return
	 */
	public boolean isCompleted() {
		// Manifest is empty so complete
		return (amountManifest.isEmpty() && optionalAmountManifest.isEmpty()
				&& equipmentManifest.isEmpty() && optionalEquipmentManifest.isEmpty()
				&& itemManifest.isEmpty() && optionalItemManifest.isEmpty())
				|| vehicleFull;
	}

	/**
	 * Get the resource id of the first failed mandatory resource
	 * 
	 * @return Return -1 if no failure has occurred
	 */
	public int getFailedResource() {
		return failedResourceId;
	}

	public Map<Integer, Double> getAmountManifest(boolean mandatory) {
		return Collections.unmodifiableMap(mandatory ? this.amountManifest : this.optionalAmountManifest);
	}

	public Map<Integer, Integer> getEquipmentManifest(boolean mandatory) {
		return Collections.unmodifiableMap(mandatory ? this.equipmentManifest : this.optionalEquipmentManifest);
	}

	public Map<Integer, Integer> getItemManifest(boolean mandatory) {
		return Collections.unmodifiableMap(mandatory ? this.itemManifest : this.optionalItemManifest);
	}

	/**
	 * Returns the settlement providing the resources for the load.
	 * 
	 * @return
	 */
	public Settlement getSettlement() {
		return settlement;
	}

	/**
	 * Gets the vehicle that is the target of this loading plan.
	 * 
	 * @return
	 */
    public Vehicle getVehicle() {
		return vehicle;
    }

	/**
	 * Get the percent progress of the loading. The percentage is based on the number of items loaded.
	 * @return Number between 0 and 100
	 */
    public int getProgress() {
        return (int) (100 * loadedItems) / totalItems;
    }
}

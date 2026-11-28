/*
 * Mars Simulation Project
 * LoadingController.java
 * @date 2025-07-21
 * @author Barry Evans
 */
package com.mars_sim.core.vehicle.task;

import java.io.Serializable;
import java.util.ArrayList;
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
		
	// Avoid transferring micro-small amount
	protected static final double SMALLEST_RESOURCE_LOAD = 0.01;

	public static final String LOAD_EVENT = "Load Controller";

	/**
	 * This represents a single item that has to be loaded onto the vehicle.
	 */
	public static final class LoadItem implements Serializable {
		private final int resourceID;
		private final double required;
		private final boolean mandatory;
		private boolean failed = false;
		private double loaded = 0D;
		
		private LoadItem(int resourceID, double required, boolean mandatory) {
			this.resourceID = resourceID;
			this.required = required;
			this.mandatory = mandatory;
		}	

		/**
		 * An amount has been loaded.
		 * @param amount The amount that has been loaded
		 * @return True if the required amount has been fully loaded, false otherwise
		 */
		private boolean addLoaded(double amount) {
			loaded += amount;
			return (required - loaded) <= SMALLEST_RESOURCE_LOAD;
		}

		/**
		 * Gets the resource ID of this load item.
		 */
		public int getResourceID() {
			return resourceID;
		}

		/**
		 * Get the total required amount to load
		 */
		public double getRequired() {
			return required;
		}

		/**
		 * Gets the amount loaded so far for this item.
		 */
		public double getLoaded() {
			return loaded;
		}
		
		/**
		 * Gets the remaining amount to be loaded for this item.
		 */
		public double getRemaining() {
			return Math.max(0, required - loaded);
		}

		private void setFailed(boolean b) {
			failed = b;
		}

		/**
		 * Checks if this load item has failed to load.
		 */
		public boolean isFailed() {
			return failed;
		}

		/**
		 * Checks if this load item is mandatory.
		 */
		public boolean isMandatory() {
			return mandatory;
		}
	}

	// Resources that can be loaded in the background
	private static final Set<Integer> BACKGROUND_RESOURCES = Set.of(
			ResourceUtil.OXYGEN_ID,
			ResourceUtil.WATER_ID,
			ResourceUtil.METHANOL_ID
	);

	// Have to limit the precision of the amount loading to avoid
	// problem with the double precision
	private static final double AMOUNT_BASE = 1000000D;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(LoadingController.class.getName());

	private boolean vehicleFull = false;

	private final List<LoadItem> toLoad = new ArrayList<>();
	private final List<LoadItem> loaded = new ArrayList<>();
	
	private final EquipmentOwner stores;
	private final EquipmentOwner vehicleStores;
	private final Settlement settlement;
	private final Vehicle vehicle;
	private boolean failed = false;

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

		// Reduce what is already in the Vehicle
		toLoad.addAll(removeVehicleAmounts(manifest.getAmounts(true), true));
		toLoad.addAll(removeVehicleAmounts(manifest.getAmounts(false), false));
		toLoad.addAll(removeVehicleEquipment(manifest.getEquipment(true), true));
		toLoad.addAll(removeVehicleEquipment(manifest.getEquipment(false), false));
		toLoad.addAll(removeVehicleItems(manifest.getItems(true), true));
		toLoad.addAll(removeVehicleItems(manifest.getItems(false), false));
	}

	private List<Equipment> getEquipmentSet(EquipmentOwner source, EquipmentType eType) {
		return source.getEquipmentSet().stream()
				.filter(e -> e.getEquipmentType() == eType)
				.toList();
	}

	/**
	 * Remove any equipment the Vehicle has from the manifest. Equipment completely loaded
	 * in the vehicle will be removed from the manifest.
	 * Return a list of LoadItem objects representing the equipment still needed.
	 * @param equipment Map of equipment IDs to quantities
	 * @param required Whether the equipment is required for the load
	 */
	private List<LoadItem> removeVehicleEquipment(Map<Integer, Integer> equipment, boolean required) {
		List<LoadItem> newToLoad = new ArrayList<>();
		for (var eqm : equipment.entrySet()) {
			EquipmentType eType = EquipmentType.convertID2Type(eqm.getKey());
			int amountLoaded = getEquipmentSet(vehicleStores, eType).size();
			int quantity = eqm.getValue().intValue() - amountLoaded;
			if (quantity > 0) {
				newToLoad.add(new LoadItem(eqm.getKey(), quantity, required));
			}
		}

		return newToLoad;
	}

	/**
	 * Remove any amount resources the Vehicle has from the manifest. Resource completely loaded
	 * in the vehicle will be removed from the manifest.
	 * 
	 * @param resources Map of resource IDs to quantities
	 * @param required Whether the resource is required for the load
	 */
	private List<LoadItem> removeVehicleAmounts(Map<Integer, Double> resources, boolean required) {
		List<LoadItem> newToLoad = new ArrayList<>();
		for (var resourceEntry : resources.entrySet()) {
			Integer resourceId = resourceEntry.getKey();
			double amountLoaded = vehicleStores.getAllAmountResourceStored(resourceId);
			var quantity = resources.get(resourceId).doubleValue() - amountLoaded;
			if (quantity > 0) {
				newToLoad.add(new LoadItem(resourceId, quantity, required));
			}
		}

		return newToLoad;
	}

	/*
	 * Remove any Items the Vehicle has from the manifest. Items completely loaded
	 * in the vehicle will be removed from the manifest.
	 */
	private List<LoadItem> removeVehicleItems(Map<Integer, Integer> items, boolean required) {
		List<LoadItem> newToLoad = new ArrayList<>();
		for (var item : items.entrySet()) {
			int amountLoaded = vehicleStores.getItemResourceStored(item.getKey());
			int quantity = item.getValue().intValue() - amountLoaded;
			if (quantity > 0) {
				newToLoad.add(new LoadItem(item.getKey(), quantity, required));
			}
		}

		return newToLoad;
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

		// Work through the loading process
		LoadItem lastQuantity = null;
		while(!toLoad.isEmpty()) {
			var nextQuantity = toLoad.get(0);

			// Check it's Ok to carry on. Must have an amount to load and
			// not repeating the same item
			if (amountLoading <= 0D || nextQuantity.equals(lastQuantity)) {
				break;
			}
			amountLoading = loadQuantity(worker, amountLoading, nextQuantity);
		}

		// Put rover back into settlement.
		if (vehicleInSettlement) {
			settlement.addParkedNGaragedVehicle(vehicle);
		}

		// Should the load stop for this worker? Either fully loaded or did not
		// use load amount (that means load couldn't complete it
		boolean completed = toLoad.isEmpty();
		
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
	 * Load a quantity of the specified item into the vehicle.
	 * @param loader the worker performing the loading.
	 * @param amountLoading the amount (kg) the worker can load in this time period.
	 * @param nextQuantity the next quantity of the item to load.
	 * @return the remaining amount (kg) the worker can load in this time period.
	 */
	private double loadQuantity(Worker loader, double amountLoading, LoadItem nextQuantity) {

		// Determine the type of the next quantity and call the appropriate loading method
		switch (ResourceType.getType(nextQuantity.getResourceID())) {
			case ResourceType.ITEM_RESOURCE:
				return loadItemResource(amountLoading, nextQuantity);
			case ResourceType.AMOUNT_RESOURCE:
				return loadAmountResource(amountLoading, nextQuantity);
			case ResourceType.EQUIPMENT_RESOURCE:
				return loadEquipment(amountLoading, nextQuantity);
			default:
				throw new IllegalArgumentException("Unsupported resource type: " + ResourceType.getType(nextQuantity.getResourceID()));
		}
	}

	/**
	 * Record a load operation being performed for a specific quantity.
	 * @param amountLoaded the amount (kg) attempted to load.
	 * @param loadFailed whether the load operation failed.
	 * @param quantity the load item being processed.
	 */
	private void recordLoad(double amountLoaded, boolean loadFailed, LoadItem quantity) {
		boolean loadComplete = loadFailed;
		if (quantity.addLoaded(amountLoaded)) {
			loadComplete = true;
			logger.fine(vehicle, "Completed full loading " + ResourceType.getName(quantity.getResourceID()));
		}
		else if (loadFailed) {
			var resourceName = ResourceType.getName(quantity.getResourceID());
			if (quantity.isMandatory()) {
				logger.severe(vehicle, "Failed to fully load mandatory " + resourceName);
				failed = true;
			}
			else {
				logger.warning(vehicle, "Failed to fully load optional " + resourceName);
			}
			quantity.setFailed(true);
		}

		if (loadComplete && toLoad.remove(quantity)) {
			loaded.add(quantity);

			vehicle.fireUnitUpdate(LOAD_EVENT, quantity);
		}
	}

	/**
	 * Loads the vehicle with an amount resource from the settlement.
	 *
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @param quantity the load item containing the resource and quantity information.
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadAmountResource(double amountLoading, LoadItem quantity) {

		int id = quantity.getResourceID();

		// Determine amount to load.
		boolean loadFailed = false;
		double amountNeeded = quantity.getRemaining();
		double amountToLoad = Math.min(amountNeeded, amountLoading);

		if (amountToLoad > 0) {
			// Check if enough resource in settlement inventory.
			double settlementStored = stores.getAllAmountResourceStored(id);

			// Does the settlement have enough stored resource ?
			if (settlementStored < amountToLoad) {
				loadFailed = true;
				amountToLoad = settlementStored;
			}

			// Check remaining capacity in vehicle inventory.
			double remainingCapacity = vehicleStores.getRemainingSpecificCapacity(id);
			if (remainingCapacity < amountToLoad) {
				loadFailed = true;
				amountToLoad = remainingCapacity;
			}

			// Load resource from settlement inventory to vehicle inventory.
			if (amountToLoad > 0) {
				// Take resource from the settlement
				stores.retrieveAmountResource(id, amountToLoad);
				// Store resource in the vehicle
				vehicleStores.storeAmountResource(id, amountToLoad);
			}
		}

		// Check if this resource is complete
		recordLoad(amountToLoad, loadFailed, quantity);

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading - amountToLoad;
	}


	/**
	 * Loads the vehicle with an item resource from the settlement.
	 *
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @param mandatory      true if the item resource is required to load, false if
	 *                      optional.
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadItemResource(double amountLoading, LoadItem quantity) {

		int id = quantity.getResourceID();
		Part p = ItemResourceUtil.findItemResource(id);
		boolean loadFailed = false;
		
		// Determine number to load. Could load at least one
		// Part if needed
		int amountNeeded = (int)quantity.getRemaining();
		int amountCouldLoad = Math.max(1, (int)(amountLoading/p.getMassPerItem()));
		int amountToLoad = Math.min(amountNeeded, amountCouldLoad);
		if (amountToLoad > 0) {
			// Check if enough resource in settlement inventory.
			int settlementStored = stores.getItemResourceStored(id);

			// Settlement has enough stored resource?
			if (settlementStored < amountToLoad) {
				amountToLoad = settlementStored;
				loadFailed = true;
			}

			// Check remaining capacity in vehicle inventory.
			double remainingMassCapacity = vehicleStores.getRemainingCargoCapacity();
			if (remainingMassCapacity < 0D) {
				remainingMassCapacity = 0D;
			}
			double loadingMass = amountToLoad * p.getMassPerItem();
			if (remainingMassCapacity < loadingMass) {
				amountToLoad = (int) Math.floor(remainingMassCapacity/p.getMassPerItem());
				loadFailed = true;
			}

			// Take resource from the settlement
			stores.retrieveItemResource(id, amountToLoad);
			// Store resource in the vehicle
			vehicleStores.storeItemResource(id, amountToLoad);
		}

		// Check if this resource is complete
		recordLoad(amountToLoad, loadFailed, quantity);

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading - (amountToLoad * p.getMassPerItem());
	}

	/**
	 * Loads the vehicle with equipment out of a manifest from the settlement.
	 *
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @param quantity the load item being processed.
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadEquipment(double amountLoading, LoadItem quantity) {

		boolean loadFailed = false;
		int eqmLoaded = 0;
		int eqmNeeded = (int)quantity.getRemaining();
		if (eqmNeeded > 0) {
			// How many available ?
			EquipmentType eType = EquipmentType.convertID2Type(quantity.getResourceID());

			// Take only empty equipment from the settlement
			List<Equipment> list = getEquipmentSet(stores, eType).stream()
								.filter(eq -> eq.isEmpty(true))
								.toList();

			if (list.size() < eqmNeeded) {
				loadFailed = true;
			}
			else {
				for (Equipment eq : list) {
					// Put this equipment into a vehicle
					boolean done = eq.transfer(vehicle);

					if (!done) {
						logger.warning(vehicle, "Cannot store Equipment " + eq.getName());
						loadFailed = true;
						break;
					}
					else {
						amountLoading -= eq.getMass();
						eqmLoaded++;

						// Check can still keep going
						if ((eqmLoaded == eqmNeeded) || (amountLoading <= 0D)) {
							break;
						}
					}
				}
			}
		}

		recordLoad(eqmLoaded, loadFailed, quantity);

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

		var backgroundQuantities = toLoad.stream()
										  .filter(item -> BACKGROUND_RESOURCES.contains(item.getResourceID()))
										  .toList();
		for (var item : backgroundQuantities) {
			// Load this resource
			if (loadAmountResource(amountLoading, item) <= 0D) {
				return;
			}
		}
	}

	/**
	 * Is the loading plan completed successfully for the mandatory details ?
	 */
	public boolean isCompleted() {
		// Manifest is empty so complete
		return !failed && (toLoad.isEmpty() || vehicleFull);
	}

	/**
	 * Gets the list of items that are still to be loaded into the vehicle.
	 */
	public List<LoadItem> getToLoad() {
		return toLoad;
	}

	/**
	 * Gets the list of items that have been successfully loaded into the vehicle.
	 */
	public List<LoadItem> getLoaded() {
		return loaded;
	}

	/**
	 * Has the load failed on the mandatory items?
	 * 
	 * @return
	 */
	public boolean isFailure() {
		return failed;
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
}

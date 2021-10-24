/*
 * Mars Simulation Project
 * LoadingController.java
 * @date 2021-10-21
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

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
			ResourceUtil.oxygenID,
			ResourceUtil.waterID,
			ResourceUtil.methaneID
	};

	// Avoid transferring micro-small amount
	private static final double SMALLEST_RESOURCE_LOAD = 0.001D;

	// Have to limit the precision of the amount loading to avoid
	// problem with the double precision
	private static final double AMOUNT_BASE = 1000000D;
	
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(LoadingController.class.getName());
	
	private Map<Integer, Number> resourcesManifest;
	private Map<Integer, Number> optionalResourcesManifest;
	private Map<Integer, Integer> equipmentManifest;
	private Map<Integer, Integer> optionalEquipmentManifest;
	private Settlement settlement;
	private Vehicle vehicle;	

	/**
	 * Load a vehicle with a manifest from a Settlement
	 * @param settlement Source of resources for the load
	 * @param vehicle Vehicle to load
	 * @param resources Mandatory resources needed
	 * @param optionalResources Optional resources needed
	 * @param equipment Mandatory equipment needed
	 * @param optionalEquipment Optional equipment needed
	 */
	public LoadingController(Settlement settlement, Vehicle vehicle,
							 Map<Integer, Number> resources,
							 Map<Integer, Number> optionalResources,
							 Map<Integer, Integer> equipment,
							 Map<Integer, Integer> optionalEquipment) {
		this.settlement = settlement;
		this.vehicle = vehicle;
		
		// Take copies to form the manifest as the quantities will be reduced
		this.resourcesManifest = new HashMap<>(resources);
		this.optionalResourcesManifest = new HashMap<>(optionalResources);
		this.equipmentManifest = new HashMap<>(equipment);
		this.optionalEquipmentManifest = new HashMap<>(optionalEquipment);
		
		// Reduce what is already in the Vehicle
		removeVehicleResources(resourcesManifest);
		removeVehicleResources(optionalResourcesManifest);
		removeVehicleEquipment(equipmentManifest);
		removeVehicleEquipment(optionalEquipmentManifest);
	}

	/*
	 * Remove any equipment the Vehicle has from the manifest. Equipment completely loaded
	 * in the vehicle will be removed from the manifest.
	 */
	private void removeVehicleEquipment(Map<Integer, Integer> equipment) {
		Set<Integer> ids = new HashSet<>(equipment.keySet());
		for (Integer eqmId : ids) {
			EquipmentType eType = EquipmentType.convertID2Type(eqmId);
			int amountLoaded = vehicle.findNumEmptyContainersOfType(eType, false);			
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

	/*
	 * Remove any resources the Vehicle has from the manifest. Resource completely loaded
	 * in the vehicle will be removed from the manifest.
	 */
	private void removeVehicleResources(Map<Integer, Number> resources) {
		Set<Integer> ids = new HashSet<>(resources.keySet());
		for (Integer resourceId : ids) {
			double amountLoaded;
			if (resourceId < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
				// Load amount resources
				amountLoaded = vehicle.getAmountResourceStored(resourceId);
				double capacity = vehicle.getAmountResourceCapacity(resourceId);
				double amountRequired = resources.get(resourceId).doubleValue();
				if (capacity < amountRequired) {
					// So the vehicle can not handle the Manifest volume
					// Adjust the manifest down
					resources.put(resourceId, (capacity - amountLoaded));
					logger.warning(vehicle, "Can not hold the "
									+ ResourceUtil.findAmountResourceName(resourceId)
									+ " in the manifest (" + amountRequired
									+ ") as capacity is " + capacity);
					amountLoaded = 0;
				}
			}
			else {
				// Load item resources
				amountLoaded = vehicle.getItemResourceStored(resourceId);
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

	/**
	 * Load resources by a worker
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
//		Inventory sInv = settlement.getInventory();
		boolean vehicleInSettlement = false;
		if (settlement.containsParkedVehicle(vehicle)) {
			vehicleInSettlement = true;
			settlement.removeParkedVehicle(vehicle);
		}
				
		// Load equipment
		if ((amountLoading > 0D) && !equipmentManifest.isEmpty()) {
			amountLoading = loadEquipment(amountLoading, equipmentManifest, true);
		}
		
		// Load resources
		if ((amountLoading > 0D) && !resourcesManifest.isEmpty()) {
			amountLoading = loadResources(worker, amountLoading, resourcesManifest, true);
		}

		// Load optionals last
		if ((amountLoading > 0D) && !optionalResourcesManifest.isEmpty()) {
			amountLoading = loadResources(worker, amountLoading, optionalResourcesManifest, false);
		}
		
		// Load optional equipment
		if ((amountLoading > 0D) && !optionalEquipmentManifest.isEmpty()) {
			amountLoading = loadEquipment(amountLoading, optionalEquipmentManifest, false);
		}
		
		// Put rover back into settlement.
		if (vehicleInSettlement) {
			settlement.addParkedVehicle(vehicle);
		}

		// Should the load stop for this worker? Either fully loaded or did not
		// use load amount (that means load couldn't complete it
		boolean completed = isCompleted();
		if (completed) {
			logger.fine(vehicle, "Loading completed by " + worker.getName());
		}
		return (amountLoading > 0D) || completed;
	}

	/**
	 * Loads the vehicle with required resources from the settlement.
	 * 
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @param manifest Manfiest to load from
	 * @return the remaining amount (kg) the person can load in this time period.
	 * @throws Exception if problem loading resources.
	 */
	private double loadResources(Worker loader, double amountLoading, Map<Integer, Number> manifest, boolean mandatory) {

		String  loaderName = loader.getName();
		
		// Load required resources. Take a cope as load will change the 
		// manifest
		Set<Integer> resources = new HashSet<>(manifest.keySet());
		for(Integer resource : resources) {
			if (resource < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
				// Load amount resources
				amountLoading = loadAmountResource(loaderName, amountLoading, resource, manifest, mandatory);
			}
			else {
				// Load item resources
				amountLoading = loadItemResource(loaderName, amountLoading, resource, manifest, mandatory);
			}
			
			// Exhausted the loading amount
			if (amountLoading <= 0D) {
				return 0;
			}
		}
		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
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
									  Map<Integer, Number> manifest, boolean mandatory) {

//		Inventory sInv = vehicle.getSettlement().getInventory();
		String resourceName = ResourceUtil.findAmountResourceName(resource);

		// Determine amount to load. 
		boolean usedSupply = false;
		double amountNeeded = manifest.get(resource).doubleValue();
		double amountToLoad = Math.min(amountNeeded, amountLoading);
		
		// Check the amount to load is not too small.
		if (amountToLoad < SMALLEST_RESOURCE_LOAD) {
			// Too small to load
			amountToLoad = 0;
			amountNeeded = 0;
		}
		
		if (amountToLoad > 0) {
			// Check if enough resource in settlement inventory.
			double settlementStored = settlement.getAmountResourceStored(resource);
			// add tracking demand
//			sInv.addAmountDemandTotalRequest(resource, amountToLoad);
		
			// Settlement has enough stored resource?
			if (settlementStored < amountToLoad) {
				if (mandatory) {
					logger.warning(vehicle, "Not enough available for loading " 
							+ resourceName
							+ Math.round(amountToLoad * 100D) / 100D 
							+ " kg. Settlement has "
							+ Math.round(settlementStored * 100D) / 100D
							+ " kg.");
					return amountLoading;
				}
				else {
					usedSupply = true;
					amountToLoad = settlementStored;
				}
			}	
			
			// Check remaining capacity in vehicle inventory.
			double remainingCapacity = vehicle.getAmountResourceRemainingCapacity(resource);
			if (remainingCapacity < amountToLoad) {
				if (mandatory && ((amountToLoad - remainingCapacity) > SMALLEST_RESOURCE_LOAD)) {
					// Will load up as much required resource as possible
					logger.warning(vehicle, "Not enough capacity for loading " 
							+ Math.round(amountToLoad * 100D) / 100D + " kg "
							+ resourceName
							+ ". Vehicle remaining cap: "
							+ Math.round(remainingCapacity * 100D) / 100D + " kg.");
				}
				usedSupply = true;
				amountToLoad = remainingCapacity;
			}

			// Load resource from settlement inventory to vehicle inventory.
			try {
				// Take resource from the settlement
				settlement.retrieveAmountResource(resource, amountToLoad);
				// Store resource in the vehicle
				vehicle.storeAmountResource(resource, amountToLoad);
			} catch (Exception e) {
				logger.severe(vehicle, "Cannot transfer from settlement to vehicle: ", e);
				return amountLoading;
			}			
		}

		// Check if this resource is complete
		amountNeeded -= amountToLoad;
		if (amountNeeded <= 0) {
			logger.fine(vehicle, loader + " completed loading amount " + resourceName);
			manifest.remove(resource);
		}
		else if (!mandatory && usedSupply) {
			logger.fine(vehicle, loader + " optional amount " + resourceName
						+ ", " + amountNeeded + " not loaded ");
			manifest.remove(resource);
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
	private double loadItemResource(String loader, double amountLoading, Integer id, Map<Integer, Number> manifest, boolean mandatory) {

//		Inventory sInv = vehicle.getSettlement().getInventory();
		Part p = ItemResourceUtil.findItemResource(id);
		boolean usedSupply = false;
		
		// Determine number to load. Could oad at least one
		// Part if needed
		int amountNeeded = (int)manifest.get(id).doubleValue();
		int amountCouldLoad = Math.max(1, (int)(amountLoading/p.getMassPerItem()));
		int amountToLoad = Math.min(amountNeeded, amountCouldLoad);
		if (amountToLoad > 0) {
			// Check if enough resource in settlement inventory.
			int settlementStored = settlement.getItemResourceStored(id);
			
			// add tracking demand
//			sInv.addItemDemand(id, amountToLoad);
		
			// Settlement has enough stored resource?
			if (settlementStored < amountToLoad) {
				if (mandatory) {
					logger.warning(vehicle, "Not enough available for loading " 
							+ p.getName()
							+ amountToLoad 
							+ ". Settlement has "
							+ settlementStored);
				}
				amountToLoad = settlementStored;
				usedSupply = true;
			}	
			
			// Check remaining capacity in vehicle inventory.
			double remainingMassCapacity = vehicle.getTotalCapacity() - vehicle.getStoredMass();
			if (remainingMassCapacity < 0D) {
				remainingMassCapacity = 0D;
			}
			double loadingMass = amountToLoad * p.getMassPerItem();
			if (remainingMassCapacity < loadingMass) {
				if (mandatory) {
					// Will load up as much required resource as possible
					logger.warning(vehicle, "Not enough capacity for loading " 
							+ Math.round(loadingMass * 100D) / 100D + " "
							+ p.getName()
							+ ". Vehicle remaining cap: "
							+ Math.round(remainingMassCapacity * 100D) / 100D + " kg.");
				}
				amountToLoad = (int) Math.floor(remainingMassCapacity/p.getMassPerItem());
			}

			// Load item from settlement inventory to vehicle inventory.
			try {
				// Take resource from the settlement
				settlement.retrieveItemResource(id, amountToLoad);
				// Store resource in the vehicle
				vehicle.storeItemResource(id, amountToLoad);
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
		}
		// If it's optional and attempted to load something then remove it.
		else if (!mandatory && usedSupply) {
			logger.fine(vehicle, loader + " optional item " + p.getName()
						+ ", " + amountNeeded + " not loaded ");
			manifest.remove(id);
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

//		Inventory sInv = settlement.getInventory();

		Set<Integer> eqmIds = new HashSet<>(manifest.keySet());
		for(Integer equipmentType : eqmIds) {
			int amountNeeded = manifest.get(equipmentType);
			if (amountNeeded > 0) {
				// How many available ?
				EquipmentType eType = EquipmentType.convertID2Type(equipmentType);
				List<Equipment> list = new ArrayList<>(settlement.getEquipmentTypeList(eType));
				for(Equipment eq : list) {
					if (eq.isEmpty(true)) {
						// Put this equipment into a vehicle
						boolean done = eq.transfer(settlement, vehicle);
						
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
			
			// Update the manifest
			if (amountNeeded == 0) {
				logger.fine(vehicle, "Completed loading equipment " + equipmentType);
				manifest.remove(equipmentType);
			}
			else if (!mandatory && (amountLoading > 0D)) {
				// For optional and still have capacity to load so abort
				logger.fine(vehicle, "Optional equipment " + equipmentType + " not loaded " + amountNeeded);
				manifest.remove(equipmentType);			
			}
			else {
				manifest.put(equipmentType, amountNeeded);
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
	 * it uses a different definition of resources needed
	 * @param amountLoading
	 */
	public void backgroundLoad(double amountLoading) {
		
		for (Integer id: BACKGROUND_RESOURCES) {
			if (resourcesManifest.containsKey(id)) {
				// Load this resource
				loadAmountResource("Background", amountLoading, id, resourcesManifest, true); 
			}
		}
	}

	/**
	 * Is the loading plan completed for the mandatory details.
	 * @return
	 */
	public boolean isCompleted() {
		// Manifest is empty so complete
		return resourcesManifest.isEmpty() && equipmentManifest.isEmpty()
				&& optionalResourcesManifest.isEmpty() && optionalEquipmentManifest.isEmpty();
	}

	public Map<Integer, Number> getResourcesManifest() {
		return Collections.unmodifiableMap(this.resourcesManifest);	
	}
	
	public Map<Integer, Number> getOptionalResourcesManifest() {
		return Collections.unmodifiableMap(this.optionalResourcesManifest);	
	}
	
	public Map<Integer, Integer> getEquipmentManifest() {
		return Collections.unmodifiableMap(this.equipmentManifest);	
	}
	
	public Map<Integer, Integer> getOptionalEquipmentManifest() {
		return Collections.unmodifiableMap(this.optionalEquipmentManifest);	
	}

	/**
	 * Settlement providing the resoruces for the load.
	 * @return
	 */
	public Settlement getSettlement() {
		return settlement;
	}
}

package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This class control the loading of a Vehicle. It exists for the lifetime
 * if a @link {@link LoadVehicleEVA} & @link {@link LoadVehicleGarage}
 */
public class LoadingController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The amount of resources (kg) one person of average strength can load per
	 * millisol.
	 */
	private static double LOAD_RATE = 20D;
	
	// Resources that can be loaded in the background
	private static final int[] BACKGROUND_RESOURCES = {
			ResourceUtil.oxygenID,
			ResourceUtil.waterID,
			ResourceUtil.methaneID
	};
	
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(LoadingController.class.getName());
	
	private Map<Integer, Number> resourcesManifest;
	private Map<Integer, Number> optionalResourcesManifest;
	private Map<Integer, Integer> equipmentManifest;
	private Map<Integer, Integer> optionalEquipmentManifest;
	private Settlement settlement;
	private Vehicle vehicle;	

	/**
	 * Define the Vehicle Mission that controls the loading
	 * @param vehicleMission
	 * @param settlement 
	 */
	public LoadingController(VehicleMission vehicleMission, Settlement settlement) {
		this.settlement = settlement;
		this.vehicle = vehicleMission.getVehicle();
		
		// Take copies as the quanities will be reduced
		resourcesManifest = new HashMap<>(vehicleMission.getRequiredResourcesToLoad());
		optionalResourcesManifest = new HashMap<>(vehicleMission.getOptionalResourcesToLoad());
		equipmentManifest = new HashMap<>(vehicleMission.getRequiredEquipmentToLoad());
		optionalEquipmentManifest = new HashMap<>(vehicleMission.getOptionalEquipmentToLoad());
		
		System.out.println("Resources :" + resourcesManifest);
		if (!optionalResourcesManifest.isEmpty()) {
			System.out.println("Optional Resources :" + optionalResourcesManifest);			
		}
		System.out.println("Equipment :" + equipmentManifest);
		if (!optionalEquipmentManifest.isEmpty()) {
			System.out.println("Optional Equip :" + optionalEquipmentManifest);			
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
		double amountLoading = LOAD_RATE * strengthModifier * time / 12D;

		// Temporarily remove rover from settlement so that inventory doesn't get mixed
		// in.
		Inventory sInv = settlement.getInventory();
		boolean vehicleInSettlement = false;
		if (sInv.containsUnit(vehicle)) {
			vehicleInSettlement = true;
			sInv.retrieveUnit(vehicle);
		}
		else { // if the rover is no longer in the settlement, end the task
			return true;
		}
				
		// Load equipment
		if (amountLoading > 0D) {
			amountLoading = loadEquipment(amountLoading);
		}
		
		// Load resources
		if (amountLoading > 0D) {
			amountLoading = loadResources(worker, amountLoading, resourcesManifest, true);
		}

		// Load optionals last
		if (amountLoading > 0D) {
			amountLoading = loadResources(worker, amountLoading, optionalResourcesManifest, false);
		}
		
		// Put rover back into settlement.
		if (vehicleInSettlement) {
			sInv.storeUnit(vehicle);
		}

		// Should the load stop for this worker? Either fully loaded or did not
		// use load amount (that means load couldn't complete it
		return (amountLoading > 0D) || isCompleted();
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
			} else if (resource >= ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
				// Load item resources
				amountLoading = loadItemResource(loaderName, amountLoading, resource, manifest, mandatory);
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
		Inventory vInv = vehicle.getInventory();
		Inventory sInv = vehicle.getSettlement().getInventory();
		String resourceName = ResourceUtil.findAmountResourceName(resource);

		// Determine amount to load.
		double amountNeeded = manifest.get(resource).doubleValue();
		double amountToLoad = Math.min(amountNeeded, amountLoading);
		if (amountToLoad > 0) {
			// Check if enough resource in settlement inventory.
			double settlementStored = sInv.getAmountResourceStored(resource, false);
			// add tracking demand
			sInv.addAmountDemandTotalRequest(resource, amountToLoad);
		
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
					amountToLoad = settlementStored;
				}
			}	
			
			// Check remaining capacity in vehicle inventory.
			double remainingCapacity = vInv.getAmountResourceRemainingCapacity(resource, true, false);
			if (remainingCapacity < amountToLoad) {
				if (mandatory) {
					// Will load up as much required resource as possible
					logger.warning(vehicle, "Not enough capacity for loading " 
							+ Math.round(amountToLoad * 100D) / 100D + " kg "
							+ resourceName
							+ ". Vehicle remaining cap: "
							+ Math.round(remainingCapacity * 100D) / 100D + " kg.");
					amountToLoad = remainingCapacity;
				}
				else {
					// Optional so skip
					return amountLoading;
				}
			}

			// Load resource from settlement inventory to vehicle inventory.
			try {
				// Take resource from the settlement
				sInv.retrieveAmountResource(resource, amountToLoad);
				// Store resource in the vehicle
				vInv.storeAmountResource(resource, amountToLoad, false);
				// Track amount demand
				sInv.addAmountDemand(resource, amountToLoad);
			} catch (Exception e) {
				logger.severe(vehicle, "B. Cannot transfer from settlement to vehicle: ", e);
				return amountLoading;
			}			
		}

		// Check if this resource is complete
		amountNeeded -= amountToLoad;
		if (amountNeeded == 0) {
			logger.info(vehicle, loader + " completed loading amount " + resourceName);
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

		Inventory vInv = vehicle.getInventory();
		Inventory sInv = vehicle.getSettlement().getInventory();
		Part p = ItemResourceUtil.findItemResource(id);
		
		// Determine number to load.
		int amountNeeded = (int)manifest.get(id).doubleValue();
		int amountToLoad = Math.min(amountNeeded, (int)(amountLoading/p.getMassPerItem()));
		if (amountToLoad > 0) {
			// Check if enough resource in settlement inventory.
			int settlementStored = sInv.getItemResourceNum(id);
			
			// add tracking demand
			sInv.addItemDemand(id, amountToLoad);
		
			// Settlement has enough stored resource?
			if (settlementStored < amountToLoad) {
				if (mandatory) {
					logger.warning(vehicle, "Not enough available for loading " 
							+ p.getName()
							+ amountToLoad 
							+ ". Settlement has "
							+ settlementStored);
					return amountLoading;
				}
				else {
					amountToLoad = settlementStored;
				}
			}	
			
			// Check remaining capacity in vehicle inventory.
			double remainingMassCapacity = vInv.getRemainingGeneralCapacity(false);
			double loadingMass = amountToLoad * p.getMassPerItem();
			if (remainingMassCapacity < loadingMass) {
				if (mandatory) {
					// Will load up as much required resource as possible
					logger.warning(vehicle, "Not enough capacity for loading " 
							+ Math.round(loadingMass * 100D) / 100D + " "
							+ p.getName()
							+ ". Vehicle remaining cap: "
							+ Math.round(remainingMassCapacity * 100D) / 100D + " kg.");
					amountToLoad = (int) Math.floor(remainingMassCapacity/p.getMassPerItem());
				}
				else {
					// Optional so skip
					return amountLoading;
				}
			}

			// Load item from settlement inventory to vehicle inventory.
			try {
				// Take resource from the settlement
				sInv.retrieveItemResources(id, amountToLoad);
				// Store resource in the vehicle
				vInv.storeItemResources(id, amountToLoad);
			} catch (Exception e) {
				logger.severe(vehicle, "Cannot transfer Item from settlement to vehicle: ", e);
				return amountLoading;
			}			
		}

		// Check if this resource is complete
		amountNeeded -= amountToLoad;
		if (amountNeeded == 0) {
			logger.info(vehicle, loader + " completed loading item " + p.getName());
			manifest.remove(id);
		}
		else {
			manifest.put(id, amountNeeded);
		}

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading - (amountToLoad * p.getMassPerItem());
	}

	/**
	 * Loads the vehicle with required and optional equipment from the settlement.
	 * 
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadEquipment(double amountLoading) {

		// Load required equipment.
		amountLoading = loadRequiredEquipment(amountLoading);

		// Load optional equipment.
		amountLoading = loadOptionalEquipment(amountLoading);

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
	}

	/**
	 * Loads the vehicle with required equipment from the settlement.
	 * 
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadRequiredEquipment(double amountLoading) {

		Inventory vInv = vehicle.getInventory();
		Inventory sInv = settlement.getInventory();

		Iterator<Integer> iE = equipmentManifest.keySet().iterator();
		while (iE.hasNext() && (amountLoading > 0D)) {
			Integer equipmentType = iE.next();
			int numNeededTotal = equipmentManifest.get(equipmentType);
			int numAlreadyLoaded = vInv.findNumEquipment(equipmentType);
			if (numAlreadyLoaded < numNeededTotal) {
				int numNeeded = numNeededTotal - numAlreadyLoaded;
				Collection<Unit> units = sInv.findAllUnitsOfClass(equipmentType);
				Object[] array = units.toArray();

				if (units.size() >= numNeeded) {
					int loaded = 0;
					for (int x = 0; (x < units.size()) && (loaded < numNeeded) && (amountLoading > 0D); x++) {
						Equipment eq = (Equipment) array[x];

						if (eq.isEmpty(true)) {
							if (vInv.canStoreUnit(eq, false)) {
								// Put this equipment into a vehicle
								eq.transfer(sInv, vInv);
//								sInv.retrieveUnit(eq);
//								vInv.storeUnit(eq);
								amountLoading -= eq.getMass();
								if (amountLoading < 0D) {
									amountLoading = 0D;
								}
								loaded++;
							} else {
                    			logger.warning(vehicle,"Cannot store " + eq);
							}
						}
					}

					array = null;
				}
			} else {

				if (optionalEquipmentManifest.containsKey(equipmentType)) {
					numNeededTotal += optionalEquipmentManifest.get(equipmentType);
				}

				if (numAlreadyLoaded > numNeededTotal) {

					// In case vehicle wasn't fully unloaded first.
					int numToRemove = numAlreadyLoaded - numNeededTotal;
					Collection<Unit> units = vInv.findAllUnitsOfClass(equipmentType);
					Object[] array = units.toArray();

					for (int x = 0; x < numToRemove; x++) {
						Equipment eq = (Equipment) array[x];
						// Put this equipment into the settlement
						eq.transfer(vInv, sInv);
//						vInv.retrieveUnit(eq);
//						sInv.storeUnit(eq);
					}

					array = null;
				}
			}
		}

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
	}

	/**
	 * Loads the vehicle with optional equipment from the settlement.
	 * 
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadOptionalEquipment(double amountLoading) {

		Inventory vInv = vehicle.getInventory();
		Inventory sInv = settlement.getInventory();

		Iterator<Integer> iE = optionalEquipmentManifest.keySet().iterator();
		while (iE.hasNext() && (amountLoading > 0D)) {
			Integer equipmentType = iE.next();
			int numNeededTotal = optionalEquipmentManifest.get(equipmentType);
			if (equipmentManifest.containsKey(equipmentType)) {
				numNeededTotal += equipmentManifest.get(equipmentType);
			}
			int numAlreadyLoaded = vInv.findNumEquipment(equipmentType);
			if (numAlreadyLoaded < numNeededTotal) {
				int numNeeded = numNeededTotal - numAlreadyLoaded;
				Collection<Unit> units = sInv.findAllUnitsOfClass(equipmentType);
				Object[] array = units.toArray();

				if (units.size() < numNeeded) {
					numNeeded = units.size();
				}

				int loaded = 0;
				for (int x = 0; (x < units.size()) && (loaded < numNeeded) && (amountLoading > 0D); x++) {
					Equipment eq = (Equipment) array[x];

					if (eq.isEmpty(true)) {
						if (vInv.canStoreUnit(eq, false)) {
							// Put this equipment into a vehicle
							eq.transfer(sInv, vInv);
//							sInv.retrieveUnit(eq);
//							vInv.storeUnit(eq);
							amountLoading -= eq.getMass();
							if (amountLoading < 0D) {
								amountLoading = 0D;
							}
							loaded++;
						} else {
                			logger.warning(vehicle, "Cannot store " + eq);
						}
					}
				}

				array = null;
			} else if (numAlreadyLoaded > numNeededTotal) {

				// In case vehicle wasn't fully unloaded first.
				int numToRemove = numAlreadyLoaded - numNeededTotal;
				Collection<Unit> units = vInv.findAllUnitsOfClass(equipmentType);
				Object[] array = units.toArray();

				for (int x = 0; x < numToRemove; x++) {
					Equipment eq = (Equipment) array[x];
					// Put this equipment into the settlement
					eq.transfer(vInv, sInv);
//					vInv.retrieveUnit(eq);
//					sInv.storeUnit(eq);
				}

				array = null;
			}
		}

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
	}

	/**
	 * Checks if the vehicle is fully loaded with resources.
	 * 
	 * @param requiredEquipment the equipment that is required for the trip.
	 * @param optionalEquipment the equipment that is optional for the trip.
	 * @param vehicle           the vehicle.
	 * @param settlement        the settlement that the vehicle is being loaded
	 *                          from.
	 * @return true if vehicle is full loaded.
	 */
	static boolean isFullyLoadedWithEquipment(Map<Integer, Integer> requiredEquipment,
			Map<Integer, Integer> optionalEquipment, Vehicle vehicle, Settlement settlement) {
		
		if (vehicle == null) {
			throw new IllegalArgumentException("vehicle is null");
		}

		boolean sufficientSupplies = true;
		
		Inventory vInv = vehicle.getInventory();
		Inventory sInv = settlement.getInventory();
		
		// Check that required equipment is loaded first.
		Iterator<Integer> iE = requiredEquipment.keySet().iterator();
		while (iE.hasNext() && sufficientSupplies) {
			Integer equipmentID = iE.next();
			String equipmentName = EquipmentType.convertID2Type(equipmentID).getName();
			
			int num = requiredEquipment.get(equipmentID);
			int availableNum = sInv.findNumEquipment(equipmentID);
			int storedNum = vInv.findNumEquipment(equipmentID);
			int toLoad = num - storedNum;
		
			if (storedNum < num) {
				sufficientSupplies = false;
				logger.info(vehicle, 10_000, vehicle.getMission() 
						+ " 6. Await loading " + num + "x " + equipmentName
						+ "  available: " + availableNum + "x "
						+ "  vehicle stored: " + storedNum + "x "
						+ "  toLoad: " + toLoad + "x "
						);
			}
			
			else
				logger.info(vehicle, 10_000, vehicle.getMission() 
						+ " 6. Done loading " + num + "x " + equipmentName
						+ "  available: " + availableNum + "x "
						+ "  vehicle stored: " + storedNum + "x "
						+ "  toLoad: " + toLoad + "x "
						);
		}

		// Check that optional equipment is loaded or can't be loaded.
		Iterator<Integer> iE2 = optionalEquipment.keySet().iterator();
		while (iE2.hasNext() && sufficientSupplies) {
			Integer equipmentID = iE2.next();
			
			String equipmentName = EquipmentType.convertID2Type(equipmentID).getName();
			
			int num = optionalEquipment.get(equipmentID);
			if (requiredEquipment.containsKey(equipmentID)) {
				num += requiredEquipment.get(equipmentID);
			}

			int storedNum = vInv.findNumEquipment(equipmentID);
			int availableNum = sInv.findNumEquipment(equipmentID);
			int toLoad = num - storedNum;

			if (storedNum < num) {
				sufficientSupplies = false;
				// Check if enough stored in settlement.
				int storedSettlement = sInv.findNumEmptyUnitsOfClass(equipmentID, false);
				if (settlement.getParkedVehicles().contains(vehicle)) {
					storedSettlement -= storedNum;
				}
				boolean hasStoredSettlement = (storedSettlement >= (num - storedNum));

				if (hasStoredSettlement) {
					logger.info(vehicle, 10_000, vehicle.getMission() 
					+ " 7. Await loading " + num + "x " + equipmentName
					+ "  available: " + availableNum + "x "
					+ "  vehicle stored: " + storedNum + "x "
					+ "  toLoad: " + toLoad + "x "
					);
				}
				
				else {
					logger.info(vehicle, 10_000, vehicle.getMission() 
					+ " 7. Insufficient settlement supply " + num + "x " + equipmentName
					+ "  available: " + availableNum + "x "
					+ "  vehicle stored: " + storedNum + "x "
					+ "  toLoad: " + toLoad + "x "
					);
				}
			}
			
			else {
				logger.info(vehicle, 10_000, vehicle.getMission() 
				+ " 7. Done loading " + num + "x " + equipmentName
				+ "  available: " + availableNum + "x "
				+ "  vehicle stored: " + storedNum + "x "
				+ "  toLoad: " + toLoad + "x "
				);
			}
		}

		return sufficientSupplies;
	}
	
	/**
	 * This is called in teh background from the Drone/Rover time pulse method 
	 * to load resources. But why is it different to the loadResources above,
	 * it uses a different defintion of reosurces needed
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
		boolean sufficientSupplies = resourcesManifest.isEmpty();
		
		// Check if there is enough equipment in the vehicle.
		if (sufficientSupplies && !equipmentManifest.isEmpty() && !optionalEquipmentManifest.isEmpty())
			sufficientSupplies = isFullyLoadedWithEquipment(equipmentManifest, optionalEquipmentManifest, vehicle, settlement);
		
		return sufficientSupplies;
	}
}

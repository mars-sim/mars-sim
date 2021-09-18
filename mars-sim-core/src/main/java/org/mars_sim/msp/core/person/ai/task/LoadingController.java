package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
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
	
	/** Comparison to indicate a small but non-zero amount. */
	private static final double SMALL_AMOUNT_COMPARISON = .0000001D;
	
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(LoadingController.class.getName());
	
	private Map<Integer, Number> requiredResources;
	private Map<Integer, Number> optionalResources;
	private Map<Integer, Integer> requiredEquipment;
	private Map<Integer, Integer> optionalEquipment;
	private VehicleMission vehicleMission;
	private Settlement settlement;
	private Vehicle vehicle;


	private boolean loadCompleted = false;

	/**
	 * Define the Vehicle Mission that controls the loading
	 * @param vehicleMission
	 * @param settlement 
	 */
	public LoadingController(VehicleMission vehicleMission, Settlement settlement) {
		this.vehicleMission = vehicleMission;
		this.settlement = settlement;
		this.vehicle = vehicleMission.getVehicle();
		requiredResources = vehicleMission.getRequiredResourcesToLoad();
		optionalResources = vehicleMission.getOptionalResourcesToLoad();
		requiredEquipment = vehicleMission.getRequiredEquipmentToLoad();
		optionalEquipment = vehicleMission.getOptionalEquipmentToLoad();
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
			amountLoading = vehicleMission.loadResources(amountLoading, 
						requiredResources, optionalResources);
		}


		// Put rover back into settlement.
		if (vehicleInSettlement) {
			sInv.storeUnit(vehicle);
		}

		// If load is not already completed check the amounts
		loadCompleted = loadCompleted || isFullyLoaded(requiredResources, optionalResources, 
				requiredEquipment, optionalEquipment, 
				vehicle, settlement);
		return loadCompleted;
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

		Iterator<Integer> iE = requiredEquipment.keySet().iterator();
		while (iE.hasNext() && (amountLoading > 0D)) {
			Integer equipmentType = iE.next();
			int numNeededTotal = requiredEquipment.get(equipmentType);
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
								loadCompleted = true;
							}
						}
					}

					array = null;
				} else {
					loadCompleted = true;
				}
			} else {

				if (optionalEquipment.containsKey(equipmentType)) {
					numNeededTotal += optionalEquipment.get(equipmentType);
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

		Iterator<Integer> iE = optionalEquipment.keySet().iterator();
		while (iE.hasNext() && (amountLoading > 0D)) {
			Integer equipmentType = iE.next();
			int numNeededTotal = optionalEquipment.get(equipmentType);
			if (requiredEquipment.containsKey(equipmentType)) {
				numNeededTotal += requiredEquipment.get(equipmentType);
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
							loadCompleted = true;
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
	 * Checks if the vehicle is fully loaded with supplies.
	 * 
	 * @param requiredResources the resources that are required for the trip.
	 * @param optionalResources the resources that are optional for the trip.
	 * @param requiredEquipment the equipment that is required for the trip.
	 * @param optionalEquipment the equipment that is optional for the trip.
	 * @param vehicle           the vehicle that is being checked.
	 * @param settlement        the settlement that the vehicle is being loaded
	 *                          from.
	 * @return true if vehicle is fully loaded.
	 */
	public static boolean isFullyLoaded(Map<Integer, Number> requiredResources, Map<Integer, Number> optionalResources,
			Map<Integer, Integer> requiredEquipment, Map<Integer, Integer> optionalEquipment, Vehicle vehicle,
			Settlement settlement) {

		boolean sufficientSupplies = true;

		// Check if there are enough resources in the vehicle.
		if (!requiredResources.isEmpty() && !optionalResources.isEmpty())
			sufficientSupplies = isFullyLoadedWithResources(requiredResources, optionalResources, vehicle, settlement);

		// Check if there is enough equipment in the vehicle.
		if (sufficientSupplies && !requiredEquipment.isEmpty() && !optionalEquipment.isEmpty())
			sufficientSupplies = isFullyLoadedWithEquipment(requiredEquipment, optionalEquipment, vehicle, settlement);

		return sufficientSupplies;
	}

	/**
	 * Checks if the vehicle is fully loaded with resources.
	 * 
	 * @param requiredResources the resources that are required for the trip.
	 * @param optionalResources the resources that are optional for the trip.
	 * @param vehicle           the vehicle.
	 * @param settlement        the settlement that the vehicle is being loaded
	 *                          from.
	 * @return true if vehicle is loaded.
	 */
	static boolean isFullyLoadedWithResources(Map<Integer, Number> requiredResources,
			Map<Integer, Number> optionalResources, Vehicle vehicle, Settlement settlement) {

		boolean sufficientSupplies = true;
		
		if (vehicle == null) {
			throw new IllegalArgumentException("vehicle is null");
		}
			
		Inventory vInv = vehicle.getInventory();
		Inventory sInv = settlement.getInventory();

		// Check that required resources are loaded first.
		Iterator<Integer> iR = requiredResources.keySet().iterator();
		while (iR.hasNext() && sufficientSupplies) {
			Integer resource = iR.next();
			if (resource < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
		
				String amountResource = ResourceUtil.findAmountResourceName(resource);

				double req = requiredResources.get(resource).doubleValue();
				double storedAmount = vInv.getAmountResourceStored(resource, false);
				double toLoad = req - storedAmount;
				double remainingCap = vInv.getAmountResourceRemainingCapacity(resource, true, false);
								
				if (storedAmount < (req - SMALL_AMOUNT_COMPARISON)) {
					sufficientSupplies = false;
					
					if (resource == ResourceUtil.oxygenID
							|| resource == ResourceUtil.waterID
							|| resource == ResourceUtil.methaneID
							|| resource == ResourceUtil.foodID
							|| PreparingDessert.isADessert(resource)
							) {
						// Note: account for occupants inside the vehicle to use up oxygen over time
						if (storedAmount > .95 * req) {
							sufficientSupplies = true;
							logger.info(vehicle, 10_000, "1. Within .95 margin for " + amountResource 
									+ " ->  stored: " + Math.round(storedAmount*10.0)/10.0 + " kg " 
									+ "  required: " + Math.round(req*10.0)/10.0 + " kg "
									+ "  toLoad: " + Math.round(toLoad*10.0)/10.0 + " kg "
									+ "  remainingCap: " + Math.round(remainingCap*10.0)/10.0 + " kg " 
									);
						}
					}

					else {
						logger.info(vehicle, 10_000, "1. Await loading " + amountResource 
							+ " ->  stored: " + Math.round(storedAmount*10.0)/10.0 + " kg "
							+ "  required: " + Math.round(req*10.0)/10.0 + " kg " 
							+ "  toLoad: " + Math.round(toLoad*10.0)/10.0 + " kg "
							+ "  remainingCap: " + Math.round(remainingCap*10.0)/10.0 + " kg " 
							);
					}
				}
				
				else {
					logger.info(vehicle, 10_000, "1. Done loading " + amountResource 
							+ " ->  stored: " + Math.round(storedAmount*10.0)/10.0 + " kg "
							+ "  required: " + Math.round(req*10.0)/10.0 + " kg " 
							+ "  toLoad: " + Math.round(toLoad*10.0)/10.0 + " kg "
							+ "  remainingCap: " + Math.round(remainingCap*10.0)/10.0 + " kg " 
							);
				}
			}
			
			else if (resource >= ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
				
				String itemResource = ItemResourceUtil.findItemResourceName(resource);
				
				int req = requiredResources.get(resource).intValue();
				int stored = vInv.getItemResourceNum(resource);
				if (stored < req) {
					sufficientSupplies = false;
					logger.info(vehicle, 10_000, "2. Await loading " + itemResource
							+ " ->  stored: " + Math.round(stored*10.0)/10.0 + "x" 
							+ "  required: " + Math.round(req*10.0)/10.0 + "x");
				}
				
				else {
					logger.info(vehicle, 10_000, "2. Done loading " + itemResource
							+ " ->  stored: " + Math.round(stored*10.0)/10.0 + "x" 
							+ "  required: " + Math.round(req*10.0)/10.0 + "x");
				}		
			} 
			
			else {
				throw new IllegalStateException("Unknown resource type: " + resource);
			}
		}

		// Check that optional resources are loaded or can't be loaded.
		Iterator<Integer> iR2 = optionalResources.keySet().iterator();
		while (iR2.hasNext() && sufficientSupplies) {
			Integer resource = iR2.next();
			if (resource < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {

				String amountResource = ResourceUtil.findAmountResourceName(resource);

				double req = optionalResources.get(resource).doubleValue();
				if (requiredResources.containsKey(resource)) {
					req += requiredResources.get(resource).doubleValue();
				}

				double storedAmount = vInv.getAmountResourceStored(resource, false);
				double toLoad = req - storedAmount;
				// Check if enough capacity in vehicle.
				double vehicleCapacity = vInv.getAmountResourceRemainingCapacity(resource, true, false);
			
				if (storedAmount < (req - SMALL_AMOUNT_COMPARISON)) {
					sufficientSupplies = false;
					
					if (resource == ResourceUtil.oxygenID
							|| resource == ResourceUtil.waterID
							|| resource == ResourceUtil.methaneID							
							|| resource == ResourceUtil.foodID
							|| PreparingDessert.isADessert(resource)
							) {
						// Note: account for occupants inside the vehicle to use up oxygen over time
						if (storedAmount > .95 * req) {
							sufficientSupplies = true;
							logger.info(vehicle, 10_000, "3. Within .95 margin for " + amountResource 
									+ " ->  stored: " + Math.round(storedAmount*10.0)/10.0 + " kg " 
									+ "  required: " + Math.round(req*10.0)/10.0 + " kg "
									+ "  toLoad: " + Math.round(toLoad*10.0)/10.0 + " kg "
									+ "  remainingCap: " + Math.round(vehicleCapacity*10.0)/10.0 + " kg " 
									);
						}
					}
						
					boolean hasVehicleCapacity = (vehicleCapacity >= toLoad);

					// Check if enough stored in settlement.
					double storedSettlement = sInv.getAmountResourceStored(resource, false);
					if (settlement.getParkedVehicles().contains(vehicle)) {
						storedSettlement -= storedAmount;
					}
									
					boolean hasStoredSettlement = (storedSettlement >= toLoad);

					if (!hasVehicleCapacity) {			
						logger.info(vehicle, 10_000, "4. Insufficient vehicle capacity for " + amountResource 
								+ " ->  storedAmount: " + Math.round(storedAmount*10.0)/10.0+ " kg " 
								+ "  storedSettlement: " + Math.round(storedSettlement*10.0)/10.0+ " kg " 
								+ "  required: " + Math.round(req*10.0)/10.0+ " kg " 
								+ "  toLoad: " + Math.round(toLoad*10.0)/10.0 + " kg "
								+ "  vehicleCapacity: " + Math.round(vehicleCapacity*10.0)/10.0 + " kg " );
					}
					
					else if (!hasStoredSettlement) {
						logger.info(vehicle, 10_000, "4. Insufficient settlement supply of " + amountResource 
								+ " ->  storedAmount: " + Math.round(storedAmount*10.0)/10.0+ " kg " 
								+ "  storedSettlement: " + Math.round(storedSettlement*10.0)/10.0+ " kg " 
								+ "  required: " + Math.round(req*10.0)/10.0+ " kg " 
								+ "  toLoad: " + Math.round(toLoad*10.0)/10.0 + " kg "
								+ "  vehicleCapacity: " + Math.round(vehicleCapacity*10.0)/10.0 + " kg " );
						// For now, if settlement doesn't have enough, let go of loading
						sufficientSupplies = true;
					}
					
					else {
						logger.info(vehicle, 10_000, "4. Await loading " + amountResource 
								+ " ->  storedAmount: " + Math.round(storedAmount*10.0)/10.0+ " kg " 
								+ "  storedSettlement: " + Math.round(storedSettlement*10.0)/10.0+ " kg " 
								+ "  required: " + Math.round(req*10.0)/10.0+ " kg " 
								+ "  toLoad: " + Math.round(toLoad*10.0)/10.0 + " kg "
								+ "  vehicleCapacity: " + Math.round(vehicleCapacity*10.0)/10.0 + " kg " );
					}
				}
				
				else {
					sufficientSupplies = true;
					logger.info(vehicle, 10_000, "4. Done loading " + amountResource 
							+ " ->  stored: " + Math.round(storedAmount*10.0)/10.0 + " kg "
							+ "  required: " + Math.round(req*10.0)/10.0 + " kg " 
							+ "  toLoad: " + Math.round(toLoad*10.0)/10.0 + " kg "
							+ "  vehicleCapacity: " + Math.round(vehicleCapacity*10.0)/10.0 + " kg " 
							);
				}
			}
			
			else if (resource >= ResourceUtil.FIRST_ITEM_RESOURCE_ID) {

				ItemResource ir = ItemResourceUtil.findItemResource(resource);
				String itemResource = ir.getName();

				int req = optionalResources.get(resource).intValue();
				if (requiredResources.containsKey(resource)) {
					req += requiredResources.get(resource).intValue();
				}

				int storedNum = vInv.getItemResourceNum(resource);
				int toLoad = req - storedNum;
				// Check if enough capacity in vehicle.
				double vehicleCapacity = vInv.getRemainingGeneralCapacity(false);
				
				if (storedNum < req) {
					sufficientSupplies = false;

					boolean hasVehicleCapacity = (vehicleCapacity >= ((req - storedNum)
							* ItemResourceUtil.findItemResource(resource).getMassPerItem()));

					// Check if enough stored in settlement.
					int storedSettlement = sInv.getItemResourceNum(resource);
					if (settlement.getParkedVehicles().contains(vehicle)) {
						storedSettlement -= storedNum;
					}
					boolean hasStoredSettlement = (storedSettlement >= (req - storedNum));

					if (!hasVehicleCapacity) {
						logger.info(vehicle, 10_000, "5. Insufficient vehicle capacity of " + itemResource
								+ "  stored: " + Math.round(storedNum*10.0)/10.0 + "x" 
								+ "  required: " + Math.round(req*10.0)/10.0 + "x"
								+ "  toLoad: " + Math.round(toLoad*10.0)/10.0 + "x "
								+ "  vehicleCapacity: " + Math.round(vehicleCapacity*10.0)/10.0 + " kg " );
					}
					
					else if (!hasStoredSettlement) {
						logger.info(vehicle, 10_000, "5. Insufficient settlement supply of " + itemResource
								+ "  stored: " + Math.round(storedNum*10.0)/10.0 + "x" 
								+ "  required: " + Math.round(req*10.0)/10.0 + "x"
								+ "  toLoad: " + Math.round(toLoad*10.0)/10.0 + "x "
								+ "  vehicleCapacity: " + Math.round(vehicleCapacity*10.0)/10.0 + " kg " );
					}
					
					else {
						logger.info(vehicle, 10_000, "5. Await loading " + itemResource
								+ "  stored: " + Math.round(storedNum*10.0)/10.0 + "x" 
								+ "  required: " + Math.round(req*10.0)/10.0 + "x"
								+ "  toLoad: " + Math.round(toLoad*10.0)/10.0 + "x "
								+ "  vehicleCapacity: " + Math.round(vehicleCapacity*10.0)/10.0 + " kg " );
					}
				}
				
				else {
					sufficientSupplies = true;
					logger.info(vehicle, 10_000, "5. Done loading " + itemResource
							+ "  stored: " + Math.round(storedNum*10.0)/10.0 + "x" 
							+ "  required: " + Math.round(req*10.0)/10.0 + "x"
							+ "  toLoad: " + Math.round(toLoad*10.0)/10.0 + "x "
							+ "  vehicleCapacity: " + Math.round(vehicleCapacity*10.0)/10.0 + " kg " );
				}
			} 
			
			else {
				throw new IllegalStateException("Unknown resource type: " + resource);
			}
		}

		return sufficientSupplies;
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
}

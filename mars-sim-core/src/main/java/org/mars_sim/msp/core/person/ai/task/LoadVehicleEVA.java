/**
 * Mars Simulation Project
 * LoadVehicleAmountResource.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The LoadVehicleEVA class is a task for loading a vehicle with fuel and
 * supplies when the vehicle is outside.
 */
public class LoadVehicleEVA extends EVAOperation implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(LoadVehicleEVA.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.loadVehicleEVA"); //$NON-NLS-1$

	/** Comparison to indicate a small but non-zero amount. */
	private static final double SMALL_AMOUNT_COMPARISON = .0000001D;

	/** Task phases. */
	private static final TaskPhase LOADING = new TaskPhase(Msg.getString("Task.phase.loading")); //$NON-NLS-1$

	/**
	 * The amount of resources (kg) one person of average strength can load per
	 * millisol.
	 */
	private static double LOAD_RATE = 20D;

	private static double WATER_NEED = 10D;
	private static double FOOD_NEED = 10D;
	private static double OXYGEN_NEED = 10D;

	// Data members
	/** The vehicle that needs to be loaded. */
	private Vehicle vehicle;
	/** The person's settlement instance. */
	private Settlement settlement;
	/** The vehicle mission instance. */
	private VehicleMission vehicleMission;
	
	/** Resources required to load. */
	private Map<Integer, Number> requiredResources;
	/** Resources desired to load but not required. */
	private Map<Integer, Number> optionalResources;
	/** Equipment required to load. */
	private Map<Integer, Integer> requiredEquipment;
	/** Equipment desired to load but not required. */
	private Map<Integer, Integer> optionalEquipment;

	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;
	private static int foodID = ResourceUtil.foodID;
//	private static int methaneID = ResourceUtil.methaneID;

	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public LoadVehicleEVA(Person person) {
		// Use Task constructor
		super(NAME, person, true, 20D + RandomUtil.getRandomInt(5) - RandomUtil.getRandomInt(5), null);
		
		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        	return;
		}

		settlement = CollectionUtils.findSettlement(person.getCoordinates());
		if (settlement == null) {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        	return;
		}
		
        if (!anyVehiclesNeedEVA(settlement)) {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        	return;
        }
        
		List<Rover> roversNeedingEVASuits = getRoversNeedingEVASuits(settlement);
		
		if (roversNeedingEVASuits.size() > 0) {
			int roverIndex = RandomUtil.getRandomInt(roversNeedingEVASuits.size() - 1);
			vehicle = roversNeedingEVASuits.get(roverIndex); 
			  
			requiredResources = new ConcurrentHashMap<Integer, Number>();
//            requiredResources.put(foodID, FOOD_NEED);
			requiredResources.put(waterID, WATER_NEED);
			requiredResources.put(oxygenID, OXYGEN_NEED);
			
			optionalResources = new ConcurrentHashMap<Integer, Number>(0);
			
			requiredEquipment = new ConcurrentHashMap<>(1);
			requiredEquipment.put(EquipmentType.convertName2ID(EVASuit.TYPE), 1);
			
			optionalEquipment = new ConcurrentHashMap<>(0);
		}
		
		vehicleMission = getRandomMissionNeedingLoading();
		if ((vehicle == null) && (vehicleMission != null)) {
			vehicle = vehicleMission.getVehicle();
			
			if (vehicle != null) {
				
				setDescription(Msg.getString("Task.description.loadVehicleEVA.detail", vehicle.getName())); // $NON-NLS-1$

				// Add the rover to a garage if possible.
				if (settlement.getBuildingManager().addToGarage(vehicle)) {
					// no need of doing EVA
		        	if (person.isOutside())
		        		setPhase(WALK_BACK_INSIDE);
		        	else
		        		endTask();
		        	return;
				}

				requiredResources = vehicleMission.getRequiredResourcesToLoad();
				optionalResources = vehicleMission.getOptionalResourcesToLoad();
				requiredEquipment = vehicleMission.getRequiredEquipmentToLoad();
				optionalEquipment = vehicleMission.getOptionalEquipmentToLoad();
				// Determine location for loading.
				Point2D loadingLoc = determineLoadingLocation();
				setOutsideSiteLocation(loadingLoc.getX(), loadingLoc.getY());
	
				// Initialize task phase
				addPhase(LOADING);
			}
			else {
				// no need of doing EVA
	        	if (person.isOutside())
	        		setPhase(WALK_BACK_INSIDE);
	        	else
	        		endTask();
	        	return;
			}
		
		}
		else {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return;
		}
	}

	/**
	 * Constructor
	 * 
	 * @param person            the person performing the task.
	 * @param vehicle           the vehicle to be loaded.
	 * @param requiredResources a map of required resources to be loaded.
	 * @param optionalResources a map of optional resources to be loaded.
	 * @param requiredEquipment a map of required equipment to be loaded.
	 * @param optionalEquipment a map of optional equipment to be loaded.
	 */
	public LoadVehicleEVA(Person person, Vehicle vehicle, Map<Integer, Number> requiredResources,
			Map<Integer, Number> optionalResources, Map<Integer, Integer> requiredEquipment,
			Map<Integer, Integer> optionalEquipment) {
		// Use Task constructor.
		super(NAME, person, true, 20D + RandomUtil.getRandomInt(5) - RandomUtil.getRandomInt(5), null);

		setDescription(Msg.getString("Task.description.loadVehicleEVA.detail", vehicle.getName())); // $NON-NLS-1$
		this.vehicle = vehicle;

		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        	return;
		}
		
		settlement = CollectionUtils.findSettlement(person.getCoordinates());
		if (settlement == null) {
			endTask();
			return;
		}
		if (requiredResources != null) {
			this.requiredResources = new ConcurrentHashMap<Integer, Number>(requiredResources);
		}
		if (optionalResources != null) {
			this.optionalResources = new ConcurrentHashMap<Integer, Number>(optionalResources);
		}
		if (requiredEquipment != null) {
			this.requiredEquipment = new ConcurrentHashMap<>(requiredEquipment);
		}
		if (optionalEquipment != null) {
			this.optionalEquipment = new ConcurrentHashMap<>(optionalEquipment);
		}
		
		// Determine location for loading.
		Point2D loadingLoc = determineLoadingLocation();
		setOutsideSiteLocation(loadingLoc.getX(), loadingLoc.getY());

		// Initialize task phase
		addPhase(LOADING);

	}


	@Override
	protected double performMappedPhase(double time) {
		time = super.performMappedPhase(time);
		if (!isDone()) {
			if (getPhase() == null) {
	            throw new IllegalArgumentException("Task phase is null");
			} else if (LOADING.equals(getPhase())) {
				time = loadingPhase(time);
			}
		}
		return time;
	}

	/**
	 * Perform the loading phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) after performing the phase.
	 */
	double loadingPhase(double time) {
			
		// Check for radiation exposure during the EVA operation.
		if (isDone() || isRadiationDetected(time)) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return time;
		}

		if (shouldEndEVAOperation() || addTimeOnSite(time)) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return time;
		}
		
		// NOTE: if a person is not at a settlement or near its vicinity,  
		if (settlement == null || vehicle == null) {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return 0;
		}
		
		if (settlement.getBuildingManager().isInGarage(vehicle)) {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return 0;
		}
		
//		logger.log(person, Level.INFO, 20_000, vehicle.getName() + " not in garage.");

        if (!anyVehiclesNeedEVA(settlement)) {
			if (person.isOutside())
				setPhase(WALK_BACK_INSIDE);	
			else 
				endTask();
			return 0;
        }
        
		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
		}
		
//		logger.log(person, Level.INFO, 20_000, "Is fit for loading " + vehicle.getName());
		
		// Determine load rate.
		int strength = worker.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRENGTH);

		double strengthModifier = .1D + (strength * .018D);
		double amountLoading = LOAD_RATE * strengthModifier * time / 16D;

		// Temporarily remove rover from settlement so that inventory doesn't get mixed in.
		Inventory sInv = settlement.getInventory();
		boolean vehicleInSettlement = false;
		if (sInv.containsUnit(vehicle)) {
			vehicleInSettlement = true;
			sInv.retrieveUnit(vehicle);
		}
		else { // if the rover is no longer in the settlement, end the task
			if (person.isOutside())
				setPhase(WALK_BACK_INSIDE);				
			else
				endTask();
			return 0;
		}

		// Load equipment
		if (amountLoading > 0D) {
			amountLoading = loadEquipment(amountLoading);
		}

		// Load resources
		try {
			amountLoading = loadResources(amountLoading);
		} catch (Exception e) {
			logger.severe(person, "Load resources", e);
		}

		// Resume from previous and put rover back into settlement.
		if (vehicleInSettlement) {
			sInv.storeUnit(vehicle);
		}
		
//		logger.log(person, Level.INFO, 20_000, "stored rover back " + vehicle.getName());
		
		if (isFullyLoaded(requiredResources, optionalResources, requiredEquipment, optionalEquipment, vehicle,
				settlement)) {
			if (person.isOutside())
				setPhase(WALK_BACK_INSIDE);	
			else
				endTask();
			return 0;
		}
		
//		logger.log(person, Level.INFO, 20_000, vehicle.getName() + " not fully loaded.");
		
        // Add experience points
        addExperience(time);

		// Check for an accident during the EVA operation.
		checkForAccident(time);
		
		return 0;
	}

	/**
	 * Loads the vehicle with required resources from the settlement.
	 * 
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadResources(double amountLoading) {

		// Load required resources.
		Iterator<Integer> iR = requiredResources.keySet().iterator();
		while (iR.hasNext() && (amountLoading > 0D)) {
			Integer resource = iR.next();
			if (resource < FIRST_ITEM_RESOURCE_ID) {
				// Load amount resources
				amountLoading = loadAmountResource(amountLoading, resource, true);
			} else if (resource >= FIRST_ITEM_RESOURCE_ID) {
				// Load item resources
				amountLoading = loadItemResource(amountLoading, resource, true);
			}
		}

		// Load optional resources.
		Iterator<Integer> iR2 = optionalResources.keySet().iterator();
		while (iR2.hasNext() && (amountLoading > 0D)) {
			Integer resource = iR2.next();
			if (resource < FIRST_ITEM_RESOURCE_ID) {
				// Load amount resources
				amountLoading = loadAmountResource(amountLoading, resource, false);
			} else if (resource >= FIRST_ITEM_RESOURCE_ID) {
				// Load item resources
				amountLoading = loadItemResource(amountLoading, resource, false);
			}
		}

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
	}

	/**
	 * Loads the vehicle with an amount resource from the settlement.
	 * 
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @param id      the amount resource to be loaded.
	 * @param required      true if the amount resource is required to load, false
	 *                      if optional.
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadAmountResource(double amountLoading, Integer resource, boolean required) {
		Inventory vInv = vehicle.getInventory();
		Inventory sInv = settlement.getInventory();
		
		double amountNeededTotal = 0D;
		if (required) {
			amountNeededTotal = requiredResources.get(resource).doubleValue();
		} else {
			if (requiredResources.containsKey(resource)) {
				amountNeededTotal += requiredResources.get(resource).doubleValue();
			}
			amountNeededTotal += optionalResources.get(resource).doubleValue();
		}

		double amountAlreadyLoaded = vInv.getAmountResourceStored(resource, false);
		
//		if (resource == 1)
//			LogConsolidated.log(Level.INFO, 0, sourceName, //"LoadVehicleGarage's loadAmountResource - " + 
//			System.out.println("1. " + 
//				vehicle.getName() + " - "
//				+ ResourceUtil.findAmountResourceName(resource) 
//				+ " needed: " + Math.round(amountNeededTotal*100.0)/100.0
//				+ " already loaded: " + Math.round(amountAlreadyLoaded*100.0)/100.0);
		
		
		if (amountAlreadyLoaded < amountNeededTotal) {
			double amountNeeded = amountNeededTotal - amountAlreadyLoaded;
			boolean canLoad = true;
			String loadingErrorMsg = "";

			// Check if enough resource in settlement inventory.
			double settlementStored = sInv.getAmountResourceStored(resource, false);
			// add tracking demand
			sInv.addAmountDemandTotalRequest(resource, amountNeeded);
		
			if (settlementStored < amountNeeded) {
				if (required) {
					canLoad = false;
					loadingErrorMsg = "Not enough capacity for loading " 
							+ ResourceUtil.findAmountResourceName(resource)
							+ Math.round(amountNeeded * 100D) / 100D 
							+ " kg. Available: "
							+ Math.round(settlementStored * 100D) / 100D
							+ " kg.";
				} else {
					amountNeeded = settlementStored;
				}
			}	
			
			// Check remaining capacity in vehicle inventory.
			double remainingCapacity = vInv.getAmountResourceRemainingCapacity(resource, true, false);
			if (remainingCapacity < amountNeeded) {
				
				if (required) {
					
					// Will load up as much required resource as possible
					// Dump the excess amount if no more room to store it
					canLoad = true;
					double excess = 0;
					
					if ((amountNeeded - remainingCapacity) < SMALL_AMOUNT_COMPARISON) {
						amountNeeded = remainingCapacity;
					}
					
					else {
						
						excess = amountNeeded - remainingCapacity;
						
						if (remainingCapacity <= SMALL_AMOUNT_COMPARISON) {
							logger.warning(vehicle, "Not enough capacity for loading " 
									+ Math.round(amountNeeded * 100D) / 100D + " kg "
									+ ResourceUtil.findAmountResourceName(resource) 
									+ ". Remaining capacity: "
									+ Math.round(remainingCapacity * 100D) / 100D + " kg.");
						}
						
						// Check the dedicated capacity of a resource in vehicle inventory.
						double amountResourceCapacity = vInv.getAmountResourceCapacity(resource, false);
						double totalRemainingCapacity = vInv.getRemainingGeneralCapacity(false);
						
						if (amountNeeded > amountResourceCapacity) {
							
							double excess1 = SMALL_AMOUNT_COMPARISON + amountNeeded - amountResourceCapacity;
							
							amountNeeded = amountResourceCapacity - SMALL_AMOUNT_COMPARISON;
							
							// Take care of the excess first
							if (excess1 <= totalRemainingCapacity) {
								
								logger.warning(vehicle,	"Allowed loading only " 
										+ Math.round(remainingCapacity * 100D) / 100D + " kg "
										+ ResourceUtil.findAmountResourceName(resource) 
										+ ".");

								// Load resource from settlement inventory to vehicle inventory.
								try {
									// Take resource from the settlement
									sInv.retrieveAmountResource(resource, excess1);
									// Store resource to the vehicle
									vInv.storeAmountResource(resource, excess1, false);
									// Track amount demand
									sInv.addAmountDemand(resource, excess1);
									
								} catch (Exception e) {
									e.printStackTrace(System.err);
								}						
							}
						}
						
						// Subtract the amount to be loaded
						amountLoading -= excess;
					}
				}
				
				else {
					amountNeeded = remainingCapacity;
				}
			}

			// Determine amount to load.
			double resourceAmount = Math.min(amountNeeded, amountLoading);

			if (canLoad) {

				// Load resource from settlement inventory to vehicle inventory.
				try {
					sInv.retrieveAmountResource(resource, resourceAmount);
					vInv.storeAmountResource(resource, resourceAmount, true);
					
					sInv.addAmountDemand(resource, resourceAmount);
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
				amountLoading -= resourceAmount;
			}
			
			else {
    			logger.warning(vehicle, loadingErrorMsg);
				endTask();
			}
		}
		
		else { // if (amountAlreadyLoaded >= amountNeededTotal) {
			if (required && optionalResources.containsKey(resource)) {
				amountNeededTotal += optionalResources.get(resource).doubleValue();
			}

			if (amountAlreadyLoaded > 0 && amountNeededTotal > 0 
					&& amountAlreadyLoaded > amountNeededTotal) {

				// In case vehicle wasn't fully unloaded first.
				double amountToRemove = amountAlreadyLoaded - amountNeededTotal;
				
				if (amountToRemove > SMALL_AMOUNT_COMPARISON) {
					try {
						vInv.retrieveAmountResource(resource, amountToRemove);
						sInv.storeAmountResource(resource, amountToRemove, true);
						
//						if (resource == 1)
//							LogConsolidated.log(Level.INFO, 0, sourceName, //"LoadVehicleGarage's loadAmountResource - " + 
//									System.out.println("4. " + 
//									vehicle.getName() + " - "
//									+ ResourceUtil.findAmountResourceName(resource) 
//									+ " needed: " + amountNeededTotal
//									+ " loaded: " + amountAlreadyLoaded
//									+ " returning: " + amountToRemove);
					
					} catch (Exception e) {
						logger.warning(vehicle, "Was trying to return the excessive " + ResourceUtil.findAmountResourceName(resource));
					}
				}
			}
		}

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
	}

	/**
	 * Loads the vehicle with an item resource from the settlement.
	 * 
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @param resource      the item resource to be loaded.
	 * @param required      true if the item resource is required to load, false if
	 *                      optional.
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadItemResource(double amountLoading, Integer resource, boolean required) {

		Inventory vInv = vehicle.getInventory();
		Inventory sInv = settlement.getInventory();

		int numNeededTotal = 0;
		if (required) {
			numNeededTotal = (int) requiredResources.get(resource);
		} else {
			if (requiredResources.containsKey(resource)) {
				numNeededTotal += (int) requiredResources.get(resource);
			}
			numNeededTotal += (int) optionalResources.get(resource);
		}

		int numAlreadyLoaded = vInv.getItemResourceNum(resource);

		if (numAlreadyLoaded < numNeededTotal) {
			int numNeeded = numNeededTotal - numAlreadyLoaded;
			boolean canLoad = true;
			String loadingError = "";

			// Check if enough resource in settlement inventory.
			int settlementStored = sInv.getItemResourceNum(resource);
			// Add tracking demand
			sInv.addItemDemandTotalRequest(resource, numNeeded);
			
			if (settlementStored < numNeeded) {
				if (required) {
					canLoad = false;
					loadingError = " did NOT have enough resource stored at settlement to load " + "resource: "
							+ resource + " needed: " + numNeeded + ", stored: " + settlementStored;
				} else {
					numNeeded = settlementStored;
				}
			}

			ItemResource ir = ItemResourceUtil.findItemResource(resource);

			// Check remaining capacity in vehicle inventory.
			double remainingMassCapacity = vInv.getRemainingGeneralCapacity(false);
			if (remainingMassCapacity < (numNeeded * ir.getMassPerItem())) {
				if (required) {
					canLoad = false;
					loadingError = " did NOT have enough capacity in vehicle for loading resource " + resource + ": "
							+ numNeeded + ", remaining capacity: " + remainingMassCapacity + " kg";
				} else {
					numNeeded = (int) (remainingMassCapacity / ir.getMassPerItem());
				}
			}

			// Determine amount to load.
			int resourceNum = (int) (amountLoading / ir.getMassPerItem());
			if (resourceNum < 1) {
				resourceNum = 1;
			}
			if (resourceNum > numNeeded) {
				resourceNum = numNeeded;
			}

			if (canLoad) {

				// Load resource from settlement inventory to vehicle inventory.
				sInv.retrieveItemResources(resource, resourceNum);
				vInv.storeItemResources(resource, resourceNum);
				// add tracking demand
				sInv.addItemDemand(resource, resourceNum);
				amountLoading -= (resourceNum * ir.getMassPerItem());
				if (amountLoading < 0D)
					amountLoading = 0D;
			} else {
				logger.warning(vehicle, loadingError);
				endTask();
//                throw new IllegalStateException(loadingError);
			}
		} else {
			if (required && optionalResources.containsKey(resource)) {
				numNeededTotal += optionalResources.get(resource).intValue();
			}

			if (numAlreadyLoaded > numNeededTotal) {

				// In case vehicle wasn't fully unloaded first.
				int numToRemove = numAlreadyLoaded - numNeededTotal;
				try {
					vInv.retrieveItemResources(resource, numToRemove);
					sInv.storeItemResources(resource, numToRemove);
				} catch (Exception e) {
				}
			}
		}

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
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
			int numNeededTotal = requiredEquipment.get(equipmentType).intValue();
			int numAlreadyLoaded = vInv.findNumEquipment(equipmentType);
			if (numAlreadyLoaded < numNeededTotal) {
				int numNeeded = numNeededTotal - numAlreadyLoaded;

				Collection<Unit> units = sInv.findAllUnitsOfClass(equipmentType);
				Object[] array = units.toArray();

				if (units.size() >= numNeeded) {
					int loaded = 0;
					for (int x = 0; (x < units.size()) && (loaded < numNeeded) && (amountLoading > 0D); x++) {
						Equipment eq = (Equipment) array[x];

						boolean isEmpty = true;
						Inventory eInv = eq.getInventory();
						if (eInv != null) {
							isEmpty = eq.getInventory().isEmpty(false);
						}

						if (isEmpty) {
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
								logger.warning(vehicle, "Cannot store " + eq);
								endTask();
							}
						}
					}

					array = null;
				} else {
					endTask();
				}
			} else {

				if (optionalEquipment.containsKey(equipmentType)) {
					numNeededTotal += optionalEquipment.get(equipmentType).intValue();
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
//			int i0 = numNeededTotal;
			
			if (requiredEquipment.containsKey(equipmentType)) {
				numNeededTotal += requiredEquipment.get(equipmentType);
			}
			int numAlreadyLoaded = vInv.findNumEquipment(equipmentType);
			
//			System.out.println("LoadVehicleEVA's loadOptionalEquipment() id : " + id 
//					+ "   amountLoading : " + amountLoading 
//					+ "   i0 : " + i0 
//					+ "   numNeededTotal : " + numNeededTotal 
//					+ "   numAlreadyLoaded : " + numAlreadyLoaded);
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

					boolean isEmpty = true;
					Inventory eInv = eq.getInventory();
					if (eInv != null) {
						isEmpty = eq.getInventory().isEmpty(false);
					}

					if (isEmpty) {
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
							endTask();
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
	 * Gets a list of all embarking vehicle missions at a settlement with vehicle
	 * currently in a garage.
	 * 
	 * @param settlement the settlement.
	 * @return list of vehicle missions.
	 */
	public static List<Mission> getAllMissionsNeedingLoading(Settlement settlement) {

		List<Mission> result = new CopyOnWriteArrayList<Mission>();

		Iterator<Mission> i = missionManager.getMissions().iterator();
		while (i.hasNext()) {
			Mission mission = (Mission) i.next();
			if (mission instanceof VehicleMission) {
				if (VehicleMission.EMBARKING.equals(mission.getPhase())) {
					VehicleMission vm = (VehicleMission) mission;
					if (vm.hasVehicle()) {
						Vehicle vehicle = vm.getVehicle();
						if (settlement == vehicle.getSettlement()) {
							if (!vm.isVehicleLoaded()) {
								if (!settlement.getBuildingManager().addToGarage(vehicle)) {
									result.add(vm);
								}
							}
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Checks if any vehicle need EVA operation
	 * 
	 * @param settlement
	 * @return
	 */
	public static boolean anyVehiclesNeedEVA(Settlement settlement) {

		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();
			if (vehicle.isReservedForMission()) { 
				if (!settlement.getBuildingManager().isInGarage(vehicle)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Gets a list of rovers with crew who are missing EVA suits.
	 * 
	 * @param settlement the settlement.
	 * @return list of rovers.
	 */
	public static List<Rover> getRoversNeedingEVASuits(Settlement settlement) {

		List<Rover> result = new CopyOnWriteArrayList<Rover>();

		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();
			if (vehicle instanceof Rover) {
				Rover rover = (Rover) vehicle;
				if (!settlement.getBuildingManager().addToGarage(vehicle)) {
					Inventory roverInv = rover.getInventory();
					int peopleOnboard = roverInv.findNumUnitsOfClass(Person.class);
					if ((peopleOnboard > 0)) {
						int numSuits = roverInv.findNumEVASuits(false, false);
						double water = roverInv.getAmountResourceStored(ResourceUtil.waterID, false);
						double oxygen = roverInv.getAmountResourceStored(ResourceUtil.oxygenID, false);
						if ((numSuits == 0) || (water < WATER_NEED) || (oxygen < OXYGEN_NEED)) {
							result.add(rover);
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets a random vehicle mission loading at the settlement.
	 * 
	 * @return vehicle mission.
	 */
	private VehicleMission getRandomMissionNeedingLoading() {

		VehicleMission result = null;
		List<Mission> loadingMissions = getAllMissionsNeedingLoading(settlement);

		if (loadingMissions.size() > 0) {
			int index = RandomUtil.getRandomInt(loadingMissions.size() - 1);
			result = (VehicleMission) loadingMissions.get(index);
		}

		return result;
	}

	/**
	 * Gets the vehicle being loaded.
	 * 
	 * @return vehicle
	 */
	public Vehicle getVehicle() {
		return vehicle;
	}

	/**
	 * Determine location to load the vehicle.
	 * 
	 * @return location.
	 */
	private Point2D determineLoadingLocation() {

		Point2D.Double newLocation = null;
		boolean goodLocation = false;
		for (int x = 0; (x < 50) && !goodLocation; x++) {
			Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(vehicle, 1D);
			newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), boundedLocalPoint.getY(),
					vehicle);
			goodLocation = LocalAreaUtil.isLocationCollisionFree(newLocation.getX(), newLocation.getY(),
					worker.getCoordinates());
		}

		return newLocation;
	}

	@Override
	protected TaskPhase getOutsideSitePhase() {
		return LOADING;
	}

	/**
	 * Checks if there are enough supplies in the settlement's stores to supply
	 * trip.
	 * 
	 * @param settlement     the settlement the vehicle is at.
	 * @param resources      a map of resources required for the trip.
	 * @param equipment      a map of equipment required for the trip.
	 * @param vehicleCrewNum the number of people in the vehicle crew.
	 * @param tripTime       the estimated time for the trip (millisols).
	 * @return true if enough supplies
	 */
	public static boolean hasEnoughSupplies(Settlement settlement, Vehicle vehicle, Map<Integer, Number> resources,
			Map<Integer, Integer> equipment, int vehicleCrewNum, double tripTime) {

		return LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resources, equipment, vehicleCrewNum, tripTime);
	}

	/**
	 * Checks if a vehicle has enough storage capacity for the supplies needed on
	 * the trip.
	 * 
	 * @param resources  a map of the resources required.
	 * @param equipment  a map of the equipment types and numbers needed.
	 * @param vehicle    the vehicle to check.
	 * @param settlement the settlement to disembark from.
	 * @return true if vehicle can carry supplies.
	 */
	public static boolean enoughCapacityForSupplies(Map<Integer, Number> resources,
			Map<Class<? extends Equipment>, Integer> equipment, Vehicle vehicle, Settlement settlement) {

		boolean sufficientCapacity = true;

		// Create vehicle inventory clone.
		Inventory inv = vehicle.getInventory().clone(null);

		try {
			// Add equipment clones.
			Iterator<Class<? extends Equipment>> i = equipment.keySet().iterator();
			while (i.hasNext()) {
				Class<? extends Equipment> equipmentType = i.next();
				int num = equipment.get(equipmentType);
//				Coordinates defaultLoc = new Coordinates(0D, 0D);
				for (int x = 0; x < num; x++) {
					inv.storeUnit(EquipmentFactory.createEquipment(equipmentType, settlement.getCoordinates(), true));
				}
			}

			// Add all resources.
			Iterator<Integer> j = resources.keySet().iterator();
			while (j.hasNext()) {
				Integer resource = j.next();
				if (resource < FIRST_ITEM_RESOURCE_ID) {
					double amount = resources.get(resource).doubleValue();
					inv.storeAmountResource(resource, amount, true);
				} else {
					int num = resources.get(resource).intValue();
					inv.storeItemResources(resource, num);
				}
			}
		} catch (Exception e) {
			logger.severe(vehicle, "NOT have enough capacity "
							+ " to store needed resources for a proposed mission. ", e);
			sufficientCapacity = false;
		}

		return sufficientCapacity;
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
		sufficientSupplies = isFullyLoadedWithResources(requiredResources, optionalResources, vehicle, settlement);

		// Check if there is enough equipment in the vehicle.
		if (sufficientSupplies) {
			sufficientSupplies = isFullyLoadedWithEquipment(requiredEquipment, optionalEquipment, vehicle, settlement);
		}

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
	private static boolean isFullyLoadedWithResources(Map<Integer, Number> requiredResources,
			Map<Integer, Number> optionalResources, Vehicle vehicle, Settlement settlement) {

		if (vehicle == null) {
			throw new IllegalArgumentException("vehicle is null");
		}

		boolean sufficientSupplies = true;
		Inventory vInv = vehicle.getInventory();
		Inventory sInv = settlement.getInventory();

		double sumAmount = 0;
		int sumNum = 0;
		String amountResource = null;
		String itemResource = null;
			
		// Check that required resources are loaded first.
		Iterator<Integer> iR = requiredResources.keySet().iterator();
		while (iR.hasNext() && sufficientSupplies) {
			Integer resource = iR.next();
			
			if (resource < FIRST_ITEM_RESOURCE_ID) {
				
				if (amountResource == null)
					amountResource = ResourceUtil.findAmountResourceName(resource);
				
				double amount = requiredResources.get(resource).doubleValue();
				sumAmount += amount;
				double storedAmount = vInv.getAmountResourceStored(resource, false);
				if (storedAmount < (amount - SMALL_AMOUNT_COMPARISON)) {
					sufficientSupplies = false;
				}
			} 
			
			else if (resource >= FIRST_ITEM_RESOURCE_ID) {
				
				if (itemResource == null)
					itemResource = ItemResourceUtil.findItemResourceName(resource);
				
				int num = requiredResources.get(resource).intValue();
				sumNum += num;
				if (vInv.getItemResourceNum(resource) < num) {
					sufficientSupplies = false;
				}
			} 
			
			
			else {
				throw new IllegalStateException("Unknown resource type: " + resource);
			}
		}

		if (sumAmount > 0)
			logger.info(vehicle, 10_000, "Loading " + Math.round(sumAmount*10.0)/10.0+ " kg " + amountResource);
		if (sumNum > 0)
			logger.info(vehicle, 10_000, "Loading " + sumNum + "x " + itemResource);
		
		
		sumAmount = 0;
		sumNum = 0;
		amountResource = null;
		itemResource = null;
		ItemResource ir = null;
		
		// Check that optional resources are loaded or can't be loaded.
		Iterator<Integer> iR2 = optionalResources.keySet().iterator();
		while (iR2.hasNext() && sufficientSupplies) {
			Integer resource = iR2.next();
			
			if (resource < FIRST_ITEM_RESOURCE_ID) {
				if (amountResource == null)
					amountResource = ResourceUtil.findAmountResourceName(resource);
				
				// AmountResource amountResource = (AmountResource) resource;
				double amount = optionalResources.get(resource).doubleValue();
				sumAmount += amount;
				if (requiredResources.containsKey(resource)) {
					amount += requiredResources.get(resource).doubleValue();
					sumAmount += amount;
				}

				double storedAmount = vInv.getAmountResourceStored(resource, false);
				if (storedAmount < (amount - SMALL_AMOUNT_COMPARISON)) {
					// Check if enough capacity in vehicle.
					double vehicleCapacity = vInv.getAmountResourceRemainingCapacity(resource, true, false);
					boolean hasVehicleCapacity = (vehicleCapacity >= (amount - storedAmount));

					// Check if enough stored in settlement.
					double storedSettlement = sInv.getAmountResourceStored(resource, false);
					if (settlement.getParkedVehicles().contains(vehicle)) {
						storedSettlement -= storedAmount;
					}
					boolean hasStoredSettlement = (storedSettlement >= (amount - storedAmount));

					if (hasVehicleCapacity && hasStoredSettlement) {
						sufficientSupplies = false;
					}
				}	
			}
			
			else if (resource >= FIRST_ITEM_RESOURCE_ID) {
				
				if (ir == null) {
					ir = ItemResourceUtil.findItemResource(resource);
					itemResource = ir.getName();
				}
					
				int num = optionalResources.get(resource).intValue();
				sumNum += num;
				if (requiredResources.containsKey(resource)) {
					num += requiredResources.get(resource).intValue();
					sumNum += num;
				}

				int storedNum = vInv.getItemResourceNum(resource);
				if (storedNum < num) {
					// Check if enough capacity in vehicle.
					double vehicleCapacity = vInv.getRemainingGeneralCapacity(false);
					boolean hasVehicleCapacity = (vehicleCapacity >= ((num - storedNum) * ir.getMassPerItem()));

					// Check if enough stored in settlement.
					int storedSettlement = sInv.getItemResourceNum(resource);
					if (settlement.getParkedVehicles().contains(vehicle)) {
						storedSettlement -= storedNum;
					}
					boolean hasStoredSettlement = (storedSettlement >= (num - storedNum));

					if (hasVehicleCapacity && hasStoredSettlement) {
						sufficientSupplies = false;
					}
				}
			} 
			
			else {
				throw new IllegalStateException("Unknown resource type: " + resource);
			}
		}

		if (sumAmount > 0)
			logger.info(vehicle, 10_000, "Loading " + Math.round(sumAmount*10.0)/10.0 + " kg " + amountResource);
		if (sumNum > 0)
			logger.info(vehicle, 10_000, "Loading " + sumNum + "x " + itemResource);
		
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
	private static boolean isFullyLoadedWithEquipment(Map<Integer, Integer> requiredEquipment,
			Map<Integer, Integer> optionalEquipment, Vehicle vehicle, Settlement settlement) {

		if (vehicle == null) {
			throw new IllegalArgumentException("vehicle is null");
		}

		boolean sufficientSupplies = true;
		Inventory vInv = vehicle.getInventory();
		Inventory sInv = settlement.getInventory();
		
		String equipmentName = null;
		int sumNum = 0;
		
		// Check that required equipment is loaded first.
		Iterator<Integer> iE = requiredEquipment.keySet().iterator();
		while (iE.hasNext() && sufficientSupplies) {
			Integer equipmentID = iE.next();
			
			if (equipmentName == null)
				equipmentName = EquipmentType.convertID2Type(equipmentID).getName();
			
			int num = requiredEquipment.get(equipmentID);
			sumNum += num;
			if (vInv.findNumEquipment(equipmentID) < num) {
				sufficientSupplies = false;
			}
		}

		if (sumNum > 0)
			logger.info(vehicle, 10_000, "Loading " + sumNum + "x " + equipmentName);
		
		equipmentName = null;
		sumNum = 0;
		
		// Check that optional equipment is loaded or can't be loaded.
		Iterator<Integer> iE2 = optionalEquipment.keySet().iterator();
		while (iE2.hasNext() && sufficientSupplies) {
			Integer equipmentID = iE2.next();
			
			if (equipmentName == null)
				equipmentName = EquipmentType.convertID2Type(equipmentID).getName();
			
			int num = optionalEquipment.get(equipmentID);
			sumNum += num;
			if (requiredEquipment.containsKey(equipmentID)) {
				num += requiredEquipment.get(equipmentID);
				sumNum += num;
			}

			int storedNum = vInv.findNumEquipment(equipmentID);
			if (storedNum < num) {

				// Check if enough stored in settlement.
				int storedSettlement = sInv.findNumEmptyUnitsOfClass(equipmentID, false);
				if (settlement.getParkedVehicles().contains(vehicle)) {
					storedSettlement -= storedNum;
				}
				boolean hasStoredSettlement = (storedSettlement >= (num - storedNum));

				if (hasStoredSettlement) {
					sufficientSupplies = false;
				}
			}
		}

		if (sumNum > 0)
			logger.info(vehicle, 10_000, "Loading " + sumNum + "x " + equipmentName);
		
		return sufficientSupplies;
	}
}

/*
 * Mars Simulation Project
 * FoodProduction.java
 * @date 2021-10-21
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.food.FoodProductionProcess;
import org.mars_sim.msp.core.food.FoodProductionProcessInfo;
import org.mars_sim.msp.core.food.FoodProductionProcessItem;
import org.mars_sim.msp.core.food.FoodProductionUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.time.ClockPulse;

/**
 * A building function for foodProduction.
 */
public class FoodProduction extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final String CONCURRENT_PROCESSES = "concurrent-processes";

	/** default logger. */
	private static final Logger logger = Logger.getLogger(FoodProduction.class.getName());

	private static final double PROCESS_MAX_VALUE = 100D;

	// Data members.
	private int techLevel;
	private int concurrentProcesses;
	
	private List<FoodProductionProcess> processes;

	/**
	 * Constructor.
	 * 
	 * @param building the building the function is for.
	 * @param spec Spec of the function
	 * @throws BuildingException if error constructing function.
	 */
	public FoodProduction(Building building, FunctionSpec spec) {
		// Use Function constructor.
		super(FunctionType.FOOD_PRODUCTION, building);

		techLevel = spec.getTechLevel();
		concurrentProcesses = spec.getIntegerProperty(CONCURRENT_PROCESSES);

		processes = new ArrayList<>();
	}

	/**
	 * Gets the value of the function for a named building type.
	 * 
	 * @param buildingType the building type.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingType, boolean newBuilding, Settlement settlement) {

		double result = 0D;

		FunctionSpec spec = buildingConfig.getFunctionSpec(buildingType, FunctionType.FOOD_PRODUCTION);

		int buildingTech = spec.getTechLevel();

		double demand = 0D;
		for(Person p : settlement.getAllAssociatedPeople()) {
			demand += p.getSkillManager().getSkillLevel(SkillType.COOKING);
		}

		double supply = 0D;
		int highestExistingTechLevel = 0;
		boolean removedBuilding = false;
		for(Building building : settlement.getBuildingManager().getBuildings(FunctionType.FOOD_PRODUCTION)) {
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingType) && !removedBuilding) {
				removedBuilding = true;
			} else {
				FoodProduction manFunction = building.getFoodProduction();
				int tech = manFunction.techLevel;
				double processes = manFunction.concurrentProcesses;
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += (tech * tech) * processes * wearModifier;

				if (tech > highestExistingTechLevel) {
					highestExistingTechLevel = tech;
				}
			}
		}

		double baseFoodProductionValue = demand / (supply + 1D);

		double processes = spec.getIntegerProperty(CONCURRENT_PROCESSES);
		double foodProductionValue = (buildingTech * buildingTech) * processes;

		result = foodProductionValue * baseFoodProductionValue;

		// If building has higher tech level than other buildings at settlement,
		// add difference between best foodProduction processes.
		if (buildingTech > highestExistingTechLevel) {
			double bestExistingProcessValue = 0D;
			if (highestExistingTechLevel > 0D) {
				bestExistingProcessValue = getBestFoodProductionProcessValue(highestExistingTechLevel, settlement);
			}
			double bestBuildingProcessValue = getBestFoodProductionProcessValue(buildingTech, settlement);
			double processValueDiff = bestBuildingProcessValue - bestExistingProcessValue;

			if (processValueDiff < 0D) {
				processValueDiff = 0D;
			}

			if (processValueDiff > PROCESS_MAX_VALUE) {
				processValueDiff = PROCESS_MAX_VALUE;
			}

			result += processValueDiff;
		}

		return result;
	}

	/**
	 * Gets the best foodProduction process value for a given foodProduction tech
	 * level at a settlement.
	 * 
	 * @param techLevel  the foodProduction tech level.
	 * @param settlement the settlement
	 * @return best foodProduction process value.
	 */
	private static double getBestFoodProductionProcessValue(int techLevel, Settlement settlement) {

		double result = 0D;

		Iterator<FoodProductionProcessInfo> i = FoodProductionUtil.getAllFoodProductionProcesses().iterator();
		while (i.hasNext()) {
			FoodProductionProcessInfo process = i.next();
			if (process.getTechLevelRequired() <= techLevel) {
				double value = FoodProductionUtil.getFoodProductionProcessValue(process, settlement);
				if (value > result) {
					result = value;
				}
			}
		}

		return result;
	}

	/**
	 * Gets the foodProduction tech level of the building.
	 * 
	 * @return tech level.
	 */
	public int getTechLevel() {
		return techLevel;
	}

	/**
	 * Gets the maximum concurrent foodProduction processes supported by the
	 * building.
	 * 
	 * @return maximum concurrent processes.
	 */
	public int getConcurrentProcesses() {
		return concurrentProcesses;
	}

	/**
	 * Gets the total food production processes currently in this building.
	 * 
	 * @return total process number.
	 */
	public int getTotalProcessNumber() {
		// return processes.size() + salvages.size();
		return processes.size();
	}

	/**
	 * Gets a list of the current foodProduction processes.
	 * 
	 * @return unmodifiable list of processes.
	 */
	public List<FoodProductionProcess> getProcesses() {
		return Collections.unmodifiableList(processes);
	}

	/**
	 * Adds a new foodProduction process to the building.
	 * 
	 * @param process the new foodProduction process.
	 * @throws BuildingException if error adding process.
	 */
	public void addProcess(FoodProductionProcess process) {

		if (process == null) {
			throw new IllegalArgumentException("process is null");
		}
		if (getTotalProcessNumber() >= concurrentProcesses) {
			throw new IllegalStateException("No space to add new foodProduction process.");
		}
		processes.add(process);

		// Consume inputs.
		for (FoodProductionProcessItem item : process.getInfo().getInputList()) {
			if (ItemType.AMOUNT_RESOURCE.equals(item.getType())) {
//				AmountResource resource = ResourceUtil.findAmountResource(item.getName());
				int id = ResourceUtil.findIDbyAmountResourceName(item.getName());
				getBuilding().getSettlement().retrieveAmountResource(id, item.getAmount());
				// Add tracking demand
//				inv.addAmountDemandTotalRequest(id, item.getAmount());
//				inv.addAmountDemand(id, item.getAmount());
			} else if (ItemType.PART.equals(item.getType())) {
//				Part part = (Part) ItemResourceUtil.findItemResource(item.getName());
				int id = ItemResourceUtil.findIDbyItemResourceName(item.getName());
				getBuilding().getSettlement().retrieveItemResource(id, (int) item.getAmount());
				// Add tracking demand
//				inv.addItemDemandTotalRequest(id, (int) item.getAmount());
//				inv.addItemDemand(id, (int) item.getAmount());
			} else
				throw new IllegalStateException(
						"FoodProduction process input, invalid type: " + item.getType());

			// Recalculate settlement good value for input item.
//            if (goodsManager == null)
//            	goodsManager = settlement.getGoodsManager();
//            settlement.getGoodsManager().updateGoodValue(FoodProductionUtil.getGood(item), false);
		}

		// Log foodProduction process starting.
		if (logger.isLoggable(Level.FINEST)) {

			logger.finest(getBuilding() + " at " + building.getSettlement() + " starting food production process: "
					+ process.getInfo().getName());
		}
	}

	@Override
	public double getFullPowerRequired() {
		double result = 0D;
		Iterator<FoodProductionProcess> i = processes.iterator();
		while (i.hasNext()) {
			FoodProductionProcess process = i.next();
			if (process.getProcessTimeRemaining() > 0D)
				result += process.getInfo().getPowerRequired();
		}
		return result;
	}

	@Override
	public double getPoweredDownPowerRequired() {
		double result = 0D;
		Iterator<FoodProductionProcess> i = processes.iterator();
		while (i.hasNext()) {
			FoodProductionProcess process = i.next();
			if (process.getProcessTimeRemaining() > 0D)
				result += process.getInfo().getPowerRequired();
		}
		return result;
	}

	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
			List<FoodProductionProcess> finishedProcesses = new ArrayList<FoodProductionProcess>();
	
			Iterator<FoodProductionProcess> i = processes.iterator();
			while (i.hasNext()) {
				FoodProductionProcess process = i.next();
				process.addProcessTime(pulse.getElapsed());
	
				if ((process.getProcessTimeRemaining() == 0D) && (process.getWorkTimeRemaining() == 0D)) {
					finishedProcesses.add(process);
				}
			}
	
			// End all processes that are done.
			Iterator<FoodProductionProcess> j = finishedProcesses.iterator();
			while (j.hasNext()) {
				endFoodProductionProcess(j.next(), false);
			}
		}
		return valid;
	}

	/**
	 * Checks if foodProduction function currently requires foodProduction work.
	 * 
	 * @param skill the person's materials science skill level.
	 * @return true if foodProduction work.
	 */
	public boolean requiresFoodProductionWork(int skill) {
		boolean result = false;

		if (concurrentProcesses > getTotalProcessNumber())
			result = true;
		else {
			Iterator<FoodProductionProcess> i = processes.iterator();
			while (i.hasNext()) {
				FoodProductionProcess process = i.next();
				boolean workRequired = (process.getWorkTimeRemaining() > 0D);
				boolean skillRequired = (process.getInfo().getSkillLevelRequired() <= skill);
				if (workRequired && skillRequired)
					result = true;
			}
		}

		return result;
	}

	/**
	 * Ends a foodProduction process.
	 * 
	 * @param process   the process to end.
	 * @param premature true if the process has ended prematurely.
	 * @throws BuildingException if error ending process.
	 */
	public void endFoodProductionProcess(FoodProductionProcess process, boolean premature) {
		Settlement settlement = building.getSettlement();
	
		if (!premature) {
			// Produce outputs.
			Iterator<FoodProductionProcessItem> j = process.getInfo().getOutputList().iterator();
			while (j.hasNext()) {
				FoodProductionProcessItem item = j.next();
				if (FoodProductionUtil.getFoodProductionProcessItemValue(item, settlement, true) > 0D) {
					if (ItemType.AMOUNT_RESOURCE.equals(item.getType())) {
						// Produce amount resources.
//						AmountResource resource = ResourceUtil.findAmountResource(item.getName());
						int id = ResourceUtil.findIDbyAmountResourceName(item.getName());
						double amount = item.getAmount();
						double capacity = settlement.getAmountResourceRemainingCapacity(id);
						if (item.getAmount() > capacity) {
							double overAmount = item.getAmount() - capacity;
							logger.fine("Not enough storage capacity to store " + overAmount + " of " + item.getName()
									+ " from " + process.getInfo().getName() + " at " + settlement.getName());
							amount = capacity;
						}
						settlement.storeAmountResource(id, amount);
						// Add tracking supply
//						inv.addAmountSupply(id, amount);
						// Add to the daily output
						settlement.addOutput(id, amount, process.getTotalWorkTime());
					} 
					
					else if (ItemType.PART.equals(item.getType())) {
						// Produce parts.
						Part part = (Part) ItemResourceUtil.findItemResource(item.getName());
						int id = part.getID();//ItemResourceUtil.findIDbyItemResourceName(item.getName());
						int num = (int) item.getAmount();
						double mass = num * part.getMassPerItem();
						double capacity = settlement.getCargoCapacity();
						if (mass <= capacity) {
							settlement.storeItemResource(id, num);
//							inv.addItemSupply(id, num);
							// Add to the daily output
							settlement.addOutput(id, num, process.getTotalWorkTime());
						}
					} 
					
					else if (ItemType.EQUIPMENT.equals(item.getType())) {
						// Produce equipment.
						String equipmentType = item.getName();
						int number = (int) item.getAmount();
						for (int x = 0; x < number; x++) {
							Equipment equipment = EquipmentFactory.createEquipment(equipmentType,
									settlement);
							
							// Place this equipment within a settlement
							unitManager.addUnit(equipment);
							// Add this equipment as being owned by this settlement
							settlement.addEquipment(equipment);
							// Set the container unit
							equipment.setContainerUnit(settlement);
							// Add to the daily output
							settlement.addOutput(equipment.getIdentifier(), number, process.getTotalWorkTime());
						}
					}
					else
						throw new IllegalStateException(
								"FoodProduction.addProcess(): output: invalid type:" + item.getType());

					// Recalculate settlement good value for output item.
//					getBuilding().getSettlement().getGoodsManager()
//							.updateGoodValue(FoodProductionUtil.getGood(item), false);
				}
			}
		} 
		
		else {

			// Premature end of process. Return all input materials.
			// Settlement settlement = getBuilding().getBuildingManager().getSettlement();
			// UnitManager manager = Simulation.instance().getUnitManager();
			// Inventory inv = getBuilding().getSettlementInventory();

			Iterator<FoodProductionProcessItem> j = process.getInfo().getInputList().iterator();
			while (j.hasNext()) {
				FoodProductionProcessItem item = j.next();
				if (FoodProductionUtil.getFoodProductionProcessItemValue(item, settlement, false) > 0D) {
					if (ItemType.AMOUNT_RESOURCE.equals(item.getType())) {
						// Produce amount resources.
//						AmountResource resource = ResourceUtil.findAmountResource(item.getName());
						int id = ResourceUtil.findIDbyAmountResourceName(item.getName());
						double amount = item.getAmount();
						double capacity = settlement.getAmountResourceRemainingCapacity(id);
						if (item.getAmount() > capacity) {
							double overAmount = item.getAmount() - capacity;
							logger.severe("Not enough storage capacity to store " + overAmount + " of " + item.getName()
									+ " from " + process.getInfo().getName() + " at " + settlement.getName());
							amount = capacity;
						}
						settlement.storeAmountResource(id, amount);
						// Add tracking supply
//						inv.addAmountSupply(id, amount);
					} else if (ItemType.PART.equals(item.getType())) {
						// Produce parts.
						Part part = (Part) ItemResourceUtil.findItemResource(item.getName());
						int id = part.getID();
						double mass = item.getAmount() * part.getMassPerItem();
						double capacity = settlement.getCargoCapacity();
						if (mass <= capacity) {
							settlement.storeItemResource(id, (int) item.getAmount());
						}
					} else if (ItemType.EQUIPMENT.equals(item.getType())) {
						// Produce equipment.
						String equipmentType = item.getName();
						int number = (int) item.getAmount();
						for (int x = 0; x < number; x++) {
							Equipment equipment = EquipmentFactory.createEquipment(equipmentType,
									settlement);
							// Place this equipment within a settlement
							settlement.addEquipment(equipment);
						}
					}
//                    else if (Type.VEHICLE.equals(item.getType())) {
//                        // Produce vehicles.
//                        String vehicleType = item.getName();
//                        int number = (int) item.getAmount();
//                        for (int x = 0; x < number; x++) {
//                            if (LightUtilityVehicle.NAME.equalsIgnoreCase(vehicleType)) {
//                                String name = manager.getNewName(UnitType.VEHICLE, "LUV", null);
//                                manager.addUnit(new LightUtilityVehicle(name, vehicleType, settlement));
//                            }
//                            else {
//                                String name = manager.getNewName(UnitType.VEHICLE, null, null);
//                                manager.addUnit(new Rover(name, vehicleType, settlement));
//                            }
//                        }
//                    }
					else
						throw new IllegalStateException(
								"FoodProduction.addProcess(): output: invalid type:" + item.getType());

					// Recalculate settlement good value for output item.
					// GoodsManager goodsManager =
					// getBuilding().getBuildingManager().getSettlement().getGoodsManager();
//					getBuilding().getBuildingManager().getSettlement().getGoodsManager()
//							.updateGoodValue(FoodProductionUtil.getGood(item), false);
				}
			}
		}

		processes.remove(process);

		// Log process ending.
		if (logger.isLoggable(Level.FINEST)) {
			logger.finest(getBuilding() + " at " + settlement + " ending foodProduction process: "
					+ process.getInfo().getName());
		}
	}

	@Override
	public double getMaintenanceTime() {
		double result = 0D;

		// Add maintenance for tech level.
		result += techLevel * 10D;

		// Add maintenance for concurrect process capacity.
		result += concurrentProcesses * 10D;

		return result;
	}

	@Override
	public void destroy() {
		super.destroy();

		Iterator<FoodProductionProcess> i = processes.iterator();
		while (i.hasNext()) {
			i.next().destroy();
		}

	}
}

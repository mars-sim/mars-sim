/*
 * Mars Simulation Project
 * FoodProductionUtil.java
 * @date 2021-10-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.food;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.equipment.BinFactory;
import org.mars_sim.msp.core.equipment.BinType;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.goods.Good;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.goods.GoodsUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.function.FoodProduction;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.time.MarsTime;

/**
 * Utility class for getting food production processes.
 */
public final class FoodProductionUtil {

	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(FoodProductionUtil.class.getName());

	private static final FoodProductionConfig config = SimulationConfig.instance().getFoodProductionConfiguration();

	/** Private constructor. */
	private FoodProductionUtil() {
	}

	/**
	 * Gets all food production processes.
	 *
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public static List<FoodProductionProcessInfo> getAllFoodProductionProcesses() {
		return config.getFoodProductionProcessList();
	}

	/**
	 * Gives back an alphabetically ordered map of all food production processes.
	 *
	 * @return {@link TreeMap}<{@link String},{@link FoodProductionProcessInfo}>
	 */
	public static TreeMap<String, FoodProductionProcessInfo> getAllFoodProductionProcessesMap() {
		TreeMap<String, FoodProductionProcessInfo> map = new TreeMap<>();
		for (FoodProductionProcessInfo item : getAllFoodProductionProcesses()) {
			map.put(item.getName(), item);
		}
		return map;
	}

	/**
	 * Gets food production processes within the capability of a tech level.
	 *
	 * @param techLevel the tech level.
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public static List<FoodProductionProcessInfo> getFoodProductionProcessesForTechLevel(int techLevel) {
		return getAllFoodProductionProcesses().stream()
				.filter(s -> s.getTechLevelRequired() <= techLevel)
    	        .collect(Collectors.toList());
	}

	/**
	 * Gets food production processes with given output.
	 *
	 * @param item {@link String} name of desired output
	 * @return {@link List}<{@link FoodProductionProcessItem}> list of processes
	 */
	public static List<FoodProductionProcessInfo> getFoodProductionProcessesWithGivenOutput(String name) {
		List<FoodProductionProcessInfo> result = new ArrayList<>();
		Iterator<FoodProductionProcessInfo> i = getAllFoodProductionProcesses().iterator();
		while (i.hasNext()) {
			FoodProductionProcessInfo process = i.next();
			for (String n : process.getOutputNames()) {
				if (name.equalsIgnoreCase(n))
					result.add(process);
			}
		}
		return result;
	}

	/**
	 * Gets food production processes with given input.
	 *
	 * @param name {@link String} desired input
	 * @return {@link List}<{@link FoodProductionProcessItem}> list of processes
	 */
	public static List<FoodProductionProcessInfo> getFoodProductionProcessesWithGivenInput(String name) {
		List<FoodProductionProcessInfo> result = new ArrayList<>();
		Iterator<FoodProductionProcessInfo> i = getAllFoodProductionProcesses().iterator();
		while (i.hasNext()) {
			FoodProductionProcessInfo process = i.next();
			for (String n : process.getInputNames()) {
				if (name.equalsIgnoreCase(n))
					result.add(process);
			}
		}
		return result;
	}

	/**
	 * Gets food production processes within the capability of a tech level and a
	 * skill level.
	 *
	 * @param techLevel  the tech level.
	 * @param skillLevel the skill level.
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public static List<FoodProductionProcessInfo> getFoodProductionProcessesForTechSkillLevel(int techLevel,
			int skillLevel) {
		return getAllFoodProductionProcesses().stream()
				.filter(s -> (s.getTechLevelRequired() <= techLevel) && (s.getSkillLevelRequired() <= skillLevel))
    	        .collect(Collectors.toList());
	}

	/**
	 * Gets the goods value of a food production process at a settlement.
	 *
	 * @param process    the food production process.
	 * @param settlement the settlement.
	 * @return goods value of output goods minus input goods.
	 * @throws Exception if error determining good values.
	 */
	public static double getFoodProductionProcessValue(FoodProductionProcessInfo process, Settlement settlement) {

		double inputsValue = 0D;
		Iterator<FoodProductionProcessItem> i = process.getInputList().iterator();
		while (i.hasNext())
			inputsValue += getFoodProductionProcessItemValue(i.next(), settlement, false);

		double outputsValue = 0D;
		Iterator<FoodProductionProcessItem> j = process.getOutputList().iterator();
		while (j.hasNext())
			outputsValue += getFoodProductionProcessItemValue(j.next(), settlement, true);

		// Subtract power value.
		double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsTime.HOURS_PER_MILLISOL;
		double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

		return outputsValue - inputsValue - powerValue;
	}

	/**
	 * Gets the good value of a food production process item for a settlement.
	 *
	 * @param item       the food production process item.
	 * @param settlement the settlement.
	 * @param isOutput   is item an output of process?
	 * @return good value.
	 * @throws Exception if error getting good value.
	 */
	public static double getFoodProductionProcessItemValue(FoodProductionProcessItem item, Settlement settlement,
			boolean isOutput) {
		double result = 0D;

		GoodsManager manager = settlement.getGoodsManager();

		if (item.getType() == ItemType.AMOUNT_RESOURCE) {
	        int id = ResourceUtil.findIDbyAmountResourceName(item.getName());
			double amount = item.getAmount();
			if (isOutput) {
				double remainingCapacity = settlement.getAmountResourceRemainingCapacity(id);
				if (amount > remainingCapacity) {
					amount = remainingCapacity;
				}
			}
			
			result = manager.getGoodValuePoint(id) * amount;
			
		} else if (item.getType() == ItemType.PART) {
			int id = ItemResourceUtil.findIDbyItemResourceName(item.getName());
			result = manager.getGoodValuePoint(id) * item.getAmount();
		} else if (item.getType() == ItemType.EQUIPMENT) {
			int id = EquipmentType.convertName2ID(item.getName());
			result = manager.getGoodValuePoint(id) * item.getAmount();
		} else
			throw new IllegalStateException("Item type: " + item.getType() + " not valid.");

		return result;
	}

	/**
	 * Checks to see if a food production process can be started at a given
	 * food production building.
	 *
	 * @param process the food production process to start.
	 * @param kitchen the food production building.
	 * @return true if process can be started.
	 * @throws Exception if error determining if process can be started.
	 */
	public static boolean canProcessBeStarted(FoodProductionProcessInfo process, FoodProduction kitchen) {

		// Check to see if this workshop can accommodate another process.
		if (kitchen.getMaxProcesses() < kitchen.getCurrentTotalProcesses()) {
			// NOTE: create a map to show which process has a 3D printer in use and which doesn't
			return false;
		}
		
        // Check to see if process tech level is above kitchen tech level.
		if (kitchen.getTechLevel() < process.getTechLevelRequired()) {
			return false;
		}

		Settlement settlement = kitchen.getBuilding().getSettlement();

		// Check to see if process input items are available at settlement.
		if (!areProcessInputsAvailable(process, settlement)) {
			return false;
		}

		// Check to see if room for process output items at settlement.
		if (!canProcessOutputsBeStored(process, settlement)) {
			return false;
		}

		return true;
	}

	/**
	 * Checks if process inputs are available in an inventory.
	 *
	 * @param process the food production process.
	 * @param inv     the inventory.
	 * @return true if process inputs are available.
	 * @throws Exception if error determining if process inputs are available.
	 */
	private static boolean areProcessInputsAvailable(FoodProductionProcessInfo process, Settlement settlement) {
		boolean result = true;

		Iterator<FoodProductionProcessItem> i = process.getInputList().iterator();
		while (result && i.hasNext()) {
			FoodProductionProcessItem item = i.next();
			if (ItemType.AMOUNT_RESOURCE == item.getType()) {
				int id = ResourceUtil.findIDbyAmountResourceName(item.getName());
				result = (settlement.getAmountResourceStored(id) >= item.getAmount());
			} else if (ItemType.PART == item.getType()) {
				int id = ItemResourceUtil.findIDbyItemResourceName(item.getName());
				result = (settlement.getItemResourceStored(id) >= (int) item.getAmount());
			} else
				throw new IllegalStateException(
						"FoodProduction process input: " + item.getType() + " not a valid type.");
		}

		return result;
	}

	/**
     * Checks if enough storage room for process outputs in an inventory.
     *
     * @param process the manufacturing process.
     * @param inv the inventory.
     * @return true if storage room.
     * @throws Exception if error determining storage room for outputs.
     */
	private static final boolean canProcessOutputsBeStored(FoodProductionProcessInfo process, Settlement settlement) {

		Iterator<FoodProductionProcessItem> j = process.getOutputList().iterator();
		while (j.hasNext()) {
			FoodProductionProcessItem item = j.next();
			if (ItemType.AMOUNT_RESOURCE == item.getType()) {
				double capacity = settlement.getAmountResourceRemainingCapacity(ResourceUtil.findIDbyAmountResourceName(item.getName()));
				if (item.getAmount() > capacity)
					return false;
			}

			else if (ItemType.PART == item.getType()) {
				double mass = item.getAmount() * ((Part) ItemResourceUtil.findItemResource(item.getName())).getMassPerItem();
				double capacity = settlement.getCargoCapacity();
				if (mass > capacity)
					return false;
			}

			else if (ItemType.EQUIPMENT == item.getType()) {
				int number = (int) item.getAmount();
				double mass = EquipmentFactory.getEquipmentMass(EquipmentType.convertName2Enum(item.getName())) * number;
				double capacity = settlement.getCargoCapacity();
				if (mass > capacity)
					return false;
			}
			
			else if (ItemType.BIN == item.getType()) {
				int number = (int) item.getAmount();
				double mass = BinFactory.getBinMass(BinType.convertName2Enum(item.getName())) * number;
				double capacity = settlement.getCargoCapacity();
				if (mass > capacity)
					return false;
			}
			
			else
				logger.severe(settlement, "FoodProductionUtil.addProcess(): output: " +
					item.getType() + " not a valid type.");
		}

		return true;
	}

	/**
	 * Checks if settlement has buildings with food production function.
	 *
	 * @param settlement the settlement.
	 * @return true if buildings with food production function.
	 * @throws BuildingException if error checking for food production buildings.
	 */
	public static boolean doesSettlementHaveFoodProduction(Settlement settlement) {
		return (settlement.getBuildingManager().getBuildings(FunctionType.FOOD_PRODUCTION).size() > 0);
	}

	/**
	 * Gets the highest food production tech level in a settlement.
	 *
	 * @param settlement the settlement.
	 * @return highest food production tech level.
	 * @throws BuildingException if error determining highest tech level.
	 */
	public static int getHighestFoodProductionTechLevel(Settlement settlement) {
		int highestTechLevel = 0;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.FOOD_PRODUCTION).iterator();
		while (i.hasNext()) {
			FoodProduction foodProductionFunction = i.next().getFoodProduction();
			if (foodProductionFunction.getTechLevel() > highestTechLevel)
				highestTechLevel = foodProductionFunction.getTechLevel();
		}

		return highestTechLevel;
	}

	/**
	 * Gets a good for a food production process item.
	 *
	 * @param item the food production process item.
	 * @return good
	 * @throws Exception if error determining good.
	 */
	public static Good getGood(FoodProductionProcessItem item) {
		Good result = null;
		if (ItemType.AMOUNT_RESOURCE == item.getType()) {
			result = GoodsUtil.getGood(ResourceUtil.findAmountResource(item.getName()).getID());
		} else if (ItemType.PART == item.getType()) {
			result = GoodsUtil.getGood(ItemResourceUtil.findItemResource(item.getName()).getID());
		} else if (ItemType.EQUIPMENT == item.getType()) {
			result = GoodsUtil.getEquipmentGood(EquipmentType.convertName2Enum(item.getName()));
		} else if (ItemType.BIN == item.getType()) {
			result = GoodsUtil.getBinGood(BinType.convertName2Enum(item.getName()));
		}

		return result;
	}

	/**
	 * Gets the mass for a food production process item.
	 *
	 * @param item the food production process item.
	 * @return mass (kg).
	 * @throws Exception if error determining the mass.
	 */
	public static double getMass(FoodProductionProcessItem item) {
		double mass = 0D;

		if (ItemType.AMOUNT_RESOURCE == item.getType()) {
			mass = item.getAmount();
		} else if (ItemType.PART == item.getType()) {
			mass = item.getAmount() * ((Part) ItemResourceUtil.findItemResource(item.getName())).getMassPerItem();
		} else if (ItemType.EQUIPMENT == item.getType()) {
			double equipmentMass = EquipmentFactory.getEquipmentMass(EquipmentType.convertName2Enum(item.getName()));
			mass = item.getAmount() * equipmentMass;
		} else if (ItemType.BIN.equals(item.getType())) {
			double binMass = BinFactory.getBinMass(BinType.convertName2Enum(item.getName()));
			mass = item.getAmount() * binMass;
		}
		
		return mass;
	}

}

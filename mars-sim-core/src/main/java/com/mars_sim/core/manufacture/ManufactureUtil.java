/*
 * Mars Simulation Project
 * ManufactureUtil.java
 * @date 2024-09-09
 * @author Scott Davis
 */

package com.mars_sim.core.manufacture;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.equipment.BinFactory;
import com.mars_sim.core.equipment.BinType;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.Manufacture;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * Utility class for getting manufacturing processes.
 */
public final class ManufactureUtil {

	private static final int OUTPUT_VALUE = 10;
	private static final int PART_VALUE = 20;
	private static final int EQUIPMENT_VALUE = 40;
	private static final int BIN_VALUE = 30;
	private static final int VEHICLE_VALUE = 60;
	
	private static final SimulationConfig simulationConfig = SimulationConfig.instance();
	private static final ManufactureConfig manufactureConfig = simulationConfig.getManufactureConfiguration();
	
	/** constructor. */
	private ManufactureUtil() {
		// Static helper class
	}

	/**
	 * Gets all manufacturing processes.
	 *
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public static List<ManufactureProcessInfo> getAllManufactureProcesses() {
		return manufactureConfig.getManufactureProcessList();
	}

	/**
	 * Gets manufacturing processes within the capability of a tech level.
	 *
	 * @param techLevel the tech level.
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public static List<ManufactureProcessInfo> getManufactureProcessesForTechLevel(int techLevel) {
		return manufactureConfig.getManufactureProcessesForTechLevel(techLevel);
	}

	/**
	 * Gets manufacturing processes with given output.
	 *
	 * @param {@link String} name of desired output
	 * @return {@link List}<{@link ManufactureProcessItem}> list of processes
	 */
	public static List<ManufactureProcessInfo> getManufactureProcessesWithGivenOutput(String name) {
		return getAllManufactureProcesses().stream()
				.filter(p -> p.isOutput(name))
				.toList();
	}

	/**
	 * Gets manufacturing processes within the capability of a tech level and a
	 * skill level.
	 *
	 * @param techLevel  the tech level.
	 * @param skillLevel the skill level.
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public static List<ManufactureProcessInfo> getManufactureProcessesForTechSkillLevel(int techLevel, int skillLevel) {
		return getManufactureProcessesForTechLevel(techLevel).stream()
				.filter(s -> (s.getSkillLevelRequired() <= skillLevel))
    	        .collect(Collectors.toList());
	}

	/**
	 * Gets salvage processes info within the capability of a tech level and a skill
	 * level.
	 *
	 * @param techLevel  the tech level.
	 * @param skillLevel the skill level.
	 * @return list of salvage processes info.
	 * @throws Exception if error getting salvage processes info.
	 */
	public static List<SalvageProcessInfo> getSalvageProcessesForTechSkillLevel(int techLevel, int skillLevel) {
		return getSalvageProcessesForTechLevel(techLevel).stream()
				.filter(s -> (s.getSkillLevelRequired() <= skillLevel))
    	        .collect(Collectors.toList());
	}

	/**
	 * Gets salvage processes info within the capability of a tech level.
	 *
	 * @param techLevel the tech level.
	 * @return list of salvage processes info.
	 * @throws Exception if error get salvage processes info.
	 */
	public static List<SalvageProcessInfo> getSalvageProcessesForTechLevel(int techLevel) {
		return manufactureConfig.getSalvageProcessesForTechLevel(techLevel);
	}
	
	/**
	 * Gets the goods value of a manufacturing process at a settlement.
	 *
	 * @param process    the manufacturing process.
	 * @param settlement the settlement.
	 * @return goods value of output goods minus input goods.
	 * @throws Exception if error determining good values.
	 */
	public static double getManufactureProcessValue(ManufactureProcessInfo process, Settlement settlement) {
		int effortLevel = process.getEffortLevel();
		
		double inputsValue = 0D;
		for(var i : process.getInputList()) {
			inputsValue += getManufactureProcessItemValue(i, settlement, false);
		}

		double outputsValue = 0D;
		for(var j : process.getOutputList()) {
			outputsValue += getManufactureProcessItemValue(j, settlement, true);
		}

		// Get power value.
		double processTimeRequired = process.getProcessTimeRequired();
		double powerValue = process.getPowerRequired() * settlement.getPowerGrid().getPowerValue() * Math.log(1 + processTimeRequired);
		
		// Get the time value.

		double workTimeRequired = process.getWorkTimeRequired();
		double timeValue = Math.log(1 + processTimeRequired + workTimeRequired);
		
		// Add a small degree of randomness to the input value 
		// to avoid getting stuck at selecting the same process over and over
		double rand0 = RandomUtil.getRandomDouble(.75, 1.25);
		inputsValue = Math.round(inputsValue * rand0 * 10.0)/10.0;
		
		// Add a small degree of randomness to the output value
		// to avoid getting stuck at selecting the same process over and over
		double rand1 = RandomUtil.getRandomDouble(.75, 1.25);
		outputsValue = Math.round(rand1 * (OUTPUT_VALUE + outputsValue) * effortLevel * 10.0)/10.0;
		
		return Math.round((outputsValue - inputsValue - timeValue - powerValue) * 10.0)/10.0;
	}

	/**
	 * Gets the estimated goods value of a salvage process at a settlement.
	 *
	 * @param process    the salvage process.
	 * @param settlement the settlement.
	 * @return goods value of estimated salvaged parts minus salvaged unit.
	 * @throws Exception if error determining good values.
	 */
	public static double getSalvageProcessValue(SalvageProcessInfo process, Settlement settlement, Person salvager) {
		double result = 0D;

		var salvagedUnit = findUnitForSalvage(process, settlement);
		if (salvagedUnit != null) {
			GoodsManager goodsManager = settlement.getGoodsManager();

			double wearConditionModifier = 1D;
			if (salvagedUnit instanceof Malfunctionable salvagedMalfunctionable) {
				double wearCondition = salvagedMalfunctionable.getMalfunctionManager().getWearCondition();
				wearConditionModifier = wearCondition / 100D;
			}

			// Determine salvaged good value.
			double salvagedGoodValue;
			Good salvagedGood = null;
			if (salvagedUnit instanceof Equipment e) {
				salvagedGood = GoodsUtil.getEquipmentGood(e.getEquipmentType());
			} else if (salvagedUnit instanceof Vehicle v) {
				salvagedGood = GoodsUtil.getVehicleGood(v.getVehicleType());
			}

			if (salvagedGood != null)
				salvagedGoodValue = goodsManager.getGoodValuePoint(salvagedGood.getID());
			else
				throw new IllegalStateException("Salvaged good is null");

			salvagedGoodValue *= (wearConditionModifier * .75D) + .25D;

			// Determine total estimated parts salvaged good value.
			double totalPartsGoodValue = 0D;
			for(var partSalvage : process.getOutputList()) {
				Good partGood = GoodsUtil.getGood(ItemResourceUtil.findItemResource(partSalvage.getName()).getID());
				double partValue = goodsManager.getGoodValuePoint(partGood.getID()) * partSalvage.getAmount();
				totalPartsGoodValue += partValue;
			}

			// Modify total parts good value by item wear and salvager skill.
			int skill = salvager.getSkillManager().getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
			double valueModifier = .25D + (wearConditionModifier * .25D) + (skill * .05D);
			totalPartsGoodValue *= valueModifier;

			// Determine process value.
			result = totalPartsGoodValue - salvagedGoodValue;
		}

		return result;
	}

	/**
	 * Gets the good value of a manufacturing process item for a settlement.
	 *
	 * @param item       the manufacturing process item.
	 * @param settlement the settlement.
	 * @param isOutput   is item an output of process?
	 * @return good value.
	 * @throws Exception if error getting good value.
	 */
	public static double getManufactureProcessItemValue(ProcessItem item, Settlement settlement,
			boolean isOutput) {
		double result = 0;

		GoodsManager manager = settlement.getGoodsManager();
		switch(item.getType()) {
			case ItemType.AMOUNT_RESOURCE: {
				AmountResource ar = ResourceUtil.findAmountResource(item.getName());
				int id = ResourceUtil.findIDbyAmountResourceName(item.getName());
				double amount = item.getAmount();
				if (isOutput) {
					double remainingCapacity = settlement.getAmountResourceRemainingCapacity(ar.getID());
					if (amount > remainingCapacity) {
						amount = remainingCapacity;
					}
				}

				result = manager.getGoodValuePoint(id) * amount;
			} break;
		
			case ItemType.PART: {
				int id = ItemResourceUtil.findIDbyItemResourceName(item.getName());
				result = manager.getGoodValuePoint(id) * item.getAmount();
				if (isOutput)
					result *= PART_VALUE;
			} break;

			case ItemType.EQUIPMENT: {
				int id = EquipmentType.convertName2ID(item.getName());
				result = manager.getGoodValuePoint(id) * item.getAmount();
				if (isOutput)
					result *= EQUIPMENT_VALUE;
			} break;

			case ItemType.BIN: {
				int id = BinType.convertName2ID(item.getName());
				result = manager.getGoodValuePoint(id) * item.getAmount();
				if (isOutput)
					result *= BIN_VALUE;
			} break;

			case ItemType.VEHICLE: {
				result = manager.getGoodValuePoint(GoodsUtil.getVehicleGood(item.getName()).getID()) * item.getAmount();
				if (isOutput)
					result *= VEHICLE_VALUE;
			} break;
		}

		return result;
	}

	/**
	 * Checks to see if a manufacturing process can be started at a given
	 * manufacturing building.
	 *
	 * @param process  the manufacturing process to start.
	 * @param workshop the manufacturing building.
	 * @return true if process can be started.
	 * @throws Exception if error determining if process can be started.
	 */
	public static boolean canProcessBeQueued(ManufactureProcessInfo process, Manufacture workshop) {

		// Q: Are the numbers of 3D printers available for another processes ?
		// Check for workshop.getNumPrintersInUse()
		// NOTE: create a map to show which process has a 3D printer in use and which doesn't

		// Check to see if process tech level is above workshop tech level.
		if (workshop.getTechLevel() < process.getTechLevelRequired()) {
			return false;
		}

		Settlement settlement = workshop.getBuilding().getSettlement();

		// Check to see if process input items are available at settlement.
        if (!process.isResourcesAvailable(settlement)) {
			return false;
		}

		// Check to see if room for process output items at settlement.
		return canProcessOutputsBeStored(process, settlement);
    }
	
	/**
	 * Checks to see if a manufacturing process can be started at a given
	 * manufacturing building.
	 *
	 * @param process  the manufacturing process to start.
	 * @param workshop the manufacturing building.
	 * @return true if process can be started.
	 * @throws Exception if error determining if process can be started.
	 */
	public static boolean canProcessBeStarted(ManufactureProcessInfo process, Manufacture workshop) {
		// Check to see if this workshop can accommodate another process.
		if (workshop.getMaxProcesses() < workshop.getCurrentTotalProcesses()) {
			// NOTE: create a map to show which process has a 3D printer in use and which doesn't
			return false;
		}

		// Q: Are the numbers of 3D printers available for another processes ?
		// Check for workshop.getNumPrintersInUse()
		// NOTE: create a map to show which process has a 3D printer in use and which doesn't

		// Check to see if process tech level is above workshop tech level.
		if (workshop.getTechLevel() < process.getTechLevelRequired()) {
			return false;
		}

		Settlement settlement = workshop.getBuilding().getSettlement();

		return canProcessBeStarted(settlement, process);
	}

	public static boolean canProcessBeStarted(Settlement settlement, ManufactureProcessInfo process) {

		// Check to see if process input items are available at settlement.
        if (!process.isResourcesAvailable(settlement)) {
			return false;
		}

		// Check to see if room for process output items at settlement.
		return canProcessOutputsBeStored(process, settlement);
    }

	/**
	 * Checks to see if a salvage process can be started at a given manufacturing
	 * building.
	 *
	 * @param process  the salvage process to start.
	 * @param workshop the manufacturing building.
	 * @return true if salvage process can be started.
	 * @throws Exception if error determining if salvage process can be started.
	 */
	public static boolean canSalvageProcessBeQueued(SalvageProcessInfo process, Manufacture workshop) {

        // Check to see if process tech level is above workshop tech level.
		if (workshop.getTechLevel() < process.getTechLevelRequired()) {
			return false;
		}

		// Check to see if a salvagable unit is available at the settlement.
		return findUnitForSalvage(process, workshop.getBuilding().getSettlement()) != null;
	}


	/**
	 * Checks to see if a salvage process can be started at a given manufacturing
	 * building. This also means it coud be queued
	 *
	 * @param process  the salvage process to start.
	 * @param workshop the manufacturing building.
	 * @return true if salvage process can be started.
	 * @throws Exception if error determining if salvage process can be started.
	 */
	public static boolean canSalvageProcessBeStarted(SalvageProcessInfo process, Manufacture workshop) {
		// Check to see if this workshop can accommodate another process.
		if (workshop.getMaxProcesses() < workshop.getCurrentTotalProcesses()) {
			// NOTE: create a map to show which process has a 3D printer in use and which doesn't
			return false;
		}
	
		// To start it the same queue condition must be met
		return canSalvageProcessBeQueued(process, workshop);
	}

    /**
     * Checks if enough storage room for process outputs in an inventory.
     *
     * @param process the manufacturing process.
     * @param inv the inventory.
     * @return true if storage room.
     * @throws Exception if error determining storage room for outputs.
     */
	private static final boolean canProcessOutputsBeStored(ManufactureProcessInfo process, Settlement settlement) {

		for(var item : process.getOutputList()) {
			switch(item.getType()) {
				case ItemType.AMOUNT_RESOURCE: {
					double capacity = settlement.getAmountResourceRemainingCapacity(ResourceUtil.findIDbyAmountResourceName(item.getName()));
					if (item.getAmount() > capacity)
						return false;
				} break;

				case ItemType.PART: {
					double mass = item.getAmount() * (ItemResourceUtil.findItemResource(item.getName())).getMassPerItem();
					double capacity = settlement.getCargoCapacity();
					if (mass > capacity)
						return false;
				} break;

				case ItemType.EQUIPMENT: {
					int number = (int) item.getAmount();
					double mass = EquipmentFactory.getEquipmentMass(EquipmentType.convertName2Enum(item.getName())) * number;
					double capacity = settlement.getCargoCapacity();
					if (mass > capacity)
						return false;
				} break;

				case ItemType.BIN: {
					int number = (int) item.getAmount();
					double mass = BinFactory.getBinMass(BinType.convertName2Enum(item.getName())) * number;
					double capacity = settlement.getCargoCapacity();
					if (mass > capacity)
						return false;
				} break;

				case ItemType.VEHICLE:
					break;
			}
		}

		return true;
	}

	/**
	 * Gets the highest manufacturing tech level in a settlement.
	 *
	 * @param settlement the settlement.
	 * @return highest manufacturing tech level or -1 if no manufacturing supported
	 */
	public static int getHighestManufacturingTechLevel(Settlement settlement) {
		int highestTechLevel = -1;
		BuildingManager manager = settlement.getBuildingManager();

		for(Building building : manager.getBuildingSet(FunctionType.MANUFACTURE)) {
			Manufacture manufacturingFunction = building.getManufacture();
			if (manufacturingFunction.getTechLevel() > highestTechLevel)
				highestTechLevel = manufacturingFunction.getTechLevel();
		}

		return highestTechLevel;
	}

	/**
	 * Finds an available unit to salvage of the type needed by a salvage process.
	 *
	 * @param info       the salvage process information.
	 * @param settlement the settlement to find the unit.
	 * @return available salvagable unit, or null if none found.
	 * @throws Exception if problem finding salvagable unit.
	 */
	public static Salvagable findUnitForSalvage(SalvageProcessInfo info, Settlement settlement) {
		Salvagable result = null;
		List<Salvagable> salvagableUnits = Collections.emptyList();

		if (info.getType() == ItemType.VEHICLE) {
			VehicleType type = VehicleType.convertNameToVehicleType(info.getItemName());

			// Take any non-empty and unreserved vehicles
			salvagableUnits = settlement.getVehicleTypeUnit(type).stream()
								.filter(v -> (v.isEmpty() && !v.isReserved()))
								.filter(v2 -> (v2.getContainerUnit().equals(settlement)))
								.map(Salvagable.class::cast)
								.toList();
		} 
		
		else if (info.getType() == ItemType.EQUIPMENT) {
			EquipmentType eType = EquipmentType.convertName2Enum(info.getItemName());
			salvagableUnits = settlement.getContainerSet(eType).stream()
								.filter(v2 -> (v2.getContainerUnit().equals(settlement)))
								.map(Salvagable.class::cast)
								.toList();
		} 

		// If malfunctionable, find most worn unit.
		if (!salvagableUnits.isEmpty()) {
			// Defaut is first item found
			result = salvagableUnits.get(0);

			// Find the lowest wear
			double lowestWearCondition = Double.MAX_VALUE;
			for(var s : salvagableUnits) {
				if (s instanceof Malfunctionable m) {
					double wearCondition = m.getMalfunctionManager().getWearCondition();
					if (wearCondition < lowestWearCondition) {
						result = s;
						lowestWearCondition = wearCondition;
					}
				}
			}
		}

		return result;
	}
}

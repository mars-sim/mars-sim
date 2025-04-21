/*
 * Mars Simulation Project
 * ManufactureUtil.java
 * @date 2024-09-09
 * @author Scott Davis
 */

package com.mars_sim.core.manufacture;

import java.util.Collections;
import java.util.List;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.RandomUtil;
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
    	        .toList();
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
    	        .toList();
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
				int id = item.getId();
				double amount = item.getAmount();
				if (isOutput) {
					double remainingCapacity = settlement.getAmountResourceRemainingCapacity(id);
					if (amount > remainingCapacity) {
						amount = remainingCapacity;
					}
				}

				result = manager.getGoodValuePoint(id) * amount;
			} break;
		
			case ItemType.PART: {
				result = manager.getGoodValuePoint(item.getId()) * item.getAmount();
				if (isOutput)
					result *= PART_VALUE;
			} break;

			case ItemType.EQUIPMENT: {
				result = manager.getGoodValuePoint(item.getId()) * item.getAmount();
				if (isOutput)
					result *= EQUIPMENT_VALUE;
			} break;

			case ItemType.BIN: {
				result = manager.getGoodValuePoint(item.getId()) * item.getAmount();
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
	 * Gets the highest manufacturing tech level in a settlement.
	 *
	 * @param settlement the settlement.
	 * @return highest manufacturing tech level or -1 if no manufacturing supported
	 */
	public static int getHighestManufacturingTechLevel(Settlement settlement) {
		return settlement.getManuManager().getMaxTechLevel();
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

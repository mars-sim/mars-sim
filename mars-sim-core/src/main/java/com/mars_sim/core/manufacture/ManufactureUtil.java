/*
 * Mars Simulation Project
 * ManufactureUtil.java
 * @date 2022-09-17
 * @author Scott Davis
 */

package com.mars_sim.core.manufacture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.Unit;
import com.mars_sim.core.equipment.BinFactory;
import com.mars_sim.core.equipment.BinType;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingException;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.Manufacture;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.tools.util.RandomUtil;

/**
 * Utility class for getting manufacturing processes.
 */
public final class ManufactureUtil {

	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ManufactureUtil.class.getName());

	private static final int OUTPUT_VALUE = 10;
	private static final int PART_VALUE = 20;
	private static final int EQUIPMENT_VALUE = 40;
	private static final int BIN_VALUE = 30;
	private static final int VEHICLE_VALUE = 60;
	
	private static final SimulationConfig simulationConfig = SimulationConfig.instance();
	private static final ManufactureConfig manufactureConfig = simulationConfig.getManufactureConfiguration();
	
	
	/** constructor. */
	public ManufactureUtil() {
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
		return getAllManufactureProcesses().stream()
				.filter(s -> s.getTechLevelRequired() <= techLevel)
    	        .collect(Collectors.toList());
	}

	/**
	 * Gets manufacturing processes with given output.
	 *
	 * @param {@link String} name of desired output
	 * @return {@link List}<{@link ManufactureProcessItem}> list of processes
	 */
	public static List<ManufactureProcessInfo> getManufactureProcessesWithGivenOutput(String name) {
		List<ManufactureProcessInfo> result = new ArrayList<>();
		Iterator<ManufactureProcessInfo> i = getAllManufactureProcesses().iterator();
		while (i.hasNext()) {
			ManufactureProcessInfo process = i.next();
			for (String n : process.getOutputNames()) {
				if (name.equalsIgnoreCase(n))
					result.add(process);
			}
		}
		return result;
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
		return getAllManufactureProcesses().stream()
				.filter(s -> (s.getTechLevelRequired() <= techLevel) && (s.getSkillLevelRequired() <= skillLevel))
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
		return manufactureConfig.getSalvageList().stream()
				.filter(s -> (s.getTechLevelRequired() <= techLevel) && (s.getSkillLevelRequired() <= skillLevel))
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
		return manufactureConfig.getSalvageList().stream()
				.filter(s -> s.getTechLevelRequired() <= techLevel)
    	        .collect(Collectors.toList());
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

		Unit salvagedUnit = findUnitForSalvage(process, settlement);
		if (salvagedUnit != null) {
			GoodsManager goodsManager = settlement.getGoodsManager();

			double wearConditionModifier = 1D;
			if (salvagedUnit instanceof Malfunctionable) {
				Malfunctionable salvagedMalfunctionable = (Malfunctionable) salvagedUnit;
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
		double result;

		GoodsManager manager = settlement.getGoodsManager();

		if (item.getType() == ItemType.AMOUNT_RESOURCE) {
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
		
		} else if (item.getType() == ItemType.PART) {
            int id = ItemResourceUtil.findIDbyItemResourceName(item.getName());
			result = manager.getGoodValuePoint(id) * item.getAmount();
			if (isOutput)
				result *= PART_VALUE;
		} else if (item.getType() == ItemType.EQUIPMENT) {
			int id = EquipmentType.convertName2ID(item.getName());
			result = manager.getGoodValuePoint(id) * item.getAmount();
			if (isOutput)
				result *= EQUIPMENT_VALUE;
		} else if (item.getType() == ItemType.BIN) {
			int id = BinType.convertName2ID(item.getName());
			result = manager.getGoodValuePoint(id) * item.getAmount();
			if (isOutput)
				result *= BIN_VALUE;
		} else if (item.getType() == ItemType.VEHICLE) {
			result = manager.getGoodValuePoint(GoodsUtil.getVehicleGood(item.getName()).getID()) * item.getAmount();
			if (isOutput)
				result *= VEHICLE_VALUE;
		}

		else
			throw new IllegalStateException("Item type: " + item.getType() + " not valid.");

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
	 * Checks to see if a salvage process can be started at a given manufacturing
	 * building.
	 *
	 * @param process  the salvage process to start.
	 * @param workshop the manufacturing building.
	 * @return true if salvage process can be started.
	 * @throws Exception if error determining if salvage process can be started.
	 */
	public static boolean canSalvageProcessBeStarted(SalvageProcessInfo process, Manufacture workshop) {

        // Check to see if process tech level is above workshop tech level.
		if (workshop.getTechLevel() < process.getTechLevelRequired()) {
			return false;
		}

		// Check to see if a salvagable unit is available at the settlement.
		if (findUnitForSalvage(process, workshop.getBuilding().getSettlement()) == null) {
			return false;
		}

		return true;
	}

	/**
	 * Checks if process inputs are available in an inventory.
	 *
	 * @param process the manufacturing process.
	 * @param inv     the inventory.
	 * @return true if process inputs are available.
	 * @throws Exception if error determining if process inputs are available.
	 */
	private static boolean areProcessInputsAvailable(ManufactureProcessInfo process, Settlement settlement) {
		boolean result = true;

		Iterator<ProcessItem> i = process.getInputList().iterator();
		while (result && i.hasNext()) {
			ProcessItem item = i.next();
			if (ItemType.AMOUNT_RESOURCE.equals(item.getType())) {
				int id = ResourceUtil.findIDbyAmountResourceName(item.getName());
				result = (settlement.getAmountResourceStored(id) >= item.getAmount());
			} else if (ItemType.PART.equals(item.getType())) {
				int id = ItemResourceUtil.findIDbyItemResourceName(item.getName());
				result = (settlement.getItemResourceStored(id) >= (int) item.getAmount());
				if (!result)
					return false;
			} else
				throw new IllegalStateException("Manufacture process input: " + item.getType() + " not a valid type.");
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
	private static final boolean canProcessOutputsBeStored(ManufactureProcessInfo process, Settlement settlement) {

		for(var item : process.getOutputList()) {
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
				logger.severe(settlement, "ManufactureUtil.addProcess(): output: " +
					item.getType() + " not a valid type.");
		}

		return true;
	}

	/**
	 * Checks if settlement has buildings with manufacture function.
	 *
	 * @param settlement the settlement.
	 * @return true if buildings with manufacture function.
	 * @throws BuildingException if error checking for manufacturing buildings.
	 */
	public static boolean doesSettlementHaveManufacturing(Settlement settlement) {
		return (!settlement.getBuildingManager().getBuildingSet(FunctionType.MANUFACTURE).isEmpty());
	}

	/**
	 * Gets the highest manufacturing tech level in a settlement.
	 *
	 * @param settlement the settlement.
	 * @return highest manufacturing tech level.
	 * @throws BuildingException if error determining highest tech level.
	 */
	public static int getHighestManufacturingTechLevel(Settlement settlement) {
		int highestTechLevel = 0;
		BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> i = manager.getBuildingSet(FunctionType.MANUFACTURE).iterator();
		while (i.hasNext()) {
			Building building = i.next();
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
	public static Unit findUnitForSalvage(SalvageProcessInfo info, Settlement settlement) {
		Unit result = null;
		Collection<? extends Unit> salvagableUnits = new ArrayList<>();

		if (info.getType() == ItemType.VEHICLE) {
			if (LightUtilityVehicle.NAME.equalsIgnoreCase(info.getItemName())) {
				salvagableUnits = settlement.getVehicleTypeList(VehicleType.LUV);
			} else {
				VehicleType type = VehicleType.convertNameToVehicleType(info.getItemName());
				salvagableUnits = settlement.getVehicleTypeList(type);
			}

			// Remove any reserved vehicles.
			Iterator<? extends Unit> i = salvagableUnits.iterator();
			while (i.hasNext()) {
				Vehicle vehicle = (Vehicle) i.next();
				boolean isEmpty = vehicle.isEmpty();
				if (vehicle.isReserved() || !isEmpty)
					i.remove();
			}
		} 
		
		else if (info.getType() == ItemType.EQUIPMENT) {
			EquipmentType eType = EquipmentType.convertName2Enum(info.getItemName());
			salvagableUnits = settlement.getContainerSet(eType);
		} 

		// Make sure container unit is settlement.
		Iterator<? extends Unit> i = salvagableUnits.iterator();
		while (i.hasNext()) {
			if (i.next().getContainerUnit() != settlement)
				i.remove();
		}

		// If malfunctionable, find most worn unit.
		if (!salvagableUnits.isEmpty()) {
			Unit firstUnit = (Unit) salvagableUnits.toArray()[0];
			if (firstUnit instanceof Malfunctionable) {
				Unit mostWorn = null;
				double lowestWearCondition = Double.MAX_VALUE;
				Iterator<? extends Unit> k = salvagableUnits.iterator();
				while (k.hasNext()) {
					Unit unit = k.next();
					Malfunctionable malfunctionable = (Malfunctionable) unit;
					double wearCondition = malfunctionable.getMalfunctionManager().getWearCondition();
					if (wearCondition < lowestWearCondition) {
						mostWorn = unit;
						lowestWearCondition = wearCondition;
					}
				}
				result = mostWorn;
			} else
				result = firstUnit;
		}

		return result;
	}
}

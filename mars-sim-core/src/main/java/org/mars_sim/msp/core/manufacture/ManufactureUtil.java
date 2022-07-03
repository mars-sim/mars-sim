/*
 * Mars Simulation Project
 * ManufactureUtil.java
 * @date 2021-10-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.manufacture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.goods.Good;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.goods.GoodsUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Manufacture;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleConfig;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * Utility class for getting manufacturing processes.
 */
public final class ManufactureUtil {

	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ManufactureUtil.class.getName());

	private static final SimulationConfig simulationConfig = SimulationConfig.instance();
	private static final ManufactureConfig manufactureConfig = simulationConfig.getManufactureConfiguration();
	private static final VehicleConfig vehicleConfig = simulationConfig.getVehicleConfiguration();

	/** constructor. */
	public ManufactureUtil() {}

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
	 * Gives back an alphabetically ordered map of all manufacturing processes.
	 *
	 * @return {@link TreeMap}<{@link String},{@link ManufactureProcessInfo}>
	 */
	public static TreeMap<String, ManufactureProcessInfo> getAllManufactureProcessesMap() {
		TreeMap<String, ManufactureProcessInfo> map = new TreeMap<>();
		for (ManufactureProcessInfo item : getAllManufactureProcesses()) {
			map.put(item.getName(), item);
		}
		return map;
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
	 * Gets manufacturing processes with given input.
	 *
	 * @param name {@link String} desired input
	 * @return {@link List}<{@link ManufactureProcessItem}> list of processes
	 */
	public static List<ManufactureProcessInfo> getManufactureProcessesWithGivenInput(String name) {
		List<ManufactureProcessInfo> result = new ArrayList<>();
		Iterator<ManufactureProcessInfo> i = getAllManufactureProcesses().iterator();
		while (i.hasNext()) {
			ManufactureProcessInfo process = i.next();
			for (String n : process.getInputNames()) {
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

		double inputsValue = 0D;
		Iterator<ManufactureProcessItem> i = process.getInputList().iterator();
		while (i.hasNext())
			inputsValue += getManufactureProcessItemValue(i.next(), settlement, false);

		double outputsValue = 0D;
		Iterator<ManufactureProcessItem> j = process.getOutputList().iterator();
		while (j.hasNext())
			outputsValue += getManufactureProcessItemValue(j.next(), settlement, true);

		// Subtract power value.
		double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsClock.HOURS_PER_MILLISOL;
		double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

		return outputsValue - inputsValue - powerValue;
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
			double salvagedGoodValue = 0D;
			Good salvagedGood = null;
			if (salvagedUnit instanceof Equipment) {
				salvagedGood = GoodsUtil.getEquipmentGood(((Equipment) salvagedUnit).getEquipmentType());
			} else if (salvagedUnit instanceof Vehicle) {
				salvagedGood = GoodsUtil.getVehicleGood(salvagedUnit.getDescription());
			}

			if (salvagedGood != null)
				salvagedGoodValue = goodsManager.getGoodValuePerItem(salvagedGood.getID());
			else
				throw new IllegalStateException("Salvaged good is null");

			salvagedGoodValue *= (wearConditionModifier * .75D) + .25D;

			// Determine total estimated parts salvaged good value.
			double totalPartsGoodValue = 0D;
			Iterator<PartSalvage> i = process.getPartSalvageList().iterator();
			while (i.hasNext()) {
				PartSalvage partSalvage = i.next();
				Good partGood = GoodsUtil.getGood(ItemResourceUtil.findItemResource(partSalvage.getName()).getID());
				double partValue = goodsManager.getGoodValuePerItem(partGood.getID()) * partSalvage.getNumber();
				totalPartsGoodValue += partValue;
			}

			// Modify total parts good value by item wear and salvager skill.
			int skill = salvager.getSkillManager().getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
			double valueModifier = .25D + (wearConditionModifier * .25D) + ((double) skill * .05D);
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
	public static double getManufactureProcessItemValue(ManufactureProcessItem item, Settlement settlement,
			boolean isOutput) {
		double result = 0D;

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

			result = manager.getGoodValuePerItem(id) * amount;
		}

		else if (item.getType() == ItemType.PART) {
            int id = ItemResourceUtil.findIDbyItemResourceName(item.getName());
			result = manager.getGoodValuePerItem(id) * item.getAmount();
		}

		else if (item.getType() == ItemType.EQUIPMENT) {
			int id = EquipmentType.convertName2ID(item.getName());
			result = manager.getGoodValuePerItem(id) * item.getAmount();
		}

		else if (item.getType() == ItemType.VEHICLE) {
			Good good = GoodsUtil.getVehicleGood(item.getName());
			result = manager.getGoodValuePerItem(good.getID()) * item.getAmount();
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
		if (workshop.getMaxProcesses() < workshop.getCurrentProcesses()) {
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

		Iterator<ManufactureProcessItem> i = process.getInputList().iterator();
		while (result && i.hasNext()) {
			ManufactureProcessItem item = i.next();
			if (ItemType.AMOUNT_RESOURCE.equals(item.getType())) {
				int id = ResourceUtil.findIDbyAmountResourceName(item.getName());
				result = (settlement.getAmountResourceStored(id) >= item.getAmount());
				// Add demand tracking
			} else if (ItemType.PART.equals(item.getType())) {
				int id = ItemResourceUtil.findIDbyItemResourceName(item.getName());
				result = (settlement.getItemResourceStored(id) >= (int) item.getAmount());
				// Add tracking demand
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

		Iterator<ManufactureProcessItem> j = process.getOutputList().iterator();
		while (j.hasNext()) {
			ManufactureProcessItem item = j.next();
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

			else if (ItemType.VEHICLE == item.getType()) {
				// Vehicles are stored outside a settlement.
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
		return (settlement.getBuildingManager().getBuildings(FunctionType.MANUFACTURE).size() > 0);
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
		Iterator<Building> i = manager.getBuildings(FunctionType.MANUFACTURE).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Manufacture manufacturingFunction = building.getManufacture();
			if (manufacturingFunction.getTechLevel() > highestTechLevel)
				highestTechLevel = manufacturingFunction.getTechLevel();
		}

		return highestTechLevel;
	}


	/**
	 * Gets the mass for a manufacturing process item.
	 *
	 * @param item the manufacturing process item.
	 * @return mass (kg).
	 * @throws Exception if error determining the mass.
	 */
	public static double getMass(ManufactureProcessItem item) {
		double mass = 0D;

		if (ItemType.AMOUNT_RESOURCE.equals(item.getType())) {
			mass = item.getAmount();
		} else if (ItemType.PART.equals(item.getType())) {
			mass = item.getAmount() * ItemResourceUtil.findItemResource(item.getName()).getMassPerItem();
		} else if (ItemType.EQUIPMENT.equals(item.getType())) {
			double equipmentMass = EquipmentFactory.getEquipmentMass(EquipmentType.convertName2Enum(item.getName()));
			mass = item.getAmount() * equipmentMass;
		} else if (ItemType.VEHICLE.equals(item.getType())) {
			mass = item.getAmount() * vehicleConfig.getVehicleSpec(item.getName()).getEmptyMass();
		}

		return mass;
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

		if (info.getType().equalsIgnoreCase("vehicle")) {
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
		} else if (info.getType().equalsIgnoreCase("equipment")) {
			EquipmentType eType = EquipmentType.convertName2Enum(info.getItemName());
			salvagableUnits = settlement.getEquipmentTypeList(eType);
		}

		// Make sure container unit is settlement.
		Iterator<? extends Unit> i = salvagableUnits.iterator();
		while (i.hasNext()) {
			if (i.next().getContainerUnit() != settlement)
				i.remove();
		}

		// Make sure unit's inventory is empty.
//		Iterator<? extends Unit> j = salvagableUnits.iterator();
//		while (j.hasNext()) {
//			if (!j.next().getInventory().isEmpty(false))
//				j.remove();
//		}

		// If malfunctionable, find most worn unit.
		if (salvagableUnits.size() > 0) {
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

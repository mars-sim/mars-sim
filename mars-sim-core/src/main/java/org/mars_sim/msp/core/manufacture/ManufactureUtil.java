/**
 * Mars Simulation Project
 * ManufactureUtil.java
 * @version 3.1.0 2018-06-19
 * @author Scott Davis
 */

package org.mars_sim.msp.core.manufacture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Manufacture;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleConfig;

/**
 * Utility class for getting manufacturing processes.
 */
public final class ManufactureUtil {

	private static SimulationConfig simulationConfig = SimulationConfig.instance();
	private static ManufactureConfig manufactureConfig = simulationConfig.getManufactureConfiguration();
	private static VehicleConfig vehicleConfig = simulationConfig.getVehicleConfiguration();
	
//    private static ItemResource printerItem;
	public final static int printerID = ItemResourceUtil.printerID;

	/** Private constructor. */
	public ManufactureUtil() {
		// printerItem =
		// ItemResource.findItemResource(Manufacture.LASER_SINTERING_3D_PRINTER);
	}

	/**
	 * Gets all manufacturing processes.
	 * 
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public static List<ManufactureProcessInfo> getAllManufactureProcesses() {
		return ManufactureConfig.getManufactureProcessList();
	}

	/**
	 * gives back an alphabetically ordered map of all manufacturing processes.
	 * 
	 * @return {@link TreeMap}<{@link String},{@link ManufactureProcessInfo}>
	 */
	public static TreeMap<String, ManufactureProcessInfo> getAllManufactureProcessesMap() {
		TreeMap<String, ManufactureProcessInfo> map = new TreeMap<String, ManufactureProcessInfo>();
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
		List<ManufactureProcessInfo> result = new ArrayList<ManufactureProcessInfo>();
		Iterator<ManufactureProcessInfo> i = ManufactureConfig.getManufactureProcessList().iterator();
		while (i.hasNext()) {
			ManufactureProcessInfo process = i.next();
			if (process.getTechLevelRequired() <= techLevel)
				result.add(process);
		}

		return result;
	}

	/**
	 * gets manufacturing processes with given output.
	 * 
	 * @param {@link String} name of desired output
	 * @return {@link List}<{@link ManufactureProcessItem}> list of processes
	 */
	public static List<ManufactureProcessInfo> getManufactureProcessesWithGivenOutput(String name) {
		List<ManufactureProcessInfo> result = new ArrayList<ManufactureProcessInfo>();
		Iterator<ManufactureProcessInfo> i = ManufactureConfig.getManufactureProcessList().iterator();
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
		List<ManufactureProcessInfo> result = new ArrayList<ManufactureProcessInfo>();
		Iterator<ManufactureProcessInfo> i = ManufactureConfig.getManufactureProcessList().iterator();
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
		List<ManufactureProcessInfo> result = new ArrayList<ManufactureProcessInfo>();

		Iterator<ManufactureProcessInfo> i = ManufactureConfig.getManufactureProcessList().iterator();
		while (i.hasNext()) {
			ManufactureProcessInfo process = i.next();
			if ((process.getTechLevelRequired() <= techLevel) && (process.getSkillLevelRequired() <= skillLevel))
				result.add(process);
		}

		return result;
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
		List<SalvageProcessInfo> result = new ArrayList<SalvageProcessInfo>();
		Iterator<SalvageProcessInfo> i = manufactureConfig.getSalvageList()
				.iterator();
		while (i.hasNext()) {
			SalvageProcessInfo process = i.next();
			if ((process.getTechLevelRequired() <= techLevel) && (process.getSkillLevelRequired() <= skillLevel))
				result.add(process);
		}

		return result;
	}

	/**
	 * Gets salvage processes info within the capability of a tech level.
	 * 
	 * @param techLevel the tech level.
	 * @return list of salvage processes info.
	 * @throws Exception if error get salvage processes info.
	 */
	public static List<SalvageProcessInfo> getSalvageProcessesForTechLevel(int techLevel) {
		List<SalvageProcessInfo> result = new ArrayList<SalvageProcessInfo>();
		Iterator<SalvageProcessInfo> i = manufactureConfig.getSalvageList()
				.iterator();
		while (i.hasNext()) {
			SalvageProcessInfo process = i.next();
			if (process.getTechLevelRequired() <= techLevel)
				result.add(process);
		}

		return result;
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
//		double hoursInMillisol = MarsClock.convertMillisolsToSeconds(1D) / 60D / 60D;
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
				salvagedGood = GoodsUtil.getEquipmentGood(salvagedUnit.getClass());
			} else if (salvagedUnit instanceof Vehicle) {
				salvagedGood = GoodsUtil.getVehicleGood(salvagedUnit.getDescription());
			}

			if (salvagedGood != null)
				salvagedGoodValue = goodsManager.getGoodValuePerItem(salvagedGood);
			else
				throw new IllegalStateException("Salvaged good is null");

			salvagedGoodValue *= (wearConditionModifier * .75D) + .25D;

			// Determine total estimated parts salvaged good value.
			double totalPartsGoodValue = 0D;
			Iterator<PartSalvage> i = process.getPartSalvageList().iterator();
			while (i.hasNext()) {
				PartSalvage partSalvage = i.next();
				Good partGood = GoodsUtil.getResourceGood(ItemResourceUtil.findItemResource(partSalvage.getName()));
				double partValue = goodsManager.getGoodValuePerItem(partGood) * partSalvage.getNumber();
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

		if (item.getType().equals(ItemType.AMOUNT_RESOURCE)) {
			AmountResource ar = ResourceUtil.findAmountResource(item.getName());
//            int id = ResourceUtil.findIDbyAmountResourceName(item.getName());
			double amount = item.getAmount();
			if (isOutput) {
				double remainingCapacity = settlement.getInventory().getAmountResourceRemainingCapacity(
						ar, true, false);
				if (amount > remainingCapacity) {
					amount = remainingCapacity;
				}
			}
			Good good = GoodsUtil.getResourceGood(ar);
			result = manager.getGoodValuePerItem(good) * amount;
		} 
		
		else if (item.getType().equals(ItemType.PART)) {
//            ItemResource ir = ItemResourceUtil.findItemResource(item.getName());
//            int id = ItemResourceUtil.findIDbyItemResourceName(item.getName());
			Good good = GoodsUtil.getResourceGood(ItemResourceUtil.findItemResource(item.getName()));
			result = manager.getGoodValuePerItem(good) * item.getAmount();
		} 
		
		else if (item.getType().equals(ItemType.EQUIPMENT)) {
//			Class<? extends Equipment> equipmentClass = EquipmentFactory.getEquipmentClass(item.getName());
			Good good = GoodsUtil.getEquipmentGood(EquipmentFactory.getEquipmentClass(item.getName()));
			result = manager.getGoodValuePerItem(good) * item.getAmount();
		} 
		
		else if (item.getType().equals(ItemType.VEHICLE)) {
			result = manager.getGoodValuePerItem(GoodsUtil.getVehicleGood(item.getName())) * item.getAmount();
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
		// settlement's inventory
		Inventory inv = workshop.getBuilding().getInventory();

		// Check to see if workshop is full of processes.
		if (workshop.getTotalProcessNumber() >= workshop.getSupportingProcesses()) {
			return false;
		}

		// Check to see if process tech level is above workshop tech level.
		else if (workshop.getTechLevel() < process.getTechLevelRequired()) {
			return false;
		}

		// Check to see if there is an available printer in this building
		else if (!isAn3DPrinterAvailable(workshop)) {
			return false;
		}

		// Check to see if process input items are available at settlement.
		else if (!areProcessInputsAvailable(process, inv)) {
			return false;
		}

		// Check to see if room for process output items at settlement.
		// if (!canProcessOutputsBeStored(process, inv)) result = false;
		else
			return true;
	}

	/**
	 * Check to see if there is an available printer in this building
	 * 
	 * @param workshop
	 * @return true if there is an available 3D Printer.
	 */
	public static synchronized boolean isAn3DPrinterAvailable(Manufacture workshop) {

		if (workshop.getSupportingProcesses() > 0)
			return true;
		else
			return false;

		// TODO: rework checking for the printer ?
//    	boolean result = false;
//    	int inBldg = 0;
//    	Building building = workshop.getBuilding();
//        //System.out.println("ManufactureUtil : starting isAn3DPrinterAvailable()");
//
//        //if (workshop.getNumPrinterInUse() == 0)
//        //	workshop.set3DPrinterLocation(building);
//
//    	if (building.getBuildingInventory() != null) {
//	        Inventory b_inv = building.getBuildingInventory();
//
//	        if (b_inv.hasItemResource(printerItem))
//		        if (b_inv.getItemResourceNum(printerItem)>0) {
//		        	inBldg = b_inv.getItemResourceNum(printerItem);
//
//			        int inUse = workshop.getNumPrinterInUse();
//			        //System.out.println("ManufactureUtil.  " + inBldg + " : inBldg    " + inUse + " : inUse ");
//
//			        if (inBldg > inUse) {
//			        	result = true;
//			        	//System.out.println("ManufactureUtil.java isAn3DPrinterAvailable() : Yes");
//			        }
//		        }
//	        // TODO: check if one of them is not malfunction or down for maintenance
//    	}
//        //System.out.println("ManufactureUtil : isAn3DPrinterAvailable() : "+ result);
//        return result;
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
		boolean result = true;

		// Check to see if workshop is full of processes.
		if (workshop.getTotalProcessNumber() >= workshop.getSupportingProcesses())
			result = false;

		// Check to see if process tech level is above workshop tech level.
		if (workshop.getTechLevel() < process.getTechLevelRequired())
			result = false;

		// Check to see if a salvagable unit is available at the settlement.
		Settlement settlement = workshop.getBuilding().getSettlement();
		if (findUnitForSalvage(process, settlement) == null)
			result = false;

		return result;
	}

	/**
	 * Checks if process inputs are available in an inventory.
	 * 
	 * @param process the manufacturing process.
	 * @param inv     the inventory.
	 * @return true if process inputs are available.
	 * @throws Exception if error determining if process inputs are available.
	 */
	private static boolean areProcessInputsAvailable(ManufactureProcessInfo process, Inventory inv) {
		boolean result = true;

		Iterator<ManufactureProcessItem> i = process.getInputList().iterator();
		while (result && i.hasNext()) {
			ManufactureProcessItem item = i.next();
			if (ItemType.AMOUNT_RESOURCE.equals(item.getType())) {
//                AmountResource resource = ResourceUtil.findAmountResource(item.getName());
				int id = ResourceUtil.findIDbyAmountResourceName(item.getName());
				result = (inv.getAmountResourceStored(id, false) >= item.getAmount());
				// Add demand tracking
				inv.addAmountDemandTotalRequest(id, item.getAmount());
			} else if (ItemType.PART.equals(item.getType())) {
				int id = ItemResourceUtil.findIDbyItemResourceName(item.getName());
				result = (inv.getItemResourceNum(id) >= (int) item.getAmount());
				// Add tracking demand
				inv.addItemDemandTotalRequest(id, (int) item.getAmount());
			} else
				throw new IllegalStateException("Manufacture process input: " + item.getType() + " not a valid type.");
		}

		return result;
	}

//    /**
//     * Checks if enough storage room for process outputs in an inventory.
//     * @param process the manufacturing process.
//     * @param inv the inventory.
//     * @return true if storage room.
//     * @throws Exception if error determining storage room for outputs.
//     */
//	private static final boolean canProcessOutputsBeStored(ManufactureProcessInfo process, Inventory inv)
//			{
//		boolean result = true;
//
//		Iterator<ManufactureProcessItem> j = process.getOutputList().iterator();
//		while (j.hasNext()) {
//			ManufactureProcessItem item = j.next();
//			if (ManufactureProcessItem.AMOUNT_RESOURCE.equalsIgnoreCase(item.getType())) {
//				AmountResource resource = ResourceUtil.findAmountResource(item.getName());
//				double capacity = inv.getAmountResourceRemainingCapacity(resource, true);
//				if (item.getAmount() > capacity) result = false;
//			}
//			else if (ManufactureProcessItem.PART.equalsIgnoreCase(item.getType())) {
//				Part part = (Part) ItemResource.findItemResource(item.getName());
//				double mass = item.getAmount() * part.getMassPerItem();
//				double capacity = inv.getGeneralCapacity();
//				if (mass > capacity) result = false;
//			}
//			else if (ManufactureProcessItem.EQUIPMENT.equalsIgnoreCase(item.getType())) {
//				String equipmentType = item.getName();
//				int number = (int) item.getAmount();
//				Equipment equipment = EquipmentFactory.getEquipment(equipmentType,
//						new Coordinates(0D, 0D), true);
//				double mass = equipment.getBaseMass() * number;
//				double capacity = inv.getGeneralCapacity();
//				if (mass > capacity) result = false;
//			}
//			else if (ManufactureProcessItem.VEHICLE.equalsIgnoreCase(item.getType())) {
//				// Vehicles are stored outside a settlement.
//			}
//			else throw new BuildingException("Manufacture.addProcess(): output: " +
//					item.getType() + " not a valid type.");
//		}
//
//		return result;
//	}

	/**
	 * Checks if settlement has buildings with manufacture function.
	 * 
	 * @param settlement the settlement.
	 * @return true if buildings with manufacture function.
	 * @throws BuildingException if error checking for manufacturing buildings.
	 */
	public static boolean doesSettlementHaveManufacturing(Settlement settlement) {
//		BuildingManager manager = settlement.getBuildingManager();
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
	 * Gets a good for a manufacture process item.
	 * 
	 * @param item the manufacture process item.
	 * @return good
	 * @throws Exception if error determining good.
	 */
	public static Good getGood(ManufactureProcessItem item) {
		Good result = null;
		if (ItemType.AMOUNT_RESOURCE.equals(item.getType())) {
			AmountResource ar = ResourceUtil.findAmountResource(item.getName());
			result = GoodsUtil.getResourceGood(ar);
			if (result == null)
				result = GoodsUtil.createResourceGood(ar);
		} 
		
		else if (ItemType.PART.equals(item.getType())) {
			ItemResource ir = ItemResourceUtil.findItemResource(item.getName());
			result = GoodsUtil.getResourceGood(ir);
			if (result == null)
				result = GoodsUtil.createResourceGood(ir);
		} 
		
		else if (ItemType.EQUIPMENT.equals(item.getType())) {
			Class<? extends Equipment> equipmentClass = EquipmentFactory.getEquipmentClass(item.getName());
			result = GoodsUtil.getEquipmentGood(equipmentClass);
			if (result == null)
				result = GoodsUtil.createEquipmentGood(equipmentClass);
		} 
		
		else if (ItemType.VEHICLE.equals(item.getType())) {
			result = GoodsUtil.getVehicleGood(item.getName());
			if (result == null)
				result = GoodsUtil.createVehicleGood(item.getName());
		}

		return result;
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
//			Part part = (Part) ItemResourceUtil.findItemResource(item.getName());
			mass = item.getAmount() * ItemResourceUtil.findItemResource(item.getName()).getMassPerItem();
		} else if (ItemType.EQUIPMENT.equals(item.getType())) {
			double equipmentMass = EquipmentFactory.getEquipmentMass(item.getName());
			mass = item.getAmount() * equipmentMass;
		} else if (ItemType.VEHICLE.equals(item.getType())) {
			mass = item.getAmount() * vehicleConfig.getEmptyMass(item.getName());
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
		Inventory inv = settlement.getInventory();
		Collection<Unit> salvagableUnits = new ArrayList<Unit>(0);

		if (info.getType().equalsIgnoreCase("vehicle")) {
			if (LightUtilityVehicle.NAME.equalsIgnoreCase(info.getItemName())) {
				salvagableUnits = inv.findAllUnitsOfClass(LightUtilityVehicle.class);
			} else {
				salvagableUnits = inv.findAllUnitsOfClass(Rover.class);

				// Remove rovers that aren't the right type.
				Iterator<Unit> i = salvagableUnits.iterator();
				while (i.hasNext()) {
					Rover rover = (Rover) i.next();
					if (!rover.getDescription().equalsIgnoreCase(info.getItemName()))
						i.remove();
				}
			}

			// Remove any reserved vehicles.
			Iterator<Unit> i = salvagableUnits.iterator();
			while (i.hasNext()) {
				Vehicle vehicle = (Vehicle) i.next();
				boolean isEmpty = vehicle.getInventory().isEmpty(false);
				if (vehicle.isReserved() || !isEmpty)
					i.remove();
			}
		} else if (info.getType().equalsIgnoreCase("equipment")) {
			Class<? extends Equipment> equipmentClass = EquipmentFactory.getEquipmentClass(info.getItemName());
			salvagableUnits = inv.findAllUnitsOfClass(equipmentClass);
		}

		// Make sure container unit is settlement.
		Iterator<Unit> i = salvagableUnits.iterator();
		while (i.hasNext()) {
			if (i.next().getContainerUnit() != settlement)
				i.remove();
		}

		// Make sure unit's inventory is empty.
		Iterator<Unit> j = salvagableUnits.iterator();
		while (j.hasNext()) {
			if (!j.next().getInventory().isEmpty(false))
				j.remove();
		}

		// If malfunctionable, find most worn unit.
		if (salvagableUnits.size() > 0) {
			Unit firstUnit = (Unit) salvagableUnits.toArray()[0];
			if (firstUnit instanceof Malfunctionable) {
				Unit mostWorn = null;
				double lowestWearCondition = Double.MAX_VALUE;
				Iterator<Unit> k = salvagableUnits.iterator();
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
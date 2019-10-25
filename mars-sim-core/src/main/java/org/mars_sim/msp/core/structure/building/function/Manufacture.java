/**
 * Mars Simulation Project
 * Manufacture.java
 * @version 3.1.0 2016-10-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.manufacture.ManufactureProcess;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.manufacture.PartSalvage;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageProcess;
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
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * A building function for manufacturing.
 */
public class Manufacture extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Manufacture.class.getName());

	private static final FunctionType FUNCTION = FunctionType.MANUFACTURE;

	private static final double PROCESS_MAX_VALUE = 100D;

	public static final String LASER_SINTERING_3D_PRINTER = ItemResourceUtil.LASER_SINTERING_3D_PRINTER;

	private static int printerID = ItemResourceUtil.printerID;
	
	// Data members.
	private int solCache = 0;
	private int techLevel;
	private int supportingProcesses;
	private int maxProcesses;
	// private boolean checkNumPrinter;

	private List<ManufactureProcess> processes;
	private List<SalvageProcess> salvages;

	private Building building;

	
	/**
	 * Constructor.
	 * 
	 * @param building the building the function is for.
	 * @throws BuildingException if error constructing function.
	 */
	public Manufacture(Building building) {
		// Use Function constructor.
		super(FUNCTION, building);

		this.building = building;

		techLevel = buildingConfig.getManufactureTechLevel(building.getBuildingType());
		maxProcesses = buildingConfig.getManufactureConcurrentProcesses(building.getBuildingType());

		// Load activity spots
		loadActivitySpots(buildingConfig.getManufactureActivitySpots(building.getBuildingType()));

		processes = new ArrayList<ManufactureProcess>();
		salvages = new ArrayList<SalvageProcess>();

		// checkNumPrinter = true;
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingName the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding, Settlement settlement) {

		double result = 0D;

		int buildingTech = buildingConfig.getManufactureTechLevel(buildingName);

		double demand = 0D;
		Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
		while (i.hasNext()) {
			demand += i.next().getSkillManager().getSkillLevel(SkillType.MATERIALS_SCIENCE);
		}

		double supply = 0D;
		int highestExistingTechLevel = 0;
		boolean removedBuilding = false;
		BuildingManager buildingManager = settlement.getBuildingManager();
		Iterator<Building> j = buildingManager.getBuildings(FUNCTION).iterator();
		while (j.hasNext()) {
			Building building = j.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				Manufacture manFunction = building.getManufacture();
				int tech = manFunction.techLevel;
				double processes = manFunction.getNumPrinterInUse();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += (tech * tech) * processes * wearModifier;

				if (tech > highestExistingTechLevel) {
					highestExistingTechLevel = tech;
				}
			}
		}

		double baseManufactureValue = demand / (supply + 1D);

		double processes = buildingConfig.getManufactureConcurrentProcesses(buildingName);
		double manufactureValue = (buildingTech * buildingTech) * processes;

		result = manufactureValue * baseManufactureValue;

		// If building has higher tech level than other buildings at settlement,
		// add difference between best manufacturing processes.
		if (buildingTech > highestExistingTechLevel) {
			double bestExistingProcessValue = 0D;
			if (highestExistingTechLevel > 0D) {
				bestExistingProcessValue = getBestManufacturingProcessValue(highestExistingTechLevel, settlement);
			}
			double bestBuildingProcessValue = getBestManufacturingProcessValue(buildingTech, settlement);
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
	 * Gets the best manufacturing process value for a given manufacturing tech
	 * level at a settlement.
	 * 
	 * @param techLevel  the manufacturing tech level.
	 * @param settlement the settlement
	 * @return best manufacturing process value.
	 */
	private static double getBestManufacturingProcessValue(int techLevel, Settlement settlement) {

		double result = 0D;

		Iterator<ManufactureProcessInfo> i = ManufactureUtil.getAllManufactureProcesses().iterator();
		while (i.hasNext()) {
			ManufactureProcessInfo process = i.next();
			if (process.getTechLevelRequired() <= techLevel) {
				double value = ManufactureUtil.getManufactureProcessValue(process, settlement);
				if (value > result) {
					result = value;
				}
			}
		}

		return result;
	}

	/**
	 * Gets the manufacturing tech level of the building.
	 * 
	 * @return tech level.
	 */
	public int getTechLevel() {
		return techLevel;
	}

	/**
	 * Gets the maximum concurrent manufacturing processes supported by the
	 * building.
	 * 
	 * @return maximum concurrent processes.
	 */
	public int getSupportingProcesses() {
		return supportingProcesses;
	}

	/**
	 * Gets the total manufacturing and salvage processes currently in this
	 * building.
	 * 
	 * @return total process number.
	 */
	public int getTotalProcessNumber() {
		return processes.size() + salvages.size();
	}

	/**
	 * Gets a list of the current manufacturing processes.
	 * 
	 * @return unmodifiable list of processes.
	 */
	public List<ManufactureProcess> getProcesses() {
		return Collections.unmodifiableList(processes);
	}

	/**
	 * Adds a new manufacturing process to the building.
	 * 
	 * @param process the new manufacturing process.
	 * @throws BuildingException if error adding process.
	 */
	public void addProcess(ManufactureProcess process) {
		if (process == null) {
			throw new IllegalArgumentException("process is null");
		}
		if (getTotalProcessNumber() >= supportingProcesses) {
			throw new IllegalStateException("No space to add new manufacturing process.");
		}
		processes.add(process);

		Inventory inv = building.getInventory();
		
		// Consume inputs.
		for (ManufactureProcessItem item : process.getInfo().getInputList()) {
			if (ItemType.AMOUNT_RESOURCE.equals(item.getType())) {
//				AmountResource resource = ResourceUtil.findAmountResource(item.getName());
				int id = ResourceUtil.findIDbyAmountResourceName(item.getName());
				inv.retrieveAmountResource(id, item.getAmount());
				// Add tracking demand
				inv.addAmountDemand(id, item.getAmount());
			} else if (ItemType.PART.equals(item.getType())) {
//				Part part = (Part) ItemResourceUtil.findItemResource(item.getName());
				int id = ItemResourceUtil.findIDbyItemResourceName(item.getName());
				inv.retrieveItemResources(id, (int) item.getAmount());
				// Add tracking demand
				inv.addItemDemand(id, (int) item.getAmount());
			} else
				// TODO: in future, add equipment here as the requirement for this process
				throw new IllegalStateException("Manufacture process input: " + item.getType() + " not a valid type.");

			// Recalculate settlement good value for input item.
			building.getSettlement().getGoodsManager().updateGoodValue(ManufactureUtil.getGood(item), false);
		}

		// Log manufacturing process starting.
		if (logger.isLoggable(Level.FINEST)) {
			// Settlement settlement = getBuilding().getBuildingManager().getSettlement();
			logger.finest(
					building + " at " + building.getSettlement() 
					+ " starting manufacturing process: " + process.getInfo().getName());
		}
	}

	/**
	 * Gets a list of the current salvage processes.
	 * 
	 * @return unmodifiable list of salvage processes.
	 */
	public List<SalvageProcess> getSalvageProcesses() {
		return Collections.unmodifiableList(salvages);
	}

	/**
	 * Adds a new salvage process to the building.
	 * 
	 * @param process the new salvage process.
	 * @throws BuildingException if error adding process.
	 */
	public void addSalvageProcess(SalvageProcess process) {
		if (process == null)
			throw new IllegalArgumentException("process is null");

		if (getTotalProcessNumber() >= supportingProcesses)
			throw new IllegalStateException("No space to add new salvage process.");

		salvages.add(process);

		// Retrieve salvaged unit from inventory and remove from unit manager.
		// Inventory inv = getBuilding().getSettlementInventory();
		Unit salvagedUnit = process.getSalvagedUnit();
		if (salvagedUnit != null) {
			// s_inv
			building.getInventory().retrieveUnit(salvagedUnit);
		} else
			throw new IllegalStateException("Salvaged unit is null");

		Settlement settlement = building.getSettlement();
		
		// Set the salvage process info for the salvaged unit.
		// Settlement settlement = getBuilding().getBuildingManager().getSettlement();
		((Salvagable) salvagedUnit).startSalvage(process.getInfo(), settlement.getIdentifier());

		// Recalculate settlement good value for salvaged unit.
		// GoodsManager goodsManager = settlement.getGoodsManager();
		Good salvagedGood = null;
		if (salvagedUnit instanceof Equipment) {
			salvagedGood = GoodsUtil.getEquipmentGood(salvagedUnit.getClass());
		} else if (salvagedUnit instanceof Vehicle) {
			salvagedGood = GoodsUtil.getVehicleGood(salvagedUnit.getDescription());
		}

		if (salvagedGood != null) {
			settlement.getGoodsManager().updateGoodValue(salvagedGood, false);
		} else
			throw new IllegalStateException("Salvaged good is null");

		// Log salvage process starting.
		if (logger.isLoggable(Level.FINEST)) {
			// Settlement stl = getBuilding().getBuildingManager().getSettlement();
			logger.finest(getBuilding() + " at " + building.getSettlement() + " starting salvage process: " + process.toString());
		}
	}

	@Override
	public double getFullPowerRequired() {
		double result = 0D;
		Iterator<ManufactureProcess> i = processes.iterator();
		while (i.hasNext()) {
			ManufactureProcess process = i.next();
			if (process.getProcessTimeRemaining() > 0D)
				result += process.getInfo().getPowerRequired();
		}
		return result;
	}

	@Override
	public double getPoweredDownPowerRequired() {
		double result = 0D;
		Iterator<ManufactureProcess> i = processes.iterator();
		while (i.hasNext()) {
			ManufactureProcess process = i.next();
			if (process.getProcessTimeRemaining() > 0D)
				result += process.getInfo().getPowerRequired();
		}
		return result;
	}

	@Override
	public void timePassing(double time) {

		checkPrinters();

		// int updatedNumPrinters = s_inv.getItemResourceNum(printerItem);
		// if (updatedNumPrinters != cacheNumPrinters) {
		// checkNumPrinter = true;
		// cacheNumPrinters = updatedNumPrinters;
		// }

		// if (checkNumPrinter) {
		// Assign where the 3D printers will go
		// checkPrinters(updatedNumPrinters);
		// }

		List<ManufactureProcess> finishedProcesses = new ArrayList<ManufactureProcess>();

		Iterator<ManufactureProcess> i = processes.iterator();
		while (i.hasNext()) {
			ManufactureProcess process = i.next();
			process.addProcessTime(time);

			if ((process.getProcessTimeRemaining() == 0D) && (process.getWorkTimeRemaining() == 0D)) {
				finishedProcesses.add(process);
			}
		}

		// End all processes that are done.
		Iterator<ManufactureProcess> j = finishedProcesses.iterator();
		while (j.hasNext()) {
			endManufacturingProcess(j.next(), false);
		}
	}

	/**
	 * Checks if manufacturing function currently requires manufacturing work.
	 * 
	 * @param skill the person's materials science skill level.
	 * @return true if manufacturing work.
	 */
	public boolean requiresManufacturingWork(int skill) {
		boolean result = false;

		if (supportingProcesses > getTotalProcessNumber())
			result = true;
		else {
			Iterator<ManufactureProcess> i = processes.iterator();
			while (i.hasNext()) {
				ManufactureProcess process = i.next();
				boolean workRequired = (process.getWorkTimeRemaining() > 0D);
				boolean skillRequired = (process.getInfo().getSkillLevelRequired() <= skill);
				if (workRequired && skillRequired)
					result = true;
			}
		}

		return result;
	}

	/**
	 * Checks if manufacturing function currently requires salvaging work.
	 * 
	 * @param skill the person's materials science skill level.
	 * @return true if manufacturing work.
	 */
	public boolean requiresSalvagingWork(int skill) {
		boolean result = false;

		if (supportingProcesses > getTotalProcessNumber())
			result = true;
		else {
			Iterator<SalvageProcess> i = salvages.iterator();
			while (i.hasNext()) {
				SalvageProcess process = i.next();
				boolean workRequired = (process.getWorkTimeRemaining() > 0D);
				boolean skillRequired = (process.getInfo().getSkillLevelRequired() <= skill);
				if (workRequired && skillRequired)
					result = true;
			}
		}

		return result;
	}

	/**
	 * Ends a manufacturing process.
	 * 
	 * @param process   the process to end.
	 * @param premature true if the process has ended prematurely.
	 * @throws BuildingException if error ending process.
	 */
	public void endManufacturingProcess(ManufactureProcess process, boolean premature) {
		Settlement settlement = building.getSettlement();
		Inventory inv = building.getInventory();
		
		if (!premature) {
			// Produce outputs.
			// WARNING : The UnitManager instance will be stale after loading from a saved
			// sim
			// It will fail to run methods in Settlement and without any warning as to why
			// that it fails.

			Iterator<ManufactureProcessItem> j = process.getInfo().getOutputList().iterator();
			while (j.hasNext()) {
				ManufactureProcessItem item = j.next();
				if (ManufactureUtil.getManufactureProcessItemValue(item, settlement, true) > 0D) {
					if (ItemType.AMOUNT_RESOURCE.equals(item.getType())) {
						// Produce amount resources.
//						AmountResource resource = ResourceUtil.findAmountResource(item.getName());
						int id = ResourceUtil.findIDbyAmountResourceName(item.getName());						
						double amount = item.getAmount();
						double capacity = inv.getAmountResourceRemainingCapacity(id, true, false);
						if (item.getAmount() > capacity) {
							double overAmount = item.getAmount() - capacity;
							logger.fine("Not enough storage capacity to store " + overAmount + " of " + item.getName()
									+ " from " + process.getInfo().getName() + " at " + settlement.getName());
							amount = capacity;
						}
						inv.storeAmountResource(id, amount, true);
						// Add tracking supply
						inv.addAmountSupply(id, amount);
						// Add to the daily output
						settlement.addOutput(id, amount, process.getTotalWorkTime());
					} 
					
					else if (ItemType.PART.equals(item.getType())) {
						// Produce parts.
						Part part = (Part) ItemResourceUtil.findItemResource(item.getName());
						int id = part.getID();//ItemResourceUtil.findIDbyItemResourceName(item.getName());
						int num = (int) item.getAmount();
						double mass = num * part.getMassPerItem();
						double capacity = inv.getGeneralCapacity();
						if (mass <= capacity) {
							inv.storeItemResources(id, num);
							// Add tracking supply
							inv.addItemSupply(id, num);
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
									settlement.getCoordinates(), false);
							equipment.setName(unitManager.getNewName(UnitType.EQUIPMENT, equipmentType, null, null));
							// Place this equipment within a settlement
//							inv.storeUnit(equipment);
//							unitManager.addUnit(equipment);
							unitManager.addEquipmentID(equipment);
							// TODO: how to add tracking supply for equipment
							// Add to the daily output
							settlement.addOutput(equipment.getIdentifier(), number, process.getTotalWorkTime());
						}
					} 
					
					else if (ItemType.VEHICLE.equals(item.getType())) {
						// Produce vehicles.
						String vehicleType = item.getName();
						int number = (int) item.getAmount();
						for (int x = 0; x < number; x++) {
							if (LightUtilityVehicle.NAME.equalsIgnoreCase(vehicleType)) {
								String name = unitManager.getNewName(UnitType.VEHICLE, LightUtilityVehicle.NAME, null, null);
								unitManager.addUnit(new LightUtilityVehicle(name, vehicleType, settlement));
							} else {
								String name = unitManager.getNewName(UnitType.VEHICLE, null, null, null);
								unitManager.addUnit(new Rover(name, vehicleType, settlement));
							}
							// Add to the daily output
							settlement.addOutput(VehicleType.convertName2ID(vehicleType), number, process.getTotalWorkTime());
						}
					} 
					
					else
						throw new IllegalStateException(
								"Manufacture.addProcess(): output: " + item.getType() + " not a valid type.");

					// Recalculate settlement good value for output item.
					settlement.getGoodsManager().updateGoodValue(ManufactureUtil.getGood(item), false);
				}
			}
		} 
		
		else {

			// Premature end of process. Return all input materials.
			// TODO: should some resources be consumed and irreversible ? 
			
			// WARNING : The UnitManager instance will be stale after loading from a saved
			// sim
			// It will fail to run methods in Settlement and without any warning as to why
			// that it fails.
//			UnitManager unitManager = Simulation.instance().getUnitManager();

			Iterator<ManufactureProcessItem> j = process.getInfo().getInputList().iterator();
			while (j.hasNext()) {
				ManufactureProcessItem item = j.next();
				if (ManufactureUtil.getManufactureProcessItemValue(item, settlement, false) > 0D) {
					if (ItemType.AMOUNT_RESOURCE.equals(item.getType())) {
						// Produce amount resources.
						AmountResource resource = ResourceUtil.findAmountResource(item.getName());
						double amount = item.getAmount();
						double capacity = inv.getAmountResourceRemainingCapacity(resource, true, false);
						if (item.getAmount() > capacity) {
							double overAmount = item.getAmount() - capacity;
							logger.severe("Not enough storage capacity to store " + overAmount + " of " + item.getName()
									+ " from " + process.getInfo().getName() + " at " + settlement.getName());
							amount = capacity;
						}
						inv.storeAmountResource(resource, amount, true);
					} 
					
					else if (ItemType.PART.equals(item.getType())) {
						// Produce parts.
						Part part = (Part) ItemResourceUtil.findItemResource(item.getName());
						int num = (int) item.getAmount();
						int id = part.getID();//ItemResourceUtil.findIDbyItemResourceName(item.getName());
						double mass = num * part.getMassPerItem();
						double capacity = inv.getGeneralCapacity();
						if (mass <= capacity) {
							inv.storeItemResources(id, num);
						}
					} 
					
					else if (ItemType.EQUIPMENT.equals(item.getType())) {
						// Produce equipment.
						String equipmentType = item.getName();
						int number = (int) item.getAmount();
						for (int x = 0; x < number; x++) {
							Equipment equipment = EquipmentFactory.createEquipment(equipmentType,
									settlement.getCoordinates(), false);
							equipment.setName(unitManager.getNewName(UnitType.EQUIPMENT, equipmentType, null, null));
//							inv.storeUnit(equipment);
						}
					} 
					
					else if (ItemType.VEHICLE.equals(item.getType())) {
						// Produce vehicles.
						String vehicleType = item.getName();
						int number = (int) item.getAmount();
						for (int x = 0; x < number; x++) {
							if (LightUtilityVehicle.NAME.equalsIgnoreCase(vehicleType)) {
								String name = unitManager.getNewName(UnitType.VEHICLE, LightUtilityVehicle.NAME, null, null);
								unitManager.addUnit(new LightUtilityVehicle(name, vehicleType, settlement));
							} else {
								String name = unitManager.getNewName(UnitType.VEHICLE, null, null, null);
								unitManager.addUnit(new Rover(name, vehicleType, settlement));
							}
						}
					} 
					
					else
						throw new IllegalStateException(
								"Manufacture.addProcess(): output: " + item.getType() + " not a valid type.");

					// Recalculate settlement good value for output item.
					settlement.getGoodsManager().updateGoodValue(ManufactureUtil.getGood(item), false);
				}
			}
		}

		processes.remove(process);

		// Untag an 3D Printer (upon the process is ended or discontinued)
		// if (numPrinterInUse >= 1)
		// numPrinterInUse--;

		// Log process ending.
		if (logger.isLoggable(Level.FINEST)) {
			// Settlement settlement = getBuilding().getBuildingManager().getSettlement();
			logger.finest(getBuilding() + " at " + settlement + " ending manufacturing process: "
					+ process.getInfo().getName());
		}
	}

	/**
	 * Ends a salvage process.
	 * 
	 * @param process   the process to end.
	 * @param premature true if process is ended prematurely.
	 * @throws BuildingException if error ending process.
	 */
	public void endSalvageProcess(SalvageProcess process, boolean premature) {
		Settlement settlement = building.getSettlement();
	
		Map<Integer, Integer> partsSalvaged = new HashMap<>(0);

		if (!premature) {
			// Produce salvaged parts.

			// Determine the salvage chance based on the wear condition of the item.
			double salvageChance = 50D;
			Unit salvagedUnit = process.getSalvagedUnit();
			if (salvagedUnit instanceof Malfunctionable) {
				Malfunctionable malfunctionable = (Malfunctionable) salvagedUnit;
				double wearCondition = malfunctionable.getMalfunctionManager().getWearCondition();
				salvageChance = (wearCondition * .25D) + 25D;
			}

			// Add the average material science skill of the salvagers.
			salvageChance += process.getAverageSkillLevel() * 5D;

			// Salvage parts.
			List<PartSalvage> partsToSalvage = process.getInfo().getPartSalvageList();
			Iterator<PartSalvage> i = partsToSalvage.iterator();
			while (i.hasNext()) {
				PartSalvage partSalvage = i.next();
				Part part = (Part) ItemResourceUtil.findItemResource(partSalvage.getName());
				int id = part.getID();

				int totalNumber = 0;
				for (int x = 0; x < partSalvage.getNumber(); x++) {
					if (RandomUtil.lessThanRandPercent(salvageChance))
						totalNumber++;
				}

				if (totalNumber > 0) {
					partsSalvaged.put(id, totalNumber);
					Inventory inv = building.getInventory();
					
					double mass = totalNumber * part.getMassPerItem();
					double capacity = inv.getGeneralCapacity();
					if (mass <= capacity)
						inv.storeItemResources(id, totalNumber);

					// Recalculate settlement good value for salvaged part.
					settlement.getGoodsManager().updateGoodValue(GoodsUtil.getResourceGood(part), false);
				}
			}
		}

		// Finish the salvage.
		((Salvagable) process.getSalvagedUnit()).getSalvageInfo().finishSalvage(partsSalvaged);

		salvages.remove(process);

		// Log salvage process ending.
		if (logger.isLoggable(Level.FINEST)) {
			// Settlement settlement = getBuilding().getBuildingManager().getSettlement();
			logger.finest(getBuilding() + " at " + settlement + " ending salvage process: " + process.toString());
		}
	}

	@Override
	public double getMaintenanceTime() {
		double result = 0D;

		// Add maintenance for tech level.
		result += techLevel * 10D;

		// Add maintenance for concurrent process capacity.
		result += supportingProcesses * 10D;

		return result;
	}

	// public void setCheckNumPrinter(boolean value) {
	// checkNumPrinter = value;
	// }

	/**
	 * Check once a sol if enough 3D printer(s) are supporting the manufacturing
	 * processes
	 */
	public void checkPrinters() {
		// Check only once a day for # of processes that are needed.
		int solElapsed = marsClock.getMissionSol();
		if (solElapsed != solCache) {
			solCache = solElapsed;
			supportingProcesses = building.getInventory().getItemResourceNum(printerID); // b_inv
			if (supportingProcesses < maxProcesses) {
				distributePrinters();
			}
//            else {
//                // push for building new 3D printers
//            }
		}
	}

	/**
	 * Takes 3D printer(s) from settlement's inventory and assigns them to this
	 * building's inventory
	 */
	public void distributePrinters() {
		Settlement settlement = building.getSettlement();
		Inventory inv = building.getInventory();
		
		int s_available = inv.getItemResourceNum(printerID);
		int s_needed = settlement.getSumOfManuProcesses();
		int surplus = s_available - s_needed;
		int b_needed = maxProcesses;

		if (surplus > 0) {
			if (surplus >= b_needed) {
				inv.retrieveItemResources(printerID, b_needed);
				// b_inv.storeItemResources(printerItem, b_needed);
				settlement.addManuProcesses(b_needed);
			} else {
				inv.retrieveItemResources(printerID, surplus);
				// b_inv.storeItemResources(printerItem, surplus);
				settlement.addManuProcesses(surplus);
			}
		}

	}

	public int getNumPrinterInUse() {
		return supportingProcesses;
	}

	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void destroy() {
		super.destroy();

		Iterator<ManufactureProcess> i = processes.iterator();
		while (i.hasNext()) {
			i.next().destroy();
		}

		Iterator<SalvageProcess> j = salvages.iterator();
		while (j.hasNext()) {
			j.next().destroy();
		}
	}

}
/*
 * Mars Simulation Project
 * Manufacture.java
 * @date 2022-07-26
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.goods.Good;
import org.mars_sim.msp.core.goods.GoodsUtil;
import org.mars_sim.msp.core.logging.SimLogger;
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
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * A building function for manufacturing.
 */
public class Manufacture extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Manufacture.class.getName());
	
	private static final int SKILL_GAP = 1;

	private static final int printerID = ItemResourceUtil.printerID;

	private static final double PROCESS_MAX_VALUE = 100D;

	private static final String CONCURRENT_PROCESSES = "concurrent-processes";

	public static final String LASER_SINTERING_3D_PRINTER = ItemResourceUtil.LASER_SINTERING_3D_PRINTER;

	// Data members.
	private int techLevel;
	private int numPrintersInUse;
	private final int numMaxConcurrentProcesses;

	private List<ManufactureProcess> processes;
	private List<SalvageProcess> salvages;

	// NOTE: create a map to show which process has a 3D printer in use and which doesn't

	/**
	 * Constructor.
	 *
	 * @param building the building the function is for.
	 * @param spec Details of teh Funciton at hand
	 * @throws BuildingException if error constructing function.
	 */
	public Manufacture(Building building, FunctionSpec spec) {
		// Use Function constructor.
		super(FunctionType.MANUFACTURE, spec, building);

		techLevel = spec.getTechLevel();
		numMaxConcurrentProcesses = spec.getIntegerProperty(CONCURRENT_PROCESSES);
		numPrintersInUse = numMaxConcurrentProcesses;

		processes = new CopyOnWriteArrayList<>();
		salvages = new CopyOnWriteArrayList<>();
	}

	/**
	 * Gets the value of the function for a named building type.
	 *
	 * @param type the building type.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String type, boolean newBuilding, Settlement settlement) {

		double result = 0D;

		FunctionSpec spec = buildingConfig.getFunctionSpec(type, FunctionType.MANUFACTURE);
		int buildingTech = spec.getTechLevel();

		double demand = 0D;
		Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
		while (i.hasNext()) {
			demand += i.next().getSkillManager().getSkillLevel(SkillType.MATERIALS_SCIENCE);
		}

		double supply = 0D;
		int highestExistingTechLevel = 0;
		boolean removedBuilding = false;
		BuildingManager buildingManager = settlement.getBuildingManager();
		Iterator<Building> j = buildingManager.getBuildings(FunctionType.MANUFACTURE).iterator();
		while (j.hasNext()) {
			Building building = j.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(type) && !removedBuilding) {
				removedBuilding = true;
			} else {
				Manufacture manFunction = building.getManufacture();
				int tech = manFunction.techLevel;
				double processes = manFunction.getNumPrintersInUse();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += (tech * tech) * processes * wearModifier;

				if (tech > highestExistingTechLevel) {
					highestExistingTechLevel = tech;
				}
			}
		}

		double baseManufactureValue = demand / (supply + 1D);

		double processes = spec.getIntegerProperty(CONCURRENT_PROCESSES);
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
	public int getMaxProcesses() {
		return numMaxConcurrentProcesses;
	}

	/**
	 * Gets the current total number of manufacturing and salvage processes happening in this
	 * building.
	 *
	 * @return current total.
	 */
	public int getCurrentTotalProcesses() {
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

		if (getCurrentTotalProcesses() >= numPrintersInUse) {
			logger.info(getBuilding().getSettlement(), 20_000,
					getBuilding()
					+ ": " + getCurrentTotalProcesses() + " concurrent processes.");
			logger.info(getBuilding().getSettlement(), 20_000,
					getBuilding()
					+ ": " + numPrintersInUse + " 3D-printer(s) installed for use."
					+ "");
			logger.info(getBuilding().getSettlement(), 20_000,
					getBuilding()
					+ ": " + (numMaxConcurrentProcesses-numPrintersInUse)
					+ " 3D-printer slot(s) available."
					+ "");
			return;
		}

		processes.add(process);

		// Consume inputs.
		for (ManufactureProcessItem item : process.getInfo().getInputList()) {
			if (ItemType.AMOUNT_RESOURCE.equals(item.getType())) {
				int id = ResourceUtil.findIDbyAmountResourceName(item.getName());
				building.getSettlement().retrieveAmountResource(id, item.getAmount());
				// Add tracking demand
			} else if (ItemType.PART.equals(item.getType())) {
				int id = ItemResourceUtil.findIDbyItemResourceName(item.getName());
				building.getSettlement().retrieveItemResource(id, (int) item.getAmount());
				// Add tracking demand
//			} else if (ItemType.EQUIPMENT.equals(item.getType())) {
//				String equipmentType = item.getName();
//				int number = (int) item.getAmount();
//				building.getSettlement().getEquipmentInventory().removeEquipment(equipmentType);
			} else 
				// Future: add equipment here as the requirement for this process
				logger.log(getBuilding().getSettlement(), Level.SEVERE, 20_000,
						getBuilding()
						+ " Manufacture process input: " + item.getType() + " not a valid type.");
		}

		// Log manufacturing process starting.
		logger.log(getBuilding().getSettlement(), Level.FINEST, 20_000,
				getBuilding()
				+ " starting manufacturing process: " + process.getInfo().getName());
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

		if (getCurrentTotalProcesses() >= numPrintersInUse)
			throw new IllegalStateException("No more space left to add new salvage process.");

		salvages.add(process);

		// Retrieve salvaged unit and remove from unit manager.
		Unit salvagedUnit = process.getSalvagedUnit();
		if (salvagedUnit != null) {
			if (salvagedUnit.getUnitType() == UnitType.EQUIPMENT) {
				building.getSettlement().removeEquipment((Equipment)salvagedUnit);
			} else if (salvagedUnit.getUnitType() == UnitType.VEHICLE) {
				building.getSettlement().removeOwnedVehicle((Vehicle)salvagedUnit);
				building.getSettlement().removeParkedVehicle((Vehicle)salvagedUnit);
			} else if (salvagedUnit.getUnitType() == UnitType.ROBOT) {
				building.getSettlement().removeOwnedRobot((Robot)salvagedUnit);
			}
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
			salvagedGood = GoodsUtil.getEquipmentGood(((Equipment) salvagedUnit).getEquipmentType());
		} else if (salvagedUnit instanceof Vehicle) {
			salvagedGood = GoodsUtil.getVehicleGood(salvagedUnit.getDescription());
		}

		if (salvagedGood != null) {
//			settlement.getGoodsManager().updateGoodValue(salvagedGood, false);
		} else
			throw new IllegalStateException("Salvaged good is null");

		// Log salvage process starting.
		logger.log(getBuilding().getSettlement(), Level.FINEST, 20_000,
				getBuilding() + " starting salvage process: " + process.toString());
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
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
			// Check once a sol only
			checkPrinters(pulse);

			List<ManufactureProcess> finishedProcesses = new CopyOnWriteArrayList<>();

			Iterator<ManufactureProcess> i = processes.iterator();
			while (i.hasNext()) {
				ManufactureProcess process = i.next();
				process.addProcessTime(pulse.getElapsed());

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
		return valid;
	}

	/**
	 * Checks if manufacturing function currently requires manufacturing work.
	 *
	 * @param skill the person's materials science skill level.
	 * @return true if manufacturing work.
	 */
	public boolean requiresWork(int skill) {
		boolean result = false;

		if (numPrintersInUse > getCurrentTotalProcesses())
			result = true;
		else {
			Iterator<ManufactureProcess> i = processes.iterator();
			while (i.hasNext()) {
				ManufactureProcess process = i.next();
				boolean workRequired = (process.getWorkTimeRemaining() > 0D);
				// Allow a low material science skill person to have access to do the next level skill process
				boolean skillRequired = (process.getInfo().getSkillLevelRequired() <= skill + SKILL_GAP);
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

		if (numPrintersInUse > getCurrentTotalProcesses())
			result = true;
		else {
			Iterator<SalvageProcess> i = salvages.iterator();
			while (i.hasNext()) {
				SalvageProcess process = i.next();
				boolean workRequired = (process.getWorkTimeRemaining() > 0D);
				// Allow a low material science skill person to have access to do the next level skill process
				boolean skillRequired = (process.getInfo().getSkillLevelRequired() <= skill + 1);
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

		if (!premature) {
			// Produce outputs.
			Iterator<ManufactureProcessItem> j = process.getInfo().getOutputList().iterator();
			while (j.hasNext()) {
				ManufactureProcessItem item = j.next();
				if (ManufactureUtil.getManufactureProcessItemValue(item, settlement, true) > 0D) {
					if (ItemType.AMOUNT_RESOURCE.equals(item.getType())) {
						// Produce amount resources.
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
						// Add to the daily output
						settlement.addOutput(id, amount, process.getTotalWorkTime());
					}

					else if (ItemType.PART.equals(item.getType())) {
						// Produce parts.
						Part part = (Part) ItemResourceUtil.findItemResource(item.getName());
						int id = part.getID();
						int num = (int) item.getAmount();
						double mass = num * part.getMassPerItem();
						double capacity = settlement.getCargoCapacity();
						if (mass <= capacity) {
							settlement.storeItemResource(id, num);
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

					else if (ItemType.VEHICLE.equals(item.getType())) {
						// Produce vehicles.
						String vehicleType = item.getName();
						ReportingAuthority sponsor = settlement.getSponsor();
						int number = (int) item.getAmount();
						for (int x = 0; x < number; x++) {
							String name = Vehicle.generateName(vehicleType, sponsor);
							if (LightUtilityVehicle.NAME.equalsIgnoreCase(vehicleType)) {
								unitManager.addUnit(new LightUtilityVehicle(name, vehicleType, settlement));
							}
							else if (VehicleType.DELIVERY_DRONE.getName().equalsIgnoreCase(vehicleType)) {
								unitManager.addUnit(new Drone(name, vehicleType, settlement));
							}
							else {
								unitManager.addUnit(new Rover(name, vehicleType, settlement));
							}
							// Add to the daily output
							settlement.addOutput(VehicleType.convertName2ID(vehicleType), number, process.getTotalWorkTime());
						}
					}

					else
						throw new IllegalStateException(
								"Manufacture.addProcess(): output: " + item.getType() + " not a valid type.");

					Good good = GoodsUtil.getGood(item.getName());
					if (good == null) {
						logger.severe(item.getName() + " is not a good.");
					}
					else
						// Recalculate settlement good value for the output item.
						settlement.getGoodsManager().determineGoodValue(good);
				}
			}
		}

		else {

			// Premature end of process. Return all input materials.
			// Note: should some resources be consumed and irreversible ?
			Iterator<ManufactureProcessItem> j = process.getInfo().getInputList().iterator();
			while (j.hasNext()) {
				ManufactureProcessItem item = j.next();
				if (ManufactureUtil.getManufactureProcessItemValue(item, settlement, false) > 0D) {
					if (ItemType.AMOUNT_RESOURCE.equals(item.getType())) {
						// Produce amount resources.
						AmountResource resource = ResourceUtil.findAmountResource(item.getName());
						double amount = item.getAmount();
						double capacity = settlement.getAmountResourceRemainingCapacity(resource.getID());
						if (item.getAmount() > capacity) {
							double overAmount = item.getAmount() - capacity;
							logger.severe("Premature ending '" +  process.getInfo().getName() + "'. "
									+ "Not enough storage capacity to store " + overAmount + " of " + item.getName()
									+ " at " + settlement.getName());
							amount = capacity;
						}
						settlement.storeAmountResource(resource.getID(), amount);
					}

					else if (ItemType.PART.equals(item.getType())) {
						// Produce parts.
						Part part = (Part) ItemResourceUtil.findItemResource(item.getName());
						int num = (int) item.getAmount();
						int id = part.getID();
						double mass = num * part.getMassPerItem();
						double capacity = settlement.getCargoCapacity();
						if (mass <= capacity) {
							settlement.storeItemResource(id, num);
						}
					}

					else if (ItemType.EQUIPMENT.equals(item.getType())) {
						// Produce equipment.
						String equipmentType = item.getName();
						int number = (int) item.getAmount();
						for (int x = 0; x < number; x++) {
							Equipment equipment = EquipmentFactory.createEquipment(equipmentType,
									settlement);
							unitManager.addUnit(equipment);
						}
					}

					else if (ItemType.VEHICLE.equals(item.getType())) {
						// Produce vehicles.
						String vehicleType = item.getName();
						int number = (int) item.getAmount();
						 ReportingAuthority sponsor = settlement.getSponsor();
						for (int x = 0; x < number; x++) {
							String name = Vehicle.generateName(vehicleType, sponsor);
							if (LightUtilityVehicle.NAME.equalsIgnoreCase(vehicleType)) {
								unitManager.addUnit(new LightUtilityVehicle(name, vehicleType, settlement));
							} else {
								unitManager.addUnit(new Rover(name, vehicleType, settlement));
							}
						}
					}

					else
						throw new IllegalStateException(
								"Manufacture.addProcess(): output: " + item.getType() + " not a valid type.");
				}
			}
		}

		processes.remove(process);

		// Untag an 3D Printer (upon the process is ended or discontinued)
		// if (numPrinterInUse >= 1)
		// numPrinterInUse--;

		// Log process ending.
		logger.log(getBuilding().getSettlement(), Level.FINEST, 20_000,
				getBuilding() + " ending manufacturing process: "
				+ process.getInfo().getName());
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

		Map<Integer, Integer> partsSalvaged = new ConcurrentHashMap<>(0);

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

					double mass = totalNumber * part.getMassPerItem();
					double capacity = settlement.getCargoCapacity();
					if (mass <= capacity)
						settlement.storeItemResource(id, totalNumber);

					Good good = GoodsUtil.getGood(part.getName());
					if (good == null) {
						logger.severe(part.getName() + " is not a good.");
					}
					else
						// Recalculate settlement good value for salvaged part.
						settlement.getGoodsManager().determineGoodValue(good);
				}
			}
		}

		// Finish the salvage.
		((Salvagable) process.getSalvagedUnit()).getSalvageInfo().finishSalvage(partsSalvaged);

		salvages.remove(process);

		// Log salvage process ending.
		logger.log(getBuilding().getSettlement(), Level.FINEST, 20_000,
				getBuilding() + " ending salvage process: " + process.toString());

	}

	@Override
	public double getMaintenanceTime() {
		double result = 0D;
		// Add maintenance for tech level.
		result += techLevel * 10D;
		// Add maintenance for num of printers in use.
		result += numPrintersInUse * 10D;

		return result;
	}

	/**
	 * Check if enough 3D printer(s) are supporting the manufacturing
	 * processes
	 * @param pulse
	 */
	public void checkPrinters(ClockPulse pulse) {
		// Check only once a day for # of processes that are needed.
		if (pulse.isNewSol()) {
			// Gets the available number of printers in storage
			int numAvailable = building.getSettlement().getItemResourceStored(printerID);

			// NOTE: it's reasonable to create a settler's task to install a 3-D printer manually over a period of time
			if (numPrintersInUse < numMaxConcurrentProcesses) {
				int deficit = numMaxConcurrentProcesses - numPrintersInUse;
				logger.info(getBuilding().getSettlement(), 20_000,
						getBuilding() + " - "
						+ numAvailable
						+ " 3D-printer(s) in storage.");
				logger.info(getBuilding().getSettlement(), 20_000,
						getBuilding() + " - "
						+ numPrintersInUse
						+ " 3D-printer(s) in use.");

				if (deficit > 0 && numAvailable > 0) {
					int size = Math.min(numAvailable, deficit);
					for (int i=0; i<size; i++) {
						numPrintersInUse++;
						numAvailable--;
						int lacking = building.getSettlement().retrieveItemResource(printerID, 1);
						if (lacking > 0) {
							logger.info(getBuilding().getSettlement(), 20_000,
									"No 3D-printer available for " + getBuilding() + ".");
						}
					}

					logger.info(getBuilding().getSettlement(), 20_000,
							getBuilding() + " - "
							+ size
							+ " 3D-printer(s) just installed.");
				}
			}

            // NOTE: if not having enough printers,
			// determine how to use GoodsManager to push for making new 3D printers
		}
	}

//	/**
//	 * Distributes used 3D printer(s) from settlement's inventory for use
//	 */
//	public void distributePrinters() {
//		Settlement settlement = building.getSettlement();
//		Inventory inv = building.getInventory();
//
//		int s_available = inv.getItemResourceNum(printerID);
//		int s_needed = settlement.getSumOfManuProcesses();
//		int surplus = s_available - s_needed;
//		int b_needed = maxProcesses;
//
//		if (surplus > 0) {
//			if (surplus >= b_needed) {
//				inv.retrieveItemResources(printerID, b_needed);
//				// b_inv.storeItemResources(printerItem, b_needed);
//				settlement.addManuProcesses(b_needed);
//			} else {
//				inv.retrieveItemResources(printerID, surplus);
//				// b_inv.storeItemResources(printerItem, surplus);
//				settlement.addManuProcesses(surplus);
//			}
//		}
//	}

	public int getNumPrintersInUse() {
		return numPrintersInUse;
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

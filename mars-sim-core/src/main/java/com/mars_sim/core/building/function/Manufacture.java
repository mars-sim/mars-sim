/*
 * Mars Simulation Project
 * Manufacture.java
 * @date 2024-09-01
 * @author Scott Davis
 */
package com.mars_sim.core.building.function;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.FunctionSpec;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.manufacture.ManufactureProcess;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.manufacture.ManufactureUtil;
import com.mars_sim.core.manufacture.SalvageProcess;
import com.mars_sim.core.manufacture.WorkshopProcess;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.MathUtils;

/**
 * A building function for manufacturing.
 */
public class Manufacture extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Manufacture.class.getName());
	
	private static final int SKILL_GAP = 1;

	private static final int PRINTER_ID = ItemResourceUtil.printerID;

	private static final double PROCESS_MAX_VALUE = 100D;

	private static final String CONCURRENT_PROCESSES = "concurrent-processes";

	// Data members.
	private int techLevel;
	private int numPrintersInUse;
	private final int numMaxConcurrentProcesses;
	
	private List<WorkshopProcess> ongoingProcesses;
	private List<SalvageProcess> ongoingSalvages;
		
	// NOTE: create a map to show which process has a 3D printer in use and which doesn't

	/**
	 * Constructor.
	 *
	 * @param building the building the function is for.
	 * @param spec Details of the Function at hand
	 * @throws BuildingException if error constructing function.
	 */
	public Manufacture(Building building, FunctionSpec spec) {
		// Use Function constructor.
		super(FunctionType.MANUFACTURE, spec, building);

		techLevel = spec.getTechLevel();
		numMaxConcurrentProcesses = spec.getIntegerProperty(CONCURRENT_PROCESSES);
		numPrintersInUse = numMaxConcurrentProcesses;

		ongoingProcesses = new CopyOnWriteArrayList<>();
		ongoingSalvages = new CopyOnWriteArrayList<>();
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

		double result;

		FunctionSpec spec = buildingConfig.getFunctionSpec(type, FunctionType.MANUFACTURE);
		int buildingTech = spec.getTechLevel();

		double demand = settlement.getAllAssociatedPeople().stream()
				.mapToDouble(p -> p.getSkillManager().getSkillLevel(SkillType.MATERIALS_SCIENCE))
				.sum();

		double supply = 0D;
		int highestExistingTechLevel = 0;
		boolean removedBuilding = false;
		BuildingManager buildingManager = settlement.getBuildingManager();
		for(Building building : buildingManager.getBuildingSet(FunctionType.MANUFACTURE)) {
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

			processValueDiff = MathUtils.between(processValueDiff, 0D, PROCESS_MAX_VALUE);

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

		for(ManufactureProcessInfo process : ManufactureUtil.getManufactureProcessesForTechLevel(techLevel)) {
			double value = ManufactureUtil.getManufactureProcessValue(process, settlement);
			if (value > result) {
				result = value;
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
		return ongoingProcesses.size() + ongoingSalvages.size();
	}

	/**
	 * Gets a list of on-going manufacturing processes.
	 *
	 * @return unmodifiable list of on-going processes.
	 */
	public List<WorkshopProcess> getProcesses() {
		return Collections.unmodifiableList(ongoingProcesses);
	}

	
	public boolean isFull() {
		return getCurrentTotalProcesses() >= numPrintersInUse;
	}
	
	/**
	 * Adds a new manufacturing process to the building.
	 *
	 * @param process the new manufacturing process.
	 * @throws BuildingException if error adding process.
	 */
	public boolean addManuProcess(ManufactureProcess process) {

		if (getCurrentTotalProcesses() >= numPrintersInUse) {
			logger.warning(getBuilding(), "No capacity adding ManuProcess " + process.getInfo().getName());
			return false;
		}

		ongoingProcesses.add(process);

		
		return true;
	}

	/**
	 * Gets a list of the current salvage processes.
	 *
	 * @return unmodifiable list of salvage processes.
	 */
	public List<SalvageProcess> getSalvageProcesses() {
		return Collections.unmodifiableList(ongoingSalvages);
	}
	
	/**
	 * Adds a new salvage process to the building.
	 *
	 * @param process the new salvage process.
	 */
	public boolean addSalvProcess(SalvageProcess process) {

		if (getCurrentTotalProcesses() >= numPrintersInUse) {
			// BUT Salvage does not use printers ??
			logger.warning(getBuilding(), "No capacity to start process '" + process.getInfo().getName() + "'.");
			
			return false;
		}

		ongoingSalvages.add(process);
		return true;
	}

	@Override
	public double getCombinedPowerLoad() {
		return ongoingProcesses.stream()
				.filter(p -> p.getProcessTimeRemaining() > 0D)
				.mapToDouble(p -> p.getInfo().getPowerRequired())
				.sum();
	}

	@Override
	public double getPoweredDownPowerRequired() {
		return getCombinedPowerLoad();
	}

	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
			// Check once a sol only
			checkPrinters(pulse);

			ongoingProcesses.forEach(p -> p.addProcessTime(pulse.getElapsed()));
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
		for(var process : ongoingProcesses) {
			boolean workRequired = (process.getWorkTimeRemaining() > 0D);
			// Allow a low material science skill person to have access to do the next level skill process
			boolean skillRequired = (process.getInfo().getSkillLevelRequired() <= skill + SKILL_GAP);
			if (workRequired && skillRequired)
				return true;
		}

		return false;
	}

	/**
	 * Checks if manufacturing function currently requires salvaging work.
	 *
	 * @param skill the person's materials science skill level.
	 * @return true if manufacturing work.
	 */
	public boolean requiresSalvagingWork(int skill) {

		for(SalvageProcess process : ongoingSalvages) {
			boolean workRequired = (process.getWorkTimeRemaining() > 0D);
			// Allow a low material science skill person to have access to do the next level skill process
			boolean skillRequired = (process.getInfo().getSkillLevelRequired() <= skill + 1);
			if (workRequired && skillRequired)
				return true;
		}

		return false;
	}

	/**
	 * Remove the process from the ongoing processes list.
	 * @param process
	 */
	public void removeProcess(WorkshopProcess process) {
		ongoingProcesses.remove(process);
		ongoingSalvages.remove(process);
	}


	@Override
	public double getMaintenanceTime() {
		double result = getCombinedPowerLoad() * .25;
		// Add maintenance for tech level.
		result *= techLevel * .5;
		// Add maintenance for num of printers in use.
		result *= numPrintersInUse * .5;
		return result;
	}

	/**
	 * Checks if enough 3D printer(s) are supporting the manufacturing.
	 * 
	 * processes
	 * @param pulse
	 */
	private void checkPrinters(ClockPulse pulse) {
		// Check only once a day for # of processes that are needed.
		if (pulse.isNewSol()) {
			// Gets the available number of printers in storage
			int numAvailable = building.getSettlement().getItemResourceStored(PRINTER_ID);

			// NOTE: it's reasonable to create a settler's task to install a 3-D printer manually over a period of time
			if (numPrintersInUse < numMaxConcurrentProcesses) {
				int deficit = numMaxConcurrentProcesses - numPrintersInUse;
				logger.info(getBuilding(), 20_000,
						numPrintersInUse
						+ " 3D-printer(s) in use out of " + numAvailable);

				if (deficit > 0 && numAvailable > 0) {
					int size = Math.min(numAvailable, deficit);
					for (int i=0; i<size; i++) {
						numPrintersInUse++;
						numAvailable--;
						int lacking = building.getSettlement().retrieveItemResource(PRINTER_ID, 1);
						if (lacking > 0) {
							logger.info(getBuilding(), 20_000,
									"No 3D-printer available.");
						}
					}

					logger.info(getBuilding(), 20_000,
							size + " 3D-printer(s) just installed.");
				}
			}

            // NOTE: if not having enough printers,
			// determine how to use GoodsManager to push for making new 3D printers
		}
	}

	public int getNumPrintersInUse() {
		return numPrintersInUse;
	}


	@Override
	public void destroy() {
		super.destroy();

		ongoingProcesses.forEach(p -> p.destroy());
        ongoingSalvages.forEach(s -> s.destroy());
	}
}

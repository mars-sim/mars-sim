/*
 * Mars Simulation Project
 * FoodProduction.java
 * @date 2024-09-01
 * @author Manny Kung
 */
package com.mars_sim.core.building.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.config.FunctionSpec;
import com.mars_sim.core.food.FoodProductionProcess;
import com.mars_sim.core.food.FoodProductionProcessInfo;
import com.mars_sim.core.food.FoodProductionUtil;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.MathUtils;

/**
 * A building function for food production.
 */
public class FoodProduction extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(FoodProduction.class.getName());

	private static final int SKILL_GAP = 1;

	// Why are we usign printer to do cooking ?
	private static final int PRINTER_ID = ItemResourceUtil.SLS_3D_PRINTER_ID;

	private static final double PROCESS_MAX_VALUE = 100D;

	private static final String CONCURRENT_PROCESSES = "concurrent-processes";

	// Data members.
	private int techLevel;
	private int numPrintersInUse;
	private int numMaxConcurrentProcesses;
	
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
		super(FunctionType.FOOD_PRODUCTION, spec, building);

		techLevel = spec.getTechLevel();
		numMaxConcurrentProcesses = spec.getIntegerProperty(CONCURRENT_PROCESSES);
		numPrintersInUse = numMaxConcurrentProcesses;
		
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

		double result;

		FunctionSpec spec = buildingConfig.getFunctionSpec(buildingType, FunctionType.FOOD_PRODUCTION);

		int buildingTech = spec.getTechLevel();

		double demand = settlement.getAllAssociatedPeople().stream()
						.mapToInt(p -> p.getSkillManager().getSkillLevel(SkillType.COOKING))
						.sum();

		double supply = 0D;
		int highestExistingTechLevel = 0;
		boolean removedBuilding = false;
		for(Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.FOOD_PRODUCTION)) {
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingType) && !removedBuilding) {
				removedBuilding = true;
			} else {
				FoodProduction manFunction = building.getFoodProduction();
				int tech = manFunction.techLevel;
				double processes = manFunction.getNumPrintersInUse();
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
		// add difference between best food production processes.
		if (buildingTech > highestExistingTechLevel) {
			double bestExistingProcessValue = 0D;
			if (highestExistingTechLevel > 0D) {
				bestExistingProcessValue = getBestFoodProductionProcessValue(highestExistingTechLevel, settlement);
			}
			double bestBuildingProcessValue = getBestFoodProductionProcessValue(buildingTech, settlement);
			double processValueDiff = bestBuildingProcessValue - bestExistingProcessValue;
			processValueDiff = MathUtils.between(processValueDiff, 0, PROCESS_MAX_VALUE);

			result += processValueDiff;
		}

		return result;
	}

	/**
	 * Gets the best food production process value for a given food production tech
	 * level at a settlement.
	 * 
	 * @param techLevel  the food production tech level.
	 * @param settlement the settlement
	 * @return best food production process value.
	 */
	private static double getBestFoodProductionProcessValue(int techLevel, Settlement settlement) {

		double result = 0D;

		for(FoodProductionProcessInfo process : FoodProductionUtil.getProcessesForTechSkillLevel(techLevel)) {
			double value = FoodProductionUtil.getFoodProductionProcessValue(process, settlement);
			if (value > result) {
				result = value;
			}
		}

		return result;
	}

	/**
	 * Gets the food production tech level of the building.
	 * 
	 * @return tech level.
	 */
	public int getTechLevel() {
		return techLevel;
	}

	/**
	 * Gets the maximum concurrent food production processes supported by the
	 * building.
	 *
	 * @return maximum concurrent processes.
	 */
	public int getMaxProcesses() {
		return numMaxConcurrentProcesses;
	}

	/**
	 * Gets the total food production processes currently in this building.
	 * 
	 * @return total process number.
	 */
	public int getCurrentTotalProcesses() {
		return processes.size();
	}

	/**
	 * Gets a list of the current food production processes.
	 * 
	 * @return unmodifiable list of processes.
	 */
	public List<FoodProductionProcess> getProcesses() {
		return Collections.unmodifiableList(processes);
	}

	/**
	 * Adds a new food production process to the building.
	 * 
	 * @param process the new food production process.
	 * @throws BuildingException if error adding process.
	 */
	public void addProcess(FoodProductionProcess process) {

		if (process == null) {
			throw new IllegalArgumentException("process is null");
		}

		if (getCurrentTotalProcesses() > numPrintersInUse) {
			logger.info(getBuilding(), 20_000,
					getBuilding()
					+ ": " + getCurrentTotalProcesses() + " concurrent processes.");
			logger.info(getBuilding(), 20_000,
					getBuilding()
					+ ": " + numPrintersInUse + " 3D-printer(s) installed for use."
					+ "");
			logger.info(getBuilding(), 20_000,
					getBuilding()
					+ ": " + (numMaxConcurrentProcesses-numPrintersInUse)
					+ " 3D-printer slot(s) available."
					+ "");
			return;
		}
		
		processes.add(process);

		process.getInfo().retrieveInputs(getBuilding().getSettlement());

		// Log food production process starting.
		logger.log(getBuilding(), Level.FINEST, 20_000,
				getBuilding()
				+ " starting food production process: " + process.getInfo().getName());	
	}

	@Override
	public double getCombinedPowerLoad() {
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
		return getCombinedPowerLoad();
	}

	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
			if (pulse.isNewSol()) {
				// Check once a sol only
				checkPrinters();
			}

			List<FoodProductionProcess> finishedProcesses = new ArrayList<>();
	
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
	 * Checks if food production function currently requires food production work.
	 * 
	 * @param skill the person's materials science skill level.
	 * @return true if food production work.
	 */
	public boolean requiresWork(int skill) {
		boolean result = false;

		if (numPrintersInUse > getCurrentTotalProcesses())
			result = true;
		else {
			Iterator<FoodProductionProcess> i = processes.iterator();
			while (i.hasNext()) {
				FoodProductionProcess process = i.next();
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
	 * Deposits the outputs.
	 * 
	 * @param process
	 */
	private void depositOutputs(FoodProductionProcess process) {
		Settlement settlement = building.getSettlement();

		process.getInfo().depositOutputs(settlement, false);

		// Record process finish
		settlement.recordProcess(process.getInfo().getName(), "Food", building);
	}

	/**
	 * Ends a food production process.
	 * 
	 * @param process   the process to end.
	 * @param premature true if the process has ended prematurely.
	 * @throws BuildingException if error ending process.
	 */
	public void endFoodProductionProcess(FoodProductionProcess process, boolean premature) {
	
		if (!premature) {
			// Produce outputs.
			depositOutputs(process);
		}
		else {
			process.getInfo().returnInputs(building.getSettlement());
		}

		processes.remove(process);
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
	 * Check if enough 3D printer(s) are supporting the manufacturing
	 * processes
	 * @param pulse
	 */
	private void checkPrinters() {
		// Gets the available number of printers in storage
		int numAvailable = building.getSettlement().getItemResourceStored(PRINTER_ID);

		// Malfunction of a 3D-printer should trigger this
		// NOTE: it's reasonable to create a settler's task to install 
		// a 3-D printer manually over a period of time
		if (numPrintersInUse < numMaxConcurrentProcesses) {
			int deficit = numMaxConcurrentProcesses - numPrintersInUse;
			logger.info(getBuilding(), 20_000,
					getBuilding() + " - "
					+ numAvailable
					+ " 3D-printer(s) in storage.");
			logger.info(getBuilding(), 20_000,
					getBuilding() + " - "
					+ numPrintersInUse
					+ " 3D-printer(s) in use.");

			if (deficit > 0 && numAvailable > 0) {
				int size = Math.min(numAvailable, deficit);
				for (int i=0; i<size; i++) {
					numPrintersInUse++;
					numAvailable--;
					int lacking = building.getSettlement().retrieveItemResource(PRINTER_ID, 1);
					if (lacking > 0) {
						logger.info(getBuilding(), 20_000,
								"No 3D-printer available for " + getBuilding() + ".");
					}
				}

				logger.info(getBuilding(), 20_000,
						getBuilding() + " - "
						+ size
						+ " 3D-printer(s) just installed.");
			}
		}

        // NOTE: if not having enough printers,
		// determine how to use GoodsManager to push for making new 3D printers
	}


	public int getNumPrintersInUse() {
		return numPrintersInUse;
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

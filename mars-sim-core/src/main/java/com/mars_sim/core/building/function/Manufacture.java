/*
 * Mars Simulation Project
 * Manufacture.java
 * @date 2024-09-01
 * @author Scott Davis
 */
package com.mars_sim.core.building.function;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.FunctionSpec;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.manufacture.ManufactureUtil;
import com.mars_sim.core.manufacture.Tooling;
import com.mars_sim.core.manufacture.WorkshopProcess;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.MathUtils;

/**
 * A building function for manufacturing.
 */
public class Manufacture extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Records the available capacity of a tooling in a Manufacture function.
	 */
	public static class ToolCapacity implements Serializable {
		private static final long serialVersionUID = 1L;
		int used = 0;
		int capacity;

		ToolCapacity(int cap) {
			this.capacity = cap;
		}

		private boolean claimTool() {
			if (used < capacity) {
				used++;
				return true;
			}
			return false;
		}

		private void releaseTool() {
			if (used > 0) {
				used--;
			}
		}

		private boolean hasCapacity() {
			return (used < capacity);
		}

		public int getInUse() {
			return used;
		}

        public int getCapacity() {
            return capacity;
        }
	}

	private static final ToolCapacity NO_TOOL = new ToolCapacity(0);

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Manufacture.class.getName());
	
	private static final int SKILL_GAP = 1;

	private static final double PROCESS_MAX_VALUE = 100D;

	private static final String CONCURRENT_PROCESSES = "concurrent-processes";

	// Data members.
	private int techLevel;
	private int numMaxConcurrentProcesses;
	private Map<Tooling, ToolCapacity> availableTools;
	
	private List<WorkshopProcess> ongoingProcesses;
		
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

		@SuppressWarnings("unchecked")
		Map<Tooling,Integer> toolCapacity = (Map<Tooling, Integer>) spec.getProperty("tooling");
		availableTools = new HashMap<>();
		toolCapacity.forEach((tool, cap) ->
			availableTools.put(tool, new ToolCapacity(cap))
		);

		ongoingProcesses = new CopyOnWriteArrayList<>();
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
				double processes = manFunction.getCurrentTotalProcesses();
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

			processValueDiff = MathUtils.between(processValueDiff, 0, PROCESS_MAX_VALUE);

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
		return ongoingProcesses.size();
	}

	/**
	 * Gets a list of on-going manufacturing processes.
	 *
	 * @return unmodifiable list of on-going processes.
	 */
	public List<WorkshopProcess> getProcesses() {
		return Collections.unmodifiableList(ongoingProcesses);
	}

	/**
	 * What spare capacity for new processes does this facility have
	 * @return
	 */
	public int getCapacity() {
		return numMaxConcurrentProcesses - getCurrentTotalProcesses();
	}
	
	/**
	 * Adds a new  process to the building.
	 *
	 * @param process the new manufacturing process.
	 * @throws BuildingException if error adding process.
	 */
	public boolean addProcess(WorkshopProcess process) {

		var tool = process.getTooling();
		if ((tool != null) && !availableTools.getOrDefault(tool, NO_TOOL).claimTool()) {
			logger.warning(getBuilding(), tool + ": no capacity adding ManuProcess " + process.getName());
			return false;
		}

		ongoingProcesses.add(process);

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
	 * Remove the process from the ongoing processes list.
	 * @param process
	 */
	public void removeProcess(WorkshopProcess process) {
		var tool = process.getTooling();
		if (tool != null) {
			availableTools.get(tool).releaseTool();
		}
		ongoingProcesses.remove(process);
	}


	@Override
	public double getMaintenanceTime() {
		double result = getCombinedPowerLoad() * .25;
		// Add maintenance for tech level.
		result *= techLevel * .5;
		// Add maintenance for num of printers in use.
		var numTools = availableTools.values().stream()
				.mapToInt(t -> t.used)
				.sum();
		result *= numTools * .5;
		return result;
	}

	/**
	 * What tools are available at thois station
	 * @return
	 */
	public Set<Tooling> getAvailableTools() {
		return availableTools.entrySet().stream()
				.filter(t -> t.getValue().hasCapacity())
				.map(Entry::getKey)
				.collect(Collectors.toSet());
	}

	/**
	 * Get the detaisl of all tools at the station
	 * @return
	 */
	public Map<Tooling,ToolCapacity> getToolDetails() {
		return availableTools;
	}


	@Override
	public void destroy() {
		super.destroy();

		ongoingProcesses.forEach(p -> p.destroy());
	}
}

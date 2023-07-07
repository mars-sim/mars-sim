/*
 * Mars Simulation Project
 * ResourceProcessor.java
 * @date 2022-10-29
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.structure.building.ResourceProcessEngine;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsTime;

/**
 * The Abstratc class that runs ResoruceProcesses as a building function.
 */
public abstract class ResourceProcessor extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	private static final String POWER_DOWN_LEVEL = "power-down-level";

	private static final double PROCESS_MAX_VALUE = 100D;

	private double powerDownProcessingLevel;

	private List<ResourceProcess> processes;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 */
	protected ResourceProcessor(FunctionType type, FunctionSpec spec, Building building,  
								List<ResourceProcessEngine> processSpecs) {
		// Use Function constructor
		super(type, spec, building);

		powerDownProcessingLevel = spec.getDoubleProperty(POWER_DOWN_LEVEL);
		processes = new ArrayList<>();
		for (ResourceProcessEngine wspec : processSpecs) {
			processes.add(new ResourceProcess(wspec));
		}
	}

	/**
	 * Gets the value of the function for a list of ProcessSpecs running at a particular Settlement
	 * 
	 * @param type the building name.
	 * @param settlement   the settlement.
	 * @param processSpecs 
	 * @return value (VP) of building function.
	 */
	protected static double calculateFunctionValue(Settlement settlement, List<ResourceProcessEngine> processSpecs) {

		double result = 0D;
		for (ResourceProcessEngine process : processSpecs) {
			double processValue = 0D;
			for (Integer outResource : process.getOutputResources()) {
				if (!process.isWasteOutputResource(outResource)) {
					double rate = process.getMaxOutputRate(outResource);
					processValue += settlement.getGoodsManager().getGoodValuePoint(outResource) * rate;
				}
			}

			double inputInventoryLimit = 1D;
			// May try List.copyOf(process.getInputResources())
			for (int inResource : process.getInputResources()) {
				if (!process.isAmbientInputResource(inResource)) {
					double rate = process.getMaxInputRate(inResource);
					processValue -= settlement.getGoodsManager().getGoodValuePoint(inResource) * rate;

					// Check inventory limit.
					double inputSupply = settlement.getAmountResourceStored(inResource);
					if (inputSupply < rate) {
						double limit = inputSupply / rate;
						if (limit < inputInventoryLimit) {
							inputInventoryLimit = limit;
						}
					}
				}
			}

			// Subtract value of require power.
			double powerHrsRequiredPerSol = process.getPowerRequired() * MarsTime.HOURS_PER_MILLISOL * 1000D;
			double powerValue = powerHrsRequiredPerSol * settlement.getPowerGrid().getPowerValue();
			processValue -= powerValue;

			if (processValue < 0D) {
				processValue = 0D;
			}

			// Modify by input inventory limit.
			processValue *= inputInventoryLimit;

			if (processValue > PROCESS_MAX_VALUE) {
				processValue = PROCESS_MAX_VALUE;
			}

			result += processValue;
		}

		return result;
	}

	/**
	 * Gets the processes in this Function.
	 *
	 * @return list of processes.
	 */
	public List<ResourceProcess> getProcesses() {
		return processes;
	}

	/**
	 * Gets the power down mode processing level.
	 *
	 * @return proportion of max processing rate (0D - 1D)
	 */
	public double getPowerDownProcessingLevel() {
		return powerDownProcessingLevel;
	}

	/**
	 * Time passing for the building.
	 *
	 * @param accumulatedTime amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {

			double productionLevel = 0D;
			if (getBuilding().getPowerMode() == PowerMode.FULL_POWER)
				productionLevel = 1D;
			else if (getBuilding().getPowerMode() == PowerMode.POWER_DOWN)
				productionLevel = powerDownProcessingLevel;
			// Run each resource process.
			for(ResourceProcess p : processes) {
				p.processResources(pulse, productionLevel, getBuilding().getSettlement());
			}
		}
		return valid;
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 *
	 * @return power (kW)
	 */
	@Override
	public double getFullPowerRequired() {
		double result = 0D;
		for(ResourceProcess process : processes) {
			if (process.isProcessRunning()) {
				result += process.getPowerRequired();
			}
		}
		return result;
	}

	/**
	 * Gets the amount of power required when function is at power down level. 
	 * This is based on the a percentage of the full power using the power down processing level
	 *
	 * @return power (kW)
	 */
	@Override
	public double getPoweredDownPowerRequired() {
		return getFullPowerRequired() * powerDownProcessingLevel;
	}

	@Override
	public double getMaintenanceTime() {
		return processes.size() * 5D;
	}
}

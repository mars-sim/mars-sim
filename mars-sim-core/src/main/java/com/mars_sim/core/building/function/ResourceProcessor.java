/*
 * Mars Simulation Project
 * ResourceProcessor.java
 * @date 2022-10-29
 * @author Barry Evans
 */
package com.mars_sim.core.building.function;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.config.FunctionSpec;
import com.mars_sim.core.building.utility.power.PowerMode;
import com.mars_sim.core.resourceprocess.ResourceProcess;
import com.mars_sim.core.resourceprocess.ResourceProcessEngine;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;

/**
 * The Abstract class that runs ResoruceProcesses as a building function.
 */
public abstract class ResourceProcessor extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static final String LOW_POWER_LEVEL = "low-power-level";

	private static final double PROCESS_MAX_VALUE = 100D;

	private double lowPowerProcessingLevel;

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

		lowPowerProcessingLevel = spec.getDoubleProperty(LOW_POWER_LEVEL);
		processes = new ArrayList<>();
		for (ResourceProcessEngine wspec : processSpecs) {
			processes.add(new ResourceProcess(wspec, building));
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
			var spec = process.getProcessSpec();
			for (Integer outResource : spec.getOutputResources()) {
				if (!spec.isWasteOutputResource(outResource)) {
					double fullRate = process.getBaseFullOutputRate(outResource);
					processValue += settlement.getGoodsManager().getGoodValuePoint(outResource) * fullRate;
				}
			}

			double inputInventoryLimit = 1D;
			// May try List.copyOf(process.getInputResources())
			for (int inResource : spec.getInputResources()) {
				if (!spec.isAmbientInputResource(inResource)) {
					double fullRate = process.getBaseFullInputRate(inResource);
					processValue -= settlement.getGoodsManager().getGoodValuePoint(inResource) * fullRate;

					// Check inventory limit.
					double inputSupply = settlement.getSpecificAmountResourceStored(inResource);
					if (inputSupply < fullRate) {
						double limit = inputSupply / fullRate;
						if (limit < inputInventoryLimit) {
							inputInventoryLimit = limit;
						}
					}
				}
			}

			// Subtract value of require power.
			double powerHrsRequiredPerReaction = spec.getkWhRequired();
			double powerValue = powerHrsRequiredPerReaction * settlement.getPowerGrid().getPowerValue();
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
	 * Gets the low power mode processing level.
	 *
	 * @return proportion of max processing rate (0D - 1D)
	 */
	public double getLowPowerProcessingLevel() {
		return lowPowerProcessingLevel;
	}

	/**
	 * Gets the overall duty cycle percentage.
	 * 
	 * @return
	 */
	public double getOverallPercentDuty() {
		double overall = 0;
		int size = 0;
		for (ResourceProcess rp: getProcesses()) {
			size++;
			overall += rp.getPercentDuty();
		}
		if (size == 0) 
			size = 1;
		
		return overall / size;
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
		
		double cumulativeMillisols = masterClock.getMarsTime().getLandingMillisols();
		
		if (valid) {
			double productionLevel = 0D;
			
			PowerMode mode = getBuilding().getPowerMonitor().getPowerMode();
			
			if (mode == PowerMode.FULL_POWER)
				productionLevel = 1D;
			else if (mode == PowerMode.LOW_POWER) {
				// Note: For now, low power mode will reduce the processing capability by 50%
				productionLevel = lowPowerProcessingLevel;
			}
			
			if (mode != PowerMode.NO_POWER) {
			// Run each resource process.
				for (ResourceProcess p : processes) {
					p.processResources(pulse, productionLevel, cumulativeMillisols);
				}
			}
		}
		return valid;
	}

	/**
	 * Gets the amount of power required only when function is running.
	 *
	 * @return power (kW)
	 */
	@Override
	public double getFullPowerLoad() {
		double result = 0D;
		for(ResourceProcess process : processes) {
			if (process.isProcessRunning()) {
				result += process.getkWhRequired();
			}
		}
		return result;
	}
	
	/**
	 * Gets the amount of power required when function is at low power level. 
	 *
	 * @return power (kW)
	 */
	@Override
	public double getLowPowerLoad() {
		return getFullPowerLoad() * lowPowerProcessingLevel;
	}

	@Override
	public double getMaintenanceTime() {
		double result = getFullPowerLoad();
		// Add num of processes.
		result *= processes.size() * .5;
		
		return result;
	}
}

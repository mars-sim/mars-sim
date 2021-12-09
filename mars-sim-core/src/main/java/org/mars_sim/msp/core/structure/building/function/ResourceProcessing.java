/*
 * Mars Simulation Project
 * ResourceProcessing.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.ResourceProcessSpec;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The ResourceProcessing class is a building function indicating that the
 * building has a set of resource processes.
 */
public class ResourceProcessing extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final double PROCESS_MAX_VALUE = 100D;
	/** The period of time [in millisols] between each resource processing call. */
	public static final double PROCESS_INTERVAL = 2.0;

	private double time;

	private double powerDownProcessingLevel;

	private List<ResourceProcess> resourceProcesses;

	/**
	 * Constructor.
	 *
	 * @param building the building the function is for.
	 * @throws BuildingException if function cannot be constructed.
	 */
	public ResourceProcessing(Building building) {
		// Use Function constructor
		super(FunctionType.RESOURCE_PROCESSING, building);

		powerDownProcessingLevel = buildingConfig.getResourceProcessingPowerDown(building.getBuildingType());
		resourceProcesses = new ArrayList<>();
		for (ResourceProcessSpec spec : buildingConfig.getResourceProcesses(building.getBuildingType())) {
			resourceProcesses.add(new ResourceProcess(spec));
		}
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
		Iterator<ResourceProcessSpec> i = buildingConfig.getResourceProcesses(buildingName).iterator();
		while (i.hasNext()) {
			ResourceProcessSpec process = i.next();
			double processValue = 0D;
			Iterator<Integer> ii = process.getOutputResources().iterator();
			while (ii.hasNext()) {
				int resource = ii.next();
				if (!process.isWasteOutputResource(resource)) {
					double rate = process.getMaxOutputResourceRate(resource);
					processValue += settlement.getGoodsManager().getGoodValuePerItem(resource) * rate;
				}
			}

			double inputInventoryLimit = 1D;
			Iterator<Integer> iii = process.getInputResources().iterator();
		    while (iii.hasNext()) {
		    	int resource = iii.next();
				if (!process.isAmbientInputResource(resource)) {
					double rate = process.getMaxInputResourceRate(resource);
					processValue -= settlement.getGoodsManager().getGoodValuePerItem(resource) * rate;

					// Check inventory limit.
					double inputSupply = settlement.getAmountResourceStored(resource);
					if (inputSupply < rate) {
						double limit = inputSupply / rate;
						if (limit < inputInventoryLimit) {
							inputInventoryLimit = limit;
						}
					}
				}
			}

			// Subtract value of require power.
			double powerHrsRequiredPerSol = process.getPowerRequired() * MarsClock.HOURS_PER_MILLISOL * 1000D;
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
	 * Gets the resource processes in this building.
	 *
	 * @return list of processes.
	 */
	public List<ResourceProcess> getProcesses() {
		return resourceProcesses;
	}

	/**
	 * Gets the power down mode resource processing level.
	 *
	 * @return proportion of max processing rate (0D - 1D)
	 */
	public double getPowerDownResourceProcessingLevel() {
		return powerDownProcessingLevel;
	}

	/**
	 * Time passing for the building.
	 *
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
			time += pulse.getElapsed();
			if (time >= PROCESS_INTERVAL) {
				time = time - PROCESS_INTERVAL;
				double productionLevel = 0D;
				if (getBuilding().getPowerMode() == PowerMode.FULL_POWER)
					productionLevel = 1D;
				else if (getBuilding().getPowerMode() == PowerMode.POWER_DOWN)
					productionLevel = powerDownProcessingLevel;
				// Run each resource process.
				Iterator<ResourceProcess> i = resourceProcesses.iterator();
				while (i.hasNext()) {
					// 	Note: need to reduce processResources since it takes up 32% of all cpu utilization
					i.next().processResources(PROCESS_INTERVAL, productionLevel, getBuilding().getSettlement());
				}
			}
		}
		return valid;
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 *
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		double result = 0D;
		Iterator<ResourceProcess> i = resourceProcesses.iterator();
		while (i.hasNext()) {
			ResourceProcess process = i.next();
			if (process.isProcessRunning()) {
				result += process.getPowerRequired();
			}
		}
		return result;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 *
	 * @return power (kW)
	 */
	public double getPoweredDownPowerRequired() {
		double result = 0D;
		Iterator<ResourceProcess> i = resourceProcesses.iterator();
		while (i.hasNext()) {
			ResourceProcess process = i.next();
			if (process.isProcessRunning()) {
				result += process.getPowerRequired();
			}
		}
		return result;
	}

	@Override
	public double getMaintenanceTime() {
		return resourceProcesses.size() * 5D;
	}

	@Override
	public void destroy() {
		super.destroy();

		resourceProcesses = null;
	}
}

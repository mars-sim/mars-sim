/*
 * Mars Simulation Project
 * ResourceProcessing.java
 * @date 2022-07-11
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.structure.building.ResourceProcessSpec;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The ResourceProcessing class is a building function indicating that the
 * building has a set of resource processes.
 */
public class ResourceProcessing extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final String POWER_DOWN_LEVEL = "power-down-level";
	public static final String SABATIER = "sabatier";
	public static final String REGOLITH = "regolith"; // Do not use "convert regolith to ores with sand"
	public static final String ICE = "melt and filter ice";
	public static final String PPA = "PPA";
	public static final String CFR = "Carbon Formation Reactor";
	public static final String OGS = "Oxygen Generation System";
	
	public static final double PROCESS_MAX_VALUE = 100D;

	private double powerDownProcessingLevel;

	private List<ResourceProcess> resourceProcesses;

	/**
	 * Constructor.
	 *
	 * @param building the building the function is for.
	 * @param spec Definition of the Function
	 * @throws BuildingException if function cannot be constructed.
	 */
	public ResourceProcessing(Building building, FunctionSpec spec) {
		// Use Function constructor
		super(FunctionType.RESOURCE_PROCESSING, spec, building);

		powerDownProcessingLevel = spec.getDoubleProperty(POWER_DOWN_LEVEL);
		resourceProcesses = new ArrayList<>();
		for (ResourceProcessSpec pSpec : buildingConfig.getResourceProcesses(building.getBuildingType())) {
			resourceProcesses.add(new ResourceProcess(pSpec));
		}
	}

	/**
	 * Gets the value of the function for a named building.
	 *
	 * @param type the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String type, boolean newBuilding, Settlement settlement) {

		double result = 0D;
		for(ResourceProcessSpec process :buildingConfig.getResourceProcesses(type)) {
			double processValue = 0D;
			for (int outResource : process.getOutputResources()) {
				if (!process.isWasteOutputResource(outResource)) {
					double rate = process.getMaxOutputRate(outResource);
					processValue += settlement.getGoodsManager().getGoodValuePoint(outResource) * rate;
				}
			}

			double inputInventoryLimit = 1D;
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
			Iterator<ResourceProcess> i = resourceProcesses.iterator();
			while (i.hasNext()) {
				i.next().processResources(pulse, productionLevel, getBuilding().getSettlement());
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
	@Override
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

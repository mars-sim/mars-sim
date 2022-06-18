/*
 * Mars Simulation Project
 * WasteProcessing.java
 * @date 2022-06-15
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.WasteProcessSpec;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The WasteProcessing class is a building function for handling waste disposal and recycling.
 */
public class WasteProcessing extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final double PROCESS_MAX_VALUE = 100D;

	private double powerDownProcessingLevel;

	private List<WasteProcess> wasteProcesses;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 */
	public WasteProcessing(Building building) {
		// Use Function constructor
		super(FunctionType.WASTE_PROCESSING, building);

		powerDownProcessingLevel = buildingConfig.getWasteProcessingPowerDown(building.getBuildingType());
		wasteProcesses = new ArrayList<>();
		for (WasteProcessSpec spec : buildingConfig.getWasteProcesses(building.getBuildingType())) {
			wasteProcesses.add(new WasteProcess(spec));
		}
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingName the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding, Settlement settlement) {

		double result = 0D;
		Iterator<WasteProcessSpec> i = buildingConfig.getWasteProcesses(buildingName).iterator();
		while (i.hasNext()) {
			WasteProcessSpec process = i.next();
			double processValue = 0D;
			Iterator<Integer> ii = process.getOutputResources().iterator();
			while (ii.hasNext()) {
				int resource = ii.next();
				if (!process.isWasteOutputResource(resource)) {
					double rate = process.getMaxOutputRate(resource);
					processValue += settlement.getGoodsManager().getGoodValuePerItem(resource) * rate;
				}
			}

			double inputInventoryLimit = 1D;
			Iterator<Integer> iii = new HashSet<>(process.getInputResources()).iterator();
		    while (iii.hasNext()) {
		    	int resource = iii.next();
				if (!process.isAmbientInputResource(resource)) {
					double rate = process.getMaxInputRate(resource);
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
	 * Gets the waste processes in this building.
	 *
	 * @return list of processes.
	 */
	public List<WasteProcess> getProcesses() {
		return wasteProcesses;
	}

	/**
	 * Gets the power down mode waste processing level.
	 *
	 * @return proportion of max processing rate (0D - 1D)
	 */
	public double getPowerDownWasteProcessingLevel() {
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
			Iterator<WasteProcess> i = wasteProcesses.iterator();
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
	public double getFullPowerRequired() {
		double result = 0D;
		Iterator<WasteProcess> i = wasteProcesses.iterator();
		while (i.hasNext()) {
			WasteProcess process = i.next();
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
		Iterator<WasteProcess> i = wasteProcesses.iterator();
		while (i.hasNext()) {
			WasteProcess process = i.next();
			if (process.isProcessRunning()) {
				result += process.getPowerRequired();
			}
		}
		return result;
	}

	@Override
	public double getMaintenanceTime() {
		return wasteProcesses.size() * 5D;
	}

	@Override
	public void destroy() {
		super.destroy();

		wasteProcesses = null;
	}
}

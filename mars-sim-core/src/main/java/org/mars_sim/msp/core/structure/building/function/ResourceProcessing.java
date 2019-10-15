/**
 * Mars Simulation Project
 * ResourceProcessing.java
 * @version 3.1.0 2017-05-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The ResourceProcessing class is a building function indicating that the
 * building has a set of resource processes.
 */
public class ResourceProcessing extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final FunctionType FUNCTION = FunctionType.RESOURCE_PROCESSING;

	public static final double PROCESS_MAX_VALUE = 100D;

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
		super(FUNCTION, building);

		powerDownProcessingLevel = buildingConfig.getResourceProcessingPowerDown(building.getBuildingType());
		resourceProcesses = buildingConfig.getResourceProcesses(building.getBuildingType());

		// Load activity spots
		loadActivitySpots(buildingConfig.getResourceProcessingActivitySpots(building.getBuildingType()));
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

		Inventory inv = settlement.getInventory();

		double result = 0D;
		List<ResourceProcess> processes = buildingConfig.getResourceProcesses(buildingName);
		for (ResourceProcess process : processes) {
			double processValue = 0D;
			for (int resource : process.getOutputResources()) {
				if (!process.isWasteOutputResource(resource)) {
					Good resourceGood = GoodsUtil.getResourceGood(resource);
					double rate = process.getMaxOutputResourceRate(resource);// * 1000D;
					processValue += settlement.getGoodsManager().getGoodValuePerItem(resourceGood) * rate;
				}
			}

			double inputInventoryLimit = 1D;
			for (int resource : process.getInputResources()) {
				if (!process.isAmbientInputResource(resource)) {
					Good resourceGood = GoodsUtil.getResourceGood(resource);
					if (resource == ResourceUtil.greyWaterID); 
					double rate = process.getMaxInputResourceRate(resource);// * 1000D;
					processValue -= settlement.getGoodsManager().getGoodValuePerItem(resourceGood) * rate;

					// Check inventory limit.
					double inputSupply = inv.getAmountResourceStored(resource, false);
					if (inputSupply < rate) {
						double limit = inputSupply / rate;
						if (limit < inputInventoryLimit) {
							inputInventoryLimit = limit;
						}
					}
				}
			}

			// Subtract value of require power.
//			double hoursInSol = MarsClock.convertMillisolsToSeconds(1000D) / 60D / 60D;
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
	public void timePassing(double time) {

		double productionLevel = 0D;
		if (getBuilding().getPowerMode() == PowerMode.FULL_POWER)
			productionLevel = 1D;
		else if (getBuilding().getPowerMode() == PowerMode.POWER_DOWN)
			productionLevel = powerDownProcessingLevel;

		// Run each resource process.
		Iterator<ResourceProcess> i = resourceProcesses.iterator();
		while (i.hasNext()) {
			i.next().processResources(time, productionLevel, getBuilding().getSettlementInventory());
		}
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

		Iterator<ResourceProcess> i = resourceProcesses.iterator();
		while (i.hasNext()) {
			i.next().destroy();
		}
		// resourceProcesses.clear();
		resourceProcesses = null;
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
}
/**
 * Mars Simulation Project
 * ThermalStorage.java
 * @version 3.06 2014-10-17
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.ThermalSystem;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The ThermalStorage class is a building function for storing heat.
 */
public class ThermalStorage
extends Function
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Building function name.
	private static final BuildingFunction FUNCTION = BuildingFunction.THERMAL_STORAGE;

	// Data members.
	private double heatStorageCapacity;
	private double heatStored;

	/**
	 * Constructor.
	 * @param building the building with the function.
	 * @throws BuildingException if error parsing configuration.
	 */
	public ThermalStorage(Building building) {
		// Call Function constructor.
		super(FUNCTION, building);

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

		heatStorageCapacity = config.getThermalStorageCapacity(building.getName());
	}

	/**
	 * Gets the value of the function for a named building.
	 * @param buildingName the building name.
	 * @param newBuilding true if adding a new building.
	 * @param settlement the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding,
			Settlement settlement) {

		ThermalSystem grid = settlement.getThermalSystem();

		double hrInSol = MarsClock.convertMillisolsToSeconds(1000D) / 60D / 60D;
		double demand = grid.getRequiredHeat() * hrInSol;

		double supply = 0D;
		Iterator<Building> iStore = settlement.getBuildingManager().getBuildings(ThermalStorage.FUNCTION).iterator();
		while (iStore.hasNext()) {
			Building building = iStore.next();
			ThermalStorage store = (ThermalStorage) building.getFunction(ThermalStorage.FUNCTION);
			double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
			supply += store.heatStorageCapacity * wearModifier;
		}

		double existingThermalStorageValue = demand / (supply + 1D);

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		double heatStorage = config.getThermalStorageCapacity(buildingName);

		double value = heatStorage * existingThermalStorageValue / hrInSol;
		if (value > 10D) value = 10D;

		return value;
	}

	/**
	 * Gets the building's heat storage capacity.
	 * @return capacity (J).
	 */
	public double getThermalStorageCapacity() {
		return heatStorageCapacity;
	}

	/**
	 * Gets the building's stored heat.
	 * @return heat (J).
	 */
	public double getHeatStored() {
		return heatStored;
	}

	/**
	 * Sets the heat stored in the building.
	 * @param heatStored the stored heat (J).
	 */
	public void setHeatStored(double heatStored) {
		if (heatStored > heatStorageCapacity) heatStored = heatStorageCapacity;
		else if (heatStored < 0D) heatStored = 0D;
		this.heatStored = heatStored;
	}

	@Override
	public double getFullHeatRequired() {
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		return 0;
	}

	@Override
	public void timePassing(double time) {
		// Do nothing.
	}

	@Override
	public double getMaintenanceTime() {
		return heatStorageCapacity / 5D;
	}

	@Override
	public double getFullPowerRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownPowerRequired() {
		// TODO Auto-generated method stub
		return 0;
	}
}
/*
 * Mars Simulation Project
 * Computation.java
 * @date 2021-09-30
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.structure.building.SourceSpec;

/**
 * The Computation class is a building function for generating computational power.
 */
public class Computation extends Function{

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Configuration properties
	private static final String COMPUTING_UNIT = "computing-unit";
	private static final String POWER_DEMAND = "power-demand";
	private static final String COOLING_DEMAND = "cooling-demand";

	private double computingUnit;
	private double powerDemand;
	private double coolingDemand;
	
	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 * @param spec Sepecification of the Compputing Function
	 * @throws BuildingException if error in constructing function.
	 */
	public Computation(Building building, FunctionSpec spec) {
		// Call Function constructor.
		super(FunctionType.COMPUTATION, building);
		
		computingUnit = spec.getDoubleProperty(COMPUTING_UNIT);
		powerDemand = spec.getDoubleProperty(POWER_DEMAND);
		coolingDemand = spec.getDoubleProperty(COOLING_DEMAND);		
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param type the building type.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String type, boolean newBuilding, Settlement settlement) {

		double demand = settlement.getPowerGrid().getRequiredPower();

		double supply = 0D;
		boolean removedBuilding = false;
		for (Building building : settlement.getBuildingManager().getBuildings(FunctionType.COMPUTATION)) {
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(type) && !removedBuilding) {
				removedBuilding = true;
			} else {
				Computation com = building.getComputation();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += com.getComputingUnit() * wearModifier;
			}
		}

		double existingPowerValue = demand / (supply + 1D);


		double powerSupply = buildingConfig.getHeatSources(type).stream()
								.mapToDouble(SourceSpec::getCapacity).sum();

		return powerSupply * existingPowerValue;
	}

	@Override
	public String[] getMalfunctionScopeStrings() {
		return new String[] {"Computation"};
	}

	public double getComputingUnit() {
		return computingUnit;
	}

	public double getPowerDemand() {
		return powerDemand;
	}

	public double getCoolingDemand() {;
		return coolingDemand;
	}
	
	@Override
	public void destroy() {
		super.destroy();

	}

}

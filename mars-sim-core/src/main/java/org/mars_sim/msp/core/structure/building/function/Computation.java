/*
 * Mars Simulation Project
 * Computation.java
 * @date 2021-09-30
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.SourceSpec;
import org.mars_sim.msp.core.time.ClockPulse;

/**
 * The Computation class is a building function for generating computational power.
 */
public class Computation extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
//	private static final SimLogger logger = SimLogger.getLogger(Computation.class.getName());

	private double computingUnit;
	private double powerDemand;
	private double coolingDemand;
	
	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public Computation(Building building) {
		// Call Function constructor.
		super(FunctionType.COMPUTATION, building);
		
		computingUnit = buildingConfig.getComputingUnit(building.getBuildingType());
		powerDemand = buildingConfig.getPowerDemand(building.getBuildingType());
		coolingDemand = buildingConfig.getCoolingDemand(building.getBuildingType());		
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

		double demand = settlement.getPowerGrid().getRequiredPower();

		double supply = 0D;
		boolean removedBuilding = false;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.COMPUTATION).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				Computation com = building.getComputation();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += com.getComputingUnit() * wearModifier;
			}
		}

		double existingPowerValue = demand / (supply + 1D);


		double powerSupply = buildingConfig.getHeatSources(buildingName).stream()
								.mapToDouble(SourceSpec::getCapacity).sum();

		return powerSupply * existingPowerValue;
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
//			powerGeneratedCache = calculateGeneratedPower(pulse.getElapsed());
		}
		return valid;
	}

	@Override
	public String[] getMalfunctionScopeStrings() {
		String[] result = new String[] {"Computation"};
//		// TODO take care to properly internationalize malfunction scope "strings"
//		result[0] = getFunctionType().getName();
//
//		for (int x = 0; x < powerSources.size(); x++) {
//			result[x + 1] = powerSources.get(x).getType().getName();
//		}

		return result;
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

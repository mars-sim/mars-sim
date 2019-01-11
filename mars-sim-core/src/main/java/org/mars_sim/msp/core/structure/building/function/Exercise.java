/**
 * Mars Simulation Project
 * Exercise.java
 * @version 3.1.0 2018-10-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;

/**
 * The Exercise class is a building function for exercise.
 */
public class Exercise extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final FunctionType FUNCTION = FunctionType.EXERCISE;

	// Data members
	private int exercisers;
	private int exerciserCapacity;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public Exercise(Building building) {
		// Use Function constructor.
		super(FUNCTION, building);

		this.exerciserCapacity = buildingConfig.getExerciseCapacity(building.getBuildingType());

		// Load activity spots
		loadActivitySpots(buildingConfig.getExerciseActivitySpots(building.getBuildingType()));
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

		// Demand is one exerciser capacity for every four inhabitants.
		double demand = settlement.getNumCitizens() / 4D;

		double supply = 0D;
		boolean removedBuilding = false;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				Exercise exerciseFunction = (Exercise) building.getFunction(FUNCTION);
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += exerciseFunction.exerciserCapacity * wearModifier;
			}
		}

		double valueExerciser = demand / (supply + 1D);

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		double exerciserCapacity = config.getExerciseCapacity(buildingName);

		return exerciserCapacity * valueExerciser;
	}

	/**
	 * Gets the number of people who can use the exercise facility at once.
	 * 
	 * @return number of people.
	 */
	public int getExerciserCapacity() {
		return exerciserCapacity;
	}

	/**
	 * Gets the current number of people using the exercise facility.
	 * 
	 * @return number of people.
	 */
	public int getNumExercisers() {
		return exercisers;
	}

	/**
	 * Adds a person to the exercise facility.
	 * 
	 * @throws BuildingException if person would exceed exercise facility capacity.
	 */
	public void addExerciser() {
		exercisers++;
		if (exercisers > exerciserCapacity) {
			exercisers = exerciserCapacity;
			throw new IllegalStateException("Exercise facility in use.");
		}
	}

	/**
	 * Removes a person from the exercise facility.
	 * 
	 * @throws BuildingException if nobody is using the exercise facility.
	 */
	public void removeExerciser() {
		exercisers--;
		if (exercisers < 0) {
			exercisers = 0;
			throw new IllegalStateException("Exercise facility empty.");
		}
	}

	/**
	 * Time passing for the building.
	 * 
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return 0D;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * 
	 * @return power (kW)
	 */
	public double getPoweredDownPowerRequired() {
		return 0D;
	}

	@Override
	public double getMaintenanceTime() {
		return exerciserCapacity * 5D;
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
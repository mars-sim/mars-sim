/**
 * Mars Simulation Project
 * Exercise.java
 * @version 3.2.0 2021-06-20
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
		super(FunctionType.EXERCISE, building);

		this.exerciserCapacity = buildingConfig.getFunctionCapacity(building.getBuildingType(), FunctionType.EXERCISE);
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
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.EXERCISE).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				Exercise exerciseFunction = (Exercise) building.getFunction(FunctionType.EXERCISE);
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += exerciseFunction.exerciserCapacity * wearModifier;
			}
		}

		double valueExerciser = demand / (supply + 1D);

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		double exerciserCapacity = config.getFunctionCapacity(buildingName, FunctionType.EXERCISE);

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

	@Override
	public double getMaintenanceTime() {
		return exerciserCapacity * 5D;
	}
}

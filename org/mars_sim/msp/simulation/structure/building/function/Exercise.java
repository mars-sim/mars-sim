/**
 * Mars Simulation Project
 * Exercise.java
 * @version 2.76 2004-06-02
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;
 
import java.io.Serializable;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.structure.building.*;

/**
 * The Exercise class is a building function for exercise.
 */
public class Exercise extends Function implements Serializable {
        
	public static final String NAME = "Exercise";
    
    // Data members
    private int exercisers;
    private int exerciserCapacity;
    
	/**
	 * Constructor
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public Exercise(Building building) throws BuildingException {
		// Use Function constructor.
		super(NAME, building);
		
		SimulationConfig simConfig = Simulation.instance().getSimConfig();
		BuildingConfig config = simConfig.getBuildingConfiguration();
		
		try {
			this.exerciserCapacity = config.getExerciseCapacity(building.getName());
		}
		catch (Exception e) {
			throw new BuildingException("Exercise.constructor: " + e.getMessage());
		}
	}
	
	/**
	 * Gets the number of people who can use the exercise facility at once.
	 * @return number of people.
	 */
	public int getExerciserCapacity() {
		return exerciserCapacity;
	}
	
	/**
	 * Gets the current number of people using the exercise facility.
	 * @return number of people.
	 */
	public int getNumExercisers() {
		return exercisers;
	}
	
	/**
	 * Adds a person to the exercise facility.
	 * @throws BuildingException if person would exceed exercise facility capacity.
	 */
	public void addExerciser() throws BuildingException {
		exercisers++;
		if (exercisers > exerciserCapacity) {
			exercisers = exerciserCapacity;
			throw new BuildingException("Exercise facility in use.");
		}
	}
	
	/**
	 * Removes a person from the exercise facility.
	 * @throws BuildingException if nobody is using the exercise facility.
	 */
	public void removeExerciser() throws BuildingException {
		exercisers--;
		if (exercisers < 0) {
			exercisers = 0;
			throw new BuildingException("Exercise facility empty.");
		}
	}
	
	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) throws BuildingException {}
	
	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return 0D;
	}
	
	/**
	 * Gets the amount of power required when function is at power down level.
	 * @return power (kW)
	 */
	public double getPowerDownPowerRequired() {
		return 0D;
	}
}
/**
 * Mars Simulation Project
 * Exercise.java
 * @version 2.85 2008-08-18
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;
 
import java.io.Serializable;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.structure.Settlement;
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
		
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		
		try {
			this.exerciserCapacity = config.getExerciseCapacity(building.getName());
		}
		catch (Exception e) {
			throw new BuildingException("Exercise.constructor: " + e.getMessage());
		}
	}
    
    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     */
    public static final double getFunctionValue(String buildingName, boolean newBuilding, 
            Settlement settlement) {
        // TODO: Implement later as needed.
        return 0D;
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
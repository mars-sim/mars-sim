/**
 * Mars Simulation Project
 * Cooking.java
 * @version 2.78 2004-11-12
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.time.MarsClock;

/**
 * The Cooking class is a building function for cooking meals.
 */
public class Cooking extends Function implements Serializable {

	public static final String NAME = "Cooking";

	// Data members
	private int numCooks;
	private int cookCapacity;
	private List meals;
	
	/**
	 * Constructor
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public Cooking(Building building) throws BuildingException {
		// Use Function constructor.
		super(NAME, building);
		
		numCooks = 0;
		meals = new ArrayList();
		
		SimulationConfig simConfig = Simulation.instance().getSimConfig();
		BuildingConfig config = simConfig.getBuildingConfiguration();
		
		try {
			this.cookCapacity = config.getCookCapacity(building.getName());
		}
		catch (Exception e) {
			throw new BuildingException("Cooking.constructor: " + e.getMessage());
		}		
	}	
	
	/**
	 * Get the maximum number of cooks supported by this facility.
	 * @return max number of cooks
	 */
	public int getCookCapacity() {
		return cookCapacity;
	}
	
	/**
	 * Adds a cook to the facility.
	 */
	public void addCook() {
		if (numCooks < cookCapacity) numCooks++;
	}
	
	/**
	 * Removes a cook from the facility.
	 *
	 */
	public void removeCook() {
		if (numCooks > 0) numCooks--;
	}
	
	/**
	 * Get the current number of cooks using this facility.
	 * @return number of cooks
	 */
	public int getNumCooks() {
		return numCooks;
	}
	
	/**
	 * Gets the number of cooked meals at this facility.
	 * @return number of meals
	 */
	public int getNumberOfMeals() {
		return meals.size();
	}
	
	/**
	 * Checks if there are any cooked meals in this facility.
	 * @return true if cooked meals
	 */
	public boolean hasCookedMeal() {
		return (meals.size() > 0);
	}
	
	/**
	 * Gets the number of cooked meals in this facility.
	 * @return number of meals
	 */
	public int getNumberOfCookedMeals() {
		return meals.size();
	}
	
	/**
	 * Gets a cooked meal from this facility.
	 * @return
	 */
	public CookedMeal getCookedMeal() {
		CookedMeal bestMeal = null;
		int bestQuality = -1;
		Iterator i = meals.iterator();
		while (i.hasNext()) {
			CookedMeal meal = (CookedMeal) i.next();
			if (meal.getQuality() > bestQuality) {
				bestQuality = meal.getQuality();
				bestMeal = meal;
			}
		}
		
		if (bestMeal != null) meals.remove(bestMeal);
		
		return bestMeal;
	}
	
	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) throws BuildingException {
		// Move expired meals back to food again (refrigerate leftovers).
		Iterator i = meals.iterator();
		while (i.hasNext()) {
			CookedMeal meal = (CookedMeal) i.next();
			MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
			if (MarsClock.getTimeDiff(meal.getExpirationTime(), currentTime) < 0D) {
				getBuilding().getInventory().addResource(Resource.FOOD, 1D);
				i.remove();
			}
		}
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return numCooks * 10D;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * @return power (kW)
	 */
	public double getPowerDownPowerRequired() {
		return 0;
	}
}
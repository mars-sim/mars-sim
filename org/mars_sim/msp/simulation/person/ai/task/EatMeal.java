/**
 * Mars Simulation Project
 * EatMeal.java
 * @version 2.78 2004-11-16
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;

/** The EatMeal class is a task for eating a meal.
 *  The duration of the task is 20 millisols.
 *
 *  Note: Eating a meal reduces hunger to 0.
 */
class EatMeal extends Task implements Serializable {

    // Static members
    private static final double DURATION = 40D; // The predetermined duration of task in millisols
    private static final double STRESS_MODIFIER = -.2D; // The stress modified per millisol.
    
    private CookedMeal meal;

    /** Constructs a EatMeal object
     *  @param person the person to perform the task
     */
    public EatMeal(Person person) {
        super("Eating a meal", person, false, false, STRESS_MODIFIER);
        
        String location = person.getLocationSituation();
        if (location.equals(Person.INSETTLEMENT)) {
        	try {
				// If person is in a settlement, try to find a dining area.
        		Building diningBuilding = getAvailableDiningBuilding(person);
        		if (diningBuilding != null) 
        			BuildingManager.addPersonToBuilding(person, diningBuilding);
        	}
        	catch (BuildingException e) {
        		System.err.println("EatMeal.constructor(): " + e.getMessage());
        		endTask();
        	}
        	
        	// If cooked meal in a local kitchen available, take it to eat.
        	Cooking kitchen = getKitchenWithFood(person);
        	if (kitchen != null) meal = kitchen.getCookedMeal();
        	if (meal != null) description = "Eating a cooked meal";
        }
        else if (location.equals(Person.OUTSIDE)) endTask();
    }

    /** Returns the weighted probability that a person might perform this task.
     *
     *  @param person the person to perform the task
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {

        double result = person.getPhysicalCondition().getHunger() - 250D;
        if (result < 0D) result = 0D;
        
        if (person.getLocationSituation().equals(Person.OUTSIDE)) result = 0D;
	
		try {
			Building building = getAvailableDiningBuilding(person);
			if (building != null) {
				result *= Task.getCrowdingProbabilityModifier(person, building);
				result *= Task.getRelationshipModifier(person, building);
			}
		}
		catch (BuildingException e) {
			System.err.println("EatMeal.getProbability(): " + e.getMessage());
		}
		
		// Check if there's a cooked meal at a local kitchen.
		if (getKitchenWithFood(person) != null) result *= 5;
	
        return result;
    }

    /** 
     * This task allows the person to eat for the duration.
     * @param time the amount of time to perform this task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     * @throws Exception if error performing task.
     */
    double performTask(double time) throws Exception {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

		SimulationConfig simConfig = Simulation.instance().getSimConfig();
		PersonConfig config = simConfig.getPersonConfiguration();

		// If person has a cooked meal, additional stress is reduced.
		if (meal != null) {
			PhysicalCondition condition = person.getPhysicalCondition();
			double stress = condition.getStress();
			condition.setStress(stress - (STRESS_MODIFIER * (meal.getQuality() + 1D)));
		}

        person.getPhysicalCondition().setHunger(0D);
        timeCompleted += time;
        if (timeCompleted > DURATION) {
        	try {
            	person.consumeFood(config.getFoodConsumptionRate() * (1D / 3D), (meal == null));
        	}
        	catch (Exception e) {
        		System.err.println(person.getName() + " unable to eat meal: " + e.getMessage());
        	}
            endTask();
           
            return timeCompleted - DURATION;
        }
        else return 0D; 
    }
    
    /**
     * Gets an available dining building that the person can use.
     * Returns null if no dining building is currently available.
     *
     * @param person the person
     * @return available dining building
     * @throws BuildingException if error finding dining building.
     */
    private static Building getAvailableDiningBuilding(Person person) throws BuildingException {
     
        Building result = null;
        
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Settlement settlement = person.getSettlement();
        	BuildingManager manager = settlement.getBuildingManager();
        	List diningBuildings = manager.getBuildings(Dining.NAME);
        	diningBuildings = BuildingManager.getNonMalfunctioningBuildings(diningBuildings);
        	diningBuildings = BuildingManager.getLeastCrowdedBuildings(diningBuildings);
        	diningBuildings = BuildingManager.getBestRelationshipBuildings(person, diningBuildings);
        	
			if (diningBuildings.size() > 0) result = (Building) diningBuildings.get(0);
		}
        
        return result;
    }
    
    /**
     * Gets a kitchen in the person's settlement that currently has cooked meals.
     * @param person the person to check for
     * @return the kitchen or null if none.
     */
    private static Cooking getKitchenWithFood(Person person) {
    	Cooking result = null;
    	
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Settlement settlement = person.getSettlement();
			BuildingManager manager = settlement.getBuildingManager();
			List cookingBuildings = manager.getBuildings(Cooking.NAME);
			Iterator i = cookingBuildings.iterator();
			while (i.hasNext()) {
				Building building = (Building) i.next();
				try {
					Cooking kitchen = (Cooking) building.getFunction(Cooking.NAME);
					if (kitchen.hasCookedMeal()) result = kitchen;
				}
				catch (BuildingException e) {
					System.err.println("EatMeal.Cooking(): " + e.getMessage());
				}
			}
		}
    	
    	return result;
    }
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		return 0;
	}
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List getAssociatedSkills() {
		List results = new ArrayList();
		return results;
	}
}
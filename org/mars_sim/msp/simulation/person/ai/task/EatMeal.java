/**
 * Mars Simulation Project
 * EatMeal.java
 * @version 2.81 2007-08-27
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;

/** The EatMeal class is a task for eating a meal.
 *  The duration of the task is 40 millisols.
 *
 *  Note: Eating a meal reduces hunger to 0.
 */
class EatMeal extends Task implements Serializable {

	// Task phase
	private static final String EATING = "Eating";
	
    // Static members
    private static final double DURATION = 40D; // The predetermined duration of task in millisols
    private static final double STRESS_MODIFIER = -.2D; // The stress modified per millisol.
    
    private CookedMeal meal;

    /** 
     * Constructs a EatMeal object
     * @param person the person to perform the task
     * @throws Exception if error constructing task.
     */
    public EatMeal(Person person) throws Exception {
        super("Eating a meal", person, false, false, STRESS_MODIFIER, true, DURATION);
        
        String location = person.getLocationSituation();
        if (location.equals(Person.INSETTLEMENT)) {
			// If person is in a settlement, try to find a dining area.
        	Building diningBuilding = getAvailableDiningBuilding(person);
        	if (diningBuilding != null) BuildingManager.addPersonToBuilding(person, diningBuilding);
        	
        	// If cooked meal in a local kitchen available, take it to eat.
        	Cooking kitchen = getKitchenWithFood(person);
        	if (kitchen != null) meal = kitchen.getCookedMeal();
        	if (meal != null) setDescription("Eating a cooked meal");
        }
        else if (location.equals(Person.OUTSIDE)) endTask();
        
        // Initialize task phase.
        addPhase(EATING);
        setPhase(EATING);
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
		if (getKitchenWithFood(person) != null) result *= 5D;
		else {
			// Check if there is food available to eat.
			if (!isFoodAvailable(person)) result = 0D;
		}
	
        return result;
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (EATING.equals(getPhase())) return eatingPhase(time);
    	else return time;
    }
    
    /**
     * Performs the eating phase of the task.
     * @param time the amount of time (millisol) to perform the eating phase.
     * @return the amount of time (millisol) left after performing the eating phase.
     * @throws Exception if error performing the eating phase.
     */
    private double eatingPhase(double time) throws Exception {
    	
    	PhysicalCondition condition = person.getPhysicalCondition();
    	
		// If person has a cooked meal, additional stress is reduced.
		if (meal != null) {
			double stress = condition.getStress();
			condition.setStress(stress - (STRESS_MODIFIER * (meal.getQuality() + 1D)));
		}
    	
        if (getDuration() < (getTimeCompleted() + time)) {
    		PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
    		try {
    			person.consumeFood(config.getFoodConsumptionRate() * (1D / 3D), (meal == null));
    			condition.setHunger(0D);
    		}
    		catch (Exception e) {
    			// If person can't obtain food from container, end the task.
    			endTask();
    		}
        }
        
        return 0D; 
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// This task adds no experience.
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
        	List<Building> diningBuildings = manager.getBuildings(Dining.NAME);
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
     * Checks if there is food available for the person.
     * @param person the person to check.
     * @return true if food is available.
     */
    private static boolean isFoodAvailable(Person person) {
    	boolean result = false;
		Unit containerUnit = person.getContainerUnit();
		if (containerUnit != null) {
			try {
				Inventory inv = containerUnit.getInventory();
				if (inv.getAmountResourceStored(AmountResource.FOOD) > 0D) result = true;;
			}
			catch (InventoryException e) {
				e.printStackTrace(System.err);
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
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(0);
		return results;
	}
}
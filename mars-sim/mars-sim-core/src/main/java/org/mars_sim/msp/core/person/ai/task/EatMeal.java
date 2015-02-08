/**
 * Mars Simulation Project
 * EatMeal.java
 * @version 3.07 2015-01-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.cooking.CookedMeal;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The EatMeal class is a task for eating a meal.
 * The duration of the task is 40 millisols.
 * Note: Eating a meal reduces hunger to 0.
 */
public class EatMeal 
extends Task 
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(EatMeal.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.eatMeal"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase EATING = new TaskPhase(Msg.getString(
            "Task.phase.eating")); //$NON-NLS-1$

    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -.2D;
    private static final int NUMBER_OF_MEAL_PER_SOL = 4;

    // Data members
    private CookedMeal meal;
    
    private Person person;
    private String mealLocation;

    /** 
     * Constructs a EatMeal object, hence a constructor.
     * @param person the person to perform the task
     */
    public EatMeal(Person person) {
        super(NAME, person, false, false, STRESS_MODIFIER, true, 10D + 
                RandomUtil.getRandomDouble(30D));

        this.person = person;
        //logger.info("just called EatMeal's constructor");
        
        boolean walkSite = false;

        LocationSituation location = person.getLocationSituation();
        if (location == LocationSituation.IN_SETTLEMENT) {
            // If person is in a settlement, try to find a dining area.
            Building diningBuilding = getAvailableDiningBuilding(person);
            if (diningBuilding != null) {

                // Walk to dining building.
                walkToActivitySpotInBuilding(diningBuilding, true);
                walkSite = true;
            }

            // If cooked meal in a local kitchen available, take it to eat.
            Cooking kitchen = getKitchenWithFood(person);
            if (kitchen != null) {
            	mealLocation = kitchen.getBuilding().getNickName(); 
                meal = kitchen.eatAMeal();
                if (meal != null) {
                	//2015-01-06 Added setConsumerName()
                   	meal.setConsumerName(person.getName());
                 }
            }   
            walkSite = true;
        }
        else if (location == LocationSituation.OUTSIDE) {
            endTask();
        }

        if (!walkSite) {
            if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
                // If person is in rover, walk to passenger activity spot.
                if (person.getVehicle() instanceof Rover) {
                    walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
                }
            }
            else {
                // Walk to random location.
                walkToRandomLocation(true);
            }
        }

        // Initialize task phase.
        addPhase(EATING);
        setPhase(EATING);
    }

    @Override
    protected BuildingFunction getRelatedBuildingFunction() {
        return BuildingFunction.DINING;
    }

    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     */
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (EATING.equals(getPhase())) {
            return eatingPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the eating phase of the task.
     * @param time the amount of time (millisol) to perform the eating phase.
     * @return the amount of time (millisol) left after performing the eating phase.
     */
    private double eatingPhase(double time) {

        PhysicalCondition condition = person.getPhysicalCondition();
        

        // If person consumes a cooked meal, stress and fatigue is reduced.
        if (meal != null) {
        	
            double stress = condition.getStress();
            condition.setStress(stress - (STRESS_MODIFIER * (meal.getQuality() + 1D)));
            
            double fatigue = condition.getFatigue();
            if (fatigue > 300)
            	condition.setFatigue(fatigue - 300);
        }

        if (getDuration() <= (getTimeCompleted() + time)) {
            if (meal != null) {
                // Person consumes the cooked meal.
                String nameMeal = meal.getName();
                setDescription(Msg.getString("Task.description.eatMeal.cooked")); //$NON-NLS-1$
                //System.out.println(person + " has just eaten " + nameMeal);
               // System.out.println("EatMeal : meal.getDryMass() "+ Math.round(meal.getDryMass()*10.0)/10.0);
                condition.setHunger(0D);
                condition.addEnergy(meal.getDryMass());
            }
            else {
                // Person consumes preserved food.
                try {                	
//                	// In a settlement, the person will choose to eat
//                	// dessert first instead of preserved food
//                	LocationSituation location = person.getLocationSituation();
//                	if (location == LocationSituation.IN_SETTLEMENT) {
//                		//can I instantiate new EatDessert(person); ?
//                       	//logger.info(person + " has just eaten desserts");
//                	}
//                	else {
	                    eatPreservedFood();
	                    //System.out.println(person + " has just eaten preserved food");
	                    //System.out.println("EatMeal : condition.getMassPerServing() "+ Math.round(condition.getMassPerServing()*10.0)/10.0);  
	                    condition.setHunger(0D);
	                    condition.addEnergy(condition.getMassPerServing());
//                	}
                }
                catch (Exception e) {
                    // If person can't obtain food from container, end the task.
                    endTask();
                }
            }
        }

        return 0D; 
    }
    
    /**
     * Eat a meal of preserved food.
     * @throws Exception if problems finding preserved food to eat.
     */
    private void eatPreservedFood() throws Exception {
        PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
        double foodAmount = config.getFoodConsumptionRate() / NUMBER_OF_MEAL_PER_SOL;
        Unit containerUnit = person.getContainerUnit();
        if (containerUnit != null) {
            Inventory inv = containerUnit.getInventory();
            AmountResource food = AmountResource.findAmountResource(org.mars_sim.msp.core.LifeSupport.FOOD);
            double foodAvailable = inv.getAmountResourceStored(food, false);
            // 2015-01-09 Added addDemandRequest()
        	inv.addAmountDemandTotalRequest(food);
            if (foodAvailable >= foodAmount) {
            	
			 	// 2015-02-06 Added addResource()
	      		int num = RandomUtil.getRandomInt(19);
	      		if (num == 0){
		      		// There is a 5% probability that the preserved food is of no good and must be discarded
		      		// Remove the bad food amount from container unit.
		            inv.retrieveAmountResource(food, foodAmount);
		            // 2015-01-09 addDemandUsage()
		            inv.addAmountDemand(food, foodAmount);
		      		addResource(foodAmount, "Food Waste", inv);
	      		}
				
                // Remove preserved food amount from container unit.
                inv.retrieveAmountResource(food, foodAmount);
            	// 2015-01-09 addDemandUsage()
               	inv.addAmountDemand(food, foodAmount);
               	//logger.info(person + " has just eaten preserved food");
            }
            else {
                throw new Exception(person + " doesn't have enough preserved food to eat in " + containerUnit + 
                        " - food available: " + foodAvailable);
            }
        }
        else {
            throw new Exception(person + " does not have a container unit to get preserved food from.");
        }
    }

	// 2015-02-06 Added addResource()
	public void addResource(double amount, String name, Inventory inv) {
	
		try {
		AmountResource ar = AmountResource.findAmountResource(name);      
		double remainingCapacity = inv.getAmountResourceRemainingCapacity(ar, false, false);
		
		if (remainingCapacity < amount) {
		    // if the remaining capacity is smaller than the harvested amount, set remaining capacity to full
			amount = remainingCapacity;
		    //logger.info("addHarvest() : storage is full!");
		}
		inv.storeAmountResource(ar, amount, true);
		inv.addAmountSupplyAmount(ar, amount);
		
		}  catch (Exception e) {}
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
    public static Building getAvailableDiningBuilding(Person person) {

        Building result = null;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Settlement settlement = person.getSettlement();
            BuildingManager manager = settlement.getBuildingManager();
            List<Building> diningBuildings = manager.getBuildings(BuildingFunction.DINING);
            diningBuildings = BuildingManager.getWalkableBuildings(person, diningBuildings);
            diningBuildings = BuildingManager.getNonMalfunctioningBuildings(diningBuildings);
            diningBuildings = BuildingManager.getLeastCrowdedBuildings(diningBuildings);

            if (diningBuildings.size() > 0) {
                Map<Building, Double> diningBuildingProbs = BuildingManager.getBestRelationshipBuildings(
                        person, diningBuildings);
                result = RandomUtil.getWeightedRandomObject(diningBuildingProbs);
            }
        }

        return result;
    }

    /**
     * Gets a kitchen in the person's settlement that currently has cooked meals.
     * @param person the person to check for
     * @return the kitchen or null if none.
     */
    public static Cooking getKitchenWithFood(Person person) {
        Cooking result = null;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Settlement settlement = person.getSettlement();
            BuildingManager manager = settlement.getBuildingManager();
            List<Building> cookingBuildings = manager.getBuildings(BuildingFunction.COOKING);
            Iterator<Building> i = cookingBuildings.iterator();
            while (i.hasNext()) {
                Building building = i.next();
                Cooking kitchen = (Cooking) building.getFunction(BuildingFunction.COOKING);
                if (kitchen.hasCookedMeal()) result = kitchen;
            }
        }

        return result;
    }

    /**
     * Checks if there is food available for the person.
     * @param person the person to check.
     * @return true if food is available.
     */
    public static boolean isFoodAvailable(Person person) {
        boolean result = false;
        Unit containerUnit = person.getContainerUnit();
        if (containerUnit != null) {
            try {
                Inventory inv = containerUnit.getInventory();
                AmountResource food = AmountResource.findAmountResource(org.mars_sim.msp.core.LifeSupport.FOOD);
                PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
                double foodAmount = config.getFoodConsumptionRate() * .25D;
                if (inv.getAmountResourceStored(food, false) >= foodAmount) {
                    result = true;;
                }
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
        return result;
    }

    @Override
    public int getEffectiveSkillLevel() {
        return 0;
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(0);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();

        meal = null;
    }
}
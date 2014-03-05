/**
 * Mars Simulation Project
 * EatMeal.java
 * @version 3.06 2014-02-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.CookedMeal;
import org.mars_sim.msp.core.structure.building.function.Cooking;
import org.mars_sim.msp.core.structure.building.function.Dining;

/**
 * The EatMeal class is a task for eating a meal.
 * The duration of the task is 40 millisols.
 * Note: Eating a meal reduces hunger to 0.
 */
class EatMeal 
extends Task 
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(EatMeal.class.getName());

    // TODO Task phase should be an enum
    private static final String EATING = "Eating";

    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -.2D;

    // Data members
    private CookedMeal meal;

    /** 
     * Constructs a EatMeal object
     * @param person the person to perform the task
     */
    public EatMeal(Person person) {
        super("Eating a meal", person, false, false, STRESS_MODIFIER, true, 10D + 
                RandomUtil.getRandomDouble(30D));

        String location = person.getLocationSituation();
        if (location.equals(Person.INSETTLEMENT)) {
            // If person is in a settlement, try to find a dining area.
            Building diningBuilding = getAvailableDiningBuilding(person);
            if (diningBuilding != null) {

                // Walk to dining building.
                walkToDiningBuilding(diningBuilding);
            }

            // If cooked meal in a local kitchen available, take it to eat.
            Cooking kitchen = getKitchenWithFood(person);
            if (kitchen != null) {
                meal = kitchen.getCookedMeal();
            }
            if (meal != null) {
                setDescription("Eating a cooked meal");
            }
        }
        else if (location.equals(Person.OUTSIDE)) {
            endTask();
        }

        // Initialize task phase.
        addPhase(EATING);
        setPhase(EATING);
    }

    /** 
     * Returns the weighted probability that a person might perform this task.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {

        double result = person.getPhysicalCondition().getHunger() - 250D;
        if (result < 0D) result = 0D;

        if (person.getLocationSituation().equals(Person.OUTSIDE)) result = 0D;

        Building building = getAvailableDiningBuilding(person);
        if (building != null) {
            result *= Task.getCrowdingProbabilityModifier(person, building);
            result *= Task.getRelationshipModifier(person, building);
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
     * Walk to dining building.
     * @param diningBuilding the dining building.
     */
    private void walkToDiningBuilding(Building diningBuilding) {

        // Determine location within dining building.
        // TODO: Use action point rather than random internal location.
        Point2D.Double buildingLoc = LocalAreaUtil.getRandomInteriorLocation(diningBuilding);
        Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(buildingLoc.getX(), 
                buildingLoc.getY(), diningBuilding);

        if (Walk.canWalkAllSteps(person, settlementLoc.getX(), settlementLoc.getY(), 
                diningBuilding)) {
            
            // Add subtask for walking to dining building.
            addSubTask(new Walk(person, settlementLoc.getX(), settlementLoc.getY(), 
                    diningBuilding));
        }
        else {
            logger.fine(person.getName() + " unable to walk to dining building " + 
                    diningBuilding.getName());
            endTask();
        }
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

        // If person has a cooked meal, additional stress is reduced.
        if (meal != null) {
            double stress = condition.getStress();
            condition.setStress(stress - (STRESS_MODIFIER * (meal.getQuality() + 1D)));
        }

        if (getDuration() <= (getTimeCompleted() + time)) {
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
    private static Building getAvailableDiningBuilding(Person person) {

        Building result = null;

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
            BuildingManager manager = settlement.getBuildingManager();
            List<Building> diningBuildings = manager.getBuildings(Dining.NAME);
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
    private static Cooking getKitchenWithFood(Person person) {
        Cooking result = null;

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
            BuildingManager manager = settlement.getBuildingManager();
            List<Building> cookingBuildings = manager.getBuildings(Cooking.NAME);
            Iterator<Building> i = cookingBuildings.iterator();
            while (i.hasNext()) {
                Building building = i.next();
                Cooking kitchen = (Cooking) building.getFunction(Cooking.NAME);
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
    private static boolean isFoodAvailable(Person person) {
        boolean result = false;
        Unit containerUnit = person.getContainerUnit();
        if (containerUnit != null) {
            try {
                Inventory inv = containerUnit.getInventory();
                AmountResource food = AmountResource.findAmountResource("food");
                if (inv.getAmountResourceStored(food, false) > 0D) result = true;;
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
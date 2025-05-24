/*
 * Mars Simulation Project
 * Cooking.java
 * @date 2023-04-18
 * @author Scott Davis
 */
package com.mars_sim.core.building.function.cooking;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.FunctionSpec;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.task.CookMeal;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.fav.Favorite;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.WaterUseType;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The Cooking class is a building function for cooking meals.
 */
public class Cooking extends Function {

	/**
	 * Statitics of a prepared Dish
	 */
	public static final class MealStats implements Serializable {
		/** default serial id. */
		private static final long serialVersionUID = 1L;
		
		private double bestQuality;
		private double worseQuality;
		private int number;

		MealStats(double quality) {
			number = 1;
			this.bestQuality = quality;
			this.worseQuality = quality;
		}

		private MealStats(int number, double worseQuality, double bestQuality) {
			this.number = number;
			this.worseQuality = worseQuality;
			this.bestQuality = bestQuality;
		}

		public double getWorseQuality() {
			return worseQuality;
		}

		public double getBestQuality() {
			return bestQuality;
		}

		public int getNumber() {
			return number;
		}

		private void addMeal(double quality) {
			number++;
			if (quality < worseQuality) {
				worseQuality = quality;
			} else if (quality > bestQuality) {
				bestQuality = quality;
			}
		}

		/**
		 * Merge two MealStats objects summing the number of meals and takign the worst & best of the 2.
		 * @param a
		 * @param b
		 * @return
		 */
		public static MealStats sum(MealStats a, MealStats b) {
			return new MealStats(a.number + b.number, Math.min(a.worseQuality, b.worseQuality),
								Math.max(a.bestQuality, b.bestQuality));
		}
	}

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Cooking.class.getName());

	private static final String CONVERTING = "A meal has expired. Converting ";
	private static final String DISCARDED = " is expired and discarded at ";
	private static final String PRESERVED = "into preserved food at ";

	private static final int RECHECKING_FREQ = 250; // in millisols
	public static final double AMOUNT_OF_SALT_PER_MEAL = 0.005D;
	public static final double AMOUNT_OF_OIL_PER_MEAL = 0.01D;
	/**  The base amount of work time (cooking skill 0) to produce one single cooked meal.*/
	private static final double COOKED_MEAL_WORK_REQUIRED = 8D;
	// Note : 10 millisols is 15 mins
	/** The minimal amount of resource to be retrieved. */
	private static final double MIN = 0.00001;

	private static final double UP = 0.01;
	private static final double DOWN = 0.007;

	private boolean cookNoMore = false;

	/** The cache for msols */
	private int cookCapacity;
	private int mealCounterPerSol = 0;
	private boolean hasCookableMeal = false;

	// Dynamically adjusted the rate of generating meals
	private double cleaningAgentPerSol;
	/** Cleanliness score between -1 and 1. */
	private double cleanliness;
	private double cookingWorkTime;
	private double dryMassPerServing;

	// Data members
	private List<CookedMeal> cookedMeals;

	/** The quality history of each meal.  */
	private Map<String, MealStats> qualityMap;

	private static MealConfig mealConfig = SimulationConfig.instance().getMealConfiguration(); 


	/**
	 * Constructor.
	 *
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	// multiple times
	public Cooking(Building building, FunctionSpec spec) {
		// Use Function constructor.
		super(FunctionType.COOKING, spec, building);

		cookedMeals = new CopyOnWriteArrayList<>();

		cookingWorkTime = 0D;

		this.cookCapacity = spec.getCapacity();

		// need this to pass maven test
		cleaningAgentPerSol = mealConfig.getCleaningAgentPerSol();
		dryMassPerServing = mealConfig.getDryMassPerServing();

		qualityMap = new HashMap<>();
	}

	/**
	 * Rerturn a map of the best and worse meals cooked today
	 * @return
	 */
	public Map<String, MealStats> getQualityMap() {
		return qualityMap;
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

		// Demand is 1 cooking capacity for every five inhabitants.
		double demand = settlement.getNumCitizens() / 5D;

		double supply = 0D;
		boolean removedBuilding = false;
		Iterator<Building> i = settlement.getBuildingManager().getBuildingSet(FunctionType.COOKING).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				Cooking cookingFunction = building.getCooking();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += cookingFunction.cookCapacity * wearModifier;
			}
		}

		double cookingCapacityValue = demand / (supply + 1D);
		double cookingCapacity = buildingConfig.getFunctionSpec(buildingName, FunctionType.COOKING).getCapacity();
		return cookingCapacity * cookingCapacityValue;
	}

	/**
	 * Get the maximum number of cooks supported by this facility.
	 *
	 * @return max number of cooks
	 */
	public int getCookCapacity() {
		return cookCapacity;
	}

	/**
	 * Get the current number of cooks using this facility.
	 *
	 * @return number of cooks
	 */
	public int getNumCooks() {
		int result = 0;

		for (Person p : getBuilding().getLifeSupport().getOccupants()) {
			if (p.getMind().getTaskManager().getTask() instanceof CookMeal) {
				result++;
			}
		}

		// Officiate Chefbot's contribution as cook
		for (Robot r : getBuilding().getRoboticStation().getRobotOccupants()) {
			if (r.getBotMind().getBotTaskManager().getTask() instanceof CookMeal) {
				result++;
			}
		}
		return result;
	}

	/**
	 * Checks if there are any cooked meals in this facility.
	 *
	 * @return true if cooked meals
	 */
	public boolean hasCookedMeal() {
		return ((cookedMeals != null) && !cookedMeals.isEmpty());
	}

	/**
	 * Gets the number of cooked meals in this facility.
	 *
	 * @return number of meals
	 */
	public int getNumberOfAvailableCookedMeals() {
		return cookedMeals.size();
	}

	public int getTotalNumberOfCookedMealsToday() {
		return mealCounterPerSol;
	}

	/**
	 * Eats a cooked meal from this facility.
	 *
	 * @return the meal
	 */
	public CookedMeal chooseAMeal(Person person) {
		CookedMeal bestFavDish = null;
		CookedMeal bestMeal = null;
		double bestQuality = -1;
		Favorite fav = person.getFavorite();
		String mainDish = fav.getFavoriteMainDish();
		String sideDish = fav.getFavoriteSideDish();

		for (CookedMeal m : cookedMeals) {
			// Note: define how a person will choose to eat a main dish and/or side dish
			String n = m.getName();
			double q = m.getQuality();
			if (n.equals(mainDish)) {
				// person will choose the main dish
				if (q > bestQuality) {
					// save the one with the best quality
					bestQuality = q;
					bestFavDish = m;
					cookedMeals.remove(bestFavDish);
					return bestFavDish;
				}
			}

			else if (n.equals(sideDish)) {
				// person will choose side dish
				if (q > bestQuality) {
					// save the one with the best quality
					bestQuality = q;
					bestFavDish = m;
					cookedMeals.remove(bestFavDish);
					return bestFavDish;
				}
			}

			else if (q > bestQuality) {
				// not his/her fav but still save the one with the best quality
				bestQuality = q;
				bestMeal = m;
			}

			else {
				// not his/her fav but still save the one with the best quality
				bestQuality = q;
				bestMeal = m;
			}
		}

		if (bestMeal != null) {
			// a person will eat the best quality meal
			cookedMeals.remove(bestMeal);
		}

		return bestMeal;
	}

	/**
	 * Gets the quality of the best quality meal at the facility.
	 *
	 * @return quality
	 */
	public double getBestMealQuality() {

		return cookedMeals.stream()
				.mapToDouble(CookedMeal::getQuality)
				.max()
				.orElse(0D);
	}

	/**
	 * Finishes up cooking.
	 */
	private void finishUp() {
		cookingWorkTime = 0D;
		cookNoMore = false;
	}

	/**
	 * Checks if there should be no more cooking at this kitchen during this meal
	 * time.
	 *
	 * @return true if no more cooking.
	 */
	public boolean getCookNoMore() {
		return cookNoMore;
	}

	private int getPopulation() {
		return building.getSettlement().getIndoorPeopleCount();
	}

	/**
	 * Adds cooking work to this facility. The amount of work is dependent upon the
	 * person's cooking skill.
	 *
	 * @param workTime work time (millisols)
	 */
	// Called by CookMeal
	public String addWork(double workTime, Worker theCook) {

		String nameOfMeal = null;

		cookingWorkTime += workTime;

		if ((cookingWorkTime >= COOKED_MEAL_WORK_REQUIRED) && (!cookNoMore)) {

			double population = getPopulation();
			double maxServings = population * building.getSettlement().getMealsReplenishmentRate();

			int totalServings = getTotalAvailableCookedMealsAtSettlement();
			if (totalServings >= maxServings) {
				cookNoMore = true;
			}

			else {
				// Randomly pick a meal which ingredients are available
				HotMeal aMeal = getACookableMeal();
				if (aMeal != null) {
					nameOfMeal = cookAHotMeal(aMeal, theCook);
				}
			}
		}

		return nameOfMeal;
	}

	/**
	 * Gets the total number of available cooked meals at a settlement.
	 *
	 * @param settlement the settlement.
	 * @return number of cooked meals.
	 */
	private int getTotalAvailableCookedMealsAtSettlement() {
		return building.getSettlement().getBuildingManager().getBuildingSet(FunctionType.COOKING).stream()
				.mapToInt(b -> b.getCooking().getNumberOfAvailableCookedMeals())
				.sum();
	}

	/**
	 * Randomly picks a hot meal with its ingredients fully available.
	 *
	 * @return a hot meal or null if none available.
	 */
	private HotMeal getACookableMeal() {
		var s = getBuilding().getSettlement();
		return mealConfig.getDishList().stream()
						.filter(m -> m.isIngredientsAvailable(s))
						.findAny().orElse(null);
	}

	/**
	 * Can this Kitchen cook any meals from available ingredients ?
	 * 
	 * @return
	 */
	public boolean canCookMeal() {

        // Check if there are enough ingredients to cook a meal.
		// Need to reset numGoodRecipes periodically since it's a cache value
		// and won't get updated unless a meal is cooked.
		// Note: it's reset at least once a day at the end of a sol
        if (!hasCookableMeal && (RandomUtil.getRandomInt(5) == 0)) {
        	resetCookableMeals();
        }

        return hasCookableMeal;
	}

	/**
	 * Tests if at least one meal is cookable with the current ingredient store.
	 */
	private void resetCookableMeals() {
		// Find the first meal with all ingredients
		Optional<HotMeal> found = mealConfig.getDishList().stream()
				.filter(i -> i.isIngredientsAvailable(building.getSettlement()))
				.findFirst();

		hasCookableMeal = found.isPresent();
	}

	/**
	 * Cooks a hot meal by retrieving ingredients.
	 *
	 * @param hotMeal the meal to cook.
	 * @return name of meal
	 */
	public String cookAHotMeal(HotMeal hotMeal, Worker theCook) {
		Settlement s = getBuilding().getSettlement();
		double mealQuality = hotMeal.retrieveIngredients(s);

		double culinarySkillPerf = 0;
		// Add influence of a person/robot's performance on meal quality

		culinarySkillPerf = .25 * theCook.getPerformanceRating()
					* theCook.getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);

		// consume oil
		if ((hotMeal.getOil() > 0) && consumeOil(hotMeal.getOil(), s)) {
			mealQuality = mealQuality + .2;
		}

		mealQuality = Math.round((mealQuality + culinarySkillPerf + cleanliness) * 10D) / 15D;

		// consume salt
		s.retrieveAmountResource(ResourceUtil.TABLE_SALT_ID, hotMeal.getSalt());

		// consume water
		consumeWater();

		String nameOfMeal = hotMeal.getMealName();

		MarsTime currentTime = masterClock.getMarsTime();
		CookedMeal meal = new CookedMeal(nameOfMeal, mealQuality, dryMassPerServing,
										currentTime);
		cookedMeals.add(meal);
		mealCounterPerSol++;

		// See if there are other meals available
		resetCookableMeals();

		// Add to Qualtity record
		var currentQuality = qualityMap.get(nameOfMeal);
		if (currentQuality == null) {
			qualityMap.put(nameOfMeal, new MealStats(mealQuality));
		} else {
			// If this meal has been cooked before, update the range
			currentQuality.addMeal(mealQuality);
		}

		cookingWorkTime -= COOKED_MEAL_WORK_REQUIRED;
		// Reduce a tiny bit of kitchen's cleanliness upon every meal made
		cleanliness = cleanliness - .0075;

		return nameOfMeal;
	}

	/**
	 * Consumes a certain amount of water for each meal.
	 */
	private void consumeWater() {
		int sign = RandomUtil.getRandomInt(0, 1);
		double rand = RandomUtil.getRandomDouble(0.2);
		double usage;
		if (sign == 0)
			usage = 1 + rand;
		else
			usage = 1 - rand;

		// If settlement is rationing water, reduce water usage according to its level
		var s = building.getSettlement();
		int level = s.getWaterRationLevel();
		if (level != 0)
			usage = usage / 1.5D / level;
		if (usage > MIN) {
			s.retrieveAmountResource(ResourceUtil.WATER_ID, usage);
			s.addWaterConsumption(WaterUseType.PREP_MEAL, usage);
		}
		double wasteWaterAmount = usage * .75;
		if (wasteWaterAmount > 0)
			store(wasteWaterAmount, ResourceUtil.GREY_WATER_ID, "Cooking::consumeWater");
	}

	/**
	 * Consumes oil.
	 *
	 * @param oilRequired
	 * @return
	 */
	private static boolean consumeOil(double oilRequired, Settlement s) {
		var oil = ResourceUtil.getOilResources().stream()
						.filter(o -> s.getAmountResourceStored(o) > oilRequired)
						.findAny()
						.orElse(-1);
		if (oil != -1) {
			s.retrieveAmountResource(oil, oilRequired);
			return true;
		}
		// oil is not available
		else {
			logger.log(s, Level.FINE, 30_000, "No oil is available.");
		}

		return false;
	}

	/**
	 * Gets the quantity of one serving of meal.
	 *
	 * @return quantity
	 */
	public double getMassPerServing() {
		return dryMassPerServing;
	}

	/**
	 * Gets a list of cooked meals.
	 *
	 * @return cookedMeals
	 */
	public List<CookedMeal> getCookedMealList() {
		return cookedMeals;
	}

	/**
	 * Time passing for the Cooking function in a building.
	 *
	 * @param time amount of time passing (in millisols)
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
			if (hasCookedMeal()) {
				double rate = building.getSettlement().getMealsReplenishmentRate();

				MarsTime now = masterClock.getMarsTime();
				var expired = cookedMeals.stream()
						.filter(meal -> meal.getExpirationTime().getTimeDiff(now) < 0D)
						.toList();

				// Handle expired cooked meals.
				for (CookedMeal meal : expired) {
					// Note: turn this into a task
					cookedMeals.remove(meal);

					// Check if cooked meal has gone bad and has to be thrown out.
					double quality = meal.getQuality() / 2D + 1D;
					double qNum = RandomUtil.getRandomDouble(7 * quality + 1);
					StringBuilder log = new StringBuilder();

					if (qNum < 1) {
						if (dryMassPerServing > 0)
							// Turn into food waste
							store(dryMassPerServing, ResourceUtil.FOOD_WASTE_ID, "Cooking::timePassing");

						log.append(dryMassPerServing)
								.append(" kg ").append(meal.getName()).append(DISCARDED);

						logger.log(building, Level.FINE, 10_000, log.toString());

					} else {
						// Convert the meal into preserved food.
						preserveFood();

						log.append(CONVERTING)
								.append(dryMassPerServing).append(" kg ").append(meal.getName())
								.append(PRESERVED);

						logger.log(building, Level.FINE, 10_000, log.toString());
					}

					// Adjust the rate to go down for each meal that wasn't eaten.
					if (rate > 0) {
						rate -= DOWN;
					}
					building.getSettlement().setMealsReplenishmentRate(rate);
				}
			}

			// Check if not meal time, clean up.
			if (!CookMeal.isMealTime(building.getSettlement(), 0)) {
				finishUp();
			}

			// Check for the end of the day
			if (pulse.isNewSol()) {
				doEndOfDay();
			}
		}
		return valid;
	}

	/**
	 * Checks in as the end of the day and empty map caches.
	 */
	private void doEndOfDay() {
		// Adjust the rate to go up automatically by default
		double rate = building.getSettlement().getMealsReplenishmentRate();
		rate += UP;
		building.getSettlement().setMealsReplenishmentRate(rate);
		// reset back to zero at the beginning of a new day.
		mealCounterPerSol = 0;

		qualityMap.clear();

		// NOTE: turn this into a task a person can do
		cleanUpKitchen();
	}

	/**
	 * Gets the cleanliness score.
	 * 
	 * @return
	 */
	public double getCleanliness() {
		return cleanliness;
	}
	
	/**
	 * Cleans up the kitchen with cleaning agent and water.
	 * NOTE: turn this into a task that a person should do
	 */
	private void cleanUpKitchen() {
		var s = building.getSettlement();
		double amountAgent = cleaningAgentPerSol;		 
		double lackingAgent = s.retrieveAmountResource(ResourceUtil.NACLO_ID, amountAgent);

		double amountWater = 10 * amountAgent;
		double lackingWater = s.retrieveAmountResource(ResourceUtil.WATER_ID, amountWater);
		
		// Track water consumption
		s.addWaterConsumption(WaterUseType.CLEAN_MEAL, amountWater - lackingWater);
		
		// Modify cleanliness
		if (lackingAgent <= 0)
			cleanliness = cleanliness + .1;
		else
			cleanliness = cleanliness - .1;

		if (lackingWater <= 0)
			cleanliness = cleanliness + .05;
		else
			cleanliness = cleanliness - .05;

		if (cleanliness > 1)
			cleanliness = 1;
		else if (cleanliness < -1)
			cleanliness = -1;
	}

	/**
	 * Preserves the food with salts.
	 */
	private void preserveFood() {
		// Note: turn this into a task
		building.getSettlement().retrieveAmountResource(ResourceUtil.TABLE_SALT_ID, AMOUNT_OF_SALT_PER_MEAL);
		if (dryMassPerServing > 0)
			store(dryMassPerServing, ResourceUtil.FOOD_ID, "Cooking::preserveFood");
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 *
	 * @return power (kW)
	 */
	@Override
	public double getCombinedPowerLoad() {
		return getNumCooks() * 10D;
	}

	@Override
	public double getMaintenanceTime() {
		return cookCapacity * 2.5D;
	}

	public boolean isFull() {
		return (getNumCooks() >= getCookCapacity());
	}

	@Override
	public void destroy() {
		super.destroy();
		cookedMeals = null;
		qualityMap = null;
	}
}

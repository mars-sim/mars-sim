/*
 * Mars Simulation Project
 * Cooking.java
 * @date 2023-04-18
 * @author Scott Davis
 */
package com.mars_sim.core.building.function.cooking;

import java.io.Serializable;
import java.util.HashMap;
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
import com.mars_sim.core.building.function.cooking.task.CookMeal;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
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
	 * Statistics of a prepared Dish.
	 */
	public static final class DishStats implements Serializable {
		/** default serial id. */
		private static final long serialVersionUID = 1L;
		
		private double bestQuality;
		private double worseQuality;
		private int number;

		DishStats(double quality) {
			number = 1;
			this.bestQuality = quality;
			this.worseQuality = quality;
		}

		private DishStats(int number, double worseQuality, double bestQuality) {
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

		private void addDish(double quality) {
			number++;
			if (quality < worseQuality) {
				worseQuality = quality;
			} else if (quality > bestQuality) {
				bestQuality = quality;
			}
		}

		/**
		 * Merges two MealStats objects summing the number of meals and takes the worst & best of the 2.
		 * 
		 * @param a
		 * @param b
		 * @return
		 */
		public static DishStats sum(DishStats a, DishStats b) {
			return new DishStats(a.number + b.number, Math.min(a.worseQuality, b.worseQuality),
								Math.max(a.bestQuality, b.bestQuality));
		}
	}

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Cooking.class.getName());

	private static final String CONVERTING = "A dish had expired. Converting ";
	private static final String DISCARDED = " was expired and discarded.";
	private static final String PRESERVED = " into thermo-stabilized/preserved food.";

	public static final double AMOUNT_OF_SALT_PER_MEAL = 0.005D;
	public static final double AMOUNT_OF_OIL_PER_MEAL = 0.01D;
	/**  The base amount of work time (cooking skill 0) to produce one single cooked meal.*/
	private static final double COOKED_MEAL_WORK_REQUIRED = 8D;
	// Note : 10 millisols is 15 mins
	/** The capacity of the water holding tank in kg. */
	private static final double WATER_TANK_CAPACITY = 5.0;
	/** The capacity of the water holding tank in kg. */
	private static final double WASTE_WATER_TANK_CAPACITY = 5.0;
	private static final double UP = 0.01;
	private static final double DOWN = 0.007;

	private boolean cookNoMore = false;

	/** The cache for msols */
	private int cookCapacity;
	
	private int mealCounterPerSol = 0;
	/** The amount of waste water in the holding tank. */
	private double wasteWaterTank;
	/** The amount of water in the holding tank. */
	private double waterHoldingTank;
	// Dynamically adjusted the rate of generating meals
	private double cleaningAgentPerSol;
	/** Cleanliness score between -1 and 1. */
	private double cleanliness;
	private double cookingWorkTime;
	private double dryMassPerServing;

	// Data members
	private List<PreparedDish> availableDishes;

	/** The quality history of each meal.  */
	private Map<String, DishStats> qualityMap;

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

		availableDishes = new CopyOnWriteArrayList<>();

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
	public Map<String, DishStats> getQualityMap() {
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
		for(Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.COOKING)) {
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
		var rSta = getBuilding().getRoboticStation();
		if (rSta != null) {
			for (Robot r : rSta.getRobotOccupants()) {
				if (r.getBotMind().getBotTaskManager().getTask() instanceof CookMeal) {
					result++;
				}
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
		return (!availableDishes.isEmpty());
	}

	/**
	 * Gets the number of cooked meals in this facility.
	 *
	 * @return number of meals
	 */
	public int getNumberOfAvailableCookedMeals() {
		return availableDishes.size();
	}

	public int getTotalNumberOfCookedMealsToday() {
		return mealCounterPerSol;
	}

	/**
	 * Eats a cooked meal from this facility.
	 *
	 * @return the meal
	 */
	public PreparedDish chooseAMeal(Person person) {
		PreparedDish bestMeal = null;
		double bestQuality = Integer.MIN_VALUE;
		var favourites = person.getFavorite().getFavoriteDishes();

		for (PreparedDish m : availableDishes) {
			String n = m.getName();
			double q = m.getQuality();

			if (favourites.contains(n)) {
				// this is a favourite meal
				bestMeal = m;
				break;
			}
			else if (q > bestQuality) {
				// save the one with the best quality
				bestQuality = q;
				bestMeal = m;
			}
		}

		if (bestMeal != null) {
			// a person will eat the best quality meal
			availableDishes.remove(bestMeal);
		}

		return bestMeal;
	}

	/**
	 * Gets the quality of the best quality meal at the facility.
	 *
	 * @return quality
	 */
	public double getBestMealQuality() {

		return availableDishes.stream()
				.mapToDouble(PreparedDish::getQuality)
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

	public static final int getSettlementMealShortfall(Settlement s) {
		// Force rounding up so at least one meal will be need if anyone is inside
		int requiredMeals = (int)Math.ceil(s.getIndoorPeopleCount() * s.getMealsReplenishmentRate());
		int availableMeals = s.getBuildingManager().getBuildingSet(FunctionType.COOKING).stream()
				.mapToInt(b -> b.getCooking().getNumberOfAvailableCookedMeals())
				.sum();
		
		return (requiredMeals - availableMeals);
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
		var s = building.getSettlement();
		cookingWorkTime += workTime;

		if ((cookingWorkTime >= COOKED_MEAL_WORK_REQUIRED) && (!cookNoMore)) {

			cookNoMore = (getSettlementMealShortfall(s) <= 0);
			if (!cookNoMore) {
				// Randomly pick a meal which ingredients are available
				DishRecipe aMeal = getACookableMeal();
				if (aMeal != null) {
					nameOfMeal = cookAHotMeal(aMeal, theCook);
				}
			}
		}

		return nameOfMeal;
	}

	/**
	 * Randomly picks a hot meal with its ingredients fully available.
	 *
	 * @return a hot meal or null if none available.
	 */
	private DishRecipe getACookableMeal() {
		var s = getBuilding().getSettlement();
		return mealConfig.getDishList().stream()
						.filter(m -> m.isIngredientsAvailable(s))
						.findAny().orElse(null);
	}

	/**
	 * Tests if at least one meal is cookable with the current ingredient store at a Settlement
	 */
	public static boolean hasMealIngredients(Settlement s) {
		// Find the first meal with all ingredients
		Optional<DishRecipe> found = mealConfig.getDishList().stream()
				.filter(i -> i.isIngredientsAvailable(s))
				.findFirst();

		return found.isPresent();
	}

	/**
	 * Cooks a hot meal by retrieving ingredients.
	 *
	 * @param hotMeal the meal to cook.
	 * @return name of meal
	 */
	private String cookAHotMeal(DishRecipe hotMeal, Worker theCook) {
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

		String nameOfMeal = hotMeal.getName();

		MarsTime currentTime = masterClock.getMarsTime();
		PreparedDish meal = new PreparedDish(nameOfMeal, mealQuality, dryMassPerServing,
										currentTime);
		availableDishes.add(meal);
		mealCounterPerSol++;

		// Add to Qualtity record
		var currentQuality = qualityMap.get(nameOfMeal);
		if (currentQuality == null) {
			qualityMap.put(nameOfMeal, new DishStats(mealQuality));
		} else {
			// If this meal has been cooked before, update the range
			currentQuality.addDish(mealQuality);
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
		double waterUsage = RandomUtil.getRandomDouble(0.1, 0.2);

		// If settlement is rationing water, reduce water usage according to its level
		var s = building.getSettlement();
		int level = s.getRationing().getRationingLevel();
		if (level != 0)
			waterUsage = waterUsage / 1.5D / level;
		
		waterUsage = retrieveFromTank(ResourceUtil.WATER_ID, s, waterHoldingTank, waterUsage, WATER_TANK_CAPACITY);
		
		double wasteWaterAmount = waterUsage * .75;
		
		storeToTank(ResourceUtil.GREY_WATER_ID, s, wasteWaterTank, wasteWaterAmount, WASTE_WATER_TANK_CAPACITY);
		
	}
	
	/**
	 * Retrieves a resource from a holding tank.
	 * 
	 * @param resource
	 * @param s
	 * @param tank
	 * @param consuming
	 * @param cap
	 * @return the amount it can take
	 */
	public double retrieveFromTank(int resource, Settlement s, double tank, double consuming, double cap) {
		
		double canConsume = 0;
				
		if (consuming <= tank) {
			// Consume it from the tank
			tank = tank - consuming;
			
			canConsume = consuming;
			
			s.addWaterConsumption(WaterUseType.PREP_MEAL, canConsume);
		}
		else {
			// Note: this way, it won't have to call retrieveAmountResource() excessively
			double shortfall = s.retrieveAmountResource(resource, cap);
			double available = cap - shortfall + tank;
			
			if (available >= consuming) {
				// the resource holder has enough to fill up the tank	
				canConsume = consuming;
						
				tank = available - canConsume;
				// Not enough resource 
				s.addWaterConsumption(WaterUseType.PREP_MEAL, canConsume);
			}
			else {
				// the resource holder doesn't have enough to fill up the tank	
				canConsume = available;
				
				tank = 0;
				// Has enough resource
				s.addWaterConsumption(WaterUseType.PREP_MEAL, canConsume);
			}
		}
		
		waterHoldingTank = tank;
		
		return canConsume;
	}
	
	
	/**
	 * Stores a resource to a holding tank.
	 * 
	 * @param resource
	 * @param s
	 * @param tank
	 * @param storing
	 * @param cap
	 * @return canStore
	 */
	public double storeToTank(int resource, Settlement s, double tank, double storing, double cap) {
	
		double canStore = tank + storing;

		if (canStore >= cap) {
			// Drain the whole tank
			// Note: this way, it won't have to call storeAmountResource() excessively
			double excess = s.storeAmountResource(resource, canStore);
			// Update what can be stored
			canStore = canStore - excess;
			
			tank = excess;
			
			if (tank >= cap) {
				logger.log(s, Level.WARNING, 30_000, getBuilding() + " - The tank for " + ResourceUtil.findAmountResourceName(resource)
					+ " was already full at " + Math.round(cap) + ".");
			}
			
			s.addWaterConsumption(WaterUseType.PREP_MEAL, storing);
		}
		
		else {
			
			tank = canStore;
			
			s.addWaterConsumption(WaterUseType.PREP_MEAL, storing);
		}

		wasteWaterTank = tank;
		
		return canStore;
	}

	/**
	 * Consumes oil.
	 *
	 * @param oilRequired
	 * @return
	 */
	private static boolean consumeOil(double oilRequired, Settlement s) {
		var oil = ResourceUtil.getOilResources().stream()
						.filter(o -> s.getSpecificAmountResourceStored(o) > oilRequired)
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
	public List<PreparedDish> getCookedMealList() {
		return availableDishes;
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

				MarsTime now = pulse.getMarsTime();
				var expired = availableDishes.stream()
						.filter(meal -> meal.getExpirationTime().getTimeDiff(now) < 0D)
						.toList();

				// Handle expired cooked meals.
				for (PreparedDish meal : expired) {
					// Note: turn this into a task
					availableDishes.remove(meal);

					// Check if cooked meal has gone bad and has to be thrown out.
					double quality = meal.getQuality() / 2D + 1D;
					double qNum = RandomUtil.getRandomDouble(7 * quality + 1);
					StringBuilder log = new StringBuilder();

					if (qNum < 1) {
						if (dryMassPerServing > 0)
							// Turn low quality food into food waste
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

						logger.log(building, Level.INFO, 20_000, log.toString());
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
		availableDishes = null;
		qualityMap = null;
	}
}

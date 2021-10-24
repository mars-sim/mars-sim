/*
 * Mars Simulation Project
 * PreparingDessert.java
 * @date 2021-10-21
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.PrepareDessert;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.WaterUseType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The PreparingDessert class is a building function for making dessert.
 */
public class PreparingDessert extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(PreparingDessert.class.getName());
	/** The minimal amount of resource to be retrieved. */
	private static final double MIN = 0.00001;

	public static final String REFRIGERATE = "A dessert has expired. Refigerating ";

	public static final String DISCARDED = " is expired and discarded at ";

	public static final String GREY_WATER = "grey water";
	public static final String FOOD_WASTE = "food waste";
	public static final String WATER = "water";
	public static final String SODIUM_HYPOCHLORITE = "sodium hypochlorite";
	/**
	 * The base amount of work time in milliSols (for cooking skill 0) to prepare
	 * fresh dessert .
	 */
	public static final double PREPARE_DESSERT_WORK_REQUIRED = 3D;

	// public double dessertsReplenishmentRate;
	public static double UP = 0.01;
	public static double DOWN = 0.007;

	// SERVING_FRACTION also used in GoodsManager
	public static final int NUM_OF_DESSERT_PER_SOL = 4;
	// DESSERT_SERVING_FRACTION is used in every mission expedition
	public static final double DESSERT_SERVING_FRACTION = .5D;
	// amount of water in kg per dessert during preparation and clean-up
	public static final double WATER_USAGE_PER_DESSERT = .5;

	private static double dessertMassPerServing;

	private static String[] availableDesserts = { "sesame milk", 
			"soymilk", "sugarcane juice", "cranberry juice",
			"strawberry", "granola bar", "blueberry muffin"};

	private static int NUM_DESSERTS = availableDesserts.length;

	private static int waterID = ResourceUtil.waterID;
//	private static int greyWaterID = ResourceUtil.greyWaterID;
	private static int foodWasteID = ResourceUtil.foodWasteID;
	public static int NaClOID = ResourceUtil.NaClOID;

	public static int[] availableDessertsID = {
			ResourceUtil.findIDbyAmountResourceName(availableDesserts[0]),
			ResourceUtil.findIDbyAmountResourceName(availableDesserts[1]),
			ResourceUtil.findIDbyAmountResourceName(availableDesserts[2]),
			ResourceUtil.findIDbyAmountResourceName(availableDesserts[3]),
			ResourceUtil.findIDbyAmountResourceName(availableDesserts[4]),
			ResourceUtil.findIDbyAmountResourceName(availableDesserts[5]),
			ResourceUtil.findIDbyAmountResourceName(availableDesserts[6])
	};
	
	
	public static AmountResource[] availableDessertsAR = { 
			ResourceUtil.findAmountResource(availableDesserts[0]),
			ResourceUtil.findAmountResource(availableDesserts[1]),
			ResourceUtil.findAmountResource(availableDesserts[2]),
			ResourceUtil.findAmountResource(availableDesserts[3]),
			ResourceUtil.findAmountResource(availableDesserts[4]),
			ResourceUtil.findAmountResource(availableDesserts[5]),
			ResourceUtil.findAmountResource(availableDesserts[6])
	};

	// Arbitrary percent of dry mass of the corresponding dessert/beverage.
	public static double[] dryMass = { 0.10, 0.05, 0.02, 0.02, 0.20, 0.4, 0.3, };
	
	private boolean makeNoMoreDessert = false;

	private int cookCapacity; // used in timePassing

	private int dessertCounterPerSol = 0;

	private double preparingWorkTime;

	private double bestQualityCache = 0;

	private double cleanliness = 0;

	private double cleaningAgentPerSol;

	private List<PreparedDessert> servingsOfDessert;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public PreparingDessert(Building building) {
		// Use Function constructor but uses COOKING as the configuration type
		super(FunctionType.PREPARING_DESSERT, FunctionType.COOKING, building);

		dessertMassPerServing = personConfig.getDessertConsumptionRate() / (double) NUM_OF_DESSERT_PER_SOL
				* DESSERT_SERVING_FRACTION;

		MealConfig mealConfig = SimulationConfig.instance().getMealConfiguration(); // need this to pass maven test
		// Add loading the two parameters from meals.xml
		cleaningAgentPerSol = mealConfig.getCleaningAgentPerSol();
		// waterUsagePerMeal = mealConfig.getWaterConsumptionRate();

		preparingWorkTime = 0D;
		servingsOfDessert = new CopyOnWriteArrayList<>();

		this.cookCapacity = buildingConfig.getFunctionCapacity(building.getBuildingType(), FunctionType.COOKING);
	}

	public static String[] getArrayOfDesserts() {
		return availableDesserts;
	}

	public static AmountResource[] getArrayOfDessertsAR() {
		return availableDessertsAR;
	}

	public static String convertAR2String(AmountResource dessertAR) {
		for (AmountResource ar : availableDessertsAR) {
			if (ar.getName().equals(dessertAR.getName()))
				return dessertAR.getName();
		}
		return null;
	}

	/**
	 * Convert the name of a desert to Amount Resource
	 * 
	 * @param dessert
	 * @return
	 */
	public static AmountResource convertString2AR(String dessert) {
		for (String s : availableDesserts) {
			if (dessert.equals(s)) {
				return availableDessertsAR[s.indexOf(dessert)];
			}
		}
		return null;
	}

//    public void setChef(String name) {
//    	this.producerName = name;
//    }

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingType the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	// TODO: make the demand for dessert user-selectable
	public static double getFunctionValue(String buildingType, boolean newBuilding, Settlement settlement) {

		// TODO: calibrate this demand
		// Demand is 1 for every 5 inhabitants.
		double demand = settlement.getNumCitizens() / 5D;

		double supply = 0D;
		boolean removedBuilding = false;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.PREPARING_DESSERT).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingType) && !removedBuilding) {
				removedBuilding = true;
			} else {
				// PreparingDessert preparingDessertFunction = (PreparingDessert)
				// building.getFunction(FUNCTION);
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .25D + .25D;
				supply += building.getPreparingDessert().cookCapacity * wearModifier;
			}
		}

		double preparingDessertCapacityValue = demand / (supply + 1D);

		double preparingDessertCapacity = buildingConfig.getFunctionCapacity(buildingType, FunctionType.COOKING);

		return preparingDessertCapacity * preparingDessertCapacityValue;
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

		if (getBuilding().hasFunction(FunctionType.LIFE_SUPPORT)) {
			LifeSupport lifeSupport = building.getLifeSupport();
			Iterator<Person> i = lifeSupport.getOccupants().iterator();
			while (i.hasNext()) {
				Task task = i.next().getMind().getTaskManager().getTask();
				if (task instanceof PrepareDessert) {
					result++;
				}
			}
		}

		return result;
	}

	/**
	 * Checks if there are any FreshDessertList in this facility.
	 * 
	 * @return true if yes
	 */
	public boolean hasFreshDessert() {
		return (getAvailableServingsDesserts() > 0);
	}

	/**
	 * Gets the number of cups of fresh dessert in this facility.
	 * 
	 * @return number of servingsOfDessertList
	 */
	public int getAvailableServingsDesserts() {
		return servingsOfDessert.size();
	}

	/**
	 * Gets a dessert from this facility.
	 * 
	 * @return PreparedDessert
	 */
	public PreparedDessert chooseADessert(Person person) {
		List<PreparedDessert> menu = new CopyOnWriteArrayList<>();// servingsOfDessert);
		PreparedDessert bestDessert = null;
		PreparedDessert bestFavDessert = null;
		double bestQuality = -1;
		String favoriteDessert = person.getFavorite().getFavoriteDessert();

		double thirst = person.getPhysicalCondition().getThirst();

		if (thirst > 100) {
			for (PreparedDessert d : servingsOfDessert) {
				if (d.getName().contains("juice") || d.getName().contains("milk"))
					menu.add(d);
			}
		} else
			menu = servingsOfDessert;

		Iterator<PreparedDessert> i = menu.iterator();
		while (i.hasNext()) {
			PreparedDessert d = i.next();
			double q = d.getQuality();
			if (d.getName().equals(favoriteDessert)) {
				// person will choose his/her favorite dessert right away
				if (q > bestQuality) {
					// if (q > currentBestQuality) {
					// currentBestQuality = q;
					// bestQuality = q;
					// }
					bestQuality = q;
					bestFavDessert = d;
					menu.remove(bestFavDessert);
					return bestFavDessert;
				}
			}

			else if (q > bestQuality) {
				bestQuality = q;
				bestDessert = d;
				// pick this by breaking
				break;
			}

			else {
				bestQuality = q;
				bestDessert = d;
			}
		}

		if (bestDessert != null) {
			menu.remove(bestDessert);
		}

		return bestDessert;
	}

	/**
	 * Gets the quantity of one serving of dessert
	 * 
	 * @return quantity
	 */
	public static double getDessertMassPerServing() {
		return dessertMassPerServing;
	}

	/**
	 * Gets the quality of the best quality fresh Dessert at the facility.
	 * 
	 * @return quality
	 */
	public double getBestDessertQuality() {
		double bestQuality = 0;
		// Question: do we want to remember the best quality ever or just the best
		// quality among the current servings ?
		Iterator<PreparedDessert> i = servingsOfDessert.iterator();
		while (i.hasNext()) {
			// PreparedDessert freshDessert = i.next();
			// if (freshDessert.getQuality() > bestQuality)
			// bestQuality = freshDessert.getQuality();
			double q = i.next().getQuality();
			if (q > bestQuality)
				bestQuality = q;
		}

		if (bestQuality > bestQualityCache)
			bestQualityCache = bestQuality;

		return bestQuality;
	}

	public double getBestDessertQualityCache() {
		getBestDessertQuality();
		return bestQualityCache;
	}

	public int getPopulation() {
		return building.getSettlement().getIndoorPeopleCount();
	}

	/**
	 * Finishes up preparing dessert
	 */
	private void finishUp() {
		preparingWorkTime = 0D;
		makeNoMoreDessert = false;
	}

	/**
	 * Cleans up the kitchen with cleaning agent and water.
	 */
	private void cleanUpKitchen() {
		boolean cleaning0 = false;
		if (cleaningAgentPerSol * .1 > MIN)
			cleaning0 = retrieve(cleaningAgentPerSol * .1, NaClOID, true);
		boolean cleaning1 = false;
		if (cleaningAgentPerSol > MIN) {
			cleaning1 = retrieve(cleaningAgentPerSol * 5, waterID, true);
			building.getSettlement().addWaterConsumption(WaterUseType.CLEAN_DESSERT, cleaningAgentPerSol * 5);
		}

		if (cleaning0)
			cleanliness = cleanliness + .05;
		else
			cleanliness = cleanliness - .025;

		if (cleaning1)
			cleanliness = cleanliness + .075;
		else
			cleanliness = cleanliness - .05;

		if (cleanliness > 1)
			cleanliness = 1;
		else if (cleanliness < -1)
			cleanliness = -1;

	}

	/**
	 * Check if no more dessert needs to be prepared during this meal time.
	 * 
	 * @return true if no more dessert is needed.
	 */
	public boolean getMakeNoMoreDessert() {
		return makeNoMoreDessert;
	}

	/**
	 * Gets a list of all desserts that can be made at the settlement.
	 * 
	 * @return list of dessert names.
	 */
	public List<String> getListDessertsToMake() {
		// Note : turn this list into an array to speed up the operation
		List<String> dessertList = new ArrayList<>();

		// Put together a list of available dessert
		for (int i = 0; i < NUM_DESSERTS; i++) {
			double amount = dryMass[i];

			boolean isAvailable = false;
			if (amount > MIN)
				isAvailable = retrieve(amount, availableDessertsAR[i].getID(), false);
			boolean isWater_av = false;
			if (dessertMassPerServing - amount > MIN)
				isWater_av = retrieve(dessertMassPerServing - amount, waterID, false);

			if (isAvailable && isWater_av) {
				dessertList.add(availableDesserts[i]);
			}
		}

		return dessertList;
	}

	/**
	 * Get a random dessert from a list of desserts.
	 * 
	 * @param dessertList the dessert list to randomly choose from.
	 * @return random dessert name or null if no desserts available.
	 */
	public static String getADessert(List<String> dessertList) {
		String selectedDessert = null;

		if (dessertList.size() > 0) {
			int index = RandomUtil.getRandomInt(dessertList.size() - 1);
			selectedDessert = dessertList.get(index);
		}

		return selectedDessert;
	}

	/**
	 * Adds work to this facility. The amount of work is dependent upon the person's
	 * skill.
	 * 
	 * @param workTime work time (millisols)
	 */
	public String addWork(double workTime, Worker worker) {
		String selectedDessert = null;

		preparingWorkTime += workTime;

		if ((preparingWorkTime >= PREPARE_DESSERT_WORK_REQUIRED) && !makeNoMoreDessert) {

			// max allowable # of dessert servings per meal time.
			double population = building.getSettlement().getIndoorPeopleCount();
			double maxServings = population * building.getSettlement().getDessertsReplenishmentRate();

			int numServings = getTotalAvailablePreparedDessertsAtSettlement(building.getSettlement());

			if (numServings >= maxServings) {
				makeNoMoreDessert = true;
			} else {
				// List<String> dessertList = getAListOfDesserts();
				// selectedDessert = makeADessert(getADessert(dessertList));
				selectedDessert = makeADessert(getADessert(getListDessertsToMake()), worker);
			}
		}

		return Conversion.capitalize(selectedDessert);
	}

	/**
	 * Gets the total number of available prepared desserts at a settlement.
	 * 
	 * @param settlement the settlement.
	 * @return number of prepared desserts.
	 */
	private int getTotalAvailablePreparedDessertsAtSettlement(Settlement settlement) {

		int result = 0;

		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.PREPARING_DESSERT).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			PreparingDessert kitchen = building.getPreparingDessert();
			result += kitchen.getAvailableServingsDesserts();
		}

		return result;
	}

	/**
	 * Gets the dry mass of a dessert
	 */
	public static double getDryMass(String selectedDessert) {
		double result = 0;

		for (int i = 0; i < availableDesserts.length; i++) {
			if (availableDesserts[i].equals(selectedDessert)) {
				return dryMass[i];
			}
		}
		return result;
	}

	private String makeADessert(String selectedDessert, Worker worker) {

		// Take out one serving of the selected dessert from storage.
		double dryMass = getDryMass(selectedDessert);

		if (selectedDessert == null) {
			// System.out.println("PreparingDessert : selectedDessert is " +
			// selectedDessert);
			return null;
		}

		else {
			if (dryMass > MIN) {
				retrieve(dryMass, ResourceUtil.findIDbyAmountResourceName(selectedDessert), true);
			}

			if (dessertMassPerServing - dryMass > MIN) {
				retrieve(dessertMassPerServing - dryMass, waterID, true);
				building.getSettlement().addWaterConsumption(WaterUseType.PREP_DESSERT, dessertMassPerServing - dryMass);
			}

			double dessertQuality = 0;
			// TODO: quality also dependent upon the hygiene of a person
			double culinarySkillPerf = 0;
			// Add influence of a person/robot's performance on meal quality
			culinarySkillPerf = .25 * worker.getPerformanceRating()
						* worker.getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);

			dessertQuality = Math.round((dessertQuality + culinarySkillPerf + cleanliness) * 10D) / 10D;
			;

			// Create a serving of dessert and add it into the list
			servingsOfDessert.add(new PreparedDessert(selectedDessert, dessertQuality, dessertMassPerServing,
					(MarsClock) marsClock.clone(), worker.getName(), this));

			// consumeWater();
			dessertCounterPerSol++;

			preparingWorkTime -= PREPARE_DESSERT_WORK_REQUIRED;

			// Reduce a tiny bit of kitchen's cleanliness upon every meal made
			cleanliness = cleanliness - .0075;

			return selectedDessert;
		}

	}


	/**
	 * Time passing for the building.
	 *
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
			if (hasFreshDessert()) {
				double rate = building.getSettlement().getDessertsReplenishmentRate();
	
				// Handle expired prepared desserts.
				Iterator<PreparedDessert> i = servingsOfDessert.iterator();
				while (i.hasNext()) {
	
					PreparedDessert dessert = i.next();
	
					if (MarsClock.getTimeDiff(dessert.getExpirationTime(), pulse.getMarsTime()) < 0D) {
						servingsOfDessert.remove(dessert);
	
						// Check if prepared dessert has gone bad and has to be thrown out.
						double quality = dessert.getQuality() / 2D + 1D;
						double num = RandomUtil.getRandomDouble(8 * quality);
						StringBuilder log = new StringBuilder();
	
						if (num < 1) {
							// Throw out bad dessert as food waste.
							double m = getDryMass(dessert.getName());
							if (m > MIN)
								store(m, foodWasteID, "PreparingDessert::timePassing");
	
							log.append(getDryMass(dessert.getName())).append(" kg ")
									.append(dessert.getName()).append(DISCARDED);
	
							logger.log(building, Level.INFO, 10000, log.toString());
	
						} else {
							// Refrigerate prepared dessert.
							refrigerateFood(dessert);
	
							log.append(REFRIGERATE).append(getDryMass(dessert.getName())).append(" kg ")
									.append(dessert.getName());
	
							logger.log(building, Level.INFO, 10000, log.toString());
						}
	
						// Adjust the rate to go down for each dessert that wasn't eaten.
						if (rate > 0) {
							rate -= DOWN;
						}
						building.getSettlement().setDessertsReplenishmentRate(rate);
					}
				}
			}
	
			// Check if not meal time, clean up.
			Coordinates location = building.getSettlement().getCoordinates();
			if (!CookMeal.isLocalMealTime(location, 10)) {
				finishUp();
			}
	
			if (pulse.isNewSol()) {
				doEndOfDay();
			}
		}	
		return valid;
	}

	private synchronized void doEndOfDay() {

		// Sanity check for the passing of each day
		double rate = building.getSettlement().getDessertsReplenishmentRate();

		// reset back to zero at the beginning of a new day.
		dessertCounterPerSol = 0;
		// Adjust this rate to go up automatically by default
		rate += UP;
		building.getSettlement().setDessertsReplenishmentRate(rate);

		cleanUpKitchen();
	}

	/**
	 * Refrigerate prepared dessert so it doesn't go bad.
	 * 
	 * @param dessert the dessert to refrigerate.
	 */
	public void refrigerateFood(PreparedDessert dessert) {
		String dessertName = dessert.getName();
		try {
			double mass = getDryMass(dessertName);
			store(mass, ResourceUtil.findIDbyAmountResourceName(dessertName), "PreparingDessert::refrigerateFood");

		} catch (Exception e) {
          	logger.log(Level.SEVERE, "Cannot store " + dessertName + ": "+ e.getMessage());
		}
	}

	public int getTotalServingsOfDessertsToday() {
		return dessertCounterPerSol;
	}



	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return getNumCooks() * 10D;
	}

	/**
	 * Checks if this resource id is a dessert id
	 * 
	 * @param id
	 * @return
	 */
	public static boolean isADessert(int id) {
		for (int i: availableDessertsID) {
			if (id == i)
				return true;
		}
		return false;
	}
	
	@Override
	public double getMaintenanceTime() {
		return cookCapacity * 10D;
	}

	@Override
	public void destroy() {
		super.destroy();

		servingsOfDessert = null;
		availableDessertsAR = null;
	}
}

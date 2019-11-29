/**
 * Mars Simulation Project
 * PreparingDessert.java
 * @version 3.1.0 2017-03-03
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.PrepareDessert;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.Storage;
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
	private static Logger logger = Logger.getLogger(PreparingDessert.class.getName());
	/** The minimal amount of resource to be retrieved. */
	private static final double MIN = 0.00001;

	private static String sourceName = logger.getName();

	private static final FunctionType FUNCTION = FunctionType.PREPARING_DESSERT;

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
	private static int greyWaterID = ResourceUtil.greyWaterID;
	private static int foodWasteID = ResourceUtil.foodWasteID;
	public static int NaClOID = ResourceUtil.NaClOID;

	public static AmountResource[] availableDessertsAR = { 
			ResourceUtil.findAmountResource(availableDesserts[0]),
			ResourceUtil.findAmountResource(availableDesserts[1]),
			ResourceUtil.findAmountResource(availableDesserts[2]),
			ResourceUtil.findAmountResource(availableDesserts[3]),
			ResourceUtil.findAmountResource(availableDesserts[4]),
			ResourceUtil.findAmountResource(availableDesserts[5]),
			ResourceUtil.findAmountResource(availableDesserts[6]) };

	// Arbitrary percent of dry mass of the corresponding dessert/beverage.
	public static double[] dryMass = { 0.10, 0.05, 0.02, 0.02, 0.20, 0.4, 0.3, };

	private boolean makeNoMoreDessert = false;

	/** The cache for msols */
	private int msolCache;

	private int solCache = 1;

	private int cookCapacity; // used in timePassing

	private int dessertCounterPerSol = 0;

	private double preparingWorkTime;

	private double bestQualityCache = 0;

	private double cleanliness = 0;

	private double cleaningAgentPerSol;

	private String producerName;

	private Building building;
	private Person person;
	private Robot robot;

	private List<PreparedDessert> servingsOfDessert;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public PreparingDessert(Building building) {
		// Use Function constructor.
		super(FUNCTION, building);
		this.building = building;

		dessertMassPerServing = personConfig.getDessertConsumptionRate() / (double) NUM_OF_DESSERT_PER_SOL
				* DESSERT_SERVING_FRACTION;

		MealConfig mealConfig = SimulationConfig.instance().getMealConfiguration(); // need this to pass maven test
		// Add loading the two parameters from meals.xml
		cleaningAgentPerSol = mealConfig.getCleaningAgentPerSol();
		// waterUsagePerMeal = mealConfig.getWaterConsumptionRate();

		preparingWorkTime = 0D;
		servingsOfDessert = new CopyOnWriteArrayList<>();

		this.cookCapacity = buildingConfig.getCookCapacity(building.getBuildingType());

		// Load activity spots
		loadActivitySpots(buildingConfig.getCookingActivitySpots(building.getBuildingType()));
	}

	public Inventory getInventory() {
		return building.getInventory();
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
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
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

		double preparingDessertCapacity = buildingConfig.getCookCapacity(buildingType);

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
			try {
				LifeSupport lifeSupport = building.getLifeSupport();
				Iterator<Person> i = lifeSupport.getOccupants().iterator();
				while (i.hasNext()) {
					Task task = i.next().getMind().getTaskManager().getTask();
					if (task instanceof PrepareDessert) {
						result++;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

//    /**
//     * Gets the skill level of the best cook using this facility.
//     * @return skill level.
//
//    public int getBestDessertSkill() {
//        int result = 0;
//
//        if (getBuilding().hasFunction(BuildingFunction.LIFE_SUPPORT)) {
//            try {
//                LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(BuildingFunction.LIFE_SUPPORT);
//                Iterator<Person> i = lifeSupport.getOccupants().iterator();
//                while (i.hasNext()) {
//                    Person person = i.next();
//                    Task task = person.getMind().getTaskManager().getTask();
//                    if (task instanceof CookMeal) {
//                        int preparingDessertSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);
//                        if (preparingDessertSkill > result) {
//                            result = preparingDessertSkill;
//                        }
//                    }
//                }
//            }
//            catch (Exception e) {
//            	e.printStackTrace();
//            }
//        }
//
//        return result;
//    }
//     */

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
		List<PreparedDessert> menu = new ArrayList<>();// servingsOfDessert);
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
	public void finishUp() {
		preparingWorkTime = 0D;
		makeNoMoreDessert = false;
	}

	/**
	 * Cleans up the kitchen with cleaning agent and water.
	 */
	public void cleanUpKitchen() {
		boolean cleaning0 = false;
		if (cleaningAgentPerSol * .1 > MIN)
			cleaning0 = retrieve(cleaningAgentPerSol * .1, NaClOID, true);
		boolean cleaning1 = false;
		if (cleaningAgentPerSol > MIN) {
			cleaning1 = retrieve(cleaningAgentPerSol * 5, waterID, true);
			building.getSettlement().addWaterConsumption(3, cleaningAgentPerSol * 5);
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
	 * Gets a list of all desserts available at the settlement.
	 * 
	 * @return list of dessert names.
	 */
	public List<String> getAListOfDesserts() {
		// TODO : turn this list into an array to speed up the operation
		List<String> dessertList = new CopyOnWriteArrayList<>(); // ArrayList<String>();

		// Put together a list of available dessert
		// for(String n : availableDesserts) {
		for (int i = 0; i < NUM_DESSERTS; i++) {
			double amount = dryMass[i];
			/// System.out.println("PreparingDessert : it's " + availableDesserts[i]);
			boolean isAvailable = false;
			if (amount > MIN)
				isAvailable = retrieve(amount, availableDessertsAR[i].getID(), false);
			boolean isWater_av = false;
			if (dessertMassPerServing - amount > MIN)
				isWater_av = retrieve(dessertMassPerServing - amount, waterID, false);

			if (isAvailable && isWater_av) {
				// System.out.println("n is available");
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
	public String addWork(double workTime, Unit theCook) {
		if (theCook instanceof Person)
			this.person = (Person) theCook;
		else if (theCook instanceof Robot)
			this.robot = (Robot) theCook;

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
				selectedDessert = makeADessert(getADessert(getAListOfDesserts()));
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

		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
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

	public String makeADessert(String selectedDessert) {

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
				building.getSettlement().addWaterConsumption(1, dessertMassPerServing - dryMass);
			}

			double dessertQuality = 0;
			// TODO: quality also dependent upon the hygiene of a person
			double culinarySkillPerf = 0;
			// Add influence of a person/robot's performance on meal quality
			if (person != null)
				culinarySkillPerf = .25 * person.getPerformanceRating()
						* person.getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);
			else if (robot != null)
				culinarySkillPerf = .1 * robot.getPerformanceRating()
						* robot.getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);

			dessertQuality = Math.round((dessertQuality + culinarySkillPerf + cleanliness) * 10D) / 10D;
			;

			if (person != null)
				producerName = person.getName();
			else if (robot != null)
				producerName = robot.getName();

			// Create a serving of dessert and add it into the list
			servingsOfDessert.add(new PreparedDessert(selectedDessert, dessertQuality, dessertMassPerServing,
					(MarsClock) marsClock.clone(), producerName, this));

			// consumeWater();
			dessertCounterPerSol++;

			// logger.info(producerName + " prepared a serving of " + selectedDessert
			// + " in " + getBuilding().getBuildingManager().getSettlement().getName()
			// + " (dessert quality : " + dessertQuality + ")");

			preparingWorkTime -= PREPARE_DESSERT_WORK_REQUIRED;

			// Reduce a tiny bit of kitchen's cleanliness upon every meal made
			cleanliness = cleanliness - .0075;

			return selectedDessert;
		}

	}

//    /**
//     * Consumes a certain amount of water for each dessert
//     */
//    public void consumeWater() {
//    	int sign = RandomUtil.getRandomInt(0, 1);
//    	double rand = RandomUtil.getRandomDouble(0.2);
//    	double usage = WATER_USAGE_PER_DESSERT;
//    	//TODO: need to move the hardcoded amount to a xml file
//    	if (sign == 0)
//    		usage = 1 + rand;
//    	else
//    		usage = 1 - rand;
//    	if (usage > MIN) {
//    		Storage.retrieveAnResource(usage, waterID, inv, true);
//        	settlement.addWaterConsumption(1, usage);	
//    	}
//    	
//		double wasteWaterAmount = usage * .5;
//		if (wasteWaterAmount > MIN)
//			Storage.storeAnResource(wasteWaterAmount, greyWaterID, inv, sourceName + "::consumeWater");
//    }

	/**
	 * Time passing for the building.
	 *
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {

		int msol = marsClock.getMillisolInt();

		if (msolCache != msol) {
			msolCache = msol;

			if (hasFreshDessert()) {
				double rate = building.getSettlement().getDessertsReplenishmentRate();

				// Handle expired prepared desserts.
				Iterator<PreparedDessert> i = servingsOfDessert.iterator();
				while (i.hasNext()) {

					PreparedDessert dessert = i.next();
					// MarsClock currentTime =
					// Simulation.instance().getMasterClock().getMarsClock();

					if (MarsClock.getTimeDiff(dessert.getExpirationTime(), marsClock) < 0D) {
						try {
							servingsOfDessert.remove(dessert);

							// Check if prepared dessert has gone bad and has to be thrown out.
							double quality = dessert.getQuality() / 2D + 1D;
							double num = RandomUtil.getRandomDouble(8 * quality);
							StringBuilder log = new StringBuilder();

							if (num < 1) {
								// Throw out bad dessert as food waste.
								double m = getDryMass(dessert.getName());
								if (m > MIN)
									store(m, foodWasteID, sourceName + "::timePassing");

								log.append("[").append(building.getSettlement().getName()).append("] ")
										.append(getDryMass(dessert.getName())).append(" kg ")
										.append(dessert.getName().toLowerCase()).append(DISCARDED)
										.append(getBuilding().getNickName()).append(".");

								LogConsolidated.log(Level.INFO, 10000, sourceName, log.toString());

							} else {
								// Refrigerate prepared dessert.
								refrigerateFood(dessert);

								log.append("[").append(building.getSettlement().getName()).append("] ")
										.append(REFRIGERATE).append(getDryMass(dessert.getName())).append(" kg ")
										.append(dessert.getName().toLowerCase()).append(" at ")
										.append(getBuilding().getNickName()).append(".");

								LogConsolidated.log(Level.INFO, 10000, sourceName, log.toString());

								// logger.finest("The dessert has lost its freshness at " +
								// getBuilding().getBuildingManager().getSettlement().getName());
							}

							// Adjust the rate to go down for each dessert that wasn't eaten.
							if (rate > 0) {
								rate -= DOWN;
							}
							building.getSettlement().setDessertsReplenishmentRate(rate);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}

			// Check if not meal time, clean up.
			Coordinates location = building.getSettlement().getCoordinates();
			if (!CookMeal.isLocalMealTime(location, 10)) {
				finishUp();
			}

			checkEndOfDay();
		}
	}

	public synchronized void checkEndOfDay() {
		// MarsClock currentTime =
		// Simulation.instance().getMasterClock().getMarsClock();
		if (marsClock == null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock(); // needed for loading a saved sim
		// Sanity check for the passing of each day
		int newSol = marsClock.getSolOfMonth();
		double rate = building.getSettlement().getDessertsReplenishmentRate();

		if (newSol != solCache) {
			solCache = newSol;
			// reset back to zero at the beginning of a new day.
			dessertCounterPerSol = 0;
			// Adjust this rate to go up automatically by default
			rate += UP;
			building.getSettlement().setDessertsReplenishmentRate(rate);

			cleanUpKitchen();
		}
	}

	/**
	 * Refrigerate prepared dessert so it doesn't go bad.
	 * 
	 * @param dessert the dessert to refrigerate.
	 */
	public void refrigerateFood(PreparedDessert dessert) {
		try {
			String dessertName = dessert.getName();
			double mass = getDryMass(dessertName);
			store(mass, ResourceUtil.findIDbyAmountResourceName(dessertName), sourceName + "::refrigerateFood");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getTotalServingsOfDessertsToday() {
		return dessertCounterPerSol;
	}

//    /**
//     * Gets the amount resource of the fresh food from a specified food group.
//     *
//     * @param String food group
//     * @return AmountResource of the specified fresh food
//
//    public AmountResource getFreshFoodAR(String foodGroup) {
//        AmountResource freshFoodAR = AmountResource.findAmountResource(foodGroup);
//        return freshFoodAR;
//    }

//    /**
//     * Computes amount of fresh food from a particular fresh food amount resource.
//     *
//     * @param AmountResource of a particular fresh food
//     * @return Amount of a particular fresh food in kg, rounded to the 4th decimal places
//
//    public double getFreshFood(AmountResource ar) {
//        double freshFoodAvailable = inv.getAmountResourceStored(ar, false);
//    	//inv.addDemandTotalRequest(ar);
//        return freshFoodAvailable;
//    }

	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return getNumCooks() * 10D;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * 
	 * @return power (kW)
	 */
	public double getPoweredDownPowerRequired() {
		return 0;
	}

	@Override
	public double getMaintenanceTime() {
		return cookCapacity * 10D;
	}

	@Override
	public double getFullHeatRequired() {
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		return 0;
	}

	public boolean retrieve(double amount, int resource, boolean value) {
		return Storage.retrieveAnResource(amount, resource, building.getInventory(), value);
	}

	public void store(double amount, int resource, String source) {
		Storage.storeAnResource(amount, resource, building.getInventory(), source);
	}

	@Override
	public void destroy() {
		super.destroy();

		person = null;
		robot = null;
		marsClock = null;
		building = null;
		servingsOfDessert = null;
		availableDessertsAR = null;
	}
}
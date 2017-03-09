/**
 * Mars Simulation Project
 * PreparingDessert.java
 * @version 3.1.0 2017-03-03
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.PrepareDessert;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.AmountResourceConfig;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.Conversion;

/**
 * The PreparingDessert class is a building function for making dessert.
 */
//2014-11-28 Changed Class name from MakingSoy to PreparingDessert
public class PreparingDessert
extends Function
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(PreparingDessert.class.getName());

    private static final BuildingFunction FUNCTION = BuildingFunction.PREPARING_DESSERT;

    /** The base amount of work time in milliSols (for cooking skill 0)
     * to prepare fresh dessert . */
    public static final double PREPARE_DESSERT_WORK_REQUIRED = 2D;

    // 2015-01-12 Dynamically adjusted the rate of generating desserts
    //public double dessertsReplenishmentRate;
    public static double UP = 0.01;
    public static double DOWN = 0.007;

    //  SERVING_FRACTION also used in GoodsManager
    public static final int NUM_OF_DESSERT_PER_SOL = 4;
    // DESSERT_SERVING_FRACTION is used in every mission expedition
    public static final double DESSERT_SERVING_FRACTION = .5D;
    // amount of water in kg per dessert during preparation and clean-up
    public static final double WATER_USAGE_PER_DESSERT = 1.0;

    private static double dessertMassPerServing;

    // 2015-01-03 Added availableDesserts
    private static String [] availableDesserts =
    	{
    		"soymilk",
			"sugarcane juice",
			"strawberry",
			"granola bar",
			"blueberry muffin",
			"cranberry juice"
		};

    private static int NUM_DESSERTS = availableDesserts.length;

    public static AmountResource [] availableDessertsAR =
    	{
	    	AmountResource.findAmountResource("soymilk"),
			AmountResource.findAmountResource("sugarcane juice"),
			AmountResource.findAmountResource("strawberry"),
			AmountResource.findAmountResource("granola bar"),
			AmountResource.findAmountResource("blueberry muffin"),
			AmountResource.findAmountResource("cranberry juice")
		};

	// TODO: get the real world figure on each serving
    // arbitrary percent of dry mass of the corresponding dessert/beverage.
    public static double [] dryMass =
    	{
    		0.3,
			0.02,
			0.5,
			0.8,
			0.5,
			0.02
		};


    private boolean makeNoMoreDessert = false;

	private int dessertCounterPerSol = 0;
	private int solCache = 1;
    private int cookCapacity;
    private int NumOfServingsCache; // used in timePassing

	private double preparingWorkTime; // used in numerous places

    private String producerName;


    private Building building;
    private Settlement settlement;
    private Inventory inv ;

    private List<PreparedDessert> servingsOfDessertList;



    /**
     * Constructor.
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    public PreparingDessert(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);
        this.building = building;

        inv = getBuilding().getBuildingManager().getSettlement().getInventory();

        settlement = getBuilding().getBuildingManager().getSettlement();

        PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
        dessertMassPerServing = personConfig.getDessertConsumptionRate() / (double) NUM_OF_DESSERT_PER_SOL * DESSERT_SERVING_FRACTION;

        preparingWorkTime = 0D;
        servingsOfDessertList = new CopyOnWriteArrayList<>();

        BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration();

        this.cookCapacity = buildingConfig.getCookCapacity(building.getBuildingType());

        // Load activity spots
        loadActivitySpots(buildingConfig.getCookingActivitySpots(building.getBuildingType()));

    }

    public Inventory getInventory() {
    	return inv;
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
    	//for (int i= 0 ; i <NUM_DESSERTS; i++) {
    	//	if (availableDessertsAR[i] == dessertAR)
    	//		return availableDesserts[i];
    	//}
    	return null;
    }


    public static AmountResource convertString2AR(String dessert) {
    	for (String s : availableDesserts) {
    		if (dessert.equals(s)) {
    			return availableDessertsAR[s.indexOf(dessert)];
    		}
    	}
    	//for (int i= 0 ; i <NUM_DESSERTS; i++) {
    	//	if (dessert.equals(availableDesserts[i]))
    	//		return availableDessertsAR[i];
    	//}
    	return null;
    }


    // 2015-01-12 Added setChef()
    public void setChef(String name) {
    	this.producerName = name;
    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingType the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    //TODO: make the demand for dessert user-selectable
    public static double getFunctionValue(String buildingType, boolean newBuilding,
            Settlement settlement) {

        // TODO: calibrate this demand
    	// Demand is 1 for every 5 inhabitants.
        double demand = settlement.getAllAssociatedPeople().size() / 5D;

        double supply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingType) && !removedBuilding) {
                removedBuilding = true;
            }
            else {
                PreparingDessert preparingDessertFunction = (PreparingDessert) building.getFunction(FUNCTION);
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .25D + .25D;
                supply += preparingDessertFunction.cookCapacity * wearModifier;
            }
        }

        double preparingDessertCapacityValue = demand / (supply + 1D);

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        double preparingDessertCapacity = config.getCookCapacity(buildingType);

        return preparingDessertCapacity * preparingDessertCapacityValue;
    }

    /**
     * Get the maximum number of cooks supported by this facility.
     * @return max number of cooks
     */
    public int getCookCapacity() {
        return cookCapacity;
    }

    /**
     * Get the current number of cooks using this facility.
     * @return number of cooks
     */
    public int getNumCooks() {
        int result = 0;

        if (getBuilding().hasFunction(BuildingFunction.LIFE_SUPPORT)) {
            try {
                LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(BuildingFunction.LIFE_SUPPORT);
                Iterator<Person> i = lifeSupport.getOccupants().iterator();
                while (i.hasNext()) {
                    Task task = i.next().getMind().getTaskManager().getTask();
                    if (task instanceof PrepareDessert) {
                        result++;
                    }
                }
            }
            catch (Exception e) {}
        }

        return result;
    }

    /**
     * Gets the skill level of the best cook using this facility.
     * @return skill level.
     */
    public int getBestDessertSkill() {
        int result = 0;

        if (getBuilding().hasFunction(BuildingFunction.LIFE_SUPPORT)) {
            try {
                LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(BuildingFunction.LIFE_SUPPORT);
                Iterator<Person> i = lifeSupport.getOccupants().iterator();
                while (i.hasNext()) {
                    Person person = i.next();
                    Task task = person.getMind().getTaskManager().getTask();
                    if (task instanceof CookMeal) {
                        int preparingDessertSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);
                        if (preparingDessertSkill > result) {
                            result = preparingDessertSkill;
                        }
                    }
                }
            }
            catch (Exception e) {}
        }

        return result;
    }

    /**
     * Checks if there are any FreshDessertList in this facility.
     * @return true if yes
     */
    public boolean hasFreshDessert() {
        return (getAvailableServingsDesserts() > 0);
    }

    /**
     * Gets the number of cups of fresh dessert in this facility.
     * @return number of servingsOfDessertList
     */
    public int getAvailableServingsDesserts() {
        return servingsOfDessertList.size();
    }

    /**
     * Gets a dessert from this facility.
     * @return PreparedDessert
     */
    public PreparedDessert chooseADessert(Person person) {
        PreparedDessert bestDessert = null;
        PreparedDessert bestFavDessert = null;
        int bestQuality = -1;
        String favoriteDessert = person.getFavorite().getFavoriteDessert();

        Iterator<PreparedDessert> i = servingsOfDessertList.iterator();
        while (i.hasNext()) {
            PreparedDessert freshDessert = i.next();
            if (freshDessert.getName().equals(favoriteDessert)) {
                if (freshDessert.getQuality() > bestQuality) {
                    bestQuality = freshDessert.getQuality();
                    bestFavDessert = freshDessert;
                }
            }

            else if (freshDessert.getQuality() > bestQuality) {
                bestQuality = freshDessert.getQuality();
                bestDessert = freshDessert;
            }
        }

        if (bestFavDessert != null) {
            servingsOfDessertList.remove(bestFavDessert);
        }
        else if (bestDessert != null) {
            servingsOfDessertList.remove(bestDessert);
        }

        return bestDessert;
    }

    /**
     * Gets the quantity of one serving of dessert
     * @return quantity
     */
    public static double getDessertMassPerServing() {
       return dessertMassPerServing;
    }

    /**
     * Gets the quality of the best quality fresh Dessert at the facility.
     * @return quality
     */
    public int getBestDessertQuality() {
        int bestQuality = 0;
        Iterator<PreparedDessert> i = servingsOfDessertList.iterator();
        while (i.hasNext()) {
            PreparedDessert freshDessert = i.next();
            if (freshDessert.getQuality() > bestQuality) bestQuality = freshDessert.getQuality();
        }

        return bestQuality;
    }

 	public int getPopulation() {
        return getBuilding().getBuildingManager().getSettlement().getCurrentPopulationNum();
 	}

    /**
     * Cleanup kitchen after eating.
     */
    public void cleanUp() {
    	preparingWorkTime = 0D;
        makeNoMoreDessert = false;
    }


    /**
     * Check if no more dessert needs to be prepared during this meal time.
     * @return true if no more dessert is needed.
     */
 	public boolean getMakeNoMoreDessert() {
 		return makeNoMoreDessert;
 	}


 	/**
 	 * Gets a list of all desserts available at the settlement.
 	 * @return list of dessert names.
 	 */
 	public List<String> getAListOfDesserts() {

    	List<String> dessertList = new CopyOnWriteArrayList<>(); //ArrayList<String>();

	  	// Put together a list of available dessert
        for(String n : availableDesserts) {
        	double amount = getDryMass(n);
        	boolean isAvailable = Storage.retrieveAnResource(amount, n, inv, false);

        	if (isAvailable) {
        		dessertList.add(n);
        	}
        }

		return dessertList;
 	}

	/**
	 * Get a random dessert from a list of desserts.
	 * @param dessertList the dessert list to randomly choose from.
	 * @return random dessert name or "None" if no desserts available.
	 */
 	public static String getADessert(List<String> dessertList) {
    	String selectedDessert = "None";

    	if (dessertList.size() > 0) {
    	    int index = RandomUtil.getRandomInt(dessertList.size() - 1);
    	    selectedDessert = dessertList.get(index);
    	}

		return selectedDessert;
 	}

    /**
     * Adds work to this facility.
     * The amount of work is dependent upon the person's skill.
     * @param workTime work time (millisols)
      */
    public String addWork(double workTime) {
    	String selectedDessert = null;

    	preparingWorkTime += workTime;

    	if ((preparingWorkTime >= PREPARE_DESSERT_WORK_REQUIRED) && !makeNoMoreDessert) {

    	    // max allowable # of dessert servings per meal time.
	        double population = building.getBuildingManager().getSettlement().getCurrentPopulationNum();
	        double maxServings = population * settlement.getDessertsReplenishmentRate();

	        int numServings = getTotalAvailablePreparedDessertsAtSettlement(settlement);

	        if (numServings >= maxServings) {
	        	makeNoMoreDessert = true;
	        }
	        else {
	        	List<String> dessertList = getAListOfDesserts();
	        	selectedDessert = makeADessert(getADessert(dessertList));
	        }
    	}

    	return Conversion.capitalize(selectedDessert);
    }

    /**
     * Gets the total number of available prepared desserts at a settlement.
     * @param settlement the settlement.
     * @return number of prepared desserts.
     */
    private int getTotalAvailablePreparedDessertsAtSettlement(Settlement settlement) {

        int result = 0;

        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            PreparingDessert kitchen = (PreparingDessert) building.getFunction(BuildingFunction.PREPARING_DESSERT);
            result += kitchen.getAvailableServingsDesserts();
        }

        return result;
    }

    /**
     * Gets the dry mass of a dessert
     */
    public static double getDryMass(String selectedDessert) {
    	double result = 0;

    	for(int i = 0; i <availableDesserts.length; i++) {
        	if (availableDesserts[i].equals(selectedDessert) ) {
        		return dryMass[i];
        	}
        }
    	return result;
    }


    public String makeADessert(String selectedDessert) {

    	// Take out one serving of the selected dessert from storage.
        double dryMass = getDryMass(selectedDessert);
        Storage.retrieveAnResource(dryMass, selectedDessert, inv, true);

        MarsClock time = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
        // TODO: quality also dependent upon the hygiene of a person
        int dessertQuality = getBestDessertSkill();

        // Create a serving of dessert and add it into the list
	    servingsOfDessertList.add(new PreparedDessert(selectedDessert, dessertQuality, dryMass, time, producerName, this));

	    useWater();

	    dessertCounterPerSol++;
	    logger.fine("addWork() : new dessert just prepared : " + selectedDessert);

	    preparingWorkTime -= PREPARE_DESSERT_WORK_REQUIRED;

	    return selectedDessert;
    }

    // 2015-01-28 Added useWater()
    public void useWater() {
    	//TODO: need to move the hardcoded amount to a xml file
    	Storage.retrieveAnResource(WATER_USAGE_PER_DESSERT, AmountResource.waterAR, inv, true);
		double wasteWaterAmount = WATER_USAGE_PER_DESSERT * .95;
		Storage.storeAnResource(wasteWaterAmount, AmountResource.greyWaterAR, inv);
    }


    /**
     * Time passing for the building.
     *
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     */
    public void timePassing(double time) {

        if (hasFreshDessert()) {
            double rate = settlement.getDessertsReplenishmentRate();

            // Handle expired prepared desserts.
            Iterator<PreparedDessert> i = servingsOfDessertList.iterator();
            while (i.hasNext()) {

                PreparedDessert dessert = i.next();
                MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();

                if (MarsClock.getTimeDiff(dessert.getExpirationTime(), currentTime) < 0D) {
                    try {
                        servingsOfDessertList.remove(dessert);

                        // Check if prepared dessert has gone bad and has to be thrown out.
                        double quality = dessert.getQuality() / 2D + 1D;
                        double num = RandomUtil.getRandomDouble(8 * quality);
                        if (num < 1) {
                            // Throw out bad dessert as food waste.
                            Storage.storeAnResource(getDryMass(dessert.getName()), AmountResource.foodWasteAR, inv);
                            logger.finest(getDryMass(dessert.getName()) + " kg "
                                    + dessert.getName()
                                    + " expired, turned bad and discarded at " + getBuilding().getNickName()
                                    + " in " + settlement.getName()
                                    );
                        }
                        else  {
                            // Refrigerate prepared dessert.
                            refrigerateFood(dessert);
                            logger.finest("Dessert Expired. Refrigerate "
                                    + getDryMass(dessert.getName()) + " kg "
                                    + dessert.getName()
                                    +  " at " + getBuilding().getNickName()
                                    + " in " + settlement.getName()
                                    );
                            logger.finest("The dessert has lost its freshness at " +
                                    getBuilding().getBuildingManager().getSettlement().getName());
                        }

                        // Adjust the rate to go down for each dessert that wasn't eaten.
                        if (rate > 0 ) {
                            rate -= DOWN;
                        }
                        settlement.setDessertsReplenishmentRate(rate);
                    }
                    catch (Exception e) {}
                }
            }
        }

        // Check if not meal time, clean up.
        Coordinates location = getBuilding().getBuildingManager().getSettlement().getCoordinates();
        if (!CookMeal.isMealTime(location)) {
            cleanUp();
        }

        checkEndOfDay();
    }

    // 2015-01-12 Added checkEndOfDay()
  	public synchronized void checkEndOfDay() {

		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
		// Added 2015-01-04 : Sanity check for the passing of each day
		int newSol = currentTime.getSolOfMonth();
	    double rate = settlement.getDessertsReplenishmentRate();

		if (newSol != solCache) {
			solCache = newSol;
	        // reset back to zero at the beginning of a new day.
	    	dessertCounterPerSol = 0;
			// 2015-01-12 Adjust this rate to go up automatically by default
	    	rate += UP;
	      	settlement.setDessertsReplenishmentRate(rate);
		}
  	}

	/**
	 * Refrigerate prepared dessert so it doesn't go bad.
	 * @param dessert the dessert to refrigerate.
	 */
	public void refrigerateFood(PreparedDessert dessert) {
		try {
			String dessertName = dessert.getName();
		    double mass = getDryMass(dessertName)  ;
            Storage.storeAnResource(mass, dessertName , inv);

		} catch (Exception e) {}
	}

    public int getTotalServingsOfDessertsToday() {
        return dessertCounterPerSol;
    }

    /**
     * Gets the amount resource of the fresh food from a specified food group.
     *
     * @param String food group
     * @return AmountResource of the specified fresh food
     */
    public AmountResource getFreshFoodAR(String foodGroup) {
        AmountResource freshFoodAR = AmountResource.findAmountResource(foodGroup);
        return freshFoodAR;
    }

    /**
     * Computes amount of fresh food from a particular fresh food amount resource.
     *
     * @param AmountResource of a particular fresh food
     * @return Amount of a particular fresh food in kg, rounded to the 4th decimal places
     */
    public double getFreshFood(AmountResource ar) {
        double freshFoodAvailable = inv.getAmountResourceStored(ar, false);
    	// 2015-01-09 Added addDemandTotalRequest()
    	//inv.addDemandTotalRequest(ar);
        return freshFoodAvailable;
    }

    /**
     * Gets the amount of power required when function is at full power.
     * @return power (kW)
     */
    public double getFullPowerRequired() {
        return getNumCooks() * 10D;
    }

    /**
     * Gets the amount of power required when function is at power down level.
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
    public void destroy() {
        super.destroy();

        building = null;
        inv = null;
        settlement = null;
        servingsOfDessertList.clear();
        servingsOfDessertList = null;
    }

	@Override
	public double getFullHeatRequired() {
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		return 0;
	}
}
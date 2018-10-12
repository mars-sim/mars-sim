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
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.PrepareDessert;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;

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

    private static String sourceName = logger.getName();
    
    private static final FunctionType FUNCTION = FunctionType.PREPARING_DESSERT;

    public static final String REFRIGERATE = "A dessert has expired. Refigerating ";
    
    public static final String DISCARDED = " is expired and discarded at ";
    
	public static final String GREY_WATER = "grey water";
    public static final String FOOD_WASTE = "food waste";
	public static final String WATER = "water";
    public static final String SODIUM_HYPOCHLORITE = "sodium hypochlorite";
    /** The base amount of work time in milliSols (for cooking skill 0)
     * to prepare fresh dessert . */
    public static final double PREPARE_DESSERT_WORK_REQUIRED = 3D;

    // 2015-01-12 Dynamically adjusted the rate of generating desserts
    //public double dessertsReplenishmentRate;
    public static double UP = 0.01;
    public static double DOWN = 0.007;

    //  SERVING_FRACTION also used in GoodsManager
    public static final int NUM_OF_DESSERT_PER_SOL = 4;
    // DESSERT_SERVING_FRACTION is used in every mission expedition
    public static final double DESSERT_SERVING_FRACTION = .5D;
    // amount of water in kg per dessert during preparation and clean-up
    public static final double WATER_USAGE_PER_DESSERT = .5;

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

	private static AmountResource waterAR = ResourceUtil.waterAR;
    private static AmountResource greyWaterAR = ResourceUtil.greyWaterAR;
    private static AmountResource foodWasteAR = ResourceUtil.foodWasteAR;
    public static AmountResource NaClOAR = ResourceUtil.NaClOAR;

    public static AmountResource [] availableDessertsAR =
    	{
	    	ResourceUtil.findAmountResource("soymilk"),
	    	ResourceUtil.findAmountResource("sugarcane juice"),
	    	ResourceUtil.findAmountResource("strawberry"),
	    	ResourceUtil.findAmountResource("granola bar"),
	    	ResourceUtil.findAmountResource("blueberry muffin"),
	    	ResourceUtil.findAmountResource("cranberry juice")
		};

	// TODO: get the real world figure on each serving
    // arbitrary percent of dry mass of the corresponding dessert/beverage.
    public static double [] dryMass =
    	{
    		0.05,
			0.02,
			0.15,
			0.3,
			0.3,
			0.02
		};


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
	
	//private double waterUsagePerMeal;

    private String producerName;

    private Building building;
    private Settlement settlement;
    private Inventory inv;
    private Person person;
    private Robot robot;

    private List<PreparedDessert> servingsOfDessert;

	private MarsClock marsClock;// = Simulation.instance().getMasterClock().getMarsClock();

    /**
     * Constructor.
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    public PreparingDessert(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);
        this.building = building;

        sourceName = sourceName.substring(sourceName.lastIndexOf(".") + 1, sourceName.length());
        
        marsClock = Simulation.instance().getMasterClock().getMarsClock();

        inv = getBuilding().getBuildingManager().getSettlement().getInventory();

        settlement = getBuilding().getBuildingManager().getSettlement();

        PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
        dessertMassPerServing = personConfig.getDessertConsumptionRate() / (double) NUM_OF_DESSERT_PER_SOL * DESSERT_SERVING_FRACTION;

    	MealConfig mealConfig = SimulationConfig.instance().getMealConfiguration(); // need this to pass maven test
        // 2016-05-31 Added loading the two parameters from meals.xml
        cleaningAgentPerSol = mealConfig.getCleaningAgentPerSol();
        //waterUsagePerMeal = mealConfig.getWaterConsumptionRate();

        preparingWorkTime = 0D;
        servingsOfDessert = new CopyOnWriteArrayList<>();

        BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration();

        this.cookCapacity = buildingConfig.getCookCapacity(building.getBuildingType());

        // Load activity spots
        loadActivitySpots(buildingConfig.getCookingActivitySpots(building.getBuildingType()));

		//greyWaterAR = ResourceUtil.greyWaterAR;//.findAmountResource(GREY_WATER);
		//waterAR = ResourceUtil.findAmountResource(WATER);
        //foodWasteAR = ResourceUtil.findAmountResource(FOOD_WASTE);
        //NaClOAR = ResourceUtil.findAmountResource(SODIUM_HYPOCHLORITE);
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
    //public void setChef(String name) {
    //	this.producerName = name;
    //}

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
        double demand = settlement.getNumCitizens() / 5D;

        double supply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingType) && !removedBuilding) {
                removedBuilding = true;
            }
            else {
                //PreparingDessert preparingDessertFunction = (PreparingDessert) building.getFunction(FUNCTION);
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .25D + .25D;
                supply += building.getPreparingDessert().cookCapacity * wearModifier;
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

        if (getBuilding().hasFunction(FunctionType.LIFE_SUPPORT)) {
            try {
                LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(FunctionType.LIFE_SUPPORT);
                Iterator<Person> i = lifeSupport.getOccupants().iterator();
                while (i.hasNext()) {
                    Task task = i.next().getMind().getTaskManager().getTask();
                    if (task instanceof PrepareDessert) {
                        result++;
                    }
                }
            }
            catch (Exception e) {
            	e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Gets the skill level of the best cook using this facility.
     * @return skill level.

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
            catch (Exception e) {
            	e.printStackTrace();
            }
        }

        return result;
    }
     */


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
        return servingsOfDessert.size();
    }

    /**
     * Gets a dessert from this facility.
     * @return PreparedDessert
     */
    public PreparedDessert chooseADessert(Person person) {
    	List<PreparedDessert> menu = new ArrayList<>();//servingsOfDessert);
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
        }
        else
        	menu = servingsOfDessert;
        
        Iterator<PreparedDessert> i = menu.iterator();
        while (i.hasNext()) {
            PreparedDessert d = i.next();
            double q = d.getQuality();
            if (d.getName().equals(favoriteDessert)) {
            	// person will choose his/her favorite dessert right away
                if (q > bestQuality) {
                	//if (q > currentBestQuality) {
                	//	currentBestQuality = q;
                	//	bestQuality = q;
                	//}
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
     * @return quantity
     */
    public static double getDessertMassPerServing() {
       return dessertMassPerServing;
    }

    /**
     * Gets the quality of the best quality fresh Dessert at the facility.
     * @return quality
     */
    public double getBestDessertQuality() {
    	double bestQuality = 0;
    	// Question: do we want to remember the best quality ever or just the best quality among the current servings ?
        Iterator<PreparedDessert> i = servingsOfDessert.iterator();
        while (i.hasNext()) {
            //PreparedDessert freshDessert = i.next();
            //if (freshDessert.getQuality() > bestQuality)
            //	bestQuality = freshDessert.getQuality();
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
        return getBuilding().getBuildingManager().getSettlement().getIndoorPeopleCount();
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
	// 2015-02-27 Added cleanUpKitchen()
	public void cleanUpKitchen() {
		boolean cleaning0 = Storage.retrieveAnResource(cleaningAgentPerSol*.1, NaClOAR, inv, true); //SODIUM_HYPOCHLORITE, inv, true);//AmountResource.
		boolean cleaning1 = Storage.retrieveAnResource(cleaningAgentPerSol, waterAR, inv, true);//org.mars_sim.msp.core.LifeSupportType.WATER, inv, true);

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
 		// TODO : turn this list into an array to speed up the operation
    	List<String> dessertList = new CopyOnWriteArrayList<>(); //ArrayList<String>();

	  	// Put together a list of available dessert
        //for(String n : availableDesserts) {
        for (int i=0; i< NUM_DESSERTS; i++) {
        	double amount = dryMass[i];
            ///System.out.println("PreparingDessert : it's " + availableDesserts[i]);
        	boolean isAvailable = Storage.retrieveAnResource(amount, availableDessertsAR[i], inv, false);
        	boolean isWater_av = false;
        	if (dessertMassPerServing > amount)
        		isWater_av = Storage.retrieveAnResource(dessertMassPerServing-amount, waterAR, inv, false);
        	
        	if (isAvailable && isWater_av) {
            	//System.out.println("n is available");
        		dessertList.add(availableDesserts[i]);
        	}
        }

		return dessertList;
 	}

	/**
	 * Get a random dessert from a list of desserts.
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
     * Adds work to this facility.
     * The amount of work is dependent upon the person's skill.
     * @param workTime work time (millisols)
      */
    public String addWork(double workTime, Unit theCook) {
    	if (theCook instanceof Person)
    		this.person = (Person)theCook;
    	else if (theCook instanceof Robot)
    		this.robot = (Robot)theCook;

    	String selectedDessert = null;

    	preparingWorkTime += workTime;

    	if ((preparingWorkTime >= PREPARE_DESSERT_WORK_REQUIRED) && !makeNoMoreDessert) {

    	    // max allowable # of dessert servings per meal time.
	        double population = building.getBuildingManager().getSettlement().getIndoorPeopleCount();
	        double maxServings = population * settlement.getDessertsReplenishmentRate();

	        int numServings = getTotalAvailablePreparedDessertsAtSettlement(settlement);

	        if (numServings >= maxServings) {
	        	makeNoMoreDessert = true;
	        }
	        else {
	        	//List<String> dessertList = getAListOfDesserts();
	        	//selectedDessert = makeADessert(getADessert(dessertList));
	        	selectedDessert = makeADessert(getADessert(getAListOfDesserts()));
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
            PreparingDessert kitchen = (PreparingDessert) building.getFunction(FunctionType.PREPARING_DESSERT);
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

        if (selectedDessert == null) {
        	//System.out.println("PreparingDessert : selectedDessert is " + selectedDessert);
        	return null;
        }

        else {
	        Storage.retrieveAnResource(dryMass, selectedDessert, inv, true);
	        if (dessertMassPerServing > dryMass)
	        	Storage.retrieveAnResource(dessertMassPerServing-dryMass, waterAR, inv, true);
	        
	        double dessertQuality = 0;
	        // TODO: quality also dependent upon the hygiene of a person
		    double culinarySkillPerf = 0;
		    // 2017-04-26 Add influence of a person/robot's performance on meal quality
		    if (person != null)
		    	culinarySkillPerf = .25 * person.getPerformanceRating() * person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);
		    else if (robot != null)
		    	culinarySkillPerf = .1 * robot.getPerformanceRating() * robot.getBotMind().getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);

	        dessertQuality = Math.round((dessertQuality + culinarySkillPerf + cleanliness)*10D)/10D;;

		    if (person != null)
		    	producerName = person.getName();
		    else if (robot != null)
		    	producerName = robot.getName();

	        // Create a serving of dessert and add it into the list
		    servingsOfDessert.add(new PreparedDessert(selectedDessert, dessertQuality, dessertMassPerServing, (MarsClock) marsClock.clone(), producerName, this));

		    //consumeWater();

		    dessertCounterPerSol++;

		    //logger.info(producerName + " prepared a serving of " + selectedDessert
		    //		+ " in " + getBuilding().getBuildingManager().getSettlement().getName()
		    //		+ " (dessert quality : " + dessertQuality + ")");

		    preparingWorkTime -= PREPARE_DESSERT_WORK_REQUIRED;

		    // Reduce a tiny bit of kitchen's cleanliness upon every meal made
			cleanliness = cleanliness - .0075;

		    return selectedDessert;
        }

    }

    /**
     * Consumes a certain amount of water for each dessert
     */
    // 2015-01-28 Added consumeWater()
    public void consumeWater() {
    	int sign = RandomUtil.getRandomInt(0, 1);
    	double rand = RandomUtil.getRandomDouble(0.2);
    	double usage = WATER_USAGE_PER_DESSERT;
    	//TODO: need to move the hardcoded amount to a xml file
    	if (sign == 0)
    		usage = 1 + rand;
    	else
    		usage = 1 - rand;
    	Storage.retrieveAnResource(usage, waterAR, inv, true);
		double wasteWaterAmount = usage * .5;
		if (wasteWaterAmount > 0)
			Storage.storeAnResource(wasteWaterAmount, greyWaterAR, inv, sourceName + "::consumeWater");
    }


    /**
     * Time passing for the building.
     *
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     */
    public void timePassing(double time) {

	    int msol = marsClock.getMsol0();
	    
	    if (msolCache != msol) {
	    	msolCache = msol;
	    	
	        if (hasFreshDessert()) {
	            double rate = settlement.getDessertsReplenishmentRate();
	
	            // Handle expired prepared desserts.
	            Iterator<PreparedDessert> i = servingsOfDessert.iterator();
	            while (i.hasNext()) {
	
	                PreparedDessert dessert = i.next();
	                //MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
	
	                if (MarsClock.getTimeDiff(dessert.getExpirationTime(), marsClock) < 0D) {
	                    try {
	                        servingsOfDessert.remove(dessert);
	
	                        // Check if prepared dessert has gone bad and has to be thrown out.
	                        double quality = dessert.getQuality() / 2D + 1D;
	                        double num = RandomUtil.getRandomDouble(8 * quality);
	                        StringBuilder log = new StringBuilder();
	        				
	                        if (num < 1) {
	                            // Throw out bad dessert as food waste.
	                            if (getDryMass(dessert.getName()) > 0)
	                            		Storage.storeAnResource(getDryMass(dessert.getName()), foodWasteAR, inv, "::timePassing");
	                            
	            				log.append("[").append(settlement.getName()).append("] ")
	                            		.append(getDryMass(dessert.getName()))
	                            		.append(" kg ")
	                            		.append(dessert.getName().toLowerCase())
	                            		.append(DISCARDED)
	                            		.append(getBuilding().getNickName())
	                            		.append(".");
	            				
	                            LogConsolidated.log(logger, Level.INFO, 10000, sourceName, log.toString(), null);
	                            
	                        }
	                        else  {
	                            // Refrigerate prepared dessert.
	                            refrigerateFood(dessert);
	                            
	            				log.append("[").append(settlement.getName()).append("] ")
	                            		.append(REFRIGERATE)
	                            		.append(getDryMass(dessert.getName()))
	                            		.append(" kg ")
	                            		.append(dessert.getName().toLowerCase())
	                            		.append(" at ")
	                            		.append(getBuilding().getNickName())
	                            		.append(".");
	            				
	                            LogConsolidated.log(logger, Level.INFO, 10000, sourceName, log.toString(), null);
	             
	                            //logger.finest("The dessert has lost its freshness at " +
	                            //        getBuilding().getBuildingManager().getSettlement().getName());
	                        }
	
	                        // Adjust the rate to go down for each dessert that wasn't eaten.
	                        if (rate > 0 ) {
	                            rate -= DOWN;
	                        }
	                        settlement.setDessertsReplenishmentRate(rate);
	                    }
	                    catch (Exception e) {
	                    	e.printStackTrace();
	                    }
	                }
	            }
	        }
	
	        // Check if not meal time, clean up.
	        Coordinates location = getBuilding().getBuildingManager().getSettlement().getCoordinates();
	        if (!CookMeal.isMealTime(location)) {
	            finishUp();
	        }
	
	        checkEndOfDay();
	    }
    }

    // 2015-01-12 Added checkEndOfDay()
  	public synchronized void checkEndOfDay() {
		//MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
		if (marsClock == null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock(); // needed for loading a saved sim
		// Added 2015-01-04 : Sanity check for the passing of each day
		int newSol = marsClock.getSolOfMonth();
	    double rate = settlement.getDessertsReplenishmentRate();

		if (newSol != solCache) {
			solCache = newSol;
	        // reset back to zero at the beginning of a new day.
	    	dessertCounterPerSol = 0;
			// 2015-01-12 Adjust this rate to go up automatically by default
	    	rate += UP;
	      	settlement.setDessertsReplenishmentRate(rate);

	        cleanUpKitchen();
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

		} catch (Exception e) {
        	e.printStackTrace();
		}
	}

    public int getTotalServingsOfDessertsToday() {
        return dessertCounterPerSol;
    }

    /**
     * Gets the amount resource of the fresh food from a specified food group.
     *
     * @param String food group
     * @return AmountResource of the specified fresh food

    public AmountResource getFreshFoodAR(String foodGroup) {
        AmountResource freshFoodAR = AmountResource.findAmountResource(foodGroup);
        return freshFoodAR;
    }
     */

    /**
     * Computes amount of fresh food from a particular fresh food amount resource.
     *
     * @param AmountResource of a particular fresh food
     * @return Amount of a particular fresh food in kg, rounded to the 4th decimal places

    public double getFreshFood(AmountResource ar) {
        double freshFoodAvailable = inv.getAmountResourceStored(ar, false);
    	// 2015-01-09 Added addDemandTotalRequest()
    	//inv.addDemandTotalRequest(ar);
        return freshFoodAvailable;
    }
    */

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
	public double getFullHeatRequired() {
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		return 0;
	}

    @Override
    public void destroy() {
        super.destroy();

        person = null;
        robot = null;
    	marsClock = null;
        building = null;
        inv = null;
        settlement = null;
        //servingsOfDessertList.clear();
        servingsOfDessert = null;
        waterAR = null;
        greyWaterAR = null;
        foodWasteAR = null;
        NaClOAR = null;
        availableDessertsAR = null;
    }
}
/*
 * Mars Simulation Project
 * Fishery.java
 * @date 2023-12-07
 * @author Barry Evans
 */

package com.mars_sim.core.structure.building.function.farming;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;

import com.mars_sim.core.data.SolMetricDataLogger;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingException;
import com.mars_sim.core.structure.building.FunctionSpec;
import com.mars_sim.core.structure.building.function.Function;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.HouseKeeping;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The Fishery function that is responsible for aquatic farming
 */
public class Fishery extends Function {	

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Fishery.class.getName());

	private static final int MAX_NUM_SOLS = 14;
	
	static final String [] INSPECTION_LIST = {"Environmental Control",
													  "Contamination Control", 
													  "Tank Integrity",
													  "Foundation",	
													  "Structural Element", 
													  "Thermal Budget",
													  "Irrigation"};
	static final String [] CLEANING_LIST = {"Equipment", 
													"Tank Glass", 
													"Water Heater",
													"Pipings", 
													"Valves"};
	
	/** Time before weed need tendering. */
	private static final int WEED_DEMAND = 500;
	
	/** Convert from kg to ounce. */
	public static final double KG_PER_OUNCE = 0.02834952;
	/** Convert from ounce to kg. */
	public static final double OUNCE_PER_KG = 35.27396195;
	/** Initial size of each weed, in ounces. */ 
	public static final double WEED_SIZE = 15;
	/** Growth rate of weeds, in ounces/millisols. */  
	public static final double WEED_GROWTH_RATE = 0.005;
	/** Fish size, in ounces. */ 
	public static final double FISH_WEIGHT = 50; 
	/** Fish length in cm. */
	public static final int FISH_LENGTH = 30; 
	/** Growth rate of fish in ounces/millisol. */
	private static final double FISH_RATE = 0.0002D;
	/** A fish must eat FRACTION times its size during a frame, or it will die. */
	public static final double FRACTION = 0.4;
	
	// Note: At the end of each millisol, some fish have babies. 
	// The total number of a newly borne fish is the current number of fish 
	// times the BIRTH_RATE (rounded down to an integer)
	/** Birth rate of fish in kg/millisol. */
	public static final double BIRTH_RATE = 0.008 / 1000;
	
	/** Average number of weeds nibbled by a fish per frame. */
	private static final double AVERAGE_NIBBLES = 0.0025;
	/** kW per litre of water. */
	private static final double POWER_PER_LITRE = 0.0001D;
	/** kW per fish. */
	private static final double POWER_PER_FISH = 0.02D;
	/** kW per weed mass. */
	private static final double POWER_PER_WEED_MASS = 0.002D;
	/** Tend time per weed. */
	private static final double TIME_PER_WEED = 0.2D;
	/** Adult fish length per litre. Cold water is 2.5cm per 4.55 litre. */
	private static final double FISHSIZE_LITRE = (2.5D/4.55D); 
	/** Number of fish as a percentage of maximum. */
	private static final double IDEAL_PERCENTAGE = 0.8D;

	/** Size of tank in litres. */
	private int tankSize;
	/** Maximum number of fish. */
	private int maxFish;
	/** Optimal number of fish.*/
	private int idealFish;
	
	/** The initial ratio of fish and water [in kg/L] . */	
	private final double fishToWaterMassRatio;
	/** Current overall health of fish (from 0 to 1). */	
	private double health = 1;
	/** The cumulative time spent in this greenhouse [in sols]. */
	private double cumulativeWorkTime;	
	/** The amount iteration for birthing fish */
	private double birthIterationCache;
	/** The amount iteration for nibbling weed */
	private double nibbleIterationCache;
	/** How long has the weed been tendered. */
	private double tendertime;
	/** How old is the weed since the last tendering. */
	private double weedAge = 0;
	/** The area of tank [in m^2]. */
	private double tankArea;
	/** The depth of tank [in m]. */
	private double tankDepth;
	/** The amount of water [in m]. */
	private double waterMass;
	
	/** A list of our fish. */
	private List<Herbivore> fish;   
	/** A list of our weeds. */
	private List<Plant> weeds;
	
	/** Keep track of cleaning and inspections. */
	private HouseKeeping houseKeeping;
	
	/** The resource logs for growing algae in this facility [kg/sol]. */
	private SolMetricDataLogger<Integer> resourceLog = new SolMetricDataLogger<>(MAX_NUM_SOLS);
	
	
	/**
	 * Constructor.
	 * 
	 * @param building the building the function is for.
	 * @param spec Definition of the Fishery properties
	 * @throws BuildingException if error in constructing function.
	 */
	public Fishery(Building building, FunctionSpec spec) {
		// Use Function constructor.
		super(FunctionType.FISHERY, spec, building);
		
		houseKeeping = new HouseKeeping(CLEANING_LIST, INSPECTION_LIST);

		// Calculate the tank size via config
		tankSize = spec.getCapacity();
		
		// Retrieve water to create pond
		// Note that 1 L of water is 1 kg
//		building.getSettlement().retrieveAmountResource(ResourceUtil.waterID, tankSize);
		
		// Calculate fish & weeds by tank size
		maxFish = (int)((tankSize * FISHSIZE_LITRE)/FISH_LENGTH);
		
		idealFish = (int)(maxFish * IDEAL_PERCENTAGE);
		
		// For now, fishToWaterMassRatio is 0.0146
		fishToWaterMassRatio = 1.0 * idealFish / tankSize; 

	    int numFish = (int)RandomUtil.getRandomDouble(idealFish * 0.05, idealFish * 0.15);

	    int numWeeds = numFish * 10;
	    
		tendertime = numWeeds * TIME_PER_WEED;

		weedAge = 0;
		
		fish = new ArrayList<>(numFish);
	    weeds = new ArrayList<>(numWeeds);
	    
	    int i;
	    // Initialize the bags of fish and weeds
	    for (i = 0; i < numFish; i++) {
	    	double weight = RandomUtil.getRandomDouble(FISH_WEIGHT *.75, FISH_WEIGHT * 1.25);
	    	double eatingRate = RandomUtil.getRandomDouble(weight * FRACTION *.75, weight * FRACTION * 1.25);
	    	fish.add(new Herbivore(weight,  
	    		   RandomUtil.getRandomDouble(FISH_RATE *.75, FISH_RATE * 1.25), 
	    		   eatingRate));
	    }
	    for (i = 0; i < numWeeds; i++) {
	    	double weight = RandomUtil.getRandomDouble(WEED_SIZE *.75, WEED_SIZE * 1.25);
	    	double growthRate = RandomUtil.getRandomDouble(WEED_GROWTH_RATE *.75, WEED_GROWTH_RATE * 1.25);
	    	weeds.add(new Plant(weight, growthRate));
	    }
	    
	    // The amount of water in kg
	 	waterMass = tankSize * fish.size() / maxFish;
	    
	    logger.log(building, Level.CONFIG, 0, "# of fish: " + numFish + "  # of weeds: " + numWeeds + ".");
	}


	/**
	 * Gets the value of the function for a named building type.
	 * 
	 * @param type the building type.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function. Called by BuildingManager.java
	 *         getBuildingValue()
	 */
	public static double getFunctionValue(String type, boolean newBuilding, Settlement settlement) {

		// Demand is number of fish needed to produce food for settlement population.
		// But it is not essential food
		double demand = 2D * settlement.getNumCitizens();

		// Supply is total number of fish at settlement.
		double supply = 0D;
		boolean removedBuilding = false;
		for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.FISHERY)) {
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(type) && !removedBuilding) {
				removedBuilding = true;
			} else {
				Fishery fishFarm = building.getFishery();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += fishFarm.getNumFish() * wearModifier;
			}
		}

		// Modify result by value (VP) of food meat at the settlement.
		double foodValue = settlement.getGoodsManager().getGoodValuePoint(ResourceUtil.fishMeatID);

		return (demand / (supply + 1D)) * foodValue;
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
			double time = pulse.getElapsed();
			
		    // Account for fish and weeds
		    simulatePond(time);

		    double degradeValue = time / 1000;
			// degrade the cleanliness
			houseKeeping.degradeCleaning(degradeValue);
			// degrade the housekeeping item
			houseKeeping.degradeInspected(degradeValue);
			
			weedAge += time;
		}
		return valid;
	}
	
	/**
	* Simulates life in the pond.
	* 
	* @param time
	**/
	private void simulatePond(double time) {
	   int i;
	   int index;
	   
	   Herbivore nextFish;
	   Plant nextWeed;
	
	   int numFish = fish.size();
	   int numWeeds = weeds.size();
	   // Have randomly selected fish nibble on randomly selected plants
	   nibbleIterationCache += AVERAGE_NIBBLES * time * numFish;
	   
	   if (nibbleIterationCache > numFish) {
		   int feedIterations = (int)nibbleIterationCache;
		   feedIterations = Math.min(feedIterations, numFish * 3);
		   feedIterations = Math.max(feedIterations, numFish);
		   feedIterations = Math.min(feedIterations, numWeeds);

		   nibbleIterationCache = nibbleIterationCache - feedIterations;

		   for (i = 0; i < feedIterations; i++) {
			   index = RandomUtil.getRandomInt(numFish-1);
			   nextFish = fish.get(index);
			   index = RandomUtil.getRandomInt(numWeeds-1);
			   nextWeed = weeds.get(index);
			   nextFish.nibble(nextWeed);
		   } 
		   
		   // Simulate the fish
		   ListIterator<Herbivore> it = fish.listIterator();
		   while(it.hasNext()) {
			  nextFish = it.next();
		      nextFish.growPerFrame(time);
		      if (!nextFish.isAlive())
		         it.remove();
		   }
		
		   // Simulate the weeds
		   for (Plant p : weeds) {
			   p.growPerFrame();
		   }
	   }
	
	   // Create some new fish, according to the BIRTH_RATE constant
	   if (fish.size() < maxFish * 1.1) {
		   birthIterationCache += BIRTH_RATE * time * fish.size() * health
				   * (1 + .01 * RandomUtil.getRandomInt(-15, 15));
		   if (birthIterationCache > 1) {
			   int newFish = (int)birthIterationCache;
			   birthIterationCache = birthIterationCache - newFish;
			   for (i = 0; i < newFish; i++) {
//			       fish.add(new Herbivore(FISH_WEIGHT, 0, FISH_WEIGHT * FRACTION));	
				   // Assume the beginning weight of a baby fish is 1/30 of an adult fish
				   double weight = RandomUtil.getRandomDouble(FISH_WEIGHT / 30 *.75, FISH_WEIGHT / 30 * 1.25);
				   double eatingRate = RandomUtil.getRandomDouble(weight * FRACTION *.75, weight * FRACTION * 1.25);
				   fish.add(new Herbivore(weight,  
			    		   RandomUtil.getRandomDouble(FISH_RATE *.75, FISH_RATE * 1.25), 
			    		   eatingRate));
			       
			       logger.info("A fish was given birth in " + building + ".");
			       
			       // Record the new fish
				   addResourceLog(1, ResourceUtil.fishMeatID);
				   
				   // Compute the amount of fresh water to be added for the new fish
				   double freshWater = FISH_WEIGHT/20 / fishToWaterMassRatio;
				   // Consume fresh water
				   retrieveWater(freshWater, ResourceUtil.waterID);
				   // Add fresh water to the existing tank water
				   waterMass += freshWater;
			   }
		   }
	   }	   
	}
	
	
	
	/**
	* Calculates the total mass of a collection of Organism.
	* 
	* @param organisms
	*   a list of Organism objects
	* @param <T>
	*   component type of the elements in the organisms list
	* <b>Precondition:</b>
	*   Every object in organisms is an Organism.
	* @return
	*   the total mass of all the objects inOrganism (in ounces).
	**/
	public static <T extends Organism> double totalMass(List<T> organisms) {
	   double answer = 0;
	   
	   for (Organism next : organisms) {
	      if (next != null)
	         answer += next.getSize( );
	   }
	   return answer;
	}


	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	@Override
	public double getFullPowerRequired() {
		// Power (kW) required for normal operations.
		return waterMass * POWER_PER_LITRE 
				+ getNumFish() * POWER_PER_FISH 
				+ getTotalWeedMass() * POWER_PER_WEED_MASS;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * 
	 * @return power (kW)
	 */
	@Override
	public double getPoweredDownPowerRequired() {
		return getFullPowerRequired() * .1;
	}

	@Override
	public double getMaintenanceTime() {
		return fish.size() * .075;
	}

	public String getUninspected() {
		return houseKeeping.getLeastInspected();
	}

	public String getUncleaned() {
		return houseKeeping.getLeastCleaned();
	}

	public double getInspectionScore() {
		return houseKeeping.getAverageInspectionScore();
	}
	
	public double getCleaningScore() {
		return houseKeeping.getAverageCleaningScore();
	}
	
	public void markInspected(String s, double value) {
		// Record the work time
		addCumulativeWorkTime(value);
		
		houseKeeping.inspected(s, value);
	}

	public void markCleaned(String s, double value) {
		// Record the work time
		addCumulativeWorkTime(value);
		
		houseKeeping.cleaned(s, value);
	}

	public int getNumFish() {
		return fish.size();
	}

	public int getIdealFish() {
		return idealFish;
	}
	
	public int getMaxFish() {
		return maxFish;
	}
	
	public int getTankSize() {
		return tankSize;
	}
	
	public double getTotalWeedMass() {
		return Math.round(totalMass(weeds)/ OUNCE_PER_KG * 100.0)/100.0;
	}

	public double getTotalFishMass() {
		return Math.round(totalMass(fish)/ OUNCE_PER_KG * 100.0)/100.0;
	}
	
	public int getNumWeed() {
		return weeds.size();
	}
	
	public int getSurplusStock() {
		return fish.size() - idealFish;
	}

	/**
	 * Spends some time on weed maintenance.
	 * 
	 * @param workTime
	 * @return
	 */
	public double tendWeeds(double workTime) {	
		double surplus = 0;
		
		// Record the work time
		addCumulativeWorkTime(workTime);
		
		// Grow the weeds
		for (Plant p : weeds) {
			p.growPerFrame();
		}

		tendertime -= workTime;

		if (tendertime < 0) {
			surplus = Math.abs(tendertime);
			tendertime = weeds.size() / fish.size() * TIME_PER_WEED;
			logger.log(building, Level.INFO, 10_000L, 
					"Weeds fully tended for " 
						+ Math.round(tendertime * 100.0)/100.0 + " millisols.");
			weedAge = 0;
		}
		
		return surplus;
	}

	/**
	 * Catches some fish.
	 * 
	 * @param fisher
	 * @param workTime
	 * @return
	 */
	public double catchFish(Worker fisher, double workTime) {
		if (fish.size() <= idealFish) {
			return workTime;
		}
		
		// Random
		int rand = RandomUtil.getRandomInt(fish.size());
		if (rand > idealFish) {
			// Catch one
			logger.log(building, fisher, Level.INFO, 0, "One fish caught. Stock:" + fish.size(), null);
			Herbivore removed = fish.remove(1);
			
			// Fish stored as KG, 90% is useful
			store((removed.getSize() * 0.9D) * KG_PER_OUNCE, ResourceUtil.fishMeatID, "Fishery::catchFish");
			
			// Fish Oil is 1% of fish size, a guess
			store((removed.getSize() * 0.01D) * KG_PER_OUNCE, ResourceUtil.fishOilID, "Fishery::catchFish");
		}
		return 0;
	}

//	private double getGasThreshold() {
//		return (waterMass + fish.size() * FISH_WEIGHT + getWeedMass()) / 100; 
//	}

	public double getWaterMass() {
		return waterMass;
	}
	
	/**
	 * Gets the demand for the weeds to be tendered.
	 * 
	 * @return
	 */
	public double getWeedDemand() {
		return weedAge / WEED_DEMAND;
	}
	
	/**
	 * Gets the cumulative work time [in sols].
	 * 
	 * @return
	 */
	public double getCumulativeWorkTime() {
		return cumulativeWorkTime;
	}
	
	/**
	 * Adds the cumulative work time.
	 * 
	 * @return
	 */
	public void addCumulativeWorkTime(double value) {
		cumulativeWorkTime += value;
	}
	
	/**
	 * Records the average resource usage of a resource
	 *
	 * @param amount    average consumption/production in kg/sol
	 * @Note positive usage amount means consumption 
	 * @Note negative usage amount means generation
	 * @param id The resource id
	 */
	public void addResourceLog(double amount, int id) {
		resourceLog.increaseDataPoint(id, amount);
	}
	

	/**
	 * Computes the daily average of a particular resource.
	 * 
	 * @param id The resource id
	 * @return average consumed or produced in kg/sol
	 */
	public double computeDaily(int id) {
		return resourceLog.getDailyAverage(id);
	}
	
	/**
	 * Retrieves water from the Settlement and record the usage in the Farm.
	 * 
	 * @param amount Amount being retrieved
	 * @param id Resource id
	 */
	private void retrieveWater(double amount, int id) {
		if (amount > 0) {
			retrieve(amount, WATER_ID, true);
			// Record the amount of water consumed
			addResourceLog(amount, id);		
		}
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		resourceLog = null;
		houseKeeping = null;
	}
}

/*
 * Mars Simulation Project
 * Fishery.java
 * @date 2026-06-04
 * @author Barry Evans
 */

package com.mars_sim.core.building.function.farming;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.config.FunctionSpec;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.HouseKeeping;
import com.mars_sim.core.data.SolMetricDataLogger;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.RandomUtil;

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
	private static final int WEED_DEMAND = 200;
	/** Convert from kg to ounce. */
	public static final double KG_PER_OUNCE = 0.02834952;
	/** Convert from ounce to kg. */
	public static final double OUNCE_PER_KG = 35.27396195;
	/** Typical adult weed size, in ounces. */ 
	public static final double WEED_OUNCES = 15;
	/** Typical adult fish size, in ounces. */ 
	public static final double FISH_OUNCES = 50; 
	/** Fish length in cm. */
	public static final int FISH_LENGTH = 30; 
	/** Growth rate of weeds, in ounces/millisols. */  
	public static final double WEED_GROWTH_RATE = 0.0005;
	/** Growth rate of fish in ounces/millisol. */
	private static final double FISH_GROWTH_RATE = 0.0015;
	/** A fish eats a fraction of its size. */
	public static final double EAT_FRACTION = 0.1;
	
	// Note: At the end of each millisol, some fish have babies. 
	// The total number of a newly borne fish is the current number of fish 
	// times the BIRTH_RATE (rounded down to an integer)
	/** Birth rate of fish in kg/millisol. */
	public static final double FISH_BIRTH_RATE = 0.0015 / 1000;
	
	public static final double WEED_BIRTH_RATE = 0.001 / 1000;
	
	/** Average number of weeds nibbled by a fish per frame. */
	private static final double AVERAGE_NIBBLES = 0.5;
	/** kW per litre of water. */
	private static final double POWER_PER_LITRE = 0.0001D;
	/** kW per fish. */
	private static final double POWER_PER_FISH = 0.02D;
	/** kW per weed mass. */
	private static final double POWER_PER_WEED_MASS = 0.002D;
	/** Tend time per weed. */
	private static final double TIME_PER_WEED = 10D;
	/** Adult fish length per litre. Cold water is 2.5cm per 4.55 litre. */
	private static final double FISHSIZE_LITRE = (2.5D/4.55D); 
	/** Number of fish as a percentage of maximum. */
	private static final double IDEAL_FRACTION = 0.8D;
	
	private static final double PORTION = AVERAGE_NIBBLES * EAT_FRACTION * FISH_OUNCES;

	/** Size of tank in litres. */
	private int tankSize;
	/** Maximum number of weeds. */
	private int maxWeed;
	/** Maximum number of fish. */
	private int maxFish;
	/** Optimal number of fish.*/
	private int idealFish;

	/** Current overall health of this fishery environment (from 0 to 1). */	
	private double health = 1;
	/** The cumulative time spent in this greenhouse [in sols]. */
	private double cumulativeWorkTime;	
	/** The amount iteration for birthing fish */
	private double fishBirthCache;
	/** The amount iteration for birthing weed */
	private double weedBirthCache;
	/** The amount iteration for nibbling weed */
	private double nibbleIterationCache;
	/** How long has the weed been tendered. */
	private double tendertime;
	/** How old is the weed since the last tendering. */
	private double weedAge = 0;
	/** The amount of water [in m]. */
	private double waterMass;
	/** The chance of fish seeing the weed. */
	private double collisionChance;
	/** The mass in kg fish harvested. */
	private double kgHarvested;
	
	/** A list of our fish. */
	private List<Fish> fish;   
	/** A list of our weeds. */
	private List<Plant> weeds;
	
	/** Keep track of cleaning and inspections. */
	private HouseKeeping houseKeeping;
	
	/** The resource logs for growing fish in this facility [kg/sol]. */
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
		
		// Calculate fish & weeds by tank size
		maxFish = (int)((tankSize * FISHSIZE_LITRE)/FISH_LENGTH);
		
		idealFish = (int)(maxFish * IDEAL_FRACTION);

		double rand = RandomUtil.getRandomDouble(0.5, 1);
		
	    int numFish = (int)(rand * idealFish);

	    int numWeeds = numFish * RandomUtil.getRandomInt(4, 6);
	    
	    maxWeed = idealFish * RandomUtil.getRandomInt(4, 6);
	    		
		tendertime = RandomUtil.getRandomDouble(.75, 1.25) * Math.min(TIME_PER_WEED, numFish / 10.0);

		weedAge = 0;
		
		fish = new ArrayList<>(numFish);
	    weeds = new ArrayList<>(numWeeds);
	    
	    // Initialize the bags of fish and weeds
	    for (int i = 0; i < numFish; i++) {
	    	generateFish(false);
	    }
	    for (int i = 0; i < numWeeds; i++) {
	    	generateWeed(false);
	    }
	    
	    // The amount of water in kg
	 	waterMass = tankSize * ((double)fish.size() / maxFish);
	 	// Chance of fish seeing the weed
	 	collisionChance = numFish * numWeeds / waterMass;
	 	
	    logger.info(building, "Initial # of fishes: " + numFish);
	    logger.info(building, "Total fish mass: " + getTotalFishMass());

	    logger.info(building, "Initial # of weeds: " + numWeeds);
	    logger.info(building, "Total weed mass: " + getTotalWeedMass());
	}

	/**
	 * Generates a young fish.
	 * 
	 * @param isYoung
	 */
	private void generateFish(boolean isYoung) {
    	double weight = RandomUtil.getRandomDouble(FISH_OUNCES *.5, FISH_OUNCES * 1.5);
    	if (isYoung)
    		weight /= 100;
    	double eatingRate = RandomUtil.getRandomDouble(weight * EAT_FRACTION *.9, weight * EAT_FRACTION * 1.1) / 100;
    	double growthRate = RandomUtil.getRandomDouble(FISH_GROWTH_RATE *.9, FISH_GROWTH_RATE * 1.1);
    	fish.add(new Fish(weight, growthRate, eatingRate));
	}
	
	/**
	 * Generates a young weed.
	 * 
	 * @param isYoung
	 */
	private void generateWeed(boolean isYoung) {
    	double weight = RandomUtil.getRandomDouble(WEED_OUNCES *.75, WEED_OUNCES * 1.25);
    	if (isYoung)
    		weight /= 50;
    	double growthRate = RandomUtil.getRandomDouble(WEED_GROWTH_RATE *.9, WEED_GROWTH_RATE * 1.1);
    	weeds.add(new Plant(weight, growthRate));
	}
	
	/**
	 * Updates the eating rate as the fish grow.
	 */
	public void updateEatingRate() {
		for (var f: fish) {
			double newMass = f.getSize() * EAT_FRACTION;
			double newEatRate = RandomUtil.getRandomDouble(newMass * .95, newMass * 1.05);
			f.setNeed(newEatRate);
		}
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
		double foodValue = settlement.getGoodsManager().getGoodValuePoint(ResourceUtil.FISH_MEAT_ID);

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
		    simulatePond(pulse);

		    double degradeValue = time / 1000;
			// degrade the cleanliness
			houseKeeping.degradeCleaning(degradeValue);
			// degrade the housekeeping item
			houseKeeping.degradeInspected(degradeValue);
			
			weedAge += time;
			
			if (pulse.isNewHalfSol()) {
				// As the fish grow, increase eating
				updateEatingRate();			
			 	// Update the chance of fish seeing the weed
			 	collisionChance = fish.size() * weeds.size() / waterMass;
			}
		}
		return valid;
	}
	
	public double getKgHarvested() {
		return kgHarvested;
	}
	
	/**
	 * Gets the average age of all fish.
	 * 
	 * @return
	 */
	public double getAverageAge() {
		int num = fish.size();
		double age = 0;
		for (Fish f : fish) {
			age += f.getAge();
		}
		if (num == 0)
			return 0;
		return age / num;
	}
	   
	/**
	 * Simulate the organisms' growing cycle.
	 * 
	 * @param organisms
	 * @param time
	 */
	private static void simulateOrganisms(List<? extends Organism> organisms, double time) {
		var it = organisms.listIterator();
		while(it.hasNext()) {
		   var f = it.next();
		   f.growPerFrame(time);
		   if (!f.isAlive()) {
				it.remove();
		   }
		}
	}

	/**
	* Simulates life in the pond.
	* 
	* @param pulse
	**/
	private void simulatePond(ClockPulse pulse) {
		double time = pulse.getElapsed();
	
		// Simulate the fish's growing cycle
		simulateOrganisms(fish, time);
	
		// Simulate the weed's natural growth cycle
		simulateOrganisms(weeds, time);
	   
		int numFish = fish.size();
		int numWeeds = weeds.size();
		
		increaseNibble(time);
		
		int feeds = (int)nibbleIterationCache;
	   
		while (nibbleIterationCache > 0 || feeds > 0) {
	   
//			Do not remove, may debug further: logger.info(building, "time: " + Math.round(time * 1000.0)/1000.0 +  "  nibbleIterationCache: " + Math.round(nibbleIterationCache * 1000.0)/1000.0 +  "  feeds: " + feeds + ".");
				   
			nibbleIterationCache -= PORTION * collisionChance * 3;
			   
			// Ensure each Fish get the chance to nibble on a weed
			for (int i = 0; i < numFish ; i++) {
				Fish f = fish.get(i);
				int index = RandomUtil.getRandomInt(numWeeds-1);
				Plant nextWeed = weeds.get(index);
				f.nibble(nextWeed);
				
				feeds--;
		   }
		}
		
		// Check for birth
		checkBirth(time, numFish, numWeeds);
	}
	
	/**
	 * Checks if fish and weed would give birth.
	 * 
	 * @param time
	 * @param numFish
	 * @param numWeeds
	 */
	private void checkBirth(double time, double numFish, double numWeeds) {
		
	   // Create some new fish, according to the FISH_BIRTH_RATE constant
	   if (numFish < maxFish) {
		   // Make fishBirthCache dependent on time since high time ratio
		   // should generate fish faster
		   fishBirthCache += FISH_BIRTH_RATE * time * numFish * health
				   * (2 + .05 * RandomUtil.getRandomInt(-15, 15));
		   if (fishBirthCache > 1) {
				int newFish = (int)fishBirthCache;
				fishBirthCache = fishBirthCache - newFish;
				addFish(newFish);
		   }
	   }	   
	   
	   // Create some new weed, according to the WEED_BIRTH_RATE constant
	   if (numWeeds < maxWeed) {
		   // Make weedBirthCache dependent on time since high time ratio
		   // should generate weeds faster
		   weedBirthCache += WEED_BIRTH_RATE * time * numWeeds * health 
				   * (3.5 + .005 * RandomUtil.getRandomInt(-15, 15));
		   if (weedBirthCache > 1) {
				int newWeed = (int)weedBirthCache;
				weedBirthCache = weedBirthCache - newWeed;
				addWeed(newWeed);
		   }
	   }
	}

	/**
	 * Adds fish.
	 * 
	 * @param numFish
	 */
    public void addFish(int numFish) {
		for (int i = 0; i < numFish; i++) {
			generateFish(true);
		}
		
		int num = fish.size();

		logger.info(building, "A fish was born: "  + (num - numFish) + " -> " + num + ".");
    }
	
	/**
	 * Adds weeds.
	 * 
	 * @param numWeed
	 */
    public void addWeed(int numWeed) {
		for (int i = 0; i < numWeed; i++) {
			generateWeed(true);
		}

		int num = weeds.size();

		logger.info(building, "A weed was born: "  + (num - numWeed) + " -> " + num + ".");
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
	         answer += next.getSize();
	   }
	   return answer;
	}


	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	@Override
	public double getFullPowerLoad() {
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
	public double getLowPowerLoad() {
		return getFullPowerLoad() * .1;
	}

	@Override
	public double getMaintenanceTime() {
		return fish.size() * .075;
	}

    public HouseKeeping getHousekeeping() {
        return houseKeeping;
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
		return Math.round(totalMass(weeds) * KG_PER_OUNCE * 100.0)/100.0;
	}

	public double getTotalFishMass() {
		return Math.round(totalMass(fish) * KG_PER_OUNCE * 100.0)/100.0;
	}
	
	public int getNumWeed() {
		return weeds.size();
	}
	
	public int getSurplusStock() {
		return fish.size() - idealFish;
	}

	/**
	 * Spends time manually on tending the fish.
	 * 
	 * @param workTime
	 * @return
	 */
	public double tendWeeds(double workTime) {	
		double surplus = 0;
		
		// Record the work time
		addCumulativeWorkTime(workTime);
		
		int numFish = fish.size();
		int numWeeds = weeds.size();
		
		// Manually increase the nibble cache since tending the weed attract fish to come to nibble more
		randomNibble(workTime, numFish, numWeeds);
		
		tendertime -= workTime;

		if (tendertime < 0) {
			surplus = Math.abs(tendertime);
			tendertime = RandomUtil.getRandomDouble(.75, 1.25) * Math.min(TIME_PER_WEED, numFish / 10.0);
			weedAge = 0;
		}
		
		return surplus;
	}

	/**
	 * Cleans the tanks.
	 * 
	 * @param workTime
	 * @return
	 */
	public double cleanTank(double workTime) {
		
		// Improve the health of the fishery
		health *= RandomUtil.getRandomDouble(1, 1.05);
		
		return 0;
	}
	

	/**
	 * Inspects the tanks.
	 * 
	 * @param workTime
	 * @return
	 */
	public double inspectTank(double workTime) {
		
		// Future: Write up items that need maintenance 
		
		return 0;
	}
	
	/**
	 * Increases the nibble cache.
	 * 
	 * @param workTime
	 */
	private void increaseNibble(double workTime) {
		// Tending the weed attract fish to come to nibble more
		nibbleIterationCache += workTime * PORTION * collisionChance;
	}
	
	/**
	 * random the nibble cache.
	 * 
	 * @param workTime
	 * @param numFish
	 * @param numWeeds
	 */
	private void randomNibble(double workTime, int numFish, int numWeeds) {
		// Tending the weed attract fish to come to nibble more
		increaseNibble(workTime);

		int feeds = (int)nibbleIterationCache;
		
		// Simulate the rest of the fish randomly to nibble on a weed
		for (int i = 0; i < feeds; i++) {
			// Future: When picking a fish to eat, pick one that's hungry
			int index = RandomUtil.getRandomInt(numFish-1);
			Fish nextFish = fish.get(index);
			index = RandomUtil.getRandomInt(numWeeds-1);
			Plant nextWeed = weeds.get(index);
			nextFish.nibble(nextWeed);
	   }   
	}
	
	/**
	 * Is it the right time to harvest a fish ?
	 * 
	 * @return
	 */
	public boolean canCatchFish() {
		int numFish = fish.size();
		return numFish > 0 && numFish >= idealFish * RandomUtil.getRandomDouble(.1, 1.9);
	}
	
	/**
	 * Catches some fish.
	 * 
	 * @param fisher
	 * @param workTime
	 * @return
	 */
	public double catchFish(Worker fisher, double workTime) {
		if (!canCatchFish()) return workTime;
				
		Fish candidate = null;
		
		double age = 0;
	
		int numFish = fish.size();
		
		// Get the oldest fish
		for (int i = 0; i < numFish ; i++) {
			Fish f = fish.get(i);
			double tempAge = f.getAge();
			if (tempAge > age) {
				age = tempAge;
				candidate = f;
			}
	   }
		
		double mass = candidate.getSize() * KG_PER_OUNCE;
		// Catch one
		logger.info(fisher, "One fish caught. Mass: " 
				+ Math.round(mass * 100.0)/100.0 
				+ " kg. Remaining: " + (fish.size() - 1));

		kgHarvested += mass;
		
		candidate.expire();
		
		// Record as a harvest
		addResourceLog(mass, ResourceUtil.FISH_MEAT_ID);
		
		// Fish stored as KG, 90% is useful
		store(mass * .9, ResourceUtil.FISH_MEAT_ID, "Fishery::catchFish");
		
		// Fish Oil is 10% of fish size, a guess
		store(mass * 0.1, ResourceUtil.FISH_OIL_ID, "Fishery::catchFish");
		
		return 0.0;
	}

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
		return cumulativeWorkTime + houseKeeping.getCumulativeWorkTime();
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
	public double computeDailyAverage(int id) {
		return resourceLog.getDailyAverage(id);
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		resourceLog = null;
		houseKeeping = null;
		super.destroy();
	}
}

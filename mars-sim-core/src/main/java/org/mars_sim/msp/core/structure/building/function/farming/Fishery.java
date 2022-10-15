/*
 * Mars Simulation Project
 * Fishery.java
 * @date 2021-10-29
 * @author Barry Evans
 */

package org.mars_sim.msp.core.structure.building.function.farming;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.HouseKeeping;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Fishery function that is responsible for aquatic farming
 */
public class Fishery extends Function {	

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Fishery.class.getName());

	
	private static final String [] INSPECTION_LIST = {"Environmental Control System",
													  "Any Traces of Contamination", 
													  "Tank Integrity",
													  "Foundation",	
													  "Structural Element", 
													  "Thermal Budget",
													  "Water and Irrigation System"};
	private static final String [] CLEANING_LIST = {"Equipment", 
													"Tank Glass", 
													"Water Heater",
													"Pipings", 
													"Valves"};
	
	// Convert from kg to ounce
	public static final double KG_PER_OUNCE = 0.02834952;
	// Convert from ounce to kg
	public static final double OUNCE_PER_KG = 35.27396195;
	// Initial size of each weed, in ounces 
	public static final double WEED_SIZE = 15;
	// Growth rate of weeds, in ounces/millisols  
	public static final double WEED_RATE = 0.000357;
	// Fish size, in ounces 
	public static final double FISH_WEIGHT = 50; 
	// Fish length in cm
	public static final int FISH_LENGTH = 30; // A basic Roach that can live in cold water
	// Growth rate of fish in ounces/millisol
	private static final double FISH_RATE = 0.0002D;
	
	// A fish must eat FRACTION times its size during a frame, or it will die.
	public static final double FRACTION = 0.4;
	// At the end of each millisol, some fish have babies. The total number of new
	// fish born is the current number of fish times the BIRTH_RATE 
	// (rounded down to an integer).
	public static final double BIRTH_RATE = 0.000008;
	
	// Number of weeds in the pond
	private static final int MANY_WEEDS = 120;
	// Average number of weeds nibbled by a fish per frame
	private static final double AVERAGE_NIBBLES = 0.005;
	// Kw per litre of water
	private static final double POWER_PER_LITRE = 0.00005D;
	// Tend time per weed
	private static final double TIME_PER_WEED = 0.2D;
	// Adult fish length per litre
	private static final double FISHSIZE_LITRE = (2.5D/4.55D); // Cold water is 2.5cm per 4.55 litre
	
	// Time before weed need tendering
	private static final int WEED_DEMAND = 500;
	
	/** Number of fish as a percentage of maximum **/
	private static final double IDEAL_PERCENTAGE = 0.8D;

	/** The amount iteration for birthing fish */
	private double birthIterationCache;
	/** The amount iteration for nibbling weed */
	private double nibbleIterationCache;
	
	/** Size of tank in litres **/
	private int tankSize;
	
	/** Maximum number of fish **/
	private int maxFish;

	/** Optimal number of fish **/
	private int idealFish;

	/** How old is the weed since the last tendering **/
	private double weedAge = 0;

	/** A Vector of our fish. */
	private List<Herbivore> fish;   
	/** A Vector of our weeds. */
	private List<Plant> weeds;
	
	private HouseKeeping houseKeeping;
	
	/** How long has the weed been tendered **/
	private double weedTendertime;
    
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
	    int numFish = (int)(maxFish * IDEAL_PERCENTAGE);
	    int numWeeds = (numFish * 30 + MANY_WEEDS)/2;
	        
	    // Healthy stock is the initial number of fish
	    idealFish = numFish;
		weedTendertime = numWeeds * TIME_PER_WEED;
		weedAge = 0;
		
		fish = new ArrayList<>(numFish);
	    weeds = new ArrayList<>(numWeeds);
	    
	    int i;
	    // Initialize the bags of fish and weeds
	    for (i = 0; i < numFish; i++)
	       fish.add(new Herbivore(FISH_WEIGHT, FISH_RATE, FISH_WEIGHT * FRACTION));
	    for (i = 0; i < numWeeds; i++)
	       weeds.add(new Plant(WEED_SIZE, WEED_RATE));
	    
//	    logger.log(building, Level.CONFIG, 0, "# of fish: " + numFish + "  # of weeds: " + numWeeds + ".");
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
		// B ut it is not essential food
		double demand = 2D * settlement.getNumCitizens();

		// Supply is total number of fish at settlement.
		double supply = 0D;
		boolean removedBuilding = false;
		for (Building building : settlement.getBuildingManager().getBuildings(FunctionType.FISHERY)) {
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
		    // Account for fish and weeds
		    simulatePond(pulse.getElapsed());

			// check for the passing of each day
			if (pulse.isNewSol()) {
				houseKeeping.resetCleaning();
				
				// Inspect every 2 days
				if ((pulse.getMarsTime().getMissionSol() % 2) == 0)
				{
					houseKeeping.resetInspected();
				}
			}
			
			weedAge += pulse.getElapsed();
		}
		return valid;
	}
	
	/**
	* Simulate life in the pond, using the values indicated in the
	* documentation.
	* 
	* @param fish
	*   Vector of fish
	* @param weeds
	*   Vector of weeds
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
		      nextFish.growPerFrame();
		      if (!nextFish.isAlive())
		         it.remove();
		   }
		
		   // Simulate the weeds
		   for (Plant p : weeds) {
			   p.growPerFrame();
		   }
	   }
	
	   // Create some new fish, according to the BIRTH_RATE constant
	   if (fish.size() < maxFish) {
		   birthIterationCache += BIRTH_RATE * time * fish.size() * (1 + .01 * RandomUtil.getRandomInt(-10, 10));
		   if (birthIterationCache > 1) {
			   int newFish = (int)birthIterationCache;
			   birthIterationCache = birthIterationCache - newFish;
			   for (i = 0; i < newFish; i++)
			       fish.add(new Herbivore(FISH_WEIGHT, 0, FISH_WEIGHT * FRACTION));		   
		   }
	   }	   
	}
	
	
	/**
	* Calculate the total mass of a collection of <CODE>Organism</CODE>s.
	* @param organisms
	*   a <CODE>Vector</CODE> of <CODE>Organism</CODE> objects
	* @param <T>
	*   component type of the elements in the organisms Vector
	* <b>Precondition:</b>
	*   Every object in <CODE>organisms</CODE> is an <CODE>Organism</CODE>.
	* @return
	*   the total mass of all the objects in <CODE>Organism</CODE> (in ounces).
	**/
	public static <T extends Organism> double totalMass(List<T> organisms) {
	   double answer = 0;
	   
	   for (Organism next : organisms)
	   {
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
		return tankSize * POWER_PER_LITRE;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * 
	 * @return power (kW)
	 */
	@Override
	public double getPoweredDownPowerRequired() {
		return tankSize * POWER_PER_LITRE;
	}


	@Override
	public double getMaintenanceTime() {
		return tankSize * 5D;
	}

	public List<String> getUninspected() {
		return houseKeeping.getUninspected();
	}

	public List<String> getUncleaned() {
		return houseKeeping.getUncleaned();
	}

	public void markInspected(String s) {
		houseKeeping.inspected(s);
	}

	public void markCleaned(String s) {
		houseKeeping.cleaned(s);
	}

	public int getNumFish() {
		return fish.size();
	}

	public int getTankSize() {
		return tankSize;
	}
	
	public double getWeedMass() {
		return Math.round(totalMass(weeds)/ OUNCE_PER_KG * 100.0)/100.0;
	}

	public int getSurplusStock() {
		return fish.size() - idealFish;
	}

	/**
	 * Spend some time on weed maintenance.
	 * @param workTime
	 * @return
	 */
	public double tendWeeds(double workTime) {
		double surplus = 0;
		weedTendertime -= workTime;
		if (weedTendertime < 0) {
			surplus = Math.abs(weedTendertime);
			weedTendertime = weeds.size() * TIME_PER_WEED;
			logger.log(building, Level.INFO, 1000, "Weeds fully tended for " + weedTendertime + " millisols.");
			weedAge = 0;
		}
		return surplus;
	}


	public double catchFish(Person fisher, double workTime) {
		if (fish.size() <= idealFish) {
			return workTime;
		}
		
		// Random
		int rand = RandomUtil.getRandomInt(fish.size());
		if (rand > idealFish) {
			// Catch one
			logger.log(building, fisher, Level.FINE, 0, "Fish caught, stock=" + fish.size(), null);
			Herbivore removed = fish.remove(1);
			
			// Fish stored as KG, 90% is useful
			store((removed.getSize() * 0.9D) * KG_PER_OUNCE, ResourceUtil.fishMeatID, "Fishery::catchFish");
			
			// Fish Oil is 1% of fish size, a guess
			store((removed.getSize() * 0.01D) * KG_PER_OUNCE, ResourceUtil.fishOilID, "Fishery::catchFish");
		}
		return 0;
	}


	/**
	 * What is the demand for the weeds to be tendered
	 * @return
	 */
	public int getWeedDemand() {
		return (int)weedAge / WEED_DEMAND;
	}
}

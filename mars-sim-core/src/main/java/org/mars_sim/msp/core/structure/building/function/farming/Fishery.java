/**
* Mars Simulation Project
 * Farming.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.HouseKeeping;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Fishery function that is responsble for aquetic farming
 */
public class Fishery extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(Fishery.class.getName());
	private static final String loggerName = logger.getName();
	private static final String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());

	
	private static final String [] INSPECTION_LIST = {"Environmental Control System",
													  "Any Traces of Contamination", "Task Integrity",
													  "Foundation",	"Structural Element", "Thermal Budget",
													  "Water and Irrigation System"};
	private static final String [] CLEANING_LIST = {"Equipment", "Task Glass", "Water Heater",
													"Pipings", "Valves"};
	

	// Convert from kg to ounce
	public static final double KG_PER_OUNCE = 0.02834952;
	// Convert from ounce to kg
	public static final double OUNCE_PER_KG = 35.27396195;
	// Initial size of each weed, in ounces 
	public static final double WEED_SIZE = 15;
	// Growth rate of weeds, in ounces/millisols  
	public static final double WEED_RATE = 0.000357;
	// Fish size, in ounces 
	public static final double FISH_SIZE = 50; 
	// A fish must eat FRACTION times its size during a frame, or it will die.
	public static final double FRACTION = 0.4;
	// At the end of each millisol, some fish have babies. The total number of new
	// fish born is the current number of fish times the BIRTH_RATE 
	// (rounded down to an integer).
	public static final double BIRTH_RATE = 0.000008;
	
	// Number of weeds in the pond
	private static final int MANY_WEEDS = 120;
	// Initial number of fish in the pond 
	private static final int INIT_FISH = 6;
	// Average number of weeds nibbled by a fish per frame
	private static final double AVERAGE_NIBBLES = 0.005;
	// Kw per litre of water
	private static final double POWER_PER_LITRE = 0.5D;

	/** The amount iteration for birthing fish */
	private double birthIterationCache;
	/** The amount iteration for nibbling weed */
	private double nibbleIterationCache;
	
	/** Size of tank in litres **/
	private int taskSize;

	
	/** A Vector of our fish. */
	private Vector<Herbivore> fish;   
	/** A Vector of our weeds. */
	private Vector<Plant> weeds;
	private HouseKeeping houseKeeping;
    
	/**
	 * Constructor.
	 * 
	 * @param building the building the function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public Fishery(Building building) {
		// Use Function constructor.
		super(FunctionType.FISHERY, building);
		
		houseKeeping = new HouseKeeping(CLEANING_LIST, INSPECTION_LIST);

		// Load activity spots
		//loadActivitySpots(buildingConfig.getFarmingActivitySpots(building.getBuildingType()));

		// Calcualte the tank size via config
		taskSize = 1000; 
		
		// Calculate fish & weeds by tanksize
	    int numFish = 0;
	    int numWeeds = 0;
	    if ("Inflatable Greenhouse".equalsIgnoreCase(building.getBuildingType())) {
	    	numFish = 1 + (int)((1 + .01 * RandomUtil.getRandomInt(-10, 10)) * INIT_FISH);
		    numWeeds = (int)((numFish * 30 + MANY_WEEDS)/2);
	    }
	    else {//if ("Large Greenhouse".equals(building.getBuildingType())) 
	    	numFish = 1 + (int)((1 + .01 * RandomUtil.getRandomInt(-10, 10)) * INIT_FISH * 5);
		    numWeeds = (int)((numFish * 30 + MANY_WEEDS * 5)/2);
	    }
	        
		fish = new Vector<Herbivore>(numFish);
	    weeds = new Vector<Plant>(numWeeds);
	    
	    int i;
	    // Initialize the bags of fish and weeds
	    for (i = 0; i < numFish; i++)
	       fish.addElement(new Herbivore(FISH_SIZE, 0, FISH_SIZE * FRACTION));
	    for (i = 0; i < numWeeds; i++)
	       weeds.addElement(new Plant(WEED_SIZE, WEED_RATE));
	}


	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingName the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function. Called by BuildingManager.java
	 *         getBuildingValue()
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding, Settlement settlement) {

		double result = 0D;
//
//		// Demand is farming area (m^2) needed to produce food for settlement
//		// population.
//		double requiredFarmingAreaPerPerson = CropConfig.getFarmingAreaNeededPerPerson();
//		double demand = requiredFarmingAreaPerPerson * settlement.getNumCitizens();
//
//		// Supply is total farming area (m^2) of all farming buildings at settlement.
//		double supply = 0D;
//		boolean removedBuilding = false;
//		List<Building> buildings = settlement.getBuildingManager().getBuildings(FARMING_FUNCTION);
//		for (Building building : buildings) {
//			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
//				removedBuilding = true;
//			} else {
//				Fishery farmingFunction = building.getFarming();
//				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
//				supply += farmingFunction.getGrowingArea() * wearModifier;
//			}
//		}
//
//		// Modify result by value (VP) of food at the settlement.
//		Good foodGood = GoodsUtil.getResourceGood(ResourceUtil.foodID);
//		double foodValue = settlement.getGoodsManager().getGoodValuePerItem(foodGood);
//
//		result = (demand / (supply + 1D)) * foodValue;

		return result;
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
		    simulatePond(fish, weeds, pulse.getElapsed());
	
			// check for the passing of each day
			if (pulse.isNewSol()) {
				houseKeeping.resetCleaning();
				
				// Inspect every 2 days
				if ((pulse.getMarsTime().getMissionSol() % 2) == 0)
				{
					houseKeeping.resetInspected();
				}
			}
		}
		return valid;
	}
	
	/**
	* Simulate life in the pond, using the values indicated in the
	* documentation.
	* @param fish
	*   Vector of fish
	* @param weeds
	*   Vector of weeds
	* @param time
	**/
	private void simulatePond(Vector<Herbivore> fish, Vector<Plant> weeds, double time) {
	   int i;
	   int manyIterations;
	   int index;
	   Herbivore nextFish;
	   Plant nextWeed;
	
	   int numFish = fish.size();
	   int numWeeds = weeds.size();
	   // Have randomly selected fish nibble on randomly selected plants
	   nibbleIterationCache += AVERAGE_NIBBLES * time * numFish;
	   
	   if (nibbleIterationCache > numFish) {
		   manyIterations = (int)nibbleIterationCache;
		   if (manyIterations > numFish * 3)
			   manyIterations = numFish * 3;
		   if (manyIterations < numFish)
			   manyIterations = numFish;
		   if (manyIterations > numWeeds)
			   manyIterations = numWeeds;
		   nibbleIterationCache = nibbleIterationCache - manyIterations;
//		   System.out.println("time: " + Math.round(time*100.0)/100.0 
//				   + "   nibbleIterationCache : " + Math.round(nibbleIterationCache*100.0)/100.0
//				   + "   manyIterations : " + Math.round(manyIterations*100.0)/100.0
//				   );
		   for (i = 0; i < manyIterations; i++) {
			   index = RandomUtil.getRandomInt(numFish-1);// (int) (RandomUtil.getRandomDouble(1.0) * fish.size()); //
			   nextFish = fish.elementAt(index);
			   index = RandomUtil.getRandomInt(numWeeds-1);// (int) (RandomUtil.getRandomDouble(1.0) * weeds.size()); //
			   nextWeed = weeds.elementAt(index);
			   nextFish.nibble(nextWeed);
		   } 
		   
		   // Simulate the fish
		   i = 0;
		   while (i < fish.size()) {
		      nextFish = fish.elementAt(i);
		      nextFish.growPerFrame();
		      if (nextFish.isAlive())
		         i++;
		      else
		         fish.removeElementAt(i);
		   }
		
		   // Simulate the weeds
		   for (i = 0; i < weeds.size(); i++) {
		      nextWeed = weeds.elementAt(i);
		      nextWeed.growPerFrame();
		   }
	   }
	
	   // Create some new fish, according to the BIRTH_RATE constant
	   birthIterationCache += BIRTH_RATE * time * fish.size() * (1 + .01 * RandomUtil.getRandomInt(-10, 10));
	   if (birthIterationCache > 1) {
		   manyIterations = (int)birthIterationCache;
		   birthIterationCache = birthIterationCache - manyIterations;
		   for (i = 0; i < manyIterations; i++)
		       fish.addElement(new Herbivore(FISH_SIZE, 0, FISH_SIZE * FRACTION));
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
	public static <T extends Organism> double totalMass(Vector<T> organisms) {
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
	public double getFullPowerRequired() {
		// Power (kW) required for normal operations.
		return taskSize * POWER_PER_LITRE;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * 
	 * @return power (kW)
	 */
	public double getPoweredDownPowerRequired() {
		return taskSize * POWER_PER_LITRE;
	}


	@Override
	public double getMaintenanceTime() {
		return taskSize * 5D;
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
	
	public double getWeedMass() {
		return Math.round(totalMass(weeds)/ OUNCE_PER_KG * 100.0)/100.0;
	}
}

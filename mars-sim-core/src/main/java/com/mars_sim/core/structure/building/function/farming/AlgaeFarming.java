/*
 * Mars Simulation Project
 * AlgaeFarming.java
 * @date 2023-09-19
 * @author Manny Kung
 */

package com.mars_sim.core.structure.building.function.farming;

import java.util.List;
import java.util.logging.Level;

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
 * This function that is responsible for farming algae.
 */
public class AlgaeFarming extends Function {	

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(AlgaeFarming.class.getName());

	
	private static final String [] INSPECTION_LIST = Fishery.INSPECTION_LIST;
	
	private static final String [] CLEANING_LIST = Fishery.CLEANING_LIST;
	
	// A fish must eat FRACTION times its size during a frame, or it will die.
//	public static final double FRACTION = 0.4;
	
	// Birth/growth rate of algae in kg/millisol
	public static final double BIRTH_RATE = 0.00005;
	
	// Max amount of food in the pond per kg of algae
	private static final double MAX_FOOD = 0.05;
	// Average amount of food nibbled by 1 kg of algae per frame
	private static final double AVERAGE_NIBBLES = 0.00001;
	// kW per litre of water
	private static final double POWER_PER_LITRE = 0.0001D;
	// kW per kg algae
	private static final double POWER_PER_KG_ALGAE = 0.001D;
	// kW per kg food mass
	private static final double POWER_PER_KG_FOOD = 0.0001D;
	// The rate of adding food when tending the pond 
	private static final double ADD_FOOD_RATE = 1.25;
	// Tend time for food
	private static final double TEND_TIME_FOR_FOOD = 0.2D;
	// Initial amount of water [in liters] per kg algae
	private static final int INITIAL_WATER_NEED_PER_KG_ALGAE = 333; 
	
	// Time before food is needed
	private static final int FOOD_DEMAND = 250;
	
	/** Number of fish as a percentage of maximum **/
	private static final double IDEAL_PERCENTAGE = 0.8D;

	/** Amount of algae per harvest **/
	private static final double KG_PER_HARVEST = 1;
	
	/** The amount iteration for growing new spirulina */
	private double birthIterationCache;
	
	/** Size of tank in liters **/
	private int tankSize;
	
	/** total mass in the tank **/
	private double totalMass;
	
	//TODO: will include the amount of water and the need to replenish water over time
	
	/** Maximum amount of algae **/
	private double maxAlgae;

	/** current amount of algae **/
	private double currentAlgae;
	
	/** current amount of food **/
	private double currentFood;
	
	/** Optimal amount of algae **/
	private double idealAlgae;
	
	/** Current health of algae **/	
	private double health = 1;

	/** How old are the food since the last tending **/
	private double foodAge = 0;
	
	private HouseKeeping houseKeeping;
	
	/** How long it has been tendered **/
	private double tendertime;
    
	/**
	 * Constructor.
	 * 
	 * @param building the building the function is for.
	 * @param spec Definition of the AlgaeFarming properties
	 * @throws BuildingException if error in constructing function.
	 */
	public AlgaeFarming(Building building, FunctionSpec spec) {
		// Use Function constructor.
		super(FunctionType.ALGAE_FARMING, spec, building);
		
		houseKeeping = new HouseKeeping(CLEANING_LIST, INSPECTION_LIST);

		// Calculate the tank size via config
		tankSize = spec.getCapacity();

		
		// Calculate max algae based on tank size
		maxAlgae = tankSize / INITIAL_WATER_NEED_PER_KG_ALGAE;
		
		currentAlgae = maxAlgae * IDEAL_PERCENTAGE;

		currentAlgae = RandomUtil.getRandomDouble(currentAlgae * 0.85, currentAlgae * 1.15);

	    double initalFood = currentAlgae * MAX_FOOD * 2.5;
	    
	    // Healthy stock is the initial number of fish
	    idealAlgae = currentAlgae;
		tendertime = initalFood * TEND_TIME_FOR_FOOD;
	    
		// Give variation to the amount of weeds and fish at the start for each tank
		initalFood = RandomUtil.getRandomDouble(initalFood * 0.85, initalFood * 1.15);
		currentFood = initalFood;
		
		totalMass = tankSize + currentAlgae + initalFood;
		
		foodAge = 0;

	    logger.log(building, Level.CONFIG, 0, "algae: " 
	    		+ Math.round(currentAlgae * 10.0)/10.0 
	    		+ " kg.  food: " + Math.round(initalFood * 10.0)/10.0 + " kg.");
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
		for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.ALGAE_FARMING)) {
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(type) && !removedBuilding) {
				removedBuilding = true;
			} else {
				AlgaeFarming algaeFarm = building.getAlgae();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += algaeFarm.getCurrentAlgae() * wearModifier;
			}
		}

		// Modify result by value (VP) of spirulina at the settlement.
		double foodValue = settlement.getGoodsManager().getGoodValuePoint(ResourceUtil.spirulinaID);

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
				if ((pulse.getMarsTime().getMissionSol() % 2) == 0) {
					houseKeeping.resetInspected();
				}
			}
			
			foodAge += pulse.getElapsed();
		}
		return valid;
	}
	
	/**
	* Simulate life in the pond, using the values indicated in the
	* documentation.
	*
	* @param time
	**/
	private void simulatePond(double time) {
	   
	   double nibbleAmount = AVERAGE_NIBBLES * time * currentAlgae;
	   
	   // Eat food
	   currentFood = currentFood - RandomUtil.getRandomDouble(.9, 1.1) * nibbleAmount;	   
	
	   // Create new spirulina, using BIRTH_RATE
	   if (currentAlgae < maxAlgae && currentFood > 0) {
		   birthIterationCache += BIRTH_RATE * time * currentAlgae 
				   * (1 + .01 * RandomUtil.getRandomInt(-10, 10));
		   if (birthIterationCache > 1) {
			   double newAlgae = birthIterationCache;
			   birthIterationCache = birthIterationCache - newAlgae;
			   currentAlgae += newAlgae;
			   totalMass += newAlgae;
		   }
	   }	   
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	@Override
	public double getFullPowerRequired() {
		// Power (kW) required for normal operations.
		return tankSize * POWER_PER_LITRE + getCurrentAlgae() * POWER_PER_KG_ALGAE + getFoodMass() * POWER_PER_KG_FOOD;
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

	public double getCurrentAlgae() {
		return currentAlgae;
	}

	public double getIdealAlgae() {
		return idealAlgae;
	}
	
	public double getMaxAlgae() {
		return maxAlgae;
	}
	
	public int getTankSize() {
		return tankSize;
	}
	
	public double getFoodMass() {
		return currentFood;
	}

	/**
	 * Gets the ratio of current algae mass to ideal algae mass.
	 * 
	 * @return
	 */
	public double getSurplusRatio() {
		return currentAlgae / idealAlgae;
	}

	/**
	 * Spends some time on tending algae.
	 * 
	 * @param workTime
	 * @return
	 */
	public double tending(double workTime) {
		double surplus = 0;
		currentFood += workTime * ADD_FOOD_RATE;
		tendertime -= workTime;
		if (tendertime < 0) {
			surplus = Math.abs(tendertime);
			tendertime = currentFood * TEND_TIME_FOR_FOOD;
			logger.log(building, Level.INFO, 10_000, "Algae fully tended for " 
					+ Math.round(tendertime * 100.0)/100.0 + " millisols.");
			foodAge = 0;
		}
		return surplus;
	}


	/**
	 * Harvests some algae.
	 *  
	 * @param worker
	 * @param workTime
	 * @return
	 */
	public double harvestAlgae(Worker worker, double workTime) {
		if (getSurplusRatio() < 0.5) {
			return workTime;
		}
		
		int rand = RandomUtil.getRandomInt((int)currentAlgae);
		if (rand > idealAlgae * 0.25) {
			
			// Harvesting a certain amount (~ 1 kg)
			double harvested = RandomUtil.getRandomDouble(.9, 1.1) * KG_PER_HARVEST;
			
			logger.log(building, worker, Level.INFO, 5000, "Harvesting " 
					+ Math.round(harvested * 100.0)/100.0 
					+ " kg algae. Stock: " + currentAlgae, null);
			
			currentAlgae = currentAlgae - harvested;
			
			totalMass = totalMass - harvested;
			
			double spirulinaExtracted = harvested * RandomUtil.getRandomDouble(.1, .2) * health;
			
			double waste = harvested - spirulinaExtracted;
			
			store(waste, ResourceUtil.foodWasteID, "AlgaeFarming::harvestAlgae");
			
			store(harvested, ResourceUtil.spirulinaID, "AlgaeFarming::harvestAlgae");
			
		}
		return 0;
	}


	/**
	 * What is the demand for food ?
	 * 
	 * @return
	 */
	public double getFoodDemand() {
		return foodAge / FOOD_DEMAND;
	}
}

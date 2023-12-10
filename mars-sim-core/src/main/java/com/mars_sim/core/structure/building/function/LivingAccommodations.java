/*
 * Mars Simulation Project
 * LivingAccommodations.java
 * @date 2023-11-24
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.data.SolSingleMetricDataLogger;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingException;
import com.mars_sim.core.structure.building.FunctionSpec;
import com.mars_sim.core.structure.building.function.ActivitySpot.AllocatedSpot;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.mapdata.location.LocalPosition;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The LivingAccommodations class is a building function for a living
 * accommodations.
 */
public class LivingAccommodations extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
	private static SimLogger logger = SimLogger.getLogger(LivingAccommodations.class.getName());

	public static final int MAX_NUM_SOLS = 14;

	public static final double TOILET_WASTE_PERSON_SOL = .02D;
	public static final double WASH_AND_WASTE_WATER_RATIO = .85D;
	/** The minimal amount of resource to be retrieved. */
	private static final double MIN = 0.0001;
	/** The chance of going to a restroom. */
	public static final int TOILET_CHANCE = 50;
	
	private static final String WASTE_NAME = "LivingAccomodation::generateWaste";
	
	/** max # of beds. */
	private int maxNumBeds;
	/** max # of guest beds. */
	private int maxGuestBeds;
	/** The average water used per person for washing (showers, washing clothes, hands, dishes, etc) [kg/sol].*/
	private double washWaterUsage;
	// private double wasteWaterProduced; // Waste water produced by
	// urination/defecation per person per millisol (avg over Sol).
	/** percent portion of grey water generated from waste water.*/
	private double greyWaterFraction;
	/** The estimated water usage. */
	private double estimatedWaterUsed;
	/** The estimated waste water produced. */
	private double estimatedWasteWaterProduced;

	/** The guest beds in this facility, using settlement-wide position. */
	private Map<Integer, LocalPosition> guestBeds = new ConcurrentHashMap<>();
	
	/** The daily water usage in this facility [kg/sol]. */
	private SolSingleMetricDataLogger dailyWaterUsage;
	
	/** The daily grey water generated in this facility [kg/sol]. */
	private SolSingleMetricDataLogger greyWaterGen;

	/**
	 * Constructor.
	 *
	 * @param building the building this function is for.
	 * @param spec Details of the Living details
	 * @throws BuildingException if error in constructing function.
	 */
	public LivingAccommodations(Building building, FunctionSpec spec) {
		// Call Function constructor.
		super(FunctionType.LIVING_ACCOMMODATIONS, spec, building);

		dailyWaterUsage = new SolSingleMetricDataLogger(MAX_NUM_SOLS);
		
		greyWaterGen = new SolSingleMetricDataLogger(MAX_NUM_SOLS);
		// Loads the max # of regular beds available
		maxNumBeds = spec.getCapacity();
		
		Set<LocalPosition> bedSet = spec.getBuildingSpec().getBeds();
		
		for (LocalPosition loc: bedSet) {
			// Convert to settlement position
			LocalPosition bedLoc = LocalAreaUtil.getLocalRelativePosition(loc, building);
			guestBeds.put(-1, bedLoc);
		}
		
		// Loads the max # of guest beds available
		maxGuestBeds = bedSet.size();
		
		// Loads the wash water usage kg/sol
		washWaterUsage = personConfig.getWaterUsageRate();
		// Loads the grey to black water ratio
		double grey2BlackWaterRatio = personConfig.getGrey2BlackWaterRatio();
		// Calculate the grey water fraction
		greyWaterFraction = grey2BlackWaterRatio / (grey2BlackWaterRatio + 1);
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

		// Demand is two beds for every inhabitant (with population expansion in mind).
		double demand = settlement.getNumCitizens() * 2D;

		double supply = 0D;
		boolean removedBuilding = false;
		Iterator<Building> i = settlement.getBuildingManager().getBuildingSet(FunctionType.LIVING_ACCOMMODATIONS).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += building.getLivingAccommodations().getTotalBeds() * wearModifier;
			}
		}

		double value = demand / (supply + 1D) / 5;

		return value * buildingConfig.getFunctionSpec(buildingName, FunctionType.LIVING_ACCOMMODATIONS).getCapacity();
	}

	/**
	 * Gets the max number of regular beds in the living accommodations.
	 *
	 * @return
	 */
	public int getBedCap() {
		return maxNumBeds;
	}

	/**
	 * Gets the max number of guest beds in the living accommodations.
	 *
	 * @return
	 */
	public int getMaxGuestBeds() {
		return maxGuestBeds;
	}
	
	/**
	 * Gets the total number of beds (regular and guest) in the living accommodations.
	 *
	 * @return
	 */
	public int getTotalBeds() {
		return maxNumBeds + maxGuestBeds;
	}

	
	/**
	 * Gets the number of assigned beds in this building.
	 *
	 * @return number of assigned beds
	 */
	public int getNumAssignedBeds() {
		return getNumOccupiedActivitySpots();
	}

	/**
	 * Gets the total number of guest beds in this building.
	 *
	 * @return total number of guest beds
	 */
	public int getTotalGuestBeds() {
		return guestBeds.size();
	}
	/**
	 * Gets the number of people registered to sleep in this building.
	 *
	 * @return number of registered sleepers
	 */
	public int getRegisteredSleepers() {
		return getNumOccupiedActivitySpots();
	}

	/**
	 * Assigns standard living necessity.
	 */
	public void	assignStandardNecessity(Person person) {

		// Obtain a standard set of clothing items
		person.wearGarment(building.getSettlement());

		// Assign thermal bottle
		person.assignThermalBottle();
	}
	
	/**
	 * Registers a sleeper with a bed.
	 *
	 * @param person
	 * @param isAGuest is this person a guest (not inhabitant) of this settlement
	 * @return the bed registered with the given person
	 */
	public LocalPosition registerSleeper(Person person, boolean isAGuest) {
		// Assign standard necessity
		assignStandardNecessity(person);	
		
		ActivitySpot registeredBed = person.getBed();

		if (registeredBed == null) {

			if (getNumEmptyActivitySpots() > 0) {
				LocalPosition bed = designateABed(person, isAGuest);
				if (bed != null) {
					return bed;
				}

				logger.log(building, person, Level.WARNING, 2000,
								   "Could not find a temporary bed.", null);
			}
		}

		else {
			return registeredBed.getPos();
		}

		return null;
	}

	/**
	 * Assigns/designates an available bed to a person.
	 *
	 * @param person
	 * @return
	 */
	private LocalPosition designateABed(Person person, boolean guest) {
		LocalPosition bedLoc = null;

		int numDesignated = getNumAssignedBeds();
		if (numDesignated >= maxNumBeds) {
			return null;
		}
		
		// Note: guest beds are reserved for temporary use and 
		// are not assigned here for use here
		Set<ActivitySpot> spots = getActivitySpots();
		for (var sp : spots) {
			if (sp.isEmpty()) {
				if (!guest) {
					// Claim the bed permanently
					AllocatedSpot bed = sp.claim(person, true);
					person.setBed(building, bed);
				}
				logger.log(building, person, Level.INFO, 0, "Given a bed ("
							+ sp.getName() + ").");
				
				bedLoc = sp.getPos();
				break;
			}
		}

		return bedLoc;
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
			int rand = RandomUtil.getRandomInt(TOILET_CHANCE);
			if (rand == 0) {
				generateWaste(pulse.getElapsed());
			}
		}
		return valid;
	}

	/**
	 * Adds to the daily water usage.
	 *
	 * @param amount
	 */
	public void addDailyWaterUsage(double amount) {
		dailyWaterUsage.increaseDataPoint(amount);
	}

	/**
	 * Gets the daily average water usage of the last 5 sols.
	 * Note: most weight on yesterday's usage. Least weight on usage from 5 sols ago.
	 *
	 * @return
	 */
	public double getDailyAverageWaterUsage() {
		return dailyWaterUsage.getDailyAverage();
	}

	/**
	 * Adds to the daily grey water generated.
	 *
	 * @param amount
	 */
	public void addDailyGreyWaterGen(double amount) {
		greyWaterGen.increaseDataPoint(amount);
	}

	/**
	 * Gets the daily average grey water generated of the last 5 sols.
	 * Note: most weight on yesterday's usage. Least weight on usage from 5 sols ago.
	 *
	 * @return
	 */
	public double getDailyAverageGreyWaterGen() {
		return greyWaterGen.getDailyAverage();
	}
	
	/**
	 * Utilizes water for bathing, washing, etc based on population.
	 *
	 * @param time amount of time passing (millisols)
	 */
	private void generateWaste(double time) {

		// Remove wash water from settlement.
		if (estimatedWaterUsed > MIN) {
			retrieve(estimatedWaterUsed, WATER_ID, true);
			// Track daily average
			addDailyWaterUsage(estimatedWaterUsed);
		}

		// Grey water is produced by wash water.
		double greyWaterProduced = estimatedWasteWaterProduced * greyWaterFraction;
		// Black water is only produced by waste water.
		double blackWaterProduced = estimatedWasteWaterProduced * (1 - greyWaterFraction);

		if (greyWaterProduced > MIN) {
			store(greyWaterProduced, GREY_WATER_ID, WASTE_NAME);
			// Track daily average
			addDailyGreyWaterGen(greyWaterProduced);
		}
		
		if (blackWaterProduced > MIN)
			store(blackWaterProduced, BLACK_WATER_ID, WASTE_NAME);

		// Use toilet paper and generate toxic waste (used toilet paper).
		double toiletPaperUsagePerMillisol = TOILET_WASTE_PERSON_SOL / 1000;

		double toiletPaperUsageBuilding = toiletPaperUsagePerMillisol * time 
				*  getNumAssignedBeds() * (1 + RandomUtil.getRandomDouble(0.5));	

		if (toiletPaperUsageBuilding > MIN) {
			retrieve(toiletPaperUsageBuilding, TOILET_TISSUE_ID, true);
			store(toiletPaperUsageBuilding, TOXIC_WASTE_ID, WASTE_NAME);
		}
	}

	/**
	 * Calculates the water usage level.
	 * 
	 * @param time
	 * @return
	 */
	public double[] calculateWaterLevel(double time) {
		Settlement settlement = building.getSettlement();
		double randomness = 1 + RandomUtil.getRandomDouble(0.5);
		int numBed = getNumAssignedBeds();
		double portion = 1;
		
		// If settlement is rationing water, reduce water usage according to its level
		int level = settlement.getWaterRationLevel();
		if (level != 0)
			portion = 1.0 / level;
		
		// Account for people who are out there in an excursion and NOT in the
		// settlement
		double absenteeFactor = (double)settlement.getIndoorPeopleCount() 
				/ settlement.getPopulationCapacity();

		double usage =  washWaterUsage * time / 1_000 * numBed * absenteeFactor;
		
		estimatedWaterUsed = usage * RandomUtil.getRandomDouble(TOILET_CHANCE) 
				* randomness * portion;
		
		estimatedWasteWaterProduced = estimatedWaterUsed * WASH_AND_WASTE_WATER_RATIO;
		
		return new double[] {estimatedWaterUsed, estimatedWasteWaterProduced};
	}

	/*
	 * Checks if an unmarked or unassigned bed is available
	 */
	public boolean hasAnUnmarkedBed() {
        return getNumAssignedBeds() < maxNumBeds || hasEmptyGuestBed();
	}

	/**
	 * Checks if there is an empty guest bed.
	 *  
	 * @return
	 */
	public boolean hasEmptyGuestBed() {
		for (int i: guestBeds.keySet()) {
			if (i == -1) 
				return true;
		}
		return false;
	}
	
	/**
	 * Gets an empty guest bed.
	 * 
	 * @return
	 */
	public LocalPosition getEmptyGuestBed() {
		for (int i: guestBeds.keySet()) {
			if (i == -1) 
				return guestBeds.get(i);
		}
		return null;
	}
	
	/**
	 * Registers a guest bed.
	 * 
	 * @param id
	 * @return
	 */
	public LocalPosition registerGuestBed(int id) {
		for (Entry<Integer, LocalPosition> entry: guestBeds.entrySet()) {
			int oldID = entry.getKey();
			if (oldID == -1) {
				LocalPosition p = entry.getValue();
				guestBeds.put(id, p);
				return p;
			}
		}
		return null;
	}
	
	/**
	 * Deregisters a guest bed.
	 * 
	 * @param id
	 */
	public void deRegisterGuestBed(int id) {
		for (Entry<Integer, LocalPosition> entry: guestBeds.entrySet()) {
			int oldID = entry.getKey();
			if (oldID == id) {
				LocalPosition p = entry.getValue();
				guestBeds.put(-1, p);
			}
		}
	}
	
	
	@Override
	public double getMaintenanceTime() {
		return maxNumBeds * 7D;
	}

	@Override
	public void destroy() {
		building = null;
		guestBeds.clear();
		guestBeds = null;
		dailyWaterUsage = null;

		super.destroy();
	}
}

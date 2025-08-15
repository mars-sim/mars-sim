/*
 * Mars Simulation Project
 * LivingAccommodation.java
 * @date 2023-11-24
 * @author Scott Davis
 */
package com.mars_sim.core.building.function;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.FunctionSpec;
import com.mars_sim.core.building.function.ActivitySpot.AllocatedSpot;
import com.mars_sim.core.data.SolSingleMetricDataLogger;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The LivingAccommodation class is a building function for a living
 * accommodation.
 */
public class LivingAccommodation extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
	private static SimLogger logger = SimLogger.getLogger(LivingAccommodation.class.getName());

	public static final int MAX_NUM_SOLS = 14;

	public static final double TOILET_WASTE_PERSON_SOL = .05D;
	public static final double WASH_AND_WASTE_WATER_RATIO = .85D;
	/** The minimal amount of resource to be retrieved. */
	private static final double MIN = 0.0001;
	/** The chance of going to a restroom. */
	public static final int TOILET_CHANCE = 50;
	
	private static final String WASTE_NAME = "LivingAccomodation::generateWaste";
	
	/** Can this be used as a bunk house for guests */
	private boolean guesthouse;
	/** The flag to see if a water ration review is due. */
	private boolean waterRatioReviewFlag = false;
	
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
	public LivingAccommodation(Building building, FunctionSpec spec) {
		// Call Function constructor.
		super(FunctionType.LIVING_ACCOMMODATION, spec, building);

		dailyWaterUsage = new SolSingleMetricDataLogger(MAX_NUM_SOLS);
		
		greyWaterGen = new SolSingleMetricDataLogger(MAX_NUM_SOLS);

		// Loads the wash water usage kg/sol
		washWaterUsage = personConfig.getWaterUsageRate();

		// Loads the grey to black water ratio. It's 80% and 20%, or 4 to 1
		double grey2BlackWaterRatio = personConfig.getGrey2BlackWaterRatio();
		// Assume black water has 1 portion
		double blackWater = 1;
		// Then grey water has 4 portion
		double greyWater = grey2BlackWaterRatio;

		// Calculate the grey water fraction
		greyWaterFraction = greyWater / (greyWater + blackWater);
		
		guesthouse = spec.getBoolProperty("guesthouse", false);
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
		Iterator<Building> i = settlement.getBuildingManager().getBuildingSet(FunctionType.LIVING_ACCOMMODATION).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += building.getLivingAccommodation().getBedCap() * wearModifier;
			}
		}

		double value = demand / (supply + 1D) / 5;

		return value * buildingConfig.getFunctionSpec(buildingName, FunctionType.LIVING_ACCOMMODATION).getCapacity();
	}

	/**
	 * Gets the max number of regular beds in the living accommodation.
	 *
	 * @return
	 */
	public int getBedCap() {
		return getActivitySpots().size();
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
	 * Gets the number of people registered to sleep in this building.
	 *
	 * @return number of registered sleepers
	 */
	public int getRegisteredSleepers() {
		return getNumOccupiedActivitySpots();
	}

	/**
	 * Assigns/designates an available bed to a person.
	 *
	 * @param person
	 * @return
	 */
	private AllocatedSpot assignBed(Person person, boolean permanent) {		

		// Note: guest beds are reserved for temporary use and 
		// are not assigned here for use here
		Set<ActivitySpot> spots = getActivitySpots();
		for (var sp : spots) {
			if (sp.isEmpty()) {
				// Claim the bed
				AllocatedSpot bed = sp.claim(person, permanent, building);
				if (permanent) {
					person.setBed(bed);
				}
				
				logger.log(person, Level.INFO, 0, "Assigned with " + sp.getName() + " as "
							+ (permanent ? "permanent" : "temporary")
							+ " bed at " + sp.getPos() + " in " + building.getName() + ".");
	
				return bed;
			}
		}

		return null;
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
			if (pulse.isNewHalfSol()) {
				// Reset the water ratio flag to allow for next review
				unlockWaterRatioReview();
			}
		}
		return valid;
	}
	
	/**
	 * Locks the flag for reviewing water ratio. Won't be able to review until it's unlocked.
	 * 
	 * @param value
	 */
	public void lockWaterRatioReview() {
		waterRatioReviewFlag = false;
	}
	
	/**
	 * Unlocks the flag for reviewing water ratio. Open for review until it's locked.
	 * 
	 * @param value
	 */
	public void unlockWaterRatioReview() {
		waterRatioReviewFlag = true;
	}
	
	/**
	 * Returns if the water ratio has been reviewed.
	 * 
	 * @return
	 */
	public boolean canReviewWaterRatio() {
		return waterRatioReviewFlag;
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
	public void generateWaste(double time) {
		
		// Remove wash water from settlement.
		if (estimatedWaterUsed > MIN) {
			retrieve(estimatedWaterUsed, ResourceUtil.WATER_ID, true);
			// Track daily average
			addDailyWaterUsage(estimatedWaterUsed);
		}

		// Grey water is produced by wash water.
		double greyWaterProduced = estimatedWasteWaterProduced * greyWaterFraction;
		// Black water is only produced by waste water.
		double blackWaterProduced = estimatedWasteWaterProduced * (1 - greyWaterFraction);

		if (greyWaterProduced > MIN) {
			store(greyWaterProduced, ResourceUtil.GREY_WATER_ID, WASTE_NAME);
			// Track daily average
			addDailyGreyWaterGen(greyWaterProduced);
		}
		
		if (blackWaterProduced > MIN)
			store(blackWaterProduced, ResourceUtil.BLACK_WATER_ID, WASTE_NAME);

		// Use toilet paper and generate toxic waste (used toilet paper).
		double toiletPaperUsagePerMillisol = TOILET_WASTE_PERSON_SOL / 1000;

		double toiletPaperUsageBuilding = toiletPaperUsagePerMillisol * time 
				*  getNumAssignedBeds() * (1 + RandomUtil.getRandomDouble(-0.5, 0.5));	

		if (toiletPaperUsageBuilding > MIN) {
			retrieve(toiletPaperUsageBuilding, ResourceUtil.TOILET_TISSUE_ID, true);
			store(toiletPaperUsageBuilding, ResourceUtil.TOXIC_WASTE_ID, WASTE_NAME);
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
		// Get the # of beds in this building
		int numBed = getNumAssignedBeds();
		double portion = 1;
		
		// If settlement is rationing water, reduce water usage according to its level
		int level = settlement.getRationing().getRationingLevel();
		if (level != 0)
			portion = 1.0 / level;
		
		// Account for people who are out there in an excursion and NOT in the
		// settlement
		// Note: Will starting using absenteeFactor after accounting for wastes
		// generated in vehicles on mission
		double absenteeFactor = 1; 

		double usage = washWaterUsage * time / 1_000 * numBed * absenteeFactor;
		
		estimatedWaterUsed = usage * RandomUtil.getRandomDouble(TOILET_CHANCE / 3D, TOILET_CHANCE * 3D) * portion;
		
		estimatedWasteWaterProduced = estimatedWaterUsed * WASH_AND_WASTE_WATER_RATIO;
		
		return new double[] {estimatedWaterUsed, estimatedWasteWaterProduced};
	}

	@Override
	public double getMaintenanceTime() {
		return getActivitySpots().size() * 7D;
	}

	/**
	 * Allocates a bed for sleeping.
	 * 
	 * @param settlement
	 * @param p the person
	 * @param permanent
	 */
	public static AllocatedSpot allocateBed(Settlement settlement, Person p, boolean permanent) {
		boolean guest = (!settlement.equals(p.getAssociatedSettlement()));
		if (guest) {
			permanent = false;
		}

		var blgManager = settlement.getBuildingManager();
		Set<Building> dorms = blgManager.getBuildingSet(FunctionType.LIVING_ACCOMMODATION);
		if (dorms.isEmpty()) {
			return null;
		}

		LivingAccommodation guestHouse = null;
		for (Building b : dorms) {
			// If looking for permanent then find an unassigned ActivitySpot
			LivingAccommodation lvi = b.getLivingAccommodation();
			if ((lvi.getNumEmptyActivitySpots() > 0)
				&& (permanent || (guest && lvi.isGuestHouse()))) {
				return lvi.assignBed(p, permanent);
			}

			if (lvi.isGuestHouse()) {
				guestHouse = lvi;
			}
		}

		// No bed found
		if (guestHouse == null) {
			if (guest) {
				logger.warning(p, "No guest bed found.");
				return null;
			}
			guestHouse = RandomUtil.getARandSet(dorms).getLivingAccommodation();
		}

		logger.config(p, "No bed assigned.");

		// Pick a random bed in the guest house; unlikely to arrive here
		return RandomUtil.getARandSet(guestHouse.getActivitySpots()).claim(p, false,
									  guestHouse.getBuilding());
	}

	/**
	 * Can guest stay here and squat on allocated beds ?
	 * 
	 * @return
	 */
	public boolean isGuestHouse() {
		return guesthouse;
	}

	@Override
	public void destroy() {
		building = null;
		dailyWaterUsage = null;

		super.destroy();
	}


}

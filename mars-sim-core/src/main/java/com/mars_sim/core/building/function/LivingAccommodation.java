/*
 * Mars Simulation Project
 * LivingAccommodation.java
 * @date 2025-08-20
 * @author Scott Davis
 */
package com.mars_sim.core.building.function;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.config.FunctionSpec;
import com.mars_sim.core.building.function.ActivitySpot.AllocatedSpot;
import com.mars_sim.core.data.SolSingleMetricDataLogger;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.GenderType;
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
	/** The chance of going to a restroom. */
	public static final int TOILET_CHANCE = 50;
	
	public static final double TOILET_WASTE_PERSON_SOL = .05D;
	
	public static final double WASH_AND_WASTE_WATER_RATIO = .85D;
	/** The minimal amount of resource to be retrieved. */
	private static final double MIN = 0.0001;

	private static final String WASTE_NAME = "LivingAccomodation::generateWaste";
	
	/** Can this be used as a bunk house for guests */
	private boolean guesthouse;
	/** The flag to see if a water ration review is due. */
	private boolean waterRatioReviewFlag = false;
	
	/** The cache for the last millisol. */
	private int millisolIntCache;
	
	/** The average water used per person for washing (showers, washing clothes, hands, dishes, etc) [kg/sol].*/
	private double washWaterUsage;
	/** percent portion of grey water generated from waste water.*/
	private double greyWaterFraction;
	
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
	 * @param permanent
	 * @param bypassGender if this is false and it's a bunk bed, it will select the same gender.
	 * @return
	 */
	private AllocatedSpot assignBed(Person person, boolean permanent, boolean bypassGender) {		
		List<ActivitySpot> spots = new ArrayList<>(getActivitySpots());
		if (spots.isEmpty())
			return null;
		
		GenderType type = person.getGender();

		
		int size = spots.size();
		
		for (int i = 0; i < size; i++) {
			ActivitySpot sp0 = null;
			GenderType type0 = null;
			String bedName0 = null;
			
			if (!bypassGender) {
				if (i - 1 >= 0) {
					sp0 = spots.get(i);
					bedName0 = sp0.getName();
					int id0 = sp0.getID();
					type0 = getGenderType(person.getSettlement(), id0);
				}
			}
			
			ActivitySpot sp = spots.get(i);
			String bedName = sp.getName();
			boolean goodToGo = false;
			
			if (sp.isEmpty()) {
				if (bypassGender) {
					goodToGo = true;
				}
				else if (sp0 != null && !sp0.isEmpty()) {
					if (bedName0 != null) {
						if (bedName0.equals(bedName) && type0 == type) {
							goodToGo = true;
						}
						// goodToGo continues to be false. Go to the next bed
					} 
					else {
						goodToGo = true;
					}		
				}
				else {
					goodToGo = true;
				}
				
				if (goodToGo) {
					// Claim the bed
					boolean isLastOne = (i == size - 1);
					return getBed(person, permanent, sp, isLastOne);
				}
			}
		}
			
		logger.log(person, Level.INFO, 0, "No empty bed available in " + building.getName() + ".");

		return null;
	}

	/**
	 * Gets a bed allocated.
	 * 
	 * @param person
	 * @param permanent
	 * @param sp
	 * @param isLastOne
	 * @return
	 */
	private AllocatedSpot getBed(Person person, boolean permanent, ActivitySpot sp, boolean isLastOne) {
		// Claim the bed
		AllocatedSpot bed = sp.claim(person, permanent, this);
		if (bed != null) {
			if (permanent) {
				person.registerBed(bed);
				return bed;
			}
			else if (isLastOne) {
				// This is the very last spot.
				// Note: if not permanent, do NOT call registerBed 
				logger.log(person, Level.INFO, 0, "Assigned " + sp.getName() + " as "
					+ " at " + sp.getPos() + " for temporary use in " + building.getName() + ".");
			}
		}
		return bed;
	}
	
	/**
	 * Gets the gender type of the person with an id.
	 * 
	 * @param s
	 * @param personID
	 * @return
	 */
	public GenderType getGenderType(Settlement s, int personID) {
		GenderType type = null;
		for (Person p: s.getCitizens()) {
			if (p.getIdentifier() == personID) {
				return p.getGender();
			}
		}
		return type;
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
			
			int now = pulse.getMarsTime().getMillisolInt();	
			
			if (now != millisolIntCache && now != 1000 && now % 20 == 5) {
				int timeSpan = now - millisolIntCache;
				if (timeSpan < 0)
					timeSpan += 1000;
	
				generateWaste(timeSpan);
				
				millisolIntCache = now;
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

		// Future: need the real data collected by the bathroom. 
		
		// Get the # of beds in this building
		int numBed = getNumAssignedBeds();
		double portion = 1;
		
		// If settlement is rationing water, reduce water usage according to its level
		int level = building.getSettlement().getRationing().getRationingLevel();
		if (level != 0)
			portion = 1.0 / level;
		
		// Account for people who are out there in an excursion and NOT in the
		// settlement
		double absenteeFactor = 1; 
		
		// Note: Will starting using absenteeFactor after accounting for wastes
		// generated in vehicles on mission
		
		double usage = washWaterUsage * time / 1_000 * numBed * absenteeFactor;
		
		double estimatedWaterUsed = usage * portion;			
		
		double estimatedWasteWaterProduced = estimatedWaterUsed * WASH_AND_WASTE_WATER_RATIO;
		
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
		AllocatedSpot spot = null;
		
		for (Building b : dorms) {
			// If looking for permanent then find an unassigned ActivitySpot
			LivingAccommodation lvi = b.getLivingAccommodation();

			
			if ((lvi.getNumEmptyActivitySpots() > 0)
				&& (permanent || (guest && lvi.isGuestHouse()))) {
				// Match the gender when assigning a bed
				spot = lvi.assignBed(p, permanent, false);
				return spot;
			}
		}

		if (spot == null) {		
			for (Building b : dorms) {
				// If looking for permanent then find an unassigned ActivitySpot
				LivingAccommodation lvi = b.getLivingAccommodation();
	
				if (spot == null && (lvi.getNumEmptyActivitySpots() > 0)
					&& (permanent || (guest && lvi.isGuestHouse()))) {		
					// Bypass matching the gender when assigning a bed
					spot = lvi.assignBed(p, permanent, true);
					return spot;
				}
				
				if (lvi.isGuestHouse()) {
					guestHouse = lvi;
				}
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

		logger.config(p, "No permanent bed found that can be assigned.");

		// Pick a random bed in the guest house; unlikely to arrive here
		AllocatedSpot guestBed = RandomUtil.getARandSet(guestHouse.getActivitySpots()).claim(p, false,
									  guestHouse);
		
		if (guestBed != null) {
			logger.config(p, "Given a guest bed at " + guestBed.toString());
		}
		
		return guestBed;
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

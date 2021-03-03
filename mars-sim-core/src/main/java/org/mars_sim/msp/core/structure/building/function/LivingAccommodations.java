/**
 * Mars Simulation Project
 * LivingAccommodations.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.mars_sim.msp.core.data.SolSingleMetricDataLogger;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The LivingAccommodations class is a building function for a living
 * accommodations.
 */
public class LivingAccommodations extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
	private static SimLogger logger = SimLogger.getLogger(LivingAccommodations.class.getName());

	
	public static final int MAX_NUM_SOLS = 14;
	
	public static final double TOILET_WASTE_PERSON_SOL = .02D;
	public static final double WASH_AND_WASTE_WATER_RATIO = .85D;
	/** The minimal amount of resource to be retrieved. */
	private static final double MIN = 0.00001;
	/** 1/5 of chance of going to a restroom per frame */
	public static final int TOILET_CHANCE = 20;

	/** max # of beds. */
	private int maxNumBeds; 
	/** The # of registered sleepers. */
	private int registeredSleepers;
	/** The average water used per person for washing (showers, washing clothes, hands, dishes, etc) [kg/sol].*/
	private double washWaterUsage;
	// private double wasteWaterProduced; // Waste water produced by
	// urination/defecation per person per millisol (avg over Sol).
	/** percent portion of grey water generated from waste water.*/
	private double greyWaterFraction; 

	/** The bed registry in this facility. */
	private transient Map<Person, Point2D> assignedBeds = new ConcurrentHashMap<>();

	/** The daily water usage in this facility [kg/sol]. */
	private SolSingleMetricDataLogger dailyWaterUsage;

	private static int waterID = ResourceUtil.waterID;
	private static int blackWaterID = ResourceUtil.blackWaterID;
	private static int greyWaterID = ResourceUtil. greyWaterID;
	
	/**
	 * Constructor
	 * 
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public LivingAccommodations(Building building) {
		// Call Function constructor.
		super(FunctionType.LIVING_ACCOMMODATIONS, building);
	
		dailyWaterUsage = new SolSingleMetricDataLogger(MAX_NUM_SOLS);	
		// Loads the max # of beds available 
		maxNumBeds = buildingConfig.getLivingAccommodationBeds(building.getBuildingType());
		// Loads the wash water usage kg/sol
		washWaterUsage = personConfig.getWaterUsageRate();
		// Loads the grey to black water ratio
		double grey2BlackWaterRatio = personConfig.getGrey2BlackWaterRatio();
		// Calculate the grey water fraction
		greyWaterFraction = grey2BlackWaterRatio / (grey2BlackWaterRatio + 1);
		// Load activity spots
		loadActivitySpots(buildingConfig.getLivingAccommodationsActivitySpots(building.getBuildingType()));
//		// Load bed locations
//		loadBedLocations(buildingConfig.getMedicalCareBedLocations(building.getBuildingType()));
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
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.LIVING_ACCOMMODATIONS).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				LivingAccommodations livingFunction = building.getLivingAccommodations();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += livingFunction.maxNumBeds * wearModifier;
			}
		}

		double bedCapacityValue = demand / (supply + 1D);

		// BuildingConfig config = simulationConfig.getBuildingConfiguration();
		double bedCapacity = buildingConfig.getLivingAccommodationBeds(buildingName);

		return bedCapacity * bedCapacityValue;
	}

	/**
	 * Gets the number of beds in the living accommodations.
	 * 
	 * @return number of beds.
	 */
	public int getBedCap() {
		return maxNumBeds;
	}

	/**
	 * Gets the number of assigned beds in this building.
	 * 
	 * @return number of assigned beds
	 */
	public int getNumAssignedBeds() {
		return assignedBeds.size();
	}
	
	/**
	 * Gets the number of people registered to sleep in this building.
	 * 
	 * @return number of registered sleepers
	 */
	public int getRegisteredSleepers() {
		return registeredSleepers;
	}

	/**
	 * Checks if all the beds have been taken/registered
	 * @return
	 */
	public boolean areAllBedsTaken() {
		if (registeredSleepers >= maxNumBeds)
			return true;
		
		return false;
	}
	
	/**
	 * Registers a sleeper with a bed.
	 * 
	 * @param person
	 * @param isAGuest is this person a guest (not inhabitant) of this settlement
	 * @return the bed registered with the given person 
	 */
	public Point2D registerSleeper(Person person, boolean isAGuest) {
		Point2D registeredBed = person.getBed();
		
		if (registeredBed == null) {
			
			if (areAllBedsTaken()) {		 
				 logger.log(building, Level.WARNING, 5000,  " All beds have been taken"
						 		+ " (# Registered Beds: " + registeredSleepers 
						 		+ ", Bed Capacity: " + maxNumBeds + ").");	
			}
			
			else if (!assignedBeds.containsKey(person)) {
				// TODO: need to rework for guest stay
				if (isAGuest) {
					// Note : do not designate a bed since he's only a guest
					Point2D bed = designateABed(person, isAGuest);
					if (bed != null) {
						logger.log(building, person, Level.WARNING, 2000, " was given a temporary bed", null); 
						return bed;
					} else {
						logger.log(building, person, Level.WARNING, 2000,
								   "Could not find a temporary bed.", null);
					}
		
	
				} else {
					// for a new inhabitant
					// if a person has never been assigned a bed
					// logger.info(person + " does not have a designated bed yet.");
					Point2D bed = designateABed(person, isAGuest);
					if (bed != null) {
						registeredSleepers++;
//						LogConsolidated.log(logger, Level.WARNING, 2000, sourceName,
//								"[" + building.getSettlement().getName() + "] " + person + " was assigned a bed in " 
//								+ person.getQuarters().getNickName(), null);
						return bed;
					} else {
						logger.log(building, person, Level.WARNING, 2000, "Did not have a bed assigned yet.", null);
					}
				}
			}
		}
		
		else {
			// Ensure the person's registered bed has been added to the assignedBeds map
			if (!assignedBeds.containsValue(registeredBed)) {
				assignedBeds.put(person, registeredBed);
			}
			return registeredBed;
		}
		
		return null;
	}

	/**
	 * Assigns a given bed to a given person
	 * 
	 * @param person
	 * @param bed
	 */
	public void assignABed(Person person, Point2D bed) {
		assignedBeds.put(person, bed);
		person.setBed(bed);
		person.setQuarters(building);
		
		logger.log(building, person, Level.INFO, 0, "Designated a bed at ("
					+ Math.round(bed.getX()*100.0)/100.0 + ", "
					+ Math.round(bed.getY()*100.0)/100.0 + ")", null);		
	}
	
	/**
	 * Assigns/designate an available bed to a person
	 * 
	 * @param person
	 * @return
	 */
	public Point2D designateABed(Person person, boolean guest) {
		Point2D bed = null;
		List<Point2D> spots = super.getActivitySpotsList();
		int numDesignated = getNumAssignedBeds();
		if (numDesignated < maxNumBeds) {
			// TODO: there should be at least one bed available-- Note: it may not be empty. a
			// traveler may be sleeping on it.
			for (Point2D spot : spots) {
				// Convert the activity spot (the bed location) to the settlement reference coordinate
				double x = spot.getX() + building.getXLocation();
				double y = spot.getY() + building.getYLocation();
				bed = new Point2D.Double(x, y);
				if (!assignedBeds.containsValue(bed)) {
					if (!guest) {
						assignABed(person, bed);

					}
					else { // is a guest
						logger.log(building, person, Level.INFO, 0, "Given a temporary bed at ("
								+ Math.round(bed.getX()*100.0)/100.0  + ", " 
								+ Math.round(bed.getY()*100.0)/100.0  + ").", null);
					}
					break;
				}
			}
		}

		return bed;
	}

	/**
	 * Removes a sleeper from a bed.
	 * 
	 * @throws BuildingException if no sleepers to remove.
	 */
	public void removeSleeper(Person person) {
		registeredSleepers--;
		if (registeredSleepers < 0) {
			registeredSleepers = 0;
			throw new IllegalStateException("Beds are empty.");
		} else {
			// bedMap.remove(bedMap.get(person));
		}
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
	 * Adds to the daily water usage
	 * 
	 * @param waterUssed
	 */
	public void addDailyWaterUsage(double waterUssed) {
		dailyWaterUsage.increaseDataPoint(waterUssed);
	}
	
	/**
	 * Gets the daily average water usage of the last 5 sols
	 * Not: most weight on yesterday's usage. Least weight on usage from 5 sols ago
	 * 
	 * @return
	 */
	public double getDailyAverageWaterUsage() {
		return dailyWaterUsage.getDailyAverage();
	}
	
	/**
	 * Utilizes water for bathing, washing, etc based on population.
	 * 
	 * @param time amount of time passing (millisols)
	 */
	private void generateWaste(double time) {
		double random_factor = 1 + RandomUtil.getRandomDouble(0.1);
		int numBed = getNumAssignedBeds();
		// int pop = settlement.getNumCurrentPopulation();
		// Total average wash water used at the settlement over this time period.
		// This includes showering, washing hands, washing dishes, etc.
		Settlement settlement = building.getSettlement();
		
		double ration = 1;
		// If settlement is rationing water, reduce water usage according to its level
		int level = settlement.getWaterRation();
		if (level != 0)
			ration = 1.0 / level;
		// Account for people who are out there in an excursion and NOT in the
		// settlement
		double absenteeFactor = (double)settlement.getIndoorPeopleCount() / settlement.getPopulationCapacity();
		
		double usage =  (washWaterUsage * time / 1_000D) * numBed * absenteeFactor;
		double waterUsed = usage * TOILET_CHANCE * random_factor * ration;
		double wasteWaterProduced = waterUsed * WASH_AND_WASTE_WATER_RATIO;

		// Remove wash water from settlement.
		if (waterUsed> MIN) {
			retrieve(waterUsed, waterID, true);
			// Track daily average
			addDailyWaterUsage(waterUsed);
		}

		// Grey water is produced by wash water.
		double greyWaterProduced = wasteWaterProduced * greyWaterFraction;
		// Black water is only produced by waste water.
		double blackWaterProduced = wasteWaterProduced * (1 - greyWaterFraction);
		String wasteName = "LivingAccomodation::generateWaste";
		if (greyWaterProduced > MIN)
			store(greyWaterProduced, greyWaterID, wasteName);
		if (blackWaterProduced > MIN)
			store(blackWaterProduced, blackWaterID, wasteName);

		// Use toilet paper and generate toxic waste (used toilet paper).
		double toiletPaperUsagePerMillisol = TOILET_WASTE_PERSON_SOL / 1000D;

		double toiletPaperUsageBuilding = toiletPaperUsagePerMillisol * time * numBed * random_factor;// toiletPaperUsageSettlement
																										// *																									// buildingProportionCap;
		if (toiletPaperUsageBuilding > MIN) {
			retrieve(toiletPaperUsageBuilding, ResourceUtil.toiletTissueID, true);
			store(toiletPaperUsageBuilding, ResourceUtil.toxicWasteID, wasteName);
		}
	}

	public Map<Person, Point2D> getAssignedBeds() {
		return assignedBeds;
	}

	/*
	 * Checks if an unmarked or undesignated bed is available
	 */
	public boolean hasAnUnmarkedBed() {
		if (getNumAssignedBeds() < maxNumBeds)
			return true;
		else
			return false;
	}

	public boolean isActivitySpotEmpty(Point2D spot) {
		return super.isActivitySpotEmpty(spot);
	}

	@Override
	public double getMaintenanceTime() {
		return maxNumBeds * 7D;
	}
	
	public void destroy() {
		building = null;
		assignedBeds.clear();
		assignedBeds = null;
		dailyWaterUsage = null;
	}
}

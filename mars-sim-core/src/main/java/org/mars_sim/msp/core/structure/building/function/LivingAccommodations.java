/**
 * Mars Simulation Project
 * LivingAccommodations.java
 * @version 3.1.1 2020-07-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.RandomUtil;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The LivingAccommodations class is a building function for a living
 * accommodations.
 */
public class LivingAccommodations extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
	private static Logger logger = Logger.getLogger(LivingAccommodations.class.getName());

	private static String sourceName = logger.getName();

	public static final int MAX_NUM_SOLS = 14;
	
	public static final double TOILET_WASTE_PERSON_SOL = .02D;
	public static final double WASH_AND_WASTE_WATER_RATIO = .85D;
	/** The minimal amount of resource to be retrieved. */
	private static final double MIN = 0.00001;
	/** 1/5 of chance of going to a restroom per frame */
	public static final int TOILET_CHANCE = 20;

	private static final FunctionType FUNCTION = FunctionType.LIVING_ACCOMMODATIONS;

	private int solCache = 0; // NOTE: can't be static since each building needs to account for it.
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

	private Building building;

	/** The bed registry in this facility. */
	private Map<Person, Point2D> assignedBeds = new HashMap<>();

	/** The daily water usage in this facility [kg/sol]. */
	private Map<Integer, Double> dailyWaterUsage;

//	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;
//	private static int co2ID = ResourceUtil.co2ID;
//	private static int foodID = ResourceUtil.foodID;
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
		super(FUNCTION, building);

		sourceName = sourceName.substring(sourceName.lastIndexOf(".") + 1, sourceName.length());

		this.building = building;
	
		dailyWaterUsage = new HashMap<>();	
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
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
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
		if (registeredSleepers == maxNumBeds)
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
				 LogConsolidated.log(logger, Level.WARNING, 5000, sourceName, 
						 "[" + building.getSettlement().getName() + "] All beds have been taken"
						 		+ " (# Registered Beds : " + registeredSleepers 
						 + "  Bed Capacity : " + maxNumBeds + ").", null);	
			}
			
			else if (!assignedBeds.containsKey(person)) {
				// TODO: need to rework for guest stay
				if (isAGuest) {
					// Note : do not designate a bed since he's only a guest
					Point2D bed = designateABed(person, isAGuest);
					if (bed != null) {
						LogConsolidated.log(logger, Level.WARNING, 2000, sourceName,
								"[" + building.getSettlement().getName() + "] " + person + " was given a temporary bed in " 
								+ person.getQuarters().getNickName(), null);
						return bed;
					} else {
						LogConsolidated.log(logger, Level.WARNING, 2000, sourceName,
								"[" + building.getSettlement().getName() + "] " + person + " could even find a temporary bed.", null);
					}
		
	
				} else {
					// for a new inhabitant
					// if a person has never been assigned a bed
					// logger.info(person + " does not have a designated bed yet.");
					Point2D bed = designateABed(person, isAGuest);
					if (bed != null) {
						registeredSleepers++;
						LogConsolidated.log(logger, Level.WARNING, 2000, sourceName,
								"[" + building.getSettlement().getName() + "] " + person + " was assigned in " 
								+ person.getQuarters().getNickName(), null);
						return bed;
					} else {
						LogConsolidated.log(logger, Level.WARNING, 2000, sourceName,
								"[" + building.getSettlement().getName() + "] " + person + " did not have a bed assigned yet.", null);
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
		
		LogConsolidated.log(logger, Level.INFO, 0, sourceName, "[" + person.getSettlement() + "] "
				+ person + " was designated a bed at (" + Math.round(bed.getX()*100.0)/100.0 + ", " +
				Math.round(bed.getY()*100.0)/100.0 + ") in " + person.getQuarters(), null);
		
//		String s = String.format("[%s] %25s (Bed) -> %25s (%...",
//				person.getLocationTag().getLocale(), 
//				person.getName(), 
//				person.getQuarters()
//				);
//		
//		LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, s);		
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
//				if (!assignedBeds.containsValue(bed)) {
					if (!guest) {
						assignABed(person, bed);

					}
					else { // is a guest
						LogConsolidated.log(logger, Level.INFO, 0, sourceName, "[" + person.getSettlement() + "] "
								+ person + " was given a temporary bed at (" + Math.round(bed.getX()*100.0)/100.0  + ", " +
								Math.round(bed.getY()*100.0)/100.0  + ") in " + person.getQuarters(), null);
					}
					break;
//				}
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
	public void timePassing(double time) {
		
		int solElapsed = marsClock.getMissionSol();
		
		int rand = RandomUtil.getRandomInt(TOILET_CHANCE);
		if (rand == 0) {
			generateWaste(time);
		}

		if (solCache != solElapsed) {
			solCache = solElapsed;
			
			// Limit the size of the dailyWaterUsage to 20 key value pairs
			if (dailyWaterUsage.size() > MAX_NUM_SOLS)
				dailyWaterUsage.remove(solElapsed - MAX_NUM_SOLS);
		}
	}

	/**
	 * Adds to the daily water usage
	 * 
	 * @param waterUssed
	 * @param solElapsed
	 */
	public void addDailyWaterUsage(double waterUssed) {
		if (dailyWaterUsage.containsKey(solCache)) {
			dailyWaterUsage.put(solCache, waterUssed + dailyWaterUsage.get(solCache));
		}
		else {
			dailyWaterUsage.put(solCache, waterUssed);
		}
	}
	
	/**
	 * Gets the daily average water usage of the last 5 sols
	 * Not: most weight on yesterday's usage. Least weight on usage from 5 sols ago
	 * 
	 * @return
	 */
	public double getDailyAverageWaterUsage() {
		boolean quit = false;
		int today = solCache;
		int sol = solCache;
		double sum = 0;
		double numSols = 0;
		double cumulativeWeight = 0.75;
		double weight = 1;

		while (!quit) {
			if (dailyWaterUsage.size() == 0) {
				quit = true;
				return 0;
			}
			
			else if (dailyWaterUsage.containsKey(sol)) {
				if (today == sol) {
					// If it's getting the today's average, one may 
					// project the full-day usage based on the usage up to this moment 
					weight = .25;
					sum = sum + dailyWaterUsage.get(sol) * 1_000D / marsClock.getMillisol() * weight ;
				}
				
				else {
					sum = sum + dailyWaterUsage.get(sol) * weight;
				}
			}
			
			else if (dailyWaterUsage.containsKey(sol - 1)) {
				sum = sum + dailyWaterUsage.get(sol - 1) * weight;
				sol--;
			}
			
			cumulativeWeight = cumulativeWeight + weight;
			weight = (numSols + 1) / (cumulativeWeight + 1);
			numSols++;
			sol--;
			// Get the last x sols only
			if (numSols > MAX_NUM_SOLS)
				quit = true;
		}
		
		return sum/cumulativeWeight; 
	}
	
	/**
	 * Utilizes water for bathing, washing, etc based on population.
	 * 
	 * @param time amount of time passing (millisols)
	 */
	public void generateWaste(double time) {
		double random_factor = 1 + RandomUtil.getRandomDouble(0.1) - RandomUtil.getRandomDouble(0.1);
		int numBed = getNumAssignedBeds();
		// int pop = settlement.getNumCurrentPopulation();
		// Total average wash water used at the settlement over this time period.
		// This includes showering, washing hands, washing dishes, etc.
		Settlement settlement = building.getSettlement();
		
		double ration = 1;
		// If settlement is rationing water, reduce water usage according to its level
		int level = settlement.getWaterRation();
//		System.out.print("level : " + level);
		if (level != 0)
			ration = 1 / level;
		// Account for people who are out there in an excursion and NOT in the
		// settlement
		double absentee_factor = settlement.getIndoorPeopleCount() / settlement.getPopulationCapacity();
		
		double usage =  (washWaterUsage * time / 1_000D) * numBed * absentee_factor;
//		System.out.print("   usage : " + usage);
		double waterUsed = usage * TOILET_CHANCE * random_factor * ration;
//		System.out.println("   waterUsed : " + waterUsed);
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

		if (greyWaterProduced > MIN)
			store(greyWaterProduced, greyWaterID, sourceName + "::generateWaste");
		if (blackWaterProduced > MIN)
			store(blackWaterProduced, blackWaterID, sourceName + "::generateWaste");

		// Use toilet paper and generate toxic waste (used toilet paper).
		double toiletPaperUsagePerMillisol = TOILET_WASTE_PERSON_SOL / 1000D;

		double toiletPaperUsageBuilding = toiletPaperUsagePerMillisol * time * numBed * random_factor;// toiletPaperUsageSettlement
																										// *																									// buildingProportionCap;
		if (toiletPaperUsageBuilding > MIN)
			retrieve(toiletPaperUsageBuilding, ResourceUtil.toiletTissueID, true);

		if (toiletPaperUsageBuilding > MIN)
			store(toiletPaperUsageBuilding, ResourceUtil.toxicWasteID, sourceName + "::generateWaste");
	}

	public Building getBuilding() {
		return building;
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

	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return 0D;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * 
	 * @return power (kW)
	 */
	public double getPoweredDownPowerRequired() {
		return 0D;
	}

	@Override
	public double getMaintenanceTime() {
		return maxNumBeds * 7D;
	}

	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean retrieve(double amount, int resource, boolean value) {
		return Storage.retrieveAnResource(amount, resource, building.getInventory(), value);
	}
	
	public void store(double amount, int resource, String source) {
		Storage.storeAnResource(amount, resource, building.getInventory(), source);
	}
	
	
	public void destroy() {
		building = null;
		assignedBeds = null;
	}
}

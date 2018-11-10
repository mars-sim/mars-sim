/**
 * Mars Simulation Project
 * LivingAccommodations.java
 * @version 3.1.0 2017-10-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.time.MarsClock;
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

	public final static double TOILET_WASTE_PERSON_SOL = .02D;
	public final static double WASH_AND_WASTE_WATER_RATIO = .85D;

	/** 1/5 of chance of going to a restroom per frame */
	public final static int TOILET_CHANCE = 5;

	private static final FunctionType FUNCTION = FunctionType.LIVING_ACCOMODATIONS;

	private int solCache = 0; // NOTE: can't be static since each building needs to account for it.
	private int maxBeds; // max # of beds
	private int sleepers;

	private double washWaterUsage; // Water used per person for washing (showers, washing clothes, hands, dishes,
									// etc) per millisol (avg over Sol).
	// private double wasteWaterProduced; // Waste water produced by
	// urination/defecation per person per millisol (avg over Sol).
	private double greyWaterFraction; // percent portion of grey water generated from waste water.

	private Settlement settlement;
	private Inventory inv;
	private Building building;

	private Map<Person, Point2D> assignedBeds = new HashMap<>();

	private static SimulationConfig simulationConfig = SimulationConfig.instance();
	private static BuildingConfig buildingConfig = simulationConfig.getBuildingConfiguration();
	private static MarsClock marsClock;
	
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

		settlement = building.getBuildingManager().getSettlement();
		inv = settlement.getInventory();
		// inv = building.getBuildingManager().getSettlement().getInventory();
		// inv = building.getSettlementInventory();

		marsClock = Simulation.instance().getMasterClock().getMarsClock();

		BuildingConfig buildingConfig = simulationConfig.getBuildingConfiguration(); // need this to pass maven test
		maxBeds = buildingConfig.getLivingAccommodationBeds(building.getBuildingType());

		PersonConfig personconfig = simulationConfig.getPersonConfiguration();
		washWaterUsage = personconfig.getWaterUsageRate() / 1000D;
		// wasteWaterProduced = (personconfig.getWaterConsumptionRate() +
		// personconfig.getFoodConsumptionRate()) / 1000D;
		double grey2BlackWaterRatio = personconfig.getGrey2BlackWaterRatio();
		greyWaterFraction = grey2BlackWaterRatio / (grey2BlackWaterRatio + 1);

		// Load activity spots
		loadActivitySpots(buildingConfig.getLivingAccommodationsActivitySpots(building.getBuildingType()));

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
				LivingAccommodations livingFunction = (LivingAccommodations) building.getFunction(FUNCTION);
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += livingFunction.maxBeds * wearModifier;
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
	public int getBeds() {
		return maxBeds;
	}

	/**
	 * Gets the number of people sleeping in the beds.
	 * 
	 * @return number of people
	 */
	public int getSleepers() {
		return sleepers;
	}

	/**
	 * Registers a sleeper with a bed.
	 * 
	 * @param person
	 * @param isAGuest is this person a guest (not inhabitant) of this settlement
	 */
	public void registerSleeper(Person person, boolean isAGuest) {
		if (sleepers > maxBeds) {
			// LogConsolidated.log(logger, Level.WARNING, 2000, sourceName, person
			// + " is going to his/her quarter in " + building.getNickName() + " in " +
			// settlement, null);
			// logger.info("[" + settlement.getName() + "] # sleepers : " + sleepers + " #
			// beds : " + maxBeds);
		} else if (!assignedBeds.containsKey(person)) {
			if (isAGuest) {
				sleepers++;
				// do not designate a bed since he's only a guest
				// Case 1 & 2
				// if (sleepers > beds) {
				// sleepers--;
				// logger.info("Living Accommodation : " + person + " could not find any
				// unoccupied beds. # sleepers : "
				// + sleepers + " # beds : " + beds + ". Will sleep at a random location.");
				// }
			} else {
				// for a new inhabitant
				// if a person has never been assigned a bed
				// logger.info(person + " does not have a designated bed yet.");
				Point2D bed = designateABed(person);
				if (bed != null) {
					sleepers++;
				} else {
					LogConsolidated.log(logger, Level.WARNING, 2000, sourceName,
							"[" + settlement.getName() + "] " + person + " does not have a bed yet.", null);
				}
			}
		} else // as an old inhabitant
			sleepers++;
	}

	/**
	 * Assigns/designate an available bed to a person
	 * 
	 * @param person
	 * @return
	 */
	public Point2D designateABed(Person person) {
		Point2D bed = null;
		List<Point2D> spots = super.getActivitySpotsList();
		// int numBeds = spots.size();
		int numDesignated = assignedBeds.size();
		if (numDesignated < maxBeds) {// numBeds) {
			// there should be at least one bed available-- Note: it may not be empty. a
			// traveler may be sleeping on it.
			for (Point2D spot : spots) {
				if (!assignedBeds.containsValue(spot)) {
					bed = spot;
					assignedBeds.put(person, bed);
					person.setBed(bed);
					person.setQuarters(building);
					// logger.info(person + " has been designated a bed at (" + bed.getX() + ", " +
					// bed.getY() + ") in " + person.getQuarters());
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
		sleepers--;
		if (sleepers < 0) {
			sleepers = 0;
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
		if (solCache != solElapsed) {
			solCache = solElapsed;
			// Designate a bed for each inhabitant
			if (settlement == null)
				settlement = building.getBuildingManager().getSettlement();
			for (Person p : settlement.getIndoorPeople()) {
				if (p.getBed() == null) {
					registerSleeper(p, false);
				}
			}
		}

		// inv = building.getSettlementInventory();
		// inv = building.getBuildingManager().getSettlement().getInventory();
		int rand = RandomUtil.getRandomInt(TOILET_CHANCE);
		if (rand == 0) {
			// Inventory inv = settlement.getInventory();
			generateWaste(time);
		}
	}

	/**
	 * Utilizes water for bathing, washing, etc based on population.
	 * 
	 * @param time amount of time passing (millisols)
	 */
	public void generateWaste(double time) {
		double random_factor = 1 + RandomUtil.getRandomDouble(0.25) - RandomUtil.getRandomDouble(0.25);
		int numBed = assignedBeds.size();
		// int pop = settlement.getNumCurrentPopulation();
		// Total average wash water used at the settlement over this time period.
		// This includes showering, washing hands, washing dishes, etc.
		double usage = TOILET_CHANCE * washWaterUsage * time * numBed;
		// If settlement is rationing water, reduce water usage according to its level
		int level = settlement.computeWaterRation();
		if (level != 0)
			usage = usage / 1.5D / level / 2D;
		// Account for people who are out there in an excursion and NOT in the
		// settlement
		double absentee_factor = settlement.getIndoorPeopleCount() / settlement.getPopulationCapacity();

		double waterUsed = usage * time * numBed * absentee_factor;
		// double waterProduced = wasteWaterProduced * time * numBed * absentee_factor;
		double wasteWaterProduced = usage * WASH_AND_WASTE_WATER_RATIO;

		// Remove wash water from settlement.
		if (inv == null)
			inv = settlement.getInventory();
		Storage.retrieveAnResource(waterUsed * random_factor, waterID, inv, true);

		// Grey water is produced by wash water.
		double greyWaterProduced = wasteWaterProduced * greyWaterFraction;
		// Black water is only produced by waste water.
		double blackWaterProduced = wasteWaterProduced * (1 - greyWaterFraction);

		if (greyWaterProduced > 0)
			Storage.storeAnResource(greyWaterProduced, greyWaterID, inv, sourceName + "::generateWaste");
		if (blackWaterProduced > 0)
			Storage.storeAnResource(blackWaterProduced, blackWaterID, inv, sourceName + "::generateWaste");

		// Use toilet paper and generate toxic waste (used toilet paper).
		double toiletPaperUsagePerMillisol = TOILET_WASTE_PERSON_SOL / 1000D;

		double toiletPaperUsageBuilding = toiletPaperUsagePerMillisol * time * numBed * random_factor;// toiletPaperUsageSettlement
																										// *
																										// buildingProportionCap;

		Storage.retrieveAnResource(toiletPaperUsageBuilding, ResourceUtil.toiletTissueAR, inv, true);

		if (toiletPaperUsageBuilding > 0)
			Storage.storeAnResource(toiletPaperUsageBuilding, ResourceUtil.toxicWasteAR, inv,
					sourceName + "::generateWaste");
	}

	public Building getBuilding() {
		return building;
	}

	public Map<Person, Point2D> getBedMap() {
		return assignedBeds;
	}

	/*
	 * Checks if an undesignated bed is available
	 */
	public boolean hasAnUnmarkedBed() {
		// List<Point2D> activitySpots = super.getActivitySpotsList();//(List<Point2D>)
		// super.getAvailableActivitySpot(person);
		// int numBeds = activitySpots.size();

		int numDesignated = assignedBeds.size();
		// logger.info("# designated beds : " + numDesignated + " # beds : " + numBeds);

		if (numDesignated < maxBeds)
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
		return maxBeds * 7D;
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

	public void destroy() {
		settlement = null;
		inv = null;
		building = null;
		assignedBeds = null;
		simulationConfig = null;
		buildingConfig = null;
		marsClock = null;
	}
}
/**
 * Mars Simulation Project
 * LivingAccommodations.java
 * @version 3.1.0 2018-08-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The LivingAccommodations class is a building function for a living accommodations.
 */
public class LivingAccommodations extends Function implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    /* default logger.*/
 	private static Logger logger = Logger.getLogger(LivingAccommodations.class.getName());

    private static String sourceName = logger.getName();
    
    public final static double TOILET_WASTE_PERSON_SOL = .02D;
    public final static double WASH_AND_WASTE_WATER_RATIO = .85D;

    private static final FunctionType FUNCTION = FunctionType.LIVING_ACCOMODATIONS;

    private int beds; // max # of beds
    private int sleepers;
	//private int solCache = 1;

    private double washWaterUsage; // Water used per person for washing (showers, washing clothes, hands, dishes, etc) per millisol (avg over Sol).
    //private double wasteWaterProduced; // Waste water produced by urination/defecation per person per millisol (avg over Sol).
    private double greyWaterFraction; // percent portion of grey water generated from waste water.

    //private boolean hasAnUndesignatedBed = true;

    private Settlement settlement;
    private Inventory inv;

    private Building building;
    private Map<Person, Point2D> bedMap = new HashMap<>();

    private static SimulationConfig simulationConfig = SimulationConfig.instance();
    private static BuildingConfig buildingConfig = simulationConfig.getBuildingConfiguration();

    /**
     * Constructor
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    public LivingAccommodations(Building building) {
        // Call Function constructor.
        super(FUNCTION, building);

        sourceName = sourceName.substring(sourceName.lastIndexOf(".") + 1, sourceName.length());
        
        this.building = building;
        this.settlement = building.getBuildingManager().getSettlement();
        this.inv = settlement.getInventory();
        //inv = building.getBuildingManager().getSettlement().getInventory();
        //inv = building.getSettlementInventory();
        

        BuildingConfig buildingConfig = simulationConfig.getBuildingConfiguration(); // need this to pass maven test
        beds = buildingConfig.getLivingAccommodationBeds(building.getBuildingType());

        PersonConfig personconfig = simulationConfig.getPersonConfiguration();
        washWaterUsage = personconfig.getWaterUsageRate() / 1000D;
        //wasteWaterProduced = (personconfig.getWaterConsumptionRate() + personconfig.getFoodConsumptionRate()) / 1000D;
        double grey2BlackWaterRatio = personconfig.getGrey2BlackWaterRatio();
        greyWaterFraction = grey2BlackWaterRatio / (grey2BlackWaterRatio + 1);

        // Load activity spots
        loadActivitySpots(buildingConfig.getLivingAccommodationsActivitySpots(building.getBuildingType()));

    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    public static double getFunctionValue(String buildingName,
            boolean newBuilding, Settlement settlement) {

        // Demand is two beds for every inhabitant (with population expansion in mind).
        double demand = settlement.getAllAssociatedPeople().size() * 2D;

        double supply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding
                    && building.getBuildingType().equalsIgnoreCase(buildingName)
                    && !removedBuilding) {
                removedBuilding = true;
            } else {
                LivingAccommodations livingFunction = (LivingAccommodations) building.getFunction(FUNCTION);
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
                supply += livingFunction.beds * wearModifier;
            }
        }

        double bedCapacityValue = demand / (supply + 1D);

        //BuildingConfig config = simulationConfig.getBuildingConfiguration();
        double bedCapacity = buildingConfig.getLivingAccommodationBeds(buildingName);

        return bedCapacity * bedCapacityValue;
    }

    /**
     * Gets the number of beds in the living accommodations.
     * @return number of beds.
     */
    public int getBeds() {
        return beds;
    }

    /**
     * Gets the number of people sleeping in the beds.
     * @return number of people
     */
    public int getSleepers() {
        return sleepers;
    }

    /**
     * Adds a sleeper to a bed.
     * @throws BuildingException if beds are already in use.
     */
    public void addSleeper(Person person, boolean isAGuest) {
    	sleepers++;
    	if (isAGuest) {
    		// Case 1 & 2
    		//if (sleepers > beds) {
            //    sleepers--;
            //    logger.info("Living Accommodation : " + person + " could not find any unoccupied beds. # sleepers : "
            //    		+ sleepers + "  # beds : " + beds + ". Will sleep at a random location.");
            //}
    	}
    	else {
    		// for inhabitants
        	if (sleepers > beds) {
                //sleepers = beds;
                sleepers--;
                logger.info("# sleepers : " + sleepers + "  # beds : " + beds);
            }
            else {
	        	if (!bedMap.containsKey(person)) {
	        		// if a person has never been assigned a bed
	        		//logger.info(person + " does not have a designated bed yet.");
	        		Point2D bed = designateABed(person);
	        		if (bed == null) {
	                    sleepers--;
	                    LogConsolidated.log(logger, Level.WARNING, 2000, sourceName, person + " could not find any unmarked beds in "
	        					+ building.getNickName() + " in " + settlement, null);
	        		}
	        	}
            }
    	}
    }

    /**
     * Assigns/designate an available bed to a person
     * @param person
     * @return
     */
    // 2016-01-09 Added designateABed()
    public Point2D designateABed(Person person) {
    	Point2D bed = null;
    	List<Point2D> spots = super.getActivitySpotsList();
    	int numBeds = spots.size();
    	int numDesignated = bedMap.size();
    	if (numDesignated < numBeds) {
    		// there should be at least one bed available-- Note: it may not be empty. a traveler may be sleeping on it.
	        Iterator<Point2D> i = spots.iterator();
	        while (i.hasNext()) {
	            Point2D spot = i.next();
	            if (!bedMap.containsValue(spot)) {
	            	bed = spot;
	            	bedMap.put(person, bed);
	        		person.setBed(bed);
	        		person.setQuarters(building);
	            	//logger.info(person + " has been designated a bed at (" + bed.getX() + ", " + bed.getY() 	+ ") in " + person.getQuarters());
	            	break;
	            }
	        }
    	}

    	return bed;
    }
    /**
     * Removes a sleeper from a bed.
     * @throws BuildingException if no sleepers to remove.
     */
    public void removeSleeper(Person person) {
    	sleepers--;
        if (sleepers < 0) {
            sleepers = 0;
            throw new IllegalStateException("Beds are empty.");
        }
        else {
        	//bedMap.remove(bedMap.get(person));
        }
    }


    /**
     * Time passing for the building.
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     */
    public void timePassing(double time) {
    	//inv = building.getSettlementInventory();
    	//inv = building.getBuildingManager().getSettlement().getInventory();
    	int rand = RandomUtil.getRandomInt(10);
    	if (rand == 0) {
    		Inventory inv = settlement.getInventory();
    		generateWaste(time);
    	}
    }

    /**
     * Utilizes water for bathing, washing, etc based on population.
     * @param time amount of time passing (millisols)
     */
    public void generateWaste(double time) {
    	double random_factor = 1 + RandomUtil.getRandomDouble(0.25) - RandomUtil.getRandomDouble(0.25);
    	int numBed = bedMap.size();
    	// Total average wash water used at the settlement over this time period.
    	// This includes showering, washing hands, washing dishes, etc.
        double usage = washWaterUsage  * time * numBed;
        // 2017-05-02 If settlement is rationing water, reduce water usage according to its level
        int level = settlement.waterRationLevel();
        if (level != 0)
            usage = usage / 1.5D / level;
        // 2017-05-02 Account for people who are out there in an excursion and NOT in the settlement
        double absentee_factor = settlement.getNumCurrentPopulation()/settlement.getPopulationCapacity();

        double waterUsed = usage  * time * numBed * absentee_factor;
        //double waterProduced = wasteWaterProduced * time * numBed * absentee_factor;
        double wasteWaterProduced = usage * WASH_AND_WASTE_WATER_RATIO;
        // Remove wash water from settlement.
        Storage.retrieveAnResource(waterUsed * random_factor, ResourceUtil.waterAR, inv, true);

        // Grey water is produced by wash water.
        double greyWaterProduced = wasteWaterProduced * greyWaterFraction;
        // Black water is only produced by waste water.
        double blackWaterProduced = wasteWaterProduced * (1 - greyWaterFraction);

        if (greyWaterProduced > 0)
        	Storage.storeAnResource(greyWaterProduced, ResourceUtil.greyWaterAR, inv, sourceName + "::generateWaste");

        if (blackWaterProduced > 0)
        	Storage.storeAnResource(blackWaterProduced, ResourceUtil.blackWaterAR, inv, sourceName + "::generateWaste");

    	// Use toilet paper and generate toxic waste (used toilet paper).
        double toiletPaperUsagePerMillisol = TOILET_WASTE_PERSON_SOL / 1000D;
        
        double toiletPaperUsageBuilding = toiletPaperUsagePerMillisol * time * numBed * random_factor;//toiletPaperUsageSettlement * buildingProportionCap;

        Storage.retrieveAnResource(toiletPaperUsageBuilding , ResourceUtil.toiletTissueAR, inv, true);
        
        if (toiletPaperUsageBuilding > 0)
        	Storage.storeAnResource(toiletPaperUsageBuilding, ResourceUtil.toxicWasteAR, inv, sourceName + "::generateWaste");
    }

    public Building getBuilding() {
    	return building;
    }

    public Map<Person, Point2D> getBedMap() {
    	return bedMap;
    }

    /*
     * Checks if an undesignated bed is available
     */
    // 2016-01-10 Added hasAnUndesignatedBed
    public boolean hasAnUnmarkedBed() {
    	//List<Point2D> activitySpots = super.getActivitySpotsList();//(List<Point2D>) super.getAvailableActivitySpot(person);
    	//int numBeds = activitySpots.size();

    	int numDesignated = bedMap.size();
		//logger.info("# designated beds : " +  numDesignated + " # beds : " +  numBeds);

    	if (numDesignated < beds)
    		return true;
    	else
    		return false;
    }

    public boolean isActivitySpotEmpty(Point2D spot) {
    	return super.isActivitySpotEmpty(spot);
    }


    /**
     * Gets the amount of power required when function is at full power.
     * @return power (kW)
     */
    public double getFullPowerRequired() {
        return 0D;
    }

    /**
     * Gets the amount of power required when function is at power down level.
     * @return power (kW)
     */
    public double getPoweredDownPowerRequired() {
        return 0D;
    }

    @Override
    public double getMaintenanceTime() {
        return beds * 7D;
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
}
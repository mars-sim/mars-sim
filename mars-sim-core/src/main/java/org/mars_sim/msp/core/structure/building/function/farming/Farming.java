/**
* Mars Simulation Project
 * Farming.java
 * @version 3.1.0 2016-10-11
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.TendGreenhouse;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.Conversion;

/**
 * The Farming class is a building function for greenhouse farming.
 */
// 2014-10-15 Fixed the crash by checking if there is any food available
// 	Added new method checkAmountOfFood() for CookMeal.java
// 2014-10-14 Implemented new way of calculating amount of crop harvest in kg,
// Crop Yield or Edible Biomass, based on NASA Advanced Life Support Baseline Values and Assumptions CR-2004-208941
// 2014-11-06 Added if clause to account for soybean harvest
// 2014-11-29 Added harvesting crops to turn into corresponding amount resource having the same name as the crop's name
// 2014-12-09 Added crop queue
// 2015-02-16 Added Germination phase and custom growing area
// 2015-02-28 Added soil usage (and changed fertilizer usage) based on sq meter
// 2015-09-30 Changed the algorithm of selecting a new crop to plant
// 2016-06-28 Added incubation phase and required tissue culture for new crops
public class Farming
extends Function
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    /** default logger. */
	private static Logger logger = Logger.getLogger(Farming.class.getName());

    private static final BuildingFunction FUNCTION = BuildingFunction.FARMING;
 
    private static final double CROP_WASTE_PER_SQM_PER_SOL = .01D; // .01 kg

	public static final String LED_KIT = "light emitting diode kit";
	public static final String HPS_LAMP = "high pressure sodium lamp";

	/** amount of crop tissue culture needed for each square meter of growing area */
    public static final double TISSUE_PER_SQM = .0005D; // 1/2 gram (arbitrary)
    //private static final double TISSUE_CULTURE_FACTOR = 100D;

	private static ItemResource LED_Item;
	private static ItemResource HPS_Item;

	private int numLEDInUse;
	private int cacheNumLED;
	private boolean checkLED;
	private int numHPSinNeed;

    private int cropNum;
	private int solCache = 1;

    private double powerGrowingCrop;
    private double powerSustainingCrop;
    private double maxGrowingArea;
    private double remainingGrowingArea;
    private double totalMaxHarvest = 0;
    
    private String cropInQueue;

    private List<Crop> crops = new ArrayList<Crop>();
  
  	// 2014-12-09 Added cropInQueue, cropListInQueue
    private List<CropType> cropListInQueue = new ArrayList<CropType>();
    private List<CropType> cropTypeList = new ArrayList<CropType>();
    private List<CropType> historyList = new ArrayList<CropType>();

    //private Map<Crop, Double> cropAreaMap = new HashMap<Crop, Double>();

    private Inventory b_inv, s_inv;

    private Settlement settlement;
    private Building building;
    //private BeeGrowing beeGrowing;
	//private GoodsManager goodsManager;

    private MarsClock marsClock;// = Simulation.instance().getMasterClock().getMarsClock();
    /**
     * Constructor.
     * @param building the building the function is for.
     * @throws BuildingException if error in constructing function.
     */
    public Farming(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);

		LED_Item = ItemResource.findItemResource(LED_KIT);
		HPS_Item = ItemResource.findItemResource(HPS_LAMP);

        this.building = building;
        this.settlement = building.getBuildingManager().getSettlement();
		this.b_inv = building.getBuildingInventory();
		this.s_inv = settlement.getInventory();
		//this.goodsManager = settlement.getGoodsManager();

        marsClock = Simulation.instance().getMasterClock().getMarsClock();
        BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
        powerGrowingCrop = buildingConfig.getPowerForGrowingCrop(building.getBuildingType());
        powerSustainingCrop = buildingConfig.getPowerForSustainingCrop(building.getBuildingType());
        maxGrowingArea = buildingConfig.getCropGrowingArea(building.getBuildingType());
        remainingGrowingArea = maxGrowingArea;

    	CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
		cropTypeList = cropConfig.getCropList();
        cropNum = buildingConfig.getCropNum(building.getBuildingType());
       
        // Load activity spots
        loadActivitySpots(buildingConfig.getFarmingActivitySpots(building.getBuildingType()));

        for (int x = 0; x < cropNum; x++) {
         	// 2014-12-09 Added cropInQueue and changed method name to getNewCrop()
        	CropType cropType = getNewCrop("0", false);
        	Crop crop = plantACrop(cropType, false, 0);
            crops.add(crop);
            building.getBuildingManager().getSettlement().fireUnitUpdate(UnitEventType.CROP_EVENT, crop);
        }

        // 2015-02-18 Created BeeGrowing
        // TODO: write codes to incorporate the idea of bee growing
        // beeGrowing = new BeeGrowing(this);
        

    }


	/**
	 * Gets a random crop type.
	 * @return crop type
	 * @throws Exception if crops could not be found.
	 */
   	// 2014-12-09 Added new param cropInQueue and changed method name to getNewCrop()
    // 2015-03-02 Added highest VP crop selection
	public CropType getNewCrop(String cropInQueue, boolean isInitialCrop) {
		CropType ct = null;
		List<CropType> cropTypes = cropTypeList;
		if (cropInQueue.equals("0")) {
			if (!isInitialCrop) {
				int r = RandomUtil.getRandomInt(cropTypes.size() - 1);
				ct = cropTypes.get(r);
			}
			else
				ct = selectNewCrop();
		} else {
			// select the CropType currently in the user queue
			Iterator<CropType> i = cropTypes.iterator();
			while (i.hasNext()) {
				CropType c = i.next();
				if (c.getName() == cropInQueue)
					ct = c;
			}
		}
		historyList.add(ct);
		return ct;
	}

    // 2015-03-02 Added selecthighestCropCropType()
	// 2015-09-30 Revised the decision branch on how the crop type is chosen
	public CropType selectNewCrop() {
		CropType highestCrop = null;
		CropType secondCrop = null;
		CropType chosen = null;
		double highestVP = 0;
		double secondVP = 0;
		List<CropType> cropCache = new ArrayList<CropType>(cropTypeList);
		Iterator<CropType> i = cropCache.iterator();
		while (i.hasNext()) {
			CropType c = i.next();
			String cropName = c.getName();
			AmountResource ar = AmountResource.findAmountResource(cropName);
			double cropVP = getCropValue(ar);
			if (cropVP >= highestVP) {
				highestVP = cropVP;
				highestCrop = c;
			} else {
				if (cropVP > secondVP) {
					secondVP = cropVP;
					secondCrop = c;
				}
			}
		}
		int size = historyList.size();
		if (size == 3) {
			// remove the oldest crop type (the 1st element)
			List<CropType> list = (List<CropType>) historyList.subList(0, 1);
			historyList = list;
			// delete all elements except the last two
			//List<CropType> temp = new ArrayList<CropType>();
			//Iterator<CropType> ii = historyList.iterator();
			//while (ii.hasNext()) {
			//	   CropType ct = ii.next();
			//	   //some condition
			//	    ii.remove();
			//	}
		}

		CropType lastCT = null;
		CropType last2CT = null;
		boolean compareVP = false;

		if (size == 2) {
			last2CT = historyList.get(size-2);
			lastCT = historyList.get(size-1);

			if (highestCrop.equals(last2CT) || highestCrop.equals(lastCT)) {
				// if highestCrop has already been selected once

				if (last2CT.equals(lastCT)) {
					// since the highestCrop has already been chosen twice,
					// should not choose the same crop type again
					//compareVP = false;
					chosen = secondCrop;
				}

				else
					compareVP = true;
			}

			else if (secondCrop.equals(last2CT) || secondCrop.equals(lastCT)) {
				// if secondCrop has already been selected once

				if (last2CT.equals(lastCT)) {
					// since the secondCrop has already been chosen twice,
					// should not choose the same crop type again
					//compareVP = false;
					chosen = highestCrop;
				}

				else
					compareVP = true;
			}

		}

		else if (size == 1) {
			lastCT = historyList.get(size-1);

			if (lastCT != null) {
				// if highestCrop has already been selected for planting last time,
				if (highestCrop.equals(lastCT))
					compareVP = true;
			}
		}

		if (compareVP){
			// compare secondVP with highestVP
			// if their VP are within 15%, toss a dice
			// if they are further apart, should pick highestCrop
			//if ((highestVP - secondVP) < .15 * secondVP)
			if ((secondVP/highestVP) > .85) {
				int rand = RandomUtil.getRandomInt(0, 1);
				if (rand == 0)
					chosen = highestCrop;
				else
					chosen = secondCrop;
			}
			else
				chosen = secondCrop;
		}
		else
			chosen = highestCrop;

		return chosen;
	}

	// 2015-03-02 Added	getCropValue()
    public double getCropValue(AmountResource resource) {
    	double cropValue = settlement.getGoodsManager().getGoodValuePerItem(GoodsUtil.getResourceGood(resource));
    	return cropValue;
    }

    // 2015-02-15 added plantACrop()
    public Crop plantACrop(CropType cropType, boolean isNewCrop, double designatedGrowingArea) {
    	Crop crop = null;
    	// 2014-10-14 Implemented new way of calculating amount of food in kg, accounting for the Edible Biomass of a crop
    	// edibleBiomass is in  [ gram / m^2 / day ]
    	double edibleBiomass = cropType.getEdibleBiomass();
    	// growing-time is in millisol vs. growingDay is in sol
    	//double growingDay = cropType.getGrowingTime() / 1000D ;
    	double cropArea = 0;
       	if (remainingGrowingArea <= 1D ) {
    		cropArea = remainingGrowingArea;
    	}
       	else if (designatedGrowingArea != 0) {
    		cropArea = designatedGrowingArea;
    	}
    	else { //if (remainingGrowingArea > 1D)
    		cropArea = maxGrowingArea / (double) cropNum ;
    	}

		remainingGrowingArea = remainingGrowingArea - cropArea;
		//System.out.println("cropArea : "+ cropArea);

		if (remainingGrowingArea < 0) remainingGrowingArea = 0;
		//System.out.println("remainingGrowingArea : "+ remainingGrowingArea);

		// 1kg = 1000 g
		double dailyMaxHarvest = edibleBiomass/1000D * cropArea;
		totalMaxHarvest = totalMaxHarvest + dailyMaxHarvest;
    	//logger.info("max possible harvest on " + cropType.getName() + " : " + Math.round(maxHarvestinKgPerDay*100.0)/100.0 + " kg per day");

		//totalHarvestinKgPerDay = (maxHarvestinKgPerDay + totalHarvestinKgPerDay) /2;
		double percentAvailable = 0;

	    if (isNewCrop) {
	        //2015-08-26 Added useSeedlings()
	    	percentAvailable = useTissueCulture(cropType, cropArea);
	    	// 2015-01-14 Added fertilizer to the soil for the new crop
	        provideFertilizer(cropArea);
	        // 2015-02-28 Replaced some amount of old soil with new soil
	        provideNewSoil(cropArea);

	    }

		crop = new Crop(cropType, cropArea, dailyMaxHarvest, this, settlement, isNewCrop, percentAvailable);

    	return crop;
    }

    /**
     * Retrieves new soil when planting new crop
     */
    // 2015-02-28 provideNewSoil()
    public void provideNewSoil(double cropArea) {
        // 2015-02-28 Replaced some amount of old soil with new soil
    	double rand = RandomUtil.getRandomDouble(1.2);

    	double amount = Crop.NEW_SOIL_NEEDED_PER_SQM * cropArea *rand;

    	// TODO: adjust how much old soil should be turned to crop waste
    	Storage.storeAnResource(amount, Crop.CROP_WASTE, s_inv);

    	// TODO: adjust how much new soil is needed to replenish the soil bed
    	Storage.retrieveAnResource(amount, Crop.SOIL, s_inv, true );

    }

    public Building getBuilding() {
    	return building;
    };

    /**
     * Retrieves the fertilizer and add to the soil when planting the crop
     */
    //2015-02-28 Modified provideFertilizer()
    public void provideFertilizer(double cropArea) {
    	double rand = RandomUtil.getRandomDouble(2);
    	double amount = Crop.FERTILIZER_NEEDED_IN_SOIL_PER_SQM * cropArea / 10D * rand;
    	Storage.retrieveAnResource(amount, Crop.FERTILIZER, s_inv, true);
		//System.out.println("fertilizer used in planting a new crop : " + amount);
    }

    /**
     * Uses available tissue culture to shorten Germinating Phase when planting the crop
     * @parama cropType
     * @param cropArea
     * @return percentAvailable
     */
    //2015-08-26 Added useSeedlings()
    //2015-09-18 Changed to useTissueCulture()
    public double useTissueCulture(CropType cropType, double cropArea) {
    	double percent = 0;

    	double requestedAmount = cropArea * cropType.getEdibleBiomass() * TISSUE_PER_SQM;

    	String tissue = cropType.getName()+ " " + Crop.TISSUE_CULTURE;
    	String name = Conversion.capitalize(cropType.getName()) + " " + Crop.TISSUE_CULTURE;
    	
    	boolean available = false;

      	try {
	    	AmountResource nameAR = AmountResource.findAmountResource(tissue);
	        double amountStored = s_inv.getAmountResourceStored(nameAR, false);
			s_inv.addAmountDemandTotalRequest(nameAR);
	    	
	    	if (amountStored < 0.0000000001) {
	    		logger.warning("No more " + name);
	    		percent = 0;
	    	}
	    	
	    	else if (amountStored < requestedAmount) {
	    		available = true;
	    		percent = amountStored / requestedAmount * 100D;
	    		requestedAmount = amountStored ;
	    		logger.info(name + " is partially available : " + requestedAmount + " kg");
	    	}

	    	else {
	    		available = true;
	    		percent = 100D ;
	    		logger.info(name + " is fully available : " + requestedAmount + " kg");
	    	}

	    	if (available) {
	    		s_inv.retrieveAmountResource(nameAR, requestedAmount);
	    	}

			s_inv.addAmountDemand(nameAR, requestedAmount);

	    }  catch (Exception e) {
    		logger.log(Level.SEVERE,e.getMessage());
	    }

      	return percent;

    }

    //2014-12-09 Added setCropInQueue()
    public void setCropInQueue(String cropInQueue) {
    	this.cropInQueue = cropInQueue;
    }

    //2014-12-09 Added getCropInQueue()
    public String getCropInQueue() {
    	return cropInQueue;
    }

    /**
     * Gets a collection of the CropType.
     * @return Collection of CropType
     */
    //2014-12-09 Added getCropListInQueue()
    public List<CropType> getCropListInQueue() {
        return cropListInQueue;
      //CollectionUtils.getPerson(getSettlementInventory().getContainedUnits());
    }

    /**
     * Adds a cropType to cropListInQueue.
     * @param cropType
     */
    //2014-12-09 Added addCropListInQueue()
    public void addCropListInQueue(CropType cropType) {
    	if (cropType != null) {
    		cropListInQueue.add(cropType);
    	}
    }

    /**
     * Deletes a cropType to cropListInQueue.
     * @param cropType
     */
    //2014-12-09 Added deleteCropListInQueue()
    public void deleteACropFromQueue(int index, CropType cropType) {
     	//cropListInQueue.remove(index);
     	// Safer removal than cropListInQueue.remove(index)
    	int size = cropListInQueue.size();
       	if ( size > 0) {
     		Iterator<CropType> j = cropListInQueue.iterator();
     		int i = 0;
    		while (j.hasNext()) {
    			CropType c = j.next();
    			if ( i == index) {
    		    	//System.out.println("Farming.java: deleteCropListInQueue() : i is at " + i);
    				String name = c.getName();
    		    	//System.out.println("Farming.java: deleteCropListInQueue() : c is " + c);
    	 			if (!cropType.getName().equals(name))
    	 				logger.log(java.util.logging.Level.SEVERE, "The crop queue encounters a problem removing a crop");
    	 			else {
    	 				j.remove();
    	 		       	//System.out.println("Farming.java: deleteCropListInQueue() : queue size is now " + cropListInQueue.size());
        				break; // remove the first entry only
    	 			}
    			}
    			i++;
    		}
    	}

    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * Called by BuildingManager.java getBuildingValue()
     */
    public static double getFunctionValue(String buildingName, boolean newBuilding,
            Settlement settlement) {

        double result = 0D;
        
        // Demand is farming area (m^2) needed to produce food for settlement population.
        double requiredFarmingAreaPerPerson = getFarmingAreaNeededPerPerson();
        double demand = requiredFarmingAreaPerPerson * settlement.getAllAssociatedPeople().size();

        // Supply is total farming area (m^2) of all farming buildings at settlement.
        double supply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
                removedBuilding = true;
            }
            else {
                Farming farmingFunction = (Farming) building.getFunction(FUNCTION);
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
                supply += farmingFunction.getGrowingArea() * wearModifier;
            }
        }

        // Modify result by value (VP) of food at the settlement.
        Good foodGood = GoodsUtil.getResourceGood(AmountResource.findAmountResource("food"));
        double foodValue = settlement.getGoodsManager().getGoodValuePerItem(foodGood);
        
        result = (demand / (supply + 1D)) * foodValue;
        
        // TODO: investigating if other food group besides food should be added as well
        
        return result;
    }
    
    /**
     * Gets the average area (m^2) of farming surface required to sustain one person.
     * @return area (m^2) of farming surface.
     */
    public static double getFarmingAreaNeededPerPerson() {
        
        // Determine average amount (kg) of food required per person per orbit.
        double neededFoodPerSol = SimulationConfig.instance().getPersonConfiguration().getFoodConsumptionRate();
        
        // Determine average amount (kg) of food produced per farm area (m^2).
        CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
        double totalFoodPerSolPerArea = 0D;
        List<CropType> cropList = cropConfig.getCropList();
        Iterator<CropType> i = cropList.iterator();
        while (i.hasNext()) {
            CropType cropType = i.next();
            // Crop type average edible biomass (kg) per Sol.
            totalFoodPerSolPerArea += cropType.getEdibleBiomass() / 1000D;
        }
        double producedFoodPerSolPerArea = totalFoodPerSolPerArea / cropList.size();
        
        return neededFoodPerSol / producedFoodPerSolPerArea;
    }

    /**
     * Gets the farm's current crops.
     * @return collection of crops
     */
    public List<Crop> getCrops() {
        return crops;
    }

    /**
     * Checks if farm currently requires work.
     * @return true if farm requires work
     */
    public boolean requiresWork() {
        boolean result = false;
        Iterator<Crop> i = crops.iterator();
        while (i.hasNext()) {
            if (i.next().requiresWork()) result = true;
        }
        return result;
    }

    /**
     * Adds work time to the crops current phase.
     * @param workTime - Work time to be added (millisols)
     * @return workTime remaining after working on crop (millisols)
     * @throws Exception if error adding work.
     */
    public double addWork(double workTime) {
        double workTimeRemaining = workTime;
        Crop needyCrop = null;
        // Scott - I used the comparison criteria 00001D rather than 0D
        // because sometimes math anomalies result in workTimeRemaining
        // becoming very small double values and an endless loop occurs.
        while (((needyCrop = getNeedyCrop()) != null) && (workTimeRemaining > .00001D)) {
            workTimeRemaining = needyCrop.addWork(workTimeRemaining);
        }

        //System.out.println("addWork() : workTimeRemaining is " + workTimeRemaining);
        return workTimeRemaining;
    }

    /**
     * Gets a crop that needs planting, tending, or harvesting.
     * @return crop or null if none found.
     */
    private Crop getNeedyCrop() {
        Crop result = null;

        List<Crop> needyCrops = new ArrayList<Crop>(crops.size());
        Iterator<Crop> i = crops.iterator();
        while (i.hasNext()) {
            Crop crop = i.next();
            if (crop.requiresWork()) {
                needyCrops.add(crop);
            }
        }

        if (needyCrops.size() > 0) {
            result = needyCrops.get(RandomUtil.getRandomInt(needyCrops.size() - 1));
        }

        return result;
    }

    /**
     * Adds the crop harvest to the farm.
     * @param harvest: harvested food to add (kg.)
     * @param cropCategory


    // Note: this method was called by Crop.java's addWork()
    // 2014-10-14 Added String cropCategory to the param list,
    // 2014-11-29 Used cropName instead of cropCategory when creating amount resource
    public void addHarvest(double harvestAmount, String cropName, String cropCategory) {
    	try {
            AmountResource harvestCropAR = AmountResource.findAmountResource(cropName);
            double remainingCapacity = inv.getAmountResourceRemainingCapacity(harvestCropAR, false, false);

            if (remainingCapacity < harvestAmount) {
                // if the remaining capacity is smaller than the harvested amount, set remaining capacity to full
            	harvestAmount = remainingCapacity;
                 	//logger.info("addHarvest() : storage is full!");
                }
            // TODO: consider the case when it is full
            // add the harvest to the remaining capacity
            // 2014-11-06 changed the last param from false to true
            inv.storeAmountResource(harvestCropAR, harvestAmount, true);
            // 2015-01-15 Add addSupplyAmount()
            inv.addAmountSupplyAmount(harvestCropAR, harvestAmount);

        }  catch (Exception e) {
    		logger.log(Level.SEVERE,e.getMessage());
        }
    }
*/
    /**
     * Gets the number of farmers currently working at the farm.
     * @return number of farmers
     */
    public int getFarmerNum() {
        int result = 0;

        if (building.hasFunction(BuildingFunction.LIFE_SUPPORT)) {
            try {
                LifeSupport lifeSupport = (LifeSupport) building.getFunction(BuildingFunction.LIFE_SUPPORT);
                Iterator<Person> i = lifeSupport.getOccupants().iterator();
                while (i.hasNext()) {
                    Task task = i.next().getMind().getTaskManager().getTask();
                    if (task instanceof TendGreenhouse) result++;
                }
            }
            catch (Exception e) {
        		logger.log(Level.SEVERE,e.getMessage());
            }
        }

        return result;
    }

    /**
     * Time passing for the building.
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     */
	public void timePassing(double time) {
		//System.out.println("timePassing() : time is " + time);

		//MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();
	    // check for the passing of each day
	    int solElapsed = marsClock.getSolElapsedFromStart();
	    if ( solElapsed != solCache) {
			solCache = solElapsed;
			// 2016-10-12 reset cumulativeDailyPAR
			Iterator<Crop> i = crops.iterator();
			while (i.hasNext()) {
				i.next().resetPAR();
			}
		}

        // Determine the production level.
        double productionLevel = 0D;
        if (building.getPowerMode() == PowerMode.FULL_POWER) productionLevel = 1D;
        else if (building.getPowerMode() ==  PowerMode.POWER_DOWN) productionLevel = .5D;
		//System.out.println("timePassing() : productionLevel  is " + productionLevel );

        // Add time to each crop.
        Iterator<Crop> i = crops.iterator();
        int newCrops = 0;
        while (i.hasNext()) {
            Crop crop = i.next();
            crop.timePassing(time * productionLevel);

            // Remove old crops.
            if (crop.getPhaseType() == PhaseType.FINISHED) {
                remainingGrowingArea = remainingGrowingArea + crop.getGrowingArea();
                i.remove();
                //System.out.println("Farming : " + crop.getCropType().getName() + " is removed.");
                newCrops++;
                //System.out.println("Farming timePassing() newCrops++ : remainingGrowingArea is "+ remainingGrowingArea);
            }
        }

        // Add any new crops.
        for (int x = 0; x < newCrops; x++) {
           	// 2014-12-09 Added cropInQueue and changed method name to getNewCrop()
        	//CropType cropType = Crop.getNewCrop();
        	CropType cropType = null;
        	int size = cropListInQueue.size();
          	if ( size > 0) {
         		// Safer to remove using iterator than just cropListInQueue.remove(0);
        		Iterator<CropType> j = cropListInQueue.iterator();
        		while (j.hasNext()) {
        			CropType c = j.next();
        			cropType = c;
        			cropInQueue = cropType.getName();
        			j.remove();
        			break; // remove the first entry only
        		}
          	} else
        		cropType = getNewCrop("0", true);

            //System.out.println("Farming timePassing() : calling plantACrop()");
          	Crop crop = plantACrop(cropType, true, 0);
            crops.add(crop);

            settlement.fireUnitUpdate(UnitEventType.CROP_EVENT, crop);
        }

        // 2015-02-18 Added beeGrowing.timePassing()
        //beeGrowing.timePassing(time);

    }

	/**
     * Creates crop waste from the daily tending of the greenhouse
     *
     */
	// 2015-02-26 Added produceDailyTendingCropWaste()
	public void produceDailyTendingCropWaste() {
		double rand = RandomUtil.getRandomDouble(2);
		// add a randomness factor
		double amountCropWaste = CROP_WASTE_PER_SQM_PER_SOL * maxGrowingArea * rand;
		Storage.storeAnResource(amountCropWaste, Crop.CROP_WASTE, s_inv);
	}

/*
   public void storeAnResource(double amount, String name) {
    	try {
            AmountResource ar = AmountResource.findAmountResource(name);
            double remainingCapacity = inv.getAmountResourceRemainingCapacity(ar, false, false);

            if (remainingCapacity < amount) {
                // if the remaining capacity is smaller than the harvested amount, set remaining capacity to full
            	amount = remainingCapacity;
                logger.info(" storage is full!");
            }
            // TODO: consider the case when it is full
            inv.storeAmountResource(ar, amount, true);
            inv.addAmountSupplyAmount(ar, amount);
        }  catch (Exception e) {
    		logger.log(Level.SEVERE,e.getMessage());
        }
    }
*/
    /**
     * Gets the amount of power required when function is at full power.
     * @return power (kW)
     */
    public double getFullPowerRequired() {
        // Power (kW) required for normal operations.
        double powerRequired = 0D;

        Iterator<Crop> i = crops.iterator();
        while (i.hasNext()) {
            Crop crop = i.next();
            if (crop.getPhaseType() == PhaseType.PLANTING || crop.getPhaseType() == PhaseType.INCUBATION)
            	powerRequired += powerGrowingCrop/2D; // half power is needed for illumination and crop monitoring
            else if (crop.getPhaseType() == PhaseType.HARVESTING || crop.getPhaseType() == PhaseType.FINISHED)
            	powerRequired += powerGrowingCrop/5D;
            else //if (crop.getPhaseType() == PhaseType.GROWING || crop.getPhaseType() == PhaseType.GERMINATION)
            	powerRequired += powerGrowingCrop + crop.getLightingPower();
        }

        // TODO: add separate auxiliary power for subsystem, not just lighting power

        return powerRequired;
    }

    /**
     * Gets the total amount of lighting power in this greenhouse.
     * @return power (kW)
     */
    public double getTotalLightingPower() {
        double powerRequired = 0D;

        Iterator<Crop> i = crops.iterator();
        while (i.hasNext()) {
            Crop crop = i.next();
            powerRequired += crop.getLightingPower();
        }

        return powerRequired;
    }


    /**
     * Gets the amount of power required when function is at power down level.
     * @return power (kW)
     */
    public double getPoweredDownPowerRequired() {

        // Get power required for occupant life support.
        double powerRequired = 0D;

        // Add power required to sustain growing or harvest-ready crops.
        Iterator<Crop> i = crops.iterator();
        while (i.hasNext()) {
            Crop crop = i.next();
            if ((crop.getPhaseNum() > 2 && crop.getPhaseNum() < crop.getPhases().size() - 1)
            		|| crop.getPhaseNum() == 2)
				powerRequired += powerSustainingCrop;
        }

        return powerRequired;
    }

    
    /**
     * Gets the total growing area for all crops.
     * @return growing area in square meters
     */
    public double getGrowingArea() {
        return maxGrowingArea;
    }

    /**
     * Gets the average number of growing cycles for a crop
     * per orbit.
     */
    public double getAverageGrowingCyclesPerOrbit() {
        
        double aveGrowingTime = Crop.getAverageCropGrowingTime();
        int solsInOrbit = MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR;
        double aveGrowingCyclesPerOrbit = solsInOrbit * 1000D / aveGrowingTime; // e.g. 668 sols * 1000 / 50,000 millisols
        
        return aveGrowingCyclesPerOrbit;
    }
    
    /**
     * Gets the estimated maximum harvest for one orbit.
     * @return max harvest (kg)
     */
    public double getEstimatedHarvestPerOrbit() {

        // Add max harvest for each crop.
        double totalMaxHarvest = 0D;
        Iterator<Crop> i = crops.iterator();
        while (i.hasNext()) {
            Crop crop = i.next();
            totalMaxHarvest += crop.getMaxHarvest();
        }
            
        return totalMaxHarvest * getAverageGrowingCyclesPerOrbit(); // 40 kg * 668 sols / 50
    }

    @Override
    public double getMaintenanceTime() {
        return maxGrowingArea * 5D;
    }


	public void addNumLamp(int num) {
		numHPSinNeed = numHPSinNeed + num;
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

    @Override
    public void destroy() {
        super.destroy();

        s_inv = null;
		b_inv = null;
        settlement = null;
        building = null;

        Iterator<CropType> i = cropListInQueue.iterator();
        while (i.hasNext()) {
            i.next().destroy();
        }
        Iterator<Crop> ii = crops.iterator();
        while (ii.hasNext()) {
            ii.next().destroy();
        }

        Iterator<CropType> iii = cropTypeList.iterator();
        while (iii.hasNext()) {
            iii.next().destroy();
        }
        Iterator<CropType> iv = historyList.iterator();
        while (iv.hasNext()) {
            iv.next().destroy();
        }

    }

}

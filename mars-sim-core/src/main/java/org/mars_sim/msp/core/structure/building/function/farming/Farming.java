/**
* Mars Simulation Project
 * Farming.java
 * @version 3.1.0 2016-10-11
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.TendGreenhouse;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Farming class is a building function for greenhouse farming.
 */

public class Farming extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(Farming.class.getName());

	private static String sourceName = logger.getName();

	private static final FunctionType FARMING_FUNCTION = FunctionType.FARMING;

	public static final String FERTILIZER = "fertilizer";
	public static final String SOIL = "soil";
	public static final String CROP_WASTE = "crop waste";
	public static final String TISSUE_CULTURE = " tissue culture";
	// public static final String LED_KIT = "light emitting diode kit";
	// public static final String HPS_LAMP = "high pressure sodium lamp";
	/**
	 * amount of crop tissue culture needed for each square meter of growing area
	 */
	public static final double TISSUE_PER_SQM = .0005; // 1/2 gram (arbitrary)
	public static final double STANDARD_AMOUNT_TISSUE_CULTURE = 0.05;
	public static final double CO2_RATE = 400;
	public static final double O2_RATE = .75;

	private static final int NUM_INSPECTIONS = 2;
	private static final int NUM_CLEANING = 2;

	/** The list of crop types from CropConfig. */
	private static List<CropType> cropTypeList;

	// private static ItemResource LED_Item;
	// private static ItemResource HPS_Item;

	/** The number of crop types available */
	private static int size;

	// private int numLEDInUse;
	// private int cacheNumLED;
	private int numHPSinNeed;
	private int cropNum;
	private int solCache = 1;

	private double powerGrowingCrop;
	private double powerSustainingCrop;
	private double maxGrowingArea;
	private double remainingGrowingArea;
	private double totalMaxHarvest = 0;

	/** The amount of air moisture in the greenhouse */
	private double moisture = 0;
	/** The amount of O2 generated in the greenhouse */
	private double o2 = 0;
	/** The amount of CO2 consumed in the greenhouse */
	private double cO2 = 0;

	private String cropInQueue;

	/** List of crop types in queue */
	private List<CropType> cropListInQueue = new ArrayList<CropType>();
	/** List of crop types the greenhouse is currently growing */
	private List<String> plantedCrops = new ArrayList<String>();
	/** List of crops the greenhouse is currently growing */
	private List<Crop> crops = new ArrayList<Crop>();

	private List<String> inspectionList, cleaningList;

	private Map<String, Integer> cleaningMap, inspectionMap;

	private Map<String, List<Double>> cropDailyWaterUsage;

	private Map<String, List<Double>> cropDailyO2Generated;

	private Map<String, List<Double>> cropDailyCO2Consumed;

	private static MarsClock marsClock;
//	private static SurfaceFeatures surface;

	private Inventory inv;
	private Settlement settlement;
	private Building building;
	private Research lab;

	/**
	 * Constructor.
	 * 
	 * @param building the building the function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public Farming(Building building) {
		// Use Function constructor.
		super(FARMING_FUNCTION, building);

		sourceName = sourceName.substring(sourceName.lastIndexOf(".") + 1, sourceName.length());

		// LED_Item = ItemResource.findItemResource(LED_KIT);
		// HPS_Item = ItemResource.findItemResource(HPS_LAMP);

		this.building = building;
		this.settlement = building.getBuildingManager().getSettlement();

		// this.b_inv = building.getBuildingInventory();
		this.inv = settlement.getInventory();
		// this.goodsManager = settlement.getGoodsManager();

		setupInspection();
		setupCleaning();

//		surface = Simulation.instance().getMars().getSurfaceFeatures();
		marsClock = Simulation.instance().getMasterClock().getMarsClock();

		BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
		powerGrowingCrop = buildingConfig.getPowerForGrowingCrop(building.getBuildingType());
		powerSustainingCrop = buildingConfig.getPowerForSustainingCrop(building.getBuildingType());
		maxGrowingArea = buildingConfig.getCropGrowingArea(building.getBuildingType());
		remainingGrowingArea = maxGrowingArea;

		CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
		if (cropTypeList == null)
			cropTypeList = new ArrayList<>(cropConfig.getCropList());
		size = cropTypeList.size();
		cropNum = buildingConfig.getCropNum(building.getBuildingType());

		// Load activity spots
		loadActivitySpots(buildingConfig.getFarmingActivitySpots(building.getBuildingType()));

		for (int x = 0; x < cropNum; x++) {
			// Add cropInQueue and chang method name to getNewCrop()
			CropType cropType = pickACrop(true, false);
			if (cropType == null)
				break;// for avoiding NullPointerException during maven test
			Crop crop = plantACrop(cropType, true, 0);
			crops.add(crop);
			building.getBuildingManager().getSettlement().fireUnitUpdate(UnitEventType.CROP_EVENT, crop);
		}

		// Create BeeGrowing
		// TODO: write codes to incorporate the idea of bee growing
		// beeGrowing = new BeeGrowing(this);

		cropDailyWaterUsage = new HashMap<>();
		cropDailyO2Generated = new HashMap<>();
		cropDailyCO2Consumed = new HashMap<>();

	}

	public void setupInspection() {
		inspectionMap = new HashMap<String, Integer>();
		inspectionList = new ArrayList<>();

		inspectionList.add("Environmental Control System");
		inspectionList.add("HVAC System");
		inspectionList.add("Waste Disposal System");
		inspectionList.add("Containment System");
		inspectionList.add("Any Traces of Contamination");
		inspectionList.add("Foundation");
		inspectionList.add("Structural Element");
		inspectionList.add("Thermal Budget");
		inspectionList.add("Water and Irrigation System");

		for (String s : inspectionList) {
			inspectionMap.put(s, 0);
		}
	}

	public void setupCleaning() {
		cleaningMap = new HashMap<String, Integer>();
		cleaningList = new ArrayList<>();

		cleaningList.add("Floor");
		cleaningList.add("Curtains");
		cleaningList.add("Canopy");
		cleaningList.add("Equipment");
		cleaningList.add("Pipings");
		cleaningList.add("Trays");
		cleaningList.add("Valves");

		for (String s : cleaningList) {
			cleaningMap.put(s, 0);
		}
	}

	/**
	 * Picks a crop type
	 * 
	 * @param isStartup - true if it is called at the start of the sim
	 * @return {@link CropType}
	 */
	// TODO: need to specify the person who is doing it using the work time in the
	// lab
	public CropType pickACrop(boolean isStartup, boolean noCorn) {
		CropType ct = null;
		boolean flag = true;
		// TODO: at the start of the sim, choose only from a list of staple food crop
		if (isStartup) {
			while (flag) {
				ct = getRandomCropType();
				if (noCorn && ct.getName().equalsIgnoreCase("corn")) {
					ct = pickACrop(isStartup, noCorn);
				}

				if (ct == null)
					break;
				flag = containCrop(ct.getName());
			}
		}

		else {
			while (flag) {
				ct = selectVPCrop();
				if (noCorn && ct.getName().equalsIgnoreCase("corn")) {
					ct = pickACrop(isStartup, noCorn);
				}

				if (ct == null)
					break;
				flag = containCrop(ct.getName());
			}
		}

		return ct;
	}

	/**
	 * Selects a crop currently having the highest value point (VP)
	 * 
	 * @return CropType
	 */
	public CropType selectVPCrop() {

		CropType no_1_crop = null;
		CropType no_2_crop = null;
		CropType chosen = null;
		double no_1_crop_VP = 0;
		double no_2_crop_VP = 0;

		for (CropType c : cropTypeList) {
			double cropVP = getCropValue(ResourceUtil.findAmountResource(c.getName()));
			if (cropVP >= no_1_crop_VP) {
				if (no_1_crop != null) {
					no_2_crop_VP = no_1_crop_VP;
					no_2_crop = no_1_crop;
				}
				no_1_crop_VP = cropVP;
				no_1_crop = c;

			} else if (cropVP > no_2_crop_VP) {
				no_2_crop_VP = cropVP;
				no_2_crop = c;
			}
		}

		String lastCT = null;
		String last2CT = null;
		boolean compareVP = false;

		int size = plantedCrops.size();
		if (size > 2) {
			// get the last two planted crops
			last2CT = plantedCrops.get(size - 2);
			lastCT = plantedCrops.get(size - 1);

			if (no_1_crop.getName().equalsIgnoreCase(last2CT) || no_1_crop.getName().equalsIgnoreCase(lastCT)) {
				// if highestCrop has already been selected once

				if (last2CT.equals(lastCT)) {
					// since the highestCrop has already been chosen twice,
					// should not choose the same crop type again
					// compareVP = false;
					chosen = no_2_crop;
				}

				else
					compareVP = true;
			}

			else if (no_2_crop.getName().equalsIgnoreCase(last2CT) || no_2_crop.getName().equalsIgnoreCase(lastCT)) {
				// if secondCrop has already been selected once

				if (last2CT.equals(lastCT)) {
					// since the secondCrop has already been chosen twice,
					// should not choose the same crop type again
					// compareVP = false;
					chosen = no_1_crop;
				}

				else
					compareVP = true;
			}

		}

		else if (size == 1) {
			lastCT = plantedCrops.get(0);

			if (lastCT != null) {
				// if highestCrop has already been selected for planting last time,
				if (no_1_crop.getName().equalsIgnoreCase(lastCT))
					compareVP = true;
			}
		}

		// else {
		// plantedCropList has 2 crops or no crops
		// }

		if (compareVP) {
			// compare secondVP with highestVP
			// if their VP are within 15%, toss a dice
			// if they are further apart, should pick highestCrop
			// if ((highestVP - secondVP) < .15 * secondVP)
			if ((no_2_crop_VP / no_1_crop_VP) > .85) {
				int rand = RandomUtil.getRandomInt(0, 1);
				if (rand == 0)
					chosen = no_1_crop;
				else
					chosen = no_2_crop;
			} else
				chosen = no_2_crop;
		} else
			chosen = no_1_crop;

		boolean flag = containCrop(chosen.getName());

		while (flag) {
			chosen = getRandomCropType();
			flag = containCrop(chosen.getName());
		}

		// if it's a mushroom, add increases the item demand of the mushroom containment
		// kit before the crop is planted
		if (chosen.getName().toLowerCase().contains("mushroom"))
			inv.addItemDemand(ItemResourceUtil.mushroomBoxAR, 2);

		return chosen;
	}

	public boolean containCrop(String name) {
		for (String c : plantedCrops) {
			if (c.equalsIgnoreCase(name))
				return true;
		}

		return false;
	}

	/**
	 * Picks a crop type randomly
	 * 
	 * @return crop type
	 */
	public CropType getRandomCropType() {
		if (size > 2)
			return cropTypeList.get(RandomUtil.getRandomInt(0, size - 1));
		else
			return null;
	}

	public double getCropValue(AmountResource resource) {
		return settlement.getGoodsManager().getGoodValuePerItem(
				GoodsUtil.getResourceGood(ResourceUtil.findIDbyAmountResourceName(resource.getName())));
	}

	/**
	 * Plants a new crop
	 * 
	 * @param cropType
	 * @param isStartup             - true if it's at the start of the sim
	 * @param designatedGrowingArea
	 * @return Crop
	 */
	public Crop plantACrop(CropType cropType, boolean isStartup, double designatedGrowingArea) {
		Crop crop = null;
		// Implement new way of calculating amount of food in kg,
		// accounting for the Edible Biomass of a crop
		// edibleBiomass is in [ gram / m^2 / day ]
		double edibleBiomass = cropType.getEdibleBiomass();
		// growing-time is in millisol vs. growingDay is in sol
		// double growingDay = cropType.getGrowingTime() / 1000D ;
		double cropArea = 0;
		if (remainingGrowingArea <= 1D) {
			cropArea = remainingGrowingArea;
		} else if (designatedGrowingArea != 0) {
			cropArea = designatedGrowingArea;
		} else { // if (remainingGrowingArea > 1D)
			cropArea = maxGrowingArea / (double) cropNum;
		}

		remainingGrowingArea = remainingGrowingArea - cropArea;
		// System.out.println("cropArea : "+ cropArea);

		if (remainingGrowingArea < 0)
			remainingGrowingArea = 0;
		// System.out.println("remainingGrowingArea : "+ remainingGrowingArea);

		// 1kg = 1000 g
		double dailyMaxHarvest = edibleBiomass / 1000D * cropArea;
		totalMaxHarvest = totalMaxHarvest + dailyMaxHarvest;
		// logger.info("max possible harvest on " + cropType.getName() + " : " +
		// Math.round(maxHarvestinKgPerDay*100.0)/100.0 + " kg per day");

		// totalHarvestinKgPerDay = (maxHarvestinKgPerDay + totalHarvestinKgPerDay) /2;
		double percentAvailable = 0;

		if (!isStartup) {
			// Use tissue culture
			percentAvailable = useTissueCulture(cropType, cropArea);
			// Add fertilizer to the soil for the new crop
			provideFertilizer(cropArea);
			// Replace some amount of old soil with new soil
			provideNewSoil(cropArea);

		}

		crop = new Crop(cropType, cropArea, dailyMaxHarvest, this, settlement, isStartup, percentAvailable);
		// TODO: Reduce the demand for this crop type

		plantedCrops.add(cropType.getName());
		// remove the very first crop on the list to prevent blotting.
		// plantedCrops.remove(0);

		return crop;
	}

	/**
	 * Retrieves new soil when planting new crop
	 */
	public void provideNewSoil(double cropArea) {
		// Replace some amount of old soil with new soil

		double rand = RandomUtil.getRandomDouble(1.2);

		double amount = Crop.NEW_SOIL_NEEDED_PER_SQM * cropArea * rand;

		// TODO: adjust how much old soil should be turned to crop waste
		Storage.storeAnResource(amount, ResourceUtil.cropWasteAR, inv, sourceName + "::provideNewSoil");

		// TODO: adjust how much new soil is needed to replenish the soil bed
		Storage.retrieveAnResource(amount, ResourceUtil.soilAR, inv, true);

	}

	public Building getBuilding() {
		return building;
	};

	/**
	 * Retrieves the fertilizer and add to the soil when planting the crop
	 */

	public void provideFertilizer(double cropArea) {
		double rand = RandomUtil.getRandomDouble(2);
		double amount = Crop.FERTILIZER_NEEDED_IN_SOIL_PER_SQM * cropArea / 10D * rand;
		Storage.retrieveAnResource(amount, ResourceUtil.fertilizerAR, inv, true);
	}

	/**
	 * Uses available tissue culture to shorten Germinating Phase when planting the
	 * crop
	 * 
	 * @parama cropType
	 * @param cropArea
	 * @return percentAvailable
	 */
	public double useTissueCulture(CropType cropType, double cropArea) {
		double percent = 0;

		double requestedAmount = cropArea * cropType.getEdibleBiomass() * TISSUE_PER_SQM;

		String tissueName = cropType.getName() + TISSUE_CULTURE;
		// String name = Conversion.capitalize(cropType.getName()) + TISSUE_CULTURE;
		AmountResource tissueAR = ResourceUtil.findAmountResource(tissueName);

		boolean available = false;

		try {

			double amountStored = inv.getAmountResourceStored(tissueAR, false);
			inv.addAmountDemandTotalRequest(tissueAR);

			if (amountStored < 0.0000000001) {
				LogConsolidated.log(logger, Level.INFO, 1000, sourceName,
						"[" + settlement + "]" + "Run out of " + tissueName, null);
				percent = 0;
			}

			else if (amountStored < requestedAmount) {
				available = true;
				percent = amountStored / requestedAmount * 100D;
				requestedAmount = amountStored;
				LogConsolidated.log(logger, Level.INFO, 1000, sourceName,
						"[" + settlement + "] " + Math.round(requestedAmount * 100.0) / 100.0 + " kg " + tissueName
								+ " is partially available.",
						null);
			}

			else {
				available = true;
				percent = 100D;
				LogConsolidated.log(logger, Level.INFO, 1000, sourceName, "[" + settlement + "] "
						+ Math.round(requestedAmount * 100.0) / 100.0 + " kg " + tissueName + " is fully available.",
						null);
			}

			if (available)
				inv.retrieveAmountResource(tissueAR, requestedAmount);

			inv.addAmountDemand(tissueAR, requestedAmount);

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
		}

		return percent;

	}

	public void setCropInQueue(String cropInQueue) {
		this.cropInQueue = cropInQueue;
	}

	public String getCropInQueue() {
		return cropInQueue;
	}

	/**
	 * Gets a collection of the CropType.
	 * 
	 * @return Collection of CropType
	 */
	public List<CropType> getCropListInQueue() {
		return cropListInQueue;
	}

	/**
	 * Adds a cropType to the crop queue.
	 * 
	 * @param cropType
	 */
	public void addCropListInQueue(CropType cropType) {
		if (cropType != null) {
			cropListInQueue.add(cropType);
		}
	}

	/**
	 * Deletes a cropType to cropListInQueue.
	 * 
	 * @param cropType
	 */
	public void deleteACropFromQueue(int index, CropType cropType) {
		// cropListInQueue.remove(index);
		// Safer removal than cropListInQueue.remove(index)
		int size = cropListInQueue.size();
		if (size > 0) {
			Iterator<CropType> j = cropListInQueue.iterator();
			int i = 0;
			while (j.hasNext()) {
				CropType c = j.next();
				if (i == index) {
					// System.out.println("Farming.java: deleteCropListInQueue() : i is at " + i);
					String name = c.getName();
					// System.out.println("Farming.java: deleteCropListInQueue() : c is " + c);
					if (!cropType.getName().equals(name))
						logger.log(java.util.logging.Level.SEVERE,
								"The crop queue encounters a problem removing a crop");
					else {
						j.remove();
						break; // remove the first entry only
					}
				}
				i++;
			}
		}

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

		// Demand is farming area (m^2) needed to produce food for settlement
		// population.
		double requiredFarmingAreaPerPerson = getFarmingAreaNeededPerPerson();
		double demand = requiredFarmingAreaPerPerson * settlement.getNumCitizens();

		// Supply is total farming area (m^2) of all farming buildings at settlement.
		double supply = 0D;
		boolean removedBuilding = false;
		List<Building> buildings = settlement.getBuildingManager().getBuildings(FARMING_FUNCTION);
		for (Building building : buildings) {
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				Farming farmingFunction = (Farming) building.getFunction(FARMING_FUNCTION);
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += farmingFunction.getGrowingArea() * wearModifier;
			}
		}

		// Modify result by value (VP) of food at the settlement.
		Good foodGood = GoodsUtil.getResourceGood(ResourceUtil.foodID);
		double foodValue = settlement.getGoodsManager().getGoodValuePerItem(foodGood);

		result = (demand / (supply + 1D)) * foodValue;

		// TODO: investigating if other food group besides food should be added as well

		return result;
	}

	/**
	 * Gets the average area (m^2) of farming surface required to sustain one
	 * person.
	 * 
	 * @return area (m^2) of farming surface.
	 */
	public static double getFarmingAreaNeededPerPerson() {

		// Determine average amount (kg) of food required per person per orbit.
		double neededFoodPerSol = SimulationConfig.instance().getPersonConfiguration().getFoodConsumptionRate();

		// Determine average amount (kg) of food produced per farm area (m^2).
		// CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
		double totalFoodPerSolPerArea = 0D;
		for (CropType c : cropTypeList)
			// Crop type average edible biomass (kg) per Sol.
			totalFoodPerSolPerArea += c.getEdibleBiomass() / 1000D;

		double producedFoodPerSolPerArea = totalFoodPerSolPerArea / size;

		return neededFoodPerSol / producedFoodPerSolPerArea;
	}

	/**
	 * Checks if farm currently requires work.
	 * 
	 * @return true if farm requires work
	 */
	public boolean requiresWork() {
		boolean result = false;
		for (Crop c : crops) {
			if (c.requiresWork()) {
				return true;
			}
		}
		return result;
	}

	/**
	 * Adds work time to the crops current phase.
	 * 
	 * @param workTime - Work time to be added (millisols)
	 * @param h        - an instance of TendGreenhouse
	 * @param unit     - a person or bot
	 * @return workTime remaining after working on crop (millisols)
	 * @throws Exception if error adding work.
	 */
	public double addWork(double workTime, TendGreenhouse h, Unit unit) {
		double timeRemaining = workTime;
		Crop needyCropCache = null;
		Crop needyCrop = getNeedyCrop(needyCropCache);
		// Scott - I used the comparison criteria 00001D rather than 0D
		// because sometimes math anomalies result in workTimeRemaining
		// becoming very small double values and an endless loop occurs.
		while (needyCrop != null && timeRemaining > .00001D) {

			timeRemaining = needyCrop.addWork(unit, timeRemaining);

			needyCropCache = needyCrop;
			// Get a new needy crop
			needyCrop = getNeedyCrop(needyCropCache);

			// if (needyCropCache != null && needyCrop != null &&
			// needyCropCache.equals(needyCrop)) {
			if (needyCrop != null && !needyCropCache.equals(needyCrop)) {
				// Update the name of the crop being worked on in the task
				// description
				h.setCropDescription(needyCrop);
			}

//			
//			 if (needyCropCache != null && needyCrop != null) { //
//			 logger.info("inside while loop. lastCrop is " + lastCrop.getCropType()); 
//			 if (needyCropCache.equals(needyCrop)) { 
//				 // Update the name of the crop being worked on in the task description 
//				 h.setCropDescription(needyCrop);
//			 } }
//			 
		}

		return timeRemaining;
	}

	/**
	 * Gets a crop that needs planting, tending, or harvesting.
	 * 
	 * @param lastCrop
	 * @param unit     a person or a bot
	 * @return crop or null if none found.
	 */
	public Crop getNeedyCrop(Crop lastCrop) {// , Unit unit) {
		Crop result = null;
		/*
		 * if (unit instanceof Person) p = (Person) unit; else r = (Robot) unit;
		 */
		List<Crop> needyCrops = new ArrayList<Crop>(crops.size());
		for (Crop c : crops) {
			if (c.requiresWork()) {
				if (lastCrop != null) {
					if (c.getCropType() == lastCrop.getCropType())
						return c;
					// else if (cropAssignment.get(unit) == c) {
					// updateAssignmentMap(unit);
					// return c;
					// }
				} else
					needyCrops.add(c);
			}
		}

		int size = needyCrops.size();
		if (size == 1)
			result = needyCrops.get(0);

		else if (size > 1) {
			result = needyCrops.get(RandomUtil.getRandomInt(0, size - 1));
			// updateCropAssignment(unit, result);
		}

		// if (result == null) logger.info("getNeedyCrop() is null");

		return result;
	}

	/**
	 * Gets the number of farmers currently working at the farm.
	 * 
	 * @return number of farmers
	 */
	public int getFarmerNum() {
		int result = 0;

		if (building.hasFunction(FunctionType.LIFE_SUPPORT)) {
			try {
				LifeSupport lifeSupport = (LifeSupport) building.getFunction(FunctionType.LIFE_SUPPORT);
				Iterator<Person> i = lifeSupport.getOccupants().iterator();
				while (i.hasNext()) {
					Task task = i.next().getMind().getTaskManager().getTask();
					if (task instanceof TendGreenhouse)
						result++;
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		}

		return result;
	}

	public double TotalPercentGrowth() {
		int sum = 0;
		for (Crop crop : crops) {
			sum += crop.getPercentGrowth();
		}
		return sum;
	}

	/**
	 * Time passing for the building.
	 * 
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {

		// check for the passing of each day
		int solElapsed = marsClock.getMissionSol();
		if (solElapsed != solCache) {
			solCache = solElapsed;

			for (String s : cleaningMap.keySet()) {
				cleaningMap.put(s, 0);
			}
			// Reset cumulativeDailyPAR
			for (Crop c : crops)
				c.resetPAR();
		}

		// Determine the production level.
		double productionLevel = 0D;
		if (building.getPowerMode() == PowerMode.FULL_POWER)
			productionLevel = 1D;
		else if (building.getPowerMode() == PowerMode.POWER_DOWN)
			productionLevel = .5D;

		// Add time to each crop.
		Iterator<Crop> i = crops.iterator();
		List<String> harvestedCrops = null;
		int numCrops2Plant = 0;
		while (i.hasNext()) {
			Crop crop = i.next();
			crop.timePassing(time * productionLevel);
			// Remove old crops.
			if (crop.getPhaseType() == PhaseType.FINISHED) {
				remainingGrowingArea = remainingGrowingArea + crop.getGrowingArea();
				if (harvestedCrops == null)
					harvestedCrops = new ArrayList<>();
				harvestedCrops.add(crop.getCropType().getName());
				i.remove();
				plantedCrops.remove(crop.getCropType().getName());
				numCrops2Plant++;
			}
		}

		// Add any new crops.
		for (int x = 0; x < numCrops2Plant; x++) {
			// Add cropInQueue and change method name to getNewCrop()
			CropType cropType = null;
			int size = cropListInQueue.size();
			if (size > 0) {
				// Safer to remove using iterator than just cropListInQueue.remove(0);
				Iterator<CropType> j = cropListInQueue.iterator();
				while (j.hasNext()) {
					CropType c = j.next();
					cropType = c;
					cropInQueue = cropType.getName();

					Iterator<String> k = harvestedCrops.iterator();
					while (k.hasNext()) {
						String s = k.next();
						// if the harvest crops contain corn, one cannot plant corn again
						// since corn depletes nitrogen quickly in the soil.
						if (s.equalsIgnoreCase("corn") && !c.getName().equalsIgnoreCase("corn")) {
							j.remove();
							break;
						}
					}
				}
			}

			else {
				for (String s : harvestedCrops) {
					// if the harvest crops contain corn, one cannot plant corn again
					// since corn depletes nitrogen quickly in the soil.
					if (s.equalsIgnoreCase("corn")) {
						cropType = pickACrop(false, true);
						break;
					} else {
						cropType = pickACrop(false, false);
						break;
					}
				}
			}

			// TODO: need to specify the person who is doing it using the work time in the
			// lab
			// System.out.println("Farming timePassing() : calling plantACrop()");
			Crop crop = plantACrop(cropType, false, 0);
			crops.add(crop);

			settlement.fireUnitUpdate(UnitEventType.CROP_EVENT, crop);
		}

		// Add beeGrowing.timePassing()
		// beeGrowing.timePassing(time);

	}

	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		// Power (kW) required for normal operations.
		double powerRequired = 0D;

		for (Crop crop : crops) {
			if (crop.getPhaseType() == PhaseType.PLANTING || crop.getPhaseType() == PhaseType.INCUBATION)
				powerRequired += powerGrowingCrop / 2D; // half power is needed for illumination and crop monitoring
			else if (crop.getPhaseType() == PhaseType.HARVESTING || crop.getPhaseType() == PhaseType.FINISHED)
				powerRequired += powerGrowingCrop / 5D;
			// else //if (crop.getPhaseType() == PhaseType.GROWING || crop.getPhaseType() ==
			// PhaseType.GERMINATION)
			// powerRequired += powerGrowingCrop + crop.getLightingPower();
		}

		powerRequired += getTotalLightingPower();

		// TODO: add separate auxiliary power for subsystem, not just lighting power

		return powerRequired;
	}

	/**
	 * Gets the total amount of lighting power in this greenhouse.
	 * 
	 * @return power (kW)
	 */
	public double getTotalLightingPower() {
		double powerRequired = 0D;

		for (Crop c : crops)
			powerRequired += c.getLightingPower();

		return powerRequired;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * 
	 * @return power (kW)
	 */
	public double getPoweredDownPowerRequired() {

		// Get power required for occupant life support.
		double powerRequired = 0D;

		// Add power required to sustain growing or harvest-ready crops.
		for (Crop crop : crops) {
			if ((crop.getCurrentPhaseNum() > 2 && crop.getCurrentPhaseNum() < crop.getPhases().size() - 1)
					|| crop.getCurrentPhaseNum() == 2)
				powerRequired += powerSustainingCrop;
		}

		return powerRequired;
	}

	/**
	 * Gets the total growing area for all crops.
	 * 
	 * @return growing area in square meters
	 */
	public double getGrowingArea() {
		return maxGrowingArea;
	}

	/**
	 * Gets the average number of growing cycles for a crop per orbit.
	 */
	public double getAverageGrowingCyclesPerOrbit() {

		double aveGrowingTime = Crop.getAverageCropGrowingTime();
		int solsInOrbit = MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;
		double aveGrowingCyclesPerOrbit = solsInOrbit * 1000D / aveGrowingTime; // e.g. 668 sols * 1000 / 50,000
																				// millisols

		return aveGrowingCyclesPerOrbit;
	}

	/**
	 * Gets the estimated maximum harvest for one orbit.
	 * 
	 * @return max harvest (kg)
	 */
	public double getEstimatedHarvestPerOrbit() {
		// Add max harvest for each crop.
		double totalMaxHarvest = 0D;
		for (Crop crop : crops)
			totalMaxHarvest += crop.getMaxHarvest();

		return totalMaxHarvest * getAverageGrowingCyclesPerOrbit(); // 40 kg * 668 sols / 50
	}

	/**
	 * Checks to see if a botany lab with an open research slot is available and
	 * performs cell tissue extraction
	 * 
	 * @param type
	 */
	public boolean checkBotanyLab(CropType type) {
		// Check to see if a botany lab is available
		boolean hasEmptySpace = false;
		boolean done = false;

		// List<String> tissues = lab0.getTissueCultureList();

		// Check to see if the local greenhouse has a research slot
		if (lab == null)
			lab = building.getResearch();
		if (lab.hasSpecialty(ScienceType.BOTANY)) {
			hasEmptySpace = lab.checkAvailability();
		}

		if (hasEmptySpace) {
			// check to see if it can accommodate another researcher
			hasEmptySpace = lab.addResearcher();

			if (hasEmptySpace) {
				growCropTissue(lab, type);// , true);
				lab.removeResearcher();
			}

			done = true;
		}

		else {
			// Check available research slot in another lab located in another greenhouse
			List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(FunctionType.RESEARCH);
			Iterator<Building> i = laboratoryBuildings.iterator();
			while (i.hasNext() && !hasEmptySpace) {
				Building building = i.next();
				Research lab1 = building.getResearch();
				if (lab1.hasSpecialty(ScienceType.BOTANY)) {
					hasEmptySpace = lab1.checkAvailability();
					if (hasEmptySpace) {
						hasEmptySpace = lab1.addResearcher();
						if (hasEmptySpace) {
							growCropTissue(lab1, type);// true);
							lab.removeResearcher();
						}

						// TODO: compute research ooints to determine if it can be carried out.
						// int points += (double) (lab.getResearcherNum() * lab.getTechnologyLevel()) /
						// 2D;
						done = true;
					}
				}
			}
		}

		// check to see if a person can still "squeeze into" this busy lab to get lab
		// time
		if (!hasEmptySpace && (lab.getLaboratorySize() == lab.getResearcherNum())) {
			growCropTissue(lab, type);// , false);
			done = true;
		} else {

			// Check available research slot in another lab located in another greenhouse
			List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(FunctionType.RESEARCH);
			Iterator<Building> i = laboratoryBuildings.iterator();
			while (i.hasNext() && !hasEmptySpace) {
				Building building = i.next();
				Research lab2 = building.getResearch();
				if (lab2.hasSpecialty(ScienceType.BOTANY)) {
					hasEmptySpace = lab2.checkAvailability();
					if (lab2.getLaboratorySize() == lab2.getResearcherNum()) {
						growCropTissue(lab2, type);// , false);
						done = true;
					}
				}
			}
		}

		return done;
	}

	/**
	 * Grows crop tissue cultures
	 * 
	 * @param lab
	 * @param croptype
	 */
	public boolean growCropTissue(Research lab, CropType cropType) {
		String cropName = cropType.getName();
		String tissueName = cropName + TISSUE_CULTURE;
		// TODO: re-tune the amount of tissue culture not just based on the edible
		// biomass (actualHarvest)
		// but also the inedible biomass and the crop category
		boolean isDone = false;
		AmountResource cropAR = ResourceUtil.findAmountResource(cropName);
		AmountResource tissueAR = ResourceUtil.findAmountResource(tissueName);
		double amountAvailable = inv.getAmountResourceStored(tissueAR, false);
		double amountExtracted = 0;

		// Add the chosen tissue culture entry to the lab if it hasn't done it today.
		boolean justAdded = lab.addTissueCulture(tissueName);
		if (justAdded) {
			lab.markChecked(tissueName);

			if (amountAvailable == 0) {
				// if no tissue culture is available, go extract some tissues from the crop
				double amount = inv.getAmountResourceStored(cropAR, false);
				// TODO : Check for the health condition
				amountExtracted = STANDARD_AMOUNT_TISSUE_CULTURE * RandomUtil.getRandomInt(5, 15);

				if (amount > amountExtracted) {
					// assume extracting an arbitrary 5 to 15% of the mass of crop will be developed
					// into tissue culture
					Storage.retrieveAnResource(amountExtracted, cropAR, inv, true);
					// store the tissues
					if (STANDARD_AMOUNT_TISSUE_CULTURE > 0) {
						Storage.storeAnResource(STANDARD_AMOUNT_TISSUE_CULTURE, tissueAR, inv,
								sourceName + "::growCropTissue");
						LogConsolidated.log(logger, Level.INFO, 1000, sourceName,
								"[" + settlement.getName() + "] During sampling, " + cropName + TISSUE_CULTURE
										+ " is not in stock. " + "Extract " + STANDARD_AMOUNT_TISSUE_CULTURE
										+ " kg from " + cropName + " and restock in " + lab.getBuilding().getNickName()
										+ ".",
								null);
						isDone = true;
					}
				}
			}
		}

		else {
			List<String> unchecked = lab.getUncheckedTissues();
			int size = unchecked.size();
			if (size > 0) {
				int rand = RandomUtil.getRandomInt(size - 1);
				String s = unchecked.get(rand);
				// mark this tissue culture. At max of 3 marks for each culture per sol
				lab.markChecked(s);

				// if there is less than 1 kg of tissue culture
				if (amountAvailable > 0 && amountAvailable < 1) {
					// increase the amount of tissue culture by 20%
					amountExtracted = amountAvailable * 0.2;
					// store the tissues
					if (amountExtracted > 0) {
						Storage.storeAnResource(amountExtracted, tissueAR, inv, sourceName + "::growCropTissue");
						// LogConsolidated.log(logger, Level.INFO, 1000, sourceName,
						// "[" + settlement.getName() + "] During sampling, " +
						// Math.round(amountExtracted*1000.0)/1000.0D + " kg "
						// + cropName + TISSUE_CULTURE + " is cloned and restocked in "
						// + lab.getBuilding().getNickName() + ".", null);

						isDone = true;
					}
				}

			} else
				return false;
		}

		return isDone;
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

	/**
	 * Gets the farm's current crops.
	 * 
	 * @return collection of crops
	 */
	public List<Crop> getCrops() {
		return crops;
	}

	/**
	 * Gets the names of the current crops.
	 * 
	 * @return list of crop names
	 */
	public List<String> getPlantedCrops() {
		return plantedCrops;
	}

	public Map<String, Integer> getCleaningMap() {
		return cleaningMap;
	}

	public Map<String, Integer> getInspectionMap() {
		return inspectionMap;
	}

	public List<String> getInspectionList() {
		return inspectionList;
	}

	public List<String> getCleaningList() {
		return cleaningList;
	}

	public List<String> getUninspected() {
		List<String> uninspected = new ArrayList<>();
		for (String s : inspectionMap.keySet()) {
			if (inspectionMap.get(s) < NUM_INSPECTIONS)
				uninspected.add(s);
		}
		return uninspected;
	}

	public List<String> getUncleaned() {
		List<String> uncleaned = new ArrayList<>();
		for (String s : cleaningMap.keySet()) {
			if (cleaningMap.get(s) < NUM_CLEANING)
				uncleaned.add(s);
		}
		return uncleaned;
	}

	public void markInspected(String s) {
		inspectionMap.put(s, inspectionMap.get(s) + 1); // .getOrDefault(s, 0)
	}

	public void markCleaned(String s) {
		cleaningMap.put(s, cleaningMap.get(s) + 1);
	}

	/**
	 * Adds air moisture to this farm
	 * 
	 * @param value the amount of air moisture in kg
	 */
	public void addMoisture(double value) {
		moisture += value;
	}

	/**
	 * Adds O2 to this farm
	 * 
	 * @param value the amount of O2 cached in kg
	 */
	public void addO2Cache(double value) {
		o2 += value;
	}

	/**
	 * Adds CO2 to this farm
	 * 
	 * @param value the amount of CO2 cached in kg
	 */
	public void addCO2Cache(double value) {
		cO2 += value;
	}

	public double getMoisture() {
		return moisture;
	}

	/**
	 * Retrieves the air moisture from this farm
	 * 
	 * @return the amount of air moisture in kg retrieved
	 */
	public double retrieveMoisture(double amount) {
		// double m = moisture;
		// Do NOT directly set to zero since a crop may be accessing the method
		// addMoisture() and change the
		// value of moisture at the same time.
		moisture = moisture - amount;
		// Note : The amount of moisture will be monitored by the CompositionOfAir
		return amount;
	}

	public double getO2() {
		return o2;
	}

	/**
	 * Retrieves the O2 generated from this farm
	 * 
	 * @return the amount of O2 in kg retrieved
	 */
	public double retrieveO2(double amount) {
		// double gas = o2;
		o2 = o2 - amount;
		// Note : The amount of o2 will be monitored by the CompositionOfAir
		return amount;
	}

	public double getCO2() {
		return cO2;
	}

	/**
	 * Retrieves the CO2 consumed from this farm
	 * 
	 * @return the amount of CO2 in kg retrieved
	 */
	public double retrieveCO2(double amount) {
		// double gas = co2;
		cO2 = cO2 - amount;
		// Note : The amount of co2 will be monitored by the CompositionOfAir
		return amount;
	}

	/**
	 * Records the average water usage on a particular crop
	 * 
	 * @param cropName
	 * @param usage    average water consumption in kg/m^2/sol
	 */
	public void addWaterUsage(String cropName, double usage) {
		if (cropDailyWaterUsage.containsKey(cropName)) {
			List<Double> cropWaterData = cropDailyWaterUsage.get(cropName);
			cropWaterData.add(usage);
		} else {
			List<Double> cropWaterData = new ArrayList<>();
			cropWaterData.add(usage);
			cropDailyWaterUsage.put(cropName, cropWaterData);
		}
	}

	/**
	 * Records the average O2 generation on a particular crop
	 * 
	 * @param cropName
	 * @param gen      average O2 generated in kg/m^2/sol
	 */
	public void addO2Generated(String cropName, double gen) {
		if (cropDailyO2Generated.containsKey(cropName)) {
			List<Double> o2Data = cropDailyO2Generated.get(cropName);
			o2Data.add(gen);
		} else {
			List<Double> o2Data = new ArrayList<>();
			o2Data.add(gen);
			cropDailyO2Generated.put(cropName, o2Data);
		}
	}

	/**
	 * Records the average CO2 consumption on a particular crop
	 * 
	 * @param cropName
	 * @param used     average CO2 consumed in kg/m^2/sol
	 */
	public void addCO2Consumed(String cropName, double used) {
		if (cropDailyCO2Consumed.containsKey(cropName)) {
			List<Double> co2Data = cropDailyCO2Consumed.get(cropName);
			co2Data.add(used);
		} else {
			List<Double> co2Data = new ArrayList<>();
			co2Data.add(used);
			cropDailyCO2Consumed.put(cropName, co2Data);
		}
	}

	/**
	 * Computes the average water usage on a particular crop
	 * 
	 * @return average water consumption in kg/m^2/sol
	 */
	public double computeCropWaterUsage(String cropName) {
		if (cropDailyWaterUsage.containsKey(cropName)) {
			double sum = 0;
			List<Double> cropWaterData = cropDailyWaterUsage.get(cropName);
			int size = cropWaterData.size();
			for (double i : cropWaterData) {
				sum += i;
			}
			if (size == 0)
				return 0;
			else
				return Math.round(sum / size * 1000.0) / 1000.0;
		}
		return 0;
	}

	/**
	 * Computes the average water usage on all crop
	 * 
	 * @return average water consumption in kg/m^2/sol
	 */
	public double computeWaterUsage() {
		double sum = 0;
		int size = 0;
		for (CropType ct : cropTypeList) {
			String n = ct.getName();
			Double ave = computeCropWaterUsage(n);
			if (ave > 0)
				size++;
			sum += ave;
		}
		if (size == 0)
			return 0;
		else
			return Math.round(sum / size * 1000.0) / 1000.0;
	}

	/**
	 * Computes the average O2 generated on a particular crop
	 * 
	 * @return average O2 generated in kg/m^2/sol
	 */
	public double computeCropO2Generated(String cropName) {
		if (cropDailyO2Generated.containsKey(cropName)) {
			double sum = 0;
			List<Double> o2Data = cropDailyO2Generated.get(cropName);
			int size = o2Data.size();
			for (double i : o2Data) {
				sum += i;
			}
			if (size == 0)
				return 0;
			else
				return sum / size;// Math.round(sum/size*1000.0)/1000.0;
		}
		return 0;
	}

	/**
	 * Computes the average O2 generated on all crop
	 * 
	 * @return average O2 generated in kg/m^2/sol
	 */
	public double computeTotalO2Generated() {
		double sum = 0;
		int size = 0;
		for (CropType ct : cropTypeList) {
			String n = ct.getName();
			Double ave = computeCropO2Generated(n);
			if (ave > 0)
				size++;
			sum += ave;
		}
		if (size == 0)
			return 0;
		else
			return Math.round(sum / size * 1000.0) / 1000.0;
	}

	/**
	 * Computes the average O2 generated on a particular crop
	 * 
	 * @return average O2 generated in kg/m^2/sol
	 */
	public double computeCropCO2Consumed(String cropName) {
		if (cropDailyCO2Consumed.containsKey(cropName)) {
			double sum = 0;
			List<Double> o2Data = cropDailyCO2Consumed.get(cropName);
			int size = o2Data.size();
			for (double i : o2Data) {
				sum += i;
			}
			if (size == 0)
				return 0;
			else
				return sum / size;// Math.round(sum/size*1000.0)/1000.0;
		}
		return 0;
	}

	/**
	 * Computes the average O2 generated on all crop
	 * 
	 * @return average O2 generated in kg/m^2/sol
	 */
	public double computeTotalCO2Consumed() {
		double sum = 0;
		int size = 0;
		for (CropType ct : cropTypeList) {
			String n = ct.getName();
			Double ave = computeCropCO2Consumed(n);
			if (ave > 0)
				size++;
			sum += ave;
		}
		if (size == 0)
			return 0;
		else
			return Math.round(sum / size * 1000.0) / 1000.0;
	}

	@Override
	public void destroy() {
		super.destroy();

		plantedCrops = null;

		cleaningMap = null;
		inspectionMap = null;
		inspectionList = null;
		cleaningList = null;

		cropDailyWaterUsage = null;
		cropDailyO2Generated = null;
		cropDailyCO2Consumed = null;

		marsClock = null;
		inv = null;
		lab = null;
		settlement = null;
		building = null;

		Iterator<CropType> i = cropListInQueue.iterator();
		while (i.hasNext()) {
			i.next().destroy();
		}

		cropListInQueue = null;

		Iterator<Crop> ii = crops.iterator();
		while (ii.hasNext()) {
			ii.next().destroy();
		}

		crops = null;

		Iterator<CropType> iii = cropTypeList.iterator();
		while (iii.hasNext()) {
			iii.next().destroy();
		}

		cropTypeList = null;

	}

}

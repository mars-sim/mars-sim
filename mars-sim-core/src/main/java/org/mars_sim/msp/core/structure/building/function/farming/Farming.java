/**
* Mars Simulation Project
 * Farming.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.data.SolMetricDataLogger;
import org.mars_sim.msp.core.data.SolSingleMetricDataLogger;
import org.mars_sim.msp.core.foodProduction.FoodType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.TendGreenhouse;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.HouseKeeping;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Farming class is a building function for greenhouse farming.
 */

public class Farming extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Farming.class.getName());
	
	private static final String [] INSPECTION_LIST = {"Environmental Control System",
													  "HVAC System", "Waste Disposal System",
													  "Containment System", "Any Traces of Contamination",
													  "Foundation",	"Structural Element", "Thermal Budget",
													  "Water and Irrigation System"};
	private static final String [] CLEANING_LIST = {"Floor", "Curtains", "Canopy", "Equipment",
													"Pipings", "Trays", "Valves"};
	
	private static final int MAX_NUM_SOLS = 14;

	public static final String MUSHROOM = "mushroom";
	public static final String FERTILIZER = "fertilizer";
	public static final String SOIL = "soil";
	public static final String CROP_WASTE = "crop waste";
	public static final String TISSUE_CULTURE = " " + FoodType.TISSUE.getName();
	public static final String CORN = "corn";
//	 public static final String LED_KIT = "light emitting diode kit";
//	 public static final String HPS_LAMP = "high pressure sodium lamp";
	
	/** The amount of crop tissue culture needed for each square meter of growing area. */
	public static final double TISSUE_PER_SQM = .0005; // 1/2 gram (arbitrary)
	public static final double STANDARD_AMOUNT_TISSUE_CULTURE = 0.05;
	public static final double CO2_RATE = 400;
	public static final double O2_RATE = .75;
	public static final double MIN  = .00001D;// 0.0000000001;

	
//	 private static ItemResource LED_Item;
//	 private static ItemResource HPS_Item;
//	 private int numLEDInUse;
//	 private int cacheNumLED;
//	/** The number of crop types available. */
//	private static int cropTypeNum;
	/** The number of High Power Sodium Lamp needed. */
	private int numHPSinNeed;
	/** The default number of crops allowed by the building type. */
	private int defaultCropNum;
	/** The id of a crop in this greenhouse. */
	private int identifer;
	/** The number of crops to plant. */
	private int numCrops2Plant;
	
	private double powerGrowingCrop;
	private double powerSustainingCrop;
	private double maxGrowingArea;
	private double remainingGrowingArea;

	/** The amount of air moisture in the greenhouse */
	private double moisture = 0;
	/** The amount of O2 generated in the greenhouse */
	private double o2 = 0;
	/** The amount of CO2 consumed in the greenhouse */
	private double cO2 = 0;

	/** List of crop types in queue */
	private List<String> cropListInQueue;
//	/** List of crop types the greenhouse is currently growing */
//	private List<Integer> plantedCrops;
	/** List of crops the greenhouse is currently growing */
	private List<Crop> crops;
	/** A map of all the crops ever planted in this greenhouse. */
	private Map<Integer, String> cropHistory;

	/** The crop usage on each crop in this facility [kg/sol]. */
	private Map<String,SolMetricDataLogger<Integer>> cropUsage;
	/** The daily water usage in this facility [kg/sol]. */
	private SolSingleMetricDataLogger dailyWaterUsage;
    
	private Research lab;
	private HouseKeeping houseKeeping;


	/**
	 * Constructor.
	 * 
	 * @param building the building the function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public Farming(Building building) {
		// Use Function constructor.
		super(FunctionType.FARMING, building);

		// LED_Item = ItemResource.findItemResource(LED_KIT);
		// HPS_Item = ItemResource.findItemResource(HPS_LAMP);

		identifer = 0;
		
		houseKeeping = new HouseKeeping(CLEANING_LIST, INSPECTION_LIST);
		
		cropListInQueue = new CopyOnWriteArrayList<>();
		crops = new CopyOnWriteArrayList<>();
		cropHistory = new ConcurrentHashMap<>();
		dailyWaterUsage = new SolSingleMetricDataLogger(MAX_NUM_SOLS);
		cropUsage = new HashMap<>();

		defaultCropNum = buildingConfig.getCropNum(building.getBuildingType());	

		powerGrowingCrop = buildingConfig.getPowerForGrowingCrop(building.getBuildingType());
		powerSustainingCrop = buildingConfig.getPowerForSustainingCrop(building.getBuildingType());
		maxGrowingArea = buildingConfig.getCropGrowingArea(building.getBuildingType());
		remainingGrowingArea = maxGrowingArea;

		for (int x = 0; x < defaultCropNum; x++) {
			CropType cropType = pickACrop(true, false);
			if (cropType == null) {
				break;// for avoiding NullPointerException during maven test
			}
			else {
				Crop crop = plantACrop(cropType, true, 0);
				crops.add(crop);
				cropHistory.put(crop.getIdentifier(), cropType.getName());//crop.getCropName());
				building.getSettlement().fireUnitUpdate(UnitEventType.CROP_EVENT, crop);
			}
		}
	}

	/**
	 * Obtains the next identifier and increment the counter
	 * 
	 * @return
	 */
	private int getNextIdentifier() {
		return identifer++;
	}


	/**
	 * Picks a crop type
	 * 
	 * @param isStartup - true if it is called at the start of the sim
	 * @return {@link CropType}
	 */
	public CropType pickACrop(boolean isStartup, boolean noCorn) {
		// TODO: need to specify the person who is doing it using the work time in the
		// lab
		CropType ct = null;
		boolean flag = true;
		// TODO: at the start of the sim, choose only from a list of staple food crop
		CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
		if (isStartup) {
			while (flag) {
				ct = cropConfig.getRandomCropType();
				if (noCorn && ct.getName().equalsIgnoreCase(CORN)) {
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
				if (noCorn && ct.getName().equalsIgnoreCase(CORN)) {
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
		CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();

		for (CropType c : cropConfig.getCropTypes()) {
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

		int size = cropHistory.size();
		if (size > 1) {
			// get the last two planted crops
			last2CT = cropHistory.get(size - 2);
			lastCT = cropHistory.get(size - 1);
//			System.out.println("0 : " + cropHistory.get(0));
//			System.out.println("1 : " + cropHistory.get(1));
//			System.out.println("last2CT : " + last2CT);
//			System.out.println("lastCT : " + lastCT);
	
			if (no_1_crop.getName().equalsIgnoreCase(last2CT) || no_1_crop.getName().equalsIgnoreCase(lastCT)) {
				// if highestCrop has already been selected once

				if (last2CT != null && lastCT != null && last2CT.equals(lastCT)) {
					// Note : since the highestCrop has already been chosen previously,
					// should not choose the same crop type again
					// compareVP = false;
					chosen = no_2_crop;
				}

				else
					compareVP = true;
			}

			else if (no_2_crop.getName().equalsIgnoreCase(last2CT) || no_2_crop.getName().equalsIgnoreCase(lastCT)) {
				// if secondCrop has already been selected once

				if (last2CT != null && lastCT != null && last2CT.equals(lastCT)) {
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
			lastCT = cropHistory.get(0);

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
			chosen = cropConfig.getRandomCropType();
			flag = containCrop(chosen.getName());
		}

		// if it's a mushroom, add increases the item demand of the mushroom containment
		// kit before the crop is planted
		if (chosen.getName().toLowerCase().contains(MUSHROOM))
			building.getInventory().addItemDemand(ItemResourceUtil.mushroomBoxID, 1);

		return chosen;
	}

	public boolean containCrop(String name) {
		for (Crop c : crops) {
			if (c.getCropName().equalsIgnoreCase(name))
				return true;
		}

		return false;
	}

	public double getCropValue(AmountResource resource) {
		return building.getSettlement().getGoodsManager().getAmountDemandValue(resource.getID());
//				GoodsUtil.getResourceGood(ResourceUtil.findIDbyAmountResourceName(resource.getName())));
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
			cropArea = maxGrowingArea / (double) defaultCropNum;
		}

		// Sets aside some areas
		remainingGrowingArea = remainingGrowingArea - cropArea;

		if (remainingGrowingArea < 0)
			remainingGrowingArea = 0;

		// Note edible-biomass is [ gram / m^2 / day ]
		double dailyMaxHarvest = edibleBiomass / 1000D * cropArea;
//		 logger.info("max possible daily harvest on " + cropType.getName() + " : " +
//		 Math.round(dailyMaxHarvest*100.0)/100.0 + " kg per sol");

		double percentAvailable = 0;

		if (!isStartup) {
			// Use tissue culture
			percentAvailable = useTissueCulture(cropType, cropArea);
			// Add fertilizer to the soil for the new crop
			provideFertilizer(cropArea);
			// Replace some amount of old soil with new soil
			provideNewSoil(cropArea);

		}

		Crop crop = new Crop(getNextIdentifier(), cropType, cropArea, dailyMaxHarvest, 
				this, building.getSettlement(), isStartup, percentAvailable);
		
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
		store(amount, ResourceUtil.cropWasteID, "Farming::provideNewSoil");

		// TODO: adjust how much new soil is needed to replenish the soil bed
		if (amount > MIN)
			retrieve(amount, ResourceUtil.soilID, true);

	}

	/**
	 * Retrieves the fertilizer and add to the soil when planting the crop
	 */

	public void provideFertilizer(double cropArea) {
		double rand = RandomUtil.getRandomDouble(2);
		double amount = Crop.FERTILIZER_NEEDED_IN_SOIL_PER_SQM * cropArea / 10D * rand;
		if (amount > MIN)
			retrieve(amount, ResourceUtil.fertilizerID, true);
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
		int tissueID = ResourceUtil.findIDbyAmountResourceName(tissueName);

		boolean available = false;

		try {

			Inventory inv = building.getInventory();
			
			double amountStored = inv.getAmountResourceStored(tissueID, false);
			inv.addAmountDemandTotalRequest(tissueID, amountStored);

			if (amountStored < MIN) {
				logger.log(building, Level.INFO, 1000, "Running out of " + tissueName + ".");
				percent = 0;
			}

			else if (amountStored < requestedAmount) {
				available = true;
				percent = amountStored / requestedAmount * 100D;
				requestedAmount = amountStored;
				logger.log(building, Level.INFO, 1000, Math.round(requestedAmount * 100.0) / 100.0
							+ " kg " + tissueName + " was partially available.");
			}

			else {
				available = true;
				percent = 100D;
				logger.log(building, Level.INFO, 1000,
						+ Math.round(requestedAmount * 100.0) / 100.0 + " kg " 
				+ tissueName + " was fully available.");
			}

			if (available) {
				inv.retrieveAmountResource(tissueID, requestedAmount);
				inv.addAmountDemand(tissueID, requestedAmount);
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
		}

		return percent;

	}

	/**
	 * Gets a collection of the CropType.
	 * 
	 * @return Collection of CropType
	 */
	public List<String> getCropListInQueue() {
		return cropListInQueue;
	}

	/**
	 * Adds a cropType to the crop queue.
	 * 
	 * @param cropType
	 */
	public void addCropListInQueue(String n) {
		if (n != null) {
			cropListInQueue.add(n);
		}
	}

	/**
	 * Deletes a cropType to cropListInQueue.
	 * 
	 * @param cropType
	 */
	public void deleteACropFromQueue(int index, String n) {
		// cropListInQueue.remove(index);
		// Safer removal than cropListInQueue.remove(index)
		int size = cropListInQueue.size();
		if (size > 0) {
			Iterator<String> j = cropListInQueue.iterator();
			int i = 0;
			while (j.hasNext()) {
				String name = j.next();
				if (i == index) {
					if (!n.equals(name))
						logger.severe(building,
								"The crop queue encountered a problem removing a crop");
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
		double requiredFarmingAreaPerPerson = SimulationConfig.instance().getCropConfiguration().getFarmingAreaNeededPerPerson();
		double demand = requiredFarmingAreaPerPerson * settlement.getNumCitizens();

		// Supply is total farming area (m^2) of all farming buildings at settlement.
		double supply = 0D;
		boolean removedBuilding = false;
		List<Building> buildings = settlement.getBuildingManager().getBuildings(FunctionType.FARMING);
		for (Building building : buildings) {
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				Farming farmingFunction = building.getFarming();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += farmingFunction.getGrowingArea() * wearModifier;
			}
		}

		// Modify result by value (VP) of food at the settlement.
//		Good foodGood = GoodsUtil.getResourceGood(ResourceUtil.foodID);
		double foodValue = settlement.getGoodsManager().getGoodValuePerItem(ResourceUtil.foodID);

		result = (demand / (supply + 1D)) * foodValue;

		// TODO: investigating if other food group besides food should be added as well

		return result;
	}

	/**
	 * Checks if farm currently requires work.
	 * 
	 * @return true if farm requires work
	 */
	public boolean requiresWork() {
		for (Crop c : crops) {
			if (c.requiresWork()) {
				return true;
			}
		}
		return getNumCrops2Plant() > 0;
	}

	/**
	 * Adds work time to the crops current phase.
	 * 
	 * @param workTime - Work time to be added (millisols)
	 * @param h        - an instance of TendGreenhouse
	 * @param worker     - a person or bot
	 * @return workTime remaining after working on crop (millisols)
	 * @throws Exception if error adding work.
	 */
	public double addWork(double workTime, TendGreenhouse h, Worker worker) {
		double t = workTime;
		Crop needyCropCache = null;
		Crop needyCrop = getNeedyCrop(needyCropCache);
		// TODO: redesign addWork() to check on each food crop
		while (needyCrop != null && t > MIN) {
			// WARNING : ensure timeRemaining gets smaller
			// or else creating an infinite loop
			t = needyCrop.addWork(worker, t) * .9999;
			
			needyCropCache = needyCrop;
			// Get a new needy crop
			needyCrop = getNeedyCrop(needyCropCache);

			if (needyCrop != null && !needyCropCache.equals(needyCrop)) {
				// Update the name of the crop being worked on in the task
				// description
				h.setCropDescription(needyCrop);
			}
		}

		return t;
	}

	/**
	 * Gets a crop that needs planting, tending, or harvesting.
	 * 
	 * @param lastCrop
	 * @param unit     a person or a bot
	 * @return crop or null if none found.
	 */
	public Crop getNeedyCrop(Crop lastCrop) {
		if (crops == null || crops.isEmpty())
			return null;
		
		Crop result = null;

		List<Crop> needyCrops = new CopyOnWriteArrayList<>();
		for (Crop c : crops) {
			if (c.requiresWork()) {
				if (lastCrop != null) {
					if (c.getCropTypeID() == lastCrop.getCropTypeID())
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
		else if (size == 2) {
			result = needyCrops.get(RandomUtil.getRandomInt(1));
			// updateCropAssignment(unit, result);
		}
		else if (size > 2) {
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
				LifeSupport lifeSupport = building.getLifeSupport();
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
	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {	
			// check for the passing of each day
			if (pulse.isNewSol()) {
			    
				houseKeeping.resetCleaning();
				
				// Inspect every 2 days
				if ((pulse.getMarsTime().getMissionSol() % 2) == 0)
				{
					houseKeeping.resetInspected();
				}

				// Reset cumulativeDailyPAR
				for (Crop c : crops)
					c.resetPAR();
				// TODO: will need to limit the size of the other usage maps
			}
	
			// Determine the production level.
			double productionLevel = 0D;
			if (building.getPowerMode() == PowerMode.FULL_POWER)
				productionLevel = 1D;
			else if (building.getPowerMode() == PowerMode.POWER_DOWN)
				productionLevel = .5D;
	
			// Call timePassing on each crop.
			Iterator<Crop> i = crops.iterator();
	//		List<Crop> harvestedCrops = null;
	
			while (i.hasNext()) {
				Crop crop = i.next();
				
				try {
					crop.timePassing(pulse, productionLevel);
				
				} catch (Exception e) {
					logger.log(null, building, Level.WARNING, 1000, crop.getCropName() + " ran into issues ", e);
					e.printStackTrace();
				}
				
				// Remove old crops.
				if (crop.getPhaseType() == PhaseType.FINISHED) {
					// Take back the growing area
					remainingGrowingArea = remainingGrowingArea + crop.getGrowingArea();
					crops.remove(crop);
					numCrops2Plant++;
				}
			}
		}
		return valid;
	}

	public void transferSeedling(double time, Worker worker) {
			
		// Add any new crops.
		for (int x = 0; x < numCrops2Plant; x++) {
			String n = null;
			int size = cropListInQueue.size();
			if (size > 0) {
				n = cropListInQueue.get(0);
				cropListInQueue.remove(0);
			}

			else {
				CropType ct = selectVPCrop();
				n = ct.getName();
			}

			CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
			if (n != null && !n.equals("")) {
				Crop crop = plantACrop(cropConfig.getCropTypeByName(n), false, 0);
				crops.add(crop);
				cropHistory.put(crop.getIdentifier(), n);
				building.fireUnitUpdate(UnitEventType.CROP_EVENT, crop);
				
				logger.log(building, worker, Level.INFO, 3_000, "Planted a new crop of " + n + ".");
				
				numCrops2Plant--;
				break;
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
	@Override
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

		double aveGrowingTime = SimulationConfig.instance().getCropConfiguration().getAverageCropGrowingTime();
		double solsInOrbit = MarsClock.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR;
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
	 * @param cropTypeID
	 * @return true if work has been done
	 */
	public boolean checkBotanyLab(int cropTypeID, Worker worker) {
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
				boolean workDone = growCropTissue(lab, cropTypeID, worker);// , true);
				lab.removeResearcher();
				return workDone;
			}
		}

		else {
			// Check available research slot in another lab located in another greenhouse
			List<Building> laboratoryBuildings = building.getSettlement().getBuildingManager().getBuildings(FunctionType.RESEARCH);
			Iterator<Building> i = laboratoryBuildings.iterator();
			while (i.hasNext() && !hasEmptySpace) {
				Building building = i.next();
				Research lab1 = building.getResearch();
				if (lab1.hasSpecialty(ScienceType.BOTANY)) {
					hasEmptySpace = lab1.checkAvailability();
					if (hasEmptySpace) {
						hasEmptySpace = lab1.addResearcher();
						if (hasEmptySpace) {
							boolean workDone = growCropTissue(lab1, cropTypeID, worker);// true);
							lab.removeResearcher();
							return workDone;
						}

						// TODO: compute research ooints to determine if it can be carried out.
						// int points += (double) (lab.getResearcherNum() * lab.getTechnologyLevel()) /
						// 2D;
					}
				}
			}
		}

		// Check to see if a person can still "squeeze into" this busy lab to get lab
		// time
		if (!hasEmptySpace && (lab.getLaboratorySize() == lab.getResearcherNum())) {
			return growCropTissue(lab, cropTypeID, worker);// , false);
		} 
		
		else {

			// Check available research slot in another lab located in another greenhouse
			List<Building> laboratoryBuildings = building.getSettlement().getBuildingManager().getBuildings(FunctionType.RESEARCH);
			Iterator<Building> i = laboratoryBuildings.iterator();
			while (i.hasNext() && !hasEmptySpace) {
				Building building = i.next();
				Research lab2 = building.getResearch();
				if (lab2.hasSpecialty(ScienceType.BOTANY)) {
					hasEmptySpace = lab2.checkAvailability();
					if (lab2.getLaboratorySize() == lab2.getResearcherNum()) {
						boolean workDone = growCropTissue(lab2, cropTypeID, worker);// , false);
						return workDone;
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
	private boolean growCropTissue(Research lab, int cropTypeID, Worker worker) {
		String cropName = SimulationConfig.instance().getCropConfiguration().getCropTypeNameByID(cropTypeID);
		String tissueName = cropName + TISSUE_CULTURE;
		// TODO: re-tune the amount of tissue culture not just based on the edible
		// biomass (actualHarvest)
		// but also the inedible biomass and the crop category
		boolean isDone = false;
		int cropID = ResourceUtil.findIDbyAmountResourceName(cropName);
		int tissueID = ResourceUtil.findIDbyAmountResourceName(tissueName);
		double amountAvailable = building.getInventory().getAmountResourceStored(tissueID, false);
		double amountExtracted = 0;

		// Add the chosen tissue culture entry to the lab if it hasn't done it today.
		boolean hasIt = lab.hasTissueCulture(tissueName);
		if (!hasIt) {
			lab.markChecked(tissueName);
			// TODO: ask trader to barter the tissue culture from other settlements
			if (amountAvailable == 0) {
				// if no tissue culture is available, go extract some tissues from the crop
				double amount = building.getInventory().getAmountResourceStored(cropID, false);
				// TODO : Check for the health condition
				amountExtracted = STANDARD_AMOUNT_TISSUE_CULTURE;// * RandomUtil.getRandomInt(5, 15);

				if (amount > amountExtracted) {
					// assume extracting an arbitrary 5 to 15% of the mass of crop will be developed
					// into tissue culture
					boolean canExtract = retrieve(amountExtracted, cropID, true);
					// store the tissues
					if (canExtract && amountExtracted > 0) {
						store(amountExtracted, tissueID, "Farming::growCropTissue");
						logger.log(building, worker, Level.FINE, 3_000,
								"Found no " + Conversion.capitalize(cropName) + TISSUE_CULTURE
								+ " in stock. Extracted " + amountExtracted
								+ " kg from its crop in Botany lab.");
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
						store(amountExtracted, tissueID, "Farming::growCropTissue");
						logger.log(building, worker, Level.FINE, 3_000,  "Cloned "
							+ Math.round(amountExtracted*1000.0)/1000.0D + " kg "
							+ cropName + TISSUE_CULTURE 
							+ " in Botany lab.");

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

	/**
	 * Gets the farm's current crops.
	 * 
	 * @return collection of crops
	 */
	public List<Crop> getCrops() {
		return crops;
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
	 * @param usage    average water consumption in kg/sol
	 */
	public void addCropUsage(String cropName, double usage, int sol, int type) {
		SolMetricDataLogger<Integer> crop = cropUsage.get(cropName);
		if (crop == null) {
			crop = new SolMetricDataLogger<>(MAX_NUM_SOLS);
			cropUsage.put(cropName, crop);
		}
		
		crop.increaseDataPoint(type, usage);
	}

	/**
	 * Computes the average water usage on a particular crop
	 * 
	 * @return average water consumption in kg/sol
	 */
	public double computeUsage(int type, String cropName) {
		double result = 0;
		SolMetricDataLogger<Integer> crop = cropUsage.get(cropName);
		if (crop != null) {
			result = crop.getDailyAverage(type);
		}
		return result;
	}
	
	/**
	 * Computes the water usage on all crops
	 * 
	 * @return water consumption in kg/sol
	 */
	public double computeUsage(int type) {
		// Note: ithe value is kg per square meter per sol
		double sum = 0;
		for (Crop c : crops) {
			sum += computeUsage(type, c.getCropName());
		}
		
		return Math.round(sum * 1000.0) / 1000.0;
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
	 * Adds to the daily water usage
	 * 
	 * @param waterUssed
	 * @param solElapsed
	 */
	public void addDailyWaterUsage(double waterUssed) {
		dailyWaterUsage.increaseDataPoint(waterUssed);
	}

	public int getNumCrops2Plant() {
		return numCrops2Plant;
	}

	
	@Override
	public void destroy() {
		super.destroy();

		lab = null;

//		Iterator<CropType> i = cropListInQueue.iterator();
//		while (i.hasNext()) {
//			i.next().destroy();
//		}

		cropListInQueue = null;

		Iterator<Crop> ii = crops.iterator();
		while (ii.hasNext()) {
			ii.next().destroy();
		}

		crops = null;

	}

}

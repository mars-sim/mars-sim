/*
 * Mars Simulation Project
 * Farming.java
 * @date 2025-07-16
 * @author Scott Davis
 */
package com.mars_sim.core.building.function.farming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.FunctionSpec;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.HouseKeeping;
import com.mars_sim.core.building.function.LifeSupport;
import com.mars_sim.core.building.function.Research;
import com.mars_sim.core.building.function.farming.task.TendGreenhouse;
import com.mars_sim.core.building.utility.power.PowerMode;
import com.mars_sim.core.data.SolMetricDataLogger;
import com.mars_sim.core.food.FoodType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The Farming class is a building function for greenhouse farming.
 */

public class Farming extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Farming.class.getName());

	private static final int MAX_NUM_SOLS = 14;
	private static final int MAX_SAME_CROPTYPE = 3;
	
	public static final double LOW_AMOUNT_TISSUE_CULTURE = 0.5;
	public static final double CROP_AMOUNT_FOR_TISSUE_EXTRACTION = 0.5;
	/** The average temperature tolerance of a crop [in C]. */
	private static final double T_TOLERANCE = 3D;
	/** The amount of crop tissue culture needed for each square meter of growing area. */
	private static final double TISSUE_PER_SQM = .0005; // 1/2 gram (arbitrary)
	public static final double STANDARD_AMOUNT_TISSUE_CULTURE = 0.05;
	private static final double MIN  = .00001D;
	
	private static final String CROPS = "crops";
	private static final String POWER_GROWING_CROP = "power-growing-crop";
	private static final String POWER_SUSTAINING_CROP = "power-sustaining-crop";
	private static final String GROWING_AREA = "growing-area";

	public static final String TISSUE = " " + FoodType.TISSUE.getName();

	private static final String [] INSPECTION_LIST = {"Environmental Control",
													  "Waste Disposal",
													  "Containment System", 
													  "Contamination Control",
													  "Foundation",	
													  "Irrigation"};
	private static final String [] CLEANING_LIST = {"Floor", "Curtains", 
													"Canopy", "Pipings", 
													"Trays", "Valves"};


	/** The default number of crops allowed by the building type. */
	private int defaultCropNum;
	/** The id of a crop in this greenhouse. */
	private int identifer;
	/** The number of crops to plant. */
	private int numCrops2Plant;

	private double powerGrowingCrop;
	
	private double powerSustainingCrop;
	/** The total growing area for all crops. */		
	private double maxGrowingArea;
	/** The total remaining growing area for all crops. */	
	private double remainingArea;
	/** The designated growing area for each crop. */
	private int designatedCropArea;
	
	/** The cumulative time spent in this greenhouse. */
	private double cumulativeWorkTime;	
	
	public enum Aspect {
	    ATTRACTIVENESS,
	    CLEANINESS,
	    MANAGEMENT,
	    SCIENCE,
	    UTILIZATION
	  }
	
	/** List of crop types in queue */
	private List<String> cropListInQueue;
	/** List of crops the greenhouse is currently growing */
	private List<Crop> cropList;
	/** A map of all the crops ever planted in this greenhouse. */
	private Map<Integer, String> cropHistory;
	/** The attribute scores map for this greenhouse. */
	private Map<Aspect, Double> attributes;
	/** The resource usage on each crop in this facility [kg/sol]. */
	private Map<String, SolMetricDataLogger<Integer>> cropUsage;
	
	/** The last crop the workers were tending. */
	private Crop needyCropCache;
	
	private Research lab;
	
	/** Keep track of cleaning and inspections. */
	private HouseKeeping houseKeeping;

	/**
	 * Constructor.
	 *
	 * @param building the building the function is for.
	 * @param spec Spec of the farming function
	 * @throws BuildingException if error in constructing function.
	 */
	public Farming(Building building, FunctionSpec spec) {
		// Use Function constructor.
		super(FunctionType.FARMING, spec, building);
		
		// Initialize the attribute scores map
		initAttributeScores();

		identifer = 0;

		houseKeeping = new HouseKeeping(CLEANING_LIST, INSPECTION_LIST);

		cropListInQueue = new ArrayList<>();
		cropList = new ArrayList<>();
		cropHistory = new HashMap<>();
		cropUsage = new HashMap<>();
		
		defaultCropNum = spec.getIntegerProperty(CROPS);

		powerGrowingCrop = spec.getDoubleProperty(POWER_GROWING_CROP);
		powerSustainingCrop = spec.getDoubleProperty(POWER_SUSTAINING_CROP);
		maxGrowingArea = spec.getDoubleProperty(GROWING_AREA);
		remainingArea = maxGrowingArea;
		
		designatedCropArea = (int)(maxGrowingArea / defaultCropNum);

		Map<CropSpec, Integer> alreadyPlanted = new HashMap<>();
		for (int x = 0; x < defaultCropNum; x++) {
			CropSpec cropSpec = pickACrop(alreadyPlanted);
			if (cropSpec == null) {
				break; // for avoiding NullPointerException during maven test
			}
			else {
				Crop crop = plantACrop(cropSpec, true, designatedCropArea);
				if (crop != null) {
					cropList.add(crop);
					cropHistory.put(crop.getIdentifier(), cropSpec.getName());
					building.getSettlement().fireUnitUpdate(UnitEventType.CROP_EVENT, crop);
					alreadyPlanted.merge(cropSpec, 1, Integer::sum);
				}
			}
		}
	}
	
	/**
	 * Initializes the attribute scores.
	 */
	private void initAttributeScores() {
		attributes = new EnumMap<>(Aspect.class); 
		attributes.put(Aspect.ATTRACTIVENESS, .5);
		attributes.put(Aspect.CLEANINESS, .5);
		attributes.put(Aspect.MANAGEMENT, .5);
		attributes.put(Aspect.SCIENCE, .5);
		attributes.put(Aspect.UTILIZATION, .5);
	}

	/**
	 * Updates an attribute score.
	 * 
	 * @param aspect
	 * @param amount
	 */
	public void updateAttribute(Aspect aspect, double amount) {
		attributes.computeIfPresent(aspect, (k, v) -> v = adjust(v, amount));
	}
	
	/**
	 * Adjusts an attribute score.
	 * 
	 * @param score
	 * @param change
	 * @return
	 */
	private double adjust(double score, double change) {
		double result = score + change;
		if (result > 1.0)
			return 1.0;
		else if (result < 0.0)
			return 0.0;
		return result;
	}
	
	/**
	 * Picks a crop. Ensure this crop is not being grown more than MAX_SAME_CROPTYPE.
	 *
	 * @param isStartup - true if it is called at the start of the sim
	 * @return {@link CropSpec}
	 */
	private CropSpec pickACrop(Map<CropSpec, Integer> cropsPlanted) {
		CropSpec ct = null;
		boolean cropAlreadyPlanted = true;

		int totalCropTypes = cropConfig.getCropTypes().size();

		// Attempt to find a unique crop but limit the number of attempts
		int attempts = 0;
		while ((attempts < totalCropTypes) && cropAlreadyPlanted) {
			ct = cropConfig.getRandomCropType();

			attempts++;
			cropAlreadyPlanted = cropsPlanted.getOrDefault(ct, 0) > MAX_SAME_CROPTYPE;
		}
		return ct;
	}

	/**
	 * Chooses a matured crop and extract samples for growing tissues.
	 * 
	 * @return
	 */
	public String chooseCrop2Extract(double amount) {
		List<CropSpec> list = new ArrayList<>(cropConfig.getCropTypes());
		Collections.shuffle(list);

		list = list.stream()
				.filter(c -> building.getSettlement()
				.getAllAmountResourceOwned(c.getCropID()) > amount)
				.collect(Collectors.toList());

		List<AmountResource> tissues = new ArrayList<>();
		
		for (CropSpec c: list) {
			String cropName = c.getName();
			String tissueName = cropName + Farming.TISSUE;
			AmountResource tissue = ResourceUtil.findAmountResource(tissueName);
			if (tissue != null) {	
				double amountTissue = building.getSettlement().getAmountResourceStored(tissue.getID());
				if (amountTissue < LOW_AMOUNT_TISSUE_CULTURE)
					tissues.add(tissue);
			}
		}
			
		String cropName = null;
		for (AmountResource ar: tissues) {
			if (cropName == null) {
				String tissueName = ar.getName();
				cropName = tissueName.replace(" tissue", "");
				int cropId = ResourceUtil.findIDbyAmountResourceName(cropName);
				double amountCrop = building.getSettlement().getAmountResourceStored(cropId);
				if (amountCrop > CROP_AMOUNT_FOR_TISSUE_EXTRACTION) {
					building.getSettlement().retrieveAmountResource(cropId, CROP_AMOUNT_FOR_TISSUE_EXTRACTION);
					break;
				}
				else
					cropName = null;
			}
		}
	
		if (cropName != null) {
			return cropName;
		}
		else {	
			String selectedTissueName = null;
			int selectedTissueid = 0;
			double selectedTissueAmount = 0;
			
			for (AmountResource ar: tissues) {
				double tissueAmount = building.getSettlement().getAmountResourceStored(ar.getID());
				if (tissueAmount <= selectedTissueAmount) {
					selectedTissueAmount = tissueAmount;
					selectedTissueName = ar.getName();
					selectedTissueid = ar.getID();
				}
			}
			
			if (selectedTissueName != null) {
				building.getSettlement().retrieveAmountResource(selectedTissueid, STANDARD_AMOUNT_TISSUE_CULTURE);
				return selectedTissueName.replace(" tissue", "");
			}
		}
		
		return null;
	}
	
	/**
	 * Selects a crop currently having the highest value point (VP).
	 *
	 * @return CropType
	 */
	public CropSpec selectVPCrop() {

		CropSpec no1Crop = null;
		CropSpec no2Crop = null;
		CropSpec chosen = null;
		double no1CropVP = 0;
		double no2CropVP = 0;

		for (CropSpec c : cropConfig.getCropTypes()) {
			double cropVP = getCropValue(ResourceUtil.findAmountResource(c.getCropID()));
			if (no1Crop == null) {
				// Push no1Crop to no2Crop
				no1CropVP = cropVP;
				no1Crop = c;
			}
			else if (cropVP >= no1CropVP) {
				// Push no1Crop to no2Crop
				no2CropVP = no1CropVP;
				no2Crop = no1Crop;
				// Make this new crop no1Crop
				no1CropVP = cropVP;
				no1Crop = c;
			}
			else if (cropVP > no2CropVP) {
				no2CropVP = cropVP;
				no2Crop = c;
			}
			else if (cropVP == no2CropVP) {
				int rand = RandomUtil.getRandomInt(1);
				if (rand == 1) {
					// 50% chance of replacing no2Crop with this new crop
					no2CropVP = cropVP;
					no2Crop = c;
				}
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

			if (no1Crop.getName().equalsIgnoreCase(last2CT) 
					|| no1Crop.getName().equalsIgnoreCase(lastCT)) {
				// if highestCrop has already been selected once

				if (last2CT != null && lastCT != null && last2CT.equals(lastCT)) {
					// Note : since the highestCrop has already been chosen previously,
					// should not choose the same crop type again
					chosen = no2Crop;
				}

				else
					compareVP = true;
			}

			else if (no2Crop != null && 
					(no2Crop.getName().equalsIgnoreCase(last2CT) 
							|| no2Crop.getName().equalsIgnoreCase(lastCT))) {
				// if secondCrop has already been selected once

				if (last2CT != null && lastCT != null && last2CT.equals(lastCT)) {
					// since the secondCrop has already been chosen twice,
					// should not choose the same crop type again
					chosen = no1Crop;
				}

				else
					compareVP = true;
			}

		}

		else if (size == 1) {
			lastCT = cropHistory.get(0);

			if (lastCT != null) {
				// if highestCrop has already been selected for planting last time,
				if (no1Crop.getName().equalsIgnoreCase(lastCT))
					compareVP = true;
			}
		}

		if (compareVP) {
			// compare secondVP with highestVP
			// if their VP are within 15%, toss a dice
			// if they are further apart, should pick highestCrop
			if ((no2CropVP / no1CropVP) > .85) {
				int rand = RandomUtil.getRandomInt(0, 1);
				if (rand == 0)
					chosen = no1Crop;
				else
					chosen = no2Crop;
			} else
				chosen = no2Crop;
		} else
			chosen = no1Crop;

		boolean flag = hasTooMany(chosen);

		while (flag) {
			chosen = cropConfig.getRandomCropType();
			flag = hasTooMany(chosen);
		}

		return chosen;
	}

	/**
	 * Checks if the greenhouse has too many crops of this type.
	 *
	 * @param name
	 * @return
	 */
	private boolean hasTooMany(CropSpec name) {
		int num = 0;
		for (Crop c : cropList) {
			if (c.getCropSpec().equals(name))
				num++;
		}

        return num > MAX_SAME_CROPTYPE;
    }

	private double getCropValue(AmountResource resource) {
		return building.getSettlement().getGoodsManager().getGoodValuePoint(resource.getID());
	}

	/**
	 * Plants a new crop.
	 *
	 * @param cropSpec		  The spec of the crop
	 * @param isStartup       True if it's at the start of the sim
	 * @param designatedArea  Will use this param in near future. FOr now it's set to zero
	 * @return Crop
	 */
	private Crop plantACrop(CropSpec cropSpec, boolean isStartup, double designatedArea) {
		double cropArea = 0;
		
		if (remainingArea == 0)
			return null;
		
		if (remainingArea <= 1) {
			cropArea = remainingArea;
		} 
		else if (designatedArea != 0) {
			// Note that remainingArea is > 1
			cropArea = Math.min(remainingArea, designatedArea);
		} 
		else {
			cropArea = Math.min(remainingArea, maxGrowingArea / defaultCropNum);
		}

		// Sets aside some areas
		remainingArea = remainingArea - cropArea;

		if (remainingArea < 0)
			remainingArea = 0;

		double percentAvailable = 0;

		if (!isStartup) {
			// Use tissue culture
			percentAvailable = useTissueCulture(cropSpec, cropArea);
			// Add fertilizer to the soil for the new crop
			provideFertilizer(cropArea);
			// Replace some amount of old soil with new soil
			provideNewSoil(cropArea);

		}

		return new Crop(identifer++, cropSpec, cropArea,
				this, isStartup, percentAvailable);
	}

	/**
	 * Retrieves new soil when planting new crop.
	 * 
	 * @param cropArea
	 */
	private void provideNewSoil(double cropArea) {
		double rand = RandomUtil.getRandomDouble(0.8, 1.2);

		double amount = Crop.NEW_SOIL_NEEDED_PER_SQM * cropArea * rand;

		if (amount > MIN) {
			// Collect some old crop and turn them into crop waste 
			store(amount, ResourceUtil.CROP_WASTE_ID, "Farming::provideNewSoil");
			// Note: adjust how much new soil is needed to replenish the soil bed
			retrieve(amount, ResourceUtil.SOIL_ID, true);
		}
	}

	/**
	 * Retrieves the fertilizer and add to the soil when planting the crop.
	 * 
	 * @param cropArea
	 */
	private void provideFertilizer(double cropArea) {
		double rand = RandomUtil.getRandomDouble(2);
		double amount = Crop.FERTILIZER_NEEDED_IN_SOIL_PER_SQM * cropArea * rand;
		if (amount > MIN)
			retrieve(amount, ResourceUtil.FERTILIZER_ID, true);
	}

	/**
	 * Uses available tissue culture to shorten Germinating Phase when planting the
	 * crop.
	 *
	 * @param cropType
	 * @param cropArea
	 * @return percentAvailable
	 */
	private double useTissueCulture(CropSpec cropType, double cropArea) {
		double percent = 0;

		double requestedAmount = cropArea * cropType.getEdibleBiomass() * TISSUE_PER_SQM;

		String tissueName = cropType.getName() + TISSUE;

		int tissueID = ResourceUtil.findIDbyAmountResourceName(tissueName);

		boolean available = false;

		try {
			double amountStored = building.getSettlement().getAmountResourceStored(tissueID);

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
				building.getSettlement().retrieveAmountResource(tissueID, requestedAmount);
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
	 * @param type the building type.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function. Called by BuildingManager.java
	 *         getBuildingValue()
	 */
	public static double getFunctionValue(String type, boolean newBuilding, Settlement settlement) {

		double result;

		// Demand is farming area (m^2) needed to produce food for settlement
		// population.
		double requiredFarmingAreaPerPerson = cropConfig.getFarmingAreaNeededPerPerson();
		double demand = requiredFarmingAreaPerPerson * settlement.getNumCitizens();

		// Supply is total farming area (m^2) of all farming buildings at settlement.
		double supply = 0D;
		boolean removedBuilding = false;
		for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.FARMING)) {
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(type) && !removedBuilding) {
				removedBuilding = true;
			} else {
				Farming farmingFunction = building.getFarming();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += farmingFunction.getGrowingArea() * wearModifier;
			}
		}

		// Modify result by value (VP) of food at the settlement.
		double foodValue = settlement.getGoodsManager().getGoodValuePoint(ResourceUtil.FOOD_ID);

		result = (demand / (supply + 1D)) * foodValue;

		// NOTE: investigating if other food group besides food should be added as well

		return result;
	}

	/**
	 * Checks if farm currently requires work.
	 *
	 * @return true if farm requires work
	 */
	public boolean requiresWork() {
		for (Crop c : cropList) {
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
	public double addWork(double workTime, Worker worker, Crop needyCrop) {
		return needyCrop.addWork(worker, workTime);
	}
	
	/**
	 * Gets a crop that needs work time.
	 *
	 * @param currentCrop
	 * @return crop or null if none found.
	 */
	public Crop getNeedyCrop() {
		// Check if Crops are in harvest first
		var harvestable = cropList.stream()
			.filter(c -> c.getPhase().getPhaseType() == PhaseType.HARVESTING)
			.filter(c -> c.getDailyHarvest().remaining() > 0D)
			.toList();
		if (!harvestable.isEmpty()) {
			return RandomUtil.getRandomElement(harvestable);
		}

		Crop currentCrop = needyCropCache;
		if (cropList.isEmpty())
			return null;

		int rand = RandomUtil.getRandomInt(3);
			
		if (rand == 0) {
			Crop mostNeedyCrop = null;
			double mostWork = 0; 
			
			// Pick the crop that requires most work
			for (Crop c : cropList) {
				if (c.requiresWork() && c.getCurrentWorkRequired() > mostWork) {
					mostNeedyCrop = c;
					mostWork = c.getCurrentWorkRequired();
				}
			}
			
			return mostNeedyCrop;
		}
		// Half the chance it will pick the current crop to work on
		else if ((rand == 1 || rand == 2) && currentCrop != null
			&& currentCrop.requiresWork()) {
			// Pick the current crop again unless it no longer requires work
			return currentCrop;
		}
		
		else {
			// Pick another crop that requires work
			List<Crop> needyCrops = new ArrayList<>();
			for (Crop c : cropList) {
				if (c.requiresWork()) {
					needyCrops.add(c);
				}
			}
	
			needyCropCache = RandomUtil.getRandomElement(needyCrops);
			return needyCropCache;
		}
	}

	/**
	 * Gets the number of farmers currently working at the farm.
	 *
	 * @return number of farmers
	 */
	public int getFarmerNum() {
		int result = 0;

		if (building.hasFunction(FunctionType.LIFE_SUPPORT)) {
			LifeSupport lifeSupport = building.getLifeSupport();
			for(Person p : lifeSupport.getOccupants()) {
				Task task = p.getMind().getTaskManager().getTask();
				if (task instanceof TendGreenhouse)
					result++;
			}
		}

		return result;
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
				
				// Gradually reduce aspect score by default
				for (Aspect aspect: attributes.keySet()) {
					updateAttribute(aspect, -0.01);
				}
				
				// degrade the cleanliness
				houseKeeping.degradeCleaning(1);

				// degrade the housekeeping item
				houseKeeping.degradeInspected(1);

				// Reset cumulativeDailyPAR
				for (Crop c : cropList)
					c.resetPAR();
				// Note: will need to limit the size of the other usage maps
			}

			// Determine the production level.
			double productionLevel = 0D;
			PowerMode powerMode = building.getPowerMode();
						
			if (powerMode == PowerMode.FULL_POWER)
				productionLevel = 1D;
			else if (powerMode == PowerMode.LOW_POWER)
				productionLevel = .5D;

			double solarIrradiance = surface.getSolarIrradiance(building.getSettlement().getCoordinates());
			double greyFilterRate = building.getSettlement().getGreyWaterFilteringRate();

			// Compute the effect of the temperature
			double temperatureModifier = 1D;
			double tNow = building.getCurrentTemperature();
			double tPreset = building.getPresetTemperature();
			if (tNow > (tPreset + T_TOLERANCE))
				temperatureModifier = tPreset / tNow;
			else if (tNow < (tPreset - T_TOLERANCE))
				temperatureModifier = tNow / tPreset;

			// Call timePassing on each crop.
			List<Crop> toRemove = new ArrayList<>();
			for(Crop crop : cropList) {

				try {
					crop.timePassing(pulse, productionLevel, solarIrradiance,
									 greyFilterRate, temperatureModifier);

				} catch (Exception e) {
					logger.severe(building, crop.getName() + " ran into issues ", e);
				}

				// Remove old crops.
				if (crop.getPhase().getPhaseType() == PhaseType.FINISHED) {
					// Take back the growing area
					remainingArea = remainingArea + crop.getGrowingArea();
					
					toRemove.add(crop);
				}
			}

			int size = cropList.size();
			numCrops2Plant = defaultCropNum - size;
			
			// Remove finished crops
			cropList.removeAll(toRemove);
		}
		return valid;
	}

	/**
	 * Transfers the seedling.
	 * Note: Enable this task to take time to complete the work.
	 *
	 * @param time
	 * @param worker
	 * @return Crop
	 */
	public CropSpec selectSeedling() {
		CropSpec ct = null;
		// Add any new crops.
		for (int x = 0; x < numCrops2Plant; x++) {
			int size = cropListInQueue.size();
			if (size > 0) {
				String n = cropListInQueue.get(0);
				cropListInQueue.remove(0);
				ct = cropConfig.getCropTypeByName(n);
			}

			else {
				ct = selectVPCrop();
			}
		}
		return ct;
	}
	
	/**
	 * Plants a new crop seedling.
	 * 
	 * @param ct
	 * @param time
	 * @param worker
	 */
	public void plantSeedling(CropSpec ct, double time, Worker worker) {
		Crop crop = plantACrop(ct, false, designatedCropArea);
		if (crop != null) {
			cropList.add(crop);
			cropHistory.put(crop.getIdentifier(), crop.getName());
			building.fireUnitUpdate(UnitEventType.CROP_EVENT, crop);
	
			numCrops2Plant--;
		}
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 *
	 * @return power (kW)
	 */
	@Override
	public double getCombinedPowerLoad() {
		// Power (kW) required for normal operations.
		double powerRequired = 0D;

		for (Crop crop : cropList) {
			var phaseType = crop.getPhase().getPhaseType();
		
			// Tailor the lighting according to the phase type the crop is at
			powerRequired += switch (phaseType) {
				// More power is needed for illumination and crop monitoring
				case PLANTING, INCUBATION -> crop.getLightingPower()/10; //powerGrowingCrop/2D;
				case GERMINATION -> crop.getLightingPower()/2;
				case HARVESTING, FINISHED -> crop.getLightingPower()*.75;
				default -> crop.getLightingPower() * 1.1;
			};
		}

		// The normal lighting power during growing phase
		// Note: add separate auxiliary power for subsystem, not just lighting power
		// May add back powerRequired += getTotalLightingPower()
	
		return powerRequired;
	}

	/**
	 * Gets the total amount of lighting power in this greenhouse.
	 *
	 * @return power (kW)
	 */
	public double getTotalLightingPower() {
		return cropList.stream().mapToDouble(Crop::getLightingPower).sum();
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
		for (Crop crop : cropList) {
			if (crop.needsPower())
				powerRequired += powerSustainingCrop;
		}

		return powerRequired;
	}

	/**
	 * Gets the total growing area in this building function.
	 *
	 * @return growing area in square meters
	 */
	public double getGrowingArea() {
		return maxGrowingArea;
	}

	/**
	 * Gets the remaining growing area in this building function.
	 *
	 * @return remaining area in square meters
	 */
	public double getRemainingArea() {
		return remainingArea;
	}
	
	/**
	 * Gets the average number of growing cycles for a crop per orbit.
	 */
	public double getAverageGrowingCyclesPerOrbit() {

		double aveGrowingTime = cropConfig.getAverageCropGrowingTime();
		double solsInOrbit = MarsTime.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR;
		return solsInOrbit * 1000D / aveGrowingTime; // e.g. 668 sols * 1000 / 50,000
																				// millisols
	}

	/**
	 * Checks to see if a botany lab with an open research slot is available and
	 * performs cell tissue extraction.
	 * 
	 * @return true if work has been done
	 */
	public boolean checkBotanyLab() {
		// Check to see if a botany lab is available
		boolean hasEmptySpace = false;
		
		// Check to see if the local greenhouse has a research slot
		if (lab == null)
			lab = building.getResearch();
		if (lab.hasSpecialty(ScienceType.BOTANY)) {
			hasEmptySpace = lab.checkAvailability();
		}

		if (hasEmptySpace) {
			// check to see if it can accommodate another researcher
			hasEmptySpace = lab.addResearcher();
		}

		else {
			// Check available research slot in another lab located in another greenhouse
			Set<Building> laboratoryBuildings = building.getSettlement().getBuildingManager().getBuildingSet(FunctionType.RESEARCH);
			Iterator<Building> i = laboratoryBuildings.iterator();
			while (i.hasNext() && !hasEmptySpace) {
				Building building = i.next();
				Research lab1 = building.getResearch();
				if (lab1.hasSpecialty(ScienceType.BOTANY)) {
					hasEmptySpace = lab1.checkAvailability();
					if (hasEmptySpace) {
						hasEmptySpace = lab1.addResearcher();
					}
				}
			}
		}
		
		return hasEmptySpace;
	}

	
	/**
	 * Checks on a crop tissue.
	 * 
	 * @return
	 */
	public String checkOnCropTissue() {
		String name = null;
		List<String> unchecked = lab.getUncheckedTissues();
		int size = unchecked.size();
		if (size > 0) {
			int rand = RandomUtil.getRandomInt(size - 1);
			name = unchecked.get(rand);
			// mark this tissue culture. At max of 3 marks for each culture per sol
			lab.markChecked(name);
		}
		
		return name;
	}
		

	@Override
	public double getMaintenanceTime() {
		return maxGrowingArea * .15D;
	}

	/**
	 * Gets the farm's current crops.
	 *
	 * @return collection of crops
	 */
	public List<Crop> getCrops() {
		return cropList;
	}

	
    public HouseKeeping getHousekeeping() {
        return houseKeeping;
    }

	/**
	 * Records the average resource usage of a resource on a crop.
	 *
	 * @param cropName
	 * @param usage    average water consumption in kg/sol
	 * @Note positive usage amount means consumption 
	 * @Note negative usage amount means generation
	 * @param type resource (0 for water, 1 for o2, 2 for co2, 3 for grey water)
	 */
	public void addCropUsage(String cropName, double usage, int type) {
		SolMetricDataLogger<Integer> crop = cropUsage.get(cropName);
		if (crop == null) {
			crop = new SolMetricDataLogger<>(MAX_NUM_SOLS);
			cropUsage.put(cropName, crop);
		}

		crop.increaseDataPoint(type, usage);
	}

	/**
	 * Computes the average usage of a resource on a crop.
	 *
	 * @param type resource (0 for water, 1 for o2, 2 for co2, 3 for grey water)
	 * @param cropName
	 * @return average water consumption in kg/sol
	 */
	private double computeUsage(int type, String cropName) {
		double result = 0;
		SolMetricDataLogger<Integer> crop = cropUsage.get(cropName);
		if (crop != null) {
			result = crop.getDailyAverage(type);
		}
		return result;
	}

	/**
	 * Computes the resource usage on all crops.
	 *
	 * @type resource type (0 for water, 1 for o2, 2 for co2)
	 * @return water consumption in kg/sol
	 */
	public double computeUsage(int type) {
		// Note: the value is kg per square meter per sol
		double sum = 0;

		for (Crop c : cropList) {
			sum += computeUsage(type, c.getName());
		}
		
		return Math.round(sum * 100.0) / 100.0;
	}

	/**
	 * Gets the daily average water usage of the last 5 sols.
	 * Not: most weight on yesterday's usage. Least weight on usage from 5 sols ago.
	 *
	 * @return
	 */
	public double getDailyAverageWaterUsage() {
		return computeUsage(0);
	}

	public int getNumCrops2Plant() {
		return numCrops2Plant;
	}
	
	public Research getResearch() {
		if (lab == null)
			lab = building.getResearch();
		return lab;
	}

	/**
	 * Get the number of crops that needs tending in this Farm.
	 * 
	 * @return
	 */
	public int getNumNeedTending() {

		int cropsNeedingTending = 0;
		for (Crop c : cropList) {
			if (c.requiresWork()) {
				cropsNeedingTending++;
			}
			// if the health condition is below 50%,
			// need special care
			if (c.getHealthCondition() < .5)
				cropsNeedingTending++;
			
			if (c.getPhase().getPhaseType() == PhaseType.HARVESTING)
				cropsNeedingTending++;
			
		}
		cropsNeedingTending += numCrops2Plant;

		return cropsNeedingTending;
	}

	/**
	 * Gets the tending score of all growing crops.
	 * 
	 * @return
	 */
	public double getTendingScore() {
		double score = 0;
		for (Crop c : cropList) {
			score += c.getTendingScore();
		}
		return score;
	}
	
	/**
	 * Gets the cumulative work time.
	 * 
	 * @return
	 */
	public double getCumulativeWorkTime() {
		return cumulativeWorkTime + houseKeeping.getCumulativeWorkTime();
	}
	
	/**
	 * Adds the cumulative work time.
	 * 
	 * @return
	 */
	public void addCumulativeWorkTime(double value) {
		cumulativeWorkTime += value;
	}
	
	public void setDesignatedCropArea(int area) {
		designatedCropArea = area;
	}
	
	public int getDesignatedCropArea() {
		return designatedCropArea;
	}
	
	@Override
	public void destroy() {
		super.destroy();
	
		lab = null;
		cropListInQueue = null;
		cropList = null;
	}
}

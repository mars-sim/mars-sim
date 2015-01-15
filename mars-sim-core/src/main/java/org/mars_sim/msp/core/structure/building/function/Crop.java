/**
 * Mars Simulation Project
 * Crop.java
 * @version 3.07 2014-12-09
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * The Crop class is a food crop grown on a farm.
 */
public class Crop
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Crop.class.getName());

	// TODO Static members of crops should be initialized from some xml instead of being hard coded.
	/** Amount of waste water needed / harvest mass. */
	public static final double WASTE_WATER_NEEDED = 2D;
	/** Amount of carbon dioxide needed /harvest mass. */
	public static final double CARBON_DIOXIDE_NEEDED = 2D;

	// TODO Crop phases should be an internationalizable enum.
	public static final String PLANTING = "Planting";
	public static final String GROWING = "Growing";
	// TODO: add FLOWERING phase
	public static final String HARVESTING = "Harvesting";
	public static final String FINISHED = "Finished";

	// Data members
	/** The type of crop. */
	private CropType cropType;
	/** Maximum possible food harvest for crop. (kg) */
	private double maxHarvest;
	/** Farm crop being grown in. */
	private Farming farm;
	/** The settlement the crop is located at. */
	private Settlement settlement;
	/** Current phase of crop. */
	private String phase;
	/** Required work time for planting (millisols). */
	private double plantingWorkRequired;
	/** Required work time to tend crop daily (millisols). */
	private double dailyTendingWorkRequired;
	/** Required work time for harvesting (millisols). */
	private double harvestingWorkRequired;
	/** Completed work time in current phase (millisols). */
	private double currentPhaseWorkCompleted;
	/** Actual food harvest for crop. (kg) */
	private double actualHarvest;
	/** Growing phase time completed thus far (millisols). */
	private double growingTimeCompleted;
	/** Current sol of month. */
	private int currentSol;

	/**
	 * Constructor.
	 * @param cropType the type of crop.
	 * @param maxHarvest - Maximum possible food harvest for crop. (kg)
	 * @param farm - Farm crop being grown in.
	 * @param settlement - the settlement the crop is located at.
	 * @param newCrop - true if this crop starts in it's planting phase.
	 * called by Farming.java constructor and timePassing()
	 */
	public Crop(CropType cropType, double maxHarvest, Farming farm, Settlement settlement, boolean newCrop) {
		this.cropType = cropType;
				//logger.info("constructor : the new crop is " + cropType.getName());
		this.maxHarvest = maxHarvest;
		this.farm = farm;
		this.settlement = settlement;

		//cropTypeList = new ArrayList<CropType>();
		//cropType.getCropList(); 

		
		// Determine work required.
		// TODO: need further debugging !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		plantingWorkRequired = maxHarvest;
		dailyTendingWorkRequired = maxHarvest;
		harvestingWorkRequired = maxHarvest * 3D; // old default is 5. why?
				//logger.info("constructor :"
				//		+ " maxHarvest/dailyTendingWorkRequired/plantingWorkRequired is " + Math.round(maxHarvest));
				//logger.info("constructor :"
				//		+ " harvestingWorkRequired is " + Math.round(harvestingWorkRequired));
		if (newCrop) {
			phase = PLANTING;
				//logger.info("constructor : a new crop is in Planting phase");
			actualHarvest = 0D;
		}
		else {
			phase = GROWING;
			// set up a crop's "initial" percentage of growth when the simulation gets started
			growingTimeCompleted = RandomUtil.getRandomDouble(cropType.getGrowingTime());
					//logger.info("constructor : the crop is in Growing phase");
					//logger.info("constructor : Growing-time is " + Math.round(cropType.getGrowingTime()/1000) );
					//logger.info("constructor : # of days completed (in random) is " + Math.round(growingTimeCompleted/1000));
			actualHarvest = maxHarvest * (growingTimeCompleted / cropType.getGrowingTime());
					//logger.info("constructor : work completed so far is " + Math.round(actualHarvest));
		}
	}

	/**
	 * Gets the type of crop.
	 *
	 * @return crop type
	 */
	public CropType getCropType() {
		return cropType;
	}
	
	/**
	 * Gets the phase of the crop.
	 * @return phase
	 */
	// Called by BuildingPanelFarming.java to retrieve the phase of the crop
	public String getPhase() {
		return phase;
	}


	/**
	 * Gets the crop category
	 * @return category
	 * 2014-10-10 by mkung: added this method for UI to show crop category
	 */
	// Called by BuildingPanelFarming.java to retrieve the crop category
	public String getCategory() {
		return cropType.getCropCategory();
	}
	
	/**
	 * Gets the maximum possible food harvest for crop.
	 * @return food harvest (kg.)
	 */
	public double getMaxHarvest() { return maxHarvest; }

	/**
	 * Gets the amount of growing time completed.
	 * @return growing time (millisols)
	 */
	public double getGrowingTimeCompleted() { return growingTimeCompleted; }

	/**
	 * Checks if crop needs additional work on current sol.
	 * @return true if more work needed.
	 */
	public boolean requiresWork() {
		//logger.info("currentPhaseWorkCompleted is " + currentPhaseWorkCompleted);
		//logger.info("dailyTendingWorkRequired is " + dailyTendingWorkRequired);
		boolean result = false;
		if (phase.equals(PLANTING) || phase.equals(HARVESTING)) result = true;
		if (phase.equals(GROWING)) {
			if (dailyTendingWorkRequired > currentPhaseWorkCompleted) result = true;
		}

		return result;
	}

	/**
	 * Gets the overall health condition of the crop.
	 * 
	 * @return condition as value from 0 (poor) to 1 (healthy)
	 */
	// Called by BuildingPanelFarming.java to retrieve the health condition status
	public double getCondition() {
		// O:bad, 1:good
		double result = 0D;
			
		if (phase.equals(PLANTING)) result = 1D;
		else if (phase.equals(GROWING)) {
			if ((maxHarvest == 0D) || (growingTimeCompleted == 0D)) result = 1D;
			else result = (actualHarvest * cropType.getGrowingTime()) / (maxHarvest * growingTimeCompleted);
		}
		else if (phase.equals(HARVESTING) || phase.equals(FINISHED)) {
			result = actualHarvest / maxHarvest;
		}

		if (result > 1D) result = 1D;
		else if (result < 0D) result = 0D;
		
		//logger.info("getCondition() : crop's condition is "+ result);
				
		return result;
	}

	/**
	 * Adds work time to the crops current phase.
	 * @param workTime - Work time to be added (millisols)
	 * @return workTime remaining after working on crop (millisols)
	 * @throws Exception if error adding work.
	 */ 
	//called by Farming.java's addWork()
	// 2014-10-07 by mkung: see below for changes when calling addHarvest()
	public double addWork(double workTime) {
		double remainingWorkTime = workTime;

		if (phase.equals(PLANTING)) {
				//logger.info("addWork() : crop is in Planting phase");
			currentPhaseWorkCompleted += remainingWorkTime;
			if (currentPhaseWorkCompleted >= plantingWorkRequired) {
				remainingWorkTime = currentPhaseWorkCompleted - plantingWorkRequired;
				currentPhaseWorkCompleted = 0D;
				currentSol = Simulation.instance().getMasterClock().getMarsClock().getSolOfMonth();
				phase = GROWING;
			}
			else {
				remainingWorkTime = 0D;
			}
		}

		if (phase.equals(GROWING)) {
			//logger.info("addWork() : crop is in Growing phase");
			currentPhaseWorkCompleted += remainingWorkTime;
			if (currentPhaseWorkCompleted >= dailyTendingWorkRequired) {
				remainingWorkTime = currentPhaseWorkCompleted - dailyTendingWorkRequired;
				currentPhaseWorkCompleted = dailyTendingWorkRequired;
			}
			else {
				remainingWorkTime = 0D;
			}
		}

		if (phase.equals(HARVESTING)) {
				//logger.info("addWork() : crop is in Harvesting phase");
			currentPhaseWorkCompleted += remainingWorkTime;
			if (currentPhaseWorkCompleted >= harvestingWorkRequired) {
				// Harvest is over. Close out this phase
					//logger.info("addWork() : done harvesting. remainingWorkTime is " + Math.round(remainingWorkTime));
				double overWorkTime = currentPhaseWorkCompleted - harvestingWorkRequired;
				// 2014-10-07 mkung: modified addHarvest parameter list to include crop name
				farm.addHarvest(actualHarvest * (remainingWorkTime - overWorkTime) / harvestingWorkRequired,
						cropType.getName(), cropType.getCropCategory());
					//logger.info("addWork() : last actualHarvest amount is " + Math.round(actualHarvest * (remainingWorkTime - overWorkTime) / harvestingWorkRequired));
				remainingWorkTime = overWorkTime;
				phase = FINISHED;

			}
			else { 	// continue the harvesting process
				// 2014-10-07 mkung: modified addHarvest parameter list to include crop name
					//logger.info("addWork() : still harvesting.... calculating actualHarvest");
					//logger.info("addWork() : actualHarvest is " + Math.round(actualHarvest * workTime / harvestingWorkRequired));
					//logger.info("addWork() : calling Farming.java's addHarvest() now");
					farm.addHarvest(actualHarvest * workTime / harvestingWorkRequired, 
							cropType.getName(), cropType.getCropCategory());
				remainingWorkTime = 0D;
				//logger.info("addWork() : just set remainingWorkTime to " + remainingWorkTime);
			}
		}

		return remainingWorkTime;
	}

	/**
	 * Time passing for crop.
	 * @param time - amount of time passing (millisols)
	 */
	public void timePassing(double time) {

		if (time > 0D) {
			if (phase.equals(GROWING)) {
				growingTimeCompleted += time;
				if (growingTimeCompleted > cropType.getGrowingTime()) {
					phase = HARVESTING;
					currentPhaseWorkCompleted = 0D;
				}
				else {
					// Modify actual harvest amount based on daily tending work.
					int newSol = Simulation.instance().getMasterClock().getMarsClock().getSolOfMonth();
					if (newSol != currentSol) {
						double maxDailyHarvest = maxHarvest / (cropType.getGrowingTime() / 1000D);
						double dailyWorkCompleted = currentPhaseWorkCompleted / dailyTendingWorkRequired;
						actualHarvest += (maxDailyHarvest * (dailyWorkCompleted - .5D));
						currentSol = newSol;
						currentPhaseWorkCompleted = 0D;
					}

					double maxPeriodHarvest = maxHarvest * (time / cropType.getGrowingTime());
					double harvestModifier = 1D;

					// Determine harvest modifier by amount of sunlight.
					SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
					double sunlight = surface.getSurfaceSunlight(settlement.getCoordinates());
					harvestModifier = harvestModifier * ((sunlight * .5D) + .5D);

					// TODO mkung: Modify harvest modifier by amount of artificial light available to the greenhouse
					
					Inventory inv = settlement.getInventory();

					// Determine harvest modifier by amount of waste water available.
					double wasteWaterRequired = maxPeriodHarvest * WASTE_WATER_NEEDED;
					AmountResource wasteWater = AmountResource.findAmountResource("waste water");
					double wasteWaterAvailable = inv.getAmountResourceStored(wasteWater, false);
					double wasteWaterUsed = wasteWaterRequired;
					if (wasteWaterUsed > wasteWaterAvailable) 
						wasteWaterUsed = wasteWaterAvailable;
					inv.retrieveAmountResource(wasteWater, wasteWaterUsed);
					
					// 2015-01-09 Added addDemandTotalRequest()
				    inv.addDemandTotalRequest(wasteWater);
					// 2015-01-09 addDemandRealUsage()
				    inv.addDemandAmount(wasteWater, wasteWaterUsed);
					
					harvestModifier = harvestModifier * (((wasteWaterUsed / wasteWaterRequired) * .5D) + .5D);

					
					// Determine harvest modifier by amount of water available.				
					double waterAmount = wasteWaterUsed * .8D;
					AmountResource water = AmountResource.findAmountResource(org.mars_sim.msp.core.LifeSupport.WATER);
					double waterCapacity = inv.getAmountResourceRemainingCapacity(water, false, false);
					if (waterAmount > waterCapacity) 
						waterAmount = waterCapacity;
					inv.storeAmountResource(water, waterAmount, false);
					// 2015-01-15 Add addSupplyAmount()
		            inv.addSupplyAmount(water, waterAmount);
	
					// Determine harvest modifier by amount of carbon dioxide available.
					AmountResource carbonDioxide = AmountResource.findAmountResource("carbon dioxide");
					double carbonDioxideRequired = maxPeriodHarvest * CARBON_DIOXIDE_NEEDED;
					double carbonDioxideAvailable = inv.getAmountResourceStored(carbonDioxide, false);
					double carbonDioxideUsed = carbonDioxideRequired;
					if (carbonDioxideUsed > carbonDioxideAvailable) {
						carbonDioxideUsed = carbonDioxideAvailable;
					}
					inv.retrieveAmountResource(carbonDioxide, carbonDioxideUsed);
					
					// 2015-01-09 Added addDemandTotalRequest()
				    inv.addDemandTotalRequest(carbonDioxide);
					// 2015-01-09 addDemandRealUsage()
				   	inv.addDemandAmount(carbonDioxide, carbonDioxideUsed);
					
					
					harvestModifier = harvestModifier * (((carbonDioxideUsed / carbonDioxideRequired) *
							.5D) + .5D);

					
					// Determine harvest modifier by amount of oxygen available.							
					AmountResource oxygen = AmountResource.findAmountResource(org.mars_sim.msp.core.LifeSupport.OXYGEN);
					double oxygenAmount = carbonDioxideUsed * .9D;
					double oxygenCapacity = inv.getAmountResourceRemainingCapacity(oxygen, false, false);
					if (oxygenAmount > oxygenCapacity) oxygenAmount = oxygenCapacity;
					inv.storeAmountResource(oxygen, oxygenAmount, false);
					 // 2015-01-15 Add addSupplyAmount()
		            inv.addSupplyAmount(oxygen, oxygenAmount);   	
					// Modify harvest amount.
					actualHarvest += maxPeriodHarvest * harvestModifier;

					// Check if crop is dying if it's at least 25% along on it's growing time and its condition
					// is less than 10% normal.
					if (((growingTimeCompleted / cropType.getGrowingTime()) > .25D) &&
							(getCondition() < .1D)) {
						phase = FINISHED;
						logger.info("Crop " + cropType.getName() + " at " + settlement.getName() + " died.");
					}
				}
			}
		}
	}

	/**
	 * Gets a random crop type.
	 * @return crop type
	 * @throws Exception if crops could not be found.
	 */
   	// 2014-12-09 Added new param cropInQueue and changed method name to getNewCrop()
	public static CropType getNewCrop(String cropInQueue) {
		CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
		List<CropType> cropTypes = cropConfig.getCropList();
		//cropTypeList = cropTypes;
		if (cropInQueue == null) {
			int r = RandomUtil.getRandomInt(cropTypes.size() - 1);
			return cropTypes.get(r);
		} else {
			CropType crop = null;
			Iterator<CropType> i = cropTypes.iterator();
			while (i.hasNext()) {
				CropType c = i.next();
				if (c.getName() == cropInQueue)
					crop = c;
			}
			return crop;	
		}
	}

	/**
	 * Gets the average growing time for a crop.
	 * @return average growing time (millisols)
	 * @throws Exception if error reading crop config.
	 */
	public static double getAverageCropGrowingTime() {
		CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
		double totalGrowingTime = 0D;
		List<CropType> cropTypes = cropConfig.getCropList();
		Iterator<CropType> i = cropTypes.iterator();
		while (i.hasNext()) totalGrowingTime += i.next().getGrowingTime();
		return totalGrowingTime / cropTypes.size();
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		cropType = null;
		farm = null;
		settlement = null;
		phase = null;
	}
}

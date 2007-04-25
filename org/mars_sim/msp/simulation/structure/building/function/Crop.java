/**
 * Mars Simulation Project
 * Crop.java
 * @version 2.78 2004-11-20
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.mars.*;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;

/**
 * The Crop class is a food crop grown on a farm.
 */
public class Crop implements Serializable {
    
	// Static members
	public static final double WASTE_WATER_NEEDED = 5D; // Amount of waste water needed / harvest mass.
	public static final double CARBON_DIOXIDE_NEEDED = 2D; // Amount of carbon dioxide needed /harvest mass.
	
    // Crop phases
    public static final String PLANTING = "Planting";
    public static final String GROWING = "Growing";
    public static final String HARVESTING = "Harvesting";
    public static final String FINISHED = "Finished";
    
    // Data members
    private CropType cropType; // The type of crop.
    private double maxHarvest; // Maximum possible food harvest for crop. (kg)
    private Farming farm; // Farm crop being grown in.
    private Settlement settlement; // The settlement the crop is located at.
    private String phase; // Current phase of crop.
    private double plantingWorkRequired; // Required work time for planting (millisols)
    private double dailyTendingWorkRequired; // Required work time to tend crop daily (millisols)
    private double harvestingWorkRequired; // Required work time to for harvesting (millisols)
    private double currentPhaseWorkCompleted; // Completed work time in current phase (millisols)
    private double actualHarvest; // Actual food harvest for crop. (kg)
    private double growingTimeCompleted; // Growing phase time completed thus far (millisols)
    private int currentSol; // Current sol of month.
   
    /**
     * Constructor
     * @param cropType the type of crop.
     * @param maxHarvest - Maximum possible food harvest for crop. (kg)
     * @param farm - Farm crop being grown in.
     * @param settlement - the settlement the crop is located at.
     * @param newCrop - true if this crop starts in it's planting phase.
     */
    public Crop(CropType cropType, double maxHarvest, Farming farm, Settlement settlement, boolean newCrop) {
        this.cropType = cropType;
        this.maxHarvest = maxHarvest;
        this.farm = farm;
        this.settlement = settlement;
        
        // Determine work required.
        plantingWorkRequired = maxHarvest;
        dailyTendingWorkRequired = maxHarvest;
        harvestingWorkRequired = maxHarvest * 5D;
        
        if (newCrop) {
        	phase = PLANTING;
        	actualHarvest = 0D;
        } 
        else {
        	phase = GROWING;
        	growingTimeCompleted = RandomUtil.getRandomDouble(cropType.getGrowingTime());
        	actualHarvest = maxHarvest * (growingTimeCompleted / cropType.getGrowingTime());
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
    public String getPhase() {
        return phase;
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
    public double getCondition() {
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
        return result;
    }
    
    /**
     * Adds work time to the crops current phase.
     * @param workTime - Work time to be added (millisols)
     * @return workTime remaining after working on crop (millisols)
     * @throws Exception if error adding work.
     */
    public double addWork(double workTime) throws Exception {
        double remainingWorkTime = workTime;
        
        if (phase.equals(PLANTING)) {
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
            currentPhaseWorkCompleted += remainingWorkTime;
            if (currentPhaseWorkCompleted >= harvestingWorkRequired) {
                double overWorkTime = currentPhaseWorkCompleted - harvestingWorkRequired;
                farm.addHarvest(actualHarvest * (remainingWorkTime - overWorkTime) / harvestingWorkRequired);
                remainingWorkTime = overWorkTime;
                phase = FINISHED;
            }
            else {
                farm.addHarvest(actualHarvest * workTime / harvestingWorkRequired);
                remainingWorkTime = 0D;
            }
        }
     
        return remainingWorkTime;
    }
    
    /**
     * Time passing for crop.
     * @param time - amount of time passing (millisols)
     * @throws Exception if error during time.
     */
    public void timePassing(double time) throws Exception {
        
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
    				double sunlight = (double) surface.getSurfaceSunlight(settlement.getCoordinates());
    				harvestModifier = harvestModifier * ((sunlight * .5D) + .5D);
                    
                	Inventory inv = settlement.getInventory();
                	
                	// Determine harvest modifier by amount of waste water available.
                	double wasteWaterRequired = maxPeriodHarvest * WASTE_WATER_NEEDED;
                	double wasteWaterAvailable = inv.getAmountResourceStored(AmountResource.WASTE_WATER);
                	double wasteWaterUsed = wasteWaterRequired;
                	if (wasteWaterUsed > wasteWaterAvailable) wasteWaterUsed = wasteWaterAvailable;
                	try {
                		inv.retrieveAmountResource(AmountResource.WASTE_WATER, wasteWaterUsed);
                		inv.storeAmountResource(AmountResource.WATER, wasteWaterUsed * .8D);
                	}
                	catch (Exception e) {}
                	harvestModifier = harvestModifier * (((wasteWaterUsed / wasteWaterRequired) * .5D) + .5D);
                    
                	// Determine harvest modifier by amount of carbon dioxide available.
                	double carbonDioxideRequired = maxPeriodHarvest * CARBON_DIOXIDE_NEEDED;
                	double carbonDioxideAvailable = inv.getAmountResourceStored(AmountResource.CARBON_DIOXIDE);
                	double carbonDioxideUsed = carbonDioxideRequired;
                	if (carbonDioxideUsed > carbonDioxideAvailable) carbonDioxideUsed = carbonDioxideAvailable;
                	try {
                		inv.retrieveAmountResource(AmountResource.CARBON_DIOXIDE, carbonDioxideUsed);
                		inv.storeAmountResource(AmountResource.OXYGEN, carbonDioxideUsed * .9D);
                	}
                	catch (Exception e) {}
                	harvestModifier = harvestModifier * (((carbonDioxideUsed / carbonDioxideRequired) * .5D) + .5D);   
                
                	// Modifiy harvest amount.
                	actualHarvest += maxPeriodHarvest * harvestModifier;
                
                	// Check if crop is dying if it's at least 25% along on it's growing time and its condition 
                	// is less than 10% normal.
                	if (((growingTimeCompleted / cropType.getGrowingTime()) > .25D) && (getCondition() < .1D)) {
                		phase = FINISHED;
                		// System.out.println("Crop " + cropType.getName() + " at " + settlement.getName() + " died.");
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
    public static CropType getRandomCropType() throws Exception {
    	CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
    	List cropTypes = cropConfig.getCropList();    
        int r = RandomUtil.getRandomInt(cropTypes.size() - 1);
        return (CropType) cropTypes.get(r);
    }
    
    /**
     * Gets the average growing time for a crop.
     * @return average growing time (millisols)
     * @throws Exception if error reading crop config.
     */
    public static double getAverageCropGrowingTime() throws Exception {
    	CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
    	double totalGrowingTime = 0D;
    	List cropTypes = cropConfig.getCropList();  
    	Iterator i = cropTypes.iterator();
    	while (i.hasNext()) totalGrowingTime += ((CropType) i.next()).getGrowingTime();
    	return totalGrowingTime / cropTypes.size();
    }
}
/**
 * Mars Simulation Project
 * Crop.java
 * @version 2.75 2004-04-01
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import java.io.Serializable;
import java.util.List;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.function.Farming;

/**
 * The Crop class is a food crop grown on a farm.
 */
public class Crop implements Serializable {
    
    // Crop phases
    public static final String PLANTING = "Planting";
    public static final String GROWING = "Growing";
    public static final String HARVESTING = "Harvesting";
    public static final String FINISHED = "Finished";
    
    // Data members
    private CropType cropType; // The type of crop.
    private double maxHarvest; // Maximum possible food harvest for crop. (kg)
    private Farming farm; // Farm crop being grown in.
    private Mars mars; // The planet Mars.
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
     * @param name - The name of the crop.
     * @param maxHarvest - Maximum possible food harvest for crop. (kg)
     * @param growingPeiod - Length of growing phase for crop. (millisols)
     * @param farm - Farm crop being grown in.
     * @param mars - planet Mars.
     * @param settlement - the settlement the crop is located at.
     */
    public Crop(CropType cropType, double maxHarvest, Farming farm, Mars mars, Settlement settlement) {
        this.cropType = cropType;
        this.maxHarvest = maxHarvest;
        this.farm = farm;
        this.mars = mars;
        this.settlement = settlement;
        
        // Determine work required.
        plantingWorkRequired = maxHarvest;
        dailyTendingWorkRequired = maxHarvest;
        harvestingWorkRequired = maxHarvest * 5D;
        
        phase = PLANTING;
        actualHarvest = 0D;
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
     */
    public double addWork(double workTime) {
        double remainingWorkTime = workTime;
        
        if (phase.equals(PLANTING)) {
            currentPhaseWorkCompleted += remainingWorkTime;
            if (currentPhaseWorkCompleted >= plantingWorkRequired) {
                remainingWorkTime = currentPhaseWorkCompleted - plantingWorkRequired;
                currentPhaseWorkCompleted = 0D;
                currentSol = farm.getBuilding().getBuildingManager().getSettlement()
                    .getMars().getMasterClock().getMarsClock().getSolOfMonth();
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
     */
    public void timePassing(double time) {
        
        if (phase.equals(GROWING)) {
            growingTimeCompleted += time;
            if (growingTimeCompleted > cropType.getGrowingTime()) {
                phase = HARVESTING;
                currentPhaseWorkCompleted = 0D;
            }
            else {
                // Modify actual harvest amount based on daily tending work.
                int newSol = mars.getMasterClock().getMarsClock().getSolOfMonth();
                if (newSol != currentSol) {
                    double maxDailyHarvest = maxHarvest / (cropType.getGrowingTime() / 1000D);
                    double dailyWorkCompleted = currentPhaseWorkCompleted / dailyTendingWorkRequired;
                    actualHarvest = actualHarvest + (maxDailyHarvest * (dailyWorkCompleted - .5D));
                    currentSol = newSol;
                    currentPhaseWorkCompleted = 0D;
                }
                
                double maxPeriodHarvest = maxHarvest * (time / cropType.getGrowingTime());
                double harvestModifier = 1D;
                
                // Determine harvest modifier by amount of sunlight.
                double sunlight = (double) mars.getSurfaceFeatures().getSurfaceSunlight(settlement.getCoordinates());
                harvestModifier = harvestModifier * ((sunlight * .5D) + .5D);
                    
                // Determine harvest modifier by amount of waste water available.
                double wasteWaterRequired = maxPeriodHarvest * 100D;
                double wasteWaterUsed = settlement.getInventory().removeResource(Resource.WASTE_WATER, wasteWaterRequired);
                settlement.getInventory().addResource(Resource.WATER, wasteWaterUsed * .8D);
                harvestModifier = harvestModifier * (((wasteWaterUsed / wasteWaterRequired) * .5D) + .5D);
                    
                // Determine harvest modifier by amount of carbon dioxide available.
                double carbonDioxideRequired = maxPeriodHarvest * 2D;
                double carbonDioxideUsed = settlement.getInventory().removeResource(Resource.CARBON_DIOXIDE, carbonDioxideRequired);
                settlement.getInventory().addResource(Resource.OXYGEN, carbonDioxideUsed * .9D);
                harvestModifier = harvestModifier * (((carbonDioxideUsed / carbonDioxideRequired) * .5D) + .5D);
                    
                // Modifiy harvest amount.
                actualHarvest += maxPeriodHarvest * harvestModifier;
            }
        }
    }
    
    /**
     * Gets a random crop type.
     * @param cropConfig the crop configuration.
     * @return crop type
     * @throws Exception if crops could not be found.
     */
    public static CropType getRandomCropType(CropConfig cropConfig) throws Exception {
    	
    	List cropTypes = cropConfig.getCropList();    
        int r = RandomUtil.getRandomInt(cropTypes.size() - 1);
        return (CropType) cropTypes.get(r);
    }
}
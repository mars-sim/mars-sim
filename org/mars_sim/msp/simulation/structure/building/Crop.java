/**
 * Mars Simulation Project
 * Crop.java
 * @version 2.75 2003-01-15
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
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
    
    // Crop types
    private static ArrayList cropTypes = null;
    
    // Data members
    private CropType cropType; // The type of crop.
    private double maxHarvest; // Maximum possible food harvest for crop. (kg)
    private Farming farm; // Farm crop being grown in.
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
     */
    public Crop(CropType cropType, double maxHarvest, Farming farm) {
        this.cropType = cropType;
        this.maxHarvest = maxHarvest;
        this.farm = farm;
        
        // Determine work required.
        plantingWorkRequired = maxHarvest * 10D;
        dailyTendingWorkRequired = maxHarvest;
        harvestingWorkRequired = maxHarvest * 10D;
        
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
     * Adds work time to the crops current phase.
     * @param workTime - Work time to be added (millisols)
     * @return workTime remaining after working on crop (millisols)
     */
    public double addWork(double workTime) {
        double remainingWorkTime = workTime;
        
        if (phase.equals(PLANTING)) {
            currentPhaseWorkCompleted += remainingWorkTime;
            if (currentPhaseWorkCompleted > plantingWorkRequired) {
                remainingWorkTime = currentPhaseWorkCompleted - plantingWorkRequired;
                currentPhaseWorkCompleted = 0D;
                currentSol = ((Building) farm).getBuildingManager().getSettlement()
                    .getMars().getMasterClock().getMarsClock().getSolOfMonth();
                phase = GROWING;
            }
            else {
                remainingWorkTime = 0D;
            }
        }
        
        if (phase.equals(GROWING)) {
            currentPhaseWorkCompleted += remainingWorkTime;
            if (currentPhaseWorkCompleted > dailyTendingWorkRequired) {
                remainingWorkTime = currentPhaseWorkCompleted - dailyTendingWorkRequired;
                currentPhaseWorkCompleted = dailyTendingWorkRequired;
            }
        }
        
        if (phase.equals(HARVESTING)) {
            currentPhaseWorkCompleted += remainingWorkTime;
            if (currentPhaseWorkCompleted > harvestingWorkRequired) {
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
                int newSol = ((Building) farm).getBuildingManager().getSettlement()
                    .getMars().getMasterClock().getMarsClock().getSolOfMonth();
                if (newSol != currentSol) {
                    currentSol = newSol;
                    double maxDailyHarvest = maxHarvest / (cropType.getGrowingTime() / 1000D);
                    actualHarvest += (maxDailyHarvest * (currentPhaseWorkCompleted / dailyTendingWorkRequired));
                    currentPhaseWorkCompleted = 0D;
                }
            }
        }
    }
    
    /**
     * Gets a random crop type.
     * @return crop type
     */
    public static CropType getRandomCropType() {
        
        if (cropTypes == null) {
            CropXmlReader cropReader = new CropXmlReader();
            cropReader.parse();
            cropTypes = cropReader.getCropTypes();
        }
        
        int r = RandomUtil.getRandomInt(cropTypes.size() - 1);
        return (CropType) cropTypes.get(r);
    }
}

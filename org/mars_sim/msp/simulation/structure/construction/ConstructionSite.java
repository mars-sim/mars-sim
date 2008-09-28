/**
 * Mars Simulation Project
 * ConstructionSite.java
 * @version 2.85 2008-08-28
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.construction;

import java.io.Serializable;

import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;

/**
 * A building construction site.
 */
public class ConstructionSite implements Serializable {
    
    // Data members
    private ConstructionStage foundationStage;
    private ConstructionStage frameStage;
    private ConstructionStage buildingStage;
    private boolean undergoingConstruction;
    
    /**
     * Constructor
     */
    ConstructionSite() {
        foundationStage = null;
        frameStage = null;
        buildingStage = null;
        undergoingConstruction = false;
    }
    
    /**
     * Checks if all construction is complete at the site.
     * @return true if construction is complete.
     */
    public boolean isAllConstructionComplete() {
        if (buildingStage != null) return buildingStage.isComplete();
        else return false;
    }
    
    /**
     * Checks if site is currently undergoing construction.
     * @return true if undergoing construction.
     */
    public boolean isUndergoingConstruction() {
        return undergoingConstruction;
    }
    
    /**
     * Sets if site is currently undergoing construction.
     * @param undergoingConstruction true if undergoing construction.
     */
    public void setUndergoingConstruction(boolean undergoingConstruction) {
        this.undergoingConstruction = undergoingConstruction;
    }
    
    /**
     * Gets the current construction stage at the site.
     * @return construction stage.
     */
    public ConstructionStage getCurrentConstructionStage() {
        ConstructionStage result = null;
        
        if (buildingStage != null) result = buildingStage;
        else if (frameStage != null) result = frameStage;
        else if (foundationStage != null) result = foundationStage;
        
        return result;
    }
    
    /**
     * Gets the next construction stage type.
     * @return next construction stage type or null if none.
     */
    public String getNextStageType() {
        String result = null;
        
        if (buildingStage != null) result = null;
        else if (frameStage != null) result = ConstructionStageInfo.BUILDING;
        else if (foundationStage != null) result = ConstructionStageInfo.FRAME;
        else result = ConstructionStageInfo.FOUNDATION;
        
        return result;
    }
    
    /**
     * Adds a new construction stage to the site.
     * @param stage the new construction stage.
     * @throws Exception if error adding construction stage.
     */
    public void addNewStage(ConstructionStage stage) throws Exception {
        if (ConstructionStageInfo.FOUNDATION.equals(stage.getInfo().getType())) {
            if (foundationStage != null) throw new Exception("Foundation stage already exists.");
            foundationStage = stage;
        }
        else if (ConstructionStageInfo.FRAME.equals(stage.getInfo().getType())) {
            if (frameStage != null) throw new Exception("Frame stage already exists");
            if (foundationStage == null) throw new Exception("Foundation stage hasn't been added yet.");
            frameStage = stage;
        }
        else if (ConstructionStageInfo.BUILDING.equals(stage.getInfo().getType())) {
            if (buildingStage != null) throw new Exception("Building stage already exists");
            if (frameStage == null) throw new Exception("Frame stage hasn't been added yet.");
            buildingStage = stage;
        }
        else throw new Exception("Stage type: " + stage.getInfo().getType() + " not valid");
    }
    
    /**
     * Creates a new building from the construction site.
     * @param manager the settlement's building manager.
     * @return newly constructed building.
     * @throws Exception if error constructing building.
     */
    public Building createBuilding(BuildingManager manager) throws Exception {
        if (buildingStage == null) throw new Exception("Building stage doesn't exist");
        Building newBuilding = new Building(buildingStage.getInfo().getName(), manager);
        manager.addBuilding(newBuilding);
        
        // Record completed building name.
        ConstructionManager constructionManager = manager.getSettlement().getConstructionManager();
        constructionManager.addConstructedBuildingName(buildingStage.getInfo().getName());
        
        // Clear construction value cache.
        constructionManager.getConstructionValues().clearCache();
        
        return newBuilding;
    }
    
    /**
     * Gets the building name the site will construct.
     * @return building name or null if undetermined.
     */
    public String getBuildingName() {
        if (buildingStage != null) return buildingStage.getInfo().getName();
        else return null;
    }
    
    /**
     * Checks if the site's current stage is unfinished.
     * @return true if stage unfinished.
     */
    public boolean hasUnfinishedStage() {
        ConstructionStage currentStage = getCurrentConstructionStage();
        return (currentStage != null) && !currentStage.isComplete();
    }
    
    /**
     * Checks if this site contains a given stage.
     * @param stage the stage info.
     * @return true if contains stage.
     */
    public boolean hasStage(ConstructionStageInfo stage) {
        if (stage == null) throw new IllegalArgumentException("stage cannot be null");
        
        boolean result = false;
        if (stage.equals(foundationStage)) result = true;
        else if (stage.equals(frameStage)) result = true;
        else if (stage.equals(buildingStage)) result = true;
        return result;
    }
}
/**
 * Mars Simulation Project
 * ConstructionManager.java
 * @version 2.85 2008-10-12
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.construction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Manager for construction sites at a settlement.
 */
public class ConstructionManager implements Serializable {
    
    //  Unit update events.
    public static final String START_CONSTRUCTION_SITE_EVENT = "start construction site";
    public static final String START_CONSTRUCTION_STAGE_EVENT = "start construction stage";
    public static final String FINISH_CONSTRUCTION_STAGE_EVENT = "finish construction stage";
    public static final String FINISH_BUILDING_EVENT = "finish building";
    
    // Data members.
    private List<ConstructionSite> sites; // The settlement's construction sites.
    private ConstructionValues values;
    private SalvageValues salvageValues;
    private List<ConstructedBuildingLogEntry> constructedBuildingLog; 
   
    /**
     * Constructor
     * @param settlement the settlement.
     */
    public ConstructionManager(Settlement settlement) {
        sites = new ArrayList<ConstructionSite>();
        values = new ConstructionValues(settlement);
        salvageValues = new SalvageValues(settlement);
        constructedBuildingLog = new ArrayList<ConstructedBuildingLogEntry>();
    }
    
    /**
     * Gets all construction sites at the settlement.
     * @return list of construction sites.
     */
    public List<ConstructionSite> getConstructionSites() {
        return new ArrayList<ConstructionSite>(sites);
    }
    
    /**
     * Gets construction sites needing a construction mission.
     * @return list of construction sites.
     */
    public List<ConstructionSite> getConstructionSitesNeedingConstructionMission() {
        List<ConstructionSite> result = new ArrayList<ConstructionSite>();
        Iterator<ConstructionSite> i = sites.iterator();
        while (i.hasNext()) {
            ConstructionSite site = i.next();
            if (!site.isUndergoingConstruction() && !site.isUndergoingSalvage() && 
                    !site.isAllConstructionComplete() && !site.isAllSalvageComplete()) {
                if (site.getCurrentConstructionStage() != null) {
                    if (!site.getCurrentConstructionStage().isSalvaging()) result.add(site);
                }
                else result.add(site);
            }
        }
        return result;
    }
    
    /**
     * Gets construction sites needing a salvage mission.
     * @return list of construction sites.
     */
    public List<ConstructionSite> getConstructionSitesNeedingSalvageMission() {
        List<ConstructionSite> result = new ArrayList<ConstructionSite>();
        Iterator<ConstructionSite> i = sites.iterator();
        while (i.hasNext()) {
            ConstructionSite site = i.next();
            if (!site.isUndergoingConstruction() && !site.isUndergoingSalvage() && 
                    !site.isAllConstructionComplete() && !site.isAllSalvageComplete()) {
                if (site.getCurrentConstructionStage() != null) {
                    if (site.getCurrentConstructionStage().isSalvaging()) result.add(site);
                }
                else result.add(site);
            }
        }
        return result;
    }
    
    /**
     * Creates a new construction site.
     * @return newly created construction site.
     */
    public ConstructionSite createNewConstructionSite() {
        ConstructionSite result = new ConstructionSite();
        sites.add(result);
        return result;
    }
    
    /**
     * Removes a construction site.
     * @param site the construction site to remove.
     * @throws Exception if site doesn't exist.
     */
    public void removeConstructionSite(ConstructionSite site) throws Exception {
        if (sites.contains(site)) sites.remove(site);
        else throw new Exception("Construction site doesn't exist.");
    }
    
    /**
     * Gets the construction values.
     * @return construction values.
     */
    public ConstructionValues getConstructionValues() {
        return values;
    }
    
    /**
     * Gets the salvage values.
     * @return salvage values.
     */
    public SalvageValues getSalvageValues() {
        return salvageValues;
    }
    
    /**
     * Adds a building log entry to the constructed buildings list.
     * @param buildingName the building name to add.
     * @param builtTime the time stamp that construction was finished.
     */
    void addConstructedBuildingLogEntry(String buildingName, MarsClock builtTime) {
        if (buildingName == null) throw new IllegalArgumentException("buildingName is null");
        else if (builtTime == null) throw new IllegalArgumentException("builtTime is null");
        else {
            ConstructedBuildingLogEntry logEntry = 
                new ConstructedBuildingLogEntry(buildingName, builtTime);
            constructedBuildingLog.add(logEntry);
        }
    }
    
    /**
     * Gets a log of all constructed buildings at the settlement.
     * @return list of ConstructedBuildingLogEntry
     */
    public List<ConstructedBuildingLogEntry> getConstructedBuildingLog() {
        return new ArrayList<ConstructedBuildingLogEntry>(constructedBuildingLog);
    }
    
    /**
     * Creates a new salvaging construction site to replace a building.
     * @param salvagedBuilding the building to be salvaged.
     * @return the construction site.
     * @throws Exception if error creating construction site.
     */
    public ConstructionSite createNewSalvageConstructionSite(Building salvagedBuilding) throws Exception {
        
        // Remove building from settlement.
        BuildingManager buildingManager = salvagedBuilding.getBuildingManager();
        buildingManager.removeBuilding(salvagedBuilding);
        
        // Move any people in building to somewhere else in the settlement.
        if (salvagedBuilding.hasFunction(LifeSupport.NAME)) {
            LifeSupport lifeSupport = (LifeSupport) salvagedBuilding.getFunction(LifeSupport.NAME);
            Iterator<Person> i = lifeSupport.getOccupants().iterator();
            while (i.hasNext()) {
                Person occupant = i.next();
                lifeSupport.removePerson(occupant);
                BuildingManager.addToRandomBuilding(occupant, buildingManager.getSettlement());
            }
        }
        
        // Add construction site.
        ConstructionSite site = createNewConstructionSite();
        ConstructionStageInfo buildingStageInfo = ConstructionUtil.getConstructionStageInfo(salvagedBuilding.getName());
        if (buildingStageInfo != null) {
            String frameName = buildingStageInfo.getPrerequisiteStage();
            ConstructionStageInfo frameStageInfo = ConstructionUtil.getConstructionStageInfo(frameName);
            if (frameStageInfo != null) {
                String foundationName = frameStageInfo.getPrerequisiteStage();
                ConstructionStageInfo foundationStageInfo = ConstructionUtil.getConstructionStageInfo(foundationName);
                if (foundationStageInfo != null) {
                    // Add foundation stage.
                    ConstructionStage foundationStage = new ConstructionStage(foundationStageInfo, site);
                    foundationStage.setCompletedWorkTime(foundationStageInfo.getWorkTime());
                    site.addNewStage(foundationStage);
                    
                    // Add frame stage.
                    ConstructionStage frameStage = new ConstructionStage(frameStageInfo, site);
                    frameStage.setCompletedWorkTime(frameStageInfo.getWorkTime());
                    site.addNewStage(frameStage);
                    
                    // Add building stage and prepare for salvage.
                    ConstructionStage buildingStage = new ConstructionStage(buildingStageInfo, site);
                    buildingStage.setSalvaging(true);
                    site.addNewStage(buildingStage);
                }
                else throw new Exception("Could not find foundation construction stage for building: " + salvagedBuilding.getName());
            }
            else throw new Exception("Could not find frame construction stage for building: " + salvagedBuilding.getName());
        }
        else throw new Exception("Could not find building construction stage for building: " + salvagedBuilding.getName());
        
        // Clear construction values cache.
        values.clearCache();
        
        return site;
    }
}
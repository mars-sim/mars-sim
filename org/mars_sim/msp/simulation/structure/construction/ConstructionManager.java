/**
 * Mars Simulation Project
 * ConstructionManager.java
 * @version 2.85 2008-10-12
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.construction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.time.MarsClock;

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
    private List<ConstructedBuildingLogEntry> constructedBuildingLog; 
   
    /**
     * Constructor
     * @param settlement the settlement.
     */
    public ConstructionManager(Settlement settlement) {
        sites = new ArrayList<ConstructionSite>();
        values = new ConstructionValues(settlement);
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
    public List<ConstructionSite> getConstructionSitesNeedingMission() {
        List<ConstructionSite> result = new ArrayList<ConstructionSite>();
        Iterator<ConstructionSite> i = sites.iterator();
        while (i.hasNext()) {
            ConstructionSite site = i.next();
            if (!site.isUndergoingConstruction() && !site.isAllConstructionComplete())
                result.add(site);
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
}
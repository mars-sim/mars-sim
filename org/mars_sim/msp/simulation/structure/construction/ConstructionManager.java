/**
 * Mars Simulation Project
 * ConstructionManager.java
 * @version 2.85 2008-08-24
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.construction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.simulation.structure.Settlement;

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
   
    /**
     * Constructor
     * @param settlement the settlement.
     */
    public ConstructionManager(Settlement settlement) {
        sites = new ArrayList<ConstructionSite>();
        values = new ConstructionValues(settlement);
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
}
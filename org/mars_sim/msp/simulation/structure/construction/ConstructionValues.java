/**
 * Mars Simulation Project
 * ConstructionValues.java
 * @version 2.85 2008-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.construction;

import java.io.Serializable;
import java.util.Map;

import org.mars_sim.msp.simulation.structure.Settlement;

/**
 * Provides value information for construction.
 */
public class ConstructionValues implements Serializable {

    // Data members
    private Settlement settlement;
    
    /**
     * Constructor
     * @param settlement the settlement.
     */
    ConstructionValues(Settlement settlement) {
        this.settlement = settlement;
    }
    
    /**
     * Gets the overall value for construction at the settlement.
     * @return value (VP)
     * @throws Exception if error determining value.
     */
    public double getSettlementConstructionValue() throws Exception {
        // TODO: Implement
        return 0D;
    }
    
    /**
     * Gets the overall value for construction at the settlement.
     * @param constructionSkill the architect's construction skill.
     * @return value (VP)
     * @throws Exception if error determining value.
     */
    public double getSettlementConstructionValue(double constructionSkill) 
            throws Exception {
        // TODO: Implement
        return 0D;
    }
    
    /**
     * Gets the overall value of all existing construction sites at a settlement
     * that can be worked on with a given construction skill.
     * @param constructionSkill the architect's construction skill.
     * @return value (VP)
     * @throws Exception if error determining value.
     */
    public double getAllConstructionSitesValue(double constructionSkill) 
            throws Exception {
        // TODO: Implement
        return 0D;
    }
    
    /**
     * Gets the value of an existing construction site at a settlement.
     * @param site the construction site.
     * @return value (VP)
     * @throws Exception if error determining value.
     */
    public double getConstructionSiteValue(ConstructionSite site) throws Exception {
        // TODO: Implement
        return 0D;
    }
    
    /**
     * Gets the value of creating a new construction site at a settlement.
     * @param constructionSkill the architect's construction skill.
     * @return value (VP)
     * @throws Exception if error determining value.
     */
    public double getNewConstructionSiteValue(double constructionSkill) 
            throws Exception {
        // TODO: Implement
        return 0D;
    }
    
    /**
     * Gets a map of construction stages and their values for a particular 
     * construction site.
     * @param site the construction site.
     * @param constructionSkill the architect's construction skill.
     * @return value (VP)
     * @throws Exception if error determining value.
     */
    public Map<ConstructionStage, Double> getNewConstructionStageValues(
            ConstructionSite site, double constructionSkill) throws Exception {
        // TODO: Implement
        return null;
    }
}
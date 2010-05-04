/**
 * Mars Simulation Project
 * ConstructionStage.java
 * @version 2.85 2008-08-23
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.construction;

import java.io.Serializable;

/**
 * A construction stage of a construction site.
 */
public class ConstructionStage implements Serializable {

    // Construction site events.
    public static final String ADD_CONSTRUCTION_WORK_EVENT = "adding construction work";
    public static final String ADD_SALVAGE_WORK_EVENT = "adding salvage work";
    
    // Data members
    private ConstructionStageInfo info;
    private ConstructionSite site;
    private double completedWorkTime;
    private boolean isSalvaging;
    
    /**
     * Constructor
     * @param info the stage information.
     */
    public ConstructionStage(ConstructionStageInfo info, ConstructionSite site) {
        this.info = info;
        this.site = site;
        completedWorkTime = 0D;
        isSalvaging = false;
    }
    
    /**
     * Get the construction stage information.
     * @return stage information.
     */
    public ConstructionStageInfo getInfo() {
        return info;
    }
    
    /**
     * Gets the completed work time on the stage.
     * @return work time (in millisols).
     */
    public double getCompletedWorkTime() {
        return completedWorkTime;
    }
    
    /**
     * Sets the completed work time on the stage.
     * @param completedWorkTime work time (in millisols).
     */
    public void setCompletedWorkTime(double completedWorkTime) {
        this.completedWorkTime = completedWorkTime;
    }
 
    /**
     * Adds work time to the construction stage.
     * @param workTime the work time (in millisols) to add.
     */
    public void addWorkTime(double workTime) {
        completedWorkTime += workTime;
        if (completedWorkTime > info.getWorkTime())
            completedWorkTime = info.getWorkTime();
        
        // Fire construction event
        if (isSalvaging) site.fireConstructionUpdate(ADD_SALVAGE_WORK_EVENT, this);
        else site.fireConstructionUpdate(ADD_CONSTRUCTION_WORK_EVENT, this);
    }
    
    /**
     * Checks if the stage is complete.
     * @return true if stage is complete.
     */
    public boolean isComplete() {
        return (completedWorkTime == info.getWorkTime());
    }
    
    /**
     * Checks if the stage is salvaging.
     * @return true if stage is salvaging.
     */
    public boolean isSalvaging() {
        return isSalvaging;
    }
    
    /**
     * Sets if the stage is salvaging.
     * @param isSalvaging true if staging is salvaging.
     */
    public void setSalvaging(boolean isSalvaging) {
        this.isSalvaging = isSalvaging;
    }
    
    @Override
    public String toString() {
        String result = "";
        if (isSalvaging) result = "salvaging " + info.getName();
        else result = "constructing " + info.getName();
        return result;
    }
}
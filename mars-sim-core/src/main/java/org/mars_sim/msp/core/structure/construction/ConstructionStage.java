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
    
    // Data members
    private ConstructionStageInfo info;
    private ConstructionSite site;
    private double completedWorkTime;
    
    /**
     * Constructor
     * @param info the stage information.
     */
    public ConstructionStage(ConstructionStageInfo info, ConstructionSite site) {
        this.info = info;
        this.site = site;
        completedWorkTime = 0D;
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
     * Adds work time to the construction stage.
     * @param workTime the work time (in millisols) to add.
     */
    public void addWorkTime(double workTime) {
        completedWorkTime += workTime;
        if (completedWorkTime > info.getWorkTime())
            completedWorkTime = info.getWorkTime();
        
        // Fire construction event
        site.fireConstructionUpdate(ADD_CONSTRUCTION_WORK_EVENT, this);
    }
    
    /**
     * Checks if the stage is complete.
     * @return true if stage is complete.
     */
    public boolean isComplete() {
        return (completedWorkTime == info.getWorkTime());
    }
    
    @Override
    public String toString() {
        return info.getName();
    }
}
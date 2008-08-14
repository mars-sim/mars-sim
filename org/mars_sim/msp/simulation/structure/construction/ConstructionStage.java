/**
 * Mars Simulation Project
 * ConstructionStage.java
 * @version 2.85 2008-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.construction;

import java.io.Serializable;

/**
 * A construction stage of a construction site.
 */
public class ConstructionStage implements Serializable {

    // Data members
    private ConstructionStageInfo info;
    private double completedWorkTime;
    
    /**
     * Constructor
     * @param info the stage information.
     */
    ConstructionStage(ConstructionStageInfo info) {
        this.info = info;
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
    }
    
    /**
     * Checks if the stage is complete.
     * @return true if stage is complete.
     */
    public boolean isComplete() {
        return (completedWorkTime == info.getWorkTime());
    }
}
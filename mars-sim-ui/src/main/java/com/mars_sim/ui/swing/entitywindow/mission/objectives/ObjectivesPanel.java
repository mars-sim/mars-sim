/*
 * Mars Simulation Project
 * ObjectivesPanel.java
 * @date 2025-06-22
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.mission.objectives;

/**
 * The is an Objective Panel that has extra connection/registeration
 */
public interface ObjectivesPanel {

    /**
     * Called when the objectives panel is no longer needed.
     */
    void unregister();

}

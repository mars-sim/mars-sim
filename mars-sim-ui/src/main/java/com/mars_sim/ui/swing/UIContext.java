/*
 * Mars Simulation Project
 * UIContext.java
 * @date 2025-11-12
 * @author Scott Davis
 */
package com.mars_sim.ui.swing;

import java.io.Serializable;

import javax.swing.JFrame;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Simulation;

/** 
 * Context interface for UI components to interact with the UI and simulation.
 * This is a Work In Progress and will be refactor once all Tools converted to ContentPanel.
 */
public interface UIContext extends Serializable {

    /**
     * Show the details for an Entity object.
     * @param entity
     * @return
     */
    void showDetails(Entity entity);

    /**
     * Open a Tool window by name.
     * @param name
     * @return
     */
    ContentPanel openToolWindow(String name);

    /**
     * Get the Simulation monitored by this UI
     * @return
     */
    Simulation getSimulation();

    /**
     * Get the top-level JFrame for this UI. This is used to host dialogs.
     * @return
     */
    JFrame getTopFrame();
}

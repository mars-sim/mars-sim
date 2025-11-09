/*
 * Mars Simulation Project
 * ContentPanel.java
 * @date 2025-11-09
 * @author Barry Evans
 */
package com.mars_sim.ui.swing;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.mars_sim.core.time.ClockPulse;

/**
 * This is a panel that displays content relavent to the Simulation.
 */
public class ContentPanel extends JPanel{

    private String title;

    protected ContentPanel(String title) {
        this.title = title;

        setLayout(new BorderLayout());
    }

    /**
     * Get the title of the content panel
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * Updates content panel with clock pulse information.
     * @param pulse
     */
    public void update(ClockPulse pulse) {
        //  By default do nothing
    }

    /**
     * Prepares content panel for deletion as teh window is closed down.
     * This is to be overridden by particular content panels as needed.
     */
    public void destroy() {
        // By default do nothing
    }

}

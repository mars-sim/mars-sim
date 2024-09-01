/*
 * Mars Simulation Project
 * ConfigurableWindow.java
 * @date 2023-03-15
 * @author Barry Evans
 */
package com.mars_sim.ui.swing;

import java.util.Properties;

/**
 * Represents a window that user configurable properties.
 */
public interface ConfigurableWindow {

    /**
     * Generates the configurable properties of this window
     */
    public Properties getUIProps();

}

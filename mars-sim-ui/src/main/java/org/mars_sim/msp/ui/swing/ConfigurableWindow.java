/*
 * Mars Simulation Project
 * ConfigurableWindow.java
 * @date 2023-03-15
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing;

import java.util.Properties;

/**
 * Represents a window that user configurable properties.
 */
public interface ConfigurableWindow {

    /**
     * Generate the configurable properties of this window
     */
    public Properties getUIProps();

}

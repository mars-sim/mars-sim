/*
 * Mars Simulation Project
 * ContentManager.java
 * @date 2026-03-17
 * @author Barry Evans
 */
package com.mars_sim.ui.swing;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;

import com.mars_sim.ui.swing.UIConfig.WindowSpec;
import com.mars_sim.ui.swing.sound.AudioPlayer;
import com.mars_sim.ui.swing.terminal.MarsTerminal;

/**
 * This represents the content manager for the main window. It provides methods to get the properties of all UI elements and the details of all content windows currently open on the desktop.
 * This is used to save the UI configuration.
 * It provides an interface to the shared main UI components, such as the Mars Terminal and the content windows and UIConfig.
 */
public interface ContentManager {

    /**
     * Get the MarsTerminal instance. This is used to save the UI configuration.
     * THis will be replaced once the Mars Terminal becomes a Content Panel.
     * @return The MarsTerminal instance.
     */
    MarsTerminal getMarsTerminal();

    /**
     * Get the properties of all UI elements. This is used to save the UI configuration.
     * @return A map of UI element names to their properties.
     */
    Map<String, Properties> getUIProps();

    /**
	 * Get the details of all content windows currently open on the desktop. This is used to save the UI configuration.
	 * @return
	 */
	List<WindowSpec> getContentSpecs();

    /**
     * Get the UIConfig for this UI. This is used to save the UI configuration.
     * @return
     */
    UIConfig getConfig();


    /**
     * Get the top-level frame of the main window. This is used to save the UI configuration.
     * @return Top lewvel main frame.
     */
    JFrame getTopFrame();

    /**
     * Shutdown the UI. This is used to save the UI configuration and close all UI elements.
     * It is a method of no return.
     */
    void shutdown();

    /**
     * Get the AudioPlayer instance. 
     * @return The AudioPlayer instance.
     */
    AudioPlayer getAudio();
}

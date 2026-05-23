/*
 * Mars Simulation Project
 * StatefulComponent.java
 * @date 2026-05-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

/**
 * An interface for components that have a state and need to be cleaned up before being removed from the UI.
 * It is just a marker interface with a single method to clean up the component. 
 */
public interface StatefulComponent {

    /**
     * Cleans up the component for removal from the UI. This method should be called before removing the component from the UI
     * to ensure that any resources are properly released and any listeners are unregistered.
     */
    void cleanUp();
}

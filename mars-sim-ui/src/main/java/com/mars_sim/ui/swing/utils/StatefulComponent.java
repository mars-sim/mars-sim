/*
 * Mars Simulation Project
 * StatefulComponent.java
 * @date 2026-05-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

/**
 * A cleanup/lifecycle interface for components that have a state and need to be cleaned up before being removed from the UI.
 * Implementers must provide a {@link #release()} method to properly release resources and unregister listeners. 
 */
public interface StatefulComponent {

    /**
     * Cleans up the component for removal from the UI. This method should be called before removing the component from the UI
     * to ensure that any resources are properly released and any listeners are unregistered.
     */
    void release();
}

/*
 * Mars Simulation Project
 * MonitorableEntity.java
 * @date 2025-12-06
 * @author Barry Evans
 */
package com.mars_sim.core;

/**
 * An Entity that supports event listeners for monitoring changes.
 * This interface allows UI components and other observers to register
 * for notifications when the entity's state changes.
 */
public interface MonitorableEntity extends Entity {
    
    /**
     * Adds an entity listener.
     * 
     * @param newListener the listener to add.
     */
    void addEntityListener(EntityListener newListener);
    
    /**
     * Removes an entity listener.
     * 
     * @param oldListener the listener to remove.
     */
    void removeEntityListener(EntityListener oldListener);
}

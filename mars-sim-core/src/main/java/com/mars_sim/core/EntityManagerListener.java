/*
 * Mars Simulation Project
 * EntityManagerListener.java
 * @date 2025-12-14
 * @author Barry Evans
 */
package com.mars_sim.core;

/**
 * Listener interface for the adding/removing of Entities. The concept of an Entity Manager
 * is not a concrete class, but any class that manages a collection of Entities may elect to
 * use this interface as a means to communicate changes to the collection.
 */
public interface EntityManagerListener {

    /**
     * An Entity has been added to the parent manager
     * @param newEntity
     */
    void entityAdded(Entity newEntity);

    /**
     * An Entity has been removed from the parent manager
     * @param removedEntity
     */
    void entityRemoved(Entity removedEntity);
}

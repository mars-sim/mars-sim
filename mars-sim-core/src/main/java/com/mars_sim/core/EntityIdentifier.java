/*
 * Mars Simulation Project
 * EntityIdentifier.java
 * @date 2026-05-10
 * @author Barry Evans
 */
package com.mars_sim.core;

/**
 * Represents a unique identifier for an entity in the Mars Simulation Project. 
 */
public record EntityIdentifier(String type, String id, String parentId) {

    public EntityIdentifier(String type, String id) {
        this(type, id, null);
    }
}

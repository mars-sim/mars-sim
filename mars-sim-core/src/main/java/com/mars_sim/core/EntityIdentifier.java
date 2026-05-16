/*
 * Mars Simulation Project
 * Entity.java
 * @date 2026-05-10
 * @author Barry Evans
 */
package com.mars_sim.core;

/**
 * Represents a unique identifier for an entity in the Mars Simulation Project. 
 */
public record EntityIdentifier(String type, String id) {
}

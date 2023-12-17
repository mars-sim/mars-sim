/*
 * Mars Simulation Project
 * NamedPosition.java
 * @date 2023-12-08
 * @author Barry Evans
 */
package com.mars_sim.core.structure.building;

import com.mars_sim.mapdata.location.LocalPosition;

/**
 * Represents a LocalPosition with an associated name.
 */
public record NamedPosition(String name, LocalPosition position) {}

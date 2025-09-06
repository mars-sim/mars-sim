/*
 * Mars Simulation Project
 * NamedPosition.java
 * @date 2023-12-08
 * @author Barry Evans
 */
package com.mars_sim.core.building;

import com.mars_sim.core.configuration.RelativePosition;

/**
 * Represents a relative position in a Building with an associated name.
 */
public record NamedPosition(String name, RelativePosition position) {}

package com.mars_sim.core.manufacture;

import java.io.Serializable;

/**
 * Tooling used in manufacturing processes.
 */
public record Tooling(String name, String description) implements Serializable {}


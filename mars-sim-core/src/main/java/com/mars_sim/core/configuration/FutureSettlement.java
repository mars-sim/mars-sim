/*
 * Mars Simulation Project
 * FutureSettlement.java
 * @date 2026-03-04
 * @author Barry Evans
 */
package com.mars_sim.core.configuration;

import com.mars_sim.core.map.location.Coordinates;

/**
 * Represents a future settlement defined in a scenario.
 */
public record FutureSettlement(String name, String template, String sponsorCode,
			int arrivalSols, Coordinates landingLocation,
			int populationNum, int numOfRobots) {}

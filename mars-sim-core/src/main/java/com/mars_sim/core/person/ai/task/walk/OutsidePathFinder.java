/*
 * Mars Simulation Project
 * OutsidePathFinder.java
 * @date 2025-10-04
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.walk;

import java.util.List;

import com.mars_sim.core.map.location.LocalPosition;

/**
 * Interface for path finding outside a settlement or vehicle.
 */
public interface OutsidePathFinder {

    /**
     * A record class to hold the path solution
     */
    public record PathSolution(List<LocalPosition> path, boolean obstaclesInPath) {}

    /**
     * Determines the outside walking path, avoiding obstacles as necessary.
     *
     * @param destination the destination location.
     * @return walking path as list of X,Y locations.
     */
    PathSolution determineWalkingPath(LocalPosition destination);
}

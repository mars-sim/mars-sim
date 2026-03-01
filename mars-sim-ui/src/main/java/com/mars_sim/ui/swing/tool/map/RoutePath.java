/*
 * Mars Simulation Project
 * RoutePath.java
 * @date 2026-02-16
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.map;

import java.util.List;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.SurfacePOI;

/**
 * Interface representing a route path, which consists of a starting point and a list of navpoints on the RoutePathLayer.
 */
public interface RoutePath {
    /**
     * Context for the route path, which can be used in tooltips.
     * @return
     */
    String getContext();

    /**
     * Starting coordinates of the route path.
     * @return
     */
    Coordinates getStart();

    /**
     * List of navpoints on the route path.
     * @return List could be zero elements.
     */
    List<? extends SurfacePOI> getNavpoints();
}

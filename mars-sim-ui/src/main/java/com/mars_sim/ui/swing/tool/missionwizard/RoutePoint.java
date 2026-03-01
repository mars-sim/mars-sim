/*
 * Mars Simulation Project
 * RoutePoint.java
 * @date 2026-02-16
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.missionwizard;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.SurfacePOI;

/**
 * Represents a single point in a proposed route, containing coordinates,
 * leg distance, and cumulative total distance.
 * It implements SurfacePOI so it can be displayed as a point of interest on the map.
 */
public class RoutePoint implements SurfacePOI {
    
    private final String name;
    private final Coordinates coordinates;
    private final double legDistance;  // Distance from previous point to this point
    private final double totalDistance; // Cumulative distance from start
    
    /**
     * Constructor for a route leg.
     * 
     * @param name A name or label for this route point (e.g. "Waypoint 1")
     * @param coordinates The location of this leg endpoint
     * @param legDistance The distance from the previous leg endpoint to this point
     * @param totalDistance The cumulative distance from the start of the route
     */
    public RoutePoint(String name, Coordinates coordinates, double legDistance, double totalDistance) {
        this.name = name;
        this.coordinates = coordinates;
        this.legDistance = legDistance;
        this.totalDistance = totalDistance;
    }
    
    /**
     * Gets the coordinates of this leg endpoint.
     * 
     * @return the coordinates
     */
    @Override
    public Coordinates getCoordinates() {
        return coordinates;
    }
    
    /**
     * Gets the name or label for this route point.
     * @return the name of this route point
     */
    @Override
    public String getName() {
        return name;
    }
    
    /**
     * Gets the distance of this leg from the previous waypoint.
     * 
     * @return the leg distance in km
     */
    public double getLegDistance() {
        return legDistance;
    }
    
    /**
     * Gets the total cumulative distance from the start of the route to this leg.
     * 
     * @return the total distance in km
     */
    public double getTotalDistance() {
        return totalDistance;
    }
}

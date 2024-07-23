/*
 * Mars Simulation Project
 * MineralMap.java
 * @date 2022-07-14
 * @author Scott Davis
 */

package com.mars_sim.core.environment;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.mars_sim.mapdata.location.Coordinates;

/**
 * Interface for mineral maps of Mars.
 */
public interface MineralMap extends Serializable {

	/**
	 * Gets the mineral concentration at a given location.
	 * 
	 * @param mineralType the mineral type (see MineralMap.java)
	 * @param location    the coordinate location.
	 * @return percentage concentration (0 to 100.0)
	 */
	public double getMineralConcentration(String mineralType, Coordinates location);

	/**
	 * Gets all of the mineral concentrations at a given location.
	 * 
	 * @param location the coordinate location.
	 * @return map of mineral types and percentage concentration (0 to 100.0)
	 */
	public Map<String, Integer> getAllMineralConcentrations(Coordinates location);
	
	/**
	 * Gets all of the mineral concentrations at a given location.
	 * 
	 * @param mineralsDisplaySet 	a set of mineral strings.
	 * @param location 				the coordinate location.
	 * @param mag					the magnification
	 * @return map of mineral types and percentage concentration (0 to 100.0)
	 */
	public Map<String, Double> getSomeMineralConcentrations(Set<String> mineralsDisplaySet, Coordinates location, double mag);

	/**
	 * Creates mineral concentrations at a given location.
	 * 
	 * @param location the coordinate location.
	 */
	public void createLocalConcentration(Coordinates location);
	
	/**
	 * Gets an array of all mineral type names.
	 * 
	 * @return array of name strings.
	 */
	public String[] getMineralTypeNames();

	
	/**
	 * Generates a set of Mineral locations from a starting location.
	 * 
	 * @param startingLocation
	 * @param range
	 * @return
	 */
	public Set<Coordinates> generateMineralLocations(Coordinates startingLocation, double range);
	
	/**
	 * Finds a random location with mineral concentrations from a starting location.
	 * and within a distance range.
	 * 
	 * @param startingLocation the starting location
	 * @param range            the distance range (km)
	 * @param sol
	 * @return location with one or more mineral concentrations or null if none
	 *         found.
	 */
	public Coordinates findRandomMineralLocation(Coordinates startingLocation, double range, int sol);

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy();
}

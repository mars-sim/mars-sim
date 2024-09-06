/*
 * Mars Simulation Project
 * MineralMap.java
 * @date 2024-08-30
 * @author Scott Davis
 */

package com.mars_sim.core.environment;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.map.location.Coordinates;

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
	 * @param phi
	 * @param theta
	 * @param mag					the magnification
	 * @return map of mineral types and percentage concentration (0 to 100.0)
	 */
	public Map<String, Integer> getSomeMineralConcentrations(Set<String> mineralsDisplaySet, double phi, double theta, double mag);

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
	 * Gets the color string of a mineral.
	 * 
	 * @param mineralName
	 * @return
	 */
	public String getColorString(String mineralTypeName);
	
	/**
	 * Finds a random location with mineral concentrations from a starting location.
	 * and within a distance range.
	 * 
	 * @param startingLocation the starting location
	 * @param range            the distance range (km)
	 * @param sol
	 * @param foundLocations
	 * @return location and distance pair
	 */
	public Map.Entry<Coordinates, Double> findRandomMineralLocation(Coordinates startingLocation, double range, int sol, 
			Collection<Coordinates> foundLocations);

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy();
}

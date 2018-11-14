/**
 * Mars Simulation Project
 * MineralMap.java
 * @version 3.1.0 2017-08-08
 * @author Scott Davis
 */

package org.mars_sim.msp.core.mars;

import org.mars_sim.msp.core.Coordinates;

import java.util.Map;

/**
 * Interface for mineral maps of Mars.
 */
public interface MineralMap {

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
	public Map<String, Double> getAllMineralConcentrations(Coordinates location);

	/**
	 * Gets an array of all mineral type names.
	 * 
	 * @return array of name strings.
	 */
	public String[] getMineralTypeNames();

	/**
	 * Finds a random location with mineral concentrations from a starting location
	 * and within a distance range.
	 * 
	 * @param startingLocation the starting location.
	 * @param range            the distance range (km).
	 * @return location with one or more mineral concentrations or null if none
	 *         found.
	 */
	public Coordinates findRandomMineralLocation(Coordinates startingLocation, double range);

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy();
}
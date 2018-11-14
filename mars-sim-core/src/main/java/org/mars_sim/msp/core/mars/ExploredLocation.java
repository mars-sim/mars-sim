/**
 * Mars Simulation Project
 * ExploredLocation.java
 * @version 3.1.0 2017-08-08
 * @author Scott Davis
 */

package org.mars_sim.msp.core.mars;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.structure.Settlement;

import java.io.Serializable;
import java.util.Map;

/**
 * A class representing an explored location. It contains information on
 * estimated mineral concentrations and if it has been mined or not. Perhaps
 * later we can add more information related to exploration, such as evidence
 * for life.
 */
public class ExploredLocation implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Private members.
	private Coordinates location;
	private Map<String, Double> estimatedMineralConcentrations;
	private boolean mined;
	private boolean explored;
	private boolean reserved;
	private Settlement settlement;
	private int numEstimationImprovement;

	/**
	 * Constructor
	 * 
	 * @param location                       the location coordinates.
	 * @param estimatedMineralConcentrations a map of all mineral types and their
	 *                                       estimated concentrations (0% -100%)
	 * @param the                            settlement the exploring mission is
	 *                                       from.
	 */
	ExploredLocation(Coordinates location, Map<String, Double> estimatedMineralConcentrations, Settlement settlement) {
		this.location = new Coordinates(location);
		this.estimatedMineralConcentrations = estimatedMineralConcentrations;
		this.settlement = settlement;
		mined = false;
		explored = false;
		reserved = false;
		numEstimationImprovement = 0;
	}

	/**
	 * Gets the location coordinates.
	 * 
	 * @return coordinates.
	 */
	public Coordinates getLocation() {
		return location;
	}

	/**
	 * Gets a map of estimated mineral concentrations at the location.
	 * 
	 * @return a map of all mineral types and their estimated concentrations (0%
	 *         -100%)
	 */
	public Map<String, Double> getEstimatedMineralConcentrations() {
		return estimatedMineralConcentrations;
	}

	/**
	 * Get the number of times the mineral concentration estimation has been
	 * improved.
	 * 
	 * @return number of times.
	 */
	public int getNumEstimationImprovement() {
		return numEstimationImprovement;
	}

	/**
	 * Add an mineral concentration estimation improvement.
	 */
	public void addEstimationImprovement() {
		numEstimationImprovement++;
	}

	/**
	 * Sets if the location has been mined or not.
	 * 
	 * @param mined true if mined.
	 */
	public void setMined(boolean mined) {
		this.mined = mined;
	}

	/**
	 * Checks if the location has been mined or not.
	 * 
	 * @return true if mined.
	 */
	public boolean isMined() {
		return mined;
	}

	/**
	 * Sets if the location has been explored or not.
	 * 
	 * @param explored true if explored.
	 */
	public void setExplored(boolean explored) {
		this.explored = explored;
	}

	/**
	 * Checks if the location has been explored of not.
	 * 
	 * @return true if explored.
	 */
	public boolean isExplored() {
		return explored;
	}

	/**
	 * Sets if the location has been reserved for mining.
	 * 
	 * @param reserved true if reserved.
	 */
	public void setReserved(boolean reserved) {
		this.reserved = reserved;
	}

	/**
	 * Checks if the location has been reserved for mining.
	 * 
	 * @return true if reserved.
	 */
	public boolean isReserved() {
		return reserved;
	}

	/**
	 * The settlement that explored this site.
	 * 
	 * @return settlement
	 */
	public Settlement getSettlement() {
		return settlement;
	}
}
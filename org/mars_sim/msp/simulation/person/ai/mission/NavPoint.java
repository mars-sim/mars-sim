/**
 * Mars Simulation Project
 * RoverMission.java
 * @version 2.78 2005-09-11
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.mission;

import java.io.Serializable;
import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.structure.Settlement;

/**
 * A navigation point for travel missions.
 */
public class NavPoint implements Serializable {
	
	// Data members.
	private Coordinates location; // The location of the navpoint.
	private Settlement settlement; // The settlement at this navpoint.

	/**
	 * Constructor with location.
	 * @param location the location of the navpoint.
	 */
	public NavPoint(Coordinates location) {
		if (location == null) throw new IllegalArgumentException("location is null");
		this.location = new Coordinates(location);
	}

	/**
	 * Constructor with location and settlement.
	 * @param location the location of the navpoint.
	 * @param settlement the settlement at the navpoint.
	 */
	public NavPoint(Coordinates location, Settlement settlement) {
		this(location);
		if (settlement == null) throw new IllegalArgumentException("settlement is null");
		this.settlement = settlement;
	}
	
	/**
	 * Gets the location of this navpoint.
	 * @return the coordinate location.
	 */
	public Coordinates getLocation() {
		return new Coordinates(location);
	}
	
	/**
	 * Gets the settlement at the navpoint.
	 * @return the settlement or null if none.
	 */
	public Settlement getSettlement() {
		return settlement;
	}
	
	/**
	 * Checks if there is a settlement at this navpoint.
	 * @return true if settlement.
	 */
	public boolean isSettlementAtNavpoint() {
		return (settlement != null);
	}
	
	/**
	 * Checks if this NavPoint is the same as another object.
	 * @return true if the same navpoint.
	 */
	public boolean equals(Object object) {
		boolean result = false;
		if (object instanceof NavPoint) {
			NavPoint otherNavpoint = (NavPoint) object;
			if (getLocation().equals(otherNavpoint.getLocation())) {
				if (isSettlementAtNavpoint()) {
					if (getSettlement().equals(otherNavpoint.getSettlement())) result = true;
				}
				else result = true;
			}
		}
		return result;
	}
}
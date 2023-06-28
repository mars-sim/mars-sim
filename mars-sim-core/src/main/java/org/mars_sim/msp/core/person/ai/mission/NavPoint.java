/**
 * Mars Simulation Project
 * RoverMission.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A navigation point for travel missions.
 */
public class NavPoint implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	/** The location of the navpoint. */
	private Coordinates location;
	/** The settlement at this navpoint. */
	private Settlement settlement;
	/** The description of the navpoint. */
	private String description;
	
	private double distance;

	/**
	 * Constructor with location.
	 * 
	 * @param location    the location of the navpoint.
	 * @param description the navpoint description.
	 * @param start Starting point
	 */
	public NavPoint(Coordinates location, String description, Coordinates start) {
		if (location == null)
			throw new IllegalArgumentException("location is null");
		this.location = location;
		this.description = description;

		if (start != null) {
			distance = location.getDistance(start);
		}
		else {
			distance = 0;
		}
	}

	/**
	 * Constructor with settlement.
	 * 
	 * @param settlement  the settlement at the navpoint.
	 * @param start Starting point
	 */
	public NavPoint(Settlement settlement, Coordinates start) {
		this(settlement.getCoordinates(), settlement.getName(), start);
		this.settlement = settlement;
	}

	/**
	 * Gets the location of this navpoint.
	 * 
	 * @return the coordinate location.
	 */
	public Coordinates getLocation() {
		return location;
	}

	/**
	 * Distance to travel to reach this point from the previous
	 */
    public double getDistance() {
        return distance;
    }

	/**
	 * Gets the description of the navpoint.
	 * 
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the settlement at the navpoint.
	 * 
	 * @return the settlement or null if none.
	 */
	public Settlement getSettlement() {
		return settlement;
	}

	/**
	 * Checks if there is a settlement at this navpoint.
	 * 
	 * @return true if settlement.
	 */
	public boolean isSettlementAtNavpoint() {
		return (settlement != null);
	}

	/**
	 * Checks if this NavPoint is the same as another object.
	 * 
	 * @return true if the same navpoint.
	 */
	public boolean equals(Object object) {
		boolean result = false;
		if (object instanceof NavPoint) {
			NavPoint otherNavpoint = (NavPoint) object;
			if (getLocation().equals(otherNavpoint.getLocation())) {
				if (isSettlementAtNavpoint()) {
					if (settlement.equals(otherNavpoint.settlement))
						result = true;
				} else
					result = true;
			}
		}
		return result;
	}

	/**
	 * Gets the hash code for this object.
	 * 
	 * @return hash code.
	 */
	public int hashCode() {
		return location.hashCode();
	}
	
	@Override
	public String toString() {
		return description + " @ " + location.getFormattedString();
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		// take care to avoid null exceptions
		settlement = null;
		location = null;
	}
}

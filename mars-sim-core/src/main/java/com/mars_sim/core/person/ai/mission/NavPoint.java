/*
 * Mars Simulation Project
 * RoverMission.java
 * @date 2024-08-01
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.io.Serializable;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.core.structure.Settlement;

/**
 * A navigation point for travel missions.
 */
public class NavPoint implements Serializable, SurfacePOI {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	/** The location of the navpoint. */
	private Coordinates location;
	/** The settlement at this navpoint. */
	private Settlement settlement;
	/** The description of the navpoint. */
	private String description;
	
	private double point2PointDistance;

	private double actualTravelled;
	
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
			point2PointDistance = location.getDistance(start);
		}
		else {
			point2PointDistance = 0;
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
	@Override
	public Coordinates getCoordinates() {
		return location;
	}

	/**
	 * Gets the map point-to-point distance between this point and the previous.
	 */
    public double getPointToPointDistance() {
        return point2PointDistance;
    }


	/**
	 * Gets the actual travelled distance between this point and the previous.
	 */
    public double getActualTravelled() {
        return actualTravelled;
    }

	/**
	 * Adds the actual travelled distance between this point and the previous.
	 */
    public void addActualTravelled(double dist) {
        actualTravelled += dist;
    }
    
	/**
	 * Gets the name of the navpoint. This is the same as the description.
	 * 
	 * @return the name of the navpoint.
	 */
    @Override
	public String getName() {
		return getDescription();
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
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		// take care to avoid null exceptions
		settlement = null;
		location = null;
	}
}

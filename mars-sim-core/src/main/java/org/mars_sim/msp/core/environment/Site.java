/*
 * Mars Simulation Project
 * Site.java
 * @date 2021-12-09
 * @author Manny Kung
 */
package org.mars_sim.msp.core.environment;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;

public class Site implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

//	private static final Logger logger = Logger.getLogger(Site.class.getName());

	protected Coordinates location;
	// degree of uncertainty [in % ] of its content
	private double uncertainty;

	private double steepness = -1;

	private double elevation = -10_000;

	public Site (Coordinates location) {
		this.location = location;
	}

	public Coordinates getLocation() {
		return location;
	}

	public void setLocation(Coordinates location) {
		this.location = location;
	}

	public double getUncertainty() {
		return uncertainty;
	}

	public void setUncertainty(double uncertainty) {
		this.uncertainty = uncertainty;
	}

	public double getSteepness() {
		return steepness;
	}

	public void setSteepness(double steepness) {
		this.steepness = steepness;
	}

	public double getElevation() {
		return elevation;
	}

	public void setElevation(double elevation) {
		this.elevation = elevation;
	}

	/**
	 * Returns true if the collection site have the same coordinates
	 *
	 * @param o
	 * @return true if matched, false otherwise
	 */
	public boolean equals(Object o) {
		if (this == o) return true;
		if ((o != null) && (o instanceof Site)) {
			Site s = (Site) o;
            return this.location.equals(s.getLocation())
                    && this.steepness == s.getSteepness()
                    && this.elevation == s.getElevation();
		}

		return false;
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	public int hashCode() {
		return (int)(Math.abs(steepness) + Math.abs(elevation)) + location.hashCode();
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		location = null;
	}
}

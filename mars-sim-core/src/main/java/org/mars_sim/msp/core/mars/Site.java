/**
 * Mars Simulation Project
 * Site.java
 * @version 3.1.0 2019-10-29
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import org.mars_sim.msp.core.Coordinates;

public class Site {

	private Coordinates location;
	// degree of uncertainty [in % ] of its content
	private double uncertainty;
	
	private double steepness;
	
	private double elevation;
	
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
	
	
	
}

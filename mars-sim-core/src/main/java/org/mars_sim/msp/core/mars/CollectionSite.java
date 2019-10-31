/**
 * Mars Simulation Project
 * CollectionSite.java
 * @version 3.1.0 2019-10-29
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import org.mars_sim.msp.core.Coordinates;

public class CollectionSite extends Site {

	private double iceCollectionRate;
	
	private double regolithCollectionRate;
	
	private double estimatedIceVolume;
	
	public CollectionSite(Coordinates location) {
		super(location);
	}

	public double getIceCollectionRate() {
		return iceCollectionRate;
	}

	public void setIceCollectionRate(double iceCollectionRate) {
		this.iceCollectionRate = iceCollectionRate;
	}

	public double getRegolithCollectionRate() {
		return regolithCollectionRate;
	}

	public void setRegolithCollectionRate(double regolithCollectionRate) {
		this.regolithCollectionRate = regolithCollectionRate;
	}

	public double getEstimatedIceVolume() {
		return estimatedIceVolume;
	}

	public void setEstimatedIceVolume(double estimatedVolume) {
		this.estimatedIceVolume = estimatedVolume;
	}

}

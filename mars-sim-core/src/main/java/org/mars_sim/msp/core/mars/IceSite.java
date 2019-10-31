/**
 * Mars Simulation Project
 * IceSite.java
 * @version 3.1.0 2019-10-29
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import org.mars_sim.msp.core.Coordinates;

public class IceSite extends Site {

	private double iceCollectionRate;
	
	private double estimatedVolume;
	
	public IceSite(Coordinates location) {
		super(location);
	}

	public double getIceCollectionRate() {
		return iceCollectionRate;
	}

	public void setIceCollectionRate(double iceCollectionRate) {
		this.iceCollectionRate = iceCollectionRate;
	}

	public double getEstimatedVolume() {
		return estimatedVolume;
	}

	public void setEstimatedVolume(double estimatedVolume) {
		this.estimatedVolume = estimatedVolume;
	}

}

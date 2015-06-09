/**
 * Mars Simulation Project
 * RadiationEvent.java
 * @version 3.08 2015-06-08
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.time.MarsClock;

public class RadiationEvent {

	MarsClock clock;
	BodyRegionType region;
	double amount;

	public RadiationEvent(MarsClock clock, BodyRegionType region, double amount ) {
		this.clock = clock;
		this.region = region;
		this.amount = amount;
	}

	public double getAmount() {
		return amount;
	}

	public BodyRegionType getBodyRegion() {
		return region;
	}

}

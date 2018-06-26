/**
 * Mars Simulation Project
 * RadiationHit.java
 * @version 3.1.0 2017-08-31
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.health;

import java.io.Serializable;

import org.mars_sim.msp.core.person.BodyRegionType;
import org.mars_sim.msp.core.time.MarsClock;

public class RadiationEvent implements Serializable {

	/** default serial id.*/
	private static final long serialVersionUID = 1L;

	private MarsClock clock;
	private BodyRegionType region;
	private double amount;

	/**
	 * Create an radiation hit.
	 * @param clock {@link MarsClock}
	 * @param region {@link BodyRegionType} the body region affected.
	 * @param amount the dosage amount.
	 */
	public RadiationEvent(MarsClock clock, BodyRegionType region, double amount) {
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

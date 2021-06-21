/**
 * Mars Simulation Project
 * RadiationHit.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.health;

import java.io.Serializable;

import org.mars_sim.msp.core.person.BodyRegionType;

public class RadiationEvent implements Serializable {

	/** default serial id.*/
	private static final long serialVersionUID = 1L;

	private BodyRegionType region;
	private double amount;

	/**
	 * Create an radiation hit.
	 * @param region {@link BodyRegionType} the body region affected.
	 * @param amount the dosage amount.
	 */
	public RadiationEvent(BodyRegionType region, double amount) {
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

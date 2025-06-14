/**
 * Mars Simulation Project
 * CertificationType.java
 * @date 2023-11-09
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.training;

import com.mars_sim.core.tool.Msg;

public enum CertificationType {

	EVA, LAB, SAFETY, PROSPECTING;
	
	private String name;

	/** Hidden Constructor. */
	private CertificationType() {
        this.name = Msg.getStringOptional("CertificationType", name());
	}

	public final String getName() {
		return this.name;
	}
}

/**
 * Mars Simulation Project
 * CertificationType.java
 * @date 2023-11-09
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.training;

import com.mars_sim.core.tool.Msg;

public enum CertificationType {

	EVA				(Msg.getString("CertificationType.eva")), //$NON-NLS-1$
	LAB				(Msg.getString("CertificationType.lab")), //$NON-NLS-1$
	SAFETY			(Msg.getString("CertificationType.safety")), //$NON-NLS-1$
	PROSPECTING		(Msg.getString("CertificationType.prospecting")), //$NON-NLS-1$
	;
	
	private String name;

	/** Hidden Constructor. */
	private CertificationType(String name) {
		this.name = name;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
}

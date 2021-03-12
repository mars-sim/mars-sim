/*
 * Mars Simulation Project
 * PhaseType.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;

/**
 * Describes a phase in a Crop development
 */
public class Phase implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
	
	private PhaseType phaseType;
	private double workRequired;
	private double percentGrowth;

	public Phase(PhaseType phaseType, double workRequired, double percentGrowth) {
		this.phaseType = phaseType;
		this.workRequired = workRequired;
		this.percentGrowth = percentGrowth;
	}
	
	public double getWorkRequired() {
		return workRequired;
	}

	public double getPercentGrowth() {
		return percentGrowth;
	}
	
	public PhaseType getPhaseType() {
		return phaseType;
	}
}

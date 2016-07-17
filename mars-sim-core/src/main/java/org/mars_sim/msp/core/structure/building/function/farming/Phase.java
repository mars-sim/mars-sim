/*
 * Mars Simulation Project
 * PhaseType.java
 * @version 3.1.0 2016-06-29
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;
import java.util.logging.Logger;

public class Phase implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Phase.class.getName());
	
	private PhaseType phaseType;
	private double workRequired;
	private double percentGrowth;
	private double harvestFactor;

	public Phase(PhaseType phaseType, double percentGrowth) {
		this.phaseType = phaseType;
		this.percentGrowth = percentGrowth;
	}
	
	public Phase(PhaseType phaseType, double workRequired, double percentGrowth) {
		this.phaseType = phaseType;
		this.workRequired = workRequired;
		this.percentGrowth = percentGrowth;
	}
	
	public void setHarvestFactor(double harvestFactor) {
		this.harvestFactor = harvestFactor;
		workRequired = harvestFactor * workRequired;
	}
	
	public void setWorkRequired(double value) {
		this.workRequired = value;
	}

	public void setPercentGrowth(double value) {
		this.percentGrowth = value;
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

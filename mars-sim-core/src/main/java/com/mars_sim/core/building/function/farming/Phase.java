/*
 * Mars Simulation Project
 * Phase.java
 * @date 2023-05-06
 * @author Barry Evans
 */

package com.mars_sim.core.building.function.farming;

import java.io.Serializable;

/**
 * Describes a phase in a Crop development
 */
public class Phase implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	private String name;
	private PhaseType phaseType;
	 /** The work needed [in sols] at this phase. */
	private double workRequired;
	private double percentGrowth;
	private double totalGrowth;


	Phase(String name, double workRequired, double percentGrowth, double totalGrowth) {
		this.name = name;
		this.phaseType = getAssociatedPhaseType(name);
		this.workRequired = workRequired;
		this.percentGrowth = percentGrowth;
		this.totalGrowth = totalGrowth;
	}

	/**
	 * See of this phase is one of the special pre-defined types
	 * @param phaseName
	 * @return
	 */
	static PhaseType getAssociatedPhaseType(String phaseName) {

		// Have to do it hardcoded to avoid exception throwing from valueOf method
		return switch (phaseName.toUpperCase()) {
			case "GERMINATION" -> PhaseType.GERMINATION;
			case "INCUBATION" -> PhaseType.INCUBATION;
			case "HARVESTING" -> PhaseType.HARVESTING;
			case "MATURATION" -> PhaseType.MATURATION;
			case "PLANTING" -> PhaseType.PLANTING;
			case "FINISHED" -> PhaseType.FINISHED;
			default -> PhaseType.OTHER;
		};
	}

	/**
	 * The unique name of the phase
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * The manual work required to complete this phase.
	 * @return
	 */
	public double getWorkRequired() {
		return workRequired;
	}

	/**
	 * The percent growth at this phase.
	 * @return
	 */
	public double getPercentGrowth() {
		return percentGrowth;
	}

	/**
	 * The cumulative percent growth by the end of this phase.
	 * @return
	 */
	public double getCumulativePercentGrowth() {
		return totalGrowth;
	}	

	/**
	 * Associated Phase type
	 * @return
	 */
	public PhaseType getPhaseType() {
		return phaseType;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Phase other = (Phase) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}

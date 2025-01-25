/*
 * Mars Simulation Project
 * CropCategory.java
 * @date 2023-05-06
 * @author Manny Kung
 */

package com.mars_sim.core.structure.building.function.farming;

import java.io.Serializable;
import java.util.List;

public class CropCategory implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String description;
	private boolean needsLight;

	private List<Phase> phases;
	private int numPhases;

	CropCategory(String name, String description, boolean needsLight, List<Phase> phases) {
		this.name = name;
		this.description = description;
		this.needsLight = needsLight;
		this.phases = phases;
		this.numPhases = phases.size();
	}	

	/**
	 * Name of the category
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Get the description of the crop category.
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Crop category needs light to grow.
	 * @return
	 */
	public boolean needsLight() {
		return needsLight;
	}

	/**
	 * What are the phases of this crop category.
	 * @return
	 */
	public List<Phase> getPhases() {
		return this.phases;
	}

	/**
	 * Gets the next phase in the growing sequence.
	 * 
	 * @param phaseType
	 * @return
	 */
	public Phase getNextPhase(Phase currentPhase) {

		PhaseType target = currentPhase.getPhaseType();
		for (int idx = 0; idx < numPhases-1; idx++) {
			if (phases.get(idx).getPhaseType() == target) {
				return phases.get(idx+1);
			}
		}
		return null;
	}
	
	/**
	 * Get the percentage of growth for the next phase.
	 * @param currentPhase
	 * @return
	 */
	public double getNextPhasePercentage(Phase currentPhase) {
		return getNextPhase(currentPhase).getPercentGrowth();
	}

	/**
	 * Gets the Phase for a specific PhaseType.
	 * 
	 * @param phaseType
	 * @return
	 */
	public Phase getPhase(PhaseType phaseType) {
		for (Phase entry : phases) {
			if (entry.getPhaseType() == phaseType) {
				return entry;
			}
		}
		throw new IllegalArgumentException("Phase type " + phaseType.getName() + " is not support in " + name);
	}

	@Override
	public final String toString() {
		return getName();
	}
}

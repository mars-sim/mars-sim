/*
 * Mars Simulation Project
 * CropCategory.java
 * @date 2023-05-06
 * @author Manny Kung
 */

package com.mars_sim.core.building.function.farming;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a category of crop. It contains a list of phases that the crop goes through
 * as well as other characteristics of the crop.
 */
public class CropCategory implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String description;
	private boolean needsLight;
	private double inGroundPercentage;
	private List<Phase> phases;
	private int numPhases;

	CropCategory(String name, String description, boolean needsLight, List<Phase> phases) {
		this.name = name;
		this.description = description;
		this.needsLight = needsLight;
		this.phases = phases;
		this.numPhases = phases.size();

		// Check mandatory phases are present
		var harvestPhase = getPhase(PhaseType.HARVESTING);
		if (harvestPhase == null) {
			throw new IllegalArgumentException(name + " must have a harvesting phase");
		}
		var plantPhase = getPhase(PhaseType.PLANTING);
		if (plantPhase == null) {
			throw new IllegalArgumentException(name + " must have a planting phase");
		}
		if (getPhase(PhaseType.FINISHED) == null) {
			throw new IllegalArgumentException(name + " must have a finished phase");
		}

		// In-gound percentage is total minus planting and harvesting
		inGroundPercentage = 100D - (harvestPhase.getPercentGrowth() + plantPhase.getCumulativePercentGrowth());
	}	

	/**
	 * Gets the name of the category.
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the description of the crop category.
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Does it needs light to grow ?
	 * 
	 * @return
	 */
	public boolean needsLight() {
		return needsLight;
	}

	/**
	 * What percentage of the cycle is in the ground increasing mass ?
	 * 
	 * @return
	 */
    public double getInGroundPercentage() {
        return inGroundPercentage;
    }

	/**
	 * What are the phases of this crop category ?
	 * 
	 * @return
	 */
	public List<Phase> getPhases() {
		return this.phases;
	}

	/**
	 * Gets the next phase in the growing sequence.
	 * 
	 * @param currentPhase Phase to get the next phase from.
	 * @return
	 */
	public Phase getNextPhase(Phase currentPhase) {

		int i = phases.indexOf(currentPhase);
		if (i+1 < numPhases) {
			return phases.get(i+1);
		}
		return null;
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
		throw new IllegalArgumentException("Phase type " + phaseType.name() + " is not support in " + name);
	}

	@Override
	public final String toString() {
		return getName();
	}
}

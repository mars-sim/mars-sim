/*
 * Mars Simulation Project
 * CropSpec.java
 * @date 2023-05-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;
import java.util.List;

import org.mars_sim.msp.core.resource.ResourceUtil;

/**
 * The CropSpec class is a type of crop.
 */
public class CropSpec implements Serializable, Comparable<CropSpec> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final int PERENNIAL = 0;
	private static final int ANNUAL = 1;
	private static final int BIENNIAL = 2;

	// Data members
	/** The id of this crop spec. */
	private int id;
	/** The length of the growing phase. */
	private double growingTime;
	/** The fresh basis edible biomass productivity [in gram per sq m per day] */
	private double edibleBiomass;
	/** The percentage of watet content */
	private double edibleWaterContent;
	/** The inedible biomass [in gram per sq m per day] */
	private double inedibleBiomass;
	/**
	 * The daily photosynthetically active radiation (PAR) or Daily Light Integral
	 * (DLI) [in moles per square meter per day, or mol/m^2/d]
	 */
	private double dailyPAR; // Note: not umol / m^2 / s // PAR is the instantaneous light with a wavelength
								// between 400 to 700 nm

	/** TODO The name of the crop spec should be internationalizable. */
	private String name;
	/** The life cycle type of this crop. */
	private String lifeCycle;
	/** THe phenological phases of this crop */
	private List<Phase> phases = null;
	/** The category of this crop */
	private CropCategory cropCategory;

	private int cropID;

	private int seedID = -1;

	private boolean seedOnly;

	/**
	 * Constructor.
	 *
	 * @param id               id of the crop.
	 * @param name             Name of the crop.
	 * @param growingTime        Length of growing phase for crop in millisols.
	 * @param cropCategory  The category of crop.
	 * @param lifeCycle
	 * @param edibleBiomass
	 * @param edibleWaterContent
	 * @param inedibleBiomass
	 * @param dailyPAR
	 * @param phases a list of phases
	 * @param seedName
	 * @param seedOnly
	 */
	CropSpec(int id, String name, double growingTime, CropCategory cropCategory, String lifeCycle,
			double edibleBiomass, double edibleWaterContent, double inedibleBiomass, double dailyPAR,
			List<Phase> phases, String seedName, boolean seedOnly) {

		this.id = id;
		this.name = name;
		this.growingTime = growingTime;
		this.cropCategory = cropCategory;
		this.lifeCycle = lifeCycle;
		this.edibleBiomass = edibleBiomass;
		this.edibleWaterContent = edibleWaterContent;
		this.inedibleBiomass = inedibleBiomass;
		this.dailyPAR = dailyPAR;
		this.phases = phases;

		this.cropID = ResourceUtil.findIDbyAmountResourceName(name);
		if (seedName != null) {
			this.seedID = ResourceUtil.findIDbyAmountResourceName(seedName);
			this.seedOnly = seedOnly;
		}
	}

	/**
	 * Gets the crop type's name.
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the Resource ID assigned to this crop.
	 * 
	 * @return
	 */
	public int getCropID() {
		return cropID;
	}

	/**
	 * Gets the Resource ID assigned to the seed of the crop.
	 * 
	 * @return
	 */
	public int getSeedID() {
		return seedID;
	}

	/**
	 * Does this crop only produce a the seed ?
	 */
	public boolean isSeedPlant() {
		return seedOnly;
	}

	/**
	 * Gets the crop type's life cycle type.
	 *
	 * @return type of life cycle
	 */
	public int getLifeCycleType() {
		int type = -1;
		if (lifeCycle.equalsIgnoreCase("Annual"))
			type = ANNUAL;
		else if (lifeCycle.equalsIgnoreCase("Biennial"))
			type = BIENNIAL;
		else if (lifeCycle.equalsIgnoreCase("Perennial"))
			type = PERENNIAL;
		return type;
	}

	/**
	 * Gets the length of the crop type's growing phase.
	 *
	 * @return crop type's growing time in millisols.
	 */
	public double getGrowingTime() {
		return growingTime;
	}

	/**
	 * Gets the crop's category.
	 *
	 * @return cropCategory
	 */
	public CropCategory getCropCategory() {
		return cropCategory;
	}

	/**
	 * Gets the edible biomass.
	 *
	 * @return crop's edible biomass (grams per m^2 per day)
	 */
	public double getEdibleBiomass() {
		return edibleBiomass;
	}

	/**
	 * Gets the edible water content.
	 *
	 * @return crop's edible water content (grams per m^2 per day)
	 */
	public double getEdibleWaterContent() {
		return edibleWaterContent;
	}

	/**
	 * Gets the inedible biomass.
	 *
	 * @return crop's inedible biomass (grams per m^2 per day)
	 */
	public double getInedibleBiomass() {
		return inedibleBiomass;
	}

	/**
	 * Gets the daily PAR, the average amount of light needed per day in terms of
	 * daily Photosynthetically active radiation (PAR).
	 *
	 * @return crop's daily PAR
	 */
	public double getDailyPAR() {
		return dailyPAR;
	}

	public int getID() {
		return id;
	}

	/**
	 * Gets the next phase in the growing sequence.
	 * 
	 * @param phaseType
	 * @return
	 */
	public Phase getNextPhase(Phase currentPhase) {
		int nextId = 1;

		PhaseType target = currentPhase.getPhaseType();
		for (Phase entry : phases) {
			if (entry.getPhaseType() == target) {
				return phases.get(nextId);
			}
			nextId++;
		}
		return null;
	}
	
	/**
	 * Gets the next phase in the growing sequence.
	 * 
	 * @param phaseType
	 * @return
	 */
	public PhaseType getNextPhaseType(PhaseType phaseType) {
		int nextId = 1;

		for (Phase entry : phases) {
			if (entry.getPhaseType() == phaseType) {
				return phases.get(nextId).getPhaseType();
			}
			nextId++;
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
		throw new IllegalArgumentException("Phase type " + phaseType.getName() + " is not support in " + name);
	}

	/**
	 * What percentage complete has to be completed for the specified phase
	 * to advance to the next one ?
	 * 
	 * @param phaseType
	 * @return
	 */
	public double getNextPhasePercentage(PhaseType phaseType) {
		double result = 0;
		for(Phase p : phases) {
			result += p.getPercentGrowth();
			if (p.getPhaseType() == phaseType)
				return result;
		}
		return result;
	}

	/**
	 * Does this crop need light ?
	 * 
	 * @return
	 */
	public boolean needsLight() {
		return (cropCategory != CropCategory.FUNGI);
	}

	/**
	 * Compares this object with the specified object for order.
	 *
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	public int compareTo(CropSpec c) {
		return name.compareToIgnoreCase(c.name);
	}

	/**
	 * String representation of this cropType.
	 *
	 * @return The cropType's name.
	 */
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return id % 32;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		CropSpec c = (CropSpec) obj;
		return this.id == c.id;
	}
}

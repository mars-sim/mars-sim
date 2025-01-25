/*
 * Mars Simulation Project
 * CropSpec.java
 * @date 2023-05-06
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function.farming;

import java.io.Serializable;

import com.mars_sim.core.resource.ResourceUtil;

/**
 * The CropSpec class is a type of crop.
 */
public class CropSpec implements Serializable, Comparable<CropSpec> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** Is this a seed only? */
	private boolean seedOnly;
	/** The crop id. */
	private int cropID;
	/** The seed id. */
	private int seedID = -1;
	/** The length of the growing phase. */
	private int growingSols;
	/** The fresh basis edible biomass productivity [in gram per sq m per day]. */
	private double edibleBiomass;
	/** The percentage of watet content. */
	private double edibleWaterContent;
	/** The inedible biomass [in gram per sq m per day]. */
	private double inedibleBiomass;
	/**
	 * The daily photosynthetically active radiation (PAR) or Daily Light Integral
	 * (DLI) [in moles per square meter per day, or mol/m^2/d].
	 */
	private double dailyPAR; // Note: not umol / m^2 / s // PAR is the instantaneous light with a wavelength
								// between 400 to 700 nm

	private String name;

	/** The category of this crop. */
	private CropCategory cropCategory;

	/**
	 * Constructor.
	 *
	 * @param name             Name of the crop.
	 * @param growingSol        Length of growing phase for crop in sols.
	 * @param cropCategory  The category of crop.
	 * @param edibleBiomass
	 * @param edibleWaterContent
	 * @param inedibleBiomass
	 * @param dailyPAR
	 * @param seedName
	 * @param seedOnly
	 */
	CropSpec(String name, int growingSols, CropCategory cropCategory, 
			double edibleBiomass, double edibleWaterContent, double inedibleBiomass, double dailyPAR,
			String seedName, boolean seedOnly) {

		this.name = name;
		this.growingSols = growingSols;
		this.cropCategory = cropCategory;
		this.edibleBiomass = edibleBiomass;
		this.edibleWaterContent = edibleWaterContent;
		this.inedibleBiomass = inedibleBiomass;
		this.dailyPAR = dailyPAR;
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
	 * Gets the length of the crop type's growing phase.
	 *
	 * @return crop type's growing time in sols.
	 */
	public int getGrowingSols() {
		return growingSols;
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
	
	

	/**
	 * Does this crop need light ?
	 * 
	 * @return
	 */
	public boolean needsLight() {
		return cropCategory.needsLight();
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
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		CropSpec c = (CropSpec) obj;
		return this.name.equals(c.name);
	}
}

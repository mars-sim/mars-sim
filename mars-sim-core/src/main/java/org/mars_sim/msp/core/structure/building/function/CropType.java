/**
 * Mars Simulation Project
 * CropType.java
* @version 3.07 2014-10-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

/**
 * The CropType class is a type of crop.
 */
public class CropType
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** TODO The name of the type of crop should be internationalizable. */
	private String name;
	/** The length of the crop type's growing phase. */
	private double growingTime;

	//2014-10-05 mkung: added new attributes: harvestIndex, cropCategory, ppf and photoperiod
	/** The type of crop */
	private String cropCategory;
	/** The Photosynthetic Photon Flux (PPF) is the amount of light needed [in micro mol per sq meter per second] */
	private double ppf;
	/** The Photoperiod is the number of hours of light needed [in hours per day] */
	private double photoperiod;
	/** The average harvest index (from 0 to 1) -- the ratio of the edible over the inedible part of the harvested crop [dimenionsion-less] */
	private double harvestIndex;



	/**
	 * Constructor.
	 * @param name - The name of the type of crop.
	 * @param growingTime - Length of growing phase for crop. (millisols)
	 */
	public CropType(String name, double growingTime, String cropCategory, double ppf, double photoperiod, double harvestIndex) {
		this.name = name;
		this.growingTime = growingTime;
		//2014-10-05 Added by mkung
		this.cropCategory = cropCategory;
		this.ppf = ppf;
		this.photoperiod = photoperiod;
		this.harvestIndex = harvestIndex;
	}


	/**
	 * Gets the crop type's name.
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the length of the crop type's growing phase.
	 * @return crop type's growing time in millisols.
	 */
	public double getGrowingTime() {
		return growingTime;
	}

	/**
	* Gets the crop's type.
	* @return cropCategory
	*/
	public String getCropCategory() {
		return cropCategory;
	}
	/**
	* Gets the amount of light needed in terms of Photosynthetic Photon Flux (PPF)
	* @return crop type's PPF in micro mol per sq meter per second.
	*/
	public double getPpf() {
		return ppf;
	}
	/**
	* Gets the hours of light needed as Photoperiod
	* @return crop's photoperiod in hours per day.
	*/
	public double getPhotoperiod() {
		return photoperiod;
	}
	/**
	* Gets the average harvest index 
	* @return crop's harvest index (from 0 to 1) [dimenionsion-less]
	*/
	public double getHarvestIndex() {
		return harvestIndex;
	}
}

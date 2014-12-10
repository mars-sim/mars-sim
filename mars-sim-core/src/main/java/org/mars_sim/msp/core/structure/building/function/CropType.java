/**
 * Mars Simulation Project
 * CropType.java
* @version 3.07 2014-10-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

/**
 * The CropType class is a type of crop.
 */
//2014-10-14 mkung: added new attribute: edibleBiomass. commented out ppf and photoperiod
public class CropType
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** TODO The name of the type of crop should be internationalizable. */
	private String name;
	/** The length of the crop type's growing phase. */
	private double growingTime;

	//2014-10-05 mkung: added new attributes:  cropCategory, edibleBiomass, edibleWaterContent, inedibleBiomass; 
	// commented out ppf and photoperiod and harvestIndex
	/** The type of crop */
	private String cropCategory;
	/** The Photosynthetic Photon Flux (PPF) is the amount of light needed [in micro mol per sq meter per second] */
	//private double ppf;
	/** The Photoperiod is the number of hours of light needed [in hours per day] */
	//private double photoperiod;
	//2014-10-14 mkung: added the fresh basis Edible Biomass Productivity [ in gram per sq m per day ]
	private double edibleBiomass; 
	private double edibleWaterContent; 
	private double inedibleBiomass; 
	/** The average harvest index (from 0 to 1) -- the ratio of the edible over the inedible part of the harvested crop [dimenionsion-less] */
	//private double harvestIndex;



	/**
	 * Constructor.
	 * @param name - The name of the type of crop.
	 * @param growingTime - Length of growing phase for crop. (millisols)
	 */
	public CropType(String name, double growingTime, String cropCategory, 
			double edibleBiomass, double edibleWaterContent, double inedibleBiomass) {
		this.name = name;
		this.growingTime = growingTime;
		//2014-10-05 Added by mkung
		this.cropCategory = cropCategory;
		//this.ppf = ppf;
		//this.photoperiod = photoperiod;
		//2014-10-14 Added by mkung		
		this.edibleBiomass = edibleBiomass; 
		this.edibleWaterContent = edibleWaterContent; 
		this.inedibleBiomass = inedibleBiomass;
		//this.harvestIndex = harvestIndex;
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
	public double getPpf() {
		return ppf;
	}
	* Gets the hours of light needed as Photoperiod
	* @return crop's photoperiod in hours per day.
	public double getPhotoperiod() {
		return photoperiod;
	}
	**/

	/**
	* Gets the edible biomass  
	* @return crop's edible biomass 
	*/
	public double getEdibleBiomass() {
		return edibleBiomass;
	}
	/**
	* Gets the edible water content 
	* @return crop's edible water content
	*/
	public double getEdibleWaterContent() {
		return edibleWaterContent;
	}
	/**
	* Gets the inedible biomass  
	* @return crop's inedible biomass 
	*/
	public double getInedibleBiomass() {
		return inedibleBiomass;
	}

	/**
	 * String representation of this cropType.
	 * @return The settlement and cropType's name.
	 */
	// 2014-12-09 Added toString() 
	public String toString() {
		return name;	
	}
	
}

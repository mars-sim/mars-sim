/**
 * Mars Simulation Project
 * CropType.java
 * @version 3.06 2014-01-29
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

	/**
	 * Constructor.
	 * @param name - The name of the type of crop.
	 * @param growingTime - Length of growing phase for crop. (millisols)
	 */
	public CropType(String name, double growingTime) {
		this.name = name;
		this.growingTime = growingTime;
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
}


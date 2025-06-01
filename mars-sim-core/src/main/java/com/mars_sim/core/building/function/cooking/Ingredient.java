/*
 * Mars Simulation Project
 * Ingredient.java
 * @date 2022-08-30
 * @author Manny Kung
 */

package com.mars_sim.core.building.function.cooking;

import java.io.Serializable;

import com.mars_sim.core.resource.ResourceUtil;

public class Ingredient implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private boolean mandatory;
	private double impact;

	private double proportion;
	private double ingredientDryMass;

	private int amountResource;

	Ingredient(int amountResourceID, double proportion, boolean mandatory, double impact) {
		this.impact = impact;
		this.mandatory = mandatory;
		this.amountResource = amountResourceID;
		this.proportion = proportion;
	}

	public String getName() {
		return ResourceUtil.findAmountResourceName(amountResource);
	}

	public int getAmountResourceID() {
		return amountResource;
	}

	public double getProportion() {
		return proportion;
	}

	public double getImpact() {
		return impact;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	void setDrymass(double newMass) {
		ingredientDryMass = newMass;
	}

	public double getDryMass() {
		return ingredientDryMass;
	}

	@Override
	public String toString() {
		return getName();
	}
}

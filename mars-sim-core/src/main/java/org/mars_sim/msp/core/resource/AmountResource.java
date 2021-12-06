/*
 * Mars Simulation Project
 * AmountResource.java
 * @date 2021-12-05
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;

/**
 * The AmountResource class represents a type of resource measured in mass kg.
 */
public final class AmountResource extends ResourceAbstract implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	public static final int FOOD = 1;
	public static final int WATER = 2;
	public static final int OXYGEN = 3;
	public static final int CO2 = 4;

	public static final int METHANE = 8;
	public static final int ICE = 12;
	public static final int REGOLITH = 142;
	public static final int ROCK_SAMLE = 143;

	public static final int FOOD_WASTE = 16;
	public static final int SOLID_WASTE = 17;
	public static final int GREY_WATER = 19;
	public static final int TABLE_SALT = 23;
	public static final int SODIUM_HYPOCHLORITE = 145;
	public static final int NAPKIN = 150;

	// Data members
	private int hashcode = -1;

	private boolean edible;

	private boolean lifeSupport;

	private String type;

	private PhaseType phase;

	/**
	 * Constructor
	 *
	 * @param name        the resource's name
	 * @param description {@link String}
	 * @param phase       the material phase of the resource.
	 * @param lifeSupport true if life support resource.
	 */
	public AmountResource(int id, String name, String type, String description, PhaseType phase, boolean lifeSupport,
			boolean edible) {
		super(name.toLowerCase(), id, description);
		this.type = type;
		this.phase = phase;
		this.lifeSupport = lifeSupport;
		this.edible = edible;
		this.hashcode = getName().toLowerCase().hashCode() * phase.hashCode();
	}

	/**
	 * Gets the resource's type.
	 *
	 * @return type of resource.
	 */
	// @Override
	public String getType() {
		return type;
	}


	/**
	 * Gets the resources material phase.
	 *
	 * @return phase value
	 */
	public PhaseType getPhase() {
		return phase;
	}

	/**
	 * Checks if life support resource.
	 *
	 * @return true if life support resource.
	 */
	public boolean isLifeSupport() {
		return lifeSupport;
	}

	/**
	 * Checks if edible resource.
	 *
	 * @return true if edible resource.
	 */
	public boolean isEdible() {
		return edible;
	}

	/**
	 * Gets the hash code value.
	 */
	@Override
	public int hashCode() {
		return hashcode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		AmountResource ar = (AmountResource) obj;
		return this.getName().equalsIgnoreCase(ar.getName());
	}

	public void destroy() {

//		amountResourceConfig = null;
//		phase = null;
//		foodAR = null;
//		oxygenAR = null;
//		waterAR = null;
//		carbonDioxideAR = null;
//	    tableSaltAR = null;
//	    NaClOAR = null;
//	    greyWaterAR = null;
//	    foodWasteAR = null;
//	    solidWasteAR = null;
//	    napkinAR = null;

	}

}

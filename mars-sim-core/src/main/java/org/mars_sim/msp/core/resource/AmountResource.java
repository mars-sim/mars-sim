/*
 * Mars Simulation Project
 * AmountResource.java
 * @date 2021-12-05
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;

import org.mars_sim.msp.core.goods.GoodType;

/**
 * The AmountResource class represents a type of resource measured in mass kg.
 */
public final class AmountResource extends ResourceAbstract {

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
	private boolean edible;

	private boolean lifeSupport;
	
	/** By default, demandMultiplier is zero. */
	private double demandMultiplier = 0;

	private GoodType goodType;

	private PhaseType phase;

	/**
	 * Constructor.
	 *
	 * @param name			the resource's name
	 * @param goodType		the good type
	 * @param description {@link String}
	 * @param phase			the material phase of the resource
	 * @param demand		the demand multiplier of this good
	 * @param lifeSupport	true if life support resource
	 * @param edible		true if edible.
	 */
	public AmountResource(int id, String name, GoodType goodType, String description, 
			PhaseType phase, double demand, boolean lifeSupport, boolean edible) {
		super(name, id, description);
		this.goodType = goodType;
		this.phase = phase;
		this.demandMultiplier = demand;
		this.lifeSupport = lifeSupport;
		this.edible = edible;
	}
	
	/**
	 * Gets the resource's good type.
	 *
	 * @return the good type.
	 */
	public GoodType getGoodType() {
		return goodType;
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
	 * Gets the demand multiplier.
	 * 
	 * @return
	 */
	public double getDemand() {
		return demandMultiplier;
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
		return getID() % 128;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		AmountResource ar = (AmountResource) obj;
		return this.getID() == ar.getID();
	}
}

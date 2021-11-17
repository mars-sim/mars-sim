/*
 * Mars Simulation Project
 * ItemResource.java
 * @date 2021-11-16
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;

/**
 * The ItemResource class represents a type of resource measured in countable
 * units of quantity. It's for simple tools and parts.
 */
public class ItemResource extends ResourceAbstract implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private double massPerItem;
	private int startSol;
	protected String type;

	/**
	 * Constructor.
	 *
	 * @param name        the name of the resource.
	 * @param description {@link String}
	 * @param massPerItem the mass (kg) of the resource per item.
	 * @param the         sol when this resource is put to use.
	 */
	protected ItemResource(String name, int id, String description, String type, double massPerItem, int startSol) {
		super(name, id, description);

		this.massPerItem = massPerItem;
		this.startSol = startSol;
		this.type = type;
	}

	/**
	 * Gets the starting sol of the resource
	 *
	 * @return the starting sol
	 */
	public int getStartSol() {
		return startSol;
	}

	/**
	 * Gets the mass for an item of the resource.
	 *
	 * @return mass (kg)
	 */
	public double getMassPerItem() {
		return massPerItem;
	}

	/**
	 * Gets the type for an item of the resource.
	 *
	 * @return type
	 */
	public String getType() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ItemResource other = (ItemResource) obj;
		if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
			return false;
		}
        return Double.doubleToLongBits(this.massPerItem) == Double.doubleToLongBits(other.massPerItem);
    }

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 19 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
		hash = 19 * hash + (int) (Double.doubleToLongBits(this.massPerItem)
				^ (Double.doubleToLongBits(this.massPerItem) >>> 32));
		return hash;
	}
}

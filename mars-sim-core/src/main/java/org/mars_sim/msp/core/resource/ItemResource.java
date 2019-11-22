/**
 * Mars Simulation Project
 * ItemResource.java
 * @version 3.1.0 2017-09-05
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
	private int id;
	private double massPerItem;
	private String name;
	private String description;
	private int startSol;

//	private static PartConfig partConfig;

	public ItemResource() {
//		partConfig = SimulationConfig.instance().getPartConfiguration();
		ResourceUtil.getInstance();
	}

//	/*
//	 * Default private constructor
//	 *
//	private ItemResource() {
//		throw new UnsupportedOperationException("invalid constructor");
//	}

	/**
	 * Constructor.
	 * 
	 * @param name        the name of the resource.
	 * @param description {@link String}
	 * @param massPerItem the mass (kg) of the resource per item.
	 * @param the         sol when this resource is put to use.
	 */
	protected ItemResource(String name, int id, String description, double massPerItem, int startSol) {
		this.name = name;
		this.id = id;
		this.description = description;
		this.massPerItem = massPerItem;
		this.startSol = startSol;
	}

	/**
	 * Gets the resource's id.
	 * 
	 * @return resource id.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * Gets the resource's name.
	 * 
	 * @return name of resource.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Gets the description of the resource
	 * 
	 * @return description
	 */
	@Override
	public String getDescription() {
		return description;
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
		if (Double.doubleToLongBits(this.massPerItem) != Double.doubleToLongBits(other.massPerItem)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 19 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
		hash = 19 * hash + (int) (Double.doubleToLongBits(this.massPerItem)
				^ (Double.doubleToLongBits(this.massPerItem) >>> 32));
		return hash;
	}

	
//	public static Part createItemResource(String resourceName, int id, String description, double massPerItem,
//			int solsUsed) {
//		Part p = new Part(resourceName, id, description, massPerItem, solsUsed);
//		ItemResourceUtil.registerBrandNewPart(p);
//		return p;
//	}

//	private static class UnknownResourceName extends RuntimeException {
//
//		/** default serial id. */
//		private static final long serialVersionUID = 1L;
//
//		private String name;
//
//		public UnknownResourceName(String name) {
//			super("Unknown resource name : " + name);
//			this.name = name;
//		}
//
////		public String getName() {
////			return name;
////		}
//
//	}
}
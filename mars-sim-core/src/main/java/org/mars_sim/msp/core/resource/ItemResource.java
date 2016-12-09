/**
 * Mars Simulation Project
 * ItemResource.java
 * @version 3.07 2014-12-01
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.mars_sim.msp.core.SimulationConfig;

/**
 * The ItemResource class represents a type of resource that is measured in units, 
 * such as simple tools and parts.
 */
public class ItemResource
extends ResourceAbstract
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// Light utility vehicle attachment parts for mining.
	public static final String PNEUMATIC_DRILL = "pneumatic drill";
	public static final String BACKHOE = "backhoe";
	public static final String SMALL_HAMMER = "small hammer";
	public static final String SOCKET_WRENCH = "socket wrench";
	public static final String PIPE_WRENCH = "pipe wrench";
	
	// Data members
	private int id;
	private double massPerItem;
	private String name;
	private String description;

	private static PartConfig partConfig;
	public static Part pneumaticDrill, backhoe, smallHammer, socketWrench, pipeWrench;
  
	
	public ItemResource() {
		partConfig = SimulationConfig.instance().getPartConfiguration();
		pneumaticDrill = (Part) findItemResource(PNEUMATIC_DRILL);
		backhoe = (Part) findItemResource(BACKHOE);	
		smallHammer = (Part) findItemResource(SMALL_HAMMER);
		socketWrench = (Part) findItemResource(SOCKET_WRENCH);
		pipeWrench = (Part) findItemResource(PIPE_WRENCH); 
	}
	

	/*
	 * Default private constructor
	 *
	private ItemResource() {
		throw new UnsupportedOperationException("invalid constructor");
	}
	 */

	
	/**
	 * Constructor.
	 * @param name the name of the resource.
	 * @param description {@link String}
	 * @param massPerItem the mass (kg) of the resource per item.
	 */
	protected ItemResource(String name, String description, double massPerItem) {
		this.name = name;
		this.description = description;
		this.massPerItem = massPerItem;
	}

	/**
	 * Gets the resource's id.
	 * @return resource id.
	 */
	@Override
	public int getID() {
		return id;
	}
	
	/**
	 * Gets the resource's name.
	 * @return name of resource.
	 */
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	
	/**
	 * Gets the mass for an item of the resource.
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
		hash = 19 * hash + (int) (Double.doubleToLongBits(this.massPerItem) ^ (Double.doubleToLongBits(this.massPerItem) >>> 32));
		return hash;
	}

	/**
	 * Finds an item resource by name.
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.

	public static ItemResource findItemResource(String name) {
		ItemResource result = null;
		Iterator<Part> i = getItemResources().iterator();
		while (i.hasNext()) {
			ItemResource resource = i.next();
			//2014-12-01 Added equalsIgnoreCase()
			if (resource.getName().equalsIgnoreCase(name)) result = resource;
		}
		if (result != null) return result;
		else throw new UnknownResourceName(name);
	}
	 */
	
	/**
	 * Finds an item resource by name.
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */	 
	public static ItemResource findItemResource(String name) {
		// 2016-12-08 Using Java 8 stream
		return getItemResources()
				.stream()
				.filter(item -> item.getName().equals(name.toLowerCase()))
				.findFirst().orElse(null);//.get();	

		//return getItemResourcesMap().get(name.toLowerCase());
	}
	
	/**
	 * Gets a ummutable collection of all the item resources.
	 * @return collection of item resources.
	 */
	//public static Set<ItemResource> getItemResources() {
	//	return Collections.unmodifiableSet(partConfig.getItemResources());
	//}
	
	public static Set<Part> getItemResources() {
		return Collections.unmodifiableSet(partConfig.getItemResources());
	}
	
	public static Map<String, Part> getItemResourcesMap() {
		//if (partConfig == null) System.err.println("partConfig == null");
		return partConfig.getItemResourcesMap();
	}

	public static ItemResource createItemResource(
			String resourceName,
			String description,
			double massPerItem
			) {
		return new ItemResource(resourceName,description,massPerItem);
	}

	private static class UnknownResourceName
	extends RuntimeException {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private String name;

		public UnknownResourceName(String name) {
			super("Unknown resource name : " + name);
			this.name = name;
		}
		/*
		public String getName() {
			return name;
		}
		 */
	}
}
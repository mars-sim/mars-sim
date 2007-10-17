/**
 * Mars Simulation Project
 * ItemResource.java
 * @version 2.81 2007-04-16
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.resource;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The ItemResource class represents a type of resource that is measured in units, 
 * such as simple tools and parts.
 */
public class ItemResource implements Resource, Serializable {

	// Set of all item resources.
	private static final Set<ItemResource> resources = new HashSet<ItemResource>(3);
	
	// Item resources.
	public static final ItemResource HAMMER = new ItemResource("hammer", 1.4D);
	public static final ItemResource SOCKET_WRENCH = new ItemResource("socket wrench", .5D);
	public static final ItemResource PIPE_WRENCH = new ItemResource("pipe wrench", 2.5D);
	
	// Data members
	private String name;
	private double massPerItem;
	
	/**
	 * Default private constructor
	 */
	private ItemResource() {}
	
	/**
	 * Constructor
	 * @param name the name of the resource.
	 * @param massPerItem the mass (kg) of the resource per item.
	 */
	protected ItemResource(String name, double massPerItem) {
		this.name = name;
		this.massPerItem = massPerItem;
		if (!resources.contains(this)) resources.add(this);
	}
	
	/**
	 * Gets the resource's name.
	 * @return name of resource.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the resource as a string.
	 */
	public String toString() {
		return getName();
	}
	
	/**
	 * Gets the mass for an item of the resource.
	 * @return mass (kg)
	 */
	public double getMassPerItem() {
		return massPerItem;
	}
	
	/**
	 * Checks if an object is equal to this object.
	 * @return true if equal
	 */
	public boolean equals(Object object) {
		if (object instanceof ItemResource) {
			ItemResource otherObject = (ItemResource) object;
			if ((name.equals(otherObject.getName())) && (massPerItem == otherObject.getMassPerItem()))
				return true;
		}
		return false;
	}
	
	/**
	 * Gets the hash code value.
	 */
	public int hashCode() {
		return (name.hashCode() * new Double(massPerItem).hashCode());
	}
	
	/**
	 * Finds an item resource by name.
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static final ItemResource findItemResource(String name) throws ResourceException {
		ItemResource result = null;
		Iterator<ItemResource> i = resources.iterator();
		while (i.hasNext()) {
			ItemResource resource = i.next();
			if (resource.getName().equals(name)) result = resource;
		}
		if (result != null) return result;
		else throw new ResourceException("Resource: " + name + " could not be found.");
	}
	
	/**
	 * Gets a ummutable set of all the item resources.
	 * @return set of item resources.
	 */
	public static final Set<ItemResource> getItemResources() {
		return Collections.unmodifiableSet(resources);
	}
}
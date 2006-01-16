/**
 * Mars Simulation Project
 * ItemResource.java
 * @version 2.79 2005-11-05
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.resource;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class ItemResource implements Resource, Serializable {

	// Set of all item resources.
	private static final Set resources = new HashSet(3);
	
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
	 * Private constructor
	 * @param name the name of the resource.
	 * @param massPerItem the mass (kg) of the resource per item.
	 */
	private ItemResource(String name, double massPerItem) {
		this.name = name;
		this.massPerItem = massPerItem;
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
	 * Finds an item resource by name.
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static final ItemResource findItemResource(String name) throws ResourceException {
		ItemResource result = null;
		Iterator i = resources.iterator();
		while (i.hasNext()) {
			ItemResource resource = (ItemResource) i.next();
			if (resource.getName().equals(name)) result = resource;
		}
		if (result != null) return result;
		else throw new ResourceException("Resource: " + name + " could not be found.");
	}
	
	/**
	 * Gets a ummutable set of all the item resources.
	 * @return set of item resources.
	 */
	public static final Set getItemResources() {
		return Collections.unmodifiableSet(resources);
	}
}
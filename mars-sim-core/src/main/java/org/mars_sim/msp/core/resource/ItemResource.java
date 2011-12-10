/**
 * Mars Simulation Project
 * ItemResource.java
 * @version 3.02 2011-12-09
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.mars_sim.msp.core.SimulationConfig;

/**
 * The ItemResource class represents a type of resource that is measured in units, 
 * such as simple tools and parts.
 */
public class ItemResource implements Resource, Serializable {

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
		return name;
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
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
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
        hash = 19 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.massPerItem) ^ (Double.doubleToLongBits(this.massPerItem) >>> 32));
        return hash;
    }
	
	/**
	 * Finds an item resource by name.
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static ItemResource findItemResource(String name) {
		ItemResource result = null;
		Iterator<ItemResource> i = getItemResources().iterator();
		while (i.hasNext()) {
			ItemResource resource = i.next();
			if (resource.name.equals(name)) result = resource;
		}
		if (result != null) return result;
		else throw new IllegalStateException("Resource: " + name + " could not be found.");
	}
	
	/**
	 * Gets a ummutable set of all the item resources.
	 * @return set of item resources.
	 */
	public static Set<ItemResource> getItemResources() {
	    Set<ItemResource> resources = SimulationConfig.instance().getPartConfiguration().
                getItemResources();
		return Collections.unmodifiableSet(resources);
	}
	
	/**
	 * Gets a mock item resource of a hammer.
	 * @return item resource.
	 */
	public static ItemResource getTestResourceHammer() {
		return new ItemResource("hammer", 1.4D);
	}
	
	/**
	 * Gets a mock item resource of a socket wrench.
	 * @return item resource.
	 */
	public static ItemResource getTestResourceSocketWrench() {
		return new ItemResource("socket wrench", .5D);
	}
	
	/**
	 * Gets a mock item resource of a pipe wrench.
	 * @return item resource.
	 */
	public static ItemResource getTestResourcePipeWrench() {
		return new ItemResource("pipe wrench", 2.5D);
	}
    
    /**
     * Compares this object with the specified object for order.
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, 
     * equal to, or greater than the specified object.
     */
    public int compareTo(Resource o) {
        return name.compareTo(o.getName());
    }
}
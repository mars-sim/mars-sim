/**
 * Mars Simulation Project
 * AmountResource.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The AmountResource class represents a type of resource that is a material 
 * measured in mass kg.
 */
public final class AmountResource implements Resource, Serializable {
	
    // Set of all amount resources.
    private static final Set<AmountResource> resources = new HashSet<AmountResource>(10);
    
	// Data members
	private String name;
	private Phase phase;
	private boolean lifeSupport;
	
	/**
	 * Default private constructor
	 */
	private AmountResource() {}
	
	/**
	 * Constructor with life support parameter.
	 * @param name the resource's name
	 * @param phase the material phase of the resource.
	 * @param lifeSupport true if life support resource.
	 */
	public AmountResource(String name, Phase phase, boolean lifeSupport) {
		this.name = name;
		this.phase = phase;
		this.lifeSupport = lifeSupport;
		resources.add(this);
	}
	
	/**
	 * Gets the resource's name
	 * @return name
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
	 * Gets the resources material phase.
	 * @return phase value
	 */
	public Phase getPhase() {
		return phase;
	}
	
	/**
	 * Checks if life support resource.
	 * @return true if life support resource.
	 */
	public boolean isLifeSupport() {
		return lifeSupport;
	}
	
	/**
	 * Finds an amount resource by name.
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static AmountResource findAmountResource(String name) {
		AmountResource result = null;
		Iterator<AmountResource> i = resources.iterator();
		while (i.hasNext()) {
			AmountResource resource = i.next();
			if (resource.name.equalsIgnoreCase(name)) result = resource;
		}
		if (result != null) return result;
		else throw new IllegalStateException("Resource: " + name + " could not be found.");
	}
	
	/**
	 * Gets a ummutable set of all the amount resources.
	 * @return set of amount resources.
	 */
	public static Set<AmountResource> getAmountResources() {
		return Collections.unmodifiableSet(resources);
	}
	
	/**
	 * Checks if an object is equal to this object.
	 * @return true if equal
	 */
	public boolean equals(Object object) {
		if (object instanceof AmountResource) {
			AmountResource otherObject = (AmountResource) object;
			if ((name.equals(otherObject.name)) && (phase.equals(otherObject.phase)))
				return true;
		}
		return false;
	}
	
	/**
	 * Gets the hash code value.
	 */
	public int hashCode() {
		return (name.hashCode() * phase.hashCode());
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
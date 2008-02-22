/**
 * Mars Simulation Project
 * AmountResource.java
 * @version 2.83 2008-02-20
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.resource;

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
	
	// Amount resources
    public static final AmountResource WATER = new AmountResource("water", Phase.LIQUID, true);
    public static final AmountResource OXYGEN = new AmountResource("oxygen", Phase.GAS, true);
    public static final AmountResource HYDROGEN = new AmountResource("hydrogen", Phase.GAS);
    public static final AmountResource METHANE = new AmountResource("methane", Phase.GAS);
    public static final AmountResource CARBON_DIOXIDE = new AmountResource("carbon dioxide", Phase.GAS);
    public static final AmountResource CARBON_MONOXIDE = new AmountResource("carbon monoxide", Phase.GAS);
    public static final AmountResource FOOD = new AmountResource("food", Phase.SOLID, true);
    public static final AmountResource ROCK_SAMPLES = new AmountResource("rock samples", Phase.SOLID);
    public static final AmountResource WASTE_WATER = new AmountResource("waste water", Phase.LIQUID);
    public static final AmountResource ICE = new AmountResource("ice", Phase.SOLID);
    public static final AmountResource REGOLITH = new AmountResource("regolith", Phase.SOLID);
    public static final AmountResource IRON_OXIDE = new AmountResource("iron oxide", Phase.SOLID);
    public static final AmountResource CARBON = new AmountResource("carbon", Phase.SOLID);
    public static final AmountResource SAND = new AmountResource("sand", Phase.SOLID);
    public static final AmountResource IRON_POWDER = new AmountResource("iron powder", Phase.SOLID);
    public static final AmountResource ALUMINUM_OXIDE = new AmountResource("aluminum oxide", Phase.SOLID);
    public static final AmountResource ETHYLENE = new AmountResource("ethylene", Phase.GAS);
    public static final AmountResource POLYETHYLENE = new AmountResource("polyethylene", Phase.SOLID);
    public static final AmountResource GYPSUM = new AmountResource("gypsum", Phase.SOLID);
    public static final AmountResource CALCIUM_CARBONATE = new AmountResource("calcium carbonate", Phase.SOLID);
    public static final AmountResource LIME = new AmountResource("lime", Phase.SOLID);
    public static final AmountResource STYRENE = new AmountResource("styrene", Phase.SOLID);
    public static final AmountResource POLYESTER_RESIN = new AmountResource("polyester resin", Phase.LIQUID);
    
	// Data members
	private String name;
	private Phase phase;
	private boolean lifeSupport;
	
	/**
	 * Default private constructor
	 */
	private AmountResource() {}
	
	/**
	 * Private constructor
	 * @param name the resource's name
	 * @param phase the material phase of the resource.
	 */
	private AmountResource(String name, Phase phase) {
		this(name, phase, false);
	}
	
	/**
	 * Private constructor with life support parameter.
	 * @param name the resource's name
	 * @param phase the material phase of the resource.
	 * @param lifeSupport true if life support resource.
	 */
	private AmountResource(String name, Phase phase, boolean lifeSupport) {
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
		return getName();
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
	public static final AmountResource findAmountResource(String name) throws ResourceException {
		AmountResource result = null;
		Iterator<AmountResource> i = resources.iterator();
		while (i.hasNext()) {
			AmountResource resource = i.next();
			if (resource.getName().equalsIgnoreCase(name)) result = resource;
		}
		if (result != null) return result;
		else throw new ResourceException("Resource: " + name + " could not be found.");
	}
	
	/**
	 * Gets a ummutable set of all the amount resources.
	 * @return set of amount resources.
	 */
	public static final Set<AmountResource> getAmountResources() {
		return Collections.unmodifiableSet(resources);
	}
	
	/**
	 * Checks if an object is equal to this object.
	 * @return true if equal
	 */
	public boolean equals(Object object) {
		if (object instanceof AmountResource) {
			AmountResource otherObject = (AmountResource) object;
			if ((name.equals(otherObject.getName())) && (phase.equals(otherObject.getPhase())))
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
}
/**
 * Mars Simulation Project
 * AmountResource.java
 * @version 2.79 2005-11-20
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.resource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The AmountResource class represents a type of resource that is a material 
 * measured in mass kg.
 */
public final class AmountResource implements Resource {
	
    // Set of all amount resources.
    private static final Set resources = new HashSet(10);
	
	// Amount resources
    public static final AmountResource WATER = new AmountResource("water", Phase.LIQUID);
    public static final AmountResource OXYGEN = new AmountResource("oxygen", Phase.GAS);
    public static final AmountResource HYDROGEN = new AmountResource("hydrogen", Phase.GAS);
    public static final AmountResource METHANE = new AmountResource("methane", Phase.GAS);
    public static final AmountResource CARBON_DIOXIDE = new AmountResource("carbon dioxide", Phase.GAS);
    public static final AmountResource CARBON_MONOXIDE = new AmountResource("carbon monoxide", Phase.GAS);
    public static final AmountResource FOOD = new AmountResource("food", Phase.SOLID);
    public static final AmountResource ROCK_SAMPLES = new AmountResource("rock samples", Phase.SOLID);
    public static final AmountResource WASTE_WATER = new AmountResource("waste water", Phase.LIQUID);
    public static final AmountResource ICE = new AmountResource("ice", Phase.SOLID);
    
	// Data members
	private String name;
	private Phase phase;
	
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
		this.name = name;
		this.phase = phase;
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
	 * Gets the resources material phase.
	 * @return phase value
	 */
	public Phase getPhase() {
		return phase;
	}
	
	/**
	 * Finds an amount resource by name.
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static final AmountResource findAmountResource(String name) throws ResourceException {
		AmountResource result = null;
		Iterator i = resources.iterator();
		while (i.hasNext()) {
			AmountResource resource = (AmountResource) i.next();
			if (resource.getName().equals(name.toLowerCase())) result = resource;
		}
		if (result != null) return result;
		else throw new ResourceException("Resource: " + name + " could not be found.");
	}
	
	/**
	 * Gets a ummutable set of all the amount resources.
	 * @return set of amount resources.
	 */
	public static final Set getAmountResources() {
		return Collections.unmodifiableSet(resources);
	}
}
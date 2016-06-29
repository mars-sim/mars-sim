/**
 * Mars Simulation Project
 * AmountResource.java
 * @version 3.07 2015-01-15
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.core.SimulationConfig;

/**
 * The AmountResource class represents a type of resource that is a material
 * measured in mass kg.
 */
public final class AmountResource
extends ResourceAbstract
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	// Data members
	private Phase phase;
	private boolean lifeSupport;
	private int hashcode = -1;
	private String name;
	// 2016-06-28 Added type
	private String type;
	
	private String description;

	// 2014-11-25 Added edible
	private boolean edible;

	/**
	 * Constructor with life support parameter.
	 * @param name the resource's name
	 * @param description {@link String}
	 * @param phase the material phase of the resource.
	 * @param lifeSupport true if life support resource.
	 */
	public AmountResource(
		String name,
		String type,
		String description,
		Phase phase,
		boolean lifeSupport,
		boolean edible
	) {
		this.name = name.toLowerCase();
		this.type = type;
		this.description = description;
		this.phase = phase;
		this.lifeSupport = lifeSupport;
		this.edible = edible;
		this.hashcode = getName().toLowerCase().hashCode() * phase.hashCode();
	}

	/**
	 * Gets the resource's name.
	 * @return name of resource.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Gets the resource's type.
	 * @return type of resource.
	 */
	//@Override
	public String getType() {
		return type;
	}

	/**
	 * Gets the resource's description.
	 * @return description of resource.
	 */

	@Override
	public String getDescription() {
		return description;
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
	 * Checks if edible resource.
	 * @return true if edible resource.
	 */
	// 2014-11-25 Added edible
	public boolean isEdible() {
		return edible;
	}

	/**
	 * Finds an amount resource by name.
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static AmountResource findAmountResource(String name) {
		AmountResource result = null;
		Iterator<AmountResource> i = getAmountResources().iterator();
		while (i.hasNext()) {
			AmountResource resource = i.next();
			if (resource.getName().equals(name.toLowerCase())) result = resource;
		}
		if (result != null) return result;
		else throw new IllegalStateException("Resource: " + name + " could not be found.");
	}

	/**
	 * Gets a ummutable set of all the amount resources.
	 * @return set of amount resources.
	 */
	public static Set<AmountResource> getAmountResources() {
		Set<AmountResource> set = SimulationConfig
		.instance()
		.getResourceConfiguration()
		.getAmountResources();
		return Collections.unmodifiableSet(set);
	}

	/**
	 * gets a sorted map of all amount resources by calling
	 * {@link AmountResourceConfig#getAmountResourcesMap()}.
	 * @return {@link Map}<{@link String},{@link AmountResource}>
	 */
	public static Map<String,AmountResource> getAmountResourcesMap() {
		
		//System.out.println("AmountResource : caling getAmountResourcesMap()");

		return SimulationConfig
		.instance()
		.getResourceConfiguration()
		.getAmountResourcesMap();
	}

	/**
	 * convenience method that calls {@link #getAmountResources()} and
	 * turns the result into an alphabetically ordered list of strings.
	 * @return {@link List}<{@link String}>
	 */
	public static List<String> getAmountResourcesSortedList() {
		List<String> resourceNames = new ArrayList<String>();
		Iterator<AmountResource> i = AmountResource.getAmountResources().iterator();
		while (i.hasNext()) {
			resourceNames.add(i.next().getName().toLowerCase());
		}
		Collections.sort(resourceNames);
		return resourceNames;
	}

	/**
	 * Checks if an object is equal to this object.
	 * @return true if equal
	 */
	@Override
	public boolean equals(Object object) {
		if (object instanceof AmountResource) {
			AmountResource otherObject = (AmountResource) object;
			if ((getName().toLowerCase().equals(otherObject.getName().toLowerCase())) && (phase.equals(otherObject.phase)))
				return true;
		}
		return false;
	}

	/**
	 * Gets the hash code value.
	 */
	public int hashCode() {
		return hashcode;
	}
}
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
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.LifeSupportType;
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

	static AmountResourceConfig amountResourceConfig;
	
	// Data members
	private static int count = 0;
	// 2016-12-02 Added id
	private int id;
	
	private int hashcode = -1;
	// 2014-11-25 Added edible
	private boolean edible;
	
	private boolean lifeSupport;
	
	private String name;
	// 2016-06-28 Added type
	private String type;
	
	private String description;

	private Phase phase;
	
	public static AmountResource foodAR;
	public static AmountResource oxygenAR;
	public static AmountResource waterAR;
	public static AmountResource carbonDioxideAR;
	
	public AmountResource() {
		amountResourceConfig = SimulationConfig.instance().getResourceConfiguration();
		
		foodAR = findAmountResource(LifeSupportType.FOOD);
		oxygenAR = findAmountResource(LifeSupportType.OXYGEN);
		waterAR = findAmountResource(LifeSupportType.WATER);
		carbonDioxideAR = findAmountResource(LifeSupportType.CO2);
	}
	
	/**
	 * Constructor with life support parameter.
	 * @param name the resource's name
	 * @param description {@link String}
	 * @param phase the material phase of the resource.
	 * @param lifeSupport true if life support resource.
	 */
	public AmountResource(
		int id,
		String name,
		String type,
		String description,
		Phase phase,
		boolean lifeSupport,
		boolean edible
	) {
		this.id = id;
		this.name = name.toLowerCase();
		this.type = type;
		this.description = description;
		this.phase = phase;
		this.lifeSupport = lifeSupport;
		this.edible = edible;
		this.hashcode = getName().toLowerCase().hashCode() * phase.hashCode();
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

	public static AmountResource findAmountResource(String name) {
		count++;
		if (count%50_000 == 0) System.out.println("# of calls on findAmountResource() : " + count);
		//AmountResource result = null;
		Iterator<AmountResource> i = getAmountResources().iterator();
		while (i.hasNext()) {
			AmountResource resource = i.next();
			if (resource.getName().equals(name.toLowerCase())) //result = resource;
				return resource;
		}
		return null;
		//if (result != null) return result;
		//else throw new IllegalStateException("Resource: " + name + " could not be found.");
	}
*/
	
	/**
	 * Finds an amount resource by id.
	 * @param id the resource's id.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static AmountResource findAmountResource(int id) {
		//count++;
		//if (count%50_000 == 0) System.out.println("# of calls on findAmountResource() : " + count);
		//AmountResource result = null;
		//Map<Integer, AmountResource> map = getAmountResourcesIDMap();
		//result = getAmountResourcesIDMap().get(id);		
		//if (result != null) return result;
		//else throw new IllegalStateException("Resource: " + id + " could not be found.");
		return getAmountResourcesIDMap().get(id);
	}

	
	/**
	 * Finds an amount resource by name.
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
 */	
	public static AmountResource findAmountResource(String name) {
		//count++;
		//if (count%50_000 == 0) System.out.println("# of calls on findAmountResource() : " + count);		
		//getAmountResources().forEach(r -> {
		//	if (r.getName().equals(name.toLowerCase()))
		//		return r;
		//});	
		// 2016-12-08 Using Java 8 stream
		return getAmountResources()
				.stream()
				//.parallelStream()
				.filter(item -> item.getName().equals(name.toLowerCase()))
				.findFirst().get();	
		//return amountResourceConfig.getAmountResourcesMap().get(name.toLowerCase());
	}

	
	/**
	 * Finds an amount resource by name.
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static int findIDbyAmountResourceName(String name) {
		//count++;
		//if (count%50_000 == 0) System.out.println("# of calls on findAmountResource() : " + count);		
		//Map<Integer, String> map = getIDNameMap();
		//Object result = null;
		//result = getKeyByValue(getIDNameMap(), name.toLowerCase());
		//if (result != null) return (int) result;
		//else throw new IllegalStateException("Resource: " + name + " could not be found.");
		return (int)(getKeyByValue(getIDNameMap(), name.toLowerCase()));
	}
	
	
	/**
	 * Returns the first matched key from a given value in a map for one-to-one relationship
	 * @param map
	 * @param value
	 * @return key
	 */
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (Objects.equals(value, entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	/**
	 * Returns a set of keys from a given value in a map using Java 8 stream
	 * @param map
	 * @param value
	 * @return a set of key
	 */
	public static <T, E> Set<T> getKeySetByValue(Map<T, E> map, E value) {
	    return map.entrySet()
	              .stream()
	              .filter(entry -> Objects.equals(entry.getValue(), value))
	              .map(Map.Entry::getKey)
	              .collect(Collectors.toSet());
	}
	
	/**
	 * Gets a ummutable set of all the amount resources.
	 * @return set of amount resources.
	 */
	public static Set<AmountResource> getAmountResources() {
		//Set<AmountResource> set = amountResourceConfig.getAmountResources();
		//return Collections.unmodifiableSet(set);
		return Collections.unmodifiableSet(amountResourceConfig.getAmountResources());
	}
	
	/**
	 * gets a sorted map of all amount resource names by calling
	 * {@link AmountResourceConfig#getAmountResourcesMap()}.
	 * @return {@link Map}<{@link Integer}, {@link String}>
	 */
	public static Map<Integer, String> getIDNameMap() {
		return amountResourceConfig.getIDNameMap();
	}
	
	/**
	 * gets a sorted map of all amount resources by calling
	 * {@link AmountResourceConfig#getAmountResourcesIDMap()}.
	 * @return {@link Map}<{@link Integer},{@link AmountResource}>
	 */
	public static Map<Integer, AmountResource> getAmountResourcesIDMap() {
		return amountResourceConfig.getAmountResourcesIDMap();
	}
	
	/**
	 * gets a sorted map of all amount resources by calling
	 * {@link AmountResourceConfig#getAmountResourcesMap()}.
	 * @return {@link Map}<{@link String},{@link AmountResource}>
	 */
	public static Map<String,AmountResource> getAmountResourcesMap() {
		return amountResourceConfig.getAmountResourcesMap();
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
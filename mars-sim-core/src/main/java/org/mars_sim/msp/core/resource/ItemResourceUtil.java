/**
 * Mars Simulation Project
 * ItemResourceUtil.java
 * @version 3.1.0 2017-09-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;

public class ItemResourceUtil implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Light utility vehicle attachment parts for mining.
	public static final String PNEUMATIC_DRILL = "pneumatic drill";
	public static final String BACKHOE = "backhoe";
	public static final String SOCKET_WRENCH = "socket wrench";
	public static final String PIPE_WRENCH = "pipe wrench";
	
	// Other strings
	public static final String EXTINGUSHER = "fire extinguisher";
	public static final String WORK_GLOVES = "work gloves";
	public static final String CONTAINMENT = "mushroom containment kit";
	public static final String SMALL_HAMMER = "small hammer";
	public static final String LASER_SINTERING_3D_PRINTER = "laser sintering 3d printer";
	

	public static final Part pneumaticDrillAR = (Part) findItemResource(PNEUMATIC_DRILL);
	public static final Part backhoeAR = (Part) findItemResource(BACKHOE);
	public static final Part socketWrenchAR = (Part) findItemResource(SOCKET_WRENCH);
	public static final Part pipeWrenchAR = (Part) findItemResource(PIPE_WRENCH);
	
	public static final Part fireExtinguisherAR = (Part) findItemResource(EXTINGUSHER);
	public static final Part workGlovesAR = (Part) findItemResource(WORK_GLOVES);
	public static final Part mushroomBoxAR = (Part) findItemResource(CONTAINMENT);
	public static final Part smallHammerAR = (Part) findItemResource(SMALL_HAMMER);

	public static int pneumaticDrillID;
	public static int backhoeID;
	public static int socketWrenchID;
	public static int pipeWrenchID;
	
	public static int fireExtinguisherID;
	public static int workGlovesID;
	public static int mushroomBoxID;
	public static int smallHammerID;

	public static int printerID;

	private static Map<String, Part> itemResourceMap;
	private static Map<Integer, Part> itemResourceIDMap;
	private static Map<Integer, String> partIDNameMap;

	private static Set<Part> partSet;

	private static List<Part> sortedParts;

	private static MalfunctionFactory factory;

	private static PartConfig partConfig = SimulationConfig.instance().getPartConfiguration();

	
	/**
	 * Constructor
	 */
	public ItemResourceUtil() {
		factory = Simulation.instance().getMalfunctionFactory();		
		
		partSet = getItemResources();
		
		createMaps();

		createIDs();
	}

	/**
	 * Creates an item resource
	 * 
	 * @param resourceName
	 * @param id
	 * @param description
	 * @param massPerItem
	 * @param solsUsed
	 * @return
	 */
	public static Part createItemResource(String resourceName, int id, String description, double massPerItem,
			int solsUsed) {
		Part p = new Part(resourceName, id, description, massPerItem, solsUsed);
		ItemResourceUtil.registerBrandNewPart(p);
		return p;
	}
	
	/**
	 * Prepares the id's of a few item resources
	 */
	public void createIDs() {
		pneumaticDrillID = findIDbyItemResourceName(PNEUMATIC_DRILL);
		backhoeID = findIDbyItemResourceName(BACKHOE);
		socketWrenchID = findIDbyItemResourceName(SOCKET_WRENCH);
		pipeWrenchID = findIDbyItemResourceName(PIPE_WRENCH);
		
		fireExtinguisherID = findIDbyItemResourceName(EXTINGUSHER);
		workGlovesID = findIDbyItemResourceName(WORK_GLOVES);
		mushroomBoxID = findIDbyItemResourceName(CONTAINMENT);
		smallHammerID = findIDbyItemResourceName(SMALL_HAMMER);

		printerID = findIDbyItemResourceName(LASER_SINTERING_3D_PRINTER);
	}

	/**
	 * Prepares maps for storing all item resources
	 */
	public static void createMaps() {
		itemResourceMap = new HashMap<>();
		sortedParts = new ArrayList<>(partSet);
		Collections.sort(sortedParts);

		for (Part p : sortedParts) {
			itemResourceMap.put(p.getName(), p);
		}

		itemResourceIDMap = new HashMap<>();
		for (Part p : sortedParts) {
			itemResourceIDMap.put(p.getID(), p);
		}

		partIDNameMap = new HashMap<Integer, String>();
		for (Part p : sortedParts) {
			partIDNameMap.put(p.getID(), p.getName());
		}
	}

	/**
	 * Prepares maps for storing all item resources
	 */
	public static void createTestMaps() {		
		partSet = getItemResources();
		itemResourceMap = new HashMap<>();
		sortedParts = new ArrayList<>(partSet);
		Collections.sort(sortedParts);

		partIDNameMap = new HashMap<Integer, String>();
		for (Part p : sortedParts) {
			partIDNameMap.put(p.getID(), p.getName());
		}
	}
	
	/**
	 * Register a new part in all 3 item resource maps
	 * 
	 * @param p {@link Part}
	 */
	public static void registerBrandNewPart(Part p) {
		itemResourceMap.put(p.getName(), p);
		itemResourceIDMap.put(p.getID(), p);
		partIDNameMap.put(p.getID(), p.getName());
	}

	/**
	 * Finds an item resource by name.
	 * 
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static ItemResource findItemResource(String name) {
		// Use Java 8 stream
		return getItemResources().stream().filter(item -> item.getName().equals(name.toLowerCase())).findFirst()
				.orElse(null);// .get();
		// return getItemResourcesMap().get(name.toLowerCase());
	}

	/**
	 * Finds an amount resource by id.
	 * 
	 * @param id the resource's id.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static Part findItemResource(int id) {
		return itemResourceIDMap.get(id);
	}

//	/**
//	 * Gets a ummutable collection of all the item resources.
//	 * @return collection of item resources.
//	 */
//	public static Set<ItemResource> getItemResources() {
//		return Collections.unmodifiableSet(partConfig.getItemResources());
//	}

	/**
	 * Creates a set of item resources
	 * 
	 * @return
	 */
	public static Set<Part> getItemResources() {
		if (partConfig == null)
			partConfig = SimulationConfig.instance().getPartConfiguration();
		if (partSet == null)
			partSet = Collections.unmodifiableSet(partConfig.getPartSet());
		return partSet;
	}

	/**
	 * Gets an immutable set of all the amount resources.
	 * 
	 * @return set of amount resources.
	 */
	public static Set<Integer> getItemIDs() {
		return itemResourceIDMap.keySet();
	}

	/**
	 * Gets a list of sorted parts
	 * 
	 * @return
	 */
	public static List<Part> getSortedParts() {
		sortedParts = new ArrayList<>(partSet);
		Collections.sort(sortedParts);
		return sortedParts;
	}

	
	/**
	 * Gets a map of parts
	 * 
	 * @return
	 */
	public static Map<String, Part> getItemResourcesMap() {
		return factory.getNamePartMap();
	}

	/**
	 * gets a sorted map of all amount resource names by calling
	 * {@link AmountResourceConfig#getAmountResourcesMap()}.
	 * 
	 * @return {@link Map}<{@link Integer}, {@link String}>
	 */
	public static Map<Integer, String> getPartIDNameMap() {
		return partIDNameMap;
	}

	/**
	 * Finds an item resource name by id.
	 * 
	 * @param id the resource's id.
	 * @return resource name
	 * @throws ResourceException if resource could not be found.
	 */
	public static String findItemResourceName(int id) {
		return partIDNameMap.get(id);
	}
	
	
	/**
	 * Finds an amount resource by name.
	 * 
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static Integer findIDbyItemResourceName(String name) {
		return getKeyByValue(partIDNameMap, name.toLowerCase());
	}

	/**
	 * Returns the first matched key from a given value in a map for one-to-one
	 * relationship
	 * 
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
	 * 
	 * @param map
	 * @param value
	 * @return a set of key
	 */
	public static <T, E> Set<T> getKeySetByValue(Map<T, E> map, E value) {
		return map.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), value)).map(Map.Entry::getKey)
				.collect(Collectors.toSet());
	}

	/**
	 * Gets an immutable set of all the amount resources.
	 * 
	 * @return set of amount resources.
	 */
	public static Set<Integer> getIDs() {
		return itemResourceIDMap.keySet();
	}

//	/**
//	 * gets a sorted map of all amount resource names by calling
//	 * {@link AmountResourceConfig#getAmountResourcesMap()}.
//	 * @return {@link Map}<{@link Integer}, {@link String}>
//	 */
//	public static Map<Integer, String> getIDNameMap() {
//		return IDNameMap;
//	}

	/**
	 * gets a sorted map of all amount resources by calling
	 * {@link AmountResourceConfig#getAmountResourcesIDMap()}.
	 * 
	 * @return {@link Map}<{@link Integer},{@link AmountResource}>
	 */
	public static Map<Integer, Part> getItemResourcesIDMap() {
		return itemResourceIDMap;
	}

//	/**
//	 * gets a sorted map of all amount resources by calling
//	 * {@link AmountResourceConfig#getAmountResourcesMap()}.
//	 * @return {@link Map}<{@link String},{@link AmountResource}>
//	 */
//	public static Map<String, Part> getItemResourcesMap() {
//		return itemResourceMap;
//	}

	/**
	 * convenience method that calls {@link #getAmountResources()} and turns the
	 * result into an alphabetically ordered list of strings.
	 * 
	 * @return {@link List}<{@link String}>
	 */
	public static List<String> getItemResourceStringSortedList() {
		List<String> resourceNames = new ArrayList<String>();
		Iterator<Part> i = partSet.iterator();
		while (i.hasNext()) {
			resourceNames.add(i.next().getName().toLowerCase());
		}
		Collections.sort(resourceNames);
		return resourceNames;
	}


}

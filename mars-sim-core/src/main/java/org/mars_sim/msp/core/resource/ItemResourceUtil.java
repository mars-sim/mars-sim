/**
 * Mars Simulation Project
 * ItemResourceUtil.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.SimulationConfig;

public class ItemResourceUtil implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Light utility vehicle attachment parts for mining.
	public static final String BACKHOE = "backhoe";
	public static final String BULLDOZER_BLADE = "bulldozer blade";
	public static final String CRANE_BOOM = "crane boom";
	public static final String DRILLING_RIG = "drilling rig";
	public static final String PNEUMATIC_DRILL = "pneumatic drill";
	public static final String SOCKET_WRENCH = "socket wrench";
	public static final String SOIL_COMPACTOR = "soil compactor";

	public static final String PIPE_WRENCH = "pipe wrench";
	
	// Other strings
	public static final String EXTINGUSHER = "fire extinguisher";
	public static final String WORK_GLOVES = "work gloves";
	public static final String CONTAINMENT = "mushroom containment kit";
	public static final String SMALL_HAMMER = "small hammer";
	public static final String LASER_SINTERING_3D_PRINTER = "laser sintering 3d printer";
		
	public static final String IRON_INGOT = "iron ingot";
	public static final String STEEL_INGOT = "steel ingot";
	public static final String IRON_SHEET = "iron sheet";
	public static final String STEEL_SHEET = "steel sheet";
	
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
	
	public static int ironIngotID;
	public static int ironSheetID;
	public static int steelIngotID;
	public static int steelSheetID;


	private static Map<String, Part> itemResourceMap;
	private static Map<Integer, Part> itemResourceIDMap;
	private static Map<Integer, String> partIDNameMap;

	private static Set<Part> partSet;

	private static List<Part> sortedParts;

	private static PartConfig partConfig = SimulationConfig.instance().getPartConfiguration();

	public static List<String> ATTACHMENTS = new ArrayList<>();
	
	static {
		ATTACHMENTS.add(BACKHOE);
		ATTACHMENTS.add(BULLDOZER_BLADE);
		ATTACHMENTS.add(CRANE_BOOM);
		ATTACHMENTS.add(DRILLING_RIG);
		ATTACHMENTS.add(PNEUMATIC_DRILL);
		ATTACHMENTS.add(SOIL_COMPACTOR);
	}
	
	public static String[] EVASUIT_PARTS;
	
	static {
		EVASUIT_PARTS = new String[] {
					"eva helmet",
					"helmet visor",
					"counter pressure suit",
					"coveralls",
					"suit heating unit",
					
					"eva gloves",
					"eva boots",
					"eva pads",
					"eva backpack",
					"eva antenna",
					
					"eva battery",
					"eva radio",
			};
	}
	
	/**
	 * Constructor
	 */
	public ItemResourceUtil() {
		
		partSet = getItemResources();
		
		createMaps();

		createIDs();
	}

	/**
	 * Creates an item resource. This is only used for test cases but should it be here?
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
		
		ironIngotID = findIDbyItemResourceName(IRON_INGOT);
		ironSheetID = findIDbyItemResourceName(IRON_SHEET);
		steelIngotID = findIDbyItemResourceName(STEEL_INGOT);
		steelSheetID = findIDbyItemResourceName(STEEL_SHEET);
		
	}

	/**
	 * Prepares maps for storing all item resources
	 */
	public static void createMaps() {
		itemResourceMap = new ConcurrentHashMap<>();
		sortedParts = new CopyOnWriteArrayList<>(partSet);
		Collections.sort(sortedParts);

		for (Part p : sortedParts) {
			itemResourceMap.put(p.getName(), p);
		}

		itemResourceIDMap = new ConcurrentHashMap<>();
		for (Part p : sortedParts) {
			itemResourceIDMap.put(p.getID(), p);
		}

		partIDNameMap = new ConcurrentHashMap<Integer, String>();
		for (Part p : sortedParts) {
			partIDNameMap.put(p.getID(), p.getName());
		}
	}

	/**
	 * Prepares maps for storing all item resources
	 */
	public static void createTestMaps() {		
		partSet = getItemResources();
		itemResourceMap = new ConcurrentHashMap<>();
		sortedParts = new CopyOnWriteArrayList<>(partSet);
		Collections.sort(sortedParts);

		partIDNameMap = new ConcurrentHashMap<Integer, String>();
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
		sortedParts = new CopyOnWriteArrayList<>(partSet);
		Collections.sort(sortedParts);
		return sortedParts;
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
		List<String> resourceNames = new CopyOnWriteArrayList<String>();
		Iterator<Part> i = partSet.iterator();
		while (i.hasNext()) {
			resourceNames.add(i.next().getName().toLowerCase());
		}
		Collections.sort(resourceNames);
		return resourceNames;
	}


}

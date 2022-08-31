/*
 * Mars Simulation Project
 * ItemResourceUtil.java
 * @date 2022-07-12
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.goods.GoodType;

public class ItemResourceUtil implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final String PRESSURE_SUIT = "pressure suit";
	private static final String PETRI_DISH = "petri dish";
	private static final String GARMENT = "garment";
	
	// Light utility vehicle attachment parts for mining or construction.
	private static final String BACKHOE = "backhoe";
	private static final String BULLDOZER_BLADE = "bulldozer blade";
	private static final String CRANE_BOOM = "crane boom";
	private static final String DRILLING_RIG = "drilling rig";
	private static final String PNEUMATIC_DRILL = "pneumatic drill";
	private static final String SOCKET_WRENCH = "socket wrench";
	private static final String SOIL_COMPACTOR = "soil compactor";

	// Tools
	public static final String LASER_SINTERING_3D_PRINTER = "laser sintering 3d printer";

	private static final String PIPE_WRENCH = "pipe wrench";
	private static final String FIRE_EXTINGUSHER = "fire extinguisher";
	private static final String WORK_GLOVES = "work gloves";
	private static final String CONTAINMENT_KIT = "mushroom containment kit";
	private static final String SMALL_HAMMER = "small hammer";

	private static final String IRON_INGOT = "iron ingot";
	private static final String STEEL_INGOT = "steel ingot";
	private static final String IRON_SHEET = "iron sheet";
	private static final String STEEL_SHEET = "steel sheet";

	private static final String ROVER_WHEEL = "rover wheel";
	private static final String CHEMICAL_BATTERY = "chemical battery";
		
	private static final String LASER = "laser";
	private static final String STEPPER_MOTOR = "stepper motor";
	private static final String OVEN = "oven";
	private static final String BLENDER = "blender";
	private static final String AUTOCLAVE = "autoclave";
	private static final String REFRIGERATOR = "refrigerator";
	private static final String STOVE = "stove";
	private static final String MICROWAVE = "microwave";
	private static final String POLY_ROOFING = "polycarbonate roofing";
	private static final String LENS = "lens";
	private static final String FIBERGLASS = "fiberglass";
	private static final String SHEET = "sheet";
	private static final String PRISM = "prism";
	
	/** 
	 * Parts for creating an EVA Suit. 
	 */
	private static final String[] EVASUIT_PARTS = {
			"eva helmet",			"helmet visor",
			PRESSURE_SUIT,			"coveralls",
			"suit heating unit",	"eva gloves",
			"eva boots",			"eva pads",
			"eva backpack",			"eva antenna",
			"eva battery",			"eva radio",
	};

	/** 
	 * Light utility vehicle attachment parts for mining or construction. 
	 */
	private static final String[] ATTACHMENTS = {
			BACKHOE,
			BULLDOZER_BLADE,
			CRANE_BOOM,
			DRILLING_RIG,
			PNEUMATIC_DRILL,
			SOIL_COMPACTOR
	};

	/**
	 * Parts that are not needed to be fetched as repair parts for vehicle missions.
	 */
	public static final String[] UNNEEDED_PARTS = { 
			LASER,					STEPPER_MOTOR,
			OVEN,					BLENDER,
			AUTOCLAVE,				REFRIGERATOR,
			STOVE,					MICROWAVE,
			POLY_ROOFING,			LENS,
			FIBERGLASS,				SHEET,
			PRISM 
	};

	/**
	 * Parts for cooking and food production.
	 */
	private static final String[] KITCHEN_WARE = {
			AUTOCLAVE,			BLENDER,
			MICROWAVE,			OVEN,
			REFRIGERATOR,		STOVE};
	
	public static Part pneumaticDrill;
	public static Part backhoePart;
	public static Part socketWrench;
	public static Part pipeWrench;

	public static Part fireExtinguisher;
	public static Part workGloves;
	public static Part mushroomBox;
	public static Part smallHammer;

	public static Part wheel;
	public static Part battery;
	
	public static int garmentID;
	public static int pressureSuitID;
	public static int PETRI_DISH_ID;

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
	
	public static final List<Integer> ATTACHMENTS_ID = new ArrayList<>();
	public static final List<Integer> EVASUIT_PARTS_ID = new ArrayList<>();
	public static final List<Integer> KITCHEN_WARE_ID = new ArrayList<>();

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
	public static Part createItemResource(String resourceName, int id, String description, GoodType type, double massPerItem,
			int solsUsed) {
		Part p = new Part(resourceName, id, description, type, massPerItem, solsUsed);
		ItemResourceUtil.registerBrandNewPart(p);
		return p;
	}

	/**
	 * Prepares the id's of a few item resources
	 */
	public void createIDs() {
		pneumaticDrill = (Part) findItemResource(PNEUMATIC_DRILL);
		backhoePart = (Part) findItemResource(BACKHOE);
		socketWrench = (Part) findItemResource(SOCKET_WRENCH);
		pipeWrench = (Part) findItemResource(PIPE_WRENCH);

		fireExtinguisher = (Part) findItemResource(FIRE_EXTINGUSHER);
		workGloves = (Part) findItemResource(WORK_GLOVES);
		mushroomBox = (Part) findItemResource(CONTAINMENT_KIT);
		smallHammer = (Part) findItemResource(SMALL_HAMMER);

		wheel = (Part) findItemResource(ROVER_WHEEL);
		battery = (Part) findItemResource(CHEMICAL_BATTERY);
		
		PETRI_DISH_ID = findIDbyItemResourceName(PETRI_DISH);
		garmentID = findIDbyItemResourceName(GARMENT);
		pressureSuitID = findIDbyItemResourceName(PRESSURE_SUIT);

		pneumaticDrillID = findIDbyItemResourceName(PNEUMATIC_DRILL);
		backhoeID = findIDbyItemResourceName(BACKHOE);
		socketWrenchID = findIDbyItemResourceName(SOCKET_WRENCH);
		pipeWrenchID = findIDbyItemResourceName(PIPE_WRENCH);

		fireExtinguisherID = findIDbyItemResourceName(FIRE_EXTINGUSHER);
		workGlovesID = findIDbyItemResourceName(WORK_GLOVES);
		mushroomBoxID = findIDbyItemResourceName(CONTAINMENT_KIT);
		smallHammerID = findIDbyItemResourceName(SMALL_HAMMER);

		printerID = findIDbyItemResourceName(LASER_SINTERING_3D_PRINTER);

		ironIngotID = findIDbyItemResourceName(IRON_INGOT);
		ironSheetID = findIDbyItemResourceName(IRON_SHEET);
		steelIngotID = findIDbyItemResourceName(STEEL_INGOT);
		steelSheetID = findIDbyItemResourceName(STEEL_SHEET);

		for (int i = 0; i < ATTACHMENTS.length; i++) {
			int id = findIDbyItemResourceName(ATTACHMENTS[i]);
			ATTACHMENTS_ID.add(id);
		}

		for (int i = 0; i < EVASUIT_PARTS.length; i++) {
			int id = findIDbyItemResourceName(EVASUIT_PARTS[i]);
			EVASUIT_PARTS_ID.add(id);
		}

		for (int i = 0; i < KITCHEN_WARE.length; i++) {
			int id = findIDbyItemResourceName(KITCHEN_WARE[i]);
			KITCHEN_WARE_ID.add(id);
		}
	}

	/**
	 * Prepares maps for storing all item resources
	 */
	public static void createMaps() {
		itemResourceMap = new HashMap<>();
		sortedParts = new CopyOnWriteArrayList<>(partSet);
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
		sortedParts = new CopyOnWriteArrayList<>(partSet);
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
//	 * Gets an immutable collection of all the item resources.
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
	 * Gets a sorted map of all amount resource names by calling.
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


	/**
	 * Removes a variable list of parts from a resource part
	 *
	 * @param parts a map of parts
	 * @param names
	 * @return a map of parts
	 */
	public static Map<Integer, Double> removeParts(Map<Integer, Double> parts, String... names) {
		for (String n : names) {
			Integer i = ItemResourceUtil.findIDbyItemResourceName(n);
			if (i != null) {
				parts.remove(i);
			}
		}

		return parts;
	}

	public static Map<Integer, Double> removePartMap(Map<Integer, Double> parts, String[] unneeded) {
		for (String n : unneeded) {
			Integer i = ItemResourceUtil.findIDbyItemResourceName(n);
			if (i != null) {
				parts.remove(i);
			}
		}

		return parts;
	}
	
	

}

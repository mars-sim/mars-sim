/*
 * Mars Simulation Project
 * ItemResourceUtil.java
 * @date 2022-12-31
 * @author Manny Kung
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.goods.GoodType;

public class ItemResourceUtil implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final String PRESSURE_SUIT = "pressure suit";
	private static final String GARMENT = "garment";
	
	public static final String BATTERY_MODULE = "battery module";
	public static final String ROVER_WHEEL = "rover wheel";
	public static final String FIBERGLASS = "fiberglass";
	
	// Light utility vehicle attachment parts for mining or construction.
	private static final String BACKHOE = "backhoe";
	private static final String PNEUMATIC_DRILL = "pneumatic drill";

	// Tools
	private static final String LASER_SINTERING_3D_PRINTER = "laser sintering 3d printer";
	
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

	public static int garmentID;
	public static int pressureSuitID;

	public static int pneumaticDrillID;
	public static int backhoeID;

	public static int printerID;

	private static Map<String, Part> itemResourceMap;
	private static Map<Integer, Part> itemResourceIDMap;

	private static Set<Part> partSet;

	private static List<Part> sortedParts;

	private static PartConfig partConfig = SimulationConfig.instance().getPartConfiguration();
	
	//TODO These should be configurable
	public static Set<Integer> EVASUIT_PARTS_ID;

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
	public static void createIDs() {

		// Create item ids reference
		garmentID = findIDbyItemResourceName(GARMENT);
		pressureSuitID = findIDbyItemResourceName(PRESSURE_SUIT);

		pneumaticDrillID = findIDbyItemResourceName(PNEUMATIC_DRILL);
		backhoeID = findIDbyItemResourceName(BACKHOE);

		printerID = findIDbyItemResourceName(LASER_SINTERING_3D_PRINTER);
		
		EVASUIT_PARTS_ID = convertNamesToResourceIDs(EVASUIT_PARTS);
	}

	/**
	 * Convet a array of ItemResources into their equivalent ID.
	 * @param names
	 * @return
	 */
	public static Set<Integer> convertNamesToResourceIDs(String [] names) {
		Set<Integer> ids = new HashSet<>();
		for (String n : names) {
			ItemResource item = findItemResource(n);
			if (item != null) {
				ids.add(item.getID());
			}
		}
		return ids;
	}

	/**
	 * Prepares maps for storing all item resources
	 */
	private static void createMaps() {
		itemResourceMap = new HashMap<>();
		sortedParts = new CopyOnWriteArrayList<>(partSet);
		Collections.sort(sortedParts);

		for (Part p : sortedParts) {
			itemResourceMap.put(p.getName().toLowerCase(), p);
		}

		itemResourceIDMap = new HashMap<>();
		for (Part p : sortedParts) {
			itemResourceIDMap.put(p.getID(), p);
		}
	}

	/**
	 * Register a new part in all 3 item resource maps
	 *
	 * @param p {@link Part}
	 */
	public static void registerBrandNewPart(Part p) {
		itemResourceMap.put(p.getName().toLowerCase(), p);
		itemResourceIDMap.put(p.getID(), p);
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
		Part ir = getItemResources().stream().filter(item -> item.getName().equalsIgnoreCase(name)).findFirst()
				.orElse(null);
		if (ir == null) {
			throw new IllegalArgumentException("No ItemResource called " + name);	
		}

		return ir;
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
	 * Finds an item resource name by id.
	 *
	 * @param id the resource's id.
	 * @return resource name
	 * @throws ResourceException if resource could not be found.
	 */
	public static String findItemResourceName(int id) {
		return findItemResource(id).getName();
	}

	/**
	 * Finds the id of the item resource by name.
	 *
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	
	public static Integer findIDbyItemResourceName(String name) {
		ItemResource ir = findItemResource(name);
		return ir.getID();
	}

	public static Map<Integer, Double> removePartMap(Map<Integer, Double> parts, Set<Integer> unneeded) {
		for (Integer i : unneeded) {
			parts.remove(i);
		}
		return parts;
	}
	
	

}

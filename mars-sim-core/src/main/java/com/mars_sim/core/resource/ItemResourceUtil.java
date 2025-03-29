/*
 * Mars Simulation Project
 * ItemResourceUtil.java
 * @date 2024-07-12
 * @author Manny Kung
 */

package com.mars_sim.core.resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.goods.GoodType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.process.ProcessItem;

public class ItemResourceUtil {

	
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ItemResourceUtil.class.getName());

	private static final String EXTINGUISHER = "fire extinguisher";
	private static final String PATCH = "airleak patch";
	private static final String GLOVE = "work gloves";
	
	private static final String PRESSURE_SUIT = "pressure suit";
	private static final String GARMENT = "garment";
	
	public static final String BATTERY_MODULE = "battery module";
	public static final String ROVER_WHEEL = "rover wheel";
	public static final String FIBERGLASS = "fiberglass";
	
	// Light utility vehicle attachment parts for mining or construction.
	private static final String BACKHOE = "backhoe";
	private static final String PNEUMATIC_DRILL = "pneumatic drill";

	/** String name of the manufacturing process of producing an EVA suit. */	
	private static final String ASSEMBLE_EVA_SUIT = "Assemble EVA suit";
	
	// 3-D printer
	private static final String SLS_3D_PRINTER = "SLS 3D Printer";

	public static int garmentID;
	public static int pressureSuitID;
	public static int pneumaticDrillID;
	public static int backhoeID;
	public static int printerID;

	private static Map<String, Part> itemResourceMap;
	private static Map<Integer, Part> itemResourceIDMap;
	private static Set<Part> partSet;
	
	public static Set<Integer> evaSuitPartIDs;

	/** A set of common parts that will be consumed during a malfunction repair. */
	public static Set<Integer> consumablePartIDs;

	/**
	 * Constructor.
	 */
	private ItemResourceUtil() {
	}

	/**
	 * Register the parts of the simulation.
	 * @param parts
	 */
	public static void registerParts(Set<Part> parts) {
		partSet = parts;
		createMaps();
		createIDs();
	}
	
	/**
	 * Initializes the consumable parts for use during malfunction.
	 */
	public static void initConsumableParts() {
		if (consumablePartIDs == null) {
			consumablePartIDs = Set.of(findIDbyItemResourceName(EXTINGUISHER),
										findIDbyItemResourceName(PATCH),
										findIDbyItemResourceName(GLOVE));
		}
	}
	
	/**
	 * Initializes the EVA suit parts.
	 */
	public static void initEVASuit() {
		if (evaSuitPartIDs == null || evaSuitPartIDs.isEmpty()) {

			ManufactureProcessInfo manufactureProcessInfo = null;
			
			var manufactureConfig = SimulationConfig.instance().getManufactureConfiguration();
			
			for (ManufactureProcessInfo info : manufactureConfig.getManufactureProcessList()) {
				if (info.getName().equals(ASSEMBLE_EVA_SUIT)) {
		        	manufactureProcessInfo = info;
					evaSuitPartIDs = info.getInputList().stream().map(ProcessItem::getId).collect(Collectors.toSet());
		        	break;
				}
			}

			if (manufactureProcessInfo == null)
				logger.config("Unable to find EVA suit part IDs.");
		}
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
	 * Prepares the id's of a few item resources.
	 */
	private static void createIDs() {

		// Create item ids reference
		garmentID = findIDbyItemResourceName(GARMENT);
		pressureSuitID = findIDbyItemResourceName(PRESSURE_SUIT);

		pneumaticDrillID = findIDbyItemResourceName(PNEUMATIC_DRILL);
		backhoeID = findIDbyItemResourceName(BACKHOE);

		printerID = findIDbyItemResourceName(SLS_3D_PRINTER);
	}

	/**
	 * Converts a array of string names into their equivalent IDs.
	 * Note: Currently, it will look for parts only.
	 * 
	 * @param name array
	 * @return a set of ids
	 */
	public static Set<Integer> convertNameArray2ResourceIDs(String [] names) {
		Set<Integer> ids = new HashSet<>();
		for (String n : names) {
			
			AmountResource ar = ResourceUtil.findAmountResource(n);
			if (ar != null) {
				// Not including amount resources
//				ids.add(ar.getID());
			}		
			else {
				ItemResource item = findItemResource(n);
				if (item != null) {
					ids.add(item.getID());
				}
			}
		}
		return ids;
	}

	/**
	 * Prepares maps for storing all item resources.
	 */
	private static void createMaps() {
		itemResourceMap = new HashMap<>();

		for (Part p : partSet) {
			itemResourceMap.put(p.getName().toLowerCase(), p);
		}

		itemResourceIDMap = new HashMap<>();
		for (Part p : partSet) {
			itemResourceIDMap.put(p.getID(), p);
		}
	}

	/**
	 * Registers a new part in all 3 item resource maps.
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
	 * Creates a set of item resources.
	 *
	 * @return
	 */
	public static Set<Part> getItemResources() {
		return partSet;
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

	/**
	 * Removes an unneeded part from the part map.
	 * 
	 * @param parts
	 * @param unneeded
	 * @return
	 */
	public static Map<Integer, Double> removePartMap(Map<Integer, Double> parts, Set<Integer> unneeded) {
		for (Integer i : unneeded) {
			parts.remove(i);
		}
		return parts;
	}
}

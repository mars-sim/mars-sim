/*
 * Mars Simulation Project
 * ItemResourceUtil.java
 * @date 2025-10-02
 * @author Manny Kung
 */

package com.mars_sim.core.resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.process.ProcessItem;

public class ItemResourceUtil {

	
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ItemResourceUtil.class.getName());

	private static final int FIRST_ITEM_RESOURCE_ID = ResourceUtil.FIRST_ITEM_RESOURCE_ID;
	
	public static final int AIRLEAK_PATCH_ID = FIRST_ITEM_RESOURCE_ID;
	public static final int BATTERY_MODULE_ID = AIRLEAK_PATCH_ID + 1;
	
	public static final int FIRE_EXTINGUISHER_ID = BATTERY_MODULE_ID + 1;
	public static final int FIBERGLASS_ID = FIRE_EXTINGUISHER_ID + 1;
	public static final int GARMENT_ID = FIBERGLASS_ID + 1;
	public static final int WORK_GLOVES_ID = GARMENT_ID + 1;
	
	public static final int METHANE_FUEL_CELL_ID = WORK_GLOVES_ID + 1;
	public static final int METHANOL_FUEL_CELL_ID = METHANE_FUEL_CELL_ID + 1;
	public static final int METHANE_FUEL_CELL_STACK_ID = METHANOL_FUEL_CELL_ID + 1;
	public static final int METHANOL_FUEL_CELL_STACK_ID = METHANE_FUEL_CELL_STACK_ID + 1;
	
	public static final int PRESSURE_SUIT_ID = METHANOL_FUEL_CELL_STACK_ID + 1;
	public static final int ROVER_WHEEL_ID = PRESSURE_SUIT_ID + 1;
	public static final int SLS_3D_PRINTER_ID = ROVER_WHEEL_ID + 1;
	
	// Light utility vehicle attachment parts for mining or construction.
	public static final int BACKHOE_ID = SLS_3D_PRINTER_ID + 1;
	public static final int PNEUMATIC_DRILL_ID = BACKHOE_ID + 1;
	
	// Must be one after the last fixed resource
	public static final int FIRST_FREE_ITEM_RESOURCE_ID = PNEUMATIC_DRILL_ID + 1;
	
	// Light utility vehicle attachment parts for mining or construction.

	/** String name of the manufacturing process of producing an EVA suit. */	
	private static final String ASSEMBLE_EVA_SUIT = "Assemble EVA suit";
	
	/** String name of the manufacturing process of producing a repair bot. */	
	public static final String ASSEMBLE_A_REPARTBOT = "Assemble a RepairBot";
	
	private static Map<String, Part> itemResourceByName;
	private static Map<Integer, Part> itemResourceByID;
	private static Set<Part> partSet;
	
	public static Set<Integer> evaSuitPartIDs;
	public static Set<Integer> botPartIDs;
	
	/** A set of common parts that will be consumed during a malfunction repair. */
	public static Set<Integer> consumablePartIDs;

	private static final Map<String, Integer> fixedItemResources = new HashMap<>();

	static {
		// Map the pre-defined resources to their names
		fixedItemResources.put("airleak patch", AIRLEAK_PATCH_ID);
		fixedItemResources.put("battery module",  BATTERY_MODULE_ID);
		fixedItemResources.put("fire extinguisher", FIRE_EXTINGUISHER_ID);
		fixedItemResources.put("fiberglass", FIBERGLASS_ID);
		
		fixedItemResources.put("garment", GARMENT_ID);
		fixedItemResources.put("work gloves", WORK_GLOVES_ID);
		
		fixedItemResources.put("pressure suit", PRESSURE_SUIT_ID);
		fixedItemResources.put("sls 3d printer", SLS_3D_PRINTER_ID);
		fixedItemResources.put("rover wheel", ROVER_WHEEL_ID);
		fixedItemResources.put("backhoe", BACKHOE_ID);
		fixedItemResources.put("pneumatic drill", PNEUMATIC_DRILL_ID);
		
		// Map the pre-defined resources to their names
		fixedItemResources.put("methane fuel cell", METHANE_FUEL_CELL_ID);
		fixedItemResources.put("methanol fuel cell", METHANOL_FUEL_CELL_ID);
		fixedItemResources.put("methane fuel cell stack", METHANE_FUEL_CELL_STACK_ID);
		fixedItemResources.put("methanol fuel cell stack", METHANOL_FUEL_CELL_STACK_ID);
		
		// This check will only fail if a new resource has not been added correctly
		int expectedSize = FIRST_FREE_ITEM_RESOURCE_ID - FIRST_ITEM_RESOURCE_ID;
		if (fixedItemResources.size() != expectedSize) {
			throw new IllegalStateException("The number of fixed item resources is not correct. Expected: " 
					+ expectedSize + ", Actual: " + fixedItemResources.size());
		}
	}
		
	/**
	 * Default Constructor for ItemResourceUtil.
	 */
	private ItemResourceUtil() {
	}

	/**
	 * Registers the known parts in the helper.
	 * 
	 * @param parts
	 */
	public static void registerParts(Set<Part> parts) {
		partSet = parts;
		createMaps();
		
		// Double check all predefined item resources are in the list
		// Cause could be a missing item resource in the XMLfile
		var missingFixed = new HashSet<>(fixedItemResources.values());
		missingFixed.removeAll(itemResourceByID.keySet());
		if (!missingFixed.isEmpty()) {
			// Display the missing resources
			var missingFixedNames = fixedItemResources.entrySet().stream()
					.filter(entry -> missingFixed.contains(entry.getValue()))
					.map(Map.Entry::getKey)
					.collect(Collectors.joining(", "));
			throw new IllegalStateException("The following fixed item resources are missing: " + missingFixedNames);
		}
	}
	
	/**
	 * Prepares maps for storing all item resources.
	 */
	private static synchronized void createMaps() {
		if (itemResourceByName == null) {

			Map<String, Part> tempItemResourceMap = new HashMap<>();		
			Map<Integer, Part> tempItemResourceIDMap = new HashMap<>();
			
			for (Part p : partSet) {
				tempItemResourceMap.put(p.getName().toLowerCase(), p);
				tempItemResourceIDMap.put(p.getID(), p);
			}
			
			// Create immutable internals
			itemResourceByName = Collections.unmodifiableMap(tempItemResourceMap);
			itemResourceByID = Collections.unmodifiableMap(tempItemResourceIDMap);
		}
	}

	/**
	 * Maps an id to an item resources.
	 */
	public static int getFixedId(String resourceName) {
		return fixedItemResources.getOrDefault(resourceName.toLowerCase(), -1);
	}
	
	
	/**
	 * Initializes the consumable parts for use during malfunction.
	 */
	public static void initConsumableParts() {
		if (consumablePartIDs == null) {
			consumablePartIDs = Set.of(FIRE_EXTINGUISHER_ID,
										AIRLEAK_PATCH_ID,
										WORK_GLOVES_ID);
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
				if (info.getName().equalsIgnoreCase(ASSEMBLE_EVA_SUIT)) {
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
	 * Initializes the bot parts.
	 */
	public static void initBotParts() {
		if (botPartIDs == null || botPartIDs.isEmpty()) {

			ManufactureProcessInfo manufactureProcessInfo = null;
			
			var manufactureConfig = SimulationConfig.instance().getManufactureConfiguration();
			
			for (ManufactureProcessInfo info : manufactureConfig.getManufactureProcessList()) {
				if (info.getName().equalsIgnoreCase(ASSEMBLE_A_REPARTBOT)) {
		        	manufactureProcessInfo = info;
		        	botPartIDs = info.getInputList().stream().map(ProcessItem::getId).collect(Collectors.toSet());
		        	break;
				}
			}

			if (manufactureProcessInfo == null)
				logger.config("Unable to find bot part IDs.");
		}
	}
	
	/**
	 * Converts a array of string item names into their equivalent IDs.
	 * Note: Currently, it will look for parts only.
	 * 
	 * @param name array
	 * @return a set of ids
	 */
	public static Set<Integer> convertNameArray2ResourceIDs(String [] names) {
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
	 * Finds an item resource by name.
	 *
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static ItemResource findItemResource(String name) {
		Part ir = itemResourceByName.get(name.toLowerCase());
		if (ir == null) {
			throw new IllegalArgumentException("Part '" + name + "' not found.");	
		}

		return ir;
	}

	/**
	 * Finds an item resource by id.
	 *
	 * @param id the resource's id.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static Part findItemResource(int id) {
		Part ir = itemResourceByID.get(id);
		if (ir == null) {
			throw new IllegalArgumentException("Part '" + id + "' not found.");
		}
		return ir;
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
		return findItemResource(name).getID();
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

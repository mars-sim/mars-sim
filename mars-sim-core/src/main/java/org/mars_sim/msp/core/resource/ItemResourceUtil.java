/*
 * Mars Simulation Project
 * ItemResourceUtil.java
 * @date 2023-05-16
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
import org.mars_sim.msp.core.manufacture.ManufactureConfig;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;

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

	/** String name of the manufacturing process of producing an EVA suit. */	
	private static final String ASSEMBLE_EVA_SUIT = "Assemble EVA suit";
	
	private static final String[] ASSEMBLE_ROVER = {"Assemble explorer rover",
	                                                "Assemble long range explorer",
	                                                "Assemble cargo rover",
	                                                "Assemble transport rover",
	                                                "Assemble light utility vehicle",
	                                                "Assemble delivery drone"};
	
	/** 
	 * Parts for creating an EVA Suit. 
	 */
	private static List<String> evaSuitParts;
//			"eva helmet",			"helmet visor",
//			PRESSURE_SUIT,			"coveralls",
//			"suit heating unit",	"eva gloves",
//			"eva boots",			"eva pads",
//			"eva backpack",			"eva antenna",
//			"eva battery",			"eva radio",
	

	
	// 3-D printer
	private static final String LASER_SINTERING_3D_PRINTER = "laser sintering 3d printer";

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
	private static ManufactureConfig manufactureConfig = SimulationConfig.instance().getManufactureConfiguration();
	
	public static Set<Integer> evaSuitPartIDs;
	
	public static Map<Integer, Set<Integer>> vehiclePartIDs = new HashMap<>();
	// 0 : Explorer Rover
	// 1 : Long Range Explorer 
	// 2 : transport rover
	// 3 : cargo rover
	// 4 : luv
	// 5 : delivery drone

	private static Map<Integer, List<String>> vehicleParts = new HashMap<>();
	
	/**
	 * Constructor.
	 */
	public ItemResourceUtil() {
		partSet = getItemResources();
		createMaps();
		createIDs();
	}
	
	public static double initEVASuit() {

		double calculatedEmptyMass = 0;
		
		if (evaSuitPartIDs == null || evaSuitPartIDs.isEmpty()) {

			ManufactureProcessInfo manufactureProcessInfo = null;
			
			if (manufactureConfig == null)
				manufactureConfig = SimulationConfig.instance().getManufactureConfiguration();
			
			for (ManufactureProcessInfo info : manufactureConfig.getManufactureProcessList()) {
				if (info.getName().equals(ASSEMBLE_EVA_SUIT)) {
		        		manufactureProcessInfo = info;
				}
			}

			evaSuitParts = manufactureProcessInfo.getInputNames();
		
			evaSuitPartIDs = convertNameListToResourceIDs(evaSuitParts);

			// Calculate total mass as the summation of the multiplication of the quantity and mass of each part 
			calculatedEmptyMass = manufactureProcessInfo.calculateTotalInputMass();
		}
		
		return calculatedEmptyMass;
	}
	
	public static double initVehicle(int type) {

		double calculatedEmptyMass = 0;
		
		if (vehiclePartIDs.isEmpty()|| vehiclePartIDs.get(type) == null) {

			ManufactureProcessInfo manufactureProcessInfo = null;
			
			if (manufactureConfig == null)
				manufactureConfig = SimulationConfig.instance().getManufactureConfiguration();
			
			for (ManufactureProcessInfo info : manufactureConfig.getManufactureProcessList()) {
				if (info.getName().equals(ASSEMBLE_ROVER[type])) {
		        		manufactureProcessInfo = info;
				}
			}
			
			List<String> names = manufactureProcessInfo.getInputNames();
			
			
			vehicleParts.put(type, names);
		
			Set<Integer> ids = convertNameListToResourceIDs(names);
			
			vehiclePartIDs.put(type, ids);
			
			// Calculate total mass as the summation of the multiplication of the quantity and mass of each part  
			calculatedEmptyMass = manufactureProcessInfo.calculateTotalInputMass();
		}
		
		return calculatedEmptyMass;
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
	public static void createIDs() {

		// Create item ids reference
		garmentID = findIDbyItemResourceName(GARMENT);
		pressureSuitID = findIDbyItemResourceName(PRESSURE_SUIT);

		pneumaticDrillID = findIDbyItemResourceName(PNEUMATIC_DRILL);
		backhoeID = findIDbyItemResourceName(BACKHOE);

		printerID = findIDbyItemResourceName(LASER_SINTERING_3D_PRINTER);
	}

	
	/**
	 * Convert a list of string into their equivalent IDs.
	 * 
	 * @param string list
	 * @return a set of ids
	 */
	public static Set<Integer> convertNameListToResourceIDs(List<String> strings) {
		return convertNameArray2ResourceIDs(strings.stream()
		        .toArray(String[]::new));
	}
	
	/**
	 * Converts a array of string names into their equivalent IDs.
	 * 
	 * @param name array
	 * @return a set of ids
	 */
	public static Set<Integer> convertNameArray2ResourceIDs(String [] names) {
		Set<Integer> ids = new HashSet<>();
		for (String n : names) {
			
			AmountResource ar = ResourceUtil.findAmountResource(n);
			if (ar != null) {
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
		if (partConfig == null)
			partConfig = SimulationConfig.instance().getPartConfiguration();
		if (partSet == null)
			partSet = Collections.unmodifiableSet(partConfig.getPartSet());
		return partSet;
	}

	/**
	 * Gets a list of sorted parts.
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

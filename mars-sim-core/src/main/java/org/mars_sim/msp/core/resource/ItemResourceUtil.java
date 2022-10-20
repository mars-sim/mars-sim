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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.goods.GoodType;

public class ItemResourceUtil implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

//	private static final String WHEELBARROW = "wheelbarrow";
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
	private static final String[] UNNEEDED_PARTS = { 
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

	public static int wheelbarrowID;
	
	public static int garmentID;
	public static int pressureSuitID;
	public static int petriDishID;

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

	private static Set<Part> partSet;

	private static List<Part> sortedParts;

	private static PartConfig partConfig = SimulationConfig.instance().getPartConfiguration();
	
	//TODO These should be configurable
	public static List<Integer> ATTACHMENTS_ID;
	public static List<Integer> EVASUIT_PARTS_ID;
	public static List<Integer> KITCHEN_WARE_ID;
	public static List<Integer> UNNEEDED_PARTS_ID;

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
		// Create parts reference
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
		
		// Create item ids reference
		petriDishID = findIDbyItemResourceName(PETRI_DISH);
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
		
		ATTACHMENTS_ID = convertNamesToResourceIDs(ATTACHMENTS);
		EVASUIT_PARTS_ID = convertNamesToResourceIDs(EVASUIT_PARTS);
		KITCHEN_WARE_ID = convertNamesToResourceIDs(KITCHEN_WARE);
		UNNEEDED_PARTS_ID = convertNamesToResourceIDs(UNNEEDED_PARTS);
	}

	private static List<Integer> convertNamesToResourceIDs(String [] names) {
		List<Integer> ids = new ArrayList<>();
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
		return getItemResources().stream().filter(item -> item.getName().equalsIgnoreCase(name)).findFirst()
				.orElse(null);
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
	 * Finds an amount resource by name.
	 *
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static Integer findIDbyItemResourceName(String name) {
		return findItemResource(name).getID();
	}

	public static Map<Integer, Double> removePartMap(Map<Integer, Double> parts, List<Integer> unneeded) {
		for (Integer i : unneeded) {
			parts.remove(i);
		}
		return parts;
	}
	
	

}

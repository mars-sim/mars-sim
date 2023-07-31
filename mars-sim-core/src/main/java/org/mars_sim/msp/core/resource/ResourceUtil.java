/*
 * Mars Simulation Project
 * ResourceUtil.java
 * @date 2022-07-18
 * @author Manny Kung
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.food.Food;
import org.mars_sim.msp.core.food.FoodUtil;
import org.mars_sim.msp.core.logging.SimLogger;

public class ResourceUtil implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ResourceUtil.class.getName());
	
	public static final int FIRST_AMOUNT_RESOURCE_ID = 200;
	public static final int FIRST_ITEM_RESOURCE_ID = 500;
	public static final int FIRST_VEHICLE_RESOURCE_ID = 1000;
	public static final int FIRST_EQUIPMENT_RESOURCE_ID = 1010;
	public static final int FIRST_ROBOT_RESOURCE_ID = 1020;
	public static final int FIRST_BIN_RESOURCE_ID = 1040;
	
	public static final String OXYGEN = "oxygen";
	public static final String WATER = "water";
	public static final String FOOD = "food";

	public static final String ARGON = "argon";
	public static final String NITROGEN = "nitrogen";
	public static final String CO2 = "carbon dioxide";
	public static final String CO = "carbon monoxide";
	
	public static final String CHLORINE = "chlorine";
	public static final String ETHYLENE = "ethylene";
	public static final String PROPHYLENE = "prophylene";
	
	public static final String HYDROGEN = "hydrogen";
	public static final String METHANE = "methane";
	public static final String METHANOL = "methanol";
	
	public static final String SOIL = "soil";
	public static final String ICE = "ice";
	public static final String COMPOST = "compost";

	public static final String REGOLITH = "regolith";
	public static final String REGOLITH_B = "regolith-b";
	public static final String REGOLITH_C = "regolith-c";
	public static final String REGOLITH_D = "regolith-d";

	public static final String ROCK_SAMPLES = "rock samples";
	public static final String CONCRETE = "concrete";
	public static final String MORTAR = "mortar";
	public static final String CEMENT = "cement";

	public static final String SAND = "sand";

	public static final String METEORITE = "meteorite";

	public static final String ELECTRONIC_WASTE = "electronic waste";
	public static final String CROP_WASTE = "crop waste";
	public static final String FOOD_WASTE = "food waste";
	public static final String SOLID_WASTE = "solid waste";
	public static final String TOXIC_WASTE = "toxic waste";

	public static final String BRINE_WATER = "brine water";
	public static final String GREY_WATER = "grey water";
	public static final String BLACK_WATER = "black water";
	
	public static final String TABLE_SALT = "table salt";
	public static final String ROCK_SALT = "rock salt";
	public static final String EPSOM_SALT = "epsom salt";

	public static final String SODIUM_HYPOCHLORITE = "sodium hypochlorite";
	public static final String NAPKIN = "napkin";

	public static final String FERTILIZER = "fertilizer";

	public static final String SOYBEAN_OIL = "soybean oil";
	public static final String GARLIC_OIL = "garlic oil";
	public static final String SESAME_OIL = "sesame oil";
	public static final String PEANUT_OIL = "peanut oil";
	public static final String RICE_BRAN_OIL = "rice bran oil";
	public static final String FISH_OIL = "fish oil";
	public static final String TOILET_TISSUE = "toilet tissue";
	public static final String SOYMILK = "soymilk";
	public static final String LEAVES = "leaves";
	public static final String FISH_MEAT = "fish meat";

	protected static Set<Integer> essentialResources;
	
	public static final String[] ROCKS  = new String[] {
			ROCK_SAMPLES,
			"columnar basalt",
			"granite",
			"mudstone",
			"sandstone",
			"shale",
			"conglomerate",
			"cross bedding",
			METEORITE,
			"scoria"};

	protected static final String[] MINERAL_CONCENTRATIONS = new String[] {
	        "chalcopyrite",
			"goethite",
			"hematite",
			"kamacite",
			"magnesite",
			"magnetite",
			"malachite",
			"olivine",
			"taenite",
			"sylvite"};

	protected static final String[] ORE_DEPOSITS = new String[] {
			"allophane",
			"akaganeite",
			"basaltic",
			"bassanite",
			"gypsum",
			"smectite"};

	// Data members.
	private static Map<String, AmountResource> amountResourceMap;
	private static Map<Integer, AmountResource> amountResourceIDMap;
	private static Set<AmountResource> resources;
	private static List<AmountResource> sortedResources;
	
	/** A set of life support resources. */
	private static Set<Integer> lifeSupportResources;

	public static int waterID;
	public static int foodID;

	public static int oxygenID;
	public static int co2ID;
	public static int argonID;
	public static int nitrogenID;
	public static int hydrogenID;
	public static int methaneID;
	public static int methanolID;
	public static int coID;
	
	public static int chlorineID;
	public static int ethyleneID;
	public static int prophyleneID;
			
	public static int iceID;

	public static int regolithID;
	public static int regolithBID;
	public static int regolithCID;
	public static int regolithDID;

	public static int soilID;
	public static int sandID;
	public static int soymilkID;

	public static int rockSamplesID;

	public static int blackWaterID;
	public static int greyWaterID;
	
	public static int brineWaterID;

	public static int mortarID;
	public static int concreteID;
	public static int cementID;

	public static int compostID;
	public static int cropWasteID;
	public static int foodWasteID;
	public static int toxicWasteID;
	public static int solidWasteID;

	public static int toiletTissueID;

	public static int fertilizerID;
	public static int NaClOID;

	public static int leavesID;

	public static int soybeanOilID;
	public static int garlicOilID;
	public static int sesameOilID;
	public static int peanutOilID;
	public static int riceBranOilID;
	public static int fishOilID;

	public static int epsomSaltID;
	public static int tableSaltID;
	public static int rockSaltID;

	public static int eWasteID;

	public static int napkinID;

	public static int meteoriteID;

	public static int[] rockIDs = new int[ROCKS.length];
	public static int[] mineralConcIDs = new int[MINERAL_CONCENTRATIONS.length];
	public static int[] oreDepositIDs = new int[ORE_DEPOSITS.length];
	public static int[] REGOLITH_TYPES = new int[4];

	public static int fishMeatID;

	public static AmountResource foodAR;
	public static AmountResource oxygenAR;
	public static AmountResource waterAR;

	public static AmountResource iceAR;
	
	public static AmountResource hydrogenAR;
	public static AmountResource methaneAR;
	public static AmountResource methanolAR;
	public static AmountResource nitrogenAR;
	
	public static AmountResource argonAR;
	public static AmountResource carbonDioxideAR;
	public static AmountResource coAR;

	public static AmountResource regolithAR;
	public static AmountResource regolithBAR;
	public static AmountResource regolithCAR;
	public static AmountResource regolithDAR;

	public static AmountResource NaClOAR;
	public static AmountResource greyWaterAR;
	public static AmountResource blackWaterAR;

	public static AmountResource rockSamplesAR;
	public static AmountResource sandAR;

	//private static AmountResource[] ARs = new AmountResource[33];

	/**
	 * Creates the singleton instance.
	 */
	private static ResourceUtil INSTANCE = new ResourceUtil();

	/**
	 * Gets the singleton instance.
	 *
	 * @param instance the singleton instance.
	 */
	public static ResourceUtil getInstance() {
		return INSTANCE;
	}

	/**
	 * Default Constructor for ResoureUtil.
	 */
	private ResourceUtil() {
		resources = SimulationConfig.instance().getResourceConfiguration().getAmountResources();
		createItemResourceUtil();
		createLifeSupportResources();
		createEssentialResources();
	}


	/**
	 * Creates a set of life support resources.
	 */
	private static void createLifeSupportResources() {
		lifeSupportResources = new HashSet<>();
		for (AmountResource ar: resources) {
			if (ar.isLifeSupport())
				lifeSupportResources.add(ar.getID());
		}
	}
	
	/**
	 * Checks if this resource is a life support resource.
	 * 
	 * @param id
	 * @return
	 */
	public static boolean isLifeSupport(int id) {
		for (int i: getLifeSupportResources()) {
			if (i == id)
				return true;
		}
		return false;
	}

	/**
	 * Gets a set of life support resources.
	 * 
	 * @return
	 */
	public static Set<Integer> getLifeSupportResources() {
		return lifeSupportResources;
	}
	
	/**
	 * Creates a set of essential resources.
	 */
	private static void createEssentialResources() {
		essentialResources = new HashSet<>();
		for (Food f: FoodUtil.getFoodList()) {
			essentialResources.add(f.getID());
		}
		
		for (int i: mineralConcIDs) {
			essentialResources.add(i);
		}
		for (int i: oreDepositIDs) {
			essentialResources.add(i);
		}
		for (int i: REGOLITH_TYPES) {
			essentialResources.add(i);
		}
		for (int j: oreDepositIDs) {
			essentialResources.add(j);
		}
		
		essentialResources.addAll(lifeSupportResources);
	}
	
	/**
	 * Gets a set of essential resources.
	 * 
	 * @return
	 */
	public static Set<Integer> getEssentialResources() {
		return essentialResources;
	}
	
	/**
	 * Starts ItemResourceUtil
	 */
	public void createItemResourceUtil() {
		new ItemResourceUtil();
	}

	/**
	 * Sets the singleton instance.
	 *
	 * @param instance the singleton instance.
	 */
	public static void setInstance(ResourceUtil instance) {
		ResourceUtil.INSTANCE = instance;
	}

	/**
	 * Recreates the Amount Resource instances in all map.
	 */
	public void initializeInstances() {
		// Create maps
		createMaps();
		// Map the static instances
		mapInstances();
	}

	/**
	 * Creates maps of amount resources.
	 */
	public static synchronized void createMaps() {
		if (amountResourceMap == null) {
			sortedResources = new ArrayList<>(resources);
			Collections.sort(sortedResources);

			Map<String, AmountResource> tempAmountResourceMap = new HashMap<>();
			for (AmountResource resource : sortedResources) {
				tempAmountResourceMap.put(resource.getName().toLowerCase(), resource);
			}

			Map<Integer, AmountResource> tempAmountResourceIDMap = new HashMap<>();
			for (AmountResource resource : sortedResources) {
				tempAmountResourceIDMap.put(resource.getID(), resource);
			}

			// Create immutable internals
			amountResourceMap = Collections.unmodifiableMap(tempAmountResourceMap);
			amountResourceIDMap = Collections.unmodifiableMap(tempAmountResourceIDMap);
		}
	}

	/**
	 * Maps ids to amount resources.
	 */
	private static void mapInstances() {

		// AmountResource instances as Integer
		foodID = findIDbyAmountResourceName(FOOD); // 1
		waterID = findIDbyAmountResourceName(WATER); // 2

		oxygenID = findIDbyAmountResourceName(OXYGEN); // 3
		co2ID = findIDbyAmountResourceName(CO2);
		argonID = findIDbyAmountResourceName(ARGON);
		coID = findIDbyAmountResourceName(CO); 

		hydrogenID = findIDbyAmountResourceName(HYDROGEN); 
		methaneID = findIDbyAmountResourceName(METHANE);
		methanolID = findIDbyAmountResourceName(METHANOL);
		nitrogenID = findIDbyAmountResourceName(NITROGEN); 

		chlorineID = findIDbyAmountResourceName(CHLORINE); 
		ethyleneID = findIDbyAmountResourceName(ETHYLENE); 
		prophyleneID = findIDbyAmountResourceName(PROPHYLENE); 
		
		iceID = findIDbyAmountResourceName(ICE); 

		blackWaterID = findIDbyAmountResourceName(BLACK_WATER);
		greyWaterID = findIDbyAmountResourceName(GREY_WATER);
		brineWaterID = findIDbyAmountResourceName(BRINE_WATER);
		
		cropWasteID = findIDbyAmountResourceName(CROP_WASTE);
		foodWasteID = findIDbyAmountResourceName(FOOD_WASTE);
		toxicWasteID = findIDbyAmountResourceName(TOXIC_WASTE);
		solidWasteID = findIDbyAmountResourceName(SOLID_WASTE);
		eWasteID = findIDbyAmountResourceName(ELECTRONIC_WASTE);
		compostID = findIDbyAmountResourceName(COMPOST); 

		fertilizerID = findIDbyAmountResourceName(FERTILIZER);

		leavesID = findIDbyAmountResourceName(LEAVES);

		meteoriteID = findIDbyAmountResourceName(METEORITE);

		soilID = findIDbyAmountResourceName(SOIL);
		sandID = findIDbyAmountResourceName(SAND);
		concreteID = findIDbyAmountResourceName(CONCRETE);
		mortarID = findIDbyAmountResourceName(MORTAR);
		cementID = findIDbyAmountResourceName(CEMENT);

		rockSamplesID = findIDbyAmountResourceName(ROCK_SAMPLES);

		soymilkID = findIDbyAmountResourceName(SOYMILK);

		NaClOID = findIDbyAmountResourceName(SODIUM_HYPOCHLORITE);

		soybeanOilID = findIDbyAmountResourceName(SOYBEAN_OIL);
		garlicOilID = findIDbyAmountResourceName(GARLIC_OIL);
		sesameOilID = findIDbyAmountResourceName(SESAME_OIL);
		peanutOilID = findIDbyAmountResourceName(PEANUT_OIL);
		riceBranOilID = findIDbyAmountResourceName(RICE_BRAN_OIL);
		fishOilID = findIDbyAmountResourceName(FISH_OIL);

		tableSaltID = findIDbyAmountResourceName(TABLE_SALT);
		rockSaltID = findIDbyAmountResourceName(ROCK_SALT);
		epsomSaltID = findIDbyAmountResourceName(EPSOM_SALT); 

		toiletTissueID = findIDbyAmountResourceName(TOILET_TISSUE);
		napkinID = findIDbyAmountResourceName(NAPKIN);

		// Assemble the rockIDs array
		for (int i=0; i<ROCKS.length; i++) {
			rockIDs[i] = findIDbyAmountResourceName(ROCKS[i]);
		}

		// Assemble the mineralConcIDs array
		for (int i=0; i<MINERAL_CONCENTRATIONS.length; i++) {
			mineralConcIDs[i] = findIDbyAmountResourceName(MINERAL_CONCENTRATIONS[i]);
		}

		// Assemble the oreDepositIDs array
		for (int i=0; i<ORE_DEPOSITS.length; i++) {
			oreDepositIDs[i] = findIDbyAmountResourceName(ORE_DEPOSITS[i]);
		}

		// Assemble the regolith type array
		regolithID = findIDbyAmountResourceName(REGOLITH);
		regolithBID = findIDbyAmountResourceName(REGOLITH_B);
		regolithCID = findIDbyAmountResourceName(REGOLITH_C);
		regolithDID = findIDbyAmountResourceName(REGOLITH_D);
		REGOLITH_TYPES = new int[] {
				regolithID,
				regolithBID,
				regolithCID,
				regolithDID};

		fishMeatID = findIDbyAmountResourceName(FISH_MEAT);

		// AmountResource instances as objects
		foodAR = findAmountResource(FOOD);
		waterAR = findAmountResource(WATER);
		oxygenAR = findAmountResource(OXYGEN);
		carbonDioxideAR = findAmountResource(CO2);
		argonAR = findAmountResource(ARGON);
		nitrogenAR = findAmountResource(NITROGEN);
		coAR = findAmountResource(CO);
		hydrogenAR = findAmountResource(HYDROGEN);
		methaneAR = findAmountResource(METHANE);
		methanolAR = findAmountResource(METHANOL); 
		iceAR = findAmountResource(ICE);
		greyWaterAR = findAmountResource(GREY_WATER);
		blackWaterAR = findAmountResource(BLACK_WATER);

		NaClOAR = findAmountResource(SODIUM_HYPOCHLORITE);

		regolithAR = findAmountResource(REGOLITH);
		regolithBAR = findAmountResource(REGOLITH_B);
		regolithCAR = findAmountResource(REGOLITH_C);
		regolithDAR = findAmountResource(REGOLITH_D);

		rockSamplesAR = findAmountResource(ROCK_SAMPLES);
		sandAR = findAmountResource(SAND);
	}

	/**
	 * Finds an amount resource name by id.
	 *
	 * @param id the resource's id.
	 * @return resource name
	 * @throws ResourceException if resource could not be found.
	 */
	public static String findAmountResourceName(int id) {
		return findAmountResource(id).getName();
	}

	/**
	 * Finds an amount resource by id.
	 *
	 * @param id the resource's id.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static AmountResource findAmountResource(int id) {
		return amountResourceIDMap.get(id);
	}

	/**
	 * Finds an amount resource by name.
	 *
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static AmountResource findAmountResource(String name) {
		if (amountResourceMap == null)
			createMaps();
		return amountResourceMap.get(name.toLowerCase());
	}

	/**
	 * Finds an amount resource by name.
	 *
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static int findIDbyAmountResourceName(String name) {
		AmountResource ar = findAmountResource(name);
		if (ar != null)
			return ar.getID();
		
		logger.severe("The name '" + name + "' does not exist.");
		return -1;
	}

	/**
	 * Gets an immutable set of all the amount resources.
	 *
	 * @return set of amount resources.
	 */
	public static Set<AmountResource> getAmountResources() {
		return resources;
	}

	/**
	 * Gets an immutable set of all the amount resources.
	 *
	 * @return set of amount resources.
	 */
	public static Set<Integer> getIDs() {
		return amountResourceIDMap.keySet();
	}

	/**
	 * A convenience method that calls {@link #getAmountResources()} and turns the
	 * result into an alphabetically ordered list of strings.
	 *
	 * @return {@link List}<{@link String}>
	 */
	public static List<String> getAmountResourceStringSortedList() {
		List<String> resourceNames = new ArrayList<>();
		Iterator<AmountResource> i = resources.iterator();
		while (i.hasNext()) {
			resourceNames.add(i.next().getName());
		}
		Collections.sort(resourceNames);
		return resourceNames;
	}

	public static List<AmountResource> getSortedAmountResources() {
		return sortedResources;
	}
	
	public static boolean isInSitu(int resource) {
		if (resource == iceID || resource == regolithID
				|| resource == regolithBID || resource == regolithCID || resource == regolithDID
				) {
			return true;
		}
		
		return false;
	}
}

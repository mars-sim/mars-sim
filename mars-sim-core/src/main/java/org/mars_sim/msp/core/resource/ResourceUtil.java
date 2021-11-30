/*
 * Mars Simulation Project
 * ResourceUtil.java
 * @date 2021-11-16
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

import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.tool.Conversion;

public class ResourceUtil implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final int FIRST_AMOUNT_RESOURCE_ID = 200;

	public static final int FIRST_ITEM_RESOURCE_ID = 2000;

	public static final int FIRST_VEHICLE_RESOURCE_ID = 3000;

	public static final int FIRST_EQUIPMENT_RESOURCE_ID = 3010;


	public static final String ARGON = "argon";
	public static final String NITROGEN = "nitrogen";
	public static final String CO = "carbon monoxide";

	public static final String HYDROGEN = "hydrogen";
	public static final String METHANE = "methane";
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
	public static final String CEMENT = "mortar";

	public static final String SAND = "sand";

	public static final String METEORITE = "meteorite";

	public static final String ELECTRONIC_WASTE = "electronic waste";
	public static final String CROP_WASTE = "crop waste";
	public static final String FOOD_WASTE = "food waste";
	public static final String SOLID_WASTE = "solid waste";
	public static final String TOXIC_WASTE = "toxic waste";

//	public static final String TOILET_WASTE = "toilet waste";

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

	protected static final String[] ROCKS  = new String[] {
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
	private static Map<Integer, String> arIDNameMap;
	private static Set<AmountResource> resources;
	private static List<AmountResource> sortedResources;

	public static int waterID;
	public static int foodID;

	public static int oxygenID;
	public static int co2ID;
	public static int argonID;
	public static int nitrogenID;
	public static int hydrogenID;
	public static int methaneID;
	public static int coID;

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
	public static AmountResource carbonDioxideAR;
	public static AmountResource argonAR;
	public static AmountResource nitrogenAR;

	public static AmountResource hydrogenAR;
	public static AmountResource methaneAR;

	public static AmountResource coAR;

//	public static AmountResource soilAR;
	public static AmountResource iceAR;
//	public static AmountResource compostAR;

	public static AmountResource regolithAR;
	public static AmountResource regolithBAR;
	public static AmountResource regolithCAR;
	public static AmountResource regolithDAR;

//	public static AmountResource tableSaltAR;
	public static AmountResource NaClOAR;
	public static AmountResource greyWaterAR;
	public static AmountResource blackWaterAR;

//	public static AmountResource eWasteAR;
//	public static AmountResource foodWasteAR;
//	public static AmountResource solidWasteAR;
//	public static AmountResource toxicWasteAR;
//	public static AmountResource cropWasteAR;
//	public static AmountResource toiletTissueAR;

//	public static AmountResource napkinAR;

	public static AmountResource rockSamplesAR;
	public static AmountResource sandAR;

//	public static AmountResource fertilizerAR;

//	public static AmountResource soybeanOilAR;
//	public static AmountResource garlicOilAR;
//	public static AmountResource peanutOilAR;
//	public static AmountResource sesameOilAR;

	private static AmountResource[] ARs = new AmountResource[33];

	// private static int[] ARs_int = new int[33];

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
	 * Default Constructor for ResoureUtil
	 */
	private ResourceUtil() {
		resources = SimulationConfig.instance().getResourceConfiguration().getAmountResources();
		createItemResourceUtil();
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

	public void initializeNewSim() {
		// Create maps
		createMaps();
		// Map the static instances
		mapInstances();
		// Create the Amount Resource array
		createInstancesArray();
	}

	/**
	 * Recreates the Amount Resource instances in all map
	 */
	public void initializeInstances() {
		// Restores the Amount Resource array
		restoreInstancesArray();
		// Map the static instances
		mapInstances();
		// Create maps
		createMaps();
	}

	/**
	 * Restores the Amount Resource addresses
	 */
	public void restoreInstancesArray() {
		for (AmountResource r : resources) {
			for (AmountResource ar : ARs) {
				if (r.getName().equals(ar.getName())) {
					// Restore the AmountResource reference
					r = ar;
					break;
				}
			}
		}
	}

	/**
	 * Creates maps of amount resources
	 */
	public static synchronized void createMaps() {
		if (amountResourceMap == null) {
			sortedResources = new ArrayList<>(resources);
			Collections.sort(sortedResources);

			Map<String, AmountResource> tempAmountResourceMap = new HashMap<>();
			for (AmountResource resource : sortedResources) {
				tempAmountResourceMap.put(resource.getName(), resource);
			}

			Map<Integer, AmountResource> tempAmountResourceIDMap = new HashMap<>();
			for (AmountResource resource : sortedResources) {
				tempAmountResourceIDMap.put(resource.getID(), resource);
			}

			Map<Integer, String> tempArIDNameMap = new HashMap<>();
			for (AmountResource resource : sortedResources) {
				tempArIDNameMap.put(resource.getID(), resource.getName());
			}

			// Create immuatable internals
			amountResourceMap = Collections.unmodifiableMap(tempAmountResourceMap);
			amountResourceIDMap = Collections.unmodifiableMap(tempAmountResourceIDMap);
			arIDNameMap = Collections.unmodifiableMap(tempArIDNameMap);
		}
	}

	public static void mapInstances() {

		// AmountResource instances as Integer

		foodID = findIDbyAmountResourceName(LifeSupportInterface.FOOD); // 1
		waterID = findIDbyAmountResourceName(LifeSupportInterface.WATER); // 2

		oxygenID = findIDbyAmountResourceName(LifeSupportInterface.OXYGEN); // 3
		co2ID = findIDbyAmountResourceName(LifeSupportInterface.CO2); // 4
		argonID = findIDbyAmountResourceName(ARGON); // 5
		coID = findIDbyAmountResourceName(CO); // 6

		hydrogenID = findIDbyAmountResourceName(HYDROGEN); // 8
		methaneID = findIDbyAmountResourceName(METHANE); // 9
		nitrogenID = findIDbyAmountResourceName(NITROGEN); // 10

		iceID = findIDbyAmountResourceName(ICE); // 13

		blackWaterID = findIDbyAmountResourceName(BLACK_WATER); //
		greyWaterID = findIDbyAmountResourceName(GREY_WATER); // 20

		cropWasteID = findIDbyAmountResourceName(CROP_WASTE); // 15
		foodWasteID = findIDbyAmountResourceName(FOOD_WASTE);
		toxicWasteID = findIDbyAmountResourceName(TOXIC_WASTE);
		solidWasteID = findIDbyAmountResourceName(SOLID_WASTE);
		eWasteID = findIDbyAmountResourceName(ELECTRONIC_WASTE); // 16
		compostID = findIDbyAmountResourceName(COMPOST); //

		fertilizerID = findIDbyAmountResourceName(FERTILIZER); // 139

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

		soybeanOilID = findIDbyAmountResourceName(SOYBEAN_OIL); // 27
		garlicOilID = findIDbyAmountResourceName(GARLIC_OIL); // 41
		sesameOilID = findIDbyAmountResourceName(SESAME_OIL); // 53
		peanutOilID = findIDbyAmountResourceName(PEANUT_OIL); // 46
		riceBranOilID = findIDbyAmountResourceName(RICE_BRAN_OIL); //
		fishOilID = findIDbyAmountResourceName(FISH_OIL); //

		tableSaltID = findIDbyAmountResourceName(TABLE_SALT); // 23
		rockSaltID = findIDbyAmountResourceName(ROCK_SALT); //
		epsomSaltID = findIDbyAmountResourceName(EPSOM_SALT); //

		toiletTissueID = findIDbyAmountResourceName(TOILET_TISSUE);
		napkinID = findIDbyAmountResourceName(NAPKIN); //

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
		regolithID = findIDbyAmountResourceName(REGOLITH); // 156
		regolithBID = findIDbyAmountResourceName(REGOLITH_B); //
		regolithCID = findIDbyAmountResourceName(REGOLITH_C); //
		regolithDID = findIDbyAmountResourceName(REGOLITH_D); //
		REGOLITH_TYPES = new int[] {
				regolithID,
				regolithBID,
				regolithCID,
				regolithDID};

		fishMeatID = findIDbyAmountResourceName(FISH_MEAT); //

		// AmountResource instances as objects

		foodAR = findAmountResource(LifeSupportInterface.FOOD); // 1
		waterAR = findAmountResource(LifeSupportInterface.WATER); // 2
		oxygenAR = findAmountResource(LifeSupportInterface.OXYGEN); // 3
		carbonDioxideAR = findAmountResource(LifeSupportInterface.CO2); // 4
		argonAR = findAmountResource(ARGON); // 5
		nitrogenAR = findAmountResource(NITROGEN); // 10
		coAR = findAmountResource(CO); // 6
		hydrogenAR = findAmountResource(HYDROGEN); // 8
		methaneAR = findAmountResource(METHANE); // 9
//		soilAR = findAmountResource(SOIL); // 12
		iceAR = findAmountResource(ICE); // 13
//		compostAR = findAmountResource(COMPOST); // 14
//		cropWasteAR = findAmountResource(CROP_WASTE); // 15
//		eWasteAR = findAmountResource(ELECTRONIC_WASTE); // 16
//		foodWasteAR = findAmountResource(FOOD_WASTE); // 17
//		solidWasteAR = findAmountResource(SOLID_WASTE); // 18
//		toxicWasteAR = findAmountResource(TOXIC_WASTE); // 19
		greyWaterAR = findAmountResource(GREY_WATER); // 20
		blackWaterAR = findAmountResource(BLACK_WATER); // 21
//		tableSaltAR = findAmountResource(TABLE_SALT); // 23
//		fertilizerAR = findAmountResource(FERTILIZER); // 139

		NaClOAR = findAmountResource(SODIUM_HYPOCHLORITE); // 146

		regolithAR = findAmountResource(REGOLITH); // 156
		regolithBAR = findAmountResource(REGOLITH_B);
		regolithCAR = findAmountResource(REGOLITH_C);
		regolithDAR = findAmountResource(REGOLITH_D);

		rockSamplesAR = findAmountResource(ROCK_SAMPLES); //
		sandAR = findAmountResource(SAND); // 159
	}

	public static void createInstancesArray() {

		ARs = new AmountResource[] {
				foodAR, waterAR, oxygenAR, carbonDioxideAR,
				argonAR, nitrogenAR, coAR, hydrogenAR,
				methaneAR,
				iceAR,
				greyWaterAR, blackWaterAR,
				regolithAR, regolithBAR, regolithCAR, regolithDAR,
				rockSamplesAR, sandAR,
				NaClOAR,
				};
	}

	/**
	 * Finds an amount resource name by id.
	 *
	 * @param id the resource's id.
	 * @return resource name
	 * @throws ResourceException if resource could not be found.
	 */
	public static String findAmountResourceName(int id) {
		return arIDNameMap.get(id);
	}

	/**
	 * Prints the capitalized name of the resource
	 * @param id the resource's id.
	 * @return capitalized name
	 */
	public static String printCapName(int id) {
		return Conversion.capitalize(findAmountResourceName(id));
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

	public static Map<Integer, AmountResource> getAmountResourceIDMap() {
		return amountResourceIDMap;
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
		return getKeyByValue(arIDNameMap, name.toLowerCase());
	}

	/**
	 * Returns the first matched key from a given value in a map for one-to-one
	 * relationship
	 *
	 * @param map
	 * @param value
	 * @return key
	 */
	private static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
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
	 * convenience method that calls {@link #getAmountResources()} and turns the
	 * result into an alphabetically ordered list of strings.
	 *
	 * @return {@link List}<{@link String}>
	 */
	public static List<String> getAmountResourceStringSortedList() {
		List<String> resourceNames = new ArrayList<>();
		Iterator<AmountResource> i = resources.iterator();
		while (i.hasNext()) {
			resourceNames.add(i.next().getName().toLowerCase());
		}
		Collections.sort(resourceNames);
		return resourceNames;
	}

	public static List<AmountResource> getSortedAmountResources() {
		return sortedResources;
	}

	public static boolean isLifeSupport(int id) {
		return findAmountResource(id).isLifeSupport();
	}

	public void destroy() {
		resources = null;
		amountResourceMap = null;
		amountResourceIDMap = null;
		arIDNameMap = null;
	}

}

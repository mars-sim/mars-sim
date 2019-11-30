/**
 * Mars Simulation Project
 * ResourceUtil.java
 * @version 3.1.0 2017-04-07
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

import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.SimulationConfig;

public class ResourceUtil implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final int FIRST_AMOUNT_RESOURCE_ID = 200;
	
	public static final int FIRST_ITEM_RESOURCE_ID = 1000;

	public static final int FIRST_VEHICLE_RESOURCE_ID = 2000; 

	public static final int FIRST_EQUIPMENT_RESOURCE_ID = 4000;
	
	public static String[] MINERALS = new String[] {
	        "Chalcopyrite",
			"Goethite",
			"Hematite",
			"Kamacite",
			"Magnesite",
			"Magnetite",
			"Malachite",
			"Olivine",
			"Taenite",
			"Sylvite"};
	
	public static String[] ORE_DEPOSITS = new String[] {
			"Allophane",
			"Akaganeite",
			"Basaltic",
			"Bassanite",
			"Gypsum",
			"Smectite"};
	
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
	public static final String ROCK_SAMLES = "rock samples";
	public static final String SAND = "sand";

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

	
	// Data members.
	// private Set<AmountResource> resources;// = new TreeSet<AmountResource>();
	private static Map<String, AmountResource> amountResourceMap;
	private static Map<Integer, AmountResource> amountResourceIDMap;
	private static Map<Integer, String> arIDNameMap;

	private static Set<AmountResource> resources;

	private static List<AmountResource> sortedResources;

	// NOTE: This instance is critical during deserialization.
	// When loading the saved sim, amountResourceConfig from the saved sim will be
	// copied over here
	// This way, the newly created amountResourceConfig will be overridden
	// and the resources from the original version of amountResourceConfig will be
	// preserved
	// The drawback is that this won't work if one loads from a saved sim that has
	// an outdated list of Amount Resources
	private static AmountResourceConfig amountResourceConfig;

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
	
	public static int[] mineralIDs = new int[MINERALS.length];
	
	public static int[] oreDepositIDs = new int[ORE_DEPOSITS.length];
	
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
		amountResourceConfig = SimulationConfig.instance().getResourceConfiguration();
		createResourceSet();
		createItemResourceUtil();
	}

	/**
	 * Creates an amount resource instance
	 * 
	 * @param id
	 * @param name
	 * @param type
	 * @param description
	 * @param phase
	 * @param lifeSupport
	 * @param edible
	 * @return {@link AmountResource}
	 */
	public static AmountResource createAmountResource(int id, String name, String type, String description, PhaseType phase,
			boolean lifeSupport, boolean edible

	) {
		AmountResource ar = new AmountResource(id, name, type, description, phase, lifeSupport, edible);
		ResourceUtil.registerBrandNewAR(ar);
		return ar;
	}

	/**
	 * Creates an amount resource set
	 */
	public void createResourceSet() {
		resources = amountResourceConfig.getAmountResources();
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
			break;
		}
	}

	
//	public void restoreInventory() {
//		Collection<Unit> units = Simulation.instance().getUnitManager().getUnits();
//		for (Unit u : units) {
//			// if (!u.getName().contains("Large Bag") &&
//			if (u.getInventory() != null && !u.getInventory().isEmpty(false)) {
//				u.getInventory().restoreARs(ARs);
//			}
//		}
//	}

	/**
	 * Creates maps of amount resources
	 */
	public static void createMaps() {
		amountResourceMap = new HashMap<String, AmountResource>();
		sortedResources = new ArrayList<>(resources);
		Collections.sort(sortedResources);

		for (AmountResource resource : sortedResources) {
			amountResourceMap.put(resource.getName(), resource);
		}

		amountResourceIDMap = new HashMap<Integer, AmountResource>();
		for (AmountResource resource : sortedResources) {
			amountResourceIDMap.put(resource.getID(), resource);
		}

		arIDNameMap = new HashMap<Integer, String>();
		for (AmountResource resource : sortedResources) {
			arIDNameMap.put(resource.getID(), resource.getName());
		}
	}

	/**
	 * Register the brand new amount resource in all 3 resource maps
	 * 
	 * @param ar {@link ParAmountResourcet}
	 */
	public static void registerBrandNewAR(AmountResource ar) {
		amountResourceMap.put(ar.getName(), ar);
		amountResourceIDMap.put(ar.getID(), ar);
		arIDNameMap.put(ar.getID(), ar.getName());
	}

	public static void mapInstances() {

		// AmountResource instances as Integer
		
		foodID = findAmountResource(LifeSupportInterface.FOOD).getID(); // 1
		waterID = findAmountResource(LifeSupportInterface.WATER).getID(); // 2

		oxygenID = findAmountResource(LifeSupportInterface.OXYGEN).getID(); // 3
		co2ID = findAmountResource(LifeSupportInterface.CO2).getID(); // 4
		argonID = findAmountResource(ARGON).getID(); // 5
		coID = findAmountResource(CO).getID(); // 6

		hydrogenID = findAmountResource(HYDROGEN).getID(); // 8
		methaneID = findAmountResource(METHANE).getID(); // 9
		nitrogenID = findAmountResource(NITROGEN).getID(); // 10

		iceID = findAmountResource(ICE).getID(); // 13

		rockSamplesID = findAmountResource(ROCK_SAMLES).getID(); //
		blackWaterID = findAmountResource(BLACK_WATER).getID(); //

		greyWaterID = findAmountResource(GREY_WATER).getID(); // 20
		cropWasteID = findAmountResource(CROP_WASTE).getID(); // 15
		foodWasteID = findAmountResource(FOOD_WASTE).getID();
		toxicWasteID = findAmountResource(TOXIC_WASTE).getID();
		solidWasteID = findAmountResource(SOLID_WASTE).getID();
		eWasteID = findAmountResource(ELECTRONIC_WASTE).getID(); // 16
		compostID = findAmountResource(COMPOST).getID(); // 
		
		fertilizerID = findAmountResource(FERTILIZER).getID(); // 139

		leavesID = findAmountResource(LEAVES).getID();
		
		regolithID = findAmountResource(REGOLITH).getID(); // 156
		regolithBID = findAmountResource(REGOLITH_B).getID(); // 
		regolithCID = findAmountResource(REGOLITH_C).getID(); // 
		regolithDID = findAmountResource(REGOLITH_D).getID(); // 
		
		soilID = findAmountResource(REGOLITH).getID();  // 12
		sandID = findAmountResource(SAND).getID();

		soymilkID = findAmountResource(SOYMILK).getID();
	
		NaClOID = findAmountResource(SODIUM_HYPOCHLORITE).getID();
		
		soybeanOilID = findAmountResource(SOYBEAN_OIL).getID(); // 27
		garlicOilID = findAmountResource(GARLIC_OIL).getID(); // 41
		sesameOilID = findAmountResource(SESAME_OIL).getID(); // 53
		peanutOilID = findAmountResource(PEANUT_OIL).getID(); // 46
		riceBranOilID = findAmountResource(RICE_BRAN_OIL).getID(); // 
		fishOilID = findAmountResource(FISH_OIL).getID(); // 
		
		tableSaltID = findAmountResource(TABLE_SALT).getID(); // 23
		rockSaltID = findAmountResource(ROCK_SALT).getID(); // 
		epsomSaltID = findAmountResource(EPSOM_SALT).getID(); // 
				
		toiletTissueID = findAmountResource(TOILET_TISSUE).getID();
		napkinID = findAmountResource(NAPKIN).getID(); // 
		
		// Gets the mineralIDs
		for (int i=0; i<MINERALS.length; i++) {
			mineralIDs[i] = findIDbyAmountResourceName(MINERALS[i]);
		}
		
		// Gets the oreDepositIDs
		for (int i=0; i<ORE_DEPOSITS.length; i++) {
			oreDepositIDs[i] = findIDbyAmountResourceName(ORE_DEPOSITS[i]);
		}
		
		fishMeatID = findAmountResource(FISH_MEAT).getID(); // 

		
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
		
		regolithAR = findAmountResource(REGOLITH); // 156
		regolithBAR = findAmountResource(REGOLITH_B);
		regolithCAR = findAmountResource(REGOLITH_C);
		regolithDAR = findAmountResource(REGOLITH_D);
		
		rockSamplesAR = findAmountResource(ROCK_SAMLES); //
		sandAR = findAmountResource(SAND); // 159
		NaClOAR = findAmountResource(SODIUM_HYPOCHLORITE); // 146
		
//		napkinAR = findAmountResource(NAPKIN); // 161
//		toiletTissueAR = findAmountResource(TOILET_TISSUE); // 164
//		soybeanOilAR = findAmountResource(SOYBEAN_OIL); // 27
//		garlicOilAR = findAmountResource(GARLIC_OIL); // 41
//		sesameOilAR = findAmountResource(SESAME_OIL); // 53
//		peanutOilAR = findAmountResource(PEANUT_OIL); // 46
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
//        for (int i=0; i< 33; i++) {
//        	//for (AmountResource ar : ARs) {
//        	int n = findIDbyAmountResourceName(ARs[i].getName());
//        	ARs_int[i] = n;
//        }
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

//  An example method
//	private Set<T> intersection(Collection<T> first, Collection<T> second) {
//		// intersection with an empty collection is empty
//		if (isNullOrEmpty(first) || isNullOrEmpty(second))
//			return new HashSet<>();
//
//		return first.stream()
//				.filter(second::contains)
//				.collect(Collectors.toSet());
//	}

	/**
	 * gets a sorted map of all amount resource names by calling
	 * {@link AmountResourceConfig#getAmountResourcesMap()}.
	 * 
	 * @return {@link Map}<{@link Integer}, {@link String}>
	 */
	public static Map<Integer, String> getIDNameMap() {
		return arIDNameMap;
	}

	/**
	 * gets a sorted map of all amount resources by calling
	 * {@link AmountResourceConfig#getAmountResourcesIDMap()}.
	 * 
	 * @return {@link Map}<{@link Integer},{@link AmountResource}>
	 */
	public static Map<Integer, AmountResource> getAmountResourcesIDMap() {
		return amountResourceIDMap;
	}

	/**
	 * gets a sorted map of all amount resources by calling
	 * {@link AmountResourceConfig#getAmountResourcesMap()}.
	 * 
	 * @return {@link Map}<{@link String},{@link AmountResource}>
	 */
	public static Map<String, AmountResource> getAmountResourcesMap() {
		return amountResourceMap;
	}

	/**
	 * convenience method that calls {@link #getAmountResources()} and turns the
	 * result into an alphabetically ordered list of strings.
	 * 
	 * @return {@link List}<{@link String}>
	 */
	public static List<String> getAmountResourceStringSortedList() {
		List<String> resourceNames = new ArrayList<String>();
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
	
//	/**
//	 * Gets the hash code value.
//	 */
	// public int hashCode() {
	// return hashcode;
	// }

//    /**
//     * Prevents the singleton pattern from being destroyed
//     * at the time of serialization
//     * @return SimulationConfig instance
//
//    protected Object readResolve() throws ObjectStreamException {
//		System.out.println("amountResourceConfig :\t" + amountResourceConfig);
//    	return INSTANCE;
//    }

//    private void writeObject(java.io.ObjectOutputStream out)
//    	     throws IOException {
//
//    }
//
//    private void readObject(java.io.ObjectInputStream in)
//    	     throws IOException, ClassNotFoundException {
//
//    }
//
//    private void readObjectNoData()
//    	     throws ObjectStreamException {
//
//    }

	public void destroy() {
		resources = null;
		amountResourceMap = null;
		amountResourceIDMap = null;
		arIDNameMap = null;
	}

}

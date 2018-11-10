/**
 * Mars Simulation Project
 * ResourceUtil.java
 * @version 3.1.0 2017-04-07
 * @author Manny Kung
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;

public class ResourceUtil implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final int FIRST_ITEM_RESOURCE = 1000;

	public static final int FIRST_EQUIPMENT_RESOURCE = 1000;

	public static final String ARGON = "argon";
	public static final String NITROGEN = "nitrogen";
	public static final String CO = "carbon monoxide";

	public static final String HYDROGEN = "hydrogen";
	public static final String METHANE = "methane";
	public static final String SOIL = "soil";
	public static final String ICE = "ice";
	public static final String COMPOST = "compost";

	public static final String REGOLITH = "regolith";
	public static final String ROCK_SAMLES = "rock samples";
	public static final String SAND = "sand";

	public static final String ELECTRONIC_WASTE = "electronic waste";
	public static final String CROP_WASTE = "crop waste";
	public static final String FOOD_WASTE = "food waste";
	public static final String SOLID_WASTE = "solid waste";
	public static final String TOXIC_WASTE = "toxic waste";

	public static final String GREY_WATER = "grey water";
	public static final String BLACK_WATER = "black water";
	public static final String TABLE_SALT = "table salt";
	public static final String SODIUM_HYPOCHLORITE = "sodium hypochlorite";
	public static final String NAPKIN = "napkin";

	public static final String FERTILIZER = "fertilizer";

	public static final String SOYBEAN_OIL = "soybean oil";
	public static final String GARLIC_OIL = "garlic oil";
	public static final String SESAME_OIL = "sesame oil";
	public static final String PEANUT_OIL = "peanut oil";

	public static final String TOILET_TISSUE = "toilet tissue";
	
	public static final String SOYMILK = "soymilk";

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
	public static int sandID;
	public static int soymilkID;

	public static int rockSamplesID;

	public static int blackWaterID;
	public static int greyWaterID;
	public static int cropWasteID;
	public static int fertilizerID;

	public static AmountResource foodAR;
	public static AmountResource oxygenAR;
	public static AmountResource waterAR;
	public static AmountResource carbonDioxideAR;
	public static AmountResource argonAR;
	public static AmountResource nitrogenAR;

	public static AmountResource hydrogenAR;
	public static AmountResource methaneAR;

	public static AmountResource coAR;

	public static AmountResource soilAR;
	public static AmountResource iceAR;
	public static AmountResource compostAR;

	public static AmountResource regolithAR;

	public static AmountResource tableSaltAR;
	public static AmountResource NaClOAR;
	public static AmountResource greyWaterAR;
	public static AmountResource blackWaterAR;

	public static AmountResource eWasteAR;
	public static AmountResource foodWasteAR;
	public static AmountResource solidWasteAR;
	public static AmountResource toxicWasteAR;

	public static AmountResource napkinAR;

	public static AmountResource rockSamplesAR;
	public static AmountResource sandAR;

	public static AmountResource fertilizerAR;

	public static AmountResource cropWasteAR;
	public static AmountResource toiletTissueAR;

	public static AmountResource soybeanOilAR;
	public static AmountResource garlicOilAR;
	public static AmountResource peanutOilAR;
	public static AmountResource sesameOilAR;

	private AmountResource[] ARs = new AmountResource[33];

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
		createResourceSet();
		createItemResourceUtil();
	}

	public void createResourceSet() {
		amountResourceConfig = SimulationConfig.instance().getResourceConfiguration();
		resources = amountResourceConfig.getAmountResources();
	}

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
		createMaps();
		mapARs();
		// make a copy of ARs references
		savedARs();
	}

	public void prep4ResourcesSavedSim() {
		// Restores the saved ARs references
		restoreSavedARs();
		mapARs();
		createMaps();
		// restoreInventory();
	}

	public void restoreSavedARs() {
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

	public void restoreInventory() {
		Collection<Unit> units = Simulation.instance().getUnitManager().getUnits();
		for (Unit u : units) {
			// if (!u.getName().contains("Large Bag") &&
			if (u.getInventory() != null && !u.getInventory().isEmpty(false)) {
				u.getInventory().restoreARs(ARs);
			}
		}
	}

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

	public void mapARs() {

		// in Integer
		
		foodID = findAmountResource(LifeSupportType.FOOD).getID(); // 1
		waterID = findAmountResource(LifeSupportType.WATER).getID(); // 2

		oxygenID = findAmountResource(LifeSupportType.OXYGEN).getID(); // 3
		co2ID = findAmountResource(LifeSupportType.CO2).getID(); // 4
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
		fertilizerID = findAmountResource(FERTILIZER).getID(); // 139

		regolithID = findAmountResource(REGOLITH).getID(); // 156
		sandID = findAmountResource(SAND).getID();

		soymilkID = findAmountResource(SOYMILK).getID();

		
		// in AmountResource
		
		
		foodAR = findAmountResource(LifeSupportType.FOOD); // 1
		waterAR = findAmountResource(LifeSupportType.WATER); // 2
		oxygenAR = findAmountResource(LifeSupportType.OXYGEN); // 3
		carbonDioxideAR = findAmountResource(LifeSupportType.CO2); // 4

		argonAR = findAmountResource(ARGON); // 5
		nitrogenAR = findAmountResource(NITROGEN); // 10

		coAR = findAmountResource(CO); // 6

		hydrogenAR = findAmountResource(HYDROGEN); // 8
		methaneAR = findAmountResource(METHANE); // 9
		soilAR = findAmountResource(SOIL); // 12
		iceAR = findAmountResource(ICE); // 13
		compostAR = findAmountResource(COMPOST); // 14

		cropWasteAR = findAmountResource(CROP_WASTE); // 15
		eWasteAR = findAmountResource(ELECTRONIC_WASTE); // 16
		foodWasteAR = findAmountResource(FOOD_WASTE); // 17
		solidWasteAR = findAmountResource(SOLID_WASTE); // 18
		toxicWasteAR = findAmountResource(TOXIC_WASTE); // 19

		greyWaterAR = findAmountResource(GREY_WATER); // 20
		blackWaterAR = findAmountResource(BLACK_WATER); // 21

		tableSaltAR = findAmountResource(TABLE_SALT); // 23

		fertilizerAR = findAmountResource(FERTILIZER); // 139

		regolithAR = findAmountResource(REGOLITH); // 156

		rockSamplesAR = findAmountResource(ROCK_SAMLES); // 157
		sandAR = findAmountResource(SAND); // 159

		NaClOAR = findAmountResource(SODIUM_HYPOCHLORITE); // 146
		napkinAR = findAmountResource(NAPKIN); // 161
		toiletTissueAR = findAmountResource(TOILET_TISSUE); // 164

		soybeanOilAR = findAmountResource(SOYBEAN_OIL); // 27
		garlicOilAR = findAmountResource(GARLIC_OIL); // 41
		sesameOilAR = findAmountResource(SESAME_OIL); // 53
		peanutOilAR = findAmountResource(PEANUT_OIL); // 46

	}

	public void savedARs() {

		ARs = new AmountResource[] { 
				foodAR, waterAR, oxygenAR, carbonDioxideAR, 
				argonAR, nitrogenAR, coAR, hydrogenAR, methaneAR, iceAR,
				cropWasteAR, foodWasteAR, solidWasteAR, eWasteAR, foodWasteAR,
				solidWasteAR, toxicWasteAR, compostAR, greyWaterAR, blackWaterAR,
				soilAR, regolithAR, rockSamplesAR, sandAR,
				tableSaltAR, NaClOAR, napkinAR,
				toiletTissueAR, fertilizerAR,
				soybeanOilAR, garlicOilAR, peanutOilAR, sesameOilAR };
//        for (int i=0; i< 33; i++) {
//        	//System.out.println(ARs[i].getName());
//        	//for (AmountResource ar : ARs) {
//        	int n = findIDbyAmountResourceName(ARs[i].getName());
//        	ARs_int[i] = n;
//        	System.out.println(ARs[i] + " : " + ARs_int[i]);
//        }
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
	public Set<AmountResource> getAmountResources() {
		return resources;
	}

	/**
	 * Gets an immutable set of all the amount resources.
	 * 
	 * @return set of amount resources.
	 */
	public Set<Integer> getARIDs() {
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

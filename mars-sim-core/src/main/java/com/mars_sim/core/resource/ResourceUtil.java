/*
 * Mars Simulation Project
 * ResourceUtil.java
 * @date 2023-09-19
 * @author Manny Kung
 */

package com.mars_sim.core.resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.food.Food;
import com.mars_sim.core.food.FoodUtil;
import com.mars_sim.core.logging.SimLogger;

public class ResourceUtil {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ResourceUtil.class.getName());
	
	public static final int FIRST_AMOUNT_RESOURCE_ID = 200;
	public static final int FIRST_ITEM_RESOURCE_ID = 500;
	public static final int FIRST_VEHICLE_RESOURCE_ID = 1000;
	public static final int FIRST_EQUIPMENT_RESOURCE_ID = 1010;
	public static final int FIRST_ROBOT_RESOURCE_ID = 1020;
	public static final int FIRST_BIN_RESOURCE_ID = 1040;

	protected static Set<Integer> essentialResources;

	// Data members.
	private static Map<String, AmountResource> amountResourceByName;
	private static Map<Integer, AmountResource> amountResourceById;
	private static Set<AmountResource> resources;
	
	/** A set of life support resources. */
	private static Set<Integer> lifeSupportResources;

	public static final int waterID = FIRST_AMOUNT_RESOURCE_ID;
	public static final int foodID = waterID + 1;

	public static final int oxygenID = foodID + 1;
	public static final int co2ID = oxygenID + 1;
	public static final int argonID = co2ID + 1;
	public static final int nitrogenID = argonID + 1;
	public static final int hydrogenID = nitrogenID + 1;
	public static final int methaneID = hydrogenID + 1;
	public static final int methanolID = methaneID + 1;
	public static final int coID = methanolID + 1;
	public static final int acetyleneID = coID + 1;
	
	public static final int chlorineID = acetyleneID + 1;
			
	public static final int iceID = chlorineID + 1;

	public static final int regolithID = iceID + 1;
	public static final int regolithBID = regolithID + 1;
	public static final int regolithCID = regolithBID + 1;
	public static final int regolithDID = regolithCID + 1;
	public static final int olivineID = regolithDID + 1;
	public static final int kamaciteID = olivineID + 1;
	public static final int gypsumID = kamaciteID + 1; 
	public static final int malachiteID = gypsumID + 1;
	public static final int sylviteID = malachiteID + 1;

	public static final int soilID = sylviteID + 1;
	public static final int sandID = soilID + 1;

	public static final int rockSamplesID = sandID + 1;

	public static final int blackWaterID = rockSamplesID + 1;
	public static final int greyWaterID = blackWaterID + 1;
	
	public static final int brineWaterID = greyWaterID + 1;

	public static final int concreteID = brineWaterID + 1;
	public static final int cementID = concreteID + 1;
	public static final int limeID = cementID + 1;

	public static final int compostID = limeID + 1;
	public static final int cropWasteID = compostID + 1;
	public static final int foodWasteID = cropWasteID + 1;
	public static final int toxicWasteID = foodWasteID + 1;
	public static final int solidWasteID = toxicWasteID + 1;

	public static final int toiletTissueID = solidWasteID + 1;

	public static final int fertilizerID = toiletTissueID + 1;
	public static final int NaClOID = fertilizerID + 1; 

	public static final int leavesID = NaClOID + 1;

	public static final int soybeanOilID = leavesID + 1;
	public static final int garlicOilID = soybeanOilID + 1;
	public static final int sesameOilID = garlicOilID + 1;
	public static final int peanutOilID = sesameOilID + 1;
	public static final int riceBranOilID = peanutOilID + 1;
	public static final int fishOilID = riceBranOilID + 1;
	public static final int oliveOilID = fishOilID + 1;

	public static final int epsomSaltID = oliveOilID + 1;
	public static final int tableSaltID = epsomSaltID + 1;
	public static final int rockSaltID = tableSaltID + 1;

	public static final int eWasteID = rockSaltID + 1;

	public static final int napkinID = eWasteID + 1;

	public static final int meteoriteID = napkinID + 1;
	public static final int fishMeatID = meteoriteID + 1;
	public static final int spirulinaID = fishMeatID + 1;

	private static final int smectiteID = spirulinaID + 1;
	private static final int basalticID = smectiteID + 1;
	private static final int bassaniteID = basalticID + 1;
	private static final int allophaneID = bassaniteID + 1;
	private static final int akaganeiteID = allophaneID + 1;
	private static final int chalcopyriteID = akaganeiteID + 1;
	private static final int goethiteID = chalcopyriteID + 1;
	private static final int hematiteID = goethiteID + 1;
	private static final int magnesiteID = hematiteID + 1;
	private static final int magnetiteID = magnesiteID + 1;
	private static final int taeniteID = magnetiteID + 1;
	private static final int columnarbasaltID = taeniteID + 1;
	private static final int graniteID = columnarbasaltID + 1;
	private static final int shaleID = graniteID + 1;
	private static final int mudstoneID = shaleID + 1;
	private static final int glomerateID = mudstoneID + 1;
	private static final int conglomerateID = glomerateID + 1;
	private static final int sandstoneID = conglomerateID + 1;
	private static final int crossbeddingID = sandstoneID + 1;
	private static final int scoriaID = crossbeddingID;
	public static final int FIRST_AMOUNT_FREE_RESOURCE_ID = scoriaID + 1;

	public static final int[] rockIDs = new int[] {rockSamplesID, columnarbasaltID,
													graniteID, mudstoneID, sandstoneID,
													shaleID, glomerateID, crossbeddingID,
													meteoriteID, scoriaID};
	public static final int[] mineralConcIDs = new int[]{chalcopyriteID, goethiteID,
														hematiteID, kamaciteID, magnesiteID, magnetiteID,
														malachiteID, olivineID, taeniteID, sylviteID};
	public static final int[] oreDepositIDs = new int[]{allophaneID, akaganeiteID, basalticID,
														bassaniteID, gypsumID, smectiteID};
	public static final int[] REGOLITH_TYPES = new int[] {regolithID, regolithBID,
														regolithCID, regolithDID};


	private static final Map<String, Integer> fixedResources = new HashMap<>();

	static {
		// Map the pre-defined resources to their names
		fixedResources.put("acetylene", acetyleneID);
		fixedResources.put("akaganeite", akaganeiteID);
		fixedResources.put("allophane", allophaneID);
		fixedResources.put("argon", argonID);
		fixedResources.put("basaltic", basalticID);
		fixedResources.put("bassanite", bassaniteID);
		fixedResources.put("black water", blackWaterID);
		fixedResources.put("brine water", brineWaterID);
		fixedResources.put("carbon dioxide", co2ID);
		fixedResources.put("carbon monoxide", coID);
		fixedResources.put("cement", cementID);
		fixedResources.put("chalcopyrite", chalcopyriteID);
		fixedResources.put("chlorine", chlorineID);
		fixedResources.put("columnar basalt", columnarbasaltID);
		fixedResources.put("compost", compostID);
		fixedResources.put("concrete", concreteID);
		fixedResources.put("conglomerate", conglomerateID);
		fixedResources.put("crop waste", cropWasteID);
		fixedResources.put("cross bedding", crossbeddingID);
		fixedResources.put("electronic waste", eWasteID);
		fixedResources.put("epsom salt", epsomSaltID);
		fixedResources.put("fertilizer", fertilizerID);
		fixedResources.put("fish meat", fishMeatID);
		fixedResources.put("fish oil", fishOilID);
		fixedResources.put("food", foodID);
		fixedResources.put("food waste", foodWasteID);
		fixedResources.put("garlic oil", garlicOilID);
		fixedResources.put("goethite", goethiteID);
		fixedResources.put("granite", graniteID);
		fixedResources.put("grey water", greyWaterID);
		fixedResources.put("gypsum", gypsumID);
		fixedResources.put("hematite", hematiteID);
		fixedResources.put("hydrogen", hydrogenID);
		fixedResources.put("ice", iceID);
		fixedResources.put("kamacite", kamaciteID);
		fixedResources.put("leaves", leavesID);
		fixedResources.put("lime", limeID);
		fixedResources.put("magnesite", magnesiteID);
		fixedResources.put("magnetite", magnetiteID);
		fixedResources.put("malachite", malachiteID);
		fixedResources.put("methane", methaneID);
		fixedResources.put("methanol", methanolID);
		fixedResources.put("meteorite", meteoriteID);
		fixedResources.put("mudstone", mudstoneID);
		fixedResources.put("napkin", napkinID);
		fixedResources.put("nitrogen", nitrogenID);
		fixedResources.put("olive oil", oliveOilID);
		fixedResources.put("olivine", olivineID);
		fixedResources.put("oxygen", oxygenID);
		fixedResources.put("peanut oil", peanutOilID);
		fixedResources.put("regolith", regolithID);
		fixedResources.put("regolith-b", regolithBID);
		fixedResources.put("regolith-c", regolithCID);
		fixedResources.put("regolith-d", regolithDID);
		fixedResources.put("rice bran oil", riceBranOilID);
		fixedResources.put("rock salt", rockSaltID);
		fixedResources.put("rock samples", rockSamplesID);
		fixedResources.put("sand", sandID);
		fixedResources.put("sandstone", sandstoneID);
		fixedResources.put("scoria", scoriaID);
		fixedResources.put("sesame oil", sesameOilID);
		fixedResources.put("shale", shaleID);
		fixedResources.put("smectite", smectiteID);
		fixedResources.put("sodium hypochlorite", NaClOID);
		fixedResources.put("soil", soilID);
		fixedResources.put("solid waste", solidWasteID);
		fixedResources.put("soybean oil", soybeanOilID);
		fixedResources.put("spirulina", spirulinaID);
		fixedResources.put("sylvite", sylviteID);
		fixedResources.put("table salt", tableSaltID);
		fixedResources.put("taenite", taeniteID);
		fixedResources.put("toilet tissue", toiletTissueID);
		fixedResources.put("toxic waste", toxicWasteID);
		fixedResources.put("water", waterID);

		// This check will only fail if a new resource has not been added correctly
		int expectedSize = FIRST_AMOUNT_FREE_RESOURCE_ID - FIRST_AMOUNT_RESOURCE_ID;
		if (fixedResources.size() != expectedSize) {
			throw new IllegalStateException("The number of fixed resources is not correct. Expected: " + expectedSize + ", Actual: " + fixedResources.size());
		}
	};

	/**
	 * Default Constructor for ResoureUtil.
	 */
	private ResourceUtil() {
	}

	/**
	 * Register the known Amunt Resources in the helper
	 * @param defined
	 */
	public static void registerResources(Set<AmountResource> defined) {
		resources = defined;
		createMaps();

		// Double check all predefined resources are in the list
		// Cause could be a missing resource in the XMLfile
		var missingFixed = new HashSet<>(fixedResources.values());
		missingFixed.removeAll(amountResourceById.keySet());
		if (!missingFixed.isEmpty()) {
			// Display the missing resources
			var missingFixedNames = fixedResources.entrySet().stream()
					.filter(entry -> missingFixed.contains(entry.getValue()))
					.map(Map.Entry::getKey)
					.collect(Collectors.joining(", "));
			throw new IllegalStateException("The following fixed resources are missing: " + missingFixedNames);
		}

		lifeSupportResources = resources.stream()
				.filter(AmountResource::isLifeSupport)
				.map(AmountResource::getID)
				.collect(Collectors.toSet());
		
		createEssentialResources();
	}
	
	/**
	 * Checks if this resource is a life support resource.
	 * 
	 * @param id
	 * @return
	 */
	public static boolean isLifeSupport(int id) {
		return lifeSupportResources.contains(id);
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
	 * Creates maps of amount resources.
	 */
	private static synchronized void createMaps() {
		if (amountResourceByName == null) {

			Map<String, AmountResource> tempAmountResourceMap = new HashMap<>();
			for (AmountResource resource : resources) {
				tempAmountResourceMap.put(resource.getName().toLowerCase(), resource);
			}

			Map<Integer, AmountResource> tempAmountResourceIDMap = new HashMap<>();
			for (AmountResource resource : resources) {
				tempAmountResourceIDMap.put(resource.getID(), resource);
			}

			// Create immutable internals
			amountResourceByName = Collections.unmodifiableMap(tempAmountResourceMap);
			amountResourceById = Collections.unmodifiableMap(tempAmountResourceIDMap);
		}
	}

	/**
	 * Maps ids to amount resources.
	 */
	public static int getFixedId(String resourceName) {
		return fixedResources.getOrDefault(resourceName.toLowerCase(), -1);
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
		return amountResourceById.get(id);
	}

	/**
	 * Finds an amount resource by name.
	 *
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static AmountResource findAmountResource(String name) {
		return amountResourceByName.get(name.toLowerCase());
	}

	/**
	 * Finds an amount resource by name.
	 *
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static final int findIDbyAmountResourceName(String name) {
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
	 * Is this an in-situ resource ?
	 * 
	 * @param resource
	 * @return
	 */
	public static boolean isInSitu(int resource) {
		return switch (resource) {
			case iceID, regolithID, regolithBID, regolithCID, regolithDID -> true;
			default -> false;
		};
	}
	
	/**
	 * Is this a raw material resource ?
	 * 
	 * @param resource
	 * @return
	 */
	public static boolean isRawMaterial(int resource) {
		return switch (resource) {
			case sandID, olivineID, brineWaterID, gypsumID,
					malachiteID, kamaciteID, sylviteID -> true;
			default -> false;
		};
	}
}

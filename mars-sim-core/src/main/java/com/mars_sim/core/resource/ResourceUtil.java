/*
 * Mars Simulation Project
 * ResourceUtil.java
 * @date 2025-07-23
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
import com.mars_sim.core.goods.GoodType;

public class ResourceUtil {

	
	public static final int FIRST_AMOUNT_RESOURCE_ID = 200;
	public static final int FIRST_ITEM_RESOURCE_ID = 500;
	public static final int FIRST_VEHICLE_RESOURCE_ID = 1000;
	public static final int FIRST_EQUIPMENT_RESOURCE_ID = 1010;
	public static final int FIRST_ROBOT_RESOURCE_ID = 1020;
	public static final int FIRST_BIN_RESOURCE_ID = 1040;

	public static final int WATER_ID = FIRST_AMOUNT_RESOURCE_ID;
	public static final int FOOD_ID = WATER_ID + 1;

	public static final int OXYGEN_ID = FOOD_ID + 1;
	public static final int CO2_ID = OXYGEN_ID + 1;
	public static final int ARGON_ID = CO2_ID + 1;
	public static final int NITROGEN_ID = ARGON_ID + 1;
	public static final int HYDROGEN_ID = NITROGEN_ID + 1;
	public static final int METHANE_ID = HYDROGEN_ID + 1;
	public static final int METHANOL_ID = METHANE_ID + 1;
	public static final int CO_ID = METHANOL_ID + 1;
	public static final int ACETYLENE_ID = CO_ID + 1;
	public static final int CHLORINE_ID = ACETYLENE_ID + 1;
	public static final int ICE_ID = CHLORINE_ID + 1;
	public static final int REGOLITH_ID = ICE_ID + 1;
	public static final int REGOLITHB_ID = REGOLITH_ID + 1;
	public static final int REGOLITHC_ID = REGOLITHB_ID + 1;
	public static final int REGOLITHD_ID = REGOLITHC_ID + 1;
	public static final int GYPSUM_PLASTER_ID = REGOLITHD_ID + 1;
	
	// 6 types of ores 
	public static final int GYPSUM_ID = GYPSUM_PLASTER_ID + 1;
	public static final int BASALTIC_ID = GYPSUM_ID + 1;
	public static final int SMECTITE_ID = BASALTIC_ID + 1;
	public static final int ALLOPHANE_ID = SMECTITE_ID + 1;
	public static final int AKAGANEITE_ID = ALLOPHANE_ID + 1;
	public static final int BASSANITE_ID = AKAGANEITE_ID + 1;
	
	// 11 types of Mineral concentrations :
	public static final int CHALCOPYRITE_ID = BASSANITE_ID + 1;
	public static final int GEOTHITE_ID = CHALCOPYRITE_ID + 1;
	public static final int HEMATITE_ID = GEOTHITE_ID + 1;
	public static final int KAMACITE_ID = HEMATITE_ID + 1;
	public static final int MAGNESITE_ID = KAMACITE_ID + 1;
	
	public static final int MAGNETITE_ID = MAGNESITE_ID + 1;
	public static final int MALACHITE_ID = MAGNETITE_ID + 1;
	public static final int OLIVINE_ID = MALACHITE_ID + 1;
	public static final int PYROXENE_ID = OLIVINE_ID + 1;
	public static final int SYLVITE_ID = PYROXENE_ID + 1;
	public static final int TAENITE_ID = SYLVITE_ID + 1;
	
	// 10 types of rocks
	public static final int ROCK_SAMPLES_ID = TAENITE_ID + 1;
	private static final int COLUMNAR_BASALT_ID = ROCK_SAMPLES_ID + 1;
	private static final int GRANITE_ID = COLUMNAR_BASALT_ID + 1;
	private static final int MUDSTONE_ID = GRANITE_ID + 1;
	private static final int SANDSTONE_ID = MUDSTONE_ID + 1;
	
	private static final int SHALE_ID = SANDSTONE_ID + 1;
	private static final int CONGLOMERATE_ID = SHALE_ID + 1;
	private static final int CROSSBEDDING_ID = CONGLOMERATE_ID + 1;
	public static final int METEORITE_ID = CROSSBEDDING_ID + 1;
	private static final int SCORIAL_ID = METEORITE_ID + 1;
	
	public static final int SOIL_ID = SCORIAL_ID + 1;
	public static final int SAND_ID = SOIL_ID + 1;
	public static final int BLACK_WATER_ID = SAND_ID + 1;
	public static final int GREY_WATER_ID = BLACK_WATER_ID + 1;
	public static final int BRINE_WATER_ID = GREY_WATER_ID + 1;
	
	public static final int CONCRETE_ID = BRINE_WATER_ID + 1;
	public static final int CEMENT_ID = CONCRETE_ID + 1;
	public static final int LIME_ID = CEMENT_ID + 1;
	
	public static final int COMPOST_ID = LIME_ID + 1;
	public static final int CROP_WASTE_ID = COMPOST_ID + 1;
	public static final int FOOD_WASTE_ID = CROP_WASTE_ID + 1;
	public static final int TOXIC_WASTE_ID = FOOD_WASTE_ID + 1;
	public static final int SOLID_WASTE_ID = TOXIC_WASTE_ID + 1;
	
	public static final int TOILET_TISSUE_ID = SOLID_WASTE_ID + 1;
	public static final int FERTILIZER_ID = TOILET_TISSUE_ID + 1;
	public static final int NACLO_ID = FERTILIZER_ID + 1; 
	public static final int NA2CO3_ID = NACLO_ID + 1;
	public static final int CLEANING_AGENT_ID = NA2CO3_ID + 1; 
	
	public static final int GLUCOSE_ID = CLEANING_AGENT_ID + 1;
	public static final int LEAVES_ID = GLUCOSE_ID + 1;
	public static final int FISH_OIL_ID = LEAVES_ID + 1;
	public static final int EPSOM_SALT_ID = FISH_OIL_ID + 1;
	public static final int TABLE_SALT_ID = EPSOM_SALT_ID + 1;
	public static final int ROCK_SALT_ID = TABLE_SALT_ID + 1;
	public static final int E_WASTE_ID = ROCK_SALT_ID + 1;
	public static final int NAPKIN_ID = E_WASTE_ID + 1;

	public static final int FISH_MEAT_ID = NAPKIN_ID + 1;
	public static final int SPIRULINA_ID = FISH_MEAT_ID + 1;

	public static final int CARBON_ID = SPIRULINA_ID + 1;
	
	public static final int ETHANE_ID = CARBON_ID + 1;
	public static final int ETHYLENE_ID = ETHANE_ID + 1;
	public static final int ETHYLENE_GLYCOL_ID = ETHYLENE_ID + 1;
	public static final int POLYCARBONATE_RESIN_ID = ETHYLENE_GLYCOL_ID + 1;
	public static final int POLYESTER_FIBER_ID = POLYCARBONATE_RESIN_ID + 1;
	public static final int POLYESTER_RESIN_ID = POLYESTER_FIBER_ID + 1;
	public static final int PROPYLENE_ID = POLYESTER_RESIN_ID + 1;
	public static final int POLYETHYLENE_ID = PROPYLENE_ID + 1;
	public static final int POLYPROPYLENE_ID = POLYETHYLENE_ID + 1;
	public static final int POLYSTYRENE_ID = POLYPROPYLENE_ID + 1;
	public static final int POLYURETHANE_ID = POLYSTYRENE_ID + 1;
	public static final int STYRENE_ID = POLYURETHANE_ID + 1;
	
	public static final int IRON_POWDER_ID = STYRENE_ID  + 1;
	public static final int IRON_OXIDE_ID = IRON_POWDER_ID + 1;
	public static final int GLASS_ID = IRON_OXIDE_ID + 1;
	
	// Must be one after the last fixed resource
	public static final int FIRST_FREE_AMOUNT_RESOURCE_ID = GLASS_ID + 1;

	public static final int[] ROCK_IDS = new int[] {ROCK_SAMPLES_ID, 
			COLUMNAR_BASALT_ID, GRANITE_ID, SHALE_ID, MUDSTONE_ID, 
			CONGLOMERATE_ID ,SANDSTONE_ID, CROSSBEDDING_ID, 
			METEORITE_ID, SCORIAL_ID										
	};
	
	public static final int[] MINERAL_CONC_IDs = new int[]{CHALCOPYRITE_ID, GEOTHITE_ID,
														HEMATITE_ID, KAMACITE_ID, MAGNESITE_ID, MAGNETITE_ID,
														MALACHITE_ID, OLIVINE_ID, PYROXENE_ID, SYLVITE_ID, TAENITE_ID};
	
	public static final int[] ORE_DEPOSIT_IDS = new int[]{ALLOPHANE_ID, AKAGANEITE_ID, BASALTIC_ID,
														BASSANITE_ID, GYPSUM_ID, SMECTITE_ID};
	
	public static final int[] REGOLITH_TYPES_IDS = new int[] {REGOLITH_ID, REGOLITHB_ID,
														REGOLITHC_ID, REGOLITHD_ID};

	protected static Set<Integer> essentialResources;

	private static Map<String, AmountResource> amountResourceByName;
	private static Map<Integer, AmountResource> amountResourceById;
	private static Set<AmountResource> resources;
	
	/** A set of life support resources. */
	private static Set<Integer> lifeSupportResources;

	private static Set<Integer> oilResources;
	
	private static final Map<String, Integer> fixedResources = new HashMap<>();

	static {
		// Map the pre-defined resources to their names
		fixedResources.put("acetylene", ACETYLENE_ID);
		fixedResources.put("akaganeite", AKAGANEITE_ID);
		fixedResources.put("allophane", ALLOPHANE_ID);
		fixedResources.put("argon", ARGON_ID);
		fixedResources.put("basaltic", BASALTIC_ID);
		fixedResources.put("bassanite", BASSANITE_ID);
		fixedResources.put("black water", BLACK_WATER_ID);
		fixedResources.put("brine water", BRINE_WATER_ID);
		fixedResources.put("carbon", CARBON_ID);
		fixedResources.put("carbon dioxide", CO2_ID);
		fixedResources.put("carbon monoxide", CO_ID);
		fixedResources.put("cement", CEMENT_ID);
		fixedResources.put("chalcopyrite", CHALCOPYRITE_ID);
		fixedResources.put("chlorine", CHLORINE_ID);
		fixedResources.put("cleaning agent", CLEANING_AGENT_ID);
		fixedResources.put("columnar basalt", COLUMNAR_BASALT_ID);
		fixedResources.put("compost", COMPOST_ID);
		fixedResources.put("concrete", CONCRETE_ID);
		fixedResources.put("conglomerate", CONGLOMERATE_ID);
		fixedResources.put("crop waste", CROP_WASTE_ID);
		fixedResources.put("cross bedding", CROSSBEDDING_ID);
		fixedResources.put("electronic waste", E_WASTE_ID);
		fixedResources.put("epsom salt", EPSOM_SALT_ID);
		fixedResources.put("ethane", ETHANE_ID);
		fixedResources.put("ethylene", ETHYLENE_ID);
		fixedResources.put("ethylene glycol", ETHYLENE_GLYCOL_ID);
		fixedResources.put("fertilizer", FERTILIZER_ID);
		fixedResources.put("fish meat", FISH_MEAT_ID);
		fixedResources.put("fish oil", FISH_OIL_ID);
		fixedResources.put("food", FOOD_ID);
		fixedResources.put("food waste", FOOD_WASTE_ID);
		fixedResources.put("glass", GLASS_ID);
		fixedResources.put("goethite", GEOTHITE_ID);
		fixedResources.put("granite", GRANITE_ID);
		fixedResources.put("grey water", GREY_WATER_ID);
		fixedResources.put("gypsum", GYPSUM_ID);
		fixedResources.put("gypsum plaster", GYPSUM_PLASTER_ID);
		fixedResources.put("hematite", HEMATITE_ID);
		fixedResources.put("hydrogen", HYDROGEN_ID);
		fixedResources.put("ice", ICE_ID);
		fixedResources.put("kamacite", KAMACITE_ID);
		fixedResources.put("glucose", GLUCOSE_ID);
		fixedResources.put("iron powder", IRON_POWDER_ID);
		fixedResources.put("iron oxide", IRON_OXIDE_ID);
		fixedResources.put("leaves", LEAVES_ID);
		fixedResources.put("lime", LIME_ID);
		fixedResources.put("magnesite", MAGNESITE_ID);
		fixedResources.put("magnetite", MAGNETITE_ID);
		fixedResources.put("malachite", MALACHITE_ID);
		fixedResources.put("methane", METHANE_ID);
		fixedResources.put("methanol", METHANOL_ID);
		fixedResources.put("meteorite", METEORITE_ID);
		fixedResources.put("mudstone", MUDSTONE_ID);
		fixedResources.put("napkin", NAPKIN_ID);
		fixedResources.put("nitrogen", NITROGEN_ID);
		fixedResources.put("olivine", OLIVINE_ID);
		fixedResources.put("oxygen", OXYGEN_ID);
		
		fixedResources.put("polycarbonate resin", POLYCARBONATE_RESIN_ID);
		fixedResources.put("polyester fiber", POLYESTER_FIBER_ID);
		fixedResources.put("polyester resin", POLYESTER_RESIN_ID);
		fixedResources.put("polyethylene", POLYETHYLENE_ID);
		fixedResources.put("polypropylene", POLYPROPYLENE_ID);
		fixedResources.put("polystyrene", POLYSTYRENE_ID);
		fixedResources.put("polyurethane", POLYURETHANE_ID);
		fixedResources.put("propylene", PROPYLENE_ID);
		fixedResources.put("pyroxene", PYROXENE_ID);
	
		fixedResources.put("regolith", REGOLITH_ID);
		fixedResources.put("regolith-b", REGOLITHB_ID);
		fixedResources.put("regolith-c", REGOLITHC_ID);
		fixedResources.put("regolith-d", REGOLITHD_ID);
		fixedResources.put("rock salt", ROCK_SALT_ID);
		fixedResources.put("rock samples", ROCK_SAMPLES_ID);
		fixedResources.put("sand", SAND_ID);
		fixedResources.put("sandstone", SANDSTONE_ID);
		fixedResources.put("scoria", SCORIAL_ID);
		fixedResources.put("shale", SHALE_ID);
		fixedResources.put("smectite", SMECTITE_ID);
		fixedResources.put("sodium hypochlorite", NACLO_ID);
		fixedResources.put("sodium carbonate", NA2CO3_ID);
		fixedResources.put("soil", SOIL_ID);
		fixedResources.put("solid waste", SOLID_WASTE_ID);
		fixedResources.put("spirulina", SPIRULINA_ID);
		fixedResources.put("styrene", STYRENE_ID);
		fixedResources.put("sylvite", SYLVITE_ID);
		fixedResources.put("table salt", TABLE_SALT_ID);
		fixedResources.put("taenite", TAENITE_ID);
		fixedResources.put("toilet tissue", TOILET_TISSUE_ID);
		fixedResources.put("toxic waste", TOXIC_WASTE_ID);
		fixedResources.put("water", WATER_ID);

		// This check will only fail if a new resource has not been added correctly
		int expectedSize = FIRST_FREE_AMOUNT_RESOURCE_ID - FIRST_AMOUNT_RESOURCE_ID;
		if (fixedResources.size() != expectedSize) {
			throw new IllegalStateException("The number of fixed resources is not correct. Expected: " 
					+ expectedSize + ", Actual: " + fixedResources.size());
		}
	}

	/**
	 * Default Constructor for ResoureUtil.
	 */
	private ResourceUtil() {
	}

	/**
	 * Registers the known Amount Resources in the helper.
	 * 
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
			throw new IllegalStateException("The following fixed amount resources are missing: " + missingFixedNames);
		}

		lifeSupportResources = resources.stream()
				.filter(AmountResource::isLifeSupport)
				.map(AmountResource::getID)
				.collect(Collectors.toSet());
		
		oilResources = resources.stream()
				.filter(ar -> ar.getGoodType() == GoodType.OIL)
				.map(AmountResource::getID)
				.collect(Collectors.toSet());

		createEssentialResources();
	}
	

	/**
	 * Creates maps of amount resources.
	 */
	private static synchronized void createMaps() {
		if (amountResourceByName == null) {

			Map<String, AmountResource> tempAmountResourceMap = new HashMap<>();
			Map<Integer, AmountResource> tempAmountResourceIDMap = new HashMap<>();
			
			for (AmountResource resource : resources) {
				tempAmountResourceMap.put(resource.getName().toLowerCase(), resource);
				tempAmountResourceIDMap.put(resource.getID(), resource);
			}

			// Create immutable internals
			amountResourceByName = Collections.unmodifiableMap(tempAmountResourceMap);
			amountResourceById = Collections.unmodifiableMap(tempAmountResourceIDMap);
		}
	}

	/**
	 * Maps an id to an amount resources.
	 */
	public static int getFixedId(String resourceName) {
		return fixedResources.getOrDefault(resourceName.toLowerCase(), -1);
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
	 * Gets Resources tagged as type OIL.
	 * 
	 * @return
	 */
	public static Set<Integer> getOilResources() {
		return oilResources;
	}

	/**
	 * Creates a set of essential resources.
	 */
	private static void createEssentialResources() {
		essentialResources = new HashSet<>();
		for (Food f: FoodUtil.getFoodList()) {
			essentialResources.add(f.getID());
		}
		
		for (int i: MINERAL_CONC_IDs) {
			essentialResources.add(i);
		}
		for (int i: ORE_DEPOSIT_IDS) {
			essentialResources.add(i);
		}
		for (int i: REGOLITH_TYPES_IDS) {
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
		AmountResource ar = amountResourceById.get(id);
		if (ar == null) {
			throw new IllegalArgumentException("Resource '" + id + "' not found.");
		}
		return ar;
	}

	/**
	 * Finds an amount resource by name.
	 *
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static AmountResource findAmountResource(String name) {
		AmountResource ar = amountResourceByName.get(name.toLowerCase());
		if (ar == null) {
			throw new IllegalArgumentException("Resource '" + name + "' not found.");
		}
		return ar;
	}

	/**
	 * Finds an amount resource by name.
	 *
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static final int findIDbyAmountResourceName(String name) {
		return findAmountResource(name).getID();
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
			case REGOLITH_ID, REGOLITHB_ID, REGOLITHC_ID, REGOLITHD_ID -> true;
			default -> false;
		};
	}
	
	/**
	 * Is this a waste resource ?
	 * 
	 * @param resource
	 * @return
	 */
	public static boolean isWasteProduct(int resource) {
		return switch (resource) {
			case GREY_WATER_ID, BLACK_WATER_ID, SOLID_WASTE_ID, TOXIC_WASTE_ID, 
				COMPOST_ID, FOOD_WASTE_ID, CROP_WASTE_ID, CO_ID -> true;
			default -> false;
		};
	}
	
	/**
	 * Is this tier 0 resource ?
	 * 
	 * @param resource
	 * @return
	 */
	public static boolean isTier0Resource(int resource) {
		return switch (resource) {
			case ICE_ID, BRINE_WATER_ID, ROCK_SALT_ID, HYDROGEN_ID -> true;
			default -> false;
		};
	}
	
	/**
	 * Is this tier 1 resource ?
	 * 
	 * @param resource
	 * @return
	 */
	public static boolean isTier1Resource(int resource) {
		return switch (resource) {
			case METHANE_ID, WATER_ID -> true;
			default -> false;
		};
	}
	
	/**
	 * Is this tier 3 resource ?
	 * 
	 * @param resource
	 * @return
	 */
	public static boolean isTier3Resource(int resource) {
		return switch (resource) {
			case CO2_ID -> true;
			default -> false;
		};
	}
	
	
	/**
	 * Is this derived resource ?
	 * 
	 * @param resource
	 * @return
	 */
	public static boolean isDerivedResource(int resource) {
		return switch (resource) {
			case GLUCOSE_ID, LEAVES_ID, SOIL_ID -> true;
			default -> false;
		};
	}
	
	/**
	 * Is this a chemical ?
	 * 
	 * @param resource
	 * @return
	 */
	public static boolean isChemical(int resource) {
		return switch (resource) {
			case ETHANE_ID, ETHYLENE_ID, ETHYLENE_GLYCOL_ID, PROPYLENE_ID, STYRENE_ID,
				 POLYETHYLENE_ID, POLYPROPYLENE_ID , POLYSTYRENE_ID, 
				 POLYURETHANE_ID, 
				 POLYESTER_RESIN_ID, POLYCARBONATE_RESIN_ID -> true;
			default -> false;
		};
	}
	
	/**
	 * Is this a construction resource ?
	 * 
	 * @param resource
	 * @return
	 */
	public static boolean isConstructionResource(int resource) {
		return switch (resource) {
			case GYPSUM_PLASTER_ID, GYPSUM_ID, CEMENT_ID, CONCRETE_ID, LIME_ID, ACETYLENE_ID -> true;
			default -> false;
		};
	}
	
	/**
	 * Is this a raw resource ?
	 * 
	 * @param resource
	 * @return
	 */
	public static boolean isRawElement(int resource) {
		return switch (resource) {
			case CARBON_ID, IRON_POWDER_ID -> true;
			default -> false;
		};
	}
	
	/**
	 * Is this a critical resource ?
	 * 
	 * @param resource
	 * @return
	 */
	public static boolean isCriticalResource(int resource) {
		return switch (resource) {
			case GLASS_ID -> true;
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
			case SAND_ID, 
					// 6 types of ores :
					GYPSUM_ID,
					BASALTIC_ID,
					SMECTITE_ID,
					ALLOPHANE_ID,
					AKAGANEITE_ID,
					BASSANITE_ID,
					
					// 11 types of Mineral concentrations :
					CHALCOPYRITE_ID,
					GEOTHITE_ID,
					HEMATITE_ID, 
					KAMACITE_ID, 
					MAGNESITE_ID,
					
					MAGNETITE_ID, 
					MALACHITE_ID,
					OLIVINE_ID,
					PYROXENE_ID,
					SYLVITE_ID,
					TAENITE_ID
					
					-> true;
			default -> false;
		};
	}
}

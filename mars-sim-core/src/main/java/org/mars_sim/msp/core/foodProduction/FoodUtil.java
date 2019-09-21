/**
 * Mars Simulation Project
 * FoodUtil.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.foodProduction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.resource.ResourceUtil;

/**
 * Utility class for food information.
 */
public class FoodUtil {

	// Data members
	private static List<Food> foodList;

	/**
	 * Private constructor for utility class.
	 */
	private FoodUtil() {
	}

	/**
	 * Gets a list of all food in the simulation.
	 * 
	 * @return list of food
	 */
	public static List<Food> getFoodList() {

		if (foodList == null) {
			foodList = new ArrayList<Food>();
			populateFoodList();
		}

		return Collections.unmodifiableList(foodList);
	}

	/**
	 * Destroys the current food list.
	 */
	public static void destroyFoodList() {

		if (foodList != null) {
			foodList.clear();
		}

		foodList = null;
	}

	public static Food getResourceFood(Resource resource) {
		if (resource == null) {
			throw new IllegalArgumentException("Resource cannot be null");
		}
		FoodType category = null;

		if (resource instanceof AmountResource && ((AmountResource) resource).isEdible())
			category = FoodType.AMOUNT_RESOURCE;
		// else if (resource instanceof ItemResource)
		// category = FoodType.ITEM_RESOURCE;
		Food food = new Food(resource.getName(), resource, category);
		return food;
	}

	/**
	 * Gets a food object for a given resource.
	 * 
	 * @param resource the resource.
	 * @return food for the resource.
	 */
	public static Food getResourceFood(Integer resource) {
		if (resource == null) {
			throw new IllegalArgumentException("Resource cannot be null");
		}
		FoodType category = null;

		if (resource < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
			category = FoodType.AMOUNT_RESOURCE;
			AmountResource ar = ResourceUtil.findAmountResource(resource);
			if (ar.isEdible()) {
				Food food = new Food(ar.getName(), ar, category);
				return food;
			}
		}
//			else if (resource >= Task.FIRST_ITEM_RESOURCE) {
//				category = FoodType.ITEM_RESOURCE;
//				Part p = ItemResourceUtil.findItemResource(resource);
//				if (p.isEdible()) {
//					Food food = new Food(p.getName(), p, category);
//				}
//			}

		return null;

	}

//	/**
//	 * Gets a food object for a given equipment class.
//	 * @param equipmentClass the equipment class.
//	 * @return food for the resource class or null if none.
//	 */
//	public static Food getEquipmentFood(Class<? extends Unit> equipmentClass) {
//		if (equipmentClass == null) {
//			throw new IllegalArgumentException("goodClass cannot be null");
//		}
//		Food result = null;
//
//		Iterator<Food> i = getFoodList().iterator();
//		while (i.hasNext()) {
//			Food food = i.next();
//			if (food.getClassType() == equipmentClass)
//				result = new Food(food.getName(), equipmentClass, FoodType.EQUIPMENT);
//		}
//
//		return result;
//	}

//	/**
//	 * Gets a food object for the given vehicle type.
//	 * @param vehicleType the vehicle type string.
//	 * @return food for the vehicle type.
//	 */
//	public static Food getVehicleFood(String vehicleType) {
//		if ((vehicleType == null) || vehicleType.trim().length() == 0) {
//			throw new IllegalArgumentException("vehicleType cannot be blank or null.");
//		}
//		Class<?> vehicleClass = Rover.class;
//		if (LightUtilityVehicle.NAME.equalsIgnoreCase(vehicleType))
//			vehicleClass = LightUtilityVehicle.class;
//		return new Food(vehicleType, vehicleClass, FoodType.VEHICLE);
//
//	}

	/**
	 * Checks if a food is valid in the simulation.
	 * 
	 * @param food the food to check.
	 * @return true if food is valid.
	 */
	public static boolean containsFood(Food food) {
		if (food == null) {
			throw new IllegalArgumentException("food cannot be null.");
		}
		return getFoodList().contains(food);
	}

	/**
	 * Populates the food list with all food.
	 */
	private static void populateFoodList() {
		// Populate amount resources.
		populateAmountResources();

		// Populate item resources.
		// populateItemResources();

		// Populate equipment.
		// populateEquipment();

		// Populate vehicles.
		// populateVehicles();

		// Sort food by name.
		Collections.sort(foodList);
	}

	/**
	 * Populates the food list with all amount resources.
	 */
	private static void populateAmountResources() {
		boolean edible = false;
		AmountResource ar = null;
		Iterator<AmountResource> i = ResourceUtil.getAmountResources().iterator();
		while (i.hasNext()) {
			ar = i.next();
			edible = ar.isEdible();
			if (edible == true)
				foodList.add(getResourceFood(ar));
		}
	}

//	/**
//	 * Populates the food list with all item resources.
//	 */
//	private static void populateItemResources() {
//		// Iterator<ItemResource> i = ItemResource.getItemResources().iterator();
//		Iterator<Part> i = ItemResourceUtil.getItemResources().iterator();
//		while (i.hasNext())
//			foodList.add(getResourceFood(i.next()));
//	}

//	/**
//	 * Populates the food list with all equipment.
//   */
//	private static void populateEquipment() {
//		List<String> equipmentNames = new ArrayList<String>(EquipmentFactory.getEquipmentNames());
//		Collections.sort(equipmentNames);
//		Iterator<String> i = equipmentNames.iterator();
//		while (i.hasNext()) {
//			String name = i.next();
//			Class<? extends Equipment> equipmentClass = EquipmentFactory.getEquipmentClass(name);
//			foodList.add(new Food(name, equipmentClass, FoodType.EQUIPMENT));
//		}
//	}

//	/**
//	 * Populates the food list with all vehicles.
//	 */
//	private static void populateVehicles() {
//		VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
//
//		try {
//			Iterator<String> i = config.getVehicleTypes().iterator();
//			while (i.hasNext()) foodList.add(getVehicleFood(i.next()));
//		}
//		catch (Exception e) {
//			e.printStackTrace(System.err);
//		}
//	}

	/**
	 * Gets the mass per item for a food.
	 * 
	 * @param food the food to check.
	 * @return mass (kg) per item (or 1kg for amount resources).
	 * @throws Exception if error getting mass per item.
	 */
	public static double getFoodMassPerItem(Food food) {
		double result = 0D;

		if (FoodType.AMOUNT_RESOURCE == food.getCategory())
			result = 1D;

		// else if (FoodType.ITEM_RESOURCE == food.getCategory())
		// result = ((ItemResource) food.getObject()).getMassPerItem();
		// else if (FoodType.EQUIPMENT == food.getCategory())
		// result = EquipmentFactory.getEquipmentMass(food.getName());
		// else if (FoodType.VEHICLE == food.getCategory()) {
		// VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
		// result = config.getEmptyMass(food.getName());
		// }

		return result;
	}

}
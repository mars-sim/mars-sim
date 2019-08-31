/**
 * Mars Simulation Project
 * GoodsUtil.java
 * @version 3.1.0 2017-09-11
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.goods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.vehicle.VehicleConfig;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * Utility class for goods information.
 */
public class GoodsUtil {

	private static Logger logger = Logger.getLogger(GoodsUtil.class.getName());
	
	// Data members
	private static List<Good> goodsList;

	private static VehicleConfig vehicleConfig = SimulationConfig.instance().getVehicleConfiguration();
	
	/**
	 * Private constructor for utility class.
	 */
	private GoodsUtil() {
	}

	/**
	 * Gets a list of all goods in the simulation.
	 * 
	 * @return list of goods
	 */
	public static List<Good> getGoodsList() {
		if (goodsList == null) {
			goodsList = new ArrayList<Good>();
			populateGoodsList();
		}

		return Collections.unmodifiableList(goodsList);
	}

	/**
	 * Destroys the current goods list.
	 */
	public static void destroyGoodsList() {
		if (goodsList != null) {
			goodsList.clear();
		}

		goodsList = null;
	}

	public static Good createResourceGood(Resource resource) {
		if (resource == null) {
			logger.severe("resource is NOT supposed to be null.");
//			throw new IllegalArgumentException("resource cannot be null");
		}
		GoodType category = null;
		if (resource instanceof AmountResource)
			category = GoodType.AMOUNT_RESOURCE;
		else if (resource instanceof ItemResource)
			category = GoodType.ITEM_RESOURCE;
		return new Good(resource.getName(), resource.getID(), category);
	}

	/**
	 * Creates a good object for a given resource.
	 * 
	 * @param id the resource id.
	 * @return good for the resource.
	 */
	public static Good createResourceGood(int id) {
		GoodType category = null;
		if (id < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
			category = GoodType.AMOUNT_RESOURCE;
			return new Good(ResourceUtil.findAmountResourceName(id), id, category);
		} else if (id >= ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
			category = GoodType.ITEM_RESOURCE;
			return new Good(ItemResourceUtil.findItemResourceName(id), id, category);
		}
		return null;
	}

	/**
	 * Gets a good object for a given resource.
	 * 
	 * @param resource the resource.
	 * @return good for the resource.
	 */
	public static Good getResourceGood(Resource resource) {
		if (resource == null) { 
			logger.severe("resource is NOT supposed to be null.");
//			throw new IllegalArgumentException("resource cannot be null");
		}
//		GoodType category = null;
//		if (resource instanceof AmountResource)
//			category = GoodType.AMOUNT_RESOURCE;
//		else if (resource instanceof ItemResource)
//			category = GoodType.ITEM_RESOURCE;
//		return new Good(resource.getName(), resource.getID(), category);
		int id = resource.getID();
		Iterator<Good> i = getGoodsList().iterator();
		while (i.hasNext()) {
			Good good = i.next();
			if (good.getID() == id)
				return good;	
		}
		logger.severe("resource is NOT supposed to be null.");
		return null;
	}

	/**
	 * Gets a good object for a given resource.
	 * 
	 * @param id the resource id.
	 * @return good for the resource.
	 */
	public static Good getResourceGood(int id) {
//		if (resource == null) {
//			throw new IllegalArgumentException("resource cannot be null");
//		}
//		GoodType category = null;
//		if (id < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
//			category = GoodType.AMOUNT_RESOURCE;
//			return new Good(ResourceUtil.findAmountResourceName(id), id, category);
//		} else if (id >= ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
//			category = GoodType.ITEM_RESOURCE;
//			return new Good(ItemResourceUtil.findItemResourceName(id), id, category);
//		}
		Iterator<Good> i = getGoodsList().iterator();
		while (i.hasNext()) {
			Good good = i.next();
			if (good.getID() == id)
				return good;	
		}
		logger.severe("resource is NOT supposed to be null.");
		return null;
	}
	
	/**
	 * Creates a good object for a given equipment class.
	 * 
	 * @param equipmentClass the equipment class.
	 * @return good for the resource class or null if none.
	 */
	public static Good createEquipmentGood(Class<?> equipmentClass) {
		if (equipmentClass == null) {
			throw new IllegalArgumentException("goodClass cannot be null");
		}
		int id = EquipmentType.getEquipmentID(equipmentClass);
		return new Good(EquipmentType.convertID2Type(id).getType(), id, GoodType.EQUIPMENT);
	}

	/**
	 * Creates a good object for a given equipment class.
	 * 
	 * @param id the equipment id.
	 * @return good for the resource class or null if none.
	 */
	public static Good createEquipmentGood(int id) {
		return new Good(EquipmentType.convertID2Type(id).getType(), id, GoodType.EQUIPMENT);
//		Class<? extends Unit> equipmentClass = EquipmentFactory.getEquipmentClass(EquipmentType.int2enum(e).getName());
//		return getEquipmentGood(EquipmentFactory.getEquipmentClass(EquipmentType.convertID2Type(id).getName()));
	}
	
	/**
	 * Gets a good object for a given equipment class.
	 * 
	 * @param equipmentClass the equipment class.
	 * @return good for the resource class or null if none.
	 */
	public static Good getEquipmentGood(Class<?> equipmentClass) {
		if (equipmentClass == null) {
			logger.severe("equipmentClass is NOT supposed to be null.");
//			throw new IllegalArgumentException("equipmentClass cannot be null");
		}
		int id = EquipmentType.getEquipmentID(equipmentClass);
		Iterator<Good> i = getGoodsList().iterator();
		while (i.hasNext()) {
			Good good = i.next();
			if (good.getID() == id)
				return good;	
		}
		
		return null;
	}

	public static Good getEquipmentGood(int id) {
		Iterator<Good> i = getGoodsList().iterator();
		while (i.hasNext()) {
			Good good = i.next();
			if (good.getID() == id)
				return good;	
		}
		
		return null;
	}

	/**
	 * Creates a good object for the given vehicle type.
	 * 
	 * @param vehicleType the vehicle type string.
	 * @return good for the vehicle type.
	 */
	public static Good createVehicleGood(String vehicleType) {
		if ((vehicleType == null) || vehicleType.trim().length() == 0) {
			logger.severe("vehicleType is NOT supposed to be blacnk or null.");
//			throw new IllegalArgumentException("vehicleType cannot be blank or null.");
		}
//		Class<?> vehicleClass = Rover.class;
//		if (LightUtilityVehicle.NAME.equalsIgnoreCase(vehicleType))
//			vehicleClass = LightUtilityVehicle.class;
//		return new Good(vehicleType, vehicleClass, GoodType.VEHICLE);
		return new Good(vehicleType, VehicleType.convertName2ID(vehicleType), GoodType.VEHICLE);
	}

	/**
	 * Gets a good object for the given vehicle type.
	 * 
	 * @param vehicleType the vehicle type string.
	 * @return good for the vehicle type.
	 */
	public static Good getVehicleGood(String vehicleType) {
		if ((vehicleType == null) || vehicleType.trim().length() == 0) {
			logger.severe("vehicleType is NOT supposed to be blacnk or null.");
//			throw new IllegalArgumentException("vehicleType cannot be blank or null.");
		}
		int id = VehicleType.convertName2ID(vehicleType);
		Iterator<Good> i = getGoodsList().iterator();
		while (i.hasNext()) {
			Good good = i.next();
			if (good.getID() == id)
				return good;	
		}
		return null;
	}
	
	/**
	 * Checks if a good is valid in the simulation.
	 * 
	 * @param good the good to check.
	 * @return true if good is valid.
	 */
	public static boolean containsGood(Good good) {
		if (good == null) {
//			throw new IllegalArgumentException("good cannot be null.");
			logger.severe("good is NOT supposed to be null.");
		}
		return getGoodsList().contains(good);
	}

	/**
	 * Populates the goods list with all goods.
	 */
	private static void populateGoodsList() {
		// Populate amount resources.
		populateAmountResources();

		// Populate item resources.
		populateItemResources();

		// Populate equipment.
		populateEquipment();

		// Populate vehicles.
		populateVehicles();

		// Sort goods by name.
		Collections.sort(goodsList);
	}

	/**
	 * Populates the goods list with all amount resources.
	 */
	private static void populateAmountResources() {
//		Iterator<Integer> i = ResourceUtil.getInstance().getARIDs().iterator();
		Iterator<AmountResource> i = ResourceUtil.getAmountResources().iterator();
		while (i.hasNext())
			goodsList.add(createResourceGood(i.next()));
	}

	/**
	 * Populates the goods list with all item resources.
	 */
	private static void populateItemResources() {
//		Iterator<Integer> i = ItemResourceUtil.getItemIDs().iterator();
		Iterator<Part> i = ItemResourceUtil.getItemResources().iterator();
		while (i.hasNext())
			goodsList.add(createResourceGood(i.next()));
	}

	/**
	 * Populates the goods list with all equipment.
	 */
	private static void populateEquipment() {
		List<String> equipmentNames = new ArrayList<String>(EquipmentFactory.getEquipmentNames());
		Collections.sort(equipmentNames);
		Iterator<String> i = equipmentNames.iterator();
		while (i.hasNext()) {
			String name = i.next();
//			Class<? extends Equipment> equipmentClass = EquipmentFactory.getEquipmentClass(name);
			int id = EquipmentType.convertType2ID(name);
			goodsList.add(new Good(name, id, GoodType.EQUIPMENT));
		}
	}

	/**
	 * Populates the goods list with all vehicles.
	 */
	private static void populateVehicles() {
		try {
			Iterator<String> i = vehicleConfig.getVehicleTypes().iterator();
			while (i.hasNext())
				goodsList.add(createVehicleGood(i.next()));
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Gets the mass per item for a good.
	 * 
	 * @param good the good to check.
	 * @return mass (kg) per item (or 1kg for amount resources).
	 * @throws Exception if error getting mass per item.
	 */
	public static double getGoodMassPerItem(Good good) {
		double result = 0D;

		if (GoodType.AMOUNT_RESOURCE == good.getCategory())
			result = 1D;
		else if (GoodType.ITEM_RESOURCE == good.getCategory())
			result = ItemResourceUtil.findItemResource(good.getID()).getMassPerItem();
		else if (GoodType.EQUIPMENT == good.getCategory())
			result = EquipmentFactory.getEquipmentMass(good.getName());
		else if (GoodType.VEHICLE == good.getCategory()) {
			result = vehicleConfig.getEmptyMass(good.getName());
		}

		return result;
	}
}
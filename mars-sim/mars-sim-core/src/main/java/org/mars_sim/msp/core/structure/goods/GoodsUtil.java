/**
 * Mars Simulation Project
 * GoodsUtil.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.goods;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.*;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.VehicleConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for goods information.
 */
public class GoodsUtil {

	// Data members
	private static List<Good> goodsList;
	
	/**
	 * Private constructor for utility class.
	 */
	private GoodsUtil() {}
	
	/**
	 * Gets a list of all goods in the simulation.
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
	 * Gets a good object for a given resource.
	 * @param resource the resource.
	 * @return good for the resource.
	 */
	public static Good getResourceGood(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("resource cannot be null");
        }
            String category = null;
            if (resource instanceof AmountResource) category = Good.AMOUNT_RESOURCE;
            else if (resource instanceof ItemResource) category = Good.ITEM_RESOURCE;
            return new Good(resource.getName(), resource, category);
    }
	
	/**
	 * Gets a good object for a given equipment class.
	 * @param equipmentClass the equipment class.
	 * @return good for the resource class or null if none.
	 */
	public static Good getEquipmentGood(Class<? extends Unit> equipmentClass) {
        if (equipmentClass == null) {
            throw new IllegalArgumentException("goodClass cannot be null");
        }
            Good result = null;

            Iterator<Good> i = getGoodsList().iterator();
            while (i.hasNext()) {
                Good good = i.next();
                if (good.getClassType() == equipmentClass)
                    result = new Good(good.getName(), equipmentClass, Good.EQUIPMENT);
            }

            return result;
    }
	
	/**
	 * Gets a good object for the given vehicle type.
	 * @param vehicleType the vehicle type string.
	 * @return good for the vehicle type.
	 */
	public static Good getVehicleGood(String vehicleType) {
        if ((vehicleType == null) || vehicleType.trim().length() == 0) {
            throw new IllegalArgumentException("vehicleType cannot be blank or null.");
        }
            Class<?> vehicleClass = Rover.class;
            if (LightUtilityVehicle.NAME.equalsIgnoreCase(vehicleType))
                vehicleClass = LightUtilityVehicle.class;
            return new Good(vehicleType, vehicleClass, Good.VEHICLE);

    }
	
	/**
	 * Checks if a good is valid in the simulation.
	 * @param good the good to check.
	 * @return true if good is valid.
	 */
	public static boolean containsGood(Good good) {
        if (good == null) {
            throw new IllegalArgumentException("good cannot be null.");
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
		Iterator<AmountResource> i = AmountResource.getAmountResources().iterator();
		while (i.hasNext()) goodsList.add(getResourceGood(i.next()));
	}
	
	/**
	 * Populates the goods list with all item resources.
	 */
	private static void populateItemResources() {
		Iterator<ItemResource> i = ItemResource.getItemResources().iterator();
		while (i.hasNext()) goodsList.add(getResourceGood(i.next()));
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
	        Class<? extends Equipment> equipmentClass = EquipmentFactory.getEquipmentClass(name);
	        goodsList.add(new Good(name, equipmentClass, Good.EQUIPMENT));
	    }
	}
	
	/**
	 * Populates the goods list with all vehicles.
	 */
	private static void populateVehicles() {
		VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
		
		try {
			Iterator<String> i = config.getVehicleTypes().iterator();
			while (i.hasNext()) goodsList.add(getVehicleGood(i.next()));
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * Gets the mass per item for a good.
	 * @param good the good to check.
	 * @return mass (kg) per item (or 1kg for amount resources).
	 * @throws Exception if error getting mass per item.
	 */
	public static double getGoodMassPerItem(Good good) {
		double result = 0D;
		
		if (Good.AMOUNT_RESOURCE.equals(good.getCategory())) result = 1D;
		else if (Good.ITEM_RESOURCE.equals(good.getCategory())) 
			result = ((ItemResource) good.getObject()).getMassPerItem();
		else if (Good.EQUIPMENT.equals(good.getCategory())) 
			result = EquipmentFactory.getEquipmentMass(good.getName());
		else if (Good.VEHICLE.equals(good.getCategory())) {
			VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
			result = config.getEmptyMass(good.getName());
		}
		
		return result;
	}
}
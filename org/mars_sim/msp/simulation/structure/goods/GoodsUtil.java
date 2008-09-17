/**
 * Mars Simulation Project
 * GoodsUtil.java
 * @version 2.85 2008-09-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.goods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.equipment.Bag;
import org.mars_sim.msp.simulation.equipment.Barrel;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.equipment.EquipmentFactory;
import org.mars_sim.msp.simulation.equipment.GasCanister;
import org.mars_sim.msp.simulation.equipment.SpecimenContainer;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ItemResource;
import org.mars_sim.msp.simulation.resource.Resource;
import org.mars_sim.msp.simulation.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.simulation.vehicle.VehicleConfig;

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
		if (resource != null) {
			String category = null;
			if (resource instanceof AmountResource) category = Good.AMOUNT_RESOURCE;
			else if (resource instanceof ItemResource) category = Good.ITEM_RESOURCE;
			return new Good(resource.getName(), resource, category);
		}
		else throw new IllegalArgumentException("resource cannot be null");
	}
	
	/**
	 * Gets a good object for a given equipment class.
	 * @param equipmentClass the equipment class.
	 * @return good for the resource class or null if none.
	 */
	public static Good getEquipmentGood(Class equipmentClass) {
		if (equipmentClass != null) {
			Good result = null;
			
			Iterator<Good> i = getGoodsList().iterator();
			while (i.hasNext()) {
				Good good = i.next();
				if (good.getClassType() == equipmentClass) 
					result = new Good(good.getName(), equipmentClass, Good.EQUIPMENT);
			}
			
			return result;
		}
		else throw new IllegalArgumentException("goodClass cannot be null");
	}
	
	/**
	 * Gets a good object for the given vehicle type.
	 * @param vehicleType the vehicle type string.
	 * @return good for the vehicle type.
	 */
	public static Good getVehicleGood(String vehicleType) {
		if ((vehicleType != null) && !vehicleType.trim().equals("")) {
			Class vehicleClass = Rover.class;
			if (LightUtilityVehicle.NAME.equalsIgnoreCase(vehicleType)) 
				vehicleClass = LightUtilityVehicle.class;
			return new Good(vehicleType, vehicleClass, Good.VEHICLE);
		}
		else throw new IllegalArgumentException("vehicleType cannot be blank or null.");
	}
	
	/**
	 * Checks if a good is valid in the simulation.
	 * @param good the good to check.
	 * @return true if good is valid.
	 */
	public static boolean containsGood(Good good) {
		if (good != null) return getGoodsList().contains(good);
		else throw new IllegalArgumentException("good cannot be null.");
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
		Iterator i = AmountResource.getAmountResources().iterator();
		while (i.hasNext()) goodsList.add(getResourceGood((AmountResource) i.next()));
	}
	
	/**
	 * Populates the goods list with all item resources.
	 */
	private static void populateItemResources() {
		Iterator i = ItemResource.getItemResources().iterator();
		while (i.hasNext()) goodsList.add(getResourceGood((ItemResource) i.next()));
	}
	
	/**
	 * Populates the goods list with all equipment.
	 */
	private static void populateEquipment() {
		goodsList.add(new Good(EVASuit.TYPE, EVASuit.class, Good.EQUIPMENT));
		goodsList.add(new Good(Bag.TYPE, Bag.class, Good.EQUIPMENT));
		goodsList.add(new Good(Barrel.TYPE, Barrel.class, Good.EQUIPMENT));
		goodsList.add(new Good(GasCanister.TYPE, GasCanister.class, Good.EQUIPMENT));
		goodsList.add(new Good(SpecimenContainer.TYPE, SpecimenContainer.class, Good.EQUIPMENT));
	}
	
	/**
	 * Populates the goods list with all vehicles.
	 */
	private static void populateVehicles() {
		VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
		
		try {
			Iterator i = config.getVehicleTypes().iterator();
			while (i.hasNext()) goodsList.add(getVehicleGood((String) i.next()));
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
	public static double getGoodMassPerItem(Good good) throws Exception {
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
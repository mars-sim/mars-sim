/**
 * Mars Simulation Project
 * GoodsUtil.java
 * @version 2.81 2007-04-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.goods;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.set.ListOrderedSet;

import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.equipment.Bag;
import org.mars_sim.msp.simulation.equipment.Barrel;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.equipment.GasCanister;
import org.mars_sim.msp.simulation.equipment.SpecimenContainer;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ItemResource;
import org.mars_sim.msp.simulation.resource.Resource;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.simulation.vehicle.VehicleConfig;

/**
 * Utility class for goods information.
 */
public class GoodsUtil {

	// Note: This is set who's elements are ordered by when
	// they are added to the set.
	private static ListOrderedSet goodsSet;
	
	/**
	 * Private constructor for utility class.
	 */
	private GoodsUtil() {}
	
	/**
	 * Gets an ordered set of all goods in the simulation.
	 * @return list of goods
	 */
	public static Set getGoodsSet() {
		
		if (goodsSet == null) {
			goodsSet = new ListOrderedSet();
			populateGoodsList();
		}
		
		return Collections.unmodifiableSet(goodsSet);
	}
	
	/**
	 * Gets a good object for a given resource.
	 * @param resource the resource.
	 * @return good for the resource.
	 */
	public static Good getResourceGood(Resource resource) {
		if (resource != null) return new Good(resource.getName(), resource);
		else throw new IllegalArgumentException("resource cannot be null");
	}
	
	/**
	 * Gets a good object for a given equipment class.
	 * @param goodClass the resource class.
	 * @return good for the resource class or null if none.
	 */
	public static Good getEquipmentGood(Class equipmentClass) {
		if (equipmentClass != null) {
			Good result = null;
			
			Iterator i = goodsSet.iterator();
			while (i.hasNext()) {
				Good good = (Good) i.next();
				if (good.getClassType() == equipmentClass) 
					result = new Good(good.getName(), equipmentClass);
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
		if ((vehicleType != null) && !vehicleType.trim().equals("")) 
			return new Good(vehicleType, Rover.class);
		else throw new IllegalArgumentException("vehicleType cannot be blank or null.");
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
	}
	
	/**
	 * Populates the goods list with all amount resources.
	 */
	private static void populateAmountResources() {
		Iterator i = AmountResource.getAmountResources().iterator();
		while (i.hasNext()) {
			AmountResource resource = (AmountResource) i.next();
			goodsSet.add(new Good(resource.getName(), resource));
		}
	}
	
	/**
	 * Populates the goods list with all item resources.
	 */
	private static void populateItemResources() {
		Iterator i = ItemResource.getItemResources().iterator();
		while (i.hasNext()) {
			ItemResource resource = (ItemResource) i.next();
			goodsSet.add(new Good(resource.getName(), resource));
		}
	}
	
	/**
	 * Populates the goods list with all equipment.
	 */
	private static void populateEquipment() {
		goodsSet.add(new Good(EVASuit.TYPE, EVASuit.class));
		goodsSet.add(new Good(Bag.TYPE, Bag.class));
		goodsSet.add(new Good(Barrel.TYPE, Barrel.class));
		goodsSet.add(new Good(GasCanister.TYPE, GasCanister.class));
		goodsSet.add(new Good(SpecimenContainer.TYPE, SpecimenContainer.class));
	}
	
	/**
	 * Populates the goods list with all vehicles.
	 */
	private static void populateVehicles() {
		VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
		
		try {
			Iterator i = config.getRoverTypes().iterator();
			while (i.hasNext()) goodsSet.add(new Good((String) i.next(), Rover.class));
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
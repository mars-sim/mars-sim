/*
 * Mars Simulation Project
 * MicroInventory.java
 * @date 2025-07-15
 * @author Manny Kung
 */
package com.mars_sim.core.equipment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.ItemResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;

/**
 * The MicroInventory class represents a simple resource storage solution.
 */
public class MicroInventory implements Serializable {

	static final class StockAmountStored implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		double storedAmount = 0;

		StockAmountStored() {
			super();
		}

		@Override
		public String toString() {
			return "StockAmountStored [storedAmount: " + storedAmount + "]";
		}
	}
	
	static final class AmountStored implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		double capacity = 0;
		double storedAmount = 0;

		AmountStored(double capacity) {
			super();
			this.capacity = capacity;
		}

		@Override
		public String toString() {
			return "AmountStored [capacity: " + capacity
					+ ", storedAmount: " + storedAmount + "]";
		}
	}

	static final class ItemStored implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		int quantity = 0;
		double massPerItem = 0;
		double totalMass = 0;

		ItemStored() {
			super();
		}

		@Override
		public String toString() {
			return "ItemStored [quantity=" + quantity + " massPerItem: " + massPerItem
					+ " totalMass: " + totalMass + "]";
		}
	}

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(MicroInventory.class.getName());

	private static final double SMALL_AMOUNT = 0.000001;

	/** The owner of this micro inventory. */
	private Unit owner;
	/** A map of stock amount resources. */
	private Map<Integer, StockAmountStored> stockAmountStorage = new HashMap<>();
	/** A map of specific amount resources. */
	private Map<Integer, AmountStored> specificAmountStorage = new HashMap<>();
	/** A map of item resources. */
	private Map<Integer, ItemStored> itemStorage = new HashMap<>();

	private double stockAmountTotalMass = 0D;
	private double specificAmountTotalMass = 0D;
	private double itemTotalMass = 0D;
	private double sharedCapacity = 0D;

	public MicroInventory(Unit owner) {
		this.owner = owner;
	}

	public MicroInventory(Unit owner, double sharedCapacity) {
		this.owner = owner;
		this.sharedCapacity = sharedCapacity;
	}

	/**
	 * Gets the shared/general/stock capacity.
	 *
	 * @return
	 */
	public double getSharedCapacity() {
		return sharedCapacity;
	}

	/**
	 * Sets the shared/general/stock capacity.
	 *
	 * @return
	 */
	public void setSharedCapacity(double capacity) {
		sharedCapacity = capacity;
	}

	/**
	 * Adds the shared/general/stock capacity.
	 *
	 * @return
	 */
	public void addSharedCapacity(double capacity) {
		sharedCapacity += capacity;
	}
	
	/**
     * Gets the specific/stock capacity of this amount resource that this container can hold.
     *
     * @return capacity (kg).
     */
    public double getCapacity(int resource) {
		AmountStored s = specificAmountStorage.get(resource);
		if (s != null) {
			return s.capacity;
		}
		return 0;//sharedCapacity;
    }

	/**
	 * Sets the capacity of an amount resource.
	 *
	 * @param resource
	 * @param capacity
	 */
	public void setSpecificCapacity(int resource, double capacity) {
		AmountStored s = specificAmountStorage.get(resource);
		if (s != null) {
			// To set to a new capacity
			s.capacity = capacity;
		}
		else {
			specificAmountStorage.put(resource, new AmountStored(capacity));
		}
	}

	/**
	 * Adds the capacity of an amount resource.
	 *
	 * @param resource
	 * @param capacity
	 */
	public void addSpecificCapacity(int resource, double capacity) {
		AmountStored s = specificAmountStorage.get(resource);
		if (s != null) {
			// To add to its existing capacity
			s.capacity += capacity;
		}
		else {
			specificAmountStorage.put(resource, new AmountStored(capacity));
		}
	}

	/**
	 * Removes the capacity of an amount resource.
	 *
	 * @param resource
	 * @param capacity
	 */
	public void removeSpecificCapacity(int resource, double capacity) {
		AmountStored s = specificAmountStorage.get(resource);

		if (s != null) {
			s.capacity -= capacity;
			if (s.capacity < 0D) {
				s.capacity = 0D;
			}
		}
	}

	/**
	 * Gets the total weight of the stored resources.
	 *
	 * @return mass [kg]
	 */
	public double getStoredMass() {
		return stockAmountTotalMass + specificAmountTotalMass + itemTotalMass;
	}

	/**
	 * Is this inventory empty ?
	 *
	 * @return
	 */
	public boolean isEmpty() {
		return getStoredMass() == 0D;
	}

	/**
	 * Is this inventory empty of this amount resource ?
	 *
	 * @param resource
	 * @return
	 */
	public boolean isEmpty(int resource) {
		AmountStored s = specificAmountStorage.get(resource);
		StockAmountStored ss = stockAmountStorage.get(resource);
		return (s == null) || (s.storedAmount == 0D)
				|| (s == null) || (ss.storedAmount == 0D);
	}

	/**
	 * Stores the specific amount resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	public double storeAmountResource(int resource, double quantity) {
		AmountStored s = specificAmountStorage.get(resource);
		if (s == null) {
			return quantity;
		}
			
		double remaining =  + s.capacity - s.storedAmount;
		double excess = 0D;
		if (remaining < quantity) {
			// Obtain the excess
			excess = quantity - remaining;
			// Update the quantity
			quantity = remaining;
			
			String name = ResourceUtil.findAmountResourceName(resource);
			for (int i: ResourceUtil.getEssentialResources()) {
				if (i == resource)
					logger.warning(owner, 120_000L, "Specific Storage is full. Excess " + Math.round(excess * 1_000.0)/1_000.0 + " kg " + name + ".");
			}
			
			// Store excess as stock amount resource
			excess = storeStockAmountResource(resource, excess);	
		}

		s.storedAmount += quantity;

		// Update the specific amount total mass
//		updateSpecificAmountResourceTotalMass();
		specificAmountTotalMass += quantity;
		
		// Fire the unit event type
		owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource); //ResourceUtil.findAmountResource(resource));
		return excess;
	}

	/**
	 * Stores the stock amount resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	public double storeStockAmountResource(int resource, double quantity) {
		AmountStored s = specificAmountStorage.get(resource);
		if (s == null) {
			return quantity;
		}
			
		double remaining =  + s.capacity - s.storedAmount;
		double excess = 0D;
		if (remaining < quantity) {
			// Obtain the excess
			excess = quantity - remaining;
			// Update the quantity
			quantity = remaining;
			
			String name = ResourceUtil.findAmountResourceName(resource);
			for (int i: ResourceUtil.getEssentialResources()) {
				if (i == resource)
					logger.warning(owner, 120_000L, "Stock Storage is full. Excess " + Math.round(excess * 1_000.0)/1_000.0 + " kg " + name + ".");
			}
		}

		s.storedAmount += quantity;

		// Update the stock amount total mass
//		updateStockAmountResourceTotalMass();
		stockAmountTotalMass += quantity;
		
		// Fire the unit event type
		owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource); //ResourceUtil.findAmountResource(resource));
		return excess;
	}
	
	/**
	 * Stores the item resource.
	 *
	 * @param item resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	public int storeItemResource(int resource, int quantity) {
		ItemStored s = itemStorage.get(resource);
		if (s == null) {
			s = new ItemStored();
			s.massPerItem = ItemResourceUtil.findItemResource(resource).getMassPerItem();
			s.totalMass = 0;

			// Save the item resource
			itemStorage.put(resource, s);
		}

		double massPerItem = s.massPerItem;
		double totalMass = s.totalMass;
		
		double rCap = sharedCapacity - stockAmountTotalMass - specificAmountTotalMass - totalMass;
		int itemCap = (int)Math.floor(rCap / massPerItem);
		int excessQ = 0;

		if (itemCap > 0) {
			
			if (quantity > itemCap) {

				s.quantity += itemCap;
				excessQ = quantity - itemCap;
	
				
				logger.warning(owner, "Storing " + itemCap + "x "
						+ ItemResourceUtil.findItemResource(resource).getName()
						+ ", returning the surplus " + excessQ + ".");
			}
			else {
				s.quantity += quantity;
				excessQ = 0;
			}

			s.totalMass = s.quantity * s.massPerItem;

			// Update the item total mass
//			updateItemResourceTotalMass();
			itemTotalMass += s.totalMass;

			// Fire the unit event type
			owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
		}
		else {
			excessQ = quantity;
			
			logger.info(owner, 
					"rCap: " + rCap
					+ " itemCap: " + itemCap
					+ " excessQ: " + excessQ
					+ " quantity: " + quantity
					+ " sharedCapacity: " + sharedCapacity
					+ " stockAmountTotalMass: " + stockAmountTotalMass
					+ " specificAmountTotalMass: " + specificAmountTotalMass
					+ " totalMass: " + totalMass);
			
			logger.warning(owner, "No space to store " + ItemResourceUtil.findItemResource(resource).getName() 
					+ " [quantity: " + quantity + "].");
		}

		return excessQ;
	}

	/**
	 * Recalculates the stock amount resource total mass.
	 */
	private void updateStockAmountResourceTotalMass() {
		// Note: to avoid ConcurrentModificationException, use new ArrayList
		stockAmountTotalMass = stockAmountStorage.values().stream().mapToDouble(r -> r.storedAmount).sum();
	}
	
	/**
	 * Recalculates the specific amount resource total mass.
	 */
	private void updateSpecificAmountResourceTotalMass() {
		// Note: to avoid ConcurrentModificationException, use new ArrayList
		specificAmountTotalMass = specificAmountStorage.values().stream().mapToDouble(r -> r.storedAmount).sum();
	}

	/**
	 * Recalculates the item resource total mass.
	 */
	private void updateItemResourceTotalMass() {
		double result = 0;
		for (int resource: itemStorage.keySet()) {
			int q = itemStorage.get(resource).quantity;
			if (q > 0) {
				ItemResource ir = ItemResourceUtil.findItemResource(resource);
				if (ir != null)
					result += ir.getMassPerItem() * q;
			}
		}

		itemTotalMass = result;
	}

	/**
	 * Retrieves the specific amount resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return shortfall quantity that cannot be retrieved
	 */
	public double retrieveAmountResource(int resource, double quantity) {
		AmountStored s = specificAmountStorage.get(resource);
		if (s == null) {
			return quantity;
		}

		double shortfall = 0D;
		double remaining = s.storedAmount - quantity;

		if (remaining < 0) {
			shortfall = -remaining;
			
			if (shortfall > SMALL_AMOUNT) {
				String name = ResourceUtil.findAmountResourceName(resource);
				logger.warning(owner, 10_000L, "Attempting to retrieve "
						+ Math.round(quantity * 1_000.0)/1_000.0 + " kg "
						+ name + " from specific amount resource storage but lacking " + Math.round(shortfall * 1_000.0)/1_000.0 + " kg.");
				
				// Retrieve shortfall from stock amount resource
				shortfall = retrieveStockAmountResource(resource, shortfall);
			}
			
			remaining = 0;
		}

		
		// Update the stored amount
		s.storedAmount = remaining;

		// Update the specific amount resource total mass
//		updateSpecificAmountResourceTotalMass();
		specificAmountTotalMass -= remaining;
		
		// Fire the unit event type
		owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
		return shortfall;
	}

	/**
	 * Retrieves the stock amount resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return shortfall quantity that cannot be retrieved
	 */
	public double retrieveStockAmountResource(int resource, double quantity) {
		StockAmountStored s = stockAmountStorage.get(resource);
		if (s == null) {
			return quantity;
		}

		double shortfall = 0D;
		double remaining = s.storedAmount - quantity;

		if (remaining < 0) {
			shortfall = -remaining;
			
			if (shortfall > SMALL_AMOUNT) {
				String name = ResourceUtil.findAmountResourceName(resource);
				logger.warning(owner, 10_000L, "Attempting to retrieve "
						+ Math.round(quantity * 1_000.0)/1_000.0 + " kg "
						+ name + " from stock storage but lacking " + Math.round(shortfall * 1_000.0)/1_000.0 + " kg.");
			}
			
			remaining = 0;
		}
		
		// Update the stored amount
		s.storedAmount = remaining;

		// Update the stock amount resource total mass
//		updateStockAmountResourceTotalMass();
		stockAmountTotalMass -= remaining;
		
		// Fire the unit event type
		owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
		return shortfall;
	}
	
	/**
	 * Retrieves the item resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	public int retrieveItemResource(int resource, int quantity) {
		ItemStored s = itemStorage.get(resource);
		if (s == null) {
			return quantity;
		}

		int shortfall = 0;
		int remaining = s.quantity - quantity;

		if (remaining < 0) {
			shortfall = -remaining;
			if (shortfall > 0) {
				String name = ItemResourceUtil.findItemResourceName(resource);
				logger.warning(owner, "Attempting to retrieve " + quantity + "x " + name
					+ " but lacking " + shortfall + "x " + name + ".");
			}
			remaining = 0;
		}

		// Update the quantity
		s.quantity = remaining;

		// Update the total mass
//		updateItemResourceTotalMass();
		itemTotalMass -= remaining * s.massPerItem;

		// Fire the unit event type
		owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
		return shortfall;
	}

	/**
	 * What amount resources are stored ?
	 *
	 * @return
	 */
	public Set<Integer> getResourcesStored() {
		return specificAmountStorage.keySet()
				.stream()
				.filter(i -> (specificAmountStorage.get(i).storedAmount > 0))
				.collect(Collectors.toSet());
	}

	/**
	 * What item resources are stored ?
	 *
	 * @return
	 */
	public Set<Integer> getItemsStored() {
		return itemStorage.keySet()
				.stream()
				.filter(i -> (itemStorage.get(i).quantity > 0))
				.collect(Collectors.toSet());
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	public double getAmountResourceRemainingCapacity(int resource) {
		AmountStored s = specificAmountStorage.get(resource);
		if (s != null) {
			return s.capacity - s.storedAmount;
		}
		return 0;
	}

	/**
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	public boolean hasAmountResourceRemainingCapacity(int resource) {
		AmountStored s = specificAmountStorage.get(resource);
		if (s != null) {
			return s.capacity > s.storedAmount;
		}
		
		return false;
	}
	
	/**
	 * Obtains the remaining storage quantity of a particular item resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	public int getItemResourceRemainingQuantity(int resource) {
		ItemStored s = itemStorage.get(resource);
		if (s != null) {
//			double massPerItem = ItemResourceUtil.findItemResource(resource).getMassPerItem();
//			double totalMass = s.quantity * massPerItem;
			double rCap = sharedCapacity - stockAmountTotalMass - specificAmountTotalMass - s.totalMass;
			return (int)Math.floor(rCap / s.massPerItem);
		}
		return 0;
	}

	/**
	 * Gets the quantity of the amount resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	public double getAmountResourceStored(int resource) {
		AmountStored s = specificAmountStorage.get(resource);
		if (s != null) {
			return s.storedAmount;
		}
		return 0;
	}
	
	/**
	 * Gets the total amount of specific amount resource stored.
	 *
	 * @return total amount
	 */
	public double getTotalSpecificAmountResourceStored() {
		double total = 0;
		for (Map.Entry<Integer, AmountStored> entry : specificAmountStorage.entrySet()) {
			AmountStored s = entry.getValue();
			total += s.storedAmount;
        }
		return total;
	}
	
	/**
	 * Gets the total amount of stock amount resource stored.
	 *
	 * @return total amount
	 */
	public double getTotalStockAmountResourceStored() {
		double total = 0;
		for (Map.Entry<Integer, StockAmountStored> entry : stockAmountStorage.entrySet()) {
			StockAmountStored s = entry.getValue();
			total += s.storedAmount;
        }
		return total;
	}

	/**
	 * Gets the quantity of the item resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	public int getItemResourceStored(int resource) {
		ItemStored s = itemStorage.get(resource);
		if (s != null) {
			return s.quantity;
		}
		return 0;
	}

	/**
	 * Is this amount resource being labeled in storage ?
	 *
	 * @param resource
	 * @return
	 */
	public boolean isResourceSupported(int resource) {
		return specificAmountStorage.containsKey(resource);
	}


	/**
	 * Cleans this container for future use.
	 */
	public void clean() {
		specificAmountStorage.clear();
		itemStorage.clear();
	}
}

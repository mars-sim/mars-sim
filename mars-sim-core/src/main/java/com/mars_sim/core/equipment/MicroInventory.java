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
import java.util.stream.Stream;

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

	static final class AmountStored implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		double capacity = 0;
		double storedAmount = 0;

		// Need to decide how to handle when storedAmount becomes zero.
		// Should it be used for tagging it as a used but empty inventory ?
		
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
		
		// Note: Is there a need for defining the max capacity for storing items/parts ?

		// Need to decide how to handle when quantity becomes zero.
		// Should it be used for tagging it as a used but empty inventory ?
		
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

	private static final double SMALL_AMOUNT = 0.000_001;

	/** The owner of this micro inventory. */
	private Unit owner;
	/** A map of stock amount resources. */
	private Map<Integer, Double> stockAmountStorage = new HashMap<>();
	/** A map of specific amount resources. */
	private Map<Integer, AmountStored> specificAmountStorage = new HashMap<>();
	/** A map of item resources. */
	private Map<Integer, ItemStored> itemStorage = new HashMap<>();

	private double stockAmountTotalMass = 0D;
	private double specificAmountTotalMass = 0D;
	private double itemTotalMass = 0D;
	private double stockCapacity = 0D;

	public MicroInventory(Unit owner) {
		this.owner = owner;
	}

	public MicroInventory(Unit owner, double stockCapacity) {
		this.owner = owner;
		this.stockCapacity = stockCapacity;
	}

	
	/** 
	 * Gets a map of stock amount resources.
	 * 
	 * @return
	 */
	public Map<Integer, Double> getStockAmountStorage() {
		return stockAmountStorage;
	}
	
	/** 
	 * Gets a map of specific amount resources. 
	 * 
	 * @return
	 */
	public Map<Integer, Double> getSpecificAmountStorage() {
		Map<Integer, Double> map = new HashMap<>();
		for (Map.Entry<Integer, AmountStored> entry : specificAmountStorage.entrySet()) {
			int k = entry.getKey();
			AmountStored s = entry.getValue();
			double amount = s.storedAmount;
			map.put(k, amount);
        }
		return map;
	}
	
	/** 
	 * Gets a map of item resources. 
	 * 
	 * @return
	 */
	public Map<Integer, Double> getItemStorage() {
		Map<Integer, Double> map = new HashMap<>();
		for (Map.Entry<Integer, ItemStored> entry : itemStorage.entrySet()) {
			int k = entry.getKey();
			ItemStored s = entry.getValue();
			double q = s.quantity;
			map.put(k, q);
        }
		return map;
	}
	
	/** 
	 * Gets a map of all stock and specific amount resources. 
	 * 
	 * @return
	 */
	public Map<Integer, Double> getAllAmountResourceMap() {
		return Stream.concat(getStockAmountStorage().entrySet().stream(), 
				getSpecificAmountStorage().entrySet().stream())
				.collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                // Handling duplicate keys by adding their values together
                (v1, v2) -> v1 + v2  
        ));
	}
	
	/**
	 * Gets the stock capacity.
	 *
	 * @return
	 */
	public double getStockCapacity() {
		return stockCapacity;
	}

	/**
	 * Sets the stock capacity.
	 *
	 * @return
	 */
	public void setStockCapacity(double stockCapacity) {
		this.stockCapacity = stockCapacity;
	}

	/**
	 * Adds the stock capacity.
	 *
	 * @return
	 */
	public void addStockCapacity(double stockCapacity) {
		this.stockCapacity += stockCapacity;
	}
	
	/**
     * Gets the specific capacity of this amount resource that this container can hold.
     *
     * @return capacity (kg).
     */
    public double getSpecificCapacity(int resource) {
		AmountStored s = specificAmountStorage.get(resource);
		if (s != null) {
			return s.capacity;
		}
		return 0;
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
		updateStockAmountResourceTotalMass();
		updateSpecificAmountResourceTotalMass();
		updateItemResourceTotalMass();
		return Math.round((stockAmountTotalMass + specificAmountTotalMass + itemTotalMass) * 100.0)/100.0;
	}

	/**
	 * Prints the stored mass.
	 *
	 * @return mass [kg]
	 */
	public void printStoredMass() {
		String s = "stockAmountTotalMass: " + stockAmountTotalMass 
				+ " specificAmountTotalMass: " + specificAmountTotalMass 
				+ " itemTotalMass: " + itemTotalMass;
		System.out.println(s);
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
		boolean hasStockResource = stockAmountStorage.containsKey(resource);
		double stockAmount = stockAmountStorage.get(resource);
		return ((s == null && !hasStockResource) 
				|| (s.storedAmount == 0D && stockAmount == 0D));
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
			
		double remaining =  s.capacity - s.storedAmount;
		double excess = 0D;
		if (remaining < quantity) {
			// Obtain the excess
			excess = quantity - remaining;
			// Update the quantity
			quantity = remaining;
			
			String name = ResourceUtil.findAmountResourceName(resource);
			for (int i: ResourceUtil.getEssentialResources()) {
				if (i == resource)
					logger.warning(owner, 60_000L, "Specific Storage is full. Excess " + Math.round(excess * 1_000.0)/1_000.0 + " kg " + name + ".");
			}
			
			// Store excess as stock amount resource
			excess = storeStockAmountResource(resource, excess);

		}

		s.storedAmount += quantity;

		// Update the specific amount total mass
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
		double stockAmount = 0;
		
		if (stockAmountStorage.containsKey(resource)) {
			// Gets the existing stock amount
			stockAmount = stockAmountStorage.get(resource);
		}

		double remaining = stockAmountTotalMass - stockAmount;
		
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
		// Increase the existing stock amount by adding quantity
		stockAmount += quantity;
		// Update the stock storage map with the new stock amount
		stockAmountStorage.put(resource, stockAmount);
		// Update the stock amount total mass
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
		
		double rCap = getRemainingStockCapacity();
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
					+ " sharedCapacity: " + stockCapacity
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
	 * 
	 * NOTE: Do NOT delete. Will need it in future.
	 */
	private void updateStockAmountResourceTotalMass() {
		// Note: to avoid ConcurrentModificationException, use new ArrayList
		stockAmountTotalMass = stockAmountStorage.values().stream().mapToDouble(d -> d).sum();
	}
	
	/**
	 * Recalculates the specific amount resource total mass.
	 * 
	 * NOTE: Do NOT delete. Will need it in future.
	 */
	private void updateSpecificAmountResourceTotalMass() {
		// Note: to avoid ConcurrentModificationException, use new ArrayList
		specificAmountTotalMass = specificAmountStorage.values().stream().mapToDouble(r -> r.storedAmount).sum();
	}

	/**
	 * Recalculates the item resource total mass.
	 * 
	 * NOTE: Do NOT delete. Will need it in future.
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
			
			// Update the specific amount resource total mass
			specificAmountTotalMass -= s.storedAmount;
		}
		else {
			// Update the specific amount resource total mass
			specificAmountTotalMass -= quantity;
		}
	
		// Update the stored amount
		s.storedAmount = remaining;
	
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
		double stockAmount = 0;
		
		if (stockAmountStorage.containsKey(resource)) {
			// Gets the existing stock amount
			stockAmount = stockAmountStorage.get(resource);
		}

		double shortfall = 0D;
		double remaining = stockAmount - quantity;

		if (remaining < 0) {
			shortfall = -remaining;
			
			if (shortfall > SMALL_AMOUNT) {
				String name = ResourceUtil.findAmountResourceName(resource);
				logger.warning(owner, 10_000L, "Attempting to retrieve "
						+ Math.round(quantity * 1_000.0)/1_000.0 + " kg "
						+ name + " from stock storage but lacking " + Math.round(shortfall * 1_000.0)/1_000.0 + " kg.");
			}
			// Update the remaining
			remaining = 0;
			// Reduce the stock amount resource total mass by stockAmount
			stockAmountTotalMass -= stockAmount;
		}
		else {
			// Update the existing stock amount resource total mass
			stockAmountTotalMass -= quantity;
		}

		// Update the stock storage map with remaining
		stockAmountStorage.put(resource, remaining);
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
		int remainingQ = s.quantity - quantity;

		if (remainingQ < 0) {
			shortfall = -remainingQ;
			if (shortfall > 0) {
				String name = ItemResourceUtil.findItemResourceName(resource);
				logger.warning(owner, 10_000L, "Attempting to retrieve " + quantity + "x " + name
					+ " but lacking " + shortfall + "x " + name + ".");
			}
			remainingQ = 0;
			
			// Update the total mass
			itemTotalMass -= s.quantity * s.massPerItem;
		}
		else {
			// Update the total mass
			itemTotalMass -= quantity * s.massPerItem;
		}
		
		// Update the quantity
		s.quantity = remainingQ;

		// Fire the unit event type
		owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
		return shortfall;
	}

	
	/**
	 * Gets a set of IDs of the specific amount resources being stored.
	 * Ignore any IDs that are zero amount.
	 *
	 * @return
	 */
	public Set<Integer> getAllSpecificResourceStoredIDs() {
		Set<Integer> set = specificAmountStorage.keySet()
				.stream()
				.filter(i -> (specificAmountStorage.get(i).storedAmount > 0))
				.collect(Collectors.toSet());
		
		set.addAll(stockAmountStorage.keySet());
		
		return set;
	}
	
	/**
	 * Gets a set of IDs of the specific amount resources being stored.
	 * Ignore any IDs that are zero amount.
	 *
	 * @return
	 */
	public Set<Integer> getSpecificResourceStoredIDs() {
		return specificAmountStorage.keySet()
				.stream()
				.filter(i -> (specificAmountStorage.get(i).storedAmount > 0))
				.collect(Collectors.toSet());
	}

	/**
	 * Gets a set of IDs of the specific items being stored.
	 * Ignore any IDs that are zero amount.
	 * 
	 * @return
	 */
	public Set<Integer> getItemStoredIDs() {
		return itemStorage.keySet()
				.stream()
				.filter(i -> (itemStorage.get(i).quantity > 0))
				.collect(Collectors.toSet());
	}

	/**
	 * Obtains the combined capacity of remaining storage space for a particular amount resource.
     * @apiNote This includes the stock capacity
	 *
	 * @param resource
	 * @return quantity
	 */
	public double getRemainingCombinedCapacity(int resource) {
		return getRemainingStockCapacity() + getRemainingSpecificCapacity(resource);
	}

	/**
	 * Obtains the remaining specific capacity storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	public double getRemainingSpecificCapacity(int resource) {
		AmountStored s = specificAmountStorage.get(resource);
		if (s != null) {
			return s.capacity - s.storedAmount;
		}
		return 0;
	}
	
	/**
	 * Obtains the remaining stock capacity storage space.
	 *
	 * @param resource
	 * @return quantity
	 */
	public double getRemainingStockCapacity() {
		return stockCapacity - getStoredMass();
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
	 * Obtains the remaining quantity (in integer) of an item resource that this inventory can store.
	 *
	 * @param resource
	 * @return quantity
	 */
	public int getItemResourceRemainingQuantity(int resource) {
		ItemStored s = itemStorage.get(resource);
		double rCap = getRemainingStockCapacity();
		if (s != null) {
			// Question : does it have to be tagged or defined ahead of time for storing items or parts ?
			return (int)Math.floor(rCap / s.massPerItem);
		}
		else {
			double massPerItem = ItemResourceUtil.findItemResource(resource).getMassPerItem();
			return (int)Math.floor(rCap / massPerItem);	
		}
	}

	/**
	 * Gets the quantity of the specific amount resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	public double getSpecificAmountResourceStored(int resource) {
		AmountStored s = specificAmountStorage.get(resource);
		if (s != null) {
			return s.storedAmount;
		}
		return 0;
	}
	
	/**
	 * Gets the quantity of the stock amount resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	public double getStockAmountResourceStored(int resource) {
		return stockAmountStorage.getOrDefault(resource, 0.0);
	}
	
	/**
	 * Gets the quantity of all stock and specific amount resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	public double getAllAmountResourceStored(int resource) {
		return getStockAmountResourceStored(resource) + getSpecificAmountResourceStored(resource);
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
		updateStockAmountResourceTotalMass();
		return this.stockAmountTotalMass;
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

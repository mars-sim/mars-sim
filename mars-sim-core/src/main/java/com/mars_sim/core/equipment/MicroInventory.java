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
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.mars_sim.core.Unit;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;

/**
 * The MicroInventory class represents a simple resource storage solution.
 */
public class MicroInventory implements ItemHolder, ResourceHolder, Serializable {

	private static final class AmountStored implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		double capacity = 0;
		double storedAmount = 0;
		double overload = 0;

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

		private void adjustStoredAmount(double quantity) {
			storedAmount += quantity;
			overload = Math.max(0, storedAmount - capacity);
		}
	}

	private static final class ItemStored implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		int quantity = 0;
		double massPerItem = 0;
		double totalMass = 0;
		
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

	/** The owner of this micro inventory. */
	private Unit owner;
	/** A map of specific amount resources. */
	private Map<Integer, AmountStored> amountStorage = new HashMap<>();
	/** A map of item resources. */
	private Map<Integer, ItemStored> itemStorage = new HashMap<>();

	private double amountStockCapacity = 0D;
	private double amountStockAvailable = 0D;
	private double amountTotalMass = 0D;
	private double itemTotalMass = 0D;
	private double totalCapcity = 0D;

	/**
	 * Constructs a new MicroInventory with the specified owner and total capacity.
	 * @param owner Owner of the micro inventory.
	 * @param totalCapacity Total capacity of the micro inventory.
	 * @param amountStockCapacity The stock capacity (overload) for specific amount resources.
	 */
	public MicroInventory(Unit owner, double totalCapacity, double amountStockCapacity) {
		this(owner, totalCapacity, amountStockCapacity, null);
	}

	/**
	 * Constructs a new MicroInventory with the specified owner, total capacity, and resource capacities.
	 * @param owner Owner of the micro inventory.
	 * @param totalCapacity Total capacity of the micro inventory.
	 * @param amountStockCapacity The stock capacity (overload) for specific amount resources.
	 * @param amountCapacities Map of specific amount resource capacities.
	 */
	public MicroInventory(Unit owner, double totalCapacity, double amountStockCapacity, Map<Integer, Double> amountCapacities) {
		this.owner = owner;
		this.totalCapcity = totalCapacity;
		this.amountStockCapacity = amountStockCapacity;
		this.amountStockAvailable = amountStockCapacity;

		if (amountCapacities != null) {
			setResourceCapacityMap(amountCapacities, false);
		}
	}

	/**
	 * Get the owning Unit of this inventory.
	 */
	public Unit getOwner() {
		return owner;
	}

	@Override
	public double getCargoCapacity() {
		return totalCapcity;
	}

	/**
	 * Adds the stock capacity.
	 *
	 * @return
	 */
	public void addTotalCapacity(double extraCapacity) {
		this.totalCapcity += extraCapacity;
	}

	/**
	 * Sets the resource capacities.
	 * 
	 * @param capacities
	 * @param add True if it should these be "added" on top of its existing capacity. False if it should be 'set' to a new capacity
	 */
	public void setResourceCapacityMap(Map<Integer, Double> capacities, boolean toAdd) {
		for (Entry<Integer, Double> v : capacities.entrySet()) {
			Integer resource = v.getKey();
			Double capacity = v.getValue();
			AmountStored s = amountStorage.get(resource);
			if (s != null) {
				if (toAdd) {
					s.capacity += capacity;
				}
				else {
					// To set to a new capacity
					s.capacity = capacity;
				}
			}
			else {
				amountStorage.put(resource, new AmountStored(capacity));
			}
		}
	}

	/**
	 * Removes the capacity of an amount resource.
	 *
	 * @param resource
	 * @param capacity
	 */
	public void removeSpecificCapacity(int resource, double capacity) {
		AmountStored s = amountStorage.get(resource);

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
		return amountTotalMass + itemTotalMass;
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
		AmountStored s = amountStorage.get(resource);
		return (s == null || s.storedAmount == 0D);
	}

	/**
	 * Stores the specific amount resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	@Override
	public double storeAmountResource(int resource, double quantity) {
		AmountStored s = amountStorage.get(resource);
		if (s == null) {
			return quantity;
		}
			
		double remaining =  (s.capacity - s.storedAmount) + amountStockAvailable;
		double excess = 0D;
		if (remaining < quantity) {
			// Obtain the excess
			excess = quantity - remaining;
			// Update the quantity
			quantity = remaining;
			
			if (ResourceUtil.getEssentialResources().contains(resource)) {
				String name = ResourceUtil.findAmountResourceName(resource);
				logger.warning(owner, 60_000L, "Storage is full. Excess " + Math.round(excess * 1_000.0)/1_000.0 + " kg " + name + ".");
			}
		}

		s.adjustStoredAmount(quantity);

		// Update the specific amount total mass
		refreshAmountTotals();
		
		// Fire the unit event type
		owner.fireUnitUpdate(EntityEventType.INVENTORY_RESOURCE_EVENT, resource);
		return excess;
	}

	
	/**
	 * Stores the item resource.
	 *
	 * @param item resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	@Override
	public int storeItemResource(int resource, int quantity) {
		ItemStored s = itemStorage.computeIfAbsent(resource, k -> {
			var is = new ItemStored();
			is.massPerItem = ItemResourceUtil.findItemResource(k).getMassPerItem();
			is.totalMass = 0;
			return is;
		});

		double massPerItem = s.massPerItem;
		double totalMass = s.totalMass;
		
		double rCap = getRemainingTotalCapacity();
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
			owner.fireUnitUpdate(EntityEventType.INVENTORY_RESOURCE_EVENT, resource);
		}
		else {
			excessQ = quantity;
			
			logger.info(owner, 
					"rCap: " + rCap
					+ " itemCap: " + itemCap
					+ " excessQ: " + excessQ
					+ " quantity: " + quantity
					+ " sharedCapacity: " + totalCapcity
					+ " itemStored: " + totalMass);
			
			logger.warning(owner, "No space to store " + ItemResourceUtil.findItemResource(resource).getName() 
					+ " [quantity: " + quantity + "].");
		}

		return excessQ;
	}
	
	/**
	 * Retrieves the specific amount resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return shortfall quantity that cannot be retrieved
	 */
	@Override
	public double retrieveAmountResource(int resource, double quantity) {
		AmountStored s = amountStorage.get(resource);
		if (s == null) {
			return quantity;
		}

		double shortfall = 0D;
		if (quantity > s.storedAmount) {
			shortfall = quantity - s.storedAmount;

			String name = ResourceUtil.findAmountResourceName(resource);
			logger.warning(owner, 10_000L, "Attempting to retrieve "
					+ Math.round(quantity * 1_000.0)/1_000.0 + " kg "
					+ name + " from specific amount resource storage but lacking " + Math.round(shortfall * 1_000.0)/1_000.0 + " kg.");			
			quantity = s.storedAmount;
		}
	
		// Update the stored amount; reversed so a negative decrease in stored
		s.adjustStoredAmount(shortfall - quantity);
		refreshAmountTotals();

		// Fire the unit event type
		owner.fireUnitUpdate(EntityEventType.INVENTORY_RESOURCE_EVENT, resource);
		return shortfall;
	}

	private static class AmountVisitor implements Consumer<AmountStored> {
		double total = 0D;
		double stockUsed = 0D;
		
		@Override
		public void accept(AmountStored s) {
			total += s.storedAmount;

			stockUsed += s.overload;
		}
	}

	private void refreshAmountTotals() {
	
		var visitor = new AmountVisitor();

		// Recalculate the specific amount total mass to avoid drift between the actual stored amounts
		// Use of Double precision arithmetic which may introduce small errors over multiple operations.
		amountStorage.values().forEach(visitor);

		amountTotalMass = visitor.total;
		amountStockAvailable = amountStockCapacity - visitor.stockUsed;
	}

	/**
	 * Retrieves the item resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	@Override
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
		owner.fireUnitUpdate(EntityEventType.INVENTORY_RESOURCE_EVENT, resource);
		return shortfall;
	}

	
	/**
	 * Gets a set of IDs of the specific amount resources being stored.
	 * Ignore any IDs that are zero amount.
	 *
	 * @return
	 */
	@Override
	public Set<Integer> getAllAmountResourceStoredIDs() {
		return getSpecificResourceStoredIDs();
	}
	
	/**
	 * Gets a set of IDs of the specific amount resources being stored.
	 * Ignore any IDs that are zero amount.
	 *
	 * @return
	 */
	@Override 
	public Set<Integer> getSpecificResourceStoredIDs() {
		return amountStorage.entrySet()
				.stream()
				.filter(i -> (i.getValue().storedAmount > 0))
				.map(Entry::getKey)
				.collect(Collectors.toSet());
	}

	/**
	 * Gets a set of IDs of the specific items being stored.
	 * Ignore any IDs that are zero amount.
	 * 
	 * @return
	 */
	@Override
	public Set<Integer> getItemResourceIDs() {
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
	@Override
	public double getRemainingCombinedCapacity(int resource) {
		return getRemainingTotalCapacity() + getRemainingSpecificCapacity(resource);
	}

	/**
	 * Obtains the remaining specific capacity storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getRemainingSpecificCapacity(int resource) {
		AmountStored s = amountStorage.get(resource);
		if (s != null) {
			// Account for overload resoruces which have an effective increased capacity equals to the overload
			return ((s.capacity + s.overload) - s.storedAmount) + amountStockAvailable;
		}
		return 0;
	}
		
	/**
     * Gets the specific capacity of this amount resource that this container can hold.
     *
     * @return capacity (kg).
     */
	@Override
    public double getSpecificCapacity(int resource) {
		AmountStored s = amountStorage.get(resource);
		if (s != null) {
			return s.capacity + amountStockAvailable;
		}
		return 0;
    }

	/**
	 * Obtains the remaining total capacity storage space.
	 *
	 * @return quantity
	 */
	private double getRemainingTotalCapacity() {
		return totalCapcity - getStoredMass();
	}

	/**
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasAmountResourceRemainingCapacity(int resource) {
		return getRemainingSpecificCapacity(resource) > 0;
	}
	
	/**
	 * Obtains the remaining quantity (in integer) of an item resource that this inventory can store.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public int getItemResourceRemainingQuantity(int resource) {
		ItemStored s = itemStorage.get(resource);
		double massPerItem = (s != null ? s.massPerItem
				: ItemResourceUtil.findItemResource(resource).getMassPerItem());	
		return (int)Math.floor(getRemainingTotalCapacity() / massPerItem);	
	}

	/**
	 * Gets the quantity of the specific amount resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getSpecificAmountResourceStored(int resource) {
		AmountStored s = amountStorage.get(resource);
		if (s != null) {
			return s.storedAmount;
		}
		return 0;
	}
	
	/**
	 * Gets the quantity of all stock and specific amount resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAllAmountResourceStored(int resource) {
		return  getSpecificAmountResourceStored(resource);
	}

	/**
	 * Gets the quantity of the item resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
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
		return amountStorage.containsKey(resource);
	}
}
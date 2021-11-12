/*
 * Mars Simulation Project
 * MicroInventory.java
 * @date 2021-10-21
 * @author Manny Kung
 */
package org.mars_sim.msp.core.equipment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;

/**
 * The MicroInventory class represents a simple resource storage solution.
 */
public class MicroInventory implements Serializable {

	static final class ResourceStored implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		double capacity = 0;
		double storedAmount = 0;

		ResourceStored(double capacity) {
			super();
			this.capacity = capacity;
		}

		@Override
		public String toString() {
			return "ResourceStored [capacity=" + capacity
					+ ", storedAmount=" + storedAmount
					+ "]";
		}
	}

	static final class ItemStored implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		int quantity = 0;

		ItemStored() {
			super();
		}

		@Override
		public String toString() {
			return "ItemStored quantity=" + quantity
					+ "]";
		}
	}

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(MicroInventory.class.getName());

	private static final double SMALL_AMOUNT = 0.001;

	/** The owner of this micro inventory. */
	private Unit owner;
	/** A map of amount resources. */
	private Map<Integer, ResourceStored> amountStorage = new HashMap<>();
	/** A map of item resources. */
	private Map<Integer, ItemStored> itemStorage = new HashMap<>();

	private double amountTotalMass = 0D;
	private double itemTotalMass = 0D;
	private double generalTotalMass = 0D;

	private double sharedCapacity = 0D;

	public MicroInventory(Unit owner) {
		this.owner = owner;
	}

	public MicroInventory(Unit owner, double sharedCapacity) {
		this.owner = owner;
		this.sharedCapacity = sharedCapacity;
	}

	/**
	 * Gets the shared/general/stock capacity
	 *
	 * @return
	 */
	public double getSharedCapacity() {
		return sharedCapacity;
	}

	/**
	 * Sets the shared/general/stock capacity
	 *
	 * @return
	 */
	public void setSharedCapacity(double capacity) {
		sharedCapacity = capacity;
	}

	/**
     * Gets the capacity of this amount resource that this container can hold.
     *
     * @return capacity (kg).
     */
    public double getCapacity(int resource) {
		ResourceStored s = amountStorage.get(resource);
		if (s != null) {
			return s.capacity;
		}
		return 0;
    }

	/**
	 * Sets the capacity of an amount resource
	 *
	 * @param resource
	 * @param capacity
	 */
	public void setCapacity(int resource, double capacity) {
		ResourceStored s = amountStorage.get(resource);
		if (s != null) {
			s.capacity = capacity;
		}
		else {
			amountStorage.put(resource, new ResourceStored(capacity));
		}
	}

	/**
	 * Adds the capacity of an amount resource
	 *
	 * @param resource
	 * @param capacity
	 */
	public void addCapacity(int resource, double capacity) {
		ResourceStored s = amountStorage.get(resource);
		if (s != null) {
			s.capacity += capacity;
		}
		else {
			amountStorage.put(resource, new ResourceStored(capacity));
		}
	}

	/**
	 * Removes the capacity of an amount resource
	 *
	 * @param resource
	 * @param capacity
	 */
	public void removeCapacity(int resource, double capacity) {
		ResourceStored s = amountStorage.get(resource);

		if (s != null) {
			s.capacity -= capacity;
			if (s.capacity < 0D) {
				s.capacity = 0D;
			}
		}
	}

	/**
	 * Gets the total weight of the stored resources
	 *
	 * @return mass [kg]
	 */
	public double getStoredMass() {
		return amountTotalMass + itemTotalMass + generalTotalMass;
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
		ResourceStored s = amountStorage.get(resource);
		return (s == null) || (s.storedAmount == 0D);
	}

	/**
	 * Stores the amount resource
	 *
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	public double storeAmountResource(int resource, double quantity) {
		ResourceStored s = amountStorage.get(resource);
		if (s == null) {
			return quantity;
		}
		double remaining = s.capacity - s.storedAmount;
		double excess = 0D;
		if (remaining < quantity) {
			excess = quantity - remaining;
			quantity = remaining;
			// This generates warnings during collect ice & regolith because the Task do not check the capacity before digging
//			String name = ResourceUtil.findAmountResourceName(resource);
//			logger.warning(owner, "Storage is full. Excess " + Math.round(excess * 1_000.0)/1_000.0 + " kg " + name + ".");
		}

		s.storedAmount += quantity;

		// Update the amount total mass
		updateAmountResourceTotalMass();
		// Fire the unit event type
		owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource); //ResourceUtil.findAmountResource(resource));
		return excess;
	}

	/**
	 * Stores the item resource
	 *
	 * @param item resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	public int storeItemResource(int resource, int quantity) {
		ItemStored s = itemStorage.get(resource);
		if (s == null) {
			s = new ItemStored();
		}

		Part partSpec = ItemResourceUtil.findItemResource(resource);
		double massPerItem = partSpec.getMassPerItem();
		double totalMass = s.quantity * massPerItem;
		double rCap = sharedCapacity - totalMass;
		int itemCap = (int)Math.floor(rCap / massPerItem);

		int missing = 0;

		if (itemCap > 0) {

			if (quantity > itemCap) {
				s.quantity += itemCap;
				missing = quantity - itemCap;
				logger.warning(owner, "Storing " + itemCap + "x "
						+ partSpec.getName()
						+ ", returning the surplus " + missing + ".");
			}
			else {
				s.quantity += quantity;
				missing = 0;
			}

			// Save the item resource
			itemStorage.put(resource, s);

			// Update the item total mass
			updateItemResourceTotalMass();

			// Fire the unit event type
			owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
		}
		else {
			missing = quantity;
			logger.warning(owner, "No space to store " + quantity + "x "
								+ partSpec.getName() + ".");
		}

		return missing;
	}

	/**
	 * Recalculate the amount resource total mass.
	 */
	private void updateAmountResourceTotalMass() {
		// Note: to avoid ConcurrentModificationException, use new ArrayList
		amountTotalMass = amountStorage.values().stream().mapToDouble(r -> r.storedAmount).sum();
	}

	/**
	 * Recalculate the item resource total mass.
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
	 * Retrieves the resource
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	public double retrieveAmountResource(int resource, double quantity) {
		ResourceStored s = amountStorage.get(resource);
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
						+ name + " but lacking " + Math.round(shortfall * 1_000.0)/1_000.0 + " kg.");
			}
			remaining = 0;
		}

		// Update the stored amount
		s.storedAmount = remaining;

		// Remove this 'general' resource since its capacity is not set
//		if (s.storedAmount == 0 && s.capacity == 0) {
//			storageMap.remove(resource);
//		}

		// Update the total mass
		updateAmountResourceTotalMass();

		// Fire the unit event type
		owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
		return shortfall;
	}

	/**
	 * Retrieves the item resource
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

		// Remove this 'general' resource since its capacity is not set
//		if (s.quantity == 0 && s.capacity == 0) {
//			storageMap.remove(resource);
//		}

		// Update the total mass
		updateItemResourceTotalMass();

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
		return amountStorage.keySet();
//				.stream()
//				.filter(i -> (i.intValue() < ResourceUtil.FIRST_ITEM_RESOURCE_ID))
//				.collect(Collectors.toSet());
	}

	/**
	 * What item resources are stored ?
	 *
	 * @return
	 */
	public Set<Integer> getItemsStored() {
		return itemStorage.keySet();
//				.stream()
//				.filter(i -> (i.intValue() >= ResourceUtil.FIRST_ITEM_RESOURCE_ID))
//				.collect(Collectors.toSet());
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource
	 *
	 * @param resource
	 * @return quantity
	 */
	public double getAmountResourceRemainingCapacity(int resource) {
		ResourceStored s = amountStorage.get(resource);
		if (s != null) {
			return s.capacity - s.storedAmount;
		}
		return 0;
	}

	/**
	 * Obtains the remaining storage quantity of a particular item resource
	 *
	 * @param resource
	 * @return quantity
	 */
	public int getItemResourceRemainingQuantity(int resource) {
		ItemStored s = itemStorage.get(resource);
		if (s != null) {
			double massPerItem = ItemResourceUtil.findItemResource(resource).getMassPerItem();
			double totalMass = s.quantity * massPerItem;
			double rCap = sharedCapacity - totalMass;
			return (int)Math.floor(rCap / massPerItem);
		}
		return 0;
	}

	/**
	 * Gets the quantity of the amount resource stored
	 *
	 * @param resource
	 * @return quantity
	 */
	public double getAmountResourceStored(int resource) {
		ResourceStored s = amountStorage.get(resource);
		if (s != null) {
			return s.storedAmount;
		}
		return 0;
	}

	/**
	 * Gets the quantity of the item resource stored
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
	 * Does this amount resource exist in storage ?
	 *
	 * @param resource
	 * @return
	 */
	public boolean isResourceSupported(int resource) {
		return amountStorage.containsKey(resource);
	}


	/**
	 * Clean this container for future use
	 */
	public void clean() {
		amountStorage.clear();
		itemStorage.clear();
	}
}

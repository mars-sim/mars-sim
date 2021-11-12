/*
 * Mars Simulation Project
 * MicroInventory.java
 * @date 2021-10-21
 * @author Manny Kung
 */
package org.mars_sim.msp.core.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
		int quantity = 0;

		ResourceStored(double capacity) {
			super();
			this.capacity = capacity;
		}

		@Override
		public String toString() {
			return "ResourceStored [capacity=" + capacity + ", storedAmount=" + storedAmount + ", quantity=" + quantity
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
	/** A map of resources. */
	private Map<Integer, ResourceStored> storageMap = new HashMap<>();

	private double totalMass = 0D;

	public MicroInventory(Unit owner) {
		this.owner = owner;
	}

	/**
     * Gets the capacity of this resource that this container can hold.
     *
     * @return capacity (kg).
     */
    public double getCapacity(int resource) {
		ResourceStored s = storageMap.get(resource);
		if (s != null) {
			return s.capacity;
		}
		return 0;
    }

	/**
	 * Sets the capacity of a particular resource
	 *
	 * @param resource
	 * @param capacity
	 */
	public void setCapacity(int resource, double capacity) {
		ResourceStored s = storageMap.get(resource);
		if (s != null) {
			s.capacity = capacity;
		}
		else {
			storageMap.put(resource, new ResourceStored(capacity));
		}
	}

	/**
	 * Adds the capacity of a particular resource
	 *
	 * @param resource
	 * @param capacity
	 */
	public void addCapacity(int resource, double capacity) {
		ResourceStored s = storageMap.get(resource);
		if (s != null) {
			s.capacity += capacity;
		}
		else {
			storageMap.put(resource, new ResourceStored(capacity));
		}
	}

	/**
	 * Removes the capacity of a particular resource
	 *
	 * @param resource
	 * @param capacity
	 */
	public void removeCapacity(int resource, double capacity) {
		ResourceStored s = storageMap.get(resource);

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
		return totalMass;
	}

	/**
	 * Is this suit empty ?
	 *
	 * @return
	 */
	public boolean isEmpty() {
		return totalMass == 0D;
	}

	/**
	 * Is this suit empty of this resource ?
	 *
	 * @param resource
	 * @return
	 */
	public boolean isEmpty(int resource) {
		ResourceStored s = storageMap.get(resource);
		return (s == null) || (s.storedAmount == 0D);
	}

	/**
	 * Stores the resource
	 *
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	public double storeAmountResource(int resource, double quantity) {
		ResourceStored s = storageMap.get(resource);
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
		ResourceStored s = storageMap.get(resource);
		if (s == null) {
			return quantity;
		}

		Part partSpec = ItemResourceUtil.findItemResource(resource);
		double massPerItem = partSpec.getMassPerItem();
		double totalMass = s.quantity * massPerItem;
		double rCap = s.capacity - totalMass;
		int itemCap = (int)(rCap / massPerItem);
		int missing = 0;

		if (itemCap > 0) {

			if (quantity > itemCap) {
				s.quantity += itemCap;
				missing = quantity - itemCap;
				logger.warning(owner, "Can only store " + quantity + "@"
						+ partSpec.getName()
						+ " and return the surplus " + missing + ".");
			}
			else {
				s.quantity += quantity;
				missing = 0;
			}

			// Update the total mass
			updateItemResourceTotalMass();

			// Fire the unit event type
			owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
		}
		else {
			missing = quantity;
			logger.warning(owner, "No space to store " + quantity + "@"
								+ partSpec.getName() + ".");
		}

		return missing;
	}

	/**
	 * Recalculate the amount resource total mass.
	 */
	private void updateAmountResourceTotalMass() {
		// Note: to avoid ConcurrentModificationException, use new ArrayList
		totalMass = storageMap.values().stream().mapToDouble(r -> r.storedAmount).sum();
	}

	/**
	 * Recalculate the item resource total mass.
	 */
	private void updateItemResourceTotalMass() {
		double result = 0;
		for (int resource: storageMap.keySet()) {
			int q = storageMap.get(resource).quantity;
			if (q > 0) {
				ItemResource ir = ItemResourceUtil.findItemResource(resource);
				if (ir != null)
					result += ir.getMassPerItem() * q;
			}
		}

		totalMass = result;
	}

	/**
	 * Retrieves the resource
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	public double retrieveAmountResource(int resource, double quantity) {
		ResourceStored s = storageMap.get(resource);
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
		ResourceStored s = storageMap.get(resource);
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
		return storageMap.keySet().stream()
				.filter(i -> (i.intValue() < ResourceUtil.FIRST_ITEM_RESOURCE_ID))
				.collect(Collectors.toSet());
	}

	/**
	 * What item resources are stored ?
	 *
	 * @return
	 */
	public Set<Integer> getItemResourceIDs() {
		return storageMap.keySet().stream()
				.filter(i -> (i.intValue() >= ResourceUtil.FIRST_ITEM_RESOURCE_ID))
				.collect(Collectors.toSet());
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource
	 *
	 * @param resource
	 * @return quantity
	 */
	public double getAmountResourceRemainingCapacity(int resource) {
		ResourceStored s = storageMap.get(resource);
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
	public int getItemResourceRemainingCapacity(int resource) {
		ResourceStored s = storageMap.get(resource);
		if (s != null) {
			double massPerItem = ItemResourceUtil.findItemResource(resource).getMassPerItem();
			double totalMass = s.quantity * massPerItem;
			double rCap = s.capacity - totalMass;
			return (int)(rCap / massPerItem);
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
		ResourceStored s = storageMap.get(resource);
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
		ResourceStored s = storageMap.get(resource);
		if (s != null) {
			return s.quantity;
		}
		return 0;
	}

	/**
	 * Does this resource exist in storage ?
	 *
	 * @param resource
	 * @return
	 */
	public boolean isResourceSupported(int resource) {
		return storageMap.containsKey(resource);
	}


	/**
	 * Clean this container for future use
	 */
	public void clean() {
		storageMap.clear();
	}
}

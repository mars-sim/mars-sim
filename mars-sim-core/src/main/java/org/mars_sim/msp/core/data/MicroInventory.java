/*
 * Mars Simulation Project
 * MicroInventory.java
 * @date 2021-10-12
 * @author Manny Kung
 */
package org.mars_sim.msp.core.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.mars_sim.msp.core.logging.Loggable;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;

/**
 * The MicroInventory class represents a simple resource storage solution.
 */
public class MicroInventory implements Serializable {

	static final class ResourceStored implements Serializable {
		/** default serial id. */
		private static final long serialVersionUID = 1L;
		double capacity;
		double storedAmount = 0;
		int quantity = 0;
		
		ResourceStored(double capacity) {
			super();
			this.capacity = capacity;
		}
	}
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(MicroInventory.class.getName());

	/** The owner of this micro inventory. */
	private Loggable owner;
	/** A map of resources. */
	private Map<Integer, ResourceStored> storageMap = new HashMap<>();
	
	private double totalMass = 0D;
	
	public MicroInventory(Loggable owner) {
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
		storageMap.put(resource, new ResourceStored(capacity));
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
			// THis generates warnings during collect ice & regolith because the Task do not check the capacity before digging
//			String name = ResourceUtil.findAmountResourceName(resource);
//			logger.warning(owner, "Storage is full. Excess " + Math.round(excess * 1_000.0)/1_000.0 + " kg " + name + ".");
		}

		s.storedAmount += quantity;

		updateAmountResourceTotalMass();
		
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
		
		double massPerItem = ItemResourceUtil.findItemResource(resource).getMassPerItem();
		double totalMass = s.quantity * massPerItem;
		double rCap = s.capacity - totalMass;
		int num = (int)(rCap / massPerItem);
		int missing = 0;

		if (num > 0) {
		
			if (quantity != num) {
				s.quantity = num;
				missing = quantity - num;
				String name = ItemResourceUtil.findItemResourceName(resource);
				logger.warning(owner, "Can only retrive " + quantity + "x " + name 
					+ " and return the excess " + missing + "x " + name + ".");
			}
			
			else {
				s.quantity = num;
				missing = 0;	
			}
		}
		
		else {
			missing = quantity;
			String name = ItemResourceUtil.findItemResourceName(resource);
			logger.warning(owner, "Cannot store " + quantity + " " + name + ".");
		}

		// Update the total mass
		updateItemResourceTotalMass();
		return missing;
	}
	
	/**
	 * Recalculate the amount resource total mass.
	 */
	private void updateAmountResourceTotalMass() {
		totalMass = storageMap.values().stream().mapToDouble(r -> r.storedAmount).sum();	
	}

	/**
	 * Recalculate the item resource total mass.
	 */
	private void updateItemResourceTotalMass() {
		double result = 0;
		for (int resource: storageMap.keySet()) {
			int q = storageMap.get(resource).quantity;
			result += ItemResourceUtil.findItemResource(resource).getMassPerItem() * q;
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
			String name = ResourceUtil.findAmountResourceName(resource);
			logger.warning(owner, 10_000L, "Just retrieved all " + quantity + " kg of " 
					+ name + " but lacked " + Math.round(-remaining * 10.0)/10.0 + " kg.");
			shortfall = -remaining;
			remaining = 0;
		}

		s.storedAmount = remaining;
		// Update the total mass
		updateAmountResourceTotalMass();
		return shortfall;
	}
	
	/**
	 * Retrieves the item resource 
	 * 
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	public double retrieveItemResource(int resource, int quantity) {
		ResourceStored s = storageMap.get(resource);
		if (s == null) {
			return quantity;
		}
		
		int shortfall = 0;
		
		if (quantity > s.quantity) {
			shortfall = quantity - s.quantity;
			String name = ItemResourceUtil.findItemResourceName(resource);
			logger.warning(owner, "Can only retrive " + quantity + "x " + name 
				+ " and return the shortfall " + shortfall + "x " + name + ".");
		}
		else {
			s.quantity = s.quantity - quantity;
		}
		
		// Update the total mass
		updateItemResourceTotalMass();
		return shortfall;
	}
	
	/**
	 * What resources are stored ?
	 * 
	 * @return
	 */
	public Set<Integer> getResourcesStored() {
		return Collections.unmodifiableSet(storageMap.keySet());
	}
	
	/**
	 * Gets all stored amount resources
	 * 
	 * @return
	 */
	public Set<AmountResource> getAllAmountResourcesStored() {
		return storageMap.keySet().stream()
				.map(ar -> ResourceUtil.findAmountResource(ar))
				.filter(Objects::nonNull)
				.collect(java.util.stream.Collectors.toSet());
	}
	
	/**
	 * Gets all stored item resources 
	 * 
	 * @return
	 */
	public Set<ItemResource> getAllItemResourcesStored() {
		return storageMap.keySet().stream()
				.map(ir -> ItemResourceUtil.findItemResource(ir))
				.filter(Objects::nonNull)
		        .collect(java.util.stream.Collectors.toSet());
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
	public double getItemResourceStored(int resource) {
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
	
	public void destroy() {
		storageMap = null;
		owner = null;
	}
}
